package com.dj.antispam;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.dj.antispam.dao.SmsDao;

import java.util.ArrayList;
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

	private static final String TAG = "ImportListAdapter";
	private List<SenderStatus> senders;
	private final Activity activity;
	private volatile Boolean terminate = false;
	private Cursor conversations;
	private SmsDao dao;

	private class Item {
		SenderStatus status;
		CheckBox view;
	}
	private final Queue<Item> itemsToUpdate = new LinkedBlockingQueue<Item>();
	private Thread itemUpdater;

	public ImportListAdapter(Activity activity, SmsDao dao) {
		this.activity = activity;
		this.dao = dao;
	}

	private List<SenderStatus> getSenders() {
		if (senders == null) {
			senders = new ArrayList<SenderStatus>();
		}
		return senders;
	}

	private Cursor getConversations() {
		if (conversations == null) {
			conversations = activity.getContentResolver().query(Uri.parse("content://sms/conversations/"), null, null, null, "date DESC");
			conversations.moveToFirst();
		}
		return conversations;
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
		if (conversations != null) {
			conversations.close();
		}
	}

	@Override
	public int getCount() {
		return getConversations().getCount();
	}

	@Override
	public SenderStatus getItem(int i) {
		List<SenderStatus> s = getSenders();
		if (s.size() <= i) {
			for (int n = s.size(); n <= i; n++) {
				s.add(null);
			}
		}
		if (s.get(i) == null) {
			Cursor convs = getConversations();
			final int convPos = convs.getPosition();
			if (convPos != -1 && (i == convPos || convs.move(i-convPos))) {
				SenderStatus status = new SenderStatus(null, null, convs.getInt(convs.getColumnIndex("msg_count")));
				status.personId = convs.getLong(convs.getColumnIndex("thread_id"));
				s.set(i, status);
			}
		}
		return s.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		final SenderStatus status = getItem(i);
		final View res = activity.getLayoutInflater().inflate(R.layout.import_item, viewGroup, false);
		final CheckBox text = (CheckBox) res.findViewById(R.id.sender);
		final TextView count = (TextView) res.findViewById(R.id.messageCount);

		if (status.address != null) {
			text.setText(status.name == null ? status.address : status.name);
		}
		text.setChecked(status.isSpam != null && status.isSpam);
		if (status.isSpam == null || status.address == null || status.personId != null || status.name == null) {
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
									if (loadAddress(item) && loadDisplayName(item)) {
										suggestSpamStatus(item);
									}
								}
							}
						}
					}

					private boolean suggestSpamStatus(final Item item) {
						if (item.status.isSpam != null) return true;
						item.status.isSpam = checkSpam(item.status);
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								item.view.setChecked(item.status.isSpam);
								item.view.invalidate();
								item.view.postInvalidate();
							}
						});
						return false;
					}

					private boolean loadDisplayName(final Item item) {
						if (item.status.name != null) return true;
						item.status.name = item.status.address;
						if (PhoneNumberUtils.isGlobalPhoneNumber(item.status.address)) {
							Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(item.status.address));
							Cursor cur = activity.getContentResolver().query(uri,
									new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
							try {
								if (cur.moveToFirst()) {
									item.status.name = cur.getString(0);
									if (item.status.name != null && !item.status.name.trim().isEmpty()) {
										activity.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												item.view.setText(item.status.name);
												item.view.invalidate();
												item.view.postInvalidate();
											}
										});
									}
								}
							} finally {
								if (cur != null) {
									cur.close();
								}
							}
						}
						if (item.status.isSpam == null && item.status.name == item.status.address) {
							itemsToUpdate.add(item);
						}
						return false;
					}

					private boolean loadAddress(final Item item) {
						if (item.status.address != null) return true;
						Uri uri = Uri.parse("content://sms/inbox");
						String where = "thread_id=" + item.status.personId;
						Cursor mycursor = activity.getContentResolver().query(uri, null, where, null, null);
						item.status.address = "???";
						try {
							if (mycursor.moveToFirst()) {
								item.status.address = mycursor.getString(mycursor.getColumnIndex("address"));
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										item.view.setText(item.status.address);
										item.view.invalidate();
										item.view.postInvalidate();
										if (item.status.isSpam == null || item.status.name == null) {
											itemsToUpdate.add(item);
											synchronized (terminate) {
												terminate.notify();
											}
										}
									}
								});
							} else {
								Log.w(TAG, "Invalid conversation thread id " + item.status.personId);
							}
						} finally {
							if (mycursor != null) {
								mycursor.close();
							}
						}
						return false;
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

	private boolean checkSpam(SenderStatus status) {
		if (!PhoneNumberUtils.isGlobalPhoneNumber(status.address)) {
			return true;
		}
		Boolean seen = dao.isSenderASpammer(status.address);
		if (seen != null) {
			return seen;
		}
		if (status.read != null && !status.read && status.count == 1) {
			return true;
		}
		return false;
	}
}
