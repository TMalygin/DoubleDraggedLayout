package com.example.doublepannellayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

public class DoubleDraggedPanelLayout extends FrameLayout implements
		AnimationListener {

	public static final int STATE_NORMAL = 0;
	public static final int STATE_TOP_OPENED = 1;
	public static final int STATE_BOTTOM_OPENED = 2;
	public static final int STATE_ANIMATION = 3;

	private int mState;
	private int mNextState;

	private final ImplViewContainer mTop = new ImplViewContainer();
	private final ImplViewContainer mCenter = new ImplViewContainer();
	private final ImplViewContainer mBottom = new ImplViewContainer();

	public DoubleDraggedPanelLayout(Context context) {
		super(context);
	}

	public DoubleDraggedPanelLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DoubleDraggedPanelLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	private void initTopView(int left, int top, int right, int bottom) {
		int height = bottom - top;
		int bottomSize = (int) (height * 0.2f);

		int topHeight = mTop.view.getLayoutParams().height;
		if (topHeight < 0) {
			topHeight = bottomSize;
		}

		mTop.height = (height - bottomSize);
		mTop.normalTop = -(mTop.height - topHeight);
		mTop.normalBottom = topHeight;
		mTop.openedDiffY = mTop.height - topHeight;

	}

	/**
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	private void initCenterView(int left, int top, int right, int bottom) {

		mCenter.height = mBottom.normalTop - mTop.normalBottom;
		mCenter.normalTop = mTop.normalBottom;
		mCenter.normalBottom = mBottom.normalTop;

	}

	/**
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	private void initBottomView(int left, int top, int right, int bottom) {
		int height = bottom - top;
		int bottomSize = (int) (height * 0.2f);

		int topHeight = mBottom.view.getLayoutParams().height;
		if (topHeight < 0) {
			topHeight = bottomSize;
		}

		mBottom.height = height;
		// normal
		mBottom.normalTop = height - topHeight;
		mBottom.normalBottom = height + mBottom.normalTop;

		mBottom.openedDiffY = mBottom.height - topHeight;

	}

	/**
	 * 
	 * @param fromY
	 * @param toY
	 * @param v
	 */
	private void startAnimation(float fromY, float toY, final View v) {
		Animation anim = new TranslateAnimation(0, 0, fromY, toY);
		anim.setDuration(700);
		// fixed: blink after animation
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.clearAnimation();
				mState = mNextState;
				requestLayout();
			}
		});
		v.startAnimation(anim);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		long startTime = System.currentTimeMillis();
		if (getChildCount() != 3) {
			throw new IllegalStateException(
					"DraggedPanelLayout must have 3 children!");
		}

		mTop.view = getChildAt(0);
		mCenter.view = getChildAt(1);
		mBottom.view = getChildAt(2);

		if (changed) {
			initTopView(left, top, right, bottom);
			initBottomView(left, top, right, bottom);
			initCenterView(left, top, right, bottom);
		}

		switch (mState) {
		case STATE_TOP_OPENED:
			mTop.view.layout(left, mTop.normalTop + mTop.openedDiffY, right,
					mTop.normalBottom + mTop.openedDiffY);
			mCenter.view.layout(left, mCenter.normalTop + mTop.openedDiffY,
					right, mCenter.normalBottom + mTop.openedDiffY);
			mBottom.view.layout(left, mBottom.normalTop + mTop.openedDiffY,
					right, mBottom.normalBottom + mTop.openedDiffY);
			break;
		case STATE_BOTTOM_OPENED:
			mTop.view.layout(left, mTop.normalTop, right, mTop.normalBottom);
			mCenter.view.layout(left, mCenter.normalTop, right,
					mCenter.normalBottom);
			mBottom.view.layout(left, top, right, bottom);
			break;
		case STATE_NORMAL:
			mTop.view.layout(left, mTop.normalTop, right, mTop.normalBottom);
			mCenter.view.layout(left, mCenter.normalTop, right,
					mCenter.normalBottom);
			mBottom.view.layout(left, mBottom.normalTop, right,
					mBottom.normalBottom);
			break;
		}
		Log.v("", "time " + (System.currentTimeMillis() - startTime));
	}

	public int getState() {
		return mState;
	}

	public void switchState(int state) {
		if (mState == STATE_ANIMATION || state == mState)
			return;
		mNextState = state;
		switch (state) {
		case STATE_TOP_OPENED:
			mState = STATE_ANIMATION;

			startAnimation(0, mTop.openedDiffY, mTop.view);
			startAnimation(0, mTop.openedDiffY, mCenter.view);
			startAnimation(0, mTop.openedDiffY, mBottom.view);

			break;
		case STATE_BOTTOM_OPENED:
			mState = STATE_ANIMATION;
			startAnimation(0, -mBottom.openedDiffY, mBottom.view);
			break;
		case STATE_NORMAL:
			if (mState == STATE_BOTTOM_OPENED) {
				mState = STATE_ANIMATION;
				startAnimation(0, mBottom.openedDiffY, mBottom.view);
			} else {
				mState = STATE_ANIMATION;

				startAnimation(0, -mTop.openedDiffY, mTop.view);
				startAnimation(0, -mTop.openedDiffY, mCenter.view);
				startAnimation(0, -mTop.openedDiffY, mBottom.view);
			}
			break;
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		mState = mNextState;
		getChildAt(0).clearAnimation();
		getChildAt(1).clearAnimation();
		getChildAt(2).clearAnimation();
		animation.setAnimationListener(null);
		requestLayout();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	private static class ImplViewContainer {
		View view;

		int height;
		int normalTop, normalBottom;
		int openedDiffY;

	}

}
