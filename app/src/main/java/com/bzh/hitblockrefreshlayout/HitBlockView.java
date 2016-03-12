package com.bzh.hitblockrefreshlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * ========================================================== <br>
 * <b>版权</b>：　　　音悦台 版权所有(c) 2016 <br>
 * <b>作者</b>：　　　别志华 zhihua.bie@yinyuetai.com<br>
 * <b>创建日期</b>：　16-3-9 <br>
 * <b>描述</b>：　　　<br>
 * <b>版本</b>：　    V1.0 <br>
 * <b>修订历史</b>：　<br/>
 * ========================================================== <br>
 */
public class HitBlockView extends View {

    private static final String TAG = "HitBlockView";

    /**
     * 分割线默认宽度大小
     */
    public static final int DIVIDING_LINE_SIZE = 1;

    /**
     * 游戏状态起始索引
     */
    public static final int GAME_STATUS_DEFAULT_INDEX = 0;

    /**
     * 游戏准备
     */
    public static final int GAME_STATUS_PREPARE = GAME_STATUS_DEFAULT_INDEX;

    /**
     * 游戏开始
     */
    public static final int GAME_STATUS_PLAY = GAME_STATUS_PREPARE + 1;

    /**
     * 游戏结束
     */
    public static final int GAME_STATUS_FINISHED = GAME_STATUS_PLAY + 1;

    /**
     * 游戏死亡
     */
    public static final int GAME_STATUS_OVER = GAME_STATUS_FINISHED + 1;

    /**
     * 默认矩形块竖向排列的数目
     */
    private static final int BLOCK_VERTICAL_NUM = 5;

    /**
     * 默认矩形块横向排列的数目
     */
    private static final int BLOCK_HORIZONTAL_NUM = 3;

    /**
     * 挡板所在位置占屏幕宽度的比率
     */
    private static final float RACKET_POSITION_RATIO = 0.8f;

    /**
     * 矩形块所在位置占屏幕宽度的比率
     */
    private static final float BLOCK_POSITION_RATIO = 0.08f;

    /**
     * 小球默认其实弹射角度
     */
    private static final int DEFAULT_ANGLE = 30;

    /**
     * 小球移动速度
     */
    private static final int SPEED = 6;

    /**
     * 矩形块的高度
     */
    private static final float BLOCK_HEIGHT = 40.f;

    /**
     * 矩形块的宽度
     */
    private static final float BLOCK_WIDTH = 13.f;

    /**
     * 小球半径
     */
    private static final float BALL_RADIUS = 8.f;

    /**
     * 游戏死亡
     */
    private static final String TEXT_GAME_OVER = "Game Over";

    /**
     * 加载中
     */
    private static final String TEXT_LOADING = "Loading...";

    /**
     * 加载结束
     */
    private static final String TEXT_LOADING_FINISHED = "Loading Finished";

    private Paint mPaint;

    private Paint mBlockPaint;

    private TextPaint mTextPaint;

    private float mBlockLeft;

    private float mRacketLeft, mRacketTop, mRacketHeight;

    private float mCx, mCy;

    private int mScreenWidth;

    private List<Point> mPointList;

    private boolean mIsLeft;

    private int mGameStatus = GAME_STATUS_PREPARE;

    private int mAngle;

    private int mBlockHorizontalNum;

    private int mSpeed;

    private int mBlockColor, mBallColor, mRacketColor;

    public HitBlockView(Context context) {
        this(context, null);
    }

    public HitBlockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HitBlockView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public HitBlockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(DIVIDING_LINE_SIZE);

        mBlockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBlockPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.parseColor("#C1C2C2"));
    }

    private void initAttr(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout);
            mBlockHorizontalNum = typedArray.getInt(R.styleable.RefreshLayout_block_horizontal_num, BLOCK_HORIZONTAL_NUM);
            mSpeed = typedArray.getInt(R.styleable.RefreshLayout_ball_speed, SPEED);
            mBlockColor = typedArray.getColor(R.styleable.RefreshLayout_block_color, Color.rgb(255, 255, 255));
            mBallColor = typedArray.getColor(R.styleable.RefreshLayout_ball_color, Color.BLACK);
            mRacketColor = typedArray.getColor(R.styleable.RefreshLayout_racket_color, Color.parseColor("#A5A5A5"));
            typedArray.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mScreenWidth = w;
        mBlockLeft = mScreenWidth * BLOCK_POSITION_RATIO;
        mRacketLeft = mScreenWidth * RACKET_POSITION_RATIO;
        mRacketHeight = BLOCK_HEIGHT * 1.6F;

        initConfigParams(h);
    }

    private void initConfigParams(int h) {
        mCx = mRacketLeft - 2 * BALL_RADIUS;
        mCy = h * 0.5f;
        mRacketTop = 0;
        mAngle = DEFAULT_ANGLE;
        mIsLeft = true;
        if (mPointList == null) {
            mPointList = new ArrayList<>();
        } else {
            mPointList.clear();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = (int) Math.ceil(BLOCK_VERTICAL_NUM * BLOCK_HEIGHT + (BLOCK_VERTICAL_NUM - 1) * DIVIDING_LINE_SIZE + DIVIDING_LINE_SIZE * 2);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawText(canvas);

        drawBoundary(canvas);

        drawColorBlock(canvas);

        drawRacket(canvas);

        if (mGameStatus == GAME_STATUS_PLAY || mGameStatus == GAME_STATUS_FINISHED) {
            drawBallPath(canvas);
        }
    }

    private void drawBallPath(Canvas canvas) {
        mPaint.setColor(mBallColor);

        // 小球进入到色块区域
        if (mCx <= mBlockLeft + mBlockHorizontalNum * BLOCK_WIDTH + (mBlockHorizontalNum - 1) * DIVIDING_LINE_SIZE + BALL_RADIUS) {
            // 反弹回来
            if (checkTouchBlock(mCx, mCy)) {
                mIsLeft = false;
            }
        }

        // 小球穿过色块区域
        if (mCx <= mBlockLeft + BALL_RADIUS) {
            mIsLeft = false;
        }

        //小球当前坐标X值在挡板X值区域范围内
        if (mCx + BALL_RADIUS >= mRacketLeft && mCx - BALL_RADIUS < mRacketLeft + BLOCK_WIDTH) {
            // 小球与挡板接触
            if (checkTouchRacket(mCy)) {
                if (mPointList.size() == mBlockHorizontalNum * BLOCK_VERTICAL_NUM) { // 矩形块全部被消灭，游戏结束
                    mGameStatus = GAME_STATUS_OVER;
                    return;
                }
                mIsLeft = true;
            }
        } else if (mCx > canvas.getWidth()) { // 小球超出挡板区域
            mGameStatus = GAME_STATUS_OVER;
        }


        if (mCy <= BALL_RADIUS + DIVIDING_LINE_SIZE) {
            // 小球撞到上边界
            mAngle = -DEFAULT_ANGLE;
        } else if (mCy >= getHeight() - BALL_RADIUS - DIVIDING_LINE_SIZE) {
            mAngle = DEFAULT_ANGLE;
        }

        if (mIsLeft) {
            mCx -= mSpeed;
        } else {
            mCx += mSpeed;
        }

        mCy -= (float) Math.tan(Math.toRadians(mAngle)) * mSpeed;

        canvas.drawCircle(mCx, mCy, BALL_RADIUS, mPaint);

        invalidate();

    }

    /**
     * 检测小球是否撞击到挡板
     *
     * @param y
     * @return
     */
    private boolean checkTouchRacket(float y) {
        boolean flag = false;
        float diffVal = y - mRacketTop;
        if (diffVal >= 0 && diffVal <= mRacketHeight) { // 小球位于挡板Y值区域范围内
            flag = true;
        }
        return flag;
    }

    /**
     * 检测小球是否撞击到矩形块
     *
     * @param x 小球坐标x
     * @param y 小球坐标y
     * @return
     */
    private boolean checkTouchBlock(float x, float y) {
        int columnX = (int) ((x - mBlockLeft - BALL_RADIUS - mSpeed) / BLOCK_WIDTH);
        columnX = columnX == mBlockHorizontalNum ? columnX - 1 : columnX;

        int rowY = (int) (y / BLOCK_HEIGHT);
        rowY = rowY == BLOCK_VERTICAL_NUM ? rowY - 1 : rowY;

        Point p = new Point();
        p.set(columnX, rowY);

        boolean flag = false;
        for (Point point : mPointList) {
            if (point.equals(p.x, p.y)) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            mPointList.add(p);
        }

        return !flag;
    }


    /**
     * 绘制文字
     */
    private void drawText(Canvas canvas) {
        switch (mGameStatus) {
            case GAME_STATUS_PLAY:
            case GAME_STATUS_PREPARE: {
                mTextPaint.setTextSize(60);
                promptText(canvas, TEXT_LOADING);
            }
            break;
            case GAME_STATUS_FINISHED:
                mTextPaint.setTextSize(50);
                promptText(canvas, TEXT_LOADING_FINISHED);
                break;
            case GAME_STATUS_OVER:
                mTextPaint.setTextSize(60);
                promptText(canvas, TEXT_GAME_OVER);
                break;
        }
    }

    private void promptText(Canvas canvas, String text) {
        float textX = (canvas.getWidth() - mTextPaint.measureText(text)) * .5f;
        float textY = canvas.getHeight() * .5f - (mTextPaint.ascent() + mTextPaint.descent()) * .5f;
        canvas.drawText(text, textX, textY, mTextPaint);
    }

    /**
     * 绘制挡板
     *
     * @param canvas
     */
    private void drawRacket(Canvas canvas) {
        mPaint.setColor(mRacketColor);
        canvas.drawRect(mRacketLeft, mRacketTop, mRacketLeft + BLOCK_WIDTH, mRacketTop + mRacketHeight, mPaint);
    }

    /**
     * 矩形色块
     *
     * @param canvas
     */
    private void drawColorBlock(Canvas canvas) {
        float left, top;
        int column, row;
        int redCode, greenCode, blueCode;

        for (int i = 0; i < mBlockHorizontalNum * BLOCK_VERTICAL_NUM; i++) {
            row = i / mBlockHorizontalNum;
            column = i % mBlockHorizontalNum;

            boolean flag = false;
            for (Point point : mPointList) {
                if (point.equals(column, row)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                continue;
            }

            redCode = (Color.red(mBlockColor) / mBlockHorizontalNum) * column;
            greenCode = (Color.green(mBlockColor) / mBlockHorizontalNum) * column;
            blueCode = (Color.blue(mBlockColor) / mBlockHorizontalNum) * column;
            mBlockPaint.setColor(Color.rgb(redCode, greenCode, blueCode));

            left = mBlockLeft + column * (BLOCK_WIDTH + DIVIDING_LINE_SIZE);
            top = DIVIDING_LINE_SIZE + row * (BLOCK_HEIGHT + DIVIDING_LINE_SIZE);

            canvas.drawRect(left, top, left + BLOCK_WIDTH, top + BLOCK_HEIGHT, mBlockPaint);
        }
    }

    /**
     * 绘制边界
     *
     * @param canvas
     */
    private void drawBoundary(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#606060"));
        canvas.drawLine(0, 0, mScreenWidth, 0, mPaint);
        canvas.drawLine(0, getHeight(), mScreenWidth, getHeight(), mPaint);
    }

    /**
     * 挡板上下移动
     *
     * @param offsetY
     */
    public void moveRacket(float offsetY) {
        float maxDistance = (getHeight() - 2 * DIVIDING_LINE_SIZE - mRacketHeight);

        if (offsetY > maxDistance) {
            offsetY = maxDistance;
        }

        mRacketTop = offsetY;
        postInvalidate();
    }

    public void setGameStatus(int gameStatus) {
        this.mGameStatus = gameStatus;
        if (mGameStatus == GAME_STATUS_FINISHED) {
            initConfigParams(getHeight());
        }
        postInvalidate();
    }
}
