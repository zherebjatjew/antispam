package com.example.antispam.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 31.05.13
 * Time: 22:12
 * To change this template use File | Settings | File Templates.
 */
public class SmsDao {
	private SQLiteDatabase db;
	private DbHelper helper;

	public long putMessage(String from, long datetime, String body) {
		ContentValues values = new ContentValues();
		values.put(DbHelper.MESSAGES_FROM, from);
		values.put(DbHelper.MESSAGES_DATETIME, datetime);
		values.put(DbHelper.MESSAGES_BODY, body);
		return db.insert(DbHelper.TABLE_MESSAGES, null, values);
	}

	public String getDeviceId() {
		return helper.getMeta("deviceId");
	}

	public SmsDao(Context context) {
		helper = new DbHelper(context);
		db = helper.getWritableDatabase();
	}
}
