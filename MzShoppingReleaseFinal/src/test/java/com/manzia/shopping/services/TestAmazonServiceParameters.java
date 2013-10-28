package com.manzia.shopping.services;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestAmazonServiceParameters {
	MzRetailServiceParameters amazonParams;
	
	// Instantiate the class to test
	@Before
	public void instantiate() {
		amazonParams = new AmazonServiceParameters();
	}

	@Test
	public void testGetItemSearchParameters() {
		Map<String, String> testMap = 
				amazonParams.getItemSearchParameters(MzItemSearchType.AMAZON_DEFAULT);
		assertTrue("Operation value inserted ", 
				testMap.get("Operation").equals("ItemSearch"));
		assertTrue("SearchIndex value inserted ", 
				testMap.get("SearchIndex").equals("Apparel"));		
	}

	@Test
	public void testGetItemDetailParameters() {
		Map<String, String> testsMap =
				amazonParams.getItemDetailParameters(MzItemLookupType.AMAZON_DEFAULT);
		assertTrue("Operation value inserted ", 
				testsMap.get("Operation").equals("ItemLookup"));
	}
	
	@Test
	public void testGetItemDetailParametersUndefined() {
		
		// Test the case where the detailType has not been implemented
		Map<String, String> testsMap =
				amazonParams.getItemDetailParameters(MzItemLookupType.AMAZON_TESTCASE);
		assertTrue("Empty map is returned ", testsMap.isEmpty());
	}
	
	@Test
	public void testGetItemSearchParametersUndefined() {
		Map<String, String> testMap = 
				amazonParams.getItemSearchParameters(MzItemSearchType.AMAZON_TESTCASE);
		assertTrue("Empty map returned ", testMap.isEmpty());		
	}
	
	@Test
	public void testGetBrowseNodeParameters() {
		Map<String, String> testsMap =
				amazonParams.getBrowseNodeParameters(MzBrowseNodeLookupType.AMAZON_DEFAULT);
		assertTrue("Operation value inserted ", 
				testsMap.get("Operation").equals("BrowseNodeLookup"));
	}
	
	@Test
	public void testGetBrowseNodeParametersUndefined() {
		Map<String, String> testMap = 
				amazonParams.getBrowseNodeParameters(MzBrowseNodeLookupType.AMAZON_TESTCASE);
		assertTrue("Empty map returned ", testMap.isEmpty());		
	}

}

