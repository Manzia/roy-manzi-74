package com.manzia.shopping.bestbuy;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestReviewsResponse {
	
	private String testReviewsFile = "sampleReviews.xml";
	private String testDirectory = "testDir";
	private File reviewsDir;

	@Before
	public void setUp() throws Exception {
		reviewsDir = new File( System.getProperty("user.dir"), testDirectory);
		assertNotNull(reviewsDir);
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testList() throws RemixException {
		File reviewsFile = new File( reviewsDir, testReviewsFile);
		assertNotNull(reviewsFile);
		assertTrue("Missing sampleReviews.xml file", reviewsFile.exists());
		ReviewsResponse reviewResponse = new ReviewsResponse(reviewsFile);
		assertNotNull(reviewResponse);
		
		// Check the Reviews
		assertEquals("Unexpected number of reviews", 10, reviewResponse.list().size());
	}

}
