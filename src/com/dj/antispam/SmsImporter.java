package com.dj.antispam;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import com.dj.antispam.dao.SmsDao;

import java.util.*;

/**
 * Filter for import SMS from inbox
 * User: dj
 * Date: 23.06.13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class SmsImporter {
	private final SmsFilter filter;
	private final Context context;

	public SmsImporter(Context context, SmsDao dao) {
		this.context = context;
		this.filter = new SmsFilter(context, dao);
	}

	public List<SenderStatus> collect() {
		Cursor cur = context.getContentResolver().query(Uri.parse(Utils.URI_INBOX),
				new String[]{"address", "read"}, null, null, null);
		Map<String, SenderStatus> result = new HashMap<String, SenderStatus>();
		if (cur.moveToFirst()) {
			do {
				String sender = cur.getString(0);
				boolean read = cur.getInt(1) != 0;
				SenderStatus status = result.get(sender);
				if (status == null) {
					status = new SenderStatus(sender, 1);
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
		for (SenderStatus status : result.values()) {
			status.isSpam = checkSpam(status);
		}
		return new ArrayList<SenderStatus>(result.values());
	}

	private boolean checkSpam(SenderStatus status) {
		if (filter.isUnwelcome(status.address, false)) {
			return true;
		}
		if (!status.read && status.count == 1) {
			return true;
		}
		return false;
	}
}
