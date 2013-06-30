package com.dj.antispam;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import com.dj.antispam.dao.SmsDao;

import java.util.*;
import java.util.logging.Logger;

/**
 * Filter for import SMS from inbox
 * User: dj
 * Date: 23.06.13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class SmsImporter {
	private final String TAG = "SmsImporter";
	private final SmsFilter filter;
	private final Context context;

	public SmsImporter(Context context, SmsDao dao) {
		this.context = context;
		this.filter = new SmsFilter(context, dao);
	}

	public List<SenderStatus> collect() {
/*
		// Conversation is exactly what I need here, because I do not care of message content, but
		// I didn't figure out how to get sender name/address by thread id (I mean, how to get quickly).
		Cursor c1 = context.getContentResolver().query(Uri.parse("content://sms/conversations"), null, null, null, null);
		for (int i = 0; i < c1.getColumnCount(); i++) {
			Log.v(TAG, c1.getColumnName(i));
		}
		c1.close();
*/
		Cursor cur = context.getContentResolver().query(Uri.parse(Utils.URI_INBOX),
				new String[]{"address", "read", "person"}, null, null, "date DESC");
		Map<String, SenderStatus> result = new HashMap<String, SenderStatus>();
		if (cur.moveToFirst()) {
			do {
				String sender = cur.getString(0);
				Long person = cur.getLong(2);
				boolean read = cur.getInt(1) != 0;
				SenderStatus status = result.get(sender);
				if (status == null) {
					status = new SenderStatus(sender, person, 1);
					status.read = read;
					result.put(sender, status);
				} else {
					status.count++;
					if (status.read != null) {
						if (status.read && !read || !status.read && read) {
							status.read = null;
						}
					}
				}
			} while (cur.moveToNext());
		}
/*
		for (SenderStatus status : result.values()) {
			status.isSpam = checkSpam(status);
		}
*/
		return new ArrayList<SenderStatus>(result.values());
	}

	public boolean checkSpam(SenderStatus status) {
		if (!PhoneNumberUtils.isGlobalPhoneNumber(status.address)) {
			return true;
		}
		if (status.read != null && !status.read && status.count == 1) {
			return true;
		}
		return false;
	}
}
