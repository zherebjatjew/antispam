package com.dj.antispam;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.dj.antispam.dao.DbHelper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 15.09.13
 * Time: 15:17
 * To change this template use File | Settings | File Templates.
 */
public class SpamListAdapter extends CursorAdapter {
	private List<Integer> removed = new ArrayList<Integer>();
	private Activity activity;
	private long lastViewed;

	public SpamListAdapter(Activity activity, Cursor c, long lastViewed) {
		super(activity.getApplicationContext(), c, true);
		this.activity = activity;
		this.lastViewed = lastViewed;
	}

	public void startRemovingMessage(int id) {
		synchronized (removed) {
			int idx = Collections.binarySearch(removed, id);
			if (idx < 0) {
				idx = -(idx+1);
				removed.add(idx, id);
			}
		}
	}

	public void endRemovingMessage(int id) {
		synchronized (removed) {
			int idx = Collections.binarySearch(removed, id);
			if (idx >= 0) {
				removed.remove(idx);
			}
		}
	}

	public boolean isRemoving(int id) {
		synchronized (removed) {
			return Collections.binarySearch(removed, id) >= 0;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		if (isRemoving(cursor.getInt(0))) {
			return activity.getLayoutInflater().inflate(R.layout.removed_item, null, false);
		} else {
			return activity.getLayoutInflater().inflate(R.layout.sms_item, null, false);
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (isRemoving(cursor.getInt(0))) {
			int height = view.getMeasuredHeight();
			ViewGroup group = (ViewGroup) view;
			view = activity.getLayoutInflater().inflate(R.layout.removed_item, group, false);
			view.setMinimumHeight(height);
			group.removeAllViews();
			group.addView(view);
		} else {
			TextView from = (TextView) view.findViewById(R.id.from);
			if (from == null) {
				// Item was removed
				ViewGroup group = (ViewGroup) view;
				view = activity.getLayoutInflater().inflate(R.layout.sms_item, group, false);
				group.removeAllViews();
				group.addView(view);
				from = (TextView) view.findViewById(R.id.from);
			}
			int colFrom = cursor.getColumnIndex(DbHelper.MESSAGES_FROM);
			String strFrom = cursor.getString(colFrom);
			from.setText(strFrom);
			TextView body = (TextView) view.findViewById(R.id.body);
			body.setText(cursor.getString(cursor.getColumnIndex(DbHelper.MESSAGES_BODY)));
			((TextView)view.findViewById(R.id.date)).setText(formatTime(cursor.getLong(cursor.getColumnIndex(DbHelper.MESSAGES_DATETIME))));
			if (cursor.getLong(cursor.getColumnIndex(DbHelper.MESSAGES_DATETIME)) < lastViewed) {
				body.setTextColor(context.getResources().getColor(R.color.read_body));
			} else {
				body.setTextColor(context.getResources().getColor(R.color.unread_body));
			}
		}
	}

	private String formatTime(Long time) {
		return DateFormat.getDateTimeInstance().format(new Date(time));
	}
}
