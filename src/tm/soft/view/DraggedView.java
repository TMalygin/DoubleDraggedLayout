package tm.soft.view;

import tm.soft.doubledraggedlayout.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageButton;

public class DraggedView extends ImageButton {

	private int mNormalStateCoordinateY;
	private int mOpenedStateCoordinateY;
	private DraggedListener mDraggedListener;

	private int mSlop;
	private volatile boolean mDraggedStarted = false;
	private volatile float mTouchY;
	private Rect outRect = new Rect();
	private int[] location = new int[2];

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

	void setDrawableState(int state) {
		Log.v("", "state !!!");
		Drawable drawable = getDrawable();
		if (drawable != null)
			drawable.setState(new int[] { state });
	}

	void setDraggedListener(DraggedListener draggedListener) {
		this.mDraggedListener = draggedListener;
	}

	boolean isTouched(float x, float y) {
		getDrawingRect(outRect);
		getLocationOnScreen(location);
		outRect.offset(location[0], location[1]);
		return outRect.contains((int) x, (int) y);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		float coordY = event.getRawY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchY = coordY;
			break;
		case MotionEvent.ACTION_MOVE:
			float delta = coordY - mTouchY;

			if (Math.abs(delta) > mSlop) {
				mTouchY = coordY;
				if (!mDraggedStarted) {
					mDraggedStarted = true;
					if (mDraggedListener != null) {
						mDraggedListener.startDragged();
					}
				}

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
		default:
			Log.v("", "action " + event.getAction());
			break;
		}
		return true;
	}
}
