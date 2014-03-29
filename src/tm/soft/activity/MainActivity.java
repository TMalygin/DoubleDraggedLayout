package tm.soft.activity;

import tm.soft.doubledraggedlayout.R;
import tm.soft.view.DoubleDraggedLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity {

	private PlaceholderFragment placeholderFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			placeholderFragment = (PlaceholderFragment) getSupportFragmentManager()
					.getFragment(savedInstanceState, "first");
		}

		if (placeholderFragment == null) {
			placeholderFragment = new PlaceholderFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, placeholderFragment, "first").commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "first",
				placeholderFragment);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (!placeholderFragment.onBackPressed())
			super.onBackPressed();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			OnClickListener {

		DoubleDraggedLayout mLayout;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			mLayout = (DoubleDraggedLayout) rootView
					.findViewById(R.id.layout);
			rootView.findViewById(R.id.button1).setOnClickListener(this);
			rootView.findViewById(R.id.textView2).setOnClickListener(this);
			rootView.findViewById(R.id.button2).setOnClickListener(this);
			return rootView;
		}

		public boolean onBackPressed() {
			if (mLayout.getState() != DoubleDraggedLayout.STATE_NORMAL) {
				mLayout.switchState(DoubleDraggedLayout.STATE_NORMAL);
				return true;
			}
			return false;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button1:
				mLayout.switchState(DoubleDraggedLayout.STATE_TOP_OPENED);
				break;
			case R.id.textView2:
				mLayout.switchState(DoubleDraggedLayout.STATE_NORMAL);
				break;
			case R.id.button2:
				if (mLayout.getState() == DoubleDraggedLayout.STATE_NORMAL) {
					mLayout.switchState(DoubleDraggedLayout.STATE_BOTTOM_OPENED);
				} else {
					mLayout.switchState(DoubleDraggedLayout.STATE_NORMAL);
				}
				break;
			}
		}
	}

}
