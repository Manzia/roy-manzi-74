package com.manzia.shopping.model;

import static org.junit.Assert.*;

import java.sql.Timestamp;
/**
 * Helper class to instantiate a MzSearchDetail object
 * @author Roy Manzi Tumubweinee
 *
 */

public class MzSearchDetailHelper {
	
	public static final String searchBrand = "Manzia";
	public static final int searchDuration = 7;
	public static final float searchPrice = 55.35f;
	public static final int searchStatus = 2;
	public static final String searchTitle ="Best Smartphone";
	public static final String searchOptions ="Category:Phone,DisplayType:Touchscreen,Processor:ARM,Camera:8MegaPixel";
	public static final String searchProfile = "techno-geek";
	public static final Timestamp searchCreated = new Timestamp( System.currentTimeMillis());
	public static final Timestamp searchModified = new Timestamp( System.currentTimeMillis());
	private static final MzUserDevice userDevice = MzUserDeviceHelper.newMzUserDevice();
	
	// create an instance
	public static MzSearchDetail newMzSearchDetail() {
		return new MzSearchDetail(searchBrand, searchDuration, searchPrice, 
				searchStatus, searchTitle, searchOptions, searchProfile, searchCreated, searchModified, userDevice);
	}
	
	public static void assertMzSearchDetail(MzSearchDetail newSearch) {
		assertTrue("Search Brand does not match", newSearch.getSearchBrand().equalsIgnoreCase(searchBrand));
		assertTrue("Search Duration doensn't match", newSearch.getSearchDuration() == searchDuration);
		assertTrue("Search Price doesn't match", newSearch.getSearchPrice() == searchPrice);
		assertTrue("Search Status doesn't match", newSearch.getSearchStatus() == searchStatus);
		assertTrue("Search Title doesn't match", newSearch.getSearchTitle().equalsIgnoreCase(searchTitle));
		assertTrue("Search Options do not match", newSearch.getSearchOptions().equalsIgnoreCase(searchOptions));
		assertTrue("Search Profile doesn't match", newSearch.getSearchProfile().equalsIgnoreCase(searchProfile));
		assertTrue("Search Created Timestamp doesn't match", newSearch.getSearchCreated().equals(searchCreated));
		assertTrue("Search Modified Timestamp doesn't match", newSearch.getSearchModified().equals(searchModified));
		assertTrue("Device IDs do not match", newSearch.getUserDevice().getDeviceID().equals(userDevice.getDeviceID()));
	}
}
