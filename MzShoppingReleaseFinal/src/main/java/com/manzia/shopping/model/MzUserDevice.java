package com.manzia.shopping.model;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the user_devices database table.
 * 
 */
@Entity
@Table(name="user_devices")
@NamedQueries({
    @NamedQuery(name = "findAllDevices",query = "SELECT d "
    + "FROM MzUserDevice d")    
})
public class MzUserDevice implements Serializable {
	private static final long serialVersionUID = 1L;
	private String deviceID;
	private byte[] deviceToken;
	private Timestamp tokenStamp;
	private List<MzSearchDetail> searchItems;

    public MzUserDevice() {
    }
    
    // Main constructor
    public MzUserDevice (
    		String deviceID,
    		byte[] deviceToken,
    		Timestamp tokenStamp ) {
    	this.deviceID = deviceID;
    	this.deviceToken = deviceToken;
    	this.tokenStamp = tokenStamp;
    	this.searchItems = new ArrayList<MzSearchDetail>();
    }

	@Id
	public String getDeviceID() {
		return this.deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}


	public byte[] getDeviceToken() {
		return this.deviceToken;
	}

	public void setDeviceToken(byte[] deviceToken) {
		this.deviceToken = deviceToken;
	}


	public Timestamp getTokenStamp() {
		return this.tokenStamp;
	}

	public void setTokenStamp(Timestamp tokenStamp) {
		this.tokenStamp = tokenStamp;
	}


	//bi-directional many-to-one association to MzSearchDetail
	@OneToMany(mappedBy="userDevice", cascade={CascadeType.ALL})
	public List<MzSearchDetail> getSearchItems() {
		return this.searchItems;
	}

	public void setSearchItems(List<MzSearchDetail> searchItems) {
		this.searchItems = searchItems;
	}
	
}