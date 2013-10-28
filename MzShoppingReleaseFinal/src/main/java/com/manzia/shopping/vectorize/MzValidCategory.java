package com.manzia.shopping.vectorize;

/**
 * Helper class that represents a valid Product Category.
 * @author Roy Manzi Tumubweinee, Sept. 29, 2012, Manzia Corporation
 *
 */

public final class MzValidCategory {
	
	private boolean validCategory;
	private String productCategory;
	
	// Constructor
	public MzValidCategory(boolean validCategory, String productCategory) {
		super();
		this.validCategory = validCategory;
		this.productCategory = productCategory;
	}

	public boolean isValidCategory() {
		return validCategory;
	}

	public void setValidCategory(boolean validCategory) {
		this.validCategory = validCategory;
	}

	public String getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}	

}
