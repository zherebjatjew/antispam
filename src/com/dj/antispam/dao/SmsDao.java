package com.dj.antispam.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import com.dj.antispam.SmsModel;
import com.dj.antispam.Utils;

import java.util.List;

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
		values.put('`' + DbHelper.MESSAGES_FROM + '`', from);
		values.put(DbHelper.MESSAGES_DATETIME, datetime);
		values.put('`' + DbHelper.MESSAGES_BODY + '`', body);
		return db.insert(DbHelper.TABLE_MESSAGES, null, values);
	}

	public String getDeviceId() {
		return helper.getMeta("deviceId");
	}

	public SmsDao(Context context) {
		helper = new DbHelper(context);
		db = helper.getWritableDatabase();
	}

	public Cursor getSpamCursor() {
		return db.rawQuery("SELECT * from `messages` ORDER BY `sentAt` DESC", null);
	}


	public boolean isInSpam(String sender) {
		return !db.rawQuery("SELECT `_id` FROM `messages` WHERE `from`=? LIMIT 1", new String[]{sender}).isAfterLast();
	}

	public SmsModel getMessage(int messageId) {
		Cursor cur = db.rawQuery("SELECT `_id`, `from`, `sentAt`, `body` FROM `messages` WHERE `_id`=?",
				new String[]{Integer.toString(messageId)});
		try {
			if (cur.isAfterLast()) {
				throw new IllegalArgumentException("No message with id " + messageId);
			}
			cur.moveToFirst();
			SmsModel result = new SmsModel();
			result.id = cur.getInt(0);
			result.from = cur.getString(1);
			result.sentAt = cur.getLong(2);
			result.body = cur.getString(3);
			return result;
		} finally {
			cur.close();
		}
	}

	public void deleteMessage(int messageId) {
		db.execSQL("DELETE FROM `messages` WHERE `_id`=?", new String[]{Integer.toString(messageId)});
	}

	public Boolean isSenderASpammer(String sender) {
		Cursor cur = db.rawQuery("SELECT `_id`, `spam` FROM `senders` WHERE `_id`=?", new String[]{sender});
		try {
			if (cur.isAfterLast()) {
				return null;
			} else {
				cur.moveToFirst();
				return cur.getInt(1) == 1;
			}
		} finally {
			cur.close();
		}
	}

	public void markSender(String from, Boolean spam) {
		if (spam == null) {
			db.execSQL("DELETE FROM `senders` WHERE `_id`=?", new String[]{from});
		} else {
			db.execSQL("REPLACE INTO `senders` (`_id`, `spam`) VALUES (?, ?)", new Object[]{from, spam?1:0});
		}
	}

	public void markSender(List<String> senders, final Boolean spam) {
		if (spam == null) {
			db.execSQL("DELETE FROM `senders` WHERE `_id` IN (" + Utils.join(senders, new Utils.Processor() {
				@Override
				public void format(StringBuilder builder, Object item) {
					builder.append(item);
				}
			}) + ")");
		} else {
			for (String item : senders) {
				db.execSQL("REPLACE INTO `senders` (`_id`, `spam`) VALUES (" + DatabaseUtils.sqlEscapeString(item) + "," + (spam ? "1" : "0") + ')');
			};
		}
	}

	public void close() {
		db.close();
	}

}
