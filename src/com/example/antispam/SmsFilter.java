package com.example.antispam;

import android.telephony.SmsMessage;
import com.example.antispam.dao.SmsDao;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 31.05.13
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */
public class SmsFilter {
	private SmsDao dao;

	public SmsFilter(SmsDao dao) {
		this.dao = dao;
	}
	public boolean isUnwelcome(SmsMessage message) {
		String from = message.getOriginatingAddress();
		if (isFromWhiteList(from)) return false;
		if (isFromGateway(from)) return true;
		if (isFromBlackList(from)) return true;
		return false;
	}

	private boolean isFromBlackList(String sender) {
		return false;
	}

	private boolean isFromWhiteList(String sender) {
		return false;
	}

	private boolean isFromGateway(String sender) {
		return false;
	}
}
