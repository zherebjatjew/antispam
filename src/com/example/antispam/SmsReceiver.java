package com.example.antispam;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;

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

	private SmsFilter filter = new SmsFilter();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && ACTION.equals(intent.getAction())) {
			SmsMessage smsMessage = extractSmsMessage(intent);
			processMessage(context, smsMessage);
		}
	}

	private SmsMessage extractSmsMessage(Intent intent) {
		Bundle pudsBundle = intent.getExtras();
		Object[] pdus = (Object[]) pudsBundle.get("pdus");
		SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
		return smsMessage;
	}

	private void processMessage(Context context, SmsMessage smsMessage) {
		Log.i(TAG, "Received SMS from " + smsMessage.getOriginatingAddress());
		// Do some stuff here
		if (filter.isUnwelcome(smsMessage)) {
			Log.i(TAG, "SMS rejected due to spam");
			archiveMessage(smsMessage);
			abortBroadcast();
		}
	}

	private void archiveMessage(SmsMessage smsMessage) {
		//To change body of created methods use File | Settings | File Templates.
	}

}
