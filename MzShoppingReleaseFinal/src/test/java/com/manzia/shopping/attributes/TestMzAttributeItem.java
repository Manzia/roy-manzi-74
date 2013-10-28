package com.manzia.shopping.attributes;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.manzia.shopping.attributes.MzAttributeItem;

public class TestMzAttributeItem {
	
	private static Map<String, String> manziaAttributes;
	private MzAttributeItem attributeItem;

	@BeforeClass
	public static void setUpClass() throws Exception {
		manziaAttributes = new HashMap<String, String>();
		manziaAttributes.put("Brand", 
				"HP,Apple,Dell,Sony,Apple,Samsung,Lenovo,Gateway,Toshiba,Acer,Asus,Fujitsu,Alienware,Razer,Panasonic,Vizio,Maingear,MSI,Motion");
		manziaAttributes.put("Optical Drive", "Yes,No");
		manziaAttributes.put("HDMI Inputs", "4");
		manziaAttributes.put("Processor", "Intel Core i5,Intel Core i3,Intel Core i7,Intel Atom,AMD Athlon,AMD Turion,AMD Fusion");
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsBooleanAttribute() {
		Set<String> attributeKeys = manziaAttributes.keySet();
		assertNotNull(attributeKeys);
		Iterator<String> attributeIterator = attributeKeys.iterator();
		assertNotNull(attributeIterator);
		
		int success = 0;
		while (attributeIterator.hasNext()) {
			String key = attributeIterator.next();
			String value = manziaAttributes.get(key);
			attributeItem = new MzAttributeItem(key, value);
			assertNotNull(attributeItem);
			if (attributeItem.isBooleanAttribute()) {
				success++;
			}
		}
		// Validate
		assertEquals("Unexpected number of boolean attributes", 1, success);
	}

	@Test
	public void testIsNumericAttribute() {
		Set<String> attributeKeys = manziaAttributes.keySet();
		assertNotNull(attributeKeys);
		Iterator<String> attributeIterator = attributeKeys.iterator();
		assertNotNull(attributeIterator);
		
		int success = 0;
		while (attributeIterator.hasNext()) {
			String key = attributeIterator.next();
			String value = manziaAttributes.get(key);
			attributeItem = new MzAttributeItem(key, value);
			assertNotNull(attributeItem);
			if (attributeItem.isNumericAttribute()) {
				success++;
			}
		}
		// Validate
		assertEquals("Unexpected number of numeric attributes", 1, success);
	}

	@Test
	public void testIsSingleWordOption() {
		Set<String> attributeKeys = manziaAttributes.keySet();
		assertNotNull(attributeKeys);
		Iterator<String> attributeIterator = attributeKeys.iterator();
		assertNotNull(attributeIterator);
		
		int success = 0;
		while (attributeIterator.hasNext()) {
			String key = attributeIterator.next();
			String value = manziaAttributes.get(key);
			attributeItem = new MzAttributeItem(key, value);
			assertNotNull(attributeItem);
			if (attributeItem.isSingleWordOption()) {
				success++;
			}
		}
		// Validate
		assertEquals("Unexpected number of single word attributes", 3, success);
	}

	@Test
	public void testUniqueMatchingOption() {
		Map<String, String> bestBuyAttributes = new HashMap<String, String>();
		bestBuyAttributes.put("Processor", "Intel 3rd Generation Core i3");
		bestBuyAttributes.put("HDMI Inputs", "41");
		bestBuyAttributes.put("Optical Drive", "NO");
		
		Set<String> bbAttributes = bestBuyAttributes.keySet();
		Iterator<String> bbIterator = bbAttributes.iterator();
		assertNotNull(bbIterator);
		
		while (bbIterator.hasNext()) {
			String key = bbIterator.next();
			if (key.equals("Processor")) {
				attributeItem = new MzAttributeItem(key, manziaAttributes.get(key));
				assertNotNull(attributeItem);
				String processorOption = attributeItem.uniqueMatchingOption(bestBuyAttributes.get(key));
				assertNotNull(processorOption);
				assertEquals("Processor Option mismatch", "Intel Core i3", processorOption);				
			} else if (key.equals("HDMI Inputs")) {
				attributeItem = new MzAttributeItem(key, manziaAttributes.get(key));
				assertNotNull(attributeItem);
				String processorOption = attributeItem.uniqueMatchingOption(bestBuyAttributes.get(key));
				assertNotNull(processorOption);
				assertEquals("HDMI Input Option mismatch", "4", processorOption);
			} else if (key.equals("Optical Drive")) {
				attributeItem = new MzAttributeItem(key, manziaAttributes.get(key));
				assertNotNull(attributeItem);
				String processorOption = attributeItem.uniqueMatchingOption(bestBuyAttributes.get(key));
				assertNotNull(processorOption);
				assertEquals("Optical Drive Option mismatch", "No", processorOption);
			}
		}
	}

	@Test
	public void testTokenizeString() {
		
	}

}
