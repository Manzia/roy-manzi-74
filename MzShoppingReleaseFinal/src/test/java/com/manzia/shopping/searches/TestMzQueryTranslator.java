package com.manzia.shopping.searches;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

public class TestMzQueryTranslator {
	
	private static final String categoryName = "Tablets";
	private static final String skuValue = "7039173";
	private static final String urlString = "http://localhost:8983/solr";
	private static SolrServer server = new HttpSolrServer(urlString);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTranslateQuerySKU() throws SolrServerException {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", categoryName);
		//queryMap.put("q1", "great photos quality");
		queryMap.put("q2", "photos");
		queryMap.put("q3", "quality");
		
		// Create MzSearchQuery
		MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
		assertNotNull(searchQuery);
		
		// Do the translation
		MzQueryTranslator translator = new MzQueryTranslator();
		SolrQuery query = translator.translateQuery(searchQuery);
		assertNotNull(query);
		
		// Test that the query is valid
		assertNotNull(server);
		QueryResponse response = server.query(query);
		assertNotNull(response);
		SolrDocumentList results = response.getResults();
		assertNotNull(results);
		assertFalse(results.isEmpty()); // Test fails because of the ("fq", "reviewType:mergedReview") Solr parameter we add to query
		
		for (SolrDocument doc : results) {
			String sku = (String)doc.getFieldValue("sku");
			assertFalse(sku.isEmpty());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testTranslateQueryReviews() throws SolrServerException {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", categoryName);
		queryMap.put("sku", skuValue);
		queryMap.put("q1", "great photos quality");
		queryMap.put("q2", "photos");
		queryMap.put("q3", "quality");
		//queryMap.put("q", buffer.toString());
		
		// Create MzSearchQuery
		MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
		assertNotNull(searchQuery);
		
		// Do the translation
		MzQueryTranslator translator = new MzQueryTranslator();
		SolrQuery query = translator.translateQuery(searchQuery);
		assertNotNull(query);
		
		System.out.println("Translated Query: " + query.getQuery());
		Iterator<String> paramNames = query.getParameterNamesIterator();
		while (paramNames.hasNext()) {
			System.out.println(paramNames.next());
		}
		
		// Test that the query is valid
		assertNotNull(server);
		QueryResponse response = server.query(query);
		assertNotNull(response);
		SolrDocumentList results = response.getResults();
		assertNotNull(results);
		assertFalse(results.isEmpty());
		
		for (SolrDocument doc : results) {
			List<String> contents = (ArrayList<String>)doc.getFieldValue("content");
			assertFalse(contents.isEmpty());
			String sku = (String)doc.getFieldValue("sku");
			assertEquals(skuValue, sku);
		}
	}
	
	@Test
	public void testTranslateQueryList() throws SolrServerException {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", categoryName);
		queryMap.put("sku", skuValue);
		queryMap.put("q1", "great photos quality");
		queryMap.put("q2", "photos");
		queryMap.put("q3", "quality");
		//queryMap.put("q", buffer.toString());
		
		// Create MzSearchQuery
		MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
		assertNotNull(searchQuery);
		
		// Do the translation
		MzQueryTranslator translator = new MzQueryTranslator();
		List<SolrQuery> solrList = translator.translateQueryList(searchQuery);
		assertNotNull(solrList);
		assertEquals("Unexpected No of SolrQuery objects", 3, solrList.size());
				
		// Test that the query is valid
		assertNotNull(server);
		QueryResponse response = server.query(solrList.get(0));
		assertNotNull(response);
		SolrDocumentList results = response.getResults();
		assertNotNull(results);
		assertFalse(results.isEmpty());
	}

}
