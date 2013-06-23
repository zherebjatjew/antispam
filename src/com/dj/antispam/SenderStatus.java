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
	public Boolean isSpam;
	public int count;
	public Boolean read;

	public SenderStatus(String address, int count) {
		this.address = address;
		this.count = count;
	}
}
