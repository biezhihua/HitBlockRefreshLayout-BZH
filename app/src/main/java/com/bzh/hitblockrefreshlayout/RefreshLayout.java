package com.bzh.hitblockrefreshlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
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
 * <p>
 * 1. RefreshLayout布局内只能允许有一个孩子
 * ========================================================== <br>
 */
public class RefreshLayout extends LinearLayout {

    private static final String TAG = "RefreshLayout";

    public static final float DEFAULT_REFRESH_VIEW_MAX_HEIGHT = 300;    // 可拖动的默认最大值
    public static final float DEFAULT_REFRESH_VIEW_HEIGHT = 100;         // 刷新控件的默认高度

    private int mTouchSlop;
    private int mRefreshViewHeight;

    private RefreshHeaderView mRefreshHeaderView;
    private View mRefreshView;
    private int mTouchStartY;
    private int mTouchCurrentY;
    private int mScrollPointerId;

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
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
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

        MarginLayoutParams layoutParams = (MarginLayoutParams) mRefreshHeaderView.getLayoutParams();
        layoutParams.topMargin = -mRefreshViewHeight;
        mRefreshHeaderView.setLayoutParams(layoutParams);
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
                Log.d(TAG, "onInterceptTouchEvent() called with: " + "ACTION_DOWN ");
                mScrollPointerId = MotionEventCompat.getPointerId(ev, 0);
                mTouchCurrentY = mTouchStartY = (int) (MotionEventCompat.getY(ev, actionIndex) + 0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mScrollPointerId);
                int dy = (int) (MotionEventCompat.getY(ev, pointerIndex) + 0.5f) - mTouchStartY;
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

                Log.d(TAG, "onTouchEvent() called with: " + "ACTION_MOVE mTouchCurrentY = [" + mTouchCurrentY + "]");
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent() called with: " + "ACTION_CANCEL ");
                return true;
        }

        return super.onTouchEvent(event);
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
