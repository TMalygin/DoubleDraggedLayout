package tm.soft.view;

import tm.soft.doubledraggedlayout.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class DraggedView extends ImageButton {

	private int mNormalStateCoordinateY;
	private int mOpenedStateCoordinateY;

	public DraggedView(Context context) {
		super(context);
		init(context, null);
	}

	public DraggedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public DraggedView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context c, AttributeSet attrs) {

		if (attrs == null) {
			return;
		}

		TypedArray a = c.getTheme().obtainStyledAttributes(attrs, R.styleable.DraggedView, 0, 0);
		try {

			mOpenedStateCoordinateY = a.getDimensionPixelSize(R.styleable.DraggedView_opened_position_top_of_layout,
					Integer.MIN_VALUE);
			mNormalStateCoordinateY = a.getDimensionPixelSize(R.styleable.DraggedView_normal_position_top_of_layout,
					Integer.MIN_VALUE);
			if (mOpenedStateCoordinateY == Integer.MIN_VALUE || mNormalStateCoordinateY == Integer.MIN_VALUE) {
				throw new IllegalStateException(
						"DraggedView must have max_position_top_of_layout and min_position_top_of_layout!");
			}

		} finally {
			a.recycle();
		}
	}

	int getOpenedStateCoordY() {
		return mOpenedStateCoordinateY;
	}

	int getNormalStateCoordY() {
		return mNormalStateCoordinateY;
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// super.onTouchEvent(event);
	// switch (event.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// mTouchY = event.getY();
	// break;
	// case MotionEvent.ACTION_MOVE:
	// float delta = event.getY() - mTouchY;
	// if (mDoubleDraggedLayout != null) {
	// if (mDoubleDraggedLayout.getState() ==
	// DoubleDraggedLayout.STATE_ANIMATION) {
	// return true;
	// }
	// mDoubleDraggedLayout.switchState(DoubleDraggedLayout.STATE_DRAGGED);
	// }
	// if (Math.abs(delta) > mSlop) {
	// mIsDragged = true;
	// mCurrentPosY += delta;
	// mTouchY = event.getY();
	// int bottom = mIsUpper ? mOpenedStateCoordinateY :
	// mNormalStateCoordinateY;
	// int top = mIsUpper ? mNormalStateCoordinateY : mOpenedStateCoordinateY;
	// if (mCurrentPosY > bottom) {
	// mCurrentPosY = bottom;
	// } else if (mCurrentPosY < top) {
	// mCurrentPosY = top;
	// }
	// mDoubleDraggedLayout.requestLayout();
	// }
	// break;
	// case MotionEvent.ACTION_UP:
	// if (!mIsDragged) {
	// if (mDoubleDraggedLayout != null) {
	// int state = mIsUpper ? DoubleDraggedLayout.STATE_TOP_OPENED
	// : DoubleDraggedLayout.STATE_BOTTOM_OPENED;
	// mDoubleDraggedLayout
	// .switchState(mDoubleDraggedLayout.getState() ==
	// DoubleDraggedLayout.STATE_NORMAL ? state
	// : DoubleDraggedLayout.STATE_NORMAL);
	// }
	// }
	// mIsDragged = false;
	// break;
	// }
	// return true;
	// }
}
