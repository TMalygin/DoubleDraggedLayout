package tm.soft.view;

import tm.soft.doubledraggedlayout.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

public class DraggedView extends ImageView {

	private int mSlop;
	private float mTouchY;
	private DoubleDraggedLayout mDoubleDraggedLayout;
	private int mNormalStateCoordinateY;
	private int mOpenedStateCoordinateY;
	private int mCurrentPosY;
	private int mParentHeight = -1;
	private boolean mIsUpper;

	private boolean mIsDragged;

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
		final ViewConfiguration configuration = ViewConfiguration.get(c);
		mSlop = configuration.getScaledTouchSlop();

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

			mIsUpper = mNormalStateCoordinateY < mOpenedStateCoordinateY;
			mCurrentPosY = mNormalStateCoordinateY;
			mParentHeight = a.getDimensionPixelSize(R.styleable.DraggedView_parent_layout_height, -1);

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

	int getCurrentPosY() {
		return mCurrentPosY;
	}

	void setCurrentPosY(int currentPosY) {
		this.mCurrentPosY = currentPosY;
	}

	int getParentHeight() {
		return mParentHeight;
	}

	void setDoubleDraggedLayout(DoubleDraggedLayout doubleDraggedLayout) {
		this.mDoubleDraggedLayout = doubleDraggedLayout;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float delta = event.getY() - mTouchY;
			if (Math.abs(delta) > mSlop) {
				mIsDragged = true;
				mCurrentPosY += delta;
				mTouchY = event.getY();
				int bottom = mIsUpper ? mOpenedStateCoordinateY : mNormalStateCoordinateY;
				int top = mIsUpper ? mNormalStateCoordinateY : mOpenedStateCoordinateY;
				if (mCurrentPosY > bottom) {
					mCurrentPosY = bottom;
				} else if (mCurrentPosY < top) {
					mCurrentPosY = top;
				}
				mDoubleDraggedLayout.requestLayout();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (!mIsDragged) {
				if (mDoubleDraggedLayout != null) {
					int state = mIsUpper ? DoubleDraggedLayout.STATE_TOP_OPENED
							: DoubleDraggedLayout.STATE_BOTTOM_OPENED;
					mDoubleDraggedLayout
							.switchState(mDoubleDraggedLayout.getState() == DoubleDraggedLayout.STATE_NORMAL ? state
									: DoubleDraggedLayout.STATE_NORMAL);
				}
			}
			mIsDragged = false;
			break;
		}
		return true;
	}
}
