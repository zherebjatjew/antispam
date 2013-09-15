package com.dj.antispam;

import android.content.*;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.dj.antispam.actionbarcompat.ActionBarActivity;
import com.dj.antispam.dao.DbHelper;
import com.dj.antispam.dao.SmsDao;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private SmsDao dao;
	private Preferences prefs;
	private BroadcastReceiver updater;
	private Cursor cursor;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_import:
				openImportActivity();
				return true;
			case R.id.menu_revert:
				restoreAllMessages();
				return true;
		}
		return false;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dao = new SmsDao(this);
		prefs = new Preferences(this);

		setContentView(R.layout.main);
		final ListView list = (ListView)findViewById(R.id.listView);
		cursor = dao.getSpamCursor();
		startManagingCursor(cursor);
		final SpamListAdapter adapter = new SpamListAdapter(this, cursor, prefs.getLastViewedTime()) {

		};
		list.setAdapter(adapter);
		list.setOnTouchListener(new SwipeTouchListener() {

			@Override
			protected void onSwipeLeft(float x, float y) {
				final View v = hitTest((int) x, (int) y);
				Display display = getWindowManager().getDefaultDisplay();
				v.clearAnimation();
				TranslateAnimation translateAnim = new TranslateAnimation(0, -display.getWidth(), 0, 0);
				translateAnim.setDuration(250);
				translateAnim.setAnimationListener(new Animation.AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								int id = getMessageId(list.getPositionForView(v));
								adapter.startRemovingMessage(id);
								restoreMessage(id);
								adapter.endRemovingMessage(id);
								Intent intent = new Intent(getResources().getString(R.string.update_action));
								sendBroadcast(intent);
							}
						}).start();
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						int id = getMessageId(list.getPositionForView(v));
						if (adapter.isRemoving(id)) {
							invalidateViewItem(v);
						}
					}
				});
				v.startAnimation(translateAnim);
			}

			@Override
			protected void onSwipeRight(float x, float y) {
				final View v = hitTest((int) x, (int) y);
				Display display = getWindowManager().getDefaultDisplay();
				v.clearAnimation();
				TranslateAnimation translateAnim = new TranslateAnimation(0, display.getWidth(), 0, 0);
				translateAnim.setDuration(250);
				translateAnim.setAnimationListener(new Animation.AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								int id = getMessageId(list.getPositionForView(v));
								adapter.startRemovingMessage(id);
								deleteMessage(id);
								adapter.endRemovingMessage(id);
								Intent intent = new Intent(getResources().getString(R.string.update_action));
								sendBroadcast(intent);
							}
						}).start();
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						int id = getMessageId(list.getPositionForView(v));
						if (adapter.isRemoving(id)) {
							invalidateViewItem(v);
						}
					}
				});
				v.startAnimation(translateAnim);
			}

			void invalidateViewItem(View view) {
				// TODO: Use similar adapter's method instead
				ViewGroup group = (ViewGroup) view;
				int height = view.getMeasuredHeight();
				View newView = getLayoutInflater().inflate(R.layout.removed_item, group, false);
				newView.setMinimumHeight(height);
				group.removeAllViews();
				group.addView(newView);
				view.invalidate();
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

			private int getMessageId(int nItem) {
				Cursor cur = (Cursor) list.getAdapter().getItem(nItem);
				int col = cur.getColumnIndex("_id");
				return cur.getInt(col);
			}

			private void deleteMessage(int id) {
				SmsModel message = dao.getMessage(id);
				dao.deleteMessage(id);
				dao.markSender(message.from, true);
			}
		});

		updater = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "Update spam list intent has been received");
				// Wait for record updated is complete or you'll get empty record
				stopManagingCursor(cursor);
				cursor = dao.getSpamCursor();
				startManagingCursor(cursor);
				adapter.changeCursor(cursor);
				list.postInvalidate();
			}
		};

		importFromExistingMessages();
	}

	private void importFromExistingMessages() {
		if (prefs.showImportActivity()) {
			openImportActivity();
			prefs.setShowImportActivity(false);
		}
	}

	private void openImportActivity() {
		startActivityForResult(new Intent(this, ImportActivity.class), ImportActivity.FIRST_IMPORT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(updater, new IntentFilter(getResources().getString(R.string.update_action)));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(updater);
		prefs.updateLastViewedTime();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (dao != null) {
			dao.close();
		}
	}

	@Override
	protected void onActivityResult(int code, int result, Intent intent) {
		Log.d(TAG, "Activity has closed");
	}

	private void restoreAllMessages() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Cursor cursor = dao.getSpamCursor();
				try {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						ContentValues values = new ContentValues();
						values.put(Utils.MESSAGE_ADDRESS, cursor.getString(1));
						values.put(Utils.MESSAGE_BODY, cursor.getString(3));
						values.put(Utils.MESSAGE_READ, true);
						values.put(Utils.MESSAGE_TYPE, Utils.MESSAGE_TYPE_SMS);
						values.put(Utils.MESSAGE_DATE, cursor.getLong(2));
						getContentResolver().insert(Uri.parse(Utils.URI_INBOX), values);
						dao.deleteMessage(cursor.getInt(0));
					} cursor.moveToNext();
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), getString(R.string.msg_revert_error), Toast.LENGTH_LONG);
					getContentResolver().delete(Uri.parse(Utils.URI_THREADS + "-1"), null, null);
					return;
				} finally {
					cursor.close();
				}
				getContentResolver().delete(Uri.parse(Utils.URI_THREADS + "-1"), null, null);
				Toast.makeText(getApplicationContext(), getString(R.string.msg_revert_ok), Toast.LENGTH_LONG);
			}
		}).start();
	}

	private void restoreMessage(int messageId) {
		SmsModel message = dao.getMessage(messageId);
		ContentValues values = new ContentValues();
		values.put(Utils.MESSAGE_ADDRESS, message.from);
		values.put(Utils.MESSAGE_BODY, message.body);
		values.put(Utils.MESSAGE_READ, true);
		values.put(Utils.MESSAGE_TYPE, Utils.MESSAGE_TYPE_SMS);
		values.put(Utils.MESSAGE_DATE, message.sentAt);
		getContentResolver().insert(Uri.parse(Utils.URI_INBOX), values);
		getContentResolver().delete(Uri.parse(Utils.URI_THREADS + "-1"), null, null);
		dao.deleteMessage(messageId);
	}

}
