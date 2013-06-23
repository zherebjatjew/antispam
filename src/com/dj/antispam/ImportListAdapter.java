package com.dj.antispam;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 23.06.13
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
public class ImportListAdapter extends BaseAdapter {

	private final List<SenderStatus> senders;
	private final Activity activity;

	public ImportListAdapter(Activity activity, List<SenderStatus> senders) {
		this.activity = activity;
		this.senders = senders;
	}

	@Override
	public int getCount() {
		return senders.size();
	}

	@Override
	public Object getItem(int i) {
		return senders.get(i);
	}

	@Override
	public long getItemId(int i) {
		return senders.get(i).address.hashCode();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		final SenderStatus status = senders.get(i);
		final View res = activity.getLayoutInflater().inflate(R.layout.import_item, viewGroup, false);
		final CheckBox text = (CheckBox) res.findViewById(R.id.sender);
		final TextView count = (TextView) res.findViewById(R.id.messageCount);

		text.setText(status.address);
		text.setChecked(status.isSpam);
		count.setText(Integer.toString(status.count));

		return res;
	}
}
