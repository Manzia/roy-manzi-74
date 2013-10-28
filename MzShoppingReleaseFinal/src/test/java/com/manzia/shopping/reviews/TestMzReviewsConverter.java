package com.manzia.shopping.reviews;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.manzia.shopping.core.MzCoreService;
import com.manzia.shopping.core.MzSolrServerOne;
import com.manzia.shopping.core.MzSolrService;
import com.manzia.shopping.core.MzTaskManagerService;
import com.manzia.shopping.dao.MzProductSkusDao;
import com.manzia.shopping.dao.MzProductSkusDataImpl;
import com.manzia.shopping.products.BestBuyService;
import com.manzia.shopping.products.MzProductServiceBean;

public class TestMzReviewsConverter {
	
	private static EJBContainer container;
	private @MzCoreService MzTaskManagerService taskService;
	
	// Data Access
	private static EntityManagerFactory entityFactory;
	private static EntityManager manager;
	private static MzProductSkusDao skuDao;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// Use the same container instance for all the tests
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(EJBContainer.APP_NAME, "TaskManagerService" );
		container = EJBContainer.createEJBContainer(props);
		assert container != null;

		// Setup database
		setupDatabase();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
		// Close the container instance
		if (container != null) {
			container.close();
		}
		container = null;
		
		// close Database
		closeDatabase();
	}

	@Before
	public void setUp() throws Exception {
		taskService = (MzTaskManagerService) this.getBean("MzTaskManagerService");
		assertNotNull("MzTaskManagerService instance is null", taskService);
	}

	@After
	public void tearDown() throws Exception {
		if (taskService != null) {
			taskService = null;
		}
	}

	@Test
	public void testConvertToReviewMatchesType() throws SolrServerException, DatatypeConfigurationException {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", "Tablets");
		queryMap.put("sku", "7039173");
		
		// Test
		SolrDocumentList docList = taskService.getRelevantReviews(queryMap);
		assertNotNull(docList);
		assertFalse(docList.isEmpty());
		MzReviewsConverter reviewConverter = new MzReviewsConverter();
		assertNotNull(reviewConverter);
		int reviewMatchCount = 0;
		for (SolrDocument solrDoc : docList) {
			ReviewMatchType reviewMatch = reviewConverter.convertReviewMatchType(solrDoc, skuDao);
			if (reviewMatch != null ) {
				assertFalse("Missing Review Sku", reviewMatch.getReviewSku().isEmpty());
				assertTrue("Unexpected reviewSku", reviewMatch.reviewSku.equalsIgnoreCase("7039173") );
				
				// Test the datetime field by comparing TimeZone
				//TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
				//TimeZone reviewTimeZone = reviewMatch.getReviewSubmitTime().toGregorianCalendar().getTimeZone();
				//assertTrue("Unexpected difference in TimeZones", reviewTimeZone.hasSameRules(utcTimeZone));
				//System.out.println("Review Time: " + reviewMatch.getReviewSubmitTime().toGregorianCalendar().toString());
				
				// Test the review Properties
				assertFalse("Missing Review Title", reviewMatch.getReviewTitle().isEmpty());			
				assertFalse("Missing Review Content", reviewMatch.getReviewContent().isEmpty());
				assertFalse("Missing Review Category", reviewMatch.getReviewCategory().isEmpty());
				assertTrue("Unexpected Review Rating Precision", reviewMatch.getReviewRating().precision() == 1);
				assertFalse("Missing Review Id", reviewMatch.getReviewId().isEmpty());
				assertFalse("Missing Review Source", reviewMatch.getReviewSource().isEmpty());
				reviewMatchCount++;
			}			
		}
		System.out.printf("Converted %d SolrDocuments to %d ReviewMatchType", docList.size(), reviewMatchCount);
	}

	@Test
	public void testConvertReviewMatchType() throws SolrServerException {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", "Tablets");
		queryMap.put("sku", "7039173");
		
		// Test
		MzReviewsConverter reviewConverter = new MzReviewsConverter();
		assertNotNull(reviewConverter);
		SolrDocumentList docList = taskService.getRelevantReviews(queryMap);
		assertNotNull(docList);
		ReviewMatches reviewMatches = reviewConverter.convertToReviewMatchesType(docList, skuDao);
		assertNotNull(reviewMatches);
		assertEquals("Unexpected no of Reviews", 1, reviewMatches.getReviewMatch().size());
	}
	
	// Method that returns an instance of the request bean from the
		// EJBContainer
		public Object getBean(String bean) throws NamingException {
			return container.getContext()
					.lookup("java:global/TaskManagerService/classes/" + bean);
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


}
