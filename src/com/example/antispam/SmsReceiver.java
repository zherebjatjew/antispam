package com.example.antispam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import com.example.antispam.dao.SmsDao;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 31.05.13
 * Time: 21:17
 * To change this template use File | Settings | File Templates.
 */
public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = SmsReceiver.class.getSimpleName();
	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	private SmsFilter filter;
	private SmsDao dao;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && ACTION.equals(intent.getAction())) {
			dao = new SmsDao(context);
			filter = new SmsFilter(context, dao);
			SmsMessage smsMessage = extractSmsMessage(intent);
			processMessage(context, smsMessage);
		}
	}

	private SmsMessage extractSmsMessage(Intent intent) {
		Bundle pudsBundle = intent.getExtras();
		Object[] pdus = (Object[]) pudsBundle.get("pdus");
		return SmsMessage.createFromPdu((byte[]) pdus[0]);
	}

	private void processMessage(Context context, SmsMessage smsMessage) {
		Log.i(TAG, "Received SMS from " + smsMessage.getDisplayOriginatingAddress());
		if (filter.isUnwelcome(smsMessage)) {
			Log.i(TAG, "SMS rejected due to spam");
			archiveMessage(smsMessage);
			abortBroadcast();
		}
	}

	private void archiveMessage(SmsMessage smsMessage) {
		dao.putMessage(smsMessage.getDisplayOriginatingAddress(),
				smsMessage.getTimestampMillis(),
				smsMessage.getDisplayMessageBody());
	}

}
