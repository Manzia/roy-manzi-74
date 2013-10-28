package com.manzia.shopping.attributes;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.manzia.shopping.attributes.MzAttributeMapper;
import com.manzia.shopping.attributes.MzAttributeMapperImpl;

public class TestMzAttributeMapperImpl {
	private static final String testDirectory = "testDir";
	private static File attributeDir;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		attributeDir = new File( System.getProperty("user.dir"), testDirectory);
		assertNotNull(attributeDir);
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	// Test for existing attribute
	@Test
	public void testMappedValueForAttribute() {
		File attributeFile = new File( attributeDir, "ManziaAttributeValues.txt");
		assertNotNull(attributeFile);
		String category = "Laptops";
		String attribute = "Processor";
		String value = "Intel¨ 3rd Generation Coreª i3";
		MzAttributeMapper attributeMapper = new MzAttributeMapperImpl();
		assertNotNull(attributeMapper);
		attributeMapper.setAttributesFile(attributeFile);
		String result = attributeMapper.mappedValueForAttribute(category, attribute, value);
		assertNotNull(result);
		assertEquals("Unexpected option result", "Intel Core i3", result);
	}
	
	// Test for missing attribute
	@Test
	public void testMappedValueForAttributeMissing() {
		File attributeFile = new File( attributeDir, "ManziaAttributeValues.txt");
		assertNotNull(attributeFile);
		String category = "Laptops";
		String attribute = "someAttribute";
		String value = "someValue";
		MzAttributeMapper attributeMapper = new MzAttributeMapperImpl();
		assertNotNull(attributeMapper);
		attributeMapper.setAttributesFile(attributeFile);
		String result = attributeMapper.mappedValueForAttribute(category, attribute, value);
		assertNull("Result is not null", result);		
	}
	
	// Test for all Brand attribute values
	@Test
	public void testAllBrandAttributeValues() {
		File attributeFile = new File( attributeDir, "ManziaAttributeValues.txt");
		assertNotNull(attributeFile);
		MzAttributeMapper attributeMapper = new MzAttributeMapperImpl();
		assertNotNull(attributeMapper);
		attributeMapper.setAttributesFile(attributeFile);
		Set<String> allBrands = attributeMapper.allBrandAttributeValues();
		assertNotNull(allBrands);
		assertTrue("Missing HP brand", allBrands.contains("HP"));
		assertTrue("Missing Samsung Brand", allBrands.contains("Samsung"));
	}

}
