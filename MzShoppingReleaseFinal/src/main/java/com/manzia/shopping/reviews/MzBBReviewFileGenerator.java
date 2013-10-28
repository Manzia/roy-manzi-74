package com.manzia.shopping.reviews;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.manzia.shopping.bestbuy.RemixException;
import com.manzia.shopping.bestbuy.Review;
import com.manzia.shopping.bestbuy.ReviewsResponse;
import com.manzia.shopping.dao.MzProductSkusDao;
import com.manzia.shopping.dao.MzProductSkusDataImpl;

/**
 * <p>Generates a single Unicode text file from a set of BestBuy reviews XML files.
 * Each reviews XML file contains many reviews in XML format. The output txt file
 * contains entries per line of the following format:</p>
 * <p>productSKU,review_comment <br>
 * 4156678,love this laptop coz its cool <br>
 * 5177899,hate this laptop coz its lousy <br>
 * 
 * The output txt file serves as input to the {@link MzReviewsMergeJob} tool
 * </p>
 * The productSKU is BestBuy's SKU and entries are comma-separated.
 * 
 * @author Roy Manzi Tumubweinee, Jan 13, 2013, Manzia Corporation
 *
 */
public class MzBBReviewFileGenerator {
	
	// Data Access
	private static EntityManagerFactory entityFactory;
	private static EntityManager manager;
	private static MzProductSkusDao skuDao;
	
	//Logger
		public static final Logger logger = 
					Logger.getLogger(MzBBReviewFileGenerator.class.getCanonicalName());
	// Key=Value Separator
	public static final String keyValueSeparator = ":::";	// Highly unlikely to find this sequence in unstructured reviews
	public static final String categorySKUSeparator = "::";	// Key Format: category::1223311:::review text

	/**
	 * 
	 * @param args - takes 2 arguments, the directory containing the
	 * reviews XML files and the directory in which to write the output txt file <br>
	 * Usage: /reviewsDir /outputDir
	 */
	public static void main(String[] args) {
		
		// Check the inputs
		File reviewsDir;
		File outputDir;
		
		if (args.length != 2) {
			System.err.println("Usage: MzBBReviewFileGenerator <reviewsDir> <outputDir>");
			throw new IllegalArgumentException
			("Class MzBBReviewFileGenerator.java takes TWO string arguments e.g /reviewsDir /outputDir");
		} else {
			reviewsDir = new File(args[0]);
			assert reviewsDir != null;
			outputDir = new File(args[1]);
			assert outputDir != null;
		}
		if (!reviewsDir.isDirectory()) {
			System.err.println("Invalid <reviewsDir> specified..");
			throw new IllegalArgumentException("Invalid <reviewsDir specified..");
		}
		if (!outputDir.isDirectory()) {
			System.err.println("Invalid <outputDir> specified..");
			throw new IllegalArgumentException("Invalid <outputDir specified..");
		}
		
		// Open the database
		MzBBReviewFileGenerator.setupDatabase();
		
		// Load the reviews
		List<ReviewsResponse> reviewsList = null;
		boolean success = false;
		try {
			reviewsList = loadReviewsFromXML(reviewsDir);
			assert reviewsList != null;
			success = writeReviewsToFile(outputDir, reviewsList, skuDao);
		} catch (RemixException e) {
			logger.log(Level.SEVERE, "RemixException - failed to parse review XML files");
			throw new RuntimeException("Failed to parse review XML files");
		}
		if (success) {
			logger.log(Level.INFO, "Success writing reviews to output txt file!");
		} else {
			logger.log(Level.INFO, "Failed to write reviews to output txt file!");
		}
		
		// Close the database
		MzBBReviewFileGenerator.closeDatabase();

	}
	
	/**
	 * Sets connection to the database via EntityManager
	 */
	public static void setupDatabase() {
		
		entityFactory = 
				Persistence.createEntityManagerFactory("ManziaShoppingRelease");
		assert entityFactory != null;
		manager = entityFactory.createEntityManager();
		assert manager != null;	
		//manager.getTransaction().begin();
		
		// Setup the data access object
		skuDao = new MzProductSkusDataImpl();
		assert skuDao != null;
		skuDao.setEntityManager(manager);
	}
	
	/**
	 * Closes connections to the database
	 */
	public static void closeDatabase() {
		
		//close the EntityManager
		if (manager != null) {
			//manager.getTransaction().commit();
			manager.close();			
		}

		// close the EntityManagerFactory
		if (entityFactory != null) {
			entityFactory.close();
		}
	}
	
	/**
	 * Getter for the Data Access object
	 * @return - MzProductSkusDao object
	 */
	
	public static MzProductSkusDao getSkuDao() {
		return skuDao;
	}	

	/**
	 * <p>Method writes each Review object in each ReviewsResponse object as a new line in
	 * the output txt file in the outputDir in the following format: </p>
	 * <p>productSKU,review_comment <br>
	 * 4156678,love this laptop coz its cool <br>
	 * 5177899,hate this laptop coz its lousy <br>
	 * </p>
	 * The output Filename format: reviews01-13-2013.txt. If the current date cannot be computed
	 * the default filename: reviews01-01-2000.txt will be used.
	 * @param outputDir - directory in which to write output txt file
	 * @param reviewsList - List of ReviewsResponse objects
	 * @param productDao - data access object for the product_skus database table. We query this table to 
	 * determine the product category associated with a given product SKU
	 * @return - True if write operation succeeded else False.
	 */
	protected static boolean writeReviewsToFile(File outputDir, List<ReviewsResponse> reviewsList, MzProductSkusDao productDao ) {
		
		// check inputs
		if (!outputDir.isDirectory()) {
			System.err.println("Invalid <outputDir> specified..");
			throw new IllegalArgumentException("Invalid <outputDir> specified..");
		}
		if (reviewsList == null || reviewsList.isEmpty()) {
			logger.log(Level.WARNING, "List of ReviewsResponse objects is NULL or EMPTY...output File not created!");
			return false;
		}
		if (productDao == null) {
			logger.log(Level.WARNING, "Data access object for product_skus table is NULL!");
			return false;
		}
		
		// Create the filename
		StringBuffer filename = new StringBuffer();
		Date todayDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		String filenameDate = formatter.format(todayDate);
		if (filenameDate != null && filenameDate.length() > 0) {
			filename.append("reviews").append(filenameDate).append(".txt");
		} else {
			// use default filename
			filename.append("reviews").append("01-01-2000").append(".txt");
		}
		
		// Write to the output File
		File outputFile = new File(outputDir, filename.toString());
		assert outputFile != null;
		BufferedWriter bufWriter = null;
		
		try {
			bufWriter = new BufferedWriter(new FileWriter(outputFile));
			
			// Iterate over all the reviews
			int reviewCount = 0;
			for (ReviewsResponse reviewResponse : reviewsList) {
				for (Review review : reviewResponse.list()) {
					StringBuffer reviewBuffer = new StringBuffer();
					String skuCategory = productDao.getCategoryForSKU(review.getSku());
					if (skuCategory != null && !skuCategory.isEmpty()) {
						reviewBuffer.append(skuCategory)
						.append(categorySKUSeparator)
						.append(review.getSku())
						.append(keyValueSeparator)
						.append(review.getComment());
						bufWriter.write(reviewBuffer.toString());
						bufWriter.newLine();
						reviewCount++;
					}					
				}
			}
			logger.log(Level.INFO, "Wrote [{0}] reviews to output File: {1}", 
					new Object[]{ reviewCount, outputFile.getAbsolutePath()});
			
		} catch(FileNotFoundException fe) {
			logger.log(Level.WARNING, "Output File not found");
			fe.printStackTrace();		
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO Exception while writing to output File");
			e.printStackTrace();
		} finally {
			
			//Close the BufferedWriter
            try {
                if (bufWriter != null) {
                    bufWriter.flush();
                    bufWriter.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
		}		
		return outputFile.exists() ? true : false;		
	}
	
	/**
	 * Method loads each reviews XML file into a List <br>
	 * @param reviewDirectory - directory containing the reviews XML files
	 * @return - List of ReviewsResponse object each representing a reviews XML file. Note that if
	 * the reviews Directory has no valid reviews XML files, an empty List is returned.
	 * @throws RemixException - thrown if the XML parsing of Best Buy reviews XML files fails
	 */
	protected static List<ReviewsResponse> loadReviewsFromXML( File reviewDirectory ) throws RemixException {
		
		// check input
		if (!reviewDirectory.isDirectory()) {
			System.err.println("Invalid <reviewsDir> specified..");
			throw new IllegalArgumentException("Invalid <reviewsDir specified..");
		}
		
		// Iterate through the directory and read each reviews XML file
		FilenameFilter xmlFilter = new MzXMLFileFilter();
		List<File> xmlFiles = Arrays.asList(reviewDirectory.listFiles(xmlFilter));
		assert xmlFiles != null;
		
		// Create the ReviewsResponse objects
		List<ReviewsResponse> reviewsList = Collections.synchronizedList(new ArrayList<ReviewsResponse>());
		if (xmlFiles.size() > 0) {
			for (File reviewXML : xmlFiles) {
				ReviewsResponse reviewResponse = new ReviewsResponse(reviewXML);
				assert reviewResponse != null;
				reviewsList.add(reviewResponse);
			}
			logger.log(Level.INFO, "Created [{0}] ReviewResponse objects from [{1}] reviews XML files", 
					new Object[] {reviewsList.size(), xmlFiles.size()});
		} else {
			// In this case, we have no valid XML files in the reviews Directory
			logger.log(Level.WARNING, "Zero reviews XML files found in reviews Directory: {0}", 
					new Object[]{reviewDirectory.getAbsolutePath()});
			return reviewsList;
		}
		
		return reviewsList;		
	}
	
	/**
	 * Inner class used to filter out non XML files from specified directory
	 * @author Roy Manzi Tumubweinee, Jan 13, 2013, Manzia Corporation
	 *
	 */
	public static class MzXMLFileFilter implements FilenameFilter {

		@Override
		public boolean accept(File xmlDir, String xmlFile) {
			if (xmlFile.endsWith(".xml") && new File( xmlDir, xmlFile).length() > 0) {
				return true;
			} else {
			return false;
			}
		}
	}

}
