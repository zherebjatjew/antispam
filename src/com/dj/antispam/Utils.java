package com.dj.antispam;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 23.06.13
 * Time: 21:51
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
	public static final String URI_SMS = "content://sms";
	public static final String URI_INBOX = URI_SMS + "/inbox";
	public static final String URI_THREADS = "content://sms/conversations/";
	public static final int    MESSAGE_TYPE_SMS = 1;
	public static final String MESSAGE_ADDRESS = "address";
	public static final String MESSAGE_BODY = "body";
	public static final String MESSAGE_READ = "read";
	public static final String MESSAGE_TYPE = "type";
	public static final String MESSAGE_DATE = "date";

	public static String join(List items, Processor processor) {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < items.size(); i++) {
			processor.format(res, items.get(i));
			if (i < items.size()-1) {
				res.append(",");
			}
		}
		return res.toString();
	}

	public interface Processor {
		void format(StringBuilder builder, Object item);
	}
}
