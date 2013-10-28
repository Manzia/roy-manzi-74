package com.manzia.shopping.reviews;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.manzia.shopping.dao.MzProductSkusDao;
import com.manzia.shopping.model.MzProductSkus;

/**
 * MzReviewsConverter converts {@link SolrDocumentList} type to {@link ReviewMatches} type. <br>
 * Note: {@link MzReviewsConverter} is a CDI bean with the default @Dependent scope so its lifecycle
 * is dependent on the lifecycle of the client(injection point)
 * 
 * @author Roy Manzi Tumubweinee, Feb 02, 2013, Manzia Corporation
 *
 */
public class MzReviewsConverter {
	
	//Logger
	public static final Logger logger = 
				Logger.getLogger(MzReviewsConverter.class.getCanonicalName());
	
	/**
	 * Method converts {@link SolrDocumentList} type to {@link com.manzia.shopping.reviews.ReviewMatches} type <br>
	 * 
	 * @param docList - {@link org.apache.solr.common.SolrDocumentList} to convert
	 * @param skuDao - {@link MzProductSkusDao} data access object. Required since for every {@link SolrDocument}
	 * returned from the Index we have to query the database (product_sku table) to determine the associated
	 * review's source i.e Best Buy, Amazon etc
	 * 
	 * @return - converted {@link ReviewMatches} object
	 */
	public ReviewMatches convertToReviewMatchesType( SolrDocumentList docList, MzProductSkusDao skuDao) {
		
		// Output
		ObjectFactory reviewsFactory = new ObjectFactory();
		ReviewMatches reviewMatches = reviewsFactory.createReviewMatches();
		assert reviewMatches != null;
		
		// check inputs
		if (docList == null || docList.isEmpty()) {
			logger.log(Level.WARNING, "Expected SolrDocumentList type to convert BUT got Null or Empty SolrDocumentList!");
			return reviewMatches;
		}
		
		if (skuDao == null) {
			logger.log(Level.WARNING, "Null MzProductSkusDao object was specified..cannot convert SolrDocumentList to ReviewMatches!");
			return reviewMatches;
		}
		
		// Iterate
		for (SolrDocument doc : docList) {
			try {
				ReviewMatchType reviewType = this.convertReviewMatchType(doc, skuDao);
				if (reviewType != null) {
					reviewMatches.getReviewMatch().add(reviewType);
				}
				
			} catch (DatatypeConfigurationException e) {
				logger.log(Level.SEVERE, "javax.xml.datatype.DatatypeConfigurationException while instantiating XMLGregorianCalendar type: " 
			+ e.getLocalizedMessage());
				throw new RuntimeException("javax.xml.datatype.DatatypeConfigurationException while instantiating XMLGregorianCalendar type: " 
			+ e.getLocalizedMessage());
			}
		}
		
		return reviewMatches;		
	}
	
	/**
	 * Helper method that converts {@link SolrDocument} type to {@link ReviewMatchType} type <br>
	 * 
	 * @param solrDoc - {@link SolrDocument} to convert
	 * @param skuDao - {@link MzProductSkusDao} data access object. Required since for every {@link SolrDocument}
	 * returned from the Index we have to query the database (product_sku table) to determine the associated
	 * review's source i.e Best Buy, Amazon etc
	 * @return - converted {@link ReviewMatchType} object 
	 * @throws DatatypeConfigurationException - thrown by {@link DataTypeFactory} instance method
	 */
	@SuppressWarnings("unchecked")
	protected ReviewMatchType convertReviewMatchType( SolrDocument solrDoc, MzProductSkusDao skuDao ) 
			throws DatatypeConfigurationException {
		
		// check inputs
		if (solrDoc == null) {
			logger.log(Level.WARNING, "Null SolrDocument specified..cannot convert to ReviewMatchType!");
			return null;
		}
		
		// Assign values
		ReviewMatchType reviewMatch = null;
				
		List<String> reviewTitleList = null;
		List<String> reviewContentList = null;
		if (solrDoc.getFieldValue("title") instanceof java.util.ArrayList<?>) {
			reviewTitleList = (ArrayList<String>)solrDoc.getFieldValue("title"); // reviewTitle is characterized as multivalued=true in Solr Schema.xml
		}
		if (solrDoc.getFieldValue("content") instanceof java.util.ArrayList<?>) {
			reviewContentList = (ArrayList<String>)solrDoc.getFieldValue("content"); // reviewContent is characterized as multivalued=true in Solr Schema.xml
		}
		String reviewTitle = mergeStringList(reviewTitleList);
		String reviewSku = (String)solrDoc.getFieldValue("sku");
		String reviewCategory = (String)solrDoc.getFieldValue("category");
		String reviewId = (String)solrDoc.getFieldValue("id");
		String reviewContent = mergeStringList(reviewContentList);
		String reviewAuthor = (String)solrDoc.getFieldValue("author");
		Float reviewRating = (Float)solrDoc.getFieldValue("rating");
		Date reviewTime = (Date)solrDoc.getFieldValue("submissionTime");
		
		/* Testing purposes
		if (reviewTitle == null || reviewTitle.isEmpty()) System.out.println("Review Title is null or empty");
		if (reviewContent == null || reviewContent.isEmpty()) System.out.println("Review Content is null or empty");
		if (reviewSku == null || reviewSku.isEmpty()) System.out.println("Review Sku is null or empty");
		if (reviewCategory == null || reviewCategory.isEmpty()) System.out.println("Review Category is null or empty");
		if (reviewId == null || reviewId.isEmpty()) System.out.println("Review Id is null or empty");
		if (reviewAuthor == null || reviewAuthor.isEmpty()) reviewAuthor = "royt75";
		if (reviewRating == null ) System.out.println("Review Rating is null");
		if (reviewTime == null ) System.out.println("Review Time is null"); */
		
		// reviewSource
		MzProductSkus prodSku = skuDao.getMzProductSku(reviewSku);
		
		if (reviewAuthor != null && !reviewAuthor.isEmpty() && reviewTitle != null && !reviewTitle.isEmpty()
				&& reviewSku != null && !reviewSku.isEmpty() && reviewCategory != null && !reviewCategory.isEmpty()
				&& reviewId != null && !reviewId.isEmpty() && reviewContent != null && !reviewContent.isEmpty()
				&& reviewRating != null && reviewTime != null && prodSku != null) {
			
			// Output
			reviewMatch = new ReviewMatchType();
			
			// set
			reviewMatch.setReviewAuthor(reviewAuthor);
			reviewMatch.setReviewCategory(reviewCategory);
			reviewMatch.setReviewContent(reviewContent);
			reviewMatch.setReviewId(reviewId);
			reviewMatch.setReviewSku(reviewSku);
			reviewMatch.setReviewTitle(reviewTitle);
			reviewMatch.setReviewSource(prodSku.getRetailerName());
			//reviewMatch.setReviewSource("Best Buy");
			
			// the Rating
			MathContext mathContext = new MathContext(1); // set a precision of 1 for the ratings e.g 5.0
			BigDecimal rating = new BigDecimal(reviewRating.doubleValue(), mathContext);
			assert rating != null;
			reviewMatch.setReviewRating(rating);
			
			// the submissionTime
			DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			assert utcFormat != null;
			utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String reviewTimeStr = utcFormat.format(reviewTime);
			assert reviewTimeStr != null;
			XMLGregorianCalendar reviewCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(reviewTimeStr);
			assert reviewCal != null;
			reviewMatch.setReviewSubmitTime(reviewCal);			
		}	
		
		return reviewMatch;		
	}
	
	/**
	 * Helper to merge a {@link List} of strings to a {@link String}
	 * @param stringList - {@link List} to merge
	 * @return
	 */
	private String mergeStringList (List<String> stringList ) {
		
		// check input
		if (stringList == null) return null;
		if (stringList.isEmpty()) return new String();
		
		// merge
		StringBuffer strBuffer = new StringBuffer();
		for (Iterator<String> strIter = stringList.iterator(); strIter.hasNext();) {
			strBuffer.append(strIter.next());
			if (strIter.hasNext()) {
				strBuffer.append(" ");	// append a space between the strings
			}
		}
		return strBuffer.toString();		
	}
}
