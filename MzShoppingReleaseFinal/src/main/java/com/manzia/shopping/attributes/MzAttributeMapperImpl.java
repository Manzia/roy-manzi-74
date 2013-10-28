package com.manzia.shopping.attributes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see MzAttributeMapper#mappedValueForAttribute(String, String)
 * @author Roy Manzi Tumubweinee, Nov 7, 2012, Manzia Corporation
 *
 */
public class MzAttributeMapperImpl implements MzAttributeMapper {
	
	//Logger
	public static final Logger logger = 
						Logger.getLogger(MzAttributeMapperImpl.class.getCanonicalName());
	
	// Attribute File entry separator
	private static final String KAttributeEntrySeparator = ":";
	private static final String KBrandAttribute = "Brand";
	
	//Default Filename
	private static final String DEFAULT_ATTRIBUTE_FILE = "ManziaAttributeValues.txt";
	private File attributesFile;
	
	// Properties Map
	private Properties attributesMap;
	
	// Setter and getter for attribute FileName
	@Override
	public final File getAttributesFile() {
		return attributesFile;
	}
	@Override
	public final void setAttributesFile(File attributesFile) {
		// set and read the attributesFile
		this.attributesFile = attributesFile;
		this.readAttributesFile();
		
	}

	/*
	 * Process flow:
	 * 1- read in the ManziaAttributes file
	 * 2- Generate the appropriate MzAttributeItem object based in input attribute argument
	 * 3- return the matching value/Option for the input value argument
	 * 
	 * @see com.manzia.shopping.attributes.MzAttributeMapper#mappedValueForAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public String mappedValueForAttribute(String category, String attribute, String value) {
		// Check inputs
		if (category == null || attribute == null || value == null || 
				category.isEmpty() || attribute.isEmpty() || value.isEmpty()) {
			logger.log(Level.WARNING, "Invalid category or attribute or value argument specified...will return NULL!");
			return null;
		}
		// Result
		String result = null;
		
		// Get the match
		//Properties attributeMap = readAttributesFile(this.attributesFile);
		//assert attributeMap != null;
		StringBuffer keyBuffer = new StringBuffer();
		assert keyBuffer != null;
		keyBuffer.append(category).append(KAttributeEntrySeparator).append(attribute);
		if (this.attributesMap.size() > 0) {
			String options = this.attributesMap.getProperty(keyBuffer.toString());
			if (options != null && options.length() > 0) {
				MzAttributeItem attributeItem = new MzAttributeItem(attribute, options);
				assert attributeItem != null;
				result = attributeItem.uniqueMatchingOption(value);
			}
		}
		return result;
	}
	
	public void readAttributesFile() {
		// Result
		this.attributesMap = new Properties();
		assert this.attributesMap != null;
		
		// check input
		if (this.attributesFile == null || !this.attributesFile.exists() || !this.attributesFile.isFile()) {
			logger.log(Level.WARNING, "Invalid attributes File specified...will use Default Attributes File!");
			this.setAttributesFile(new File( System.getProperty("user.dir"), DEFAULT_ATTRIBUTE_FILE));			
		}		
				
		// Read in the Properties file
				try {
					BufferedReader propertiesReader = 
							new BufferedReader(new FileReader(attributesFile));
					assert propertiesReader != null;
					this.attributesMap.load(propertiesReader);
					logger.log(Level.INFO, "Loaded Attributes File..." + attributesFile.getName());
					propertiesReader.close();
				} catch (FileNotFoundException fe) {
					logger.log(Level.SEVERE, "Input Attributes File Not Found", fe);
					fe.printStackTrace();
				} catch (IOException ie) {
					logger.log(Level.SEVERE, "IO Exception loading Attributes file", ie);
					ie.printStackTrace();
					throw new RuntimeException("IO Exception loading Attributes file!" + ie.getLocalizedMessage());
				}				
	}
	/**
	 * @see MzAttributeMapper#allBrandAttributeValues()
	 */
	@Override
	public Set<String> allBrandAttributeValues() {
		// Result
		Set<String> result = new HashSet<String>();
		
		// check our attributesMap instance variable
		if (this.attributesMap == null || this.attributesMap.size() == 0) {
			logger.log(Level.WARNING, "Properties map of Attributes is NULL or EMPTY!");
			return result;
		}
		
		// Iterate over the attributesMap
		Set<Object> attributeSet = this.attributesMap.keySet();
		assert attributeSet != null;
		Iterator<Object> attributeIterator = attributeSet.iterator();
		assert attributeIterator != null;
		MzAttributeItem attributeItem;
		while (attributeIterator.hasNext()) {
			String key = (String) attributeIterator.next();
			String[] keyArray = key.split(KAttributeEntrySeparator);
			assert keyArray != null;
			assert keyArray.length == 2;
			String attribute = keyArray[1];
			if (attribute.equals(KBrandAttribute)) {
				String value = this.attributesMap.getProperty(key);
				assert value != null;
				attributeItem = new MzAttributeItem(attribute, value);
				assert attributeItem != null;
				result.addAll(attributeItem.getOptionsList());
			}			
		}
		// Log and return
		logger.log(Level.INFO, "Attributes File contains: {0} unique Brand attribute values", new Object[]{result.size()});
		
		return result;
	}

}
