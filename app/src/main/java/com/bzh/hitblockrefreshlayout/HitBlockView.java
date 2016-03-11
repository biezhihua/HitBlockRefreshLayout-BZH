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
import android.util.Log;
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

    private boolean mIsleft;

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
//
//    public int getScreenWidth() {
//        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics dm = new DisplayMetrics();
//        manager.getDefaultDisplay().getMetrics(dm);
//        return dm.widthPixels;
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
        mBlockLeft = mScreenWidth * BLOCK_POSITION_RATIO;
        mRacketLeft = mScreenWidth * RACKET_POSITION_RATIO;
        mRacketHeight = BLOCK_HEIGHT * 1.6F;
        mCx = mRacketLeft - 2 * BALL_RADIUS;
        mCy = h * 0.5f;
        mRacketTop = 0;
        mAngle = DEFAULT_ANGLE;
        mIsleft = true;
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

        drawBoundary(canvas);

        drawColorBlock(canvas);

        drawRacket(canvas);
    }


    /**
     * 绘制挡板
     *
     * @param canvas
     */
    private void drawRacket(Canvas canvas) {

    }

    private void drawColorBlock(Canvas canvas) {
        float left, top;
        int column, row, redCode, greenCode, blueCode;
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

    private void drawBoundary(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#606060"));
        canvas.drawLine(0, 0, mScreenWidth, 0, mPaint);
        canvas.drawLine(0, getHeight(), mScreenWidth, getHeight(), mPaint);
    }
}
