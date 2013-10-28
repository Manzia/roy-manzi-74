package com.manzia.shopping.model;

import java.sql.Timestamp;

/**
 * Helper class that instantiates a MzUserDevice object
 * @author Roy Manzi Tumubweinee
 *
 */

public class MzUserDeviceHelper {
	public static final String deviceID = "415-309-7418";
	public static final byte[] deviceToken = "mydeviceToken".getBytes();
	public static final Timestamp tokenStamp = new Timestamp(System.currentTimeMillis());
	
	// create an instance
	// NOTE that passing a NULL for the tokenStamp will cause the
	// database to auto-update the tokenStamp column with the
	// the current timestamp
	public static MzUserDevice newMzUserDevice() {
		return new MzUserDevice(deviceID, deviceToken, tokenStamp);
	}
}
