package com.manzia.shopping.model;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the search_items database table.
 * 
 */
@Entity
@Table(name="search_items")
@NamedQueries({
    @NamedQuery(name = "findAllSearchItems",query = "SELECT s "
    + "FROM MzSearchDetail s")
    , @NamedQuery(name = "findSearchDetailByDeviceId", query = "SELECT s FROM MzSearchDetail s "
    + "WHERE s.userDevice.deviceID = :deviceID " + "ORDER BY s.searchItemID")
    , @NamedQuery(name = "findSearchDetailsByStatus", query = "SELECT s FROM MzSearchDetail s " +
    		"WHERE s.searchStatus = :searchStatus " + "ORDER BY s.searchItemID")
    , @NamedQuery(name = "findSearchDetailsByDuration", query = "SELECT s FROM MzSearchDetail s " +
    		"WHERE s.searchDuration = :searchDuration " + "ORDER BY s.searchItemID")
})
public class MzSearchDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private int searchItemID;		//auto-generated
	private String searchBrand;
	private int searchDuration;
	private float searchPrice;
	private int searchStatus;
	private String searchTitle;
	private String searchOptions;
	private String searchProfile;
	private Timestamp searchCreated;
	private Timestamp searchModified;
	private List<MzModelNumber> modelNumbers;
	private MzUserDevice userDevice;

    public MzSearchDetail() {
    }
    
    // Main constructor
    public MzSearchDetail(
    		//int searchItemID,		//auto-generated
    		String searchBrand,
    		int searchDuration,
    		float searchPrice,
    		int searchStatus,
    		String searchTitle,
    		String searchOptions,
    		String searchProfile,
    		Timestamp searchCreated,
    		Timestamp searchModified,
    		MzUserDevice userDevice ) {
    	//this.searchItemID = searchItemID;
    	this.searchBrand = searchBrand;
    	this.searchDuration = searchDuration;
    	this.searchPrice = searchPrice;
    	this.searchStatus = searchStatus;
    	this.searchTitle = searchTitle;
    	this.searchOptions = searchOptions; // Format "key:value,key:value,key:value"
    	this.searchProfile = searchProfile;
    	this.searchCreated = searchCreated;
    	this.searchModified = searchModified;
    	this.userDevice = userDevice;
    	this.modelNumbers = new ArrayList<MzModelNumber>();
    }
    		

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int getSearchItemID() {
		return this.searchItemID;
	}

	public void setSearchItemID(int searchItemID) {
		this.searchItemID = searchItemID;
	}


	public String getSearchBrand() {
		return this.searchBrand;
	}

	public void setSearchBrand(String searchBrand) {
		this.searchBrand = searchBrand;
	}


	public int getSearchDuration() {
		return this.searchDuration;
	}

	public void setSearchDuration(int searchDuration) {
		this.searchDuration = searchDuration;
	}


	public float getSearchPrice() {
		return this.searchPrice;
	}

	public void setSearchPrice(float searchPrice) {
		this.searchPrice = searchPrice;
	}


	public int getSearchStatus() {
		return this.searchStatus;
	}

	public void setSearchStatus(int searchStatus) {
		this.searchStatus = searchStatus;
	}


	public String getSearchTitle() {
		return this.searchTitle;
	}

	public void setSearchTitle(String searchTitle) {
		this.searchTitle = searchTitle;
	}
	
	
	public String getSearchOptions() {
		return this.searchOptions;
	}

	public void setSearchOptions(String searchOptions) {
		this.searchOptions = searchOptions;
	}

	public String getSearchProfile() {
		return this.searchProfile;
	}

	public void setSearchProfile(String searchProfile) {
		this.searchProfile = searchProfile;
	}

	public Timestamp getSearchCreated() {
		return this.searchCreated;
	}

	public void setSearchCreated(Timestamp searchCreated) {
		this.searchCreated = searchCreated;
	}

	public Timestamp getSearchModified() {
		return this.searchModified;
	}

	public void setSearchModified(Timestamp searchModified) {
		this.searchModified = searchModified;
	}

	//bi-directional many-to-one association to MzModelNumber
	@OneToMany(mappedBy="searchItem", cascade={CascadeType.ALL})
	public List<MzModelNumber> getModelNumbers() {
		return this.modelNumbers;
	}

	public void setModelNumbers(List<MzModelNumber> modelNumbers) {
		this.modelNumbers = modelNumbers;
	}
	

	//bi-directional many-to-one association to MzUserDevice
	@ManyToOne(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name="deviceID")
	public MzUserDevice getUserDevice() {
		return this.userDevice;
	}

	public void setUserDevice(MzUserDevice userDevice) {
		this.userDevice = userDevice;
	}
	
}