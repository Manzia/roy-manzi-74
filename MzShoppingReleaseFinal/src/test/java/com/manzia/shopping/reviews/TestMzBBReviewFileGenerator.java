package com.manzia.shopping.reviews;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.manzia.shopping.bestbuy.RemixException;
import com.manzia.shopping.bestbuy.Review;
import com.manzia.shopping.bestbuy.ReviewsResponse;

public class TestMzBBReviewFileGenerator {
	
	private final String testDirectory = "testDir";
	private final String reviewsDir ="reviewsXML";
	private final File outputDir = new File( System.getProperty("user.dir"), testDirectory);
	private final File reviewsDirectory = new File( outputDir, reviewsDir );
	

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testWriteReviewsToFile() throws RemixException {
		MzBBReviewFileGenerator.setupDatabase();
		List<ReviewsResponse> reviewsList = 
				MzBBReviewFileGenerator.loadReviewsFromXML(reviewsDirectory);
		assertNotNull(reviewsList);
		boolean success = MzBBReviewFileGenerator.writeReviewsToFile(outputDir, reviewsList, MzBBReviewFileGenerator.getSkuDao());
		assertTrue("Failed to write txt File", success);
		MzBBReviewFileGenerator.closeDatabase();
	}

	@Test
	public void testLoadReviewsFromXML() throws RemixException {
		List<ReviewsResponse> reviewsList = 
				MzBBReviewFileGenerator.loadReviewsFromXML(reviewsDirectory);
		assertNotNull(reviewsList);
		assertEquals("Unexpected number of ReviewsResponse objects", 1, reviewsList.size());
		Review review = reviewsList.get(0).list().get(0);
		assertNotNull(review);
		assertNotNull("SKU is null", review.getSku());
		assertNotNull("Comment is Null", review.getComment());
	}
}
