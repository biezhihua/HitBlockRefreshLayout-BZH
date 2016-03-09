package com.bzh.hitblockrefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class RefreshHeaderView extends FrameLayout {

    private int mRefreshViewHeight;

    private static final long OPENING_ANIM_DURATION = 500;
    private RelativeLayout mMaskShadowLayout;     // 灰色背景
    private RelativeLayout mCurtainLayout;  // 窗帘

    private TextView mTopMaskView;
    private TextView mBottomMaskView;
    private int mHalfCurtainHeight;
    private AnimatorSet mStartOpeningAnim;

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout, 0, 0);
            mRefreshViewHeight = (int) a.getDimension(R.styleable.RefreshLayout_RefreshViewHeight, d2x(RefreshLayout.DEFAULT_REFRESH_VIEW_HEIGHT));
            a.recycle();
        }

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mRefreshViewHeight);
        setLayoutParams(lp);

        initMainLayout(context);

        initCurtain(context);
    }

    /**
     * 1. 初始化上下窗帘
     * 2. 计算单个窗帘的高度
     * 3. 初始化窗帘并加入到布局中
     */
    private void initCurtain(Context context) {

        mTopMaskView = getMaskTextView(context, "Pull to Break Out!", 20, Gravity.BOTTOM);
        mBottomMaskView = getMaskTextView(context, "Scroll to move handle!", 18, Gravity.TOP);

        mHalfCurtainHeight = (int) (mRefreshViewHeight * 0.5f);

        RelativeLayout.LayoutParams topLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHalfCurtainHeight);
        RelativeLayout.LayoutParams bottomLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHalfCurtainHeight);
        bottomLp.topMargin = mHalfCurtainHeight;
        mCurtainLayout.removeAllViews();
        mCurtainLayout.addView(mTopMaskView, 0,topLp);
        mCurtainLayout.addView(mBottomMaskView, 1, bottomLp);
    }

    /**
     * 两层布局：一个是阴影背景；一个是窗帘
     */
    private void initMainLayout(Context context) {

        mCurtainLayout = new RelativeLayout(context);
        mMaskShadowLayout = new RelativeLayout(context);
        mMaskShadowLayout.setBackgroundColor(Color.parseColor("#3A3A3A"));

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.topMargin = (int) HitBlockView.DIVIDING_LINE_SIZE;
        lp.bottomMargin = (int) HitBlockView.DIVIDING_LINE_SIZE;

        addView(mMaskShadowLayout, lp);
        addView(mCurtainLayout, lp);
    }

    private TextView getMaskTextView(Context context, String text, int textSize, int gravity) {
        TextView maskTextView = new TextView(context);
        maskTextView.setTextColor(Color.BLACK);
        maskTextView.setBackgroundColor(Color.WHITE);
        maskTextView.setGravity(gravity | Gravity.CENTER_HORIZONTAL);
        maskTextView.setTextSize(textSize);
        maskTextView.setText(text);
        return maskTextView;
    }

    public float d2x(float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getContext().getResources().getDisplayMetrics());
    }

    public void startOpeningAnim(long delay) {
        if (mStartOpeningAnim == null) {
            ObjectAnimator topMaskAnim = ObjectAnimator.ofFloat(mTopMaskView, "translationY", mTopMaskView.getTranslationY(), -mHalfCurtainHeight);
            ObjectAnimator bottomMaskAnim = ObjectAnimator.ofFloat(mBottomMaskView, "translationY", mBottomMaskView.getTranslationY(), mHalfCurtainHeight);
            ObjectAnimator maskShadowAnim = ObjectAnimator.ofFloat(mMaskShadowLayout, "alpha", mMaskShadowLayout.getAlpha(), 0);

            mStartOpeningAnim = new AnimatorSet();
            mStartOpeningAnim.play(topMaskAnim).with(bottomMaskAnim).with(maskShadowAnim);
            mStartOpeningAnim.setDuration(OPENING_ANIM_DURATION);
            mStartOpeningAnim.setStartDelay(delay);
            mStartOpeningAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mTopMaskView.setVisibility(View.GONE);
                    mBottomMaskView.setVisibility(View.GONE);
                    mMaskShadowLayout.setVisibility(View.GONE);
                }
            });
        } else if (mStartOpeningAnim.isRunning()) {
            mStartOpeningAnim.cancel();
        }
        mStartOpeningAnim.start();
    }

}
