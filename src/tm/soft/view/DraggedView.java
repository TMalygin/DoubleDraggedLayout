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
	private int mMinPosition;
	private int mMaxPosition;
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

		TypedArray a = c.getTheme().obtainStyledAttributes(attrs,
				R.styleable.DraggedView, 0, 0);
		try {

			mMaxPosition = a.getDimensionPixelSize(
					R.styleable.DraggedView_max_position_top_of_layout, -1);
			mMinPosition = a.getDimensionPixelSize(
					R.styleable.DraggedView_min_position_top_of_layout, -1);
			if (mMaxPosition == -1 || mMinPosition == 1) {
				throw new IllegalStateException(
						"DraggedView must have max_position_top_of_layout and min_position_top_of_layout!");
			}
		} finally {
			a.recycle();
		}
	}

	int getMaxPosition() {
		return mMaxPosition;
	}

	int getmMinPosition() {
		return mMinPosition;
	}

	public void setDoubleDraggedLayout(DoubleDraggedLayout doubleDraggedLayout) {
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

			}
			break;
		case MotionEvent.ACTION_UP:
			if (!mIsDragged) {
				if (mDoubleDraggedLayout != null) {
					mDoubleDraggedLayout
							.switchState(mDoubleDraggedLayout.getState() == DoubleDraggedLayout.STATE_NORMAL ? DoubleDraggedLayout.STATE_TOP_OPENED
									: DoubleDraggedLayout.STATE_NORMAL);
				}
			}
			mIsDragged = false;
			break;
		}
		return true;
	}
}
