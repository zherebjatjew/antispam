package com.dj.antispam;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.telephony.SmsMessage;
import android.util.Log;
import com.dj.antispam.dao.SmsDao;

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
			dao.close();
		}
	}

	private SmsMessage extractSmsMessage(Intent intent) {
		Bundle pudsBundle = intent.getExtras();
		Object[] pdus = (Object[]) pudsBundle.get("pdus");
		return SmsMessage.createFromPdu((byte[]) pdus[0]);
	}

	private void processMessage(final Context context, final SmsMessage smsMessage) {
		Log.i(TAG, "Received SMS from " + smsMessage.getDisplayOriginatingAddress());
		Boolean isSpam = filter.isUnwelcome(smsMessage.getDisplayOriginatingAddress());
		if (isSpam == null) {
			Log.i(TAG, "SMS rejected due to suspicious sender");
			notifyOnSuspiciousSender(context, smsMessage);
		} else if (isSpam) {
			Log.i(TAG, "SMS rejected due to spam");
		} else {
			return;
		}
		archiveMessage(smsMessage);
		abortBroadcast();
		Intent intent = new Intent(context.getResources().getString(R.string.update_action));
		context.sendBroadcast(intent);
	}

	private void notifyOnSuspiciousSender(Context context, SmsMessage smsMessage) {
		Notification.Builder builder = new Notification.Builder(context);
		builder
				.setContentTitle(String.format(context.getString(R.string.note_title), smsMessage.getDisplayOriginatingAddress()))
				.setContentText(String.format(context.getString(R.string.note_text), smsMessage.getDisplayMessageBody()))
				.setSmallIcon(R.drawable.ic_home)
				.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_ONE_SHOT|PendingIntent.FLAG_UPDATE_CURRENT));
		Notification notification = builder.build();
		notification.flags |= Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE|Notification.FLAG_AUTO_CANCEL;

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, notification);
	}

	private void archiveMessage(SmsMessage smsMessage) {
		dao.putMessage(smsMessage.getDisplayOriginatingAddress(),
				smsMessage.getTimestampMillis(),
				smsMessage.getDisplayMessageBody());
	}

}
