package tm.soft.view;

import tm.soft.doubledraggedlayout.R;
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

/**
 * 
 * @author timofey.malygin
 * 
 */
public class DoubleDraggedLayout extends FrameLayout {

	private static final int DURATION = 500;
	public static final int STATE_NORMAL = 0;
	public static final int STATE_TOP_OPENED = 1;
	public static final int STATE_BOTTOM_OPENED = 2;
	public static final int STATE_ANIMATION = 3;
	public static final int STATE_DRAGGED_TOP = 4;
	public static final int STATE_DRAGGED_BOTTOM = 5;

	private volatile int mState;
	private int mNextState;

	private final ImplViewContainer mTop = new ImplViewContainer();
	private final ImplViewContainer mCenter = new ImplViewContainer();
	private final ImplViewContainer mBottom = new ImplViewContainer();
	private VelocityTracker mVelocityTracker;
	private float mTouchY;
	private int mTouchedView;
	private boolean isBeingDragged;
	private boolean mIsDragged;
	private float mSlop;

	public DoubleDraggedLayout(Context context) {
		super(context);
		init(context);
	}

	public DoubleDraggedLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DoubleDraggedLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context c) {
		final ViewConfiguration configuration = ViewConfiguration.get(c);
		mSlop = configuration.getScaledTouchSlop();

	}

	/**
	 * 
	 * @param fromY
	 * @param toY
	 * @param v
	 */
	private void startAnimation(float fromY, float toY, final View v, int duration) {
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
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (getChildCount() != 3) {
			throw new IllegalStateException("DraggedPanelLayout must have 3 children!");
		}

		if (changed) {
			mTop.init(findViewById(R.id.top_layout));
			mBottom.init(findViewById(R.id.bottom_layout));
			mCenter.init(findViewById(R.id.center_layout));

			mTop.draggedView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					switch (mState) {
					case STATE_NORMAL:
						switchState(STATE_TOP_OPENED);
						break;
					case STATE_TOP_OPENED:
						switchState(STATE_NORMAL);
						break;
					}
				}
			});

			mBottom.draggedView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (mState) {
					case STATE_NORMAL:
						switchState(STATE_BOTTOM_OPENED);
						break;
					case STATE_BOTTOM_OPENED:
						switchState(STATE_NORMAL);
						break;
					}
				}
			});

			mTop.draggedView.setDraggedListener(new DraggedListener() {

				@Override
				public void stopDragged() {
					Log.v("", "stopDragged");
					finishTopDragged();
				}

				@Override
				public void startDragged() {
					Log.v("", "startDragged");
					mTop.currentTop = mState == STATE_NORMAL ? mTop.draggedView.getNormalStateCoordY()
							: mTop.draggedView.getOpenedStateCoordY();
					mState = STATE_DRAGGED_TOP;
				}

				@Override
				public void dragged(float delta) {
					Log.v("", "dragged " + delta);
					mTop.currentTop += delta;
					if (mTop.currentTop < mTop.draggedView.getNormalStateCoordY()) {
						mTop.currentTop = mTop.draggedView.getNormalStateCoordY();
					} else if (mTop.currentTop > mTop.draggedView.getOpenedStateCoordY()) {
						mTop.currentTop = mTop.draggedView.getOpenedStateCoordY();
					}
					requestLayout();
				}
			});

			mBottom.draggedView.setDraggedListener(new DraggedListener() {

				@Override
				public void stopDragged() {
					finishBottomDragged();
				}

				@Override
				public void startDragged() {
					mBottom.currentTop = mState == STATE_NORMAL ? mBottom.draggedView.getNormalStateCoordY()
							: mBottom.draggedView.getOpenedStateCoordY();
					mState = STATE_DRAGGED_BOTTOM;
				}

				@Override
				public void dragged(float delta) {
					Log.v("", "dragged " + delta);
					mBottom.currentTop += delta;
					if (mBottom.currentTop > mBottom.draggedView.getNormalStateCoordY()) {
						mBottom.currentTop = mBottom.draggedView.getNormalStateCoordY();
					} else if (mBottom.currentTop < mBottom.draggedView.getOpenedStateCoordY()) {
						mBottom.currentTop = mBottom.draggedView.getOpenedStateCoordY();
					}
					requestLayout();
				}
			});
		}

		int layoutHeight = bottom - top;

		int topHeight = mTop.view.getMeasuredHeight();
		if (topHeight <= 0) {
			topHeight = layoutHeight;
		}

		int bottomHeight = mBottom.view.getMeasuredHeight();
		if (bottomHeight <= 0) {
			bottomHeight = layoutHeight;
		}

		int centerHeight = mBottom.draggedView.getNormalStateCoordY()
				- (mTop.draggedView.getNormalStateCoordY() + topHeight);

		switch (mState) {
		case STATE_TOP_OPENED:
			int topTopOpenedTop = mTop.draggedView.getOpenedStateCoordY();
			int topTopOpenedBottom = topTopOpenedTop + topHeight;
			int centerTopOpennedBottom = topTopOpenedBottom + centerHeight;

			mTop.view.layout(left, topTopOpenedTop, right, topTopOpenedBottom);
			mCenter.view.layout(left, topTopOpenedBottom, right, centerTopOpennedBottom);
			mBottom.view.layout(left, centerTopOpennedBottom, right, centerTopOpennedBottom + bottomHeight);

			break;
		case STATE_BOTTOM_OPENED:
			int topBottomOpenedTop = mTop.draggedView.getNormalStateCoordY();
			int centerBottomOpenedTop = topBottomOpenedTop + topHeight;
			int bottomBottomOpenedTop = mBottom.draggedView.getOpenedStateCoordY();

			mTop.view.layout(left, topBottomOpenedTop, right, centerBottomOpenedTop);
			mCenter.view.layout(left, centerBottomOpenedTop, right, centerBottomOpenedTop + centerHeight);
			mBottom.view.layout(left, bottomBottomOpenedTop, right, bottomBottomOpenedTop + bottomHeight);

			break;
		case STATE_NORMAL:
			int topNormalTop = mTop.draggedView.getNormalStateCoordY();
			int topNormalBottom = topNormalTop + topHeight;
			int bottomNormalTop = mBottom.draggedView.getNormalStateCoordY();

			mTop.view.layout(left, topNormalTop, right, topNormalBottom);
			mCenter.view.layout(left, topNormalBottom, right, bottomNormalTop);
			mBottom.view.layout(left, bottomNormalTop, right, bottomNormalTop + bottomHeight);

			break;
		case STATE_DRAGGED_TOP:
			int topDraggedBottom = mTop.currentTop + topHeight;
			int bottomDraggedTop = topDraggedBottom + centerHeight;

			mTop.view.layout(left, mTop.currentTop, right, topDraggedBottom);
			mCenter.view.layout(left, topDraggedBottom, right, bottomDraggedTop);
			mBottom.view.layout(left, bottomDraggedTop, right, bottomDraggedTop + bottomHeight);
			break;
		case STATE_DRAGGED_BOTTOM:
			int topDraggedBottomTop = mTop.draggedView.getNormalStateCoordY();
			int topDraggedBottomBottom = topDraggedBottomTop + topHeight;
			int bottomDraggedBottomTop = mBottom.draggedView.getNormalStateCoordY();

			mTop.view.layout(left, topDraggedBottomTop, right, topDraggedBottomBottom);
			mCenter.view.layout(left, topDraggedBottomBottom, right, bottomDraggedBottomTop);
			mBottom.view.layout(left, mBottom.currentTop, right, mBottom.currentTop + bottomHeight);
			break;
		}
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
		if (mState == STATE_ANIMATION || mState == STATE_DRAGGED_TOP || state == mState) {
			return;
		}
		int lastState = mState;
		mState = STATE_ANIMATION;
		mNextState = state;
		switch (state) {
		case STATE_TOP_OPENED:
			int diffTop = mTop.draggedView.getOpenedStateCoordY() - mTop.draggedView.getNormalStateCoordY();
			startAnimation(0, diffTop, mTop.view, DURATION);
			startAnimation(0, diffTop, mCenter.view, DURATION);
			startAnimation(0, diffTop, mBottom.view, DURATION);
			break;
		case STATE_BOTTOM_OPENED:
			int diffBottom = mBottom.draggedView.getOpenedStateCoordY() - mBottom.draggedView.getNormalStateCoordY();
			startAnimation(0, diffBottom, mBottom.view, DURATION);
			break;
		case STATE_NORMAL:
			if (lastState == STATE_BOTTOM_OPENED) {
				int diffBottomToNormal = mBottom.draggedView.getOpenedStateCoordY()
						- mBottom.draggedView.getNormalStateCoordY();
				startAnimation(0, -diffBottomToNormal, mBottom.view, DURATION);
			} else {
				int diffTopToNormal = mTop.draggedView.getNormalStateCoordY() + mTop.draggedView.getOpenedStateCoordY();
				startAnimation(0, diffTopToNormal, mTop.view, DURATION);
				startAnimation(0, diffTopToNormal, mCenter.view, DURATION);
				startAnimation(0, diffTopToNormal, mBottom.view, DURATION);
			}
			break;
		default:
			mState = state;
			break;
		}
	}

	/**
	 * 
	 */
	void finishTopDragged() {
		int hiddenHeight = -mTop.draggedView.getNormalStateCoordY();
		mState = STATE_ANIMATION;
		int delta = (mTop.draggedView.getNormalStateCoordY() - mTop.currentTop);

		if (Math.abs(delta) > hiddenHeight / 2) {
			mNextState = STATE_TOP_OPENED;
			delta = (mTop.draggedView.getOpenedStateCoordY() - mTop.currentTop);
		} else {
			mNextState = STATE_NORMAL;
		}

		startAnimation(0, delta, mTop.view, DURATION);
		startAnimation(0, delta, mCenter.view, DURATION);
		startAnimation(0, delta, mBottom.view, DURATION);
	}

	void finishBottomDragged() {
		int hiddenHeight = mBottom.draggedView.getNormalStateCoordY();
		mState = STATE_ANIMATION;
		int delta = (mBottom.draggedView.getNormalStateCoordY() - mBottom.currentTop);

		if (Math.abs(delta) > hiddenHeight / 2) {
			mNextState = STATE_BOTTOM_OPENED;
			delta = (mBottom.draggedView.getOpenedStateCoordY() - mBottom.currentTop);
		} else {
			mNextState = STATE_NORMAL;
		}

		startAnimation(0, delta, mBottom.view, DURATION);
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// super.onTouchEvent(event);
	// // TODO: This version for API 8
	// if (mState == STATE_ANIMATION) {
	// return true;
	// }

	// switch (event.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// // startDragged(event);
	// break;
	// case MotionEvent.ACTION_MOVE:
	// if (isBeingDragged) {
	// return true;
	// }
	//
	// // if (mVelocityTracker == null)
	// // startDragged(event);
	//
	// float deltaY = event.getY() - mTouchY;
	// mTouchY = event.getY();
	//
	// if (Math.abs(deltaY) > mSlop) {
	// mVelocityTracker.addMovement(event);
	//
	// if (mTouchedView == R.id.top_layout) {
	// mTop.currentTop += deltaY;
	// requestLayout();
	// } else if (mTouchedView == R.id.bottom_layout) {
	// }
	// }
	// break;
	// case MotionEvent.ACTION_UP:
	// if (mIsDragged && mVelocityTracker != null) {
	// mVelocityTracker.addMovement(event);
	// mVelocityTracker.computeCurrentVelocity(1);
	// float velocityY = mVelocityTracker.getYVelocity();
	// mVelocityTracker.recycle();
	// mVelocityTracker = null;
	// }
	// isBeingDragged = false;
	// mIsDragged = false;
	// mTouchedView = -1;
	//
	// break;
	// }
	// return false;
	// }

	// /**
	// *
	// * @param x
	// * @param y
	// * @return
	// */
	// private int getTouchedViewPosition(float x, float y) {
	// if (mState != STATE_BOTTOM_OPENED) {
	// if (mTop.draggedView.isTouched(x, y)) {
	// mTop.draggedView.setDrawableState(android.R.attr.state_pressed);
	// return R.id.top_layout;
	// }
	// }
	//
	// if (mBottom.draggedView.isTouched(x, y)) {
	// mBottom.draggedView.setDrawableState(android.R.attr.state_pressed);
	// return R.id.bottom_layout;
	// }
	// return R.id.center_layout;
	// }

	// @Override
	// public boolean onInterceptTouchEvent(MotionEvent ev) {
	// super.onInterceptTouchEvent(ev);
	// if (mState == STATE_ANIMATION) {
	// return false;
	// }
	// switch (ev.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// mTouchY = ev.getY();
	// mTouchedView = R.id.center_layout;//getTouchedViewPosition(ev.getX(),
	// mTouchY);
	// isBeingDragged = mTouchedView == R.id.top_layout || mTouchedView ==
	// R.id.bottom_layout;
	// break;
	// case MotionEvent.ACTION_UP:
	// isBeingDragged = false;
	// break;
	// }
	// return isBeingDragged;
	// }

	/**
	 * @author timofey.malygin
	 */
	private static class ImplViewContainer {
		View view;
		DraggedView draggedView;

		int currentTop;

		void init(View v) {
			view = v;
			draggedView = (DraggedView) v.findViewById(R.id.dragged_view);
		}
	}

}
