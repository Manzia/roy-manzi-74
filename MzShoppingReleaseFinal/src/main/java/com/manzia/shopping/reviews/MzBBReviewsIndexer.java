package com.manzia.shopping.reviews;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.manzia.shopping.bestbuy.RemixException;
import com.manzia.shopping.bestbuy.Review;
import com.manzia.shopping.bestbuy.ReviewsResponse;
import com.manzia.shopping.dao.MzProductSkusDao;
import com.manzia.shopping.dao.MzProductSkusDataImpl;

/**
 * <p>MzReviewsIndexer tool uses the SolrJ Client API to index Best Buy reviews.xml files
 * to an Apache Solr server instance.
 * 
 * <p> Note that we do not create an XSLT stylesheet and use XSLTRequestHandler
 * within Solr since we have to add a category Field (whose value is retrieved
 * from the RetailerDB.product_sku table) to the indexed documents. </p>
 * 
 * Usage: MzReviewsIndexer -inputReviewsDir 
 * 
 * 
 * @author Roy Manzi Tumubweinee, Jan 26, 2013, Manzia Corporation
 *
 */
public final class MzBBReviewsIndexer extends MzBBReviewFileGenerator {
	
	// Solr Variables
	private static final String urlString = "http://ec2-50-18-18-237.us-west-1.compute.amazonaws.com:8983/solr";
	private static final String idField = "id";
	private static final String skuField = "sku";
	private static final String categoryField = "category";
	private static final String titleField = "title";
	private static final String contentField = "content";
	private static final String ratingField = "rating";
	private static final String submitTimeField = "submissionTime";
	private static final String reviewerField = "author";	
	
	// Catch-all text field
	private static final String catchAllField = "text";
	
	// Static Solr Server instance
	private static SolrServer solrServer;
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzBBReviewsIndexer.class.getCanonicalName());

	/**
	 * @param args - takes 1 arguments, the directory containing the
	 * reviews XML files <br>
	 * Usage: /reviewsDir 
	 */
	public static void main(String[] args) {
		// Check the inputs
		File reviewsDir;
		
		if (args.length != 1) {
			System.err.println("Usage: MzBBReviewsIndexer <reviewsDir>");
			throw new IllegalArgumentException
			("Class MzBBReviewsIndexer.java takes ONE string argument e.g /reviewsDir");
		} else {
			reviewsDir = new File(args[0]);
			assert reviewsDir != null;
		}
		if (!reviewsDir.isDirectory()) {
			System.err.println("Invalid <reviewsDir> specified..");
			throw new IllegalArgumentException("Invalid <reviewsDir specified..");
		}
		
		
		// Open the database
		MzBBReviewFileGenerator.setupDatabase();
		MzProductSkusDao productDao = MzBBReviewFileGenerator.getSkuDao();
		assert productDao != null;
		
		// Get the Apache Solr server instance
		solrServer = new HttpSolrServer(urlString);
		assert solrServer != null;
		

		// Load the reviews
		List<ReviewsResponse> reviewsList = null;
		boolean success = false;
		try {
			reviewsList = loadReviewsFromXML(reviewsDir);
			assert reviewsList != null;
			success = indexReviews(reviewsList, solrServer, productDao);
		} catch (RemixException e) {
			logger.log(Level.SEVERE, "RemixException - failed to parse review XML files");
			throw new RuntimeException("Failed to parse review XML files " + e.getLocalizedMessage());
		} catch (SolrServerException se) {
			logger.log(Level.SEVERE, "SolrServerException - during Indexing of Reviews");
			throw new RuntimeException("Failed to index Review files " + se.getLocalizedMessage());			
		} catch (IOException ie) {
			logger.log(Level.SEVERE, "IOException - during indexing of Reviews XML files");
			throw new RuntimeException("Failed to index review XML files " + ie.getLocalizedMessage());			
		} catch (ParseException pe) {
			logger.log(Level.SEVERE, "ParseException - during parsing of  review submissionTime in UTC date format in review XML files");
			throw new RuntimeException("Failed to parse UTC dateTime (review submissionTime) in review XML files " + pe.getLocalizedMessage());			
		}
		if (success) {
			logger.log(Level.INFO, "Success Indexing all Reviews!");
		} else {
			logger.log(Level.INFO, "Failed to Index Reviews!");
		}

		// Close the database
		MzBBReviewFileGenerator.closeDatabase();

	}
	
	/**
	 * Method converts Review objects to Document objects and adds them to the Index "managed" by
	 * the specified SolrServer instance. <br>
	 * 
	 * @param reviews - List of {@link ReviewsResponse} objects containing the {@link Review} objects
	 * @param server - {@link SolrServer} instance that "manages" Index we are adding documents to
	 * @param productDao - {@link MzProductSkusDao} object that provides access to the RetailerDB.product_sku
	 * database table
	 * 
	 * @return - true if the indexing operation succeeds else false
	 * @throws IOException - thrown during Indexing operation
	 * @throws SolrServerException - thrown during Indexing operation
	 * @throws ParseException - thrown during parsing of UTC datetime (review submissionTime)
	 */
	protected static boolean indexReviews( List<ReviewsResponse> reviews, SolrServer server, 
			MzProductSkusDao productDao) throws SolrServerException, IOException, ParseException {
		
		// check inputs
		if (reviews == null || server == null) {
			logger.log(Level.WARNING, "Null value specified for List of ReviewsResponse or SolrServer instance");
			return false;
		}
		if (reviews.isEmpty()) {
			logger.log(Level.WARNING, "Empty List of ReviewsResponse objects specified");
			return false;
		}
		
		// Iterate
		List<SolrInputDocument> inputDocs;
		int indexedDocs = 0;
		int indexResponse = 0;
		for (ReviewsResponse response : reviews) {
			inputDocs = MzBBReviewsIndexer.getDocumentsFromReviews(response.list(), productDao);
			if (!inputDocs.isEmpty()) {
				UpdateResponse updateResponse = server.add(inputDocs);
				indexedDocs = indexedDocs + inputDocs.size();
				indexResponse = indexResponse + updateResponse.getStatus();
			}
		}
		
		// Commit
		UpdateResponse newUpdate = server.commit();
		indexResponse = indexResponse + newUpdate.getStatus();
				
		// Log Results
		if (indexResponse == 0) {
			logger.log(Level.INFO, "Success indexing and commiting [{0}] Documents to the Index....", new Object[]{indexedDocs});
		} else {
			logger.log(Level.WARNING, "Unsuccessful response status from the Indexing operation!");
		}
		
		return indexResponse == 0 ? true : false;		
	}
	
	/**
	 * Method generates a List of {@link SolrInputDocument} from a List of {@link Review} on
	 * a one-to-one basis.<br>
	 * 
	 * @param reviewList - input List of Review objects
	 * @param productDao - data access object used to retrieve category value from the RetailerDB.product_sku
	 * database table
	 * @return - outputs List of SolrInputDocument objects
	 * @throws ParseException - thrown on failure to parse UTC datetime string
	 */
	protected static List<SolrInputDocument> getDocumentsFromReviews( List<Review> reviewList, MzProductSkusDao productDao) 
			throws ParseException {
		
		// Output
		List<SolrInputDocument> inputDocs = new ArrayList<SolrInputDocument>();
		
		// check inputs
		if (reviewList == null || reviewList.isEmpty()) {
			logger.log(Level.WARNING, "Empty or Null list of Review objects was specified");
			return inputDocs;
		}
		if (productDao == null) {
			logger.log(Level.WARNING, "Null data access object for RetailerDB.product_sku table was specified");
			return inputDocs;
		}
		
		// Iterate
		for (Review review : reviewList) {
			// create document
			SolrInputDocument document = new SolrInputDocument();
			
			// get Review data
			String reviewId = review.getReviewId();
			String reviewSku = review.getSku();
			String reviewTitle = review.getTitle();
			String reviewContent = review.getComment();
			Float reviewRating = review.getRating();
			String reviewTime = review.getSubmissionTime();
			String reviewUser = review.getReviewerName();
			
			// Update document
			if (reviewId != null && reviewSku != null && reviewSku.length() > 0 && reviewTitle != null && 
					reviewTitle.length() > 0 
					&& reviewContent != null && reviewContent.length() > 0 && reviewRating != null 
					&& reviewTime != null && reviewTime.length() > 0 && reviewUser != null && !reviewUser.isEmpty()) {
				
				// convert UTC reviewTime string to Date
				DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date date = utcFormat.parse(reviewTime);
				assert date != null;
				
				// get the associated category
				String reviewCategory = productDao.getCategoryForSKU(reviewSku);
				if (reviewCategory != null && !reviewCategory.isEmpty()) {
					
					// populate the document fields
					document.addField(idField, review.getReviewId());
					document.addField(skuField, reviewSku);
					document.addField(categoryField, reviewCategory);
					document.addField(titleField, reviewTitle);
					document.addField(contentField, reviewContent);
					document.addField(ratingField, reviewRating);
					document.addField(submitTimeField, date);
					document.addField(reviewerField, reviewUser);
					document.addField(catchAllField, new String());
					
					// add document
					inputDocs.add(document);
				}				
			}			
		}		
		
		return inputDocs;		
	}
	
	
}
