package com.dj.antispam.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 31.05.13
 * Time: 22:55
 * To change this template use File | Settings | File Templates.
 */
public class DbHelper extends SQLiteOpenHelper {
	public static final String TABLE_MESSAGES = "messages";
	public static final String MESSAGES_ID = "_id";
	public static final String MESSAGES_FROM = "`from`";
	public static final String MESSAGES_DATETIME = "sentAt";
	public static final String MESSAGES_BODY = "`body`";

	private static final String DB_NAME = "db";
	private static final int DB_VERSION = 1;
	public static final String CREATE_SENDERS_TABLE = "CREATE TABLE `senders` (`_id` VARCHAR(20) PRIMARY KEY, `spam` BOOL, `addedAt` TIMESTAMP);";
	private static final String DB_CREATE =
			"CREATE TABLE `messages` (`_id` INTEGER PRIMARY KEY, `from` VARCHAR(20) NOT NULL, `sentAt` DATETIME, `body` TEXT);" +
			"CREATE TABLE `meta` (`_id` VARCHAR(20) PRIMARY KEY, `value` VARCHAR(150));" +
			"INSERT INTO `meta` (`name`, `value`) VALUES ('deviceId', '%s'); ";
	private static final String SQL_GET_META = "SELECT `value` FROM `meta` WHERE `_id`='?'";

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		String query = String.format(DB_CREATE, UUID.randomUUID().toString());
		sqLiteDatabase.execSQL(query);
		sqLiteDatabase.execSQL(CREATE_SENDERS_TABLE);
		sqLiteDatabase.rawQuery("SELECT * FROM `senders`", null);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i2) {
	}

	public String getMeta(String key) {
		Cursor cur = getReadableDatabase().rawQuery(SQL_GET_META, new String[]{key});
		try {
			return cur.getString(0);
		} finally {
			cur.close();
		}
	}
}
