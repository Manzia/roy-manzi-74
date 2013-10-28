package com.manzia.shopping.model;

import static org.junit.Assert.*;

/**
 * Helper class to create and assert object instances used
 * for testing purposes
 * @author Roy Manzi Tumubweinee, Manzia Corporation
 *
 */

public final class MzModelNumberHelper {
	
	public static final String modelNumTest = "A111-222-333";
	public static final String modelBrandTest = "Manzia";
	public static final String modelCategoryTest = "Shopping App";
	public static final String modelFileNameTest = "manziashopping.xml";
	public static final String retailerCategoryTest = "shopping apps";
	public static final String retailerItemIdTest = "A11112222";
	
	// create an instance
	public static MzModelNumber newMzModelNumber() {
		
		// we pass null for the MzSearchDetail relationship!!!
		return new MzModelNumber(modelNumTest, modelBrandTest,
				modelCategoryTest, modelFileNameTest, retailerCategoryTest,
				retailerItemIdTest, null);
	}
	
	// assert that input MzModelNumber instance has the same data
	// as the one from the MzModelNumberHelper class
	public static void assertMzModelNumber(MzModelNumber modelNumber) {
		assertNotNull("Null object returned", modelNumber);
		assertEquals("ModelNums are not equal", modelNumTest, modelNumber.getId().getModelNum());
		assertEquals("ModelBrands are not equal", modelBrandTest, modelNumber.getId().getModelBrand());
		assertEquals("ModelCategorys is not equal", modelCategoryTest, modelNumber.getModelCategory());
		assertEquals("ModelFileNames are not equal", modelFileNameTest, modelNumber.getModelFileName());
		assertEquals("RetailerCategorys are not equal", retailerCategoryTest, modelNumber.getRetailerCategory());
		assertEquals("RetailerItemIds are not equal", retailerItemIdTest, modelNumber.getRetailerItemId());
		assertNull("MzSearchDetail objects are not equal", modelNumber.getSearchItem());
				
	}
	
	// return our Primary Key
	public static MzModelNumberPK testPrimaryKey() {
		return new MzModelNumberPK(modelNumTest, modelBrandTest);
	}
}
