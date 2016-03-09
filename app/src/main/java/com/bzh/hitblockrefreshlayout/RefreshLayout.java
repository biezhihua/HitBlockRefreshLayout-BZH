package com.bzh.hitblockrefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;

/**
 * ========================================================== <br>
 * <b>版权</b>：　　　音悦台 版权所有(c) 2016 <br>
 * <b>作者</b>：　　　别志华 zhihua.bie@yinyuetai.com<br>
 * <b>创建日期</b>：　16-3-8 <br>
 * <b>描述</b>：　　　<br>
 * <b>版本</b>：　    V1.0 <br>
 * <b>修订历史</b>：　<br/>
 * <p/>
 * 1. RefreshLayout布局内只能允许有一个孩子
 * ========================================================== <br>
 */
public class RefreshLayout extends LinearLayout {

    private static final String TAG = "RefreshLayout";

    private static final long DEFAULT_ANIM_DURATION = 300;

    /**
     * 刷新控件的默认高度
     */
    public static final float DEFAULT_REFRESH_VIEW_HEIGHT = 100;

    /**
     * 下拉状态
     */
    public static final int STATUS_PULL_TO_REFRESH = 0;

    /**
     * 释放准备刷新状态
     */
    public static final int STATUS_RELEASE_TO_REFRESH = STATUS_PULL_TO_REFRESH + 1;

    /**
     * 正在刷新状态
     */
    public static final int STATUS_REFRESHING = STATUS_RELEASE_TO_REFRESH + 1;

    /**
     * 释放后，又按住玩游戏状态
     */
    public static final int STATUS_AGAIN_DOWN = STATUS_REFRESHING + 1;

    /**
     * 刷新完成状态
     */
    public static final int STATUS_REFRESH_FINISHED = STATUS_AGAIN_DOWN + 1;

    /**
     * 下拉拖动的黏性比率
     */
    private static final float STICK_RATIO = .65f;


    private MarginLayoutParams mRefreshHeaderViewLayoutParams;
    private RefreshHeaderView mRefreshHeaderView;
    private View mRefreshView;
    private int mTouchStartY;
    private int mTouchCurrentY;
    private int mScrollPointerId;
    private int mRefreshViewHeight;
    private int mCurrentStatus;
    private ValueAnimator mStartRollbackTopHeaderAnim;
    private ValueAnimator mStartRollbackHeaderAnim;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (getChildCount() > 1) {
            throw new RuntimeException("RefreshLayout View is only one child");
        }
        setOrientation(VERTICAL);
        initAttrs(context, attrs);
        initHeaderView(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initRefreshView();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout, 0, 0);
            mRefreshViewHeight = (int) a.getDimension(R.styleable.RefreshLayout_RefreshViewHeight, d2x(DEFAULT_REFRESH_VIEW_HEIGHT));
            a.recycle();
        }
    }

    private void initHeaderView(Context context, AttributeSet attrs) {
        mRefreshHeaderView = new RefreshHeaderView(context, attrs);
        addView(mRefreshHeaderView, 0);

        mRefreshHeaderViewLayoutParams = (MarginLayoutParams) mRefreshHeaderView.getLayoutParams();
        mRefreshHeaderViewLayoutParams.topMargin = -mRefreshViewHeight;
        mRefreshHeaderView.setLayoutParams(mRefreshHeaderViewLayoutParams);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!isEnabled()) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);
        final int actionIndex = MotionEventCompat.getActionIndex(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScrollPointerId = MotionEventCompat.getPointerId(ev, 0);
                mTouchCurrentY = mTouchStartY = (int) (MotionEventCompat.getY(ev, actionIndex) + 0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mScrollPointerId);
                float currentY = MotionEventCompat.getY(ev, pointerIndex) + 0.5f;
                int dy = (int) currentY - mTouchStartY;
                Log.d(TAG, "onInterceptTouchEvent() called with: currentY  = [" + currentY + "] mTouchStartY = [" + mTouchStartY + "]" + "dy = [" + dy + "] !canChildScrollVerticallyUp()= [" + !canChildScrollVerticallyUp() + "]");
                if (!canChildScrollVerticallyUp() && dy > 0) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }

        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked) {
            case MotionEvent.ACTION_MOVE:
                final int pointerIndex = MotionEventCompat.findPointerIndex(event, mScrollPointerId);
                mTouchCurrentY = (int) (MotionEventCompat.getY(event, pointerIndex) + 0.5f);

                final float distance = mTouchCurrentY - mTouchStartY;
                final float offsetY = distance * STICK_RATIO;

                if (mRefreshHeaderViewLayoutParams.topMargin > 0) {
                    mCurrentStatus = STATUS_RELEASE_TO_REFRESH;
                } else {
                    mCurrentStatus = STATUS_PULL_TO_REFRESH;
                }

                mRefreshHeaderViewLayoutParams.topMargin = (int) (offsetY - mRefreshViewHeight);
                mRefreshHeaderView.setLayoutParams(mRefreshHeaderViewLayoutParams);

                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mCurrentStatus == STATUS_PULL_TO_REFRESH) {
                    startRollbackTopHeaderAnim();
                } else if (mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
                    startRollbackHeaderAnim();
                }
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void startRollbackHeaderAnim() {
        if (mStartRollbackHeaderAnim == null) {
            mStartRollbackHeaderAnim = ValueAnimator.ofFloat(0);
            mStartRollbackHeaderAnim.setDuration(DEFAULT_ANIM_DURATION);
            mStartRollbackHeaderAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mStartRollbackHeaderAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) ((float) animation.getAnimatedValue() + 0.5f);
                    mRefreshHeaderViewLayoutParams.topMargin = value;
                    mRefreshHeaderView.setLayoutParams(mRefreshHeaderViewLayoutParams);
                }
            });
            mStartRollbackHeaderAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mCurrentStatus = STATUS_REFRESH_FINISHED;
                    mRefreshHeaderView.startOpeningAnim(0);
                }
            });
        } else if (mStartRollbackHeaderAnim.isRunning()) {
            mStartRollbackHeaderAnim.cancel();
        }
        mStartRollbackHeaderAnim.setFloatValues(mRefreshHeaderViewLayoutParams.topMargin, 0);
        mStartRollbackHeaderAnim.start();
    }

    private void startRollbackTopHeaderAnim() {
        if (mStartRollbackTopHeaderAnim == null) {
            mStartRollbackTopHeaderAnim = ValueAnimator.ofFloat(0);
            mStartRollbackTopHeaderAnim.setDuration(DEFAULT_ANIM_DURATION);
            mStartRollbackTopHeaderAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mStartRollbackTopHeaderAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) ((float) animation.getAnimatedValue() + 0.5f);
                    mRefreshHeaderViewLayoutParams.topMargin = value;
                    mRefreshHeaderView.setLayoutParams(mRefreshHeaderViewLayoutParams);
                }
            });
            mStartRollbackTopHeaderAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mCurrentStatus = STATUS_REFRESH_FINISHED;
                }
            });
        } else if (mStartRollbackTopHeaderAnim.isRunning()) {
            mStartRollbackTopHeaderAnim.cancel();
        }
        mStartRollbackTopHeaderAnim.setFloatValues(mRefreshHeaderViewLayoutParams.topMargin, -mRefreshViewHeight);
        mStartRollbackTopHeaderAnim.start();
    }

    private boolean canChildScrollVerticallyUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return mRefreshView.getScrollY() > 0;
        } else {
            return ViewCompat.canScrollVertically(mRefreshView, -1);
        }
    }

    private void initRefreshView() {
        View view = null;
        ViewGroup vp = (ViewGroup) getChildAt(1);
        if (vp instanceof AbsListView || vp instanceof RecyclerView) {
            view = vp;
        }
        if (view == null) {
            view = getRefreshView(vp);
        }
        if (view == null) {
            throw new IllegalArgumentException("没有可以滚动的View");
        } else {
            mRefreshView = view;
        }
    }

    private View getRefreshView(ViewGroup vp) {
        if (vp == null) {
            return null;
        }
        for (int i = 0; i < vp.getChildCount(); i++) {
            View temp = vp.getChildAt(i);
            if (temp instanceof AbsListView || temp instanceof RecyclerView) {
                return temp;
            } else if (temp instanceof ViewGroup) {
                return getRefreshView((ViewGroup) temp);
            }
        }
        return null;
    }

    private float d2x(float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getContext().getResources().getDisplayMetrics());
    }

}
