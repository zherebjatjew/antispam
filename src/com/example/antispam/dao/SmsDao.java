package com.example.antispam.dao;

import android.content.Context;
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

	public SmsDao(Context context) {
		db = context.openOrCreateDatabase("db", Context.MODE_PRIVATE, null);
	}
}
