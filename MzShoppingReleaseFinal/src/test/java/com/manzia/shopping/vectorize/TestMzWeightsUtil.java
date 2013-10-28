package com.manzia.shopping.vectorize;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMzWeightsUtil {
	private static final String kCurrentWeightsFile = "CurrentAttributeWeights";
	private static final String kBrandKey = "Brand";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetInitialWeightsMap() {
		Properties initialProps = MzWeightsUtil.getInitialWeightsMap();
		assertNotNull(initialProps);
		assertFalse("Initial Properties is Empty", initialProps.isEmpty());
		assertEquals("Unexpected Brand Weight", "50", initialProps.getProperty(kBrandKey));
	}

	@Test
	public void testGetCurrentWeightsMap() {
		Properties currentProps = new Properties();
		currentProps.put(kBrandKey, "50");
		File currentPropsFile = new File( System.getProperty("user.dir"), kCurrentWeightsFile);
		assertNotNull(currentPropsFile);
		MzWeightsUtil.saveWeightsMapToFile(currentProps, currentPropsFile);
		
		// Test
		Properties savedProps = MzWeightsUtil.getCurrentWeightsMap();
		assertNotNull(savedProps);
		assertFalse("Current Properties is Empty", savedProps.isEmpty());
		assertEquals("Unexpected Brand Weight", "50", savedProps.getProperty(kBrandKey));
		
		// Delete File so we don't interrupt Production Logic
		assertTrue("Current Properties was not deleted", currentPropsFile.delete());
	}
	

}
