package com.dj.antispam;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
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

	public Boolean isUnwelcome(String from) {
		Log.i(TAG, "Message from " + from);
		// TODO: Remove next line after debug
		if ("+000".equals(from)) {
			Log.i(TAG, "SPAM: Predefined black list");
			return true;
		}
		if ("+001".equals(from)) {
			Log.i(TAG, "UNKNOWN status");
			return null;
		}
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
		if (isFromWhiteList(from)) {
			Log.i(TAG, "NOT SPAM: in white list");
			return false;
		}
		if (isFromGateway(from)) {
			Log.i(TAG, "SPAM: Gateway address");
			return true;
		}
		Log.i(TAG, "UNKNOWN status");
		return null;
	}

	private boolean hasEverMessagedTo(String from) {
		Cursor cur = context.getContentResolver().query(Uri.parse(Utils.URI_SMS), new String[]{Utils.MESSAGE_ADDRESS}, Utils.MESSAGE_ADDRESS + "=?", new String[]{from}, null);
		try {
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
		try {
			return !cur.isAfterLast();
		} finally {
			cur.close();
		}
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
