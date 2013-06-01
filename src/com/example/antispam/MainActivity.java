package com.example.antispam;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.example.antispam.dao.SmsDao;

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
		ListView list = (ListView)findViewById(R.id.listView);
		Cursor cursor = dao.getSpamCursor();
		startManagingCursor(cursor);
		CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.sms_item, cursor,
				new String[] {"from", "body"},
				new int[] {R.id.from, R.id.body});
		list.setAdapter(adapter);
	}

}
