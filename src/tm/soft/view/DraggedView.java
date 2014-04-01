package tm.soft.view;

import tm.soft.doubledraggedlayout.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageButton;

public class DraggedView extends ImageButton {

	private int mNormalStateCoordinateY;
	private int mOpenedStateCoordinateY;
	private DraggedListener mDraggedListener;

	private float mTouchY;
	private int mSlop;
	private volatile boolean mDraggedStarted = false;

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

	void setDraggedListener(DraggedListener draggedListener) {
		this.mDraggedListener = draggedListener;
	}

	boolean isTouched(int x, int y) {
		return getLeft() <= x && getRight() >= x && getTop() <= y && getBottom() >= y;
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
				if (mDraggedStarted) {
					mDraggedStarted = true;
					if (mDraggedListener != null) {
						mDraggedListener.startDragged();
					}
				}
				mTouchY = event.getY();
				if (mDraggedListener != null) {
					mDraggedListener.dragged(delta);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mDraggedStarted) {
				mDraggedStarted = false;
				if (mDraggedListener != null) {
					mDraggedListener.stopDragged();
				}
			}
			break;
		}
		return true;
	}
}
