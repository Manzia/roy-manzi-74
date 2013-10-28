package com.manzia.shopping.bestbuy;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestProductResponse {
	
	//File
	private File productFile;
	private final String productFilename = "sampleBestBuyLaptop.xml";
	private final String productSKU = "5689198";
	private ProductResponse productResponse;
	private Product product;

	@Before
	public void setUp() throws Exception {
		productFile = new File(System.getProperty("user.dir"), productFilename);
	}

	@After
	public void tearDown() throws Exception {
		productFile = null;
		productResponse = null;		
	}

	@Test
	public void testProductResponseFile() {
		try {
			productResponse = new ProductResponse(productFile);
			assertNotNull(productResponse);			
		} catch (RemixException e) {
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage());
		}
	}

	@Test
	public void testProduct() {
		try {
			productResponse = new ProductResponse(productFile);
			product = productResponse.product();
			assertNotNull(product);
			//assertTrue("Product SKUs are different", product.getSku().equalsIgnoreCase(productSKU));
		} catch (RemixException e) {
			e.printStackTrace();
			System.err.println(e.getLocalizedMessage());
		}
	}

}
