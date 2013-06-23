package com.dj.antispam;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;

import java.util.*;

/**
 * Filter for import SMS from inbox
 * User: dj
 * Date: 23.06.13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class SmsImporter {
	Context context;
	public List<SenderStatus> collect() {
		Cursor cur = context.getContentResolver().query(Uri.parse("content://sms/inbox"),
				new String[]{"address"}, null, null, null);
		Map<String, SenderStatus> result = new HashMap<String, SenderStatus>();
		cur.moveToFirst();
		while (cur.moveToNext()) {
			String sender = cur.getString(cur.getColumnIndex("address"));
			SenderStatus status = result.get(sender);
			if (status == null) {
				boolean isSpam = PhoneNumberUtils.isGlobalPhoneNumber(sender);
				status = new SenderStatus(sender, isSpam, 1);
				result.put(sender, status);
			} else {
				status.count++;
			}
		}
		return new ArrayList<SenderStatus>(result.values());
	}
}
