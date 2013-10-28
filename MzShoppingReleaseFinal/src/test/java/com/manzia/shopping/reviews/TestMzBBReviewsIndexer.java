package com.manzia.shopping.reviews;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.manzia.shopping.bestbuy.RemixException;
import com.manzia.shopping.bestbuy.Review;
import com.manzia.shopping.bestbuy.ReviewsResponse;

public class TestMzBBReviewsIndexer {
	
	private static final String reviewsDir = "testDir/reviewsXML";
	private static final String urlString = "http://localhost:8080/solr";
	private final String reviewCategory = "Laptops";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMain() throws SolrServerException, IOException, RemixException {
		File inputDir = new File( System.getProperty("user.dir"), reviewsDir);
		assertTrue(inputDir.isDirectory());
		
		// Index
		String[] args = new String[]{ inputDir.getAbsolutePath() };
		assertEquals(1, args.length);
		MzBBReviewsIndexer.main(args);
		
		// Query
		SolrQuery query = new SolrQuery();
		query.set("q", "laptop");
		SolrServer server = new HttpSolrServer(urlString);
		assertNotNull(server);
		QueryResponse response = server.query(query);
		assertNotNull(response);
		SolrDocumentList results = response.getResults();
		assertNotNull(results);
		
		// Test results
		assertFalse(results.isEmpty());
		SolrDocument resultDoc = results.get(0);
		String resultCategory = (String)resultDoc.getFieldValue("category");
		assertNotNull(resultCategory);
		assertEquals("Unexpected Category retrieved", reviewCategory, resultCategory);
		
		// Delete the SolrDocuments we added to the Index
		List<String> deleteIds = new ArrayList<String>();
		List<ReviewsResponse> reviewsList = MzBBReviewFileGenerator.loadReviewsFromXML(inputDir);
		assertNotNull(reviewsList);
		assertFalse(reviewsList.isEmpty());
		
		for (ReviewsResponse reviewResp : reviewsList) {
			for (Review review : reviewResp.list()) {
				deleteIds.add(review.getReviewId());
			}
		}
		assertFalse(deleteIds.isEmpty());
		server.deleteById(deleteIds);
		server.commit();
	}

}
