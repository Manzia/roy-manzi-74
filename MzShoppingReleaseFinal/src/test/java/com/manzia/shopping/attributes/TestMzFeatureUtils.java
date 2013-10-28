package com.manzia.shopping.attributes;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.manzia.shopping.attributes.MzFeatureUtils;

public class TestMzFeatureUtils {
	private static final float priceInterval = 25.0f;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetPriceRangeMedian() {
		float zeroFloat = 0.0f;
		float medPrice = 250.0f;
		float smallPrice = 49.0f;
		
		// Test
		float zeroRange = MzFeatureUtils.getPriceRangeMedian(zeroFloat);
		assertTrue("Unexpected Median Value: zeroRange", zeroRange == zeroFloat);
		float medMedian = medPrice + (priceInterval / 2);
		float medRange = MzFeatureUtils.getPriceRangeMedian(medPrice);
		assertTrue("Unexpected Median Value: medRange", medRange == medMedian);
		float smallMedian = (Math.round(smallPrice / priceInterval) * priceInterval) - (priceInterval / 2);
		float smallRange = MzFeatureUtils.getPriceRangeMedian(smallPrice);
		assertTrue("Unexpected Median Value: smallRange", smallRange == smallMedian);
	}

	@Test
	public void testGetRelationFeatures() {
		Map<String, String> featureMap = new HashMap<String, String>();
		featureMap.put("Brand", "HP");
		featureMap.put("Memory", "16GB");
		featureMap.put("HardDrive", "500GB");
		featureMap.put("Price", "250.00");
		featureMap.put("Processor", "Intel Core i5");
		
		// Test
		Map<String, String> relationMap = MzFeatureUtils.getRelationFeatures(featureMap);
		assertNotNull(relationMap);
		assertFalse("Relation Map is Empty!", relationMap.isEmpty());
		Set<String> relationSet = relationMap.keySet();
		Iterator<String> relationIterator = relationSet.iterator();
		int brandCount = 0;
		int priceCount = 0;
		while (relationIterator.hasNext()) {
			String key = relationIterator.next();
			if ( key.startsWith("Brand")) brandCount++;
			if ( key.endsWith("Price")) priceCount++;			
		}
		assertEquals("Unexpected Brand Count", 7, brandCount);
		assertEquals("Unexpected Price Count", 4, priceCount);
	}

}
