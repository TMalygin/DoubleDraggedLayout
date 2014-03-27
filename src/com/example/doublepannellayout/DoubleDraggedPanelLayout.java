package com.example.doublepannellayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class DoubleDraggedPanelLayout extends FrameLayout implements
		AnimationListener {

	public static final int STATE_NORMAL = 0;
	public static final int STATE_TOP_OPENED = 1;
	public static final int STATE_BOTTOM_OPENED = 2;
	public static final int STATE_ANIMATION = 3;

	private int mState;
	private int mNextState;

	private ImplViewContainer mTop;
	private ImplViewContainer mCenter;
	private ImplViewContainer mBottom;

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

		mTop = new ImplViewContainer();
		mTop.view = getChildAt(0);

		int topHeight = mTop.view.getLayoutParams().height;
		if (topHeight < 0) {
			topHeight = bottomSize;
		}

		mTop.height = (int) (height - bottomSize);
		mTop.normalTop = (int) -(mTop.height - topHeight);
		mTop.normalBottom = (int) topHeight;
		mTop.openedDiffY = mTop.height - topHeight;

		// animation for top
		mTop.opened = new TranslateAnimation(0, 0, 0, mTop.openedDiffY);
		mTop.opened.setDuration(700);
		mTop.opened.setAnimationListener(this);

		mTop.closed = new TranslateAnimation(0, 0, 0, -(mTop.openedDiffY));
		mTop.closed.setDuration(700);
		mTop.closed.setAnimationListener(this);

	}

	/**
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	private void initCenterView(int left, int top, int right, int bottom) {
		mCenter = new ImplViewContainer();
		mCenter.view = getChildAt(1);

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

		mBottom = new ImplViewContainer();
		mBottom.view = getChildAt(2);

		int topHeight = mBottom.view.getLayoutParams().height;
		if (topHeight < 0) {
			topHeight = bottomSize;
		}

		mBottom.height = height;
		// normal
		mBottom.normalTop = height - topHeight;
		mBottom.normalBottom = height + mBottom.normalTop;

		mBottom.openedDiffY = mTop.height - topHeight;

		// animation for top
		mBottom.opened = new TranslateAnimation(0, 0, 0, -mBottom.openedDiffY);
		mBottom.opened.setDuration(700);
		mBottom.opened.setAnimationListener(this);

		mBottom.closed = new TranslateAnimation(0, 0, 0, mBottom.openedDiffY);
		mBottom.closed.setDuration(700);
		mBottom.closed.setAnimationListener(this);

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (getChildCount() != 3) {
			throw new IllegalStateException(
					"DraggedPanelLayout must have 3 children!");
		}
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
			mTop.view.startAnimation(mTop.opened);
			mCenter.view.startAnimation(mTop.opened);
			mBottom.view.startAnimation(mTop.opened);
			break;
		case STATE_BOTTOM_OPENED:
			mState = STATE_ANIMATION;
			mBottom.view.startAnimation(mBottom.opened);
			break;
		case STATE_NORMAL:
			if (mState == STATE_BOTTOM_OPENED) {
				mState = STATE_ANIMATION;
				mBottom.view.startAnimation(mBottom.closed);
			} else {
				mState = STATE_ANIMATION;
				mTop.view.startAnimation(mTop.closed);
				mCenter.view.startAnimation(mTop.closed);
				mBottom.view.startAnimation(mTop.closed);
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
		mBottom.view.clearAnimation();
		mTop.view.clearAnimation();
		mCenter.view.clearAnimation();
		requestLayout();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	private static class ImplViewContainer {
		View view;

		Animation opened;
		Animation closed;

		int height;

		int normalTop, normalBottom;
		int openedDiffY;

	}

}
