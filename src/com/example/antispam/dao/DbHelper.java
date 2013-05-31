package com.example.antispam.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 31.05.13
 * Time: 22:55
 * To change this template use File | Settings | File Templates.
 */
public class DbHelper extends SQLiteOpenHelper {
	public final static String TABLE_MESSAGES = "messages";
	public final static String MESSAGES_ID = "_id";
	public final static String MESSAGES_FROM = "from";
	public final static String MESSAGES_DATETIME = "sentAt";
	public final static String MESSAGES_BODY = "body";

	private final static String DB_NAME = "db";
	private final static int DB_VERSION = 1;
	private final static String DB_CREATE =
			"CREATE TABLE `" + TABLE_MESSAGES + "` (`_id` INTEGER PRIMARY KEY, `from` VARCHAR(20) NOT NULL, `sentAt` DATETIME, `body` TEXT);";

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
	}
}
