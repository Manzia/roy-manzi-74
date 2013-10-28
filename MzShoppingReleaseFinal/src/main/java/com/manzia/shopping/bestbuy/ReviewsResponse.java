package com.manzia.shopping.bestbuy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReviewsResponse extends CollectionResponse {
	
	/**
     * List of {@link Review reviews} returned with this response 
     */
    private List<Review> reviews;

	/**
     * Creates a new ReviewsResponse from a reviews XML file
     * 
     * @param reviewsResponse	reviews XML file
     * @throws RemixException	thrown when there is an error parsing the product XML file
     * 
     * @author Roy Manzi Tumubweinee, Jan.10, 2013, Manzia Corporation
     * 
     */
	
	public ReviewsResponse(File reviewsResponse) throws RemixException {
		super(reviewsResponse);		
	}
	
	/**
     * Returns a list of {@link Review reviews} returned with this response.
     * 
     * @return List of {@link Review reviews} returned with this response
     */
	@Override
	public List<Review> list() {
		if(null == reviews) {
            reviews = new ArrayList<Review>();
            Element doc = getDocumentRoot();
            if(null != doc && doc.hasChildren()) {
                for(Element review : doc.getChildren()) {
                    reviews.add(new Review(review));
                }
            }
        }
        return reviews;
	}

}
