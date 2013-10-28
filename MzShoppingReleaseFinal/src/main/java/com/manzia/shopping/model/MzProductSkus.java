package com.manzia.shopping.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the product_skus database table.<br>
 * This entity is kept simple and has no table associations so
 * it can be used across different retailers.
 * 
 */
@Entity
@Table(name="product_skus")
@NamedQueries({
    @NamedQuery(name = "findAllProductSkus",query = "SELECT p "
    + "FROM MzProductSkus p")
    , @NamedQuery(name = "findSkusByModelAndBrand", query = "SELECT DISTINCT p FROM MzProductSkus p "
    + "WHERE p.productModel = :productModel AND p.productBrand = :productBrand " + "ORDER BY p.productSku")
    , @NamedQuery(name = "findProductCategoryForSku", query = "SELECT DISTINCT p.productCategory FROM MzProductSkus p "
    + "WHERE p.productSku = :productSku")
    , @NamedQuery(name = "findAllProductCategories", query = "SELECT DISTINCT p.productCategory FROM MzProductSkus p "
    	    + "ORDER BY p.productCategory")
    , @NamedQuery(name = "findProductSkusByCategory", query = "SELECT DISTINCT p.productSku FROM MzProductSkus p "
    	    + "WHERE p.productCategory =:productCategory " + "ORDER BY p.productSku")
})
public class MzProductSkus implements Serializable {
	private static final long serialVersionUID = 1L;
	private String productSku;
	private String productBrand;
	private String productCategory;
	private String productModel;
	private String retailerName;
	
	// No args constructor
    public MzProductSkus() {
    }
    
    // Main Constructor
    public MzProductSkus(String productSku, String productBrand,
			String productCategory, String productModel, String retailerName) {
		super();
		this.productSku = productSku;
		this.productBrand = productBrand;
		this.productCategory = productCategory;
		this.productModel = productModel;
		this.retailerName = retailerName;
	}
	
	@Id
	public String getProductSku() {
		return this.productSku;
	}

	public void setProductSku(String productSku) {
		this.productSku = productSku;
	}


	public String getProductBrand() {
		return this.productBrand;
	}

	public void setProductBrand(String productBrand) {
		this.productBrand = productBrand;
	}


	public String getProductCategory() {
		return this.productCategory;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}


	public String getProductModel() {
		return this.productModel;
	}

	public void setProductModel(String productModel) {
		this.productModel = productModel;
	}
	
	public String getRetailerName() {
		return this.retailerName;
	}
	
	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}

}