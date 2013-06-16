package com.example.antispam;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.example.antispam.dao.SmsDao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private SmsDao dao;
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dao = new SmsDao(this);
		setContentView(R.layout.main);
		final ListView list = (ListView)findViewById(R.id.listView);
		Cursor cursor = dao.getSpamCursor();
		startManagingCursor(cursor);
		CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.sms_item, cursor,
				new String[] {"from", "body", "sentAt"},
				new int[] {R.id.from, R.id.body, R.id.date})
		{
			@Override
			public void setViewText(TextView v, String text) {
				super.setViewText(v, convText(v, text));
			}
			private String convText(TextView v, String text) {
				if (v.getId() == R.id.date) {
					return DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(text)));
				}
				return text;
			}
		};
		list.setAdapter(adapter);
		list.setOnTouchListener(new View.OnTouchListener() {
			private float downX;
			private int downPosition;
			private VelocityTracker velocityTracker;
			private final int slop;
			private final int minFlingVelocity;
			private final int maxFlingVelocity;
			private final int animationTime;
			private int dismissAnimationRefCount = 0;

			private View swiping = null;

			{
				ViewConfiguration vc = ViewConfiguration.get(list.getContext());
				slop = vc.getScaledTouchSlop();
				minFlingVelocity = vc.getScaledMinimumFlingVelocity();
				maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
				animationTime = list.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
			}

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						int[] coords = new int[2];
						list.getLocationOnScreen(coords);
						swiping = hitTest((int) (event.getRawX() - coords[0]), (int) (event.getRawY() - coords[1]));
						if (swiping != null) {
							downX = event.getRawX();
							downPosition = list.getPositionForView(swiping);
							velocityTracker = VelocityTracker.obtain();
							velocityTracker.addMovement(event);
						}
						break;

					case MotionEvent.ACTION_UP:
						if (velocityTracker == null) break;
						float deltaX = event.getRawX() - downX;
						velocityTracker.addMovement(event);
						velocityTracker.computeCurrentVelocity(1000);
						float velocityX = Math.abs(velocityTracker.getXVelocity());
						float velocityY = Math.abs(velocityTracker.getYVelocity());
						boolean dismiss = false;
						boolean dismissRight = false;
						if (Math.abs(deltaX) > list.getWidth() / 2) {
							dismiss = true;
							dismissRight = deltaX > 0;
						} else if (minFlingVelocity <= velocityX && velocityX <= maxFlingVelocity
								&& velocityY < velocityX) {
							dismiss = true;
							dismissRight = velocityTracker.getXVelocity() > 0;
						}
						if (dismiss) {
							// dismiss
							final View downView = swiping; // mDownView gets null'd before animation ends
							final int downPosition = this.downPosition;
							++dismissAnimationRefCount;
/*
							downView.animate()
									.translationX(dismissRight ? mViewWidth : -mViewWidth)
									.alpha(0)
									.setDuration(mAnimationTime)
									.setListener(new AnimatorListenerAdapter() {
										@Override
										public void onAnimationEnd(Animator animation) {
											performDismiss(downView, downPosition);
										}
									});
*/
							if (dismissRight) {
								onSwipeRight(downPosition);
							} else {
								onSwipeLeft(downPosition);
							}
						}
						break;

					default:
						return false;
				}
				return false;
			}

			private View hitTest(int x, int y) {
				View child;
				Rect rect = new Rect();
				for (int i = 0; i < list.getChildCount(); i++) {
					child = list.getChildAt(i);
					child.getHitRect(rect);
					if (rect.contains(x, y)) {
						return child;
					}
				}
				return null;
			}

			private void onSwipeLeft(int nItem) {
				int id = getMessageId(nItem);
				restoreMessage(id);
				CursorAdapter adapter = (CursorAdapter)list.getAdapter();
				adapter.changeCursor(dao.getSpamCursor());
			}

			private int getMessageId(int nItem) {
				Cursor cur = (Cursor)list.getAdapter().getItem(nItem);
				int col = cur.getColumnIndex("_id");
				return cur.getInt(col);
			}

			private void onSwipeRight(int nItem) {
				int id = getMessageId(nItem);
				SmsModel message = dao.getMessage(id);
				dao.deleteMessage(id);
				dao.markSender(message.from, true);
				CursorAdapter adapter = (CursorAdapter)list.getAdapter();
				adapter.changeCursor(dao.getSpamCursor());
			}
		});
	}

	private void restoreMessage(int messageId) {
		SmsModel message = dao.getMessage(messageId);
		ContentValues values = new ContentValues();
		values.put("address", message.from);
		values.put("body", message.body);
		values.put("read", true);
		values.put("date", message.sentAt);
		getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
		dao.deleteMessage(messageId);
	}

}
