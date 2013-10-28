package com.manzia.shopping.bestbuy;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a Best Buy product review.
 * 
 * Note that not all fields are populated on every response.
 * Be sure to check for null values, especially for lists, or complex child elements.
 * 
 * @author  Roy Manzi Tumubweinee, Jan 10, 2013, Manzia Corporation
 * @version $Id$
 */

public class Review extends Entity {
	
	/**
     * Creates a new Review containing no data.
     */
    public Review() {
        super();
    }
    
    /**
     * Creates a new Review from a document representation.
     * 
     * @param element Root element of a Review representation
     */
    public Review(Element element) {
        super(element);
    }
    
    /**
     * Returns the "SKU" property associated with this review.
     * 
     * @return SKU # of this review
     */
    public String getSku() {
        return (String) getField("sku");
    }
    
    /**
     * Returns the "id" property associated with this review.
     * 
     * @return id of this review
     */
    public String getReviewId() {
        return (String) getField("id");
    }
    
    /**
     * Returns the "rating" property associated with this review.
     * 
     * @return rating value of this review e.g 5.0
     */
    public float getRating() {
        return Float.valueOf((String) getField("rating"));
    }
    
    /**
     * Returns the "title" property associated with this review.
     * 
     * @return title of this review
     */
    public String getTitle() {
        return (String) getField("title");
    }
    
    /**
     * Returns the "comment" property associated with this review.
     * 
     * @return comment string of this review
     */
    public String getComment() {
        return (String) getField("comment");
    }
    
    /**
     * Returns the "submissionTime" property associated with this review.
     * 
     * @return submissionTime of this review as a string e.g 2011-06-22T22:36:29
     */
    public String getSubmissionTime() {
        return (String) getField("submissionTime");
    }
    
    /**
     * Returns the name of the reviewer associated with this review
     * 
     * @return name of reviewer or empty String if none
     */
    public String getReviewerName() {
		String reviewerName = new String();
    	Object reviewer = getField("reviewer");
		if (reviewer != null && reviewer instanceof Element && ((Element) reviewer).hasChildren()) {
			for (Element child : ((Element) reviewer).getChildren()) {
				if (child.getName().equals("name")) {
					reviewerName = (String)child.getValue();
					break;
				}
			}
		}
    	return reviewerName;    	
    }

}
