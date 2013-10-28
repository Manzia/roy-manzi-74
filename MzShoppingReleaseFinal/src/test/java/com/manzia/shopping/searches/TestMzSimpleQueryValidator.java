package com.manzia.shopping.searches;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMzSimpleQueryValidator {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsValidQueryMap() {
		
		// Create Maps for each test scenario
		Map<String, String> testCategorySku = new HashMap<String, String>();
		testCategorySku.put("Category", "Tablets");
		testCategorySku.put("sku", "7039173");
		
		Map<String, String> testCategoryOnly = new HashMap<String, String>();
		testCategoryOnly.put("Category", "Tablets");
		testCategoryOnly.put("other", "7039173");
		
		Map<String, String> testSkuOnly = new HashMap<String, String>();
		testSkuOnly.put("other", "Tablets");
		testSkuOnly.put("sku", "7039173");
		
		Map<String, String> testNone = new HashMap<String, String>();
		testNone.put("first", "Tablets");
		testNone.put("second", "7039173");
		
		Map<String, String> testInvalidCategory = new HashMap<String, String>();
		testInvalidCategory.put("Category", "Cars");
		testInvalidCategory.put("sku", "7039173");
		
		Map<String, String> testInvalidSku = new HashMap<String, String>();
		testInvalidSku.put("Category", "Tablets");
		testInvalidSku.put("sku", "703-9173");
		
		// Test
		MzSimpleQueryValidator simpleValidator = new MzSimpleQueryValidator();
		assertNotNull(simpleValidator);
		assertTrue("Valid CategorySku Map was declared Invalid", simpleValidator.isValidQueryMap(testCategorySku));
		assertTrue("Valid CategoryOnly Map was declared Invalid", simpleValidator.isValidQueryMap(testCategoryOnly));
		assertTrue("Valid SkuOnly Map was declared Invalid", simpleValidator.isValidQueryMap(testSkuOnly));
		assertFalse("Map with missing Keys was declared Valid", simpleValidator.isValidQueryMap(testNone));
		assertFalse("Map with invalid Category was declared Valid", simpleValidator.isValidQueryMap(testInvalidCategory));
		assertFalse("Map with invalid Sku was declared Valid", simpleValidator.isValidQueryMap(testInvalidSku));
	}
	
	@Test
	public void testExtractQueryTerms() {
		String testPrefix = "Category:Tablets display";
		String testSplit = "category:Tablets attractive screen";
		String testSingle = "category:Tablets";
		String testEmpty = " ";
		
		// Test
		MzSimpleQueryValidator simpleValidator = new MzSimpleQueryValidator();
		assertNotNull(simpleValidator);
		assertTrue("Missed Prefix test", simpleValidator.extractQueryTerms(testPrefix).equalsIgnoreCase(testPrefix));
		assertTrue("Missed Split test", simpleValidator.extractQueryTerms(testSplit).equalsIgnoreCase("attractive screen"));
		assertTrue("Missed Single test", simpleValidator.extractQueryTerms(testSingle).equalsIgnoreCase("category:Tablets"));
		assertTrue("Missed Empty test", simpleValidator.extractQueryTerms(testEmpty).equalsIgnoreCase(" "));
	}

}
