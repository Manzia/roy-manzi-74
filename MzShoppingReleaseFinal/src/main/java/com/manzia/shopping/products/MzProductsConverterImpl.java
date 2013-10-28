package com.manzia.shopping.products;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.manzia.shopping.bestbuy.Product;

/**
 * Note: {@link MzProductsConverterImpl} is a CDI bean with @Dependent scope
 * @see MzProductsConverter
 * @author Roy Manzi Tumubweinee, Oct 9, 2012, Manzia Corporation
 *
 */
@MzBestBuyConverter
public class MzProductsConverterImpl implements MzProductsConverter {
	
	// Default Number of Products to Search
	private static final int topKProducts = 5;
	private static final float KMAXIMUMALLOWEDPRICE = 1000000000.0f; // Price cannot exceed 1 billion regardless of currency!!
	private static final String KNullAttributeValue = "null";
		
	//Logger
			public static final Logger logger = 
						Logger.getLogger(MzProductsConverterImpl.class.getCanonicalName());

	/**
	 * <p>Method converts a list of Product (BestBuy API) to a RankedProducts object by</p>
	 * 1- ranks the Product by how close its price is to the price in the query Parameters<br>
	 * 2- depending on the size of the Product list, returns the top K closest (in terms of price)
	 * as a list of RankedProductType in the RankedProducts object.<br>
	 * <p>NOTE: The RankedProducts object adheres to the JAXB interface and can easily to marshalled or
	 * unmarshalled using the JAXB API.</p> 
	 * 
	 * @param product - List of Product to be ranked into RankedProducts
	 * @param price - price point that will be used to base the ranking
	 * @param topProducts - number of top K closest products to return as RankedProducts
	 * @return - RankedProducts
	 */
	@Deprecated
	@Override
	public RankedProducts convertToRankedProducts(List<Product> product, Float price, int topProducts) {
		return null;
		
		/*
		 * Implementation uses a ConcurrentSkipListMap with a Float (Product Price) as Key
		 * and Integer (index of associated Product in the product List) as Value. This data structure
		 * provides a lot of the required logic "off-the-shelf".
		 */
		
		
	}
	
	/**
	 * Generates a RankedProductType from a Product object (BestBuy-specific API)
	 * 
	 * @param productItem - Product object to be transformed
	 * @return - RankedProductType
	 */
	public RankedProductType convertToRankedProductType( Product productItem ) {
		
		RankedProductType rankedProductType;
		
		// check inputs
		if (productItem == null) {
			logger.log(Level.WARNING, "Cannot convert null Product to RankedProductType...will return NULL!");
			return null;
		}
		
		// Instantiate
		ObjectFactory rankedTypeFactory = new ObjectFactory();
		rankedProductType = rankedTypeFactory.createRankedProductType();
		assert rankedProductType != null;
		
		//Title
		RankedProductType.Title productTitle = rankedTypeFactory.createRankedProductTypeTitle();
		assert productTitle != null;
		productTitle.setType("text");
		productTitle.setValue(productItem.getName());
		rankedProductType.setTitle(productTitle);
		
		// Link
		LinkType productLink = rankedTypeFactory.createLinkType();
		assert productLink != null;
		productLink.setRel("alternate");
		productLink.setType("text/html");
		productLink.setHref(productItem.getMobileUrl());
		rankedProductType.setLink(productLink);
		
		//Image Link
		rankedProductType.setImageLink(productItem.getImage());
		
		//Thumbnail Link
		rankedProductType.setThumbnailLink(productItem.getThumbnailImage());
		
		// Product Id
		rankedProductType.setId(productItem.getSku());
		
		//Description
		rankedProductType.setDescription(productItem.getLongDescription());
		
		//Language
		rankedProductType.setContentLanguage("en");
		
		//Country
		rankedProductType.setTargetCountry("USA");
		
		//Product Type
		ProductType productType = rankedTypeFactory.createProductType();
		assert productType != null;
		productType.setClassId(productItem.getProductClass());
		productType.setSubClassId(productItem.getSubclass());
		productType.setValue(productItem.getDepartment());
		rankedProductType.setProductType(productType);
		
		//Price Type
		PriceType productPrice = rankedTypeFactory.createPriceType();
		assert productPrice != null;
		float price = productItem.getSalePrice() > 0 ? productItem.getSalePrice() : productItem.getRegularPrice();
		productPrice.setUnit("usd");
		productPrice.setValue(Float.toString(price));
		rankedProductType.setPrice(productPrice);
		
		//Brand - replace "unknown" with "Generic"
		rankedProductType.setBrand(productItem.getManufacturer());
				
		//Condition
		String condition = productItem.isNew() ? "new" : "used";
		rankedProductType.setCondition(condition);
		
		// Availability
		rankedProductType.setAvailability(productItem.getOrderable());
		
		return rankedProductType;
		
	}
	
	/**
	 * Method that determines if a particular Product object is valid, where validity
	 * is defined as follows: <br>
	 * <p>1- has a non-null Product Title<br>
	 * 2- has a non-null Product Price<br>
	 * 3- has a non-null Product URL </p>
	 * @param aProduct - the Product object to be validated
	 * @return - true if valid else false
	 */
	public boolean isValidProduct(Product aProduct) {
		
		//check input
		boolean isValid = false;
		if (aProduct == null) { return isValid; }
		
		// Validate
		if (aProduct.getName() != null && aProduct.getMobileUrl() != null) {
			isValid = aProduct.getName().equalsIgnoreCase(KNullAttributeValue) ? false : true;
			if (!isValid) { return isValid = false; }
			
			if ((aProduct.getRegularPrice() > 0.0f) || (aProduct.getSalePrice() > 0.0f)) {
				isValid = true;
			} else { return isValid = false; }
			
			isValid = aProduct.getMobileUrl().equalsIgnoreCase(KNullAttributeValue) ? false : true;
			if (!isValid) { return isValid = false; }	
		}		
		return isValid;
	}

}
