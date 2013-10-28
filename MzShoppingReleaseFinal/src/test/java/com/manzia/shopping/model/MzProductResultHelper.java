package com.manzia.shopping.model;

/**
 * Helper class that instantiates an MzProductResult object
 * @author Roy Manzi Tumubweinee
 *
 */

public class MzProductResultHelper {
	
	public static final String productTitle = "The best smartphone";
	public static final String productDetailURL = "http://www.manzia.com";
	public static final String productImageURL = "http//www.manzia.com/images";
	public static final String productThumbnail = "http://www.manzia.com/thumbnail";
	public static final String productBrand = "Manzia";
	public static final float productPrice = 25.75f;
	public static final String productCondition = "New";
	public static final String productAvail = "In Stock";
	public static final String vendorID = "A100011";
	public static final String productDesc = "Has a 4.1inch screen with Retina display, supports 4G LTE everywhere, can also" +
			"also make you a cup of coffee and fix itself";
	public static final String productLanguage = "en";
	public static final String productCountry = "USA";
	public static final String productClass = "Phones";
	public static final String productSubClass = "Contract SmartPhones";
	private static final MzModelNumber modelNumber = MzModelNumberHelper.newMzModelNumber();
	
	// create an instance
	public static MzProductResult newMzProductResult() {
		return new MzProductResult(productTitle, productDetailURL, productImageURL, productThumbnail,
				productBrand, productPrice, productCondition, productAvail, vendorID, productDesc, 
				productLanguage, productCountry, productClass, productSubClass, modelNumber);
	}
	
	// assert that input object is the same as static object
	
}
