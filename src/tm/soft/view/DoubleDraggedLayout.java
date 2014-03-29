package tm.soft.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

public class DoubleDraggedLayout extends FrameLayout {

	public static final int STATE_NORMAL = 0;
	public static final int STATE_TOP_OPENED = 1;
	public static final int STATE_BOTTOM_OPENED = 2;
	public static final int STATE_ANIMATION = 3;
	public static final int STATE_DRAGGED = 4;

	private int mState;
	private int mNextState;

	private final ImplViewContainer mTop = new ImplViewContainer();
	private final ImplViewContainer mCenter = new ImplViewContainer();
	private final ImplViewContainer mBottom = new ImplViewContainer();
	private VelocityTracker mVelocityTracker;

	private boolean isBeingDragged = false;
	private int mTouchedView = -1;
	private int mSlop;
	private float mTouchY;
	private boolean mIsDragged = false;
	

	public DoubleDraggedLayout(Context context) {
		super(context);
		init(context);
	}

	public DoubleDraggedLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DoubleDraggedLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context c) {
		final ViewConfiguration configuration = ViewConfiguration.get(c);
		mSlop = configuration.getScaledTouchSlop();
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
		mCenter.currentBottom = mCenter.normalBottom;
		mCenter.currentTop = mCenter.normalBottom;

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
		mBottom.currentTop = mBottom.normalTop;
		mBottom.currentBottom = mBottom.normalBottom;

	}

	/**
	 * 
	 * @param fromY
	 * @param toY
	 * @param v
	 */
	private void startAnimation(float fromY, float toY, final View v,
			int duration) {

		mState = STATE_ANIMATION;
		Animation anim = new TranslateAnimation(0, 0, fromY, toY);
		anim.setDuration(duration);
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
		case STATE_DRAGGED:
			mTop.view.layout(left, mTop.currentTop, right, mTop.currentBottom);
			mCenter.view.layout(left, mCenter.currentTop, right,
					mCenter.currentBottom);
			mBottom.view.layout(left, mBottom.currentTop, right,
					mBottom.currentBottom);
			break;
		}
		Log.v("", "time " + (System.currentTimeMillis() - startTime));
	}

	/**
	 * @return
	 */
	public int getState() {
		return mState;
	}

	/**
	 * @param state
	 */
	public void switchState(int state) {
		if (mState == STATE_ANIMATION || mState == STATE_DRAGGED
				|| state == mState) {
			return;
		}
		mNextState = state;
		switch (state) {
		case STATE_TOP_OPENED:
			startAnimation(0, mTop.openedDiffY, mTop.view, 300);
			startAnimation(0, mTop.openedDiffY, mCenter.view, 300);
			startAnimation(0, mTop.openedDiffY, mBottom.view, 300);
			break;
		case STATE_BOTTOM_OPENED:
			startAnimation(0, -mBottom.openedDiffY, mBottom.view, 300);
			break;
		case STATE_NORMAL:
			if (mState == STATE_BOTTOM_OPENED) {
				startAnimation(0, mBottom.openedDiffY, mBottom.view, 300);
			} else {
				startAnimation(0, -mTop.openedDiffY, mTop.view, 300);
				startAnimation(0, -mTop.openedDiffY, mCenter.view, 300);
				startAnimation(0, -mTop.openedDiffY, mBottom.view, 300);
			}
			break;
		}
	}

	private int getTouchedViewPosition(float x, float y) {
		if (mState != STATE_BOTTOM_OPENED) {
			int bottom = mTop.view.getBottom();
			int top = bottom - 200;
			if (bottom >= y && top <= y) {
				return 0;
			}
		}
		int top = mBottom.view.getTop();
		int bottom = top + 200;
		if (bottom >= y && top <= y) {
			return 2;
		}
		return -1;
	}

	private void startDragged(MotionEvent event) {
		mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(event);
		mState = STATE_DRAGGED;

		mTop.currentTop = mTop.view.getTop();
		mTop.currentBottom = mTop.view.getBottom();

		mCenter.currentTop = mCenter.view.getTop();
		mCenter.currentBottom = mCenter.view.getBottom();

		mBottom.currentTop = mBottom.view.getTop();
		mBottom.currentBottom = mBottom.view.getBottom();

	}

	/**
	 * 
	 * @param deltaY
	 */
	private void draggedTopView(float deltaY) {

		mIsDragged = true;
		mTop.currentTop = (int) (mTop.view.getTop() + deltaY);

		if (mTop.currentTop > 0) {
			mTop.currentTop = 0;
		} else if (mTop.currentTop < mTop.normalTop) {
			mTop.currentTop = mTop.normalTop;
		}

		mTop.currentBottom = mTop.currentTop + mTop.height;

		mCenter.currentTop = mTop.currentBottom;
		mCenter.currentBottom = mCenter.currentTop + mCenter.height;

		mBottom.currentTop = mCenter.currentBottom;
		mBottom.currentBottom = mBottom.currentTop + mBottom.height;

		requestLayout();

	}

	private void draggedBottomView(float deltaY) {
		mIsDragged = true;
		mBottom.currentTop = (int) (mBottom.view.getTop() + deltaY);
		if (mBottom.currentTop < 0) {
			mBottom.currentTop = 0;
		} else if (mBottom.currentTop > mBottom.normalTop) {
			mBottom.currentTop = mBottom.normalTop;
		}
		mBottom.currentBottom = mBottom.currentTop + mBottom.height;
		requestLayout();
	}

	/**
	 * 
	 * @param velocityY
	 */
	private void finishTopDragged(float velocityY) {
		final boolean flinging = Math.abs(velocityY) > 0.5;
		if (flinging) {

			if (velocityY > 0) {
				mNextState = STATE_TOP_OPENED;
				int delta = (mTop.normalBottom + mTop.openedDiffY)
						- mTop.currentBottom;
				int duration = Math.abs(Math.round(delta / velocityY));
				startAnimation(0, delta, mTop.view, duration);
				startAnimation(0, delta, mCenter.view, duration);
				startAnimation(0, delta, mBottom.view, duration);
			} else {
				mNextState = STATE_NORMAL;
				int delta = mTop.normalTop - mTop.currentTop;
				int duration = Math.abs(Math.round(delta / velocityY));
				startAnimation(0, delta, mTop.view, duration);
				startAnimation(0, delta, mCenter.view, duration);
				startAnimation(0, delta, mBottom.view, duration);
			}
		} else {
			if ((mTop.currentBottom - mTop.normalBottom) > (-mTop.normalTop / 2)) {
				mNextState = STATE_TOP_OPENED;
				int delta = (mTop.normalBottom + mTop.openedDiffY)
						- mTop.currentBottom;

				startAnimation(0, delta, mTop.view, 300);
				startAnimation(0, delta, mCenter.view, 300);
				startAnimation(0, delta, mBottom.view, 300);
			} else {
				mNextState = STATE_NORMAL;
				int delta = mTop.normalTop - mTop.currentTop;
				startAnimation(0, delta, mTop.view, 300);
				startAnimation(0, delta, mCenter.view, 300);
				startAnimation(0, delta, mBottom.view, 300);
			}
		}
	}

	/**
	 * 
	 * @param velocityY
	 */
	private void finishBottomDragged(float velocityY) {
		final boolean flinging = Math.abs(velocityY) > 0.5;
		if (flinging) {
			if (velocityY > 0) {
				mNextState = STATE_NORMAL;
				int delta = mBottom.normalBottom - mBottom.currentBottom;
				int duration = Math.abs(Math.round(delta / velocityY));
				startAnimation(0, delta, mBottom.view, duration);
			} else {
				mNextState = STATE_BOTTOM_OPENED;
				int delta = -mBottom.currentTop;
				int duration = Math.abs(Math.round(delta / velocityY));
				startAnimation(0, delta, mBottom.view, duration);
			}
		} else {
			if ((mBottom.normalTop - mBottom.currentTop) > (mBottom.height / 2)) {
				mNextState = STATE_BOTTOM_OPENED;
				int delta = -mBottom.currentTop;
				startAnimation(0, delta, mBottom.view, 300);
			} else {
				mNextState = STATE_NORMAL;
				int delta = mBottom.normalBottom - mBottom.currentBottom;
				startAnimation(0, delta, mBottom.view, 300);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO: add code for Anroid API >11! This version for API 8
		if (mState == STATE_ANIMATION) {
			return true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startDragged(event);
			Log.v("", "down touch event");
			break;
		case MotionEvent.ACTION_MOVE:
			Log.v("", "move touch event");
			if (mVelocityTracker == null)
				startDragged(event);

			if (Math.abs(event.getY() - mTouchY) > mSlop) {
				if (mTouchedView == 0) {
					startDragged(event);
					isBeingDragged = true;
				} else if (mTouchedView == 2) {
					startDragged(event);
					isBeingDragged = true;
				}

				mVelocityTracker.addMovement(event);
				float deltaY = event.getY() - mTouchY;
				mTouchY = event.getY();
				if (mTouchedView == 0) {
					draggedTopView(deltaY);
				} else if (mTouchedView == 2) {
					draggedBottomView(deltaY);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.v("", "up touch event");
			if (mIsDragged && mVelocityTracker != null) {
				mVelocityTracker.addMovement(event);
				mVelocityTracker.computeCurrentVelocity(1);
				float velocityY = mVelocityTracker.getYVelocity() > 0 ? 0.6f
						: -0.6f;
				mVelocityTracker.recycle();
				mVelocityTracker = null;
				if (mTouchedView == 0) {
					finishTopDragged(velocityY);
				} else if (mTouchedView == 2) {
					finishBottomDragged(velocityY);
				}
			}
			isBeingDragged = false;
			mIsDragged = false;
			mTouchedView = -1;

			break;
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mState == STATE_ANIMATION) {
			return false;
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isBeingDragged = false;
			mTouchY = ev.getY();
			mTouchedView = getTouchedViewPosition(ev.getX(), mTouchY);
			break;
		case MotionEvent.ACTION_UP:
			Log.v("", "up intercept touch event");
			isBeingDragged = false;
			break;
		}
		return isBeingDragged;
	}

	/**
	 * @author timofeymalygin
	 */
	private static class ImplViewContainer {
		View view;

		int height;
		int normalTop, normalBottom;
		int currentTop, currentBottom;
		int openedDiffY;

	}

}