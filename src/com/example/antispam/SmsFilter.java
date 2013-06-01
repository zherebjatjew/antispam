package com.example.antispam;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import com.example.antispam.dao.SmsDao;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 31.05.13
 * Time: 21:38
 *
 * Determines if message is a spam or not.
 * <table>
 *     <tr>
 *         <th>Spam</th>
 *         <th>Not spam</th>
 *     </tr>
 *     <tr>
 *         <td>Spam list already has this sender</td>
 *         <td>Sender is in contact list</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;</td>
 *         <td>User has ever wrote to the sender</td>
 *     </tr>
 *     <tr>
 *         <td>&nbsp;</td>
 *         <td>User has ever called the sender</td>
 *     </tr>
 * </table>
 */
public class SmsFilter {
	private final SmsDao dao;
	private final Context context;

	public SmsFilter(Context context, SmsDao dao) {
		this.dao = dao;
		this.context = context;
	}
	public boolean isUnwelcome(SmsMessage message) {
		String from = message.getDisplayOriginatingAddress();
		if (isFromBlackList(from)) return true;
		if (hasEverCalledTo(from)) return false;
		if (hasEverMessagedTo(from)) return false;
		// TODO: Remove next line after debug
		if ("+000".equals(from)) return true;
		if (isFromWhiteList(from)) return false;
		if (isFromGateway(from)) return true;
		return false;
	}

	private boolean hasEverMessagedTo(String from) {
		final String SMS_URI_INBOX = "content://sms/inbox";
		Uri uri = Uri.parse(SMS_URI_INBOX);
		String[] projection = new String[] { "_id" };
		Cursor cur = context.getContentResolver().query(uri, projection, "address=?", new String[]{Uri.encode(from)}, null);
		return cur.isAfterLast();
	}

	private boolean hasEverCalledTo(String sender) {
		if (!PhoneNumberUtils.isGlobalPhoneNumber(sender)) {
			// sender ID is not a phone number. It usually means what the message was sent through a gate,
			// ergo user can not call to the sender.
			return false;
		}
		Uri numberUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(sender));
		Cursor cur = context.getContentResolver().query(numberUri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
		return cur.isAfterLast();
	}

	private boolean isFromBlackList(String sender) {
		// Message is a spam if it's sender is in spam list
		// TODO: Manage separate list of spammers to be able to clear spam list
		return dao.isInSpam(sender);
	}

	private boolean isFromWhiteList(String sender) {
		return false;
	}

	private boolean isFromGateway(String sender) {
		return false;
	}
}
