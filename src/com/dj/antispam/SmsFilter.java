package com.dj.antispam;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;
import com.dj.antispam.dao.SmsDao;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 31.05.13
 * Time: 21:38
 *
 * Determines if message is a spam or not.
 */
public class SmsFilter {
	private static final String TAG = SmsFilter.class.getSimpleName();
	private final SmsDao dao;
	private final Context context;

	public SmsFilter(Context context, SmsDao dao) {
		this.dao = dao;
		this.context = context;
	}

	public boolean isUnwelcome(SmsMessage message) {
		String from = message.getDisplayOriginatingAddress();
		Log.i(TAG, "Message from " + from);
		if (isFromBlackList(from)) {
			Log.i(TAG, "SPAM: sender is in black list");
			return true;
		}
		if (hasEverCalledTo(from)) {
			Log.i(TAG, "NOT SPAM: User has called to the sender");
			return false;
		}
		if (hasEverMessagedTo(from)) {
			Log.i(TAG, "NOT SPAM: User has written to the sender");
			return false;
		}
		// TODO: Remove next line after debug
		if ("+000".equals(from)) {
			Log.i(TAG, "SPAM: Predefined black list");
			return true;
		}
		if (isFromWhiteList(from)) {
			Log.i(TAG, "NOT SPAM: in white list");
			return false;
		}
		if (isFromGateway(from)) {
			Log.i(TAG, "SPAM: Gateway address");
			return true;
		}
		if (hasMessagesFrom(from)) {
			Log.i(TAG, "NOT SPAM: found message(s) in inbox");
			return false;
		}
		Log.i(TAG, "UNKNOWN status");
		return false;
	}

	private boolean hasMessagesFrom(String sender) {
		// The same as in hasEverMessagedTo
/*
		final String SMS_URI_INBOX = "content://sms/inbox";
		Uri uri = Uri.parse(SMS_URI_INBOX);
		String[] projection = new String[] { "_id" };
		Cursor cur = context.getContentResolver().query(uri, projection, "address=?", new String[]{Uri.encode(sender)}, null);
		try {
			return !cur.isAfterLast();
		} finally {
			if (cur != null) cur.close();
		}
*/
		return false;
	}

	private boolean hasEverMessagedTo(String from) {
		Cursor cur = context.getContentResolver().query(Uri.parse("content://sms"), new String[]{"address"}, "address=?", new String[]{from}, null);
		try {
/*
			if (cur.moveToFirst()) {
				for (int i = 0; i < cur.getColumnCount(); i++) {
					Log.v(TAG, cur.getColumnName(i) + " = " + cur.getString(i));
				}
			}
*/
			return !cur.isAfterLast();
		} finally {
			if (cur != null) cur.close();
		}
	}

	private boolean hasEverCalledTo(String sender) {
		if (!PhoneNumberUtils.isGlobalPhoneNumber(sender)) {
			// sender ID is not a phone number. It usually means what the message was sent through a gate,
			// ergo user can not call to the sender.
			return false;
		}
		Uri numberUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(sender));
		Cursor cur = context.getContentResolver().query(numberUri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
		return !cur.isAfterLast();
	}

	private boolean isFromBlackList(String sender) {
		// Message is a spam if it's sender is in spam list
		// TODO: Manage separate list of spammers to be able to clear spam list
		if (dao.isInSpam(sender)) {
			return true;
		}
		Boolean flaggedAsSpam = dao.isSenderASpammer(sender);
		return  (flaggedAsSpam != null && flaggedAsSpam);
	}

	private boolean isFromWhiteList(String sender) {
		return false;
	}

	private boolean isFromGateway(String sender) {
		return !PhoneNumberUtils.isGlobalPhoneNumber(sender);
	}
}
