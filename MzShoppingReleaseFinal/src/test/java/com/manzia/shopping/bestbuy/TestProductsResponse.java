package com.manzia.shopping.bestbuy;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestProductsResponse {
	
	//File
		private File productFile;
		private static final String testDirectory = "testDir";
		private final String productFilename = "sampleBestBuyLaptop.xml";
		private final String productSKU = "5689198";
		private final String phoneFileName = "sampleBestBuyPhone.xml";
		private final String phoneSKU = "5717486";
		private final String printerFileName = "sampleBestBuyPrinter.xml";
		private final String printerSKU = "9108654";
		private final String tabletFileName = "sampleBestBuyTablet.xml";
		private final String tabletSKU = "5215429";
		private final String tvFileName = "sampleBestBuyTVs.xml";
		private final String tvSKU = "4756681";
		private ProductsResponse productsResponse;
		private Product product;

	@Before
	public void setUp() throws Exception {
		File testDir = new File(System.getProperty("user.dir"), testDirectory);
		assertNotNull(testDir);
		productFile = new File( testDir, productFilename);
		assertNotNull(productFile);
	}

	@After
	public void tearDown() throws Exception {
		productsResponse = null;
		if (product != null) {
			product = null;
		}
	}

	@Test
	public void testProductsResponseFile() {
		try {
			productsResponse = new ProductsResponse(productFile);
			assertNotNull(productsResponse);			
		} catch (RemixException e) {
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage());
		}
	}

	@Test
	public void testList() {
		try {
			productsResponse = new ProductsResponse(productFile);
			product = productsResponse.list().get(0);
			assertNotNull(product);
			assertTrue("Product SKUs are different", product.getSku().equalsIgnoreCase(productSKU));
		} catch (RemixException e) {
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage());
		}		
	}
	
	@Test
	public void testGetDetailsMap() {
		Map<String, String> detailMap = new HashMap<String, String>();
		try {
			productsResponse = new ProductsResponse(productFile);
			product = productsResponse.list().get(0);
			assertNotNull(product);
			detailMap = product.getDetailsMap();
			assertTrue("Value for Processor Brand Key is not AMD", detailMap.get("Processor Brand").equalsIgnoreCase("AMD"));
			//System.out.println("Value for Processor Brand Key: " + detailMap.get("Processor Brand"));
		} catch (RemixException e) {
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage());
		}		
	}

}
