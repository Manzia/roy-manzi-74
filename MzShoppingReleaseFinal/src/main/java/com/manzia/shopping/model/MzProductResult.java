package com.manzia.shopping.model;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: MzProductResult
 *
 */
@Entity
@Table(name="product_results")
@NamedQueries({
    @NamedQuery(name = "findAllProductResults",query = "SELECT p "
    + "FROM MzProductResult p")
    , @NamedQuery(name = "findProductsByModelNum", query = "SELECT p FROM MzProductResult p "
    + "WHERE p.modelNumber.id.modelNum = :modelNum " + "ORDER BY p.productID")
    , @NamedQuery(name = "findProductResultById", query = "SELECT DISTINCT p FROM MzProductResult p "
    + "WHERE p.productID = :productID AND p.modelNumber.id.modelNum = :modelNum")
})
public class MzProductResult implements Serializable {

	
	private int productID;
	private String productTitle;
	private String productDetailURL;
	private String productImageURL;
	private String productThumbnail;
	private String productBrand;
	private float productPrice;
	private String productCondition;
	private String productAvail;
	private String vendorID;
	private String productDesc;
	private String productLanguage;
	private String productCountry;
	private String productClass;
	private String productSubClass;
	private MzModelNumber modelNumber;
	private static final long serialVersionUID = 1L;

	public MzProductResult() {
		super();
	}
	
	// Main Constructor
	public MzProductResult(
			//int productID,
			String productTitle,
			String productDetailURL,
			String productImageURL,
			String productThumbnail,
			String productBrand,
			float productPrice,
			String productCondition,
			String productAvail,
			String vendorID,
			String productDesc,
			String productLanguage,
			String productCountry,
			String productClass,
			String productSubClass,
			MzModelNumber modelNumber ) {
		//this.productID = productID;
		this.productTitle = productTitle;
		this.productDetailURL = productDetailURL;
		this.productImageURL = productImageURL;
		this.productThumbnail = productThumbnail;
		this.productBrand = productBrand;
		this.productPrice = productPrice;
		this.productCondition = productCondition;
		this.productAvail = productAvail;
		this.vendorID = vendorID;
		this.productDesc = productDesc;
		this.productLanguage = productLanguage;
		this.productCountry = productCountry;
		this.productClass = productClass;
		this.productSubClass = productSubClass;
		this.modelNumber = modelNumber;
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int getProductID() {
		return this.productID;
	}

	public void setProductID(int productID) {
		this.productID = productID;
	}   
	public String getProductTitle() {
		return this.productTitle;
	}

	public void setProductTitle(String productTitle) {
		this.productTitle = productTitle;
	}   
	public String getProductDetailURL() {
		return this.productDetailURL;
	}

	public void setProductDetailURL(String productDetailURL) {
		this.productDetailURL = productDetailURL;
	}   
	public String getProductImageURL() {
		return this.productImageURL;
	}

	public void setProductImageURL(String productImageURL) {
		this.productImageURL = productImageURL;
	}   
	public String getProductThumbnail() {
		return this.productThumbnail;
	}

	public void setProductThumbnail(String productThumbnail) {
		this.productThumbnail = productThumbnail;
	}   
	public String getProductBrand() {
		return this.productBrand;
	}

	public void setProductBrand(String productBrand) {
		this.productBrand = productBrand;
	}   
	public float getProductPrice() {
		return this.productPrice;
	}

	public void setProductPrice(float productPrice) {
		this.productPrice = productPrice;
	}   
	public String getProductCondition() {
		return this.productCondition;
	}

	public void setProductCondition(String productCondition) {
		this.productCondition = productCondition;
	}   
	public String getProductAvail() {
		return this.productAvail;
	}

	public void setProductAvail(String productAvail) {
		this.productAvail = productAvail;
	}   
	public String getVendorID() {
		return this.vendorID;
	}

	public void setVendorID(String vendorID) {
		this.vendorID = vendorID;
	}   
	public String getProductDesc() {
		return this.productDesc;
	}

	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}   
	public String getProductLanguage() {
		return this.productLanguage;
	}

	public void setProductLanguage(String productLanguage) {
		this.productLanguage = productLanguage;
	}   
	public String getProductCountry() {
		return this.productCountry;
	}

	public void setProductCountry(String productCountry) {
		this.productCountry = productCountry;
	}   
	public String getProductClass() {
		return this.productClass;
	}

	public void setProductClass(String productClass) {
		this.productClass = productClass;
	}   
	public String getProductSubClass() {
		return this.productSubClass;
	}

	public void setProductSubClass(String productSubClass) {
		this.productSubClass = productSubClass;
	}
	
	//bi-directional many-to-one association to MzModelNumber
	@ManyToOne(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="modelNum", referencedColumnName="modelNum")
		, @JoinColumn(name="modelBrand", referencedColumnName="modelBrand")
	})		
	public MzModelNumber getModelNumber() {
		return this.modelNumber;
	}

	public void setModelNumber(MzModelNumber modelNum) {
		this.modelNumber = modelNum;
	}
   
}
