package com.manzia.shopping.products;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.manzia.shopping.bestbuy.Product;
import com.manzia.shopping.bestbuy.ProductsResponse;
import com.manzia.shopping.bestbuy.RemixException;

public class TestMzProductsConverterImpl {
	
	//File
			private static File laptopFile;
			private static File phoneFile;
			private static File printerFile;
			private static File tabletFile;
			private static File tvsFile;
			private static final String testDirectory = "testDir";
			private final static String productFilename = "sampleBestBuyLaptop.xml";
			private final String productSKU = "5689198";
			private final static String phoneFileName = "sampleBestBuyPhone.xml";
			private final String phoneSKU = "5717486";
			private final String secondPhoneSKU ="5717371";
			private final static String printerFileName = "sampleBestBuyPrinter.xml";
			private final String printerSKU = "9108654";
			private final static String tabletFileName = "sampleBestBuyTablet.xml";
			private final String tabletSKU = "5215429";
			private final static String tvFileName = "sampleBestBuyTVs.xml";
			private final String tvSKU = "4756681";
			private ProductsResponse productsResponse;
			

	@BeforeClass
	public static void setUpClass() throws Exception {
		File testDir = new File(System.getProperty("user.dir"), testDirectory);
		assertNotNull(testDir);
		laptopFile = new File( testDir, productFilename);
		assertNotNull(laptopFile);
		phoneFile = new File( testDir, phoneFileName);
		assertNotNull(phoneFile);
		printerFile = new File (testDir, printerFileName);
		assertNotNull(printerFile);
		tabletFile = new File(testDir, tabletFileName);
		assertNotNull(tabletFile);
		tvsFile = new File(testDir, tvFileName);
		assertNotNull(tvsFile);
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		if (productsResponse != null) {
			productsResponse = null;
		}
	}
	
	/*
	 * Laptop
	 * <regularPrice>384.98</regularPrice>, <salePrice>384.98</salePrice>
	 * Printer
	 * <regularPrice>249.99</regularPrice>, <salePrice>237.98</salePrice>
	 * Phone 1
	 * <regularPrice>699.99</regularPrice>, <salePrice>699.99</salePrice>
	 * Phone 2
	 * <regularPrice>699.99</regularPrice>, <salePrice>699.99</salePrice>
	 * Tablet
	 * <regularPrice>399.99</regularPrice>, <salePrice>399.99</salePrice>
	 * TVs
	 * <regularPrice>1599.99</regularPrice>, <salePrice>1099.99</salePrice>
	 */
	
	@Test
	public void testConvertToRankedProductsLowest() {
		// Create a ProductResponse
				ProductsResponse phoneResponse;
				ProductsResponse printerResponse;
				ProductsResponse tabletResponse;
				ProductsResponse tvsResponse;
				
				//Laptop
				try {
					productsResponse = new ProductsResponse(laptopFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + laptopFile.getName());
					e.printStackTrace();
				}
				
				//Phone
				try {
					phoneResponse = new ProductsResponse(phoneFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + phoneFile.getName());
					phoneResponse = null;
					e.printStackTrace();
				}
				
				//Printer
				try {
					printerResponse = new ProductsResponse(printerFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + printerFile.getName());
					printerResponse = null;
					e.printStackTrace();
				}
				
				// Tablet
				try {
					tabletResponse = new ProductsResponse(tabletFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + tabletFile.getName());
					tabletResponse = null;
					e.printStackTrace();
				}
				
				// TVs
				try {
					tvsResponse = new ProductsResponse(tvsFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + tvsFile.getName());
					tvsResponse = null;
					e.printStackTrace();
				}
				
				// Combine all the ProductResponse objects into one object
				assertNotNull(productsResponse);
				assertNotNull(phoneResponse);
				assertNotNull(printerResponse);
				assertNotNull(tabletResponse);
				assertNotNull(tvsResponse);
				productsResponse.list().addAll(phoneResponse.list());
				productsResponse.list().addAll(printerResponse.list());
				productsResponse.list().addAll(tabletResponse.list());
				productsResponse.list().addAll(tvsResponse.list());
				MzProductsConverter productConverter = new MzProductsConverterImpl();
				RankedProducts rankedProducts;
				
				// Test, price less than lowest should return cheapest Products (i.e Laptop & Printer) for K=2;
				Set<String> lowestSet = new HashSet<String>();
				lowestSet.add(productSKU);
				lowestSet.add(printerSKU);				
				rankedProducts = productConverter.convertToRankedProducts(productsResponse.list(), 200.0f, 2);
				assertNotNull(rankedProducts);
				assertEquals("Unexpected number of Products", 1, rankedProducts.getRankedProduct().size());
				for (RankedProductType prodType : rankedProducts.getRankedProduct()) {
					assertTrue("RankedProductType has unexpected Price", lowestSet.contains(prodType.getId()));
					System.out.printf("Product Title: %s\n", prodType.getTitle().getValue());
					System.out.printf("Product Link: %s\n", prodType.getLink().getHref());
					System.out.printf("ProductImage Link: %s\n", prodType.getImageLink());
					System.out.printf("ProductThumbnail: %s\n", prodType.getThumbnailLink());
					System.out.printf("Product Id: %s\n", prodType.getId());
					System.out.printf("Product Description: %s\n", prodType.getDescription());
					System.out.printf("Product Language: %s\n", prodType.getContentLanguage());
					System.out.printf("Product Country: %s\n", prodType.getTargetCountry());
					System.out.printf("Product ClassId: %s\n", prodType.getProductType().getClassId());
					System.out.printf("Product SubClassId: %s\n", prodType.getProductType().getSubClassId());
					System.out.printf("Product PriceUnit: %s\n", prodType.getPrice().getUnit());
					System.out.printf("Product Price: %s\n", prodType.getPrice().getValue());
					System.out.printf("Product Brand: %s\n", prodType.getBrand());
					System.out.printf("Product Condition: %s\n", prodType.getCondition());
					System.out.printf("Product Availability: %s\n", prodType.getAvailability());
				}
	}
	
	@Test
	public void testConvertToRankedProductsHighest() {
		// Create a ProductResponse
		ProductsResponse phoneResponse;
		ProductsResponse printerResponse;
		ProductsResponse tabletResponse;
		ProductsResponse tvsResponse;
		
		//Laptop
		try {
			productsResponse = new ProductsResponse(laptopFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + laptopFile.getName());
			e.printStackTrace();
		}
		
		//Phone
		try {
			phoneResponse = new ProductsResponse(phoneFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + phoneFile.getName());
			phoneResponse = null;
			e.printStackTrace();
		}
		
		//Printer
		try {
			printerResponse = new ProductsResponse(printerFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + printerFile.getName());
			printerResponse = null;
			e.printStackTrace();
		}
		
		// Tablet
		try {
			tabletResponse = new ProductsResponse(tabletFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + tabletFile.getName());
			tabletResponse = null;
			e.printStackTrace();
		}
		
		// TVs
		try {
			tvsResponse = new ProductsResponse(tvsFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + tvsFile.getName());
			tvsResponse = null;
			e.printStackTrace();
		}
		
		// Combine all the ProductResponse objects into one object
		assertNotNull(productsResponse);
		assertNotNull(phoneResponse);
		assertNotNull(printerResponse);
		assertNotNull(tabletResponse);
		assertNotNull(tvsResponse);
		productsResponse.list().addAll(phoneResponse.list());
		productsResponse.list().addAll(printerResponse.list());
		productsResponse.list().addAll(tabletResponse.list());
		productsResponse.list().addAll(tvsResponse.list());
		MzProductsConverter productConverter = new MzProductsConverterImpl();
		RankedProducts rankedProducts;
		
		// Test, price higher than highest should return most expensive Products (i.e TV & Phone) for K=2;
		// Note: if 2 Products have the same price, any one of the 2 will be returned BUT NOT BOTH!!!
		Set<String> highestSet = new HashSet<String>();
		highestSet.add(tvSKU);
		highestSet.add(phoneSKU);
		highestSet.add(secondPhoneSKU);
		rankedProducts = productConverter.convertToRankedProducts(productsResponse.list(), 2000.0f, 2);
		assertNotNull(rankedProducts);
		assertEquals("Unexpected number of Products", 1, rankedProducts.getRankedProduct().size());
		for (RankedProductType prodType : rankedProducts.getRankedProduct()) {
			assertTrue("RankedProductType has unexpected Price", highestSet.contains(prodType.getId()));			
		}
	}
	
	@Test
	public void testConvertToRankedProductsMiddle() {
		// Create a ProductResponse
				ProductsResponse phoneResponse;
				ProductsResponse printerResponse;
				ProductsResponse tabletResponse;
				ProductsResponse tvsResponse;
				
				//Laptop
				try {
					productsResponse = new ProductsResponse(laptopFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + laptopFile.getName());
					e.printStackTrace();
				}
				
				//Phone
				try {
					phoneResponse = new ProductsResponse(phoneFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + phoneFile.getName());
					phoneResponse = null;
					e.printStackTrace();
				}
				
				//Printer
				try {
					printerResponse = new ProductsResponse(printerFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + printerFile.getName());
					printerResponse = null;
					e.printStackTrace();
				}
				
				// Tablet
				try {
					tabletResponse = new ProductsResponse(tabletFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + tabletFile.getName());
					tabletResponse = null;
					e.printStackTrace();
				}
				
				// TVs
				try {
					tvsResponse = new ProductsResponse(tvsFile);
				} catch (RemixException e) {
					System.err.println("RemixException while processing XML file: " + tvsFile.getName());
					tvsResponse = null;
					e.printStackTrace();
				}
				
				// Combine all the ProductResponse objects into one object
				assertNotNull(productsResponse);
				assertNotNull(phoneResponse);
				assertNotNull(printerResponse);
				assertNotNull(tabletResponse);
				assertNotNull(tvsResponse);
				productsResponse.list().addAll(phoneResponse.list());
				productsResponse.list().addAll(printerResponse.list());
				productsResponse.list().addAll(tabletResponse.list());
				productsResponse.list().addAll(tvsResponse.list());
				MzProductsConverter productConverter = new MzProductsConverterImpl();
				RankedProducts rankedProducts;
				
				// Test, price in the middle should return "closest" Products (i.e Laptop & Tablet) for K=2 and Price = $390;
				Set<String> middleSet = new HashSet<String>();
				middleSet.add(productSKU);
				middleSet.add(tabletSKU);				
				rankedProducts = productConverter.convertToRankedProducts(productsResponse.list(), 390.0f, 2);
				assertNotNull(rankedProducts);
				assertEquals("Unexpected number of Products", 2, rankedProducts.getRankedProduct().size());
				for (RankedProductType prodType : rankedProducts.getRankedProduct()) {
					assertTrue("RankedProductType has unexpected Price", middleSet.contains(prodType.getId()));
				}
	}
	

	@Test
	public void testConvertToRankedProductType() {
		
		// Create a ProductResponse
		ProductsResponse phoneResponse;
		ProductsResponse printerResponse;
		ProductsResponse tabletResponse;
		ProductsResponse tvsResponse;
		
		//Laptop
		try {
			productsResponse = new ProductsResponse(laptopFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + laptopFile.getName());
			e.printStackTrace();
		}
		
		//Phone
		try {
			phoneResponse = new ProductsResponse(phoneFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + phoneFile.getName());
			phoneResponse = null;
			e.printStackTrace();
		}
		
		//Printer
		try {
			printerResponse = new ProductsResponse(printerFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + printerFile.getName());
			printerResponse = null;
			e.printStackTrace();
		}
		
		// Tablet
		try {
			tabletResponse = new ProductsResponse(tabletFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + tabletFile.getName());
			tabletResponse = null;
			e.printStackTrace();
		}
		
		// TVs
		try {
			tvsResponse = new ProductsResponse(tvsFile);
		} catch (RemixException e) {
			System.err.println("RemixException while processing XML file: " + tvsFile.getName());
			tvsResponse = null;
			e.printStackTrace();
		}
		
		// Combine all the ProductResponse objects into one object
		assertNotNull(productsResponse);
		assertNotNull(phoneResponse);
		assertNotNull(printerResponse);
		assertNotNull(tabletResponse);
		assertNotNull(tvsResponse);
		productsResponse.list().addAll(phoneResponse.list());
		productsResponse.list().addAll(printerResponse.list());
		productsResponse.list().addAll(tabletResponse.list());
		productsResponse.list().addAll(tvsResponse.list());
		
		// Test each Product
		RankedProductType rankedType;
		MzProductsConverter productConverter = new MzProductsConverterImpl();
		Set<String> skuSet = new HashSet<String>();
		skuSet.add(productSKU);
		skuSet.add(phoneSKU);
		skuSet.add(printerSKU);
		skuSet.add(secondPhoneSKU);
		skuSet.add(tabletSKU);
		skuSet.add(tvSKU);
		for (Product product : productsResponse.list()) {
			rankedType = ((MzProductsConverterImpl) productConverter).convertToRankedProductType(product);
			assertTrue("Product contains unknown SKU", skuSet.contains(rankedType.getId()));			
		}
	}

}
