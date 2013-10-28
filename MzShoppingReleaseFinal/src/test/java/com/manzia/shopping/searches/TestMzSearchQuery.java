package com.manzia.shopping.searches;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMzSearchQuery {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void generateSolrQuery() {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("q1", "portable");
		queryMap.put("Category", "Laptops");
		queryMap.put("q2", "attractive display");
		
		// Create MzSearchQuery
		MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
		assertNotNull(searchQuery);

		// Do the translation
		MzQueryTranslator translator = new MzQueryTranslator();
		SolrQuery query = translator.translateQuery(searchQuery);
		assertNotNull(query);
		assertNotNull(query.getQuery());
		System.out.println("Translated Query: " + query.getQuery());
	}
	
	@Test
	public void testConvertToSolrSyntax() {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("q1", "portable");
		queryMap.put("Category", "Laptops");
		queryMap.put("q2", "attractive display");
		
		MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
		assertNotNull(searchQuery);
		List<String> termList = MzSearchQuery.getQueryTerms(queryMap);
		assertNotNull(termList);
		
		// Test
		String solrQuery = MzSearchQuery.convertToSolrSyntax(termList);
		assertNotNull(solrQuery);
		System.out.println("Solr Syntax: " + solrQuery);
	}
	
	@Test
	public void testGetQueryTerms() {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("q1", "portable");
		queryMap.put("Category", "Laptops");
		queryMap.put("q2", "attractive display");
		
		MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
		assertNotNull(searchQuery);
		List<String> termList = MzSearchQuery.getQueryTerms(queryMap);
		assertNotNull(termList);
		
		// Test
		assertEquals("Unexpected No of query Terms", 2, termList.size());
		assertTrue("Unexpected query Term", termList.contains("portable"));
		assertTrue("Unexpected query Term", termList.contains("attractive display"));
	}

	@Test
	public void testQueryStringIsPhrase() {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("q1", "portable");
		queryMap.put("Category", "Laptops");
		queryMap.put("q2", "attractive display");
		
		MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
		assertNotNull(searchQuery);
				
		// Test
		assertTrue("Incorrect Phrase", MzSearchQuery.queryStringIsPhrase(queryMap.get("q2")));
		assertFalse("Incorrect Term", MzSearchQuery.queryStringIsPhrase(queryMap.get("q1")));
	}
	
	@Test
	public void testQueryRegexPattern() {
		Pattern p = MzSearchQuery.QUERY_PATTERN;
		assertNotNull(p);
		boolean success = p.matcher("q1").matches();
		assertTrue("Incorrect Pattern Match for q1", success);
		success = p.matcher("q25").matches();
		assertTrue("Incorrect Pattern Match for q25", success);
		success = p.matcher("qq").matches();
		assertFalse("Incorrect Pattern Match for qq", success);
		success = p.matcher("bq").matches();
		assertFalse("Incorrect Pattern Match for bq", success);
		success = p.matcher("1q").matches();
		assertFalse("Incorrect Pattern Match for 1q", success);
	}

}
