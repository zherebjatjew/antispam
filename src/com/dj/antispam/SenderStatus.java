package com.dj.antispam;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 23.06.13
 * Time: 13:14
 * To change this template use File | Settings | File Templates.
 */
public class SenderStatus {
	public String address;
	public boolean isSpam;
	public int count;

	public SenderStatus(String address, boolean isSpam, int count) {
		this.address = address;
		this.isSpam = isSpam;
		this.count = count;
	}
}
