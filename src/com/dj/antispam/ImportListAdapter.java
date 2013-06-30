package com.dj.antispam;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 23.06.13
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
public class ImportListAdapter extends BaseAdapter {

	private List<SenderStatus> senders;
	private final Activity activity;
	private final SmsImporter importer;
	private volatile Boolean terminate = false;

	private class Item {
		SenderStatus status;
		CheckBox view;
	}
	private final Queue<Item> itemsToUpdate = new LinkedBlockingQueue<Item>();
	private Thread itemUpdater;

	public ImportListAdapter(Activity activity, SmsImporter importer) {
		this.activity = activity;
		this.importer = importer;
	}

	private List<SenderStatus> getSenders() {
		if (senders == null) {
			senders = importer.collect();
		}
		return senders;
	}

	public void close() {
		terminate = true;
		try {
			synchronized (terminate) {
				terminate.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	@Override
	public int getCount() {
		return getSenders().size();
	}

	@Override
	public Object getItem(int i) {
		return getSenders().get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		final SenderStatus status = getSenders().get(i);
		final View res = activity.getLayoutInflater().inflate(R.layout.import_item, viewGroup, false);
		final CheckBox text = (CheckBox) res.findViewById(R.id.sender);
		final TextView count = (TextView) res.findViewById(R.id.messageCount);

		text.setText(status.name == null ? status.address : status.name);
		text.setChecked(status.isSpam != null && status.isSpam);
		if (status.isSpam == null || status.personId != null) {
			if (itemUpdater == null) {
				itemUpdater = new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							try {
								synchronized (terminate) {
									terminate.wait();
								}
							} catch (InterruptedException e) {
								break;
							}
							if (terminate) {
								synchronized (terminate) {
									terminate.notify();
								}
								break;
							}
							while (!itemsToUpdate.isEmpty()) {
								final Item item;
								item = (Item)itemsToUpdate.poll();
								if (item != null) {
									// Suggest spam status
									if (item.status.isSpam == null) {
										item.status.isSpam = importer.checkSpam(item.status);
										activity.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												item.view.setChecked(item.status.isSpam);
											}
										});
									}
									// Substitute phone number with display name
									if (item.status.name == null && item.status.personId != null &&
											PhoneNumberUtils.isGlobalPhoneNumber(item.status.address))
									{
										Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(item.status.address));
										Cursor cur = activity.getContentResolver().query(uri,
												new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
										if (cur.moveToFirst()) {
											item.status.name = cur.getString(0);
											if (item.status.name != null && !item.status.name.trim().isEmpty()) {
												activity.runOnUiThread(new Runnable() {
													@Override
													public void run() {
														item.view.setText(item.status.name);
													}
												});
											}
										}
										cur.close();
									}
								}
							}
						}
					}
				});
				itemUpdater.start();
			}
			Item itm = new Item();
			itm.status = status;
			itm.view = text;
			itemsToUpdate.add(itm);
			synchronized (terminate) {
				terminate.notify();
			}
		}
		text.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				status.isSpam = b;
			}
		});
		count.setText(Integer.toString(status.count));

		return res;
	}
}
