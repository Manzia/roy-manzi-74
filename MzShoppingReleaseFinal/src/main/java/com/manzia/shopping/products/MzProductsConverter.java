package com.manzia.shopping.products;

import java.util.List;

import com.manzia.shopping.bestbuy.Product;

/**
 * Interface for converting Product objects (BestBuy API) to RankedProducts
 * 
 * @author Roy Manzi Tumubweinee, Oct 9, 2012, Manzia Corporation
 *
 */

public interface MzProductsConverter {
	/**
	 * <p>Method converts a list of Product (BestBuy API) to a RankedProducts object by</p>
	 * 1- ranks the Product by how close its price is to the price in the query Parameters<br>
	 * 2- depending on the size of the Product list, returns the top K closest (in terms of price)
	 * as a list of RankedProductType in the RankedProducts object.<br>
	 * <p>NOTE: The RankedProducts object adheres to the JAXB interface and can easily to marshalled or
	 * unmarshalled using the JAXB API.</p> 
	 * 
	 * @param product - List of Product to be ranked into RankedProducts
	 * @param price - price point that will be used to rank the products
	 * @param topProducts - number of top K closest products to return as RankedProducts
	 * @return - RankedProducts
	 */
	public RankedProducts convertToRankedProducts( List<Product> product, Float price, int topProducts);

}
