package com.dj.antispam;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 23.06.13
 * Time: 13:18
 * To change this template use File | Settings | File Templates.
 */
public class ImportActivity extends Activity {
	public static final int FIRST_IMPORT = 1;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.importer);
		final ListView senders = (ListView) findViewById(R.id.listView);
		final List<SenderStatus> senderStates = getSenderStates();
		senders.setAdapter(new ImportListAdapter(this, senderStates));
	}

	private List<SenderStatus> getSenderStates() {
		final List<SenderStatus> res = new ArrayList<SenderStatus>();
		res.add(new SenderStatus("+000", true, 1));
		res.add(new SenderStatus("Julia", false, 2));
		return res;
	}
}
