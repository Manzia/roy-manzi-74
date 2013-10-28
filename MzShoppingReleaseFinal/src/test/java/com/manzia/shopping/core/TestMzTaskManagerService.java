package com.manzia.shopping.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.manzia.shopping.dao.MzProdDatabase;
import com.manzia.shopping.dao.MzProductSkusDao;
import com.manzia.shopping.dao.MzProductSkusDataImpl;
import com.manzia.shopping.products.BestBuyService;
import com.manzia.shopping.products.MzProductServiceBean;
import com.manzia.shopping.products.RankedProductType;
import com.manzia.shopping.products.RankedProducts;

public class TestMzTaskManagerService {
	
	private static EJBContainer container;
	private @BestBuyService MzProductServiceBean productService;
	private @MzSolrServerOne MzSolrService solrService;
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
		props.put("org.glassfish.ejb.embedded.glassfish.instance.root", "/Users/admin/glassfish3/glassfish/domains/manzia.com");
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
		productService = (MzProductServiceBean) this.getBean("MzProductServiceBean");
		assertNotNull("MzProductServiceBean instance is null", productService);
		solrService = (MzSolrService)this.getBean("MzSolrService");
		assertNotNull("MzSolrService instance is null", solrService);
		taskService = (MzTaskManagerService) this.getBean("MzTaskManagerService");
		assertNotNull("MzTaskManagerService instance is null", taskService);
	}

	@After
	public void tearDown() throws Exception {
		if (productService != null) {
			productService = null;
		}
		if (solrService != null) {
			solrService = null;
		}
		if (taskService != null) {
			taskService = null;
		}
	}
	
	// This test fails due to the way Eclipse handles the persistence.xml file i.e
	// it does not allow more than one Persistence Unit, and yet we require both
	// RESOURCE_LOCAL and JTA types to run all the test below.
	/*@Test
	public void testgetRelevantProducts() throws SolrServerException {
		/*
		 * We use the following query to retrieve product SKUs from the Index that
		 * we then use to retrieve Products from the Retailer APIs (Best Buy only for now)
		 * q=great+photos+quality+Category:Laptops -- we expect >70 hits/numFound
		 */
		/*Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", "Laptops");
		queryMap.put("key1", "great");
		queryMap.put("key2", "photos");
		queryMap.put("key3", "quality");
		
		// Test
		RankedProducts rankedProducts = taskService.getRelevantProducts(queryMap);
		assertNotNull(rankedProducts);
		assertFalse("Unexpected Product count", rankedProducts.getRankedProduct().isEmpty());
	}*/
	
	@Test
	public void testFetchRelevantProducts() throws SolrServerException {
		
		/*
		 * We use the following query to retrieve product SKUs from the Index that
		 * we then use to retrieve Products from the Retailer APIs (Best Buy only for now)
		 * q=great+photos+quality+Category:Laptops -- we expect >70 hits/numFound
		 */
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", "Laptops");
		queryMap.put("q1", "great photos quality");		
		
		// Test
		RankedProducts rankedProducts = taskService.fetchRelevantProducts(queryMap, solrService, productService, skuDao);
		assertNotNull(rankedProducts);
		
		//ÊWe expect to get 10 results per page
		assertFalse("Unexpected Product count", rankedProducts.getRankedProduct().isEmpty());
		//assertEquals("Unexpected no of fetched Products", 10, rankedProducts.getRankedProduct().size());
	}

	@Test
	public void testFetchRelevantReviews() throws SolrServerException {
		Map<String, String> queryMap = new HashMap<String, String>();
		queryMap.put("Category", "Tablets");
		queryMap.put("sku", "7039173");
		
		// Test
		SolrDocumentList docList = taskService.fetchRelevantReviews(queryMap, solrService);
		assertNotNull(docList);
		assertFalse(docList.isEmpty());
		String retrievedSku = (String)docList.get(0).getFieldValue("sku");
		assertNotNull(retrievedSku);
		//System.out.println("Retrieved SKU: " + retrievedSku);
		assertTrue("Unexpected SKU retrieved", retrievedSku.equalsIgnoreCase("7039173") );
	}

	@Test
	public void testFetchFromBestBuyAPI() {
		List<String> skuList = new ArrayList<String>();
		skuList.add("4550361");		// 1st four are Best Buy SKUs
		skuList.add("20811916");
		skuList.add("2638269");
		skuList.add("6877744");
		skuList.add("4153097418");	// Manzia Retailer - test SKU
		
		// Test
		Map<String, List<String>> retailerSkus = taskService.groupSkusByRetailerName(skuDao, skuList);
		assertNotNull(retailerSkus);
		List<RankedProductType> rankedList = taskService.fetchFromBestBuyAPI(retailerSkus, productService);
		assertNotNull(rankedList);
		
		// Only the 4550361 productSku exists in the RetailerDB.product_sku table so we should only be getting
		// 1 result.
		assertEquals("Unexpected no of Products retrieved", 1, rankedList.size());
	}

	@Test
	public void testGroupSkusByRetailerName() {
		List<String> skuList = new ArrayList<String>();
		skuList.add("4550361");		// 1st four are Best Buy SKUs but only 4550361 exists in RetailerDB.product_sku table
		skuList.add("20811916");
		skuList.add("2638269");
		skuList.add("6877744");
		skuList.add("4153097418");	// Manzia Retailer - test SKU
		
		// Test
		Map<String, List<String>> retailerSkus = taskService.groupSkusByRetailerName(skuDao, skuList);
		assertNotNull(retailerSkus);
		assertEquals("Unexpected no of Retailers", 2, retailerSkus.size());
		assertEquals("Unexpected no of Best Buy Skus", 1, retailerSkus.get("Best Buy").size());
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
