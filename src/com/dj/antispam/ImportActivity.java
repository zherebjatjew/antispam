package com.dj.antispam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.dj.antispam.dao.SmsDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 23.06.13
 * Time: 13:18
 * To change this template use File | Settings | File Templates.
 */
public class ImportActivity extends Activity {

	public static final int FIRST_IMPORT = 1;

	private ImportListAdapter adapter;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.importer);
		final ListView senders = (ListView) findViewById(R.id.listView);
		final List<SenderStatus> senderStates = getSenderStates();
		adapter = new ImportListAdapter(this, senderStates);
		senders.setAdapter(adapter);
	}

	private List<SenderStatus> getSenderStates() {
		final List<SenderStatus> res = new ArrayList<SenderStatus>();
		res.add(new SenderStatus("+000", true, 1));
		res.add(new SenderStatus("Julia", false, 2));
		return res;
	}

	public void onOk(View view) {
		// Save checkboxes to spam list
		final SmsDao dao = new SmsDao(this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Map<String, Boolean> senders = new HashMap<String, Boolean>(adapter.getCount());
				List<String> denied = new ArrayList<String>();
				List<String> allowed = new ArrayList<String>();
				for (int i = 0; i < adapter.getCount(); i++) {
					SenderStatus status = (SenderStatus) adapter.getItem(i);
					if (status.isSpam) {
						denied.add(status.address);
					} else {
						allowed.add(status.address);
					}
				}
				dao.markSender(denied, true);
				dao.markSender(allowed, false);
				Intent intent = new Intent(getResources().getString(R.string.update_action));
				sendBroadcast(intent);

			}
		}).start();

		setResult(RESULT_OK);
		finish();
	}

	public void onCancel(View view) {
		this.finishActivity(0);
	}
}
