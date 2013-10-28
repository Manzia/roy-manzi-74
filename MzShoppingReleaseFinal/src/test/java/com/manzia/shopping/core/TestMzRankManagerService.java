package com.manzia.shopping.core;

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.manzia.shopping.ranking.RankResultType;
import com.manzia.shopping.ranking.RankResults;
import com.manzia.shopping.searches.MzQueryTranslator;
import com.manzia.shopping.searches.MzSearchQuery;

public class TestMzRankManagerService {
	
	private static EJBContainer container;
	private @MzSolrServerOne MzSolrService solrService;
	private @MzRankCoreService MzRankManagerService rankService;
		
	// SolrServer URL
	//private static final String urlString = "http://ec2-54-241-22-223.us-west-1.compute.amazonaws.com:8983/solr";
	// SolrServer instance
	//private static final SolrServer solrServer = new HttpSolrServer(urlString);;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Use the same container instance for all the tests
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(EJBContainer.APP_NAME, "RankManagerService" );
		props.put("org.glassfish.ejb.embedded.glassfish.instance.root", "/Users/admin/glassfish3/glassfish/domains/manzia.com");
		container = EJBContainer.createEJBContainer(props);
		assert container != null;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Close the container instance
		if (container != null) {
			container.close();
		}
		container = null;
	}

	@Before
	public void setUp() throws Exception {
		solrService = (MzSolrService)this.getBean("MzSolrService");
		assertNotNull("MzSolrService instance is null", solrService);
		rankService = (MzRankManagerService) this.getBean("MzRankManagerService");
		assertNotNull("MzRankManagerService instance is null", rankService);		
	}

	@After
	public void tearDown() throws Exception {
		if (solrService != null) {
			solrService = null;
		}
		if (rankService != null) {
			rankService = null;
		}		
	}

	@Test
	public void testGetRankingDataForSKU() throws SolrServerException {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", "Tablets");
		queryMap.put("sku", "6668354");
		queryMap.put("q1", "photos");
		queryMap.put("q2", "great quality");
		
		// Test
		RankResults rankResults = rankService.getRankingDataForSKU(queryMap, solrService);
		assertNotNull(rankResults);
		assertFalse("Unexpected empty RankResults", rankResults.getRankResult().isEmpty());
		assertEquals("Unexpected size of RankResults", 2, rankResults.getRankResult().size());
		
		
		// Printout
		for (RankResultType rankType : rankResults.getRankResult()) {
			assertTrue("Computed rank is <= 0", rankType.getRankRating().intValue() > 0);
			assertFalse("Computed rank is > 25",rankType.getRankRating().intValue() > 25 );
			//System.out.printf("Quality: %s\tRank: %s\n", rankType.getRankQuality(), rankType.getRankRating().toString());
		}		
	}

	@Test
	public void testComputeRankForQuery() throws SolrServerException {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", "Tablets");
		queryMap.put("q1", "photos");
		queryMap.put("q2", "great quality");
		
		// Test
		// Create MzSearchQuery
		MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
		assertNotNull(searchQuery);

		// Do the translation
		MzQueryTranslator translator = new MzQueryTranslator();
		SolrQuery query = translator.translateQuery(searchQuery);
		assertNotNull(query);

		// Test that the query is valid
		SolrServer server = solrService.getSolrServer();
		assertNotNull(server);
		QueryResponse response = server.query(query);
		assertNotNull(response);
		SolrDocumentList results = response.getResults();
		assertNotNull(results);
		assertFalse(results.isEmpty());
				
		int testSkuIndex = results.size() > 3 ? 3 : 0;
		String retrievedSku = (String)results.get(testSkuIndex).getFieldValue("sku");
		assertNotNull(retrievedSku);
		//assertTrue("Unexpected Sku value", retrievedSku.equalsIgnoreCase("4602955"));
		Integer computedRank = rankService.computeRankForQuery(retrievedSku, results);
		assertNotNull(computedRank);
		assertEquals("Unexpected computed Rank", 3, computedRank.intValue());
		
	}
	
	// Method that returns an instance of the request bean from the
		// EJBContainer
		public Object getBean(String bean) throws NamingException {
			return container.getContext()
					.lookup("java:global/RankManagerService/classes/" + bean);
		}

}
