package com.manzia.shopping.products;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.manzia.shopping.bestbuy.Product;
import com.manzia.shopping.bestbuy.ProductsResponse;
import com.manzia.shopping.model.MzModelNumber;
import com.manzia.shopping.model.MzModelNumberPK;

public class TestMzProductServiceBean {
	
	private static EJBContainer container;
	private @BestBuyService MzProductServiceBean productService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Use the same container instance for all the tests
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(EJBContainer.APP_NAME, "ProductServiceBean-ejb" );
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
		productService = (MzProductServiceBean) this.getBean("MzProductServiceBean");
		assertNotNull("MzProductServiceBean instance is null", productService);
	}

	@After
	public void tearDown() throws Exception {
		if (productService != null) {
			productService = null;
		}
	}

	@Test
	public void testUpdateProductsTable() {
		
	}
	/**
	 * This test Method goes over the network and retrieves the products
	 * from the BestBuy API that correspond to the ModelNumber object below
	 * The success of the test below depends on the availability of the BestBuy API
	 * so we have another method that uses a mock below
	 * @throws Exception 
	 */
	
	@Test
	public void testFetchProductsForModelNumber() {
		// Create a test MzModelNumber object
		ProductsResponse fetchedProducts;
		Future<ProductsResponse> futureProductResponse;
		MzModelNumber modelNum = 
				new MzModelNumber("2000-428dx", "HP", "Laptops", null, "PC Laptops", "5026109", null);
		try {
			futureProductResponse = productService.fetchProductsForModelNumber(modelNum);
		} catch (Exception e) {
			System.out.println("Exception fetching products" + e.getLocalizedMessage());
			e.printStackTrace();
			throw new RuntimeException("Exception fetching products" + e.getLocalizedMessage());
		}
		
		// Retrieve the ProductsResponse from the Future
		try {
			fetchedProducts = futureProductResponse.get();
		} catch (InterruptedException e) {
			fetchedProducts = null;			// Has the effect of causing the Test to fail!
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RuntimeException("Execution exception fetching test Product! " + e.getLocalizedMessage());
		}
		
		// assert that we got a response
		assertNotNull("Products Response is null", fetchedProducts);
		assertFalse("Fetched Products List is empty", fetchedProducts.list().isEmpty());
		
		// assert that the Products have the same ModelNo we provided
		int count = 0;
		for (Product product : fetchedProducts.list()) {
			System.out.printf("Retrieved ModelNum: %d is %s", count, product.getModelNumber());
			assertEquals("Retrieved ModelNum is different", "2000-428dx", product.getModelNumber());
			count++;
		}
		
	}
	
	@Test
	public void testFetchProductsForModelNumbers() {
		Future<List<Product>> fetchedProducts;
		MzModelNumber modelNum1 = 
				new MzModelNumber("2000-428dx", "HP", "Laptops", null, "PC Laptops", "5026109", null);
		MzModelNumber modelNum2 = 
				new MzModelNumber("A7J89UT", "HP", "Laptops", null, "PC Laptops", "4856708", null);
		List<MzModelNumber> modelList = new ArrayList<MzModelNumber>();
		modelList.add(modelNum1);
		modelList.add(modelNum2);
		
		try {
			fetchedProducts = productService.fetchProductsForModelNumbers(modelList);
		} catch (Exception e) {
			fetchedProducts = null;
			System.out.println("Exception thrown during Products Fetch" + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		// assert we got a response
		assertNotNull("Future Products Response is null", fetchedProducts);
		Set<String> modelNumSet = new HashSet<String>();
		modelNumSet.add(modelNum1.getId().getModelNum());
		modelNumSet.add(modelNum2.getId().getModelNum());
		//if (fetchedProducts.isDone()) {
			try {
				List<Product> productsResponse = fetchedProducts.get();
				assertNotNull("Products Response is null", productsResponse);
				assertTrue("List does not have 2 Products", productsResponse.size() == 2);
				
				//Because the methods under test are asynchronous we have no way of knowing in what order the Products
				// are retrieved so we just test that the retrieved Product's modelNumbers are contained in the modelNumSet
				assertTrue("Retrieved ModelNum is different", modelNumSet.contains( productsResponse.get(0).getModelNumber()));
				assertTrue("Retrieved ModelNum is different", modelNumSet.contains(productsResponse.get(1).getModelNumber()));
			} catch (InterruptedException e) {
				System.out.println("Interrupted Exception was thrown");
				e.printStackTrace();
			} catch (ExecutionException e) {
				System.out.println("Execution Exception was thrown");
				e.printStackTrace();
			}
		//}
	}
	
	@Test
	public void testFetchProductsForModelNumbersByPK() {
		Future<List<Product>> fetchedProducts;
		MzModelNumberPK modelNum1 = 
				new MzModelNumberPK("2000-2a10nr", "HP");
		MzModelNumberPK modelNum2 = 
				new MzModelNumberPK("SPH-L710", "Samsung");
		List<MzModelNumberPK> modelList = new ArrayList<MzModelNumberPK>();
		modelList.add(modelNum1);
		modelList.add(modelNum2);
		
		try {
			fetchedProducts = productService.fetchProductsForModelNumbersByPK(modelList);
		} catch (Exception e) {
			fetchedProducts = null;
			System.out.println("Exception thrown during Products Fetch" + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		// assert we got a response
		assertNotNull("Future Products Response is null", fetchedProducts);
		Set<String> modelNumSet = new HashSet<String>();
		modelNumSet.add(modelNum1.getModelNum());
		modelNumSet.add(modelNum2.getModelNum());
		//if (fetchedProducts.isDone()) {
			try {
				List<Product> productsResponse = fetchedProducts.get();
				assertNotNull("Products Response is null", productsResponse);
				assertTrue("List does not have Products", productsResponse.size() > 0);
				//System.out.printf("Size of List of Products: %d", productsResponse.size() );
				
				//Because the methods under test are asynchronous we have no way of knowing in what order the Products
				// are retrieved so we just test that the retrieved Product's modelNumbers are contained in the modelNumSet
				assertTrue("Retrieved ModelNum is different", modelNumSet.contains( productsResponse.get(0).getModelNumber()));
				assertTrue("Retrieved ModelNum is different", modelNumSet.contains(productsResponse.get(1).getModelNumber()));
			} catch (InterruptedException e) {
				System.out.println("Interrupted Exception was thrown");
				e.printStackTrace();
			} catch (ExecutionException e) {
				System.out.println("Execution Exception was thrown");
				e.printStackTrace();
			}
	} 
	
	@Test
	public void testFetchProductsForSKUs() {
		
		// Create List of SKUs and ProductsResponse
		ProductsResponse fetchedProducts;
		Future<ProductsResponse> futureProductResponse;
		List<String> skuList = new ArrayList<String>();
		skuList.add("4550361");
		skuList.add("20811916");
		skuList.add("2638269");
		skuList.add("6877744");
		
		// Test the Fetch		
		try {
			futureProductResponse = productService.fetchProductsForSKUs(skuList);
		} catch (Exception e) {
			System.out.println("Exception fetching products" + e.getLocalizedMessage());
			e.printStackTrace();
			throw new RuntimeException("Exception fetching products" + e.getLocalizedMessage());
		}

		// Retrieve the ProductsResponse from the Future
		try {
			fetchedProducts = futureProductResponse.get();
		} catch (InterruptedException e) {
			fetchedProducts = null;			// Has the effect of causing the Test to fail!
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new RuntimeException("Execution exception fetching test Product! " + e.getLocalizedMessage());
		}

		// assert that we got a response
		assertNotNull("Products Response is null", fetchedProducts);
		assertFalse("Fetched Products List is empty", fetchedProducts.list().isEmpty());
		assertEquals("Unexpected no of fetched Products", 4, fetchedProducts.list().size());

		// assert that the Products have the same SKU we provided
		for (Product product : fetchedProducts.list()) {
			assertTrue("Unexpected SKU retrieved", skuList.contains(product.getSku()));			
		}
	}
	
	@Test
	public void testCreateSkuQueryString() {
		List<String> skuList = new ArrayList<String>();
		skuList.add("4550361");
		skuList.add("20811916");
		skuList.add("2638269");
		skuList.add("6877744");
		String queryStr = productService.createSkuQueryString(skuList);
		assertNotNull(queryStr);
		assertFalse(queryStr.isEmpty());
		CharSequence querySeq = "4550361".subSequence(0, 6);
		assertTrue(queryStr.contains(querySeq));
		//System.out.println("SKU queryString: " + queryStr);
	}

	@Test
	public void testGetProductsByCategory() {
		
	}
	
	// Method that returns an instance of the request bean from the
	// EJBContainer
	public Object getBean(String bean) throws NamingException {
		return container.getContext()
				.lookup("java:global/ProductServiceBean-ejb/classes/" + bean);
	}

}
