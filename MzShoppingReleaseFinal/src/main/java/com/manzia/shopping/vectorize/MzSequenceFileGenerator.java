package com.manzia.shopping.vectorize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.lucene.analysis.CharReader;
import org.apache.lucene.analysis.CharStream;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.vectorizer.encoders.ContinuousValueEncoder;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

import com.manzia.shopping.attributes.MzAttributeMapper;
import com.manzia.shopping.attributes.MzAttributeMapperImpl;
import com.manzia.shopping.attributes.MzFeatureUtils;
import com.manzia.shopping.bestbuy.Category;
import com.manzia.shopping.bestbuy.Product;
import com.manzia.shopping.bestbuy.ProductsResponse;
import com.manzia.shopping.bestbuy.RemixException;



/**
 * Class that generates a Apache Hadoop SequenceFiles containing Apache Mahout Vectors
 * given a BestBuy API ProductsResponses, i.e, each SequenceFile contains one Vector for each
 * ProductsResponse in the BestBuy APIs ProductsResponse list of Product. 
 * This class can be executed from the command line manually by passing a directory of XML files
 * representing ProductsResponse and a Properties file whose Keys are the Features to be used 
 * in feature-hashing the Products into Vectors. If no Properties File is passed as the second
 * argument, the class uses the default Properties File.
 * 
 * WEIGHTING SCHEME:
 * Vectors generated are weighted as follows
 * 1- Product Price and Brand features = kLargeWeight
 * 2- Product Details features = kMediumWeight
 * 3- Features features = kSmallWeight
 * 
 * @author Roy Manzi Tumubweinee, September 29, 2012, Manzia Corporation. 
 * Copyright, Manzia Corporation. All Rights Reserved.
 *
 */

public class MzSequenceFileGenerator {
	
	/**
	 * <p>Instance variable that holds the Properties file that contains
	 * all the features that will be vectorized. Each entry in the Properties
	 * object maps a feature to a BestBuy API product attribute. </p>
	 * 
	 * <p>NOTE: the features in the Properties file match the taskAttributes in the
	 * Tasks XML file pushed to User Devices and that appear as Search Options
	 * on the User's Device. Thus any changes to the features in the Properties file
	 * will necessitate the following: </p>
	 * 
	 * 1- make the exact same change to the Tasks XML file <br>
	 * 2- recreate the SequenceFiles since the features used to encode Vectors are now
	 * different <br>
	 * 3- the number of entries in the Properties file corresponds to how many features
	 * are encoded<br> 
	 */
	private static Properties vectorMap;
	
	// MzAttributeMapper that maps BestBuy attribute values to Manzia attribute values
	private static MzAttributeMapper attributeMapper;
	
	// Properties object that contains the Feature weights to be used during encoding
	private static Properties featureWeights;
	
	// Properties File name
	private static String PROPERTIESFILENAME = "ManziaBestBuyVectorMap.txt";
	
	//Logger
	public static final Logger logger = 
				Logger.getLogger(MzSequenceFileGenerator.class.getCanonicalName());
	
	// Number of Features in Vector i.e Vector Size
	public static final int VECTOR_SIZE = 1000;
	
	// Product Categories
	public static final List<String> CATEGORY_LIST = 
			Arrays.asList("TVs", "Laptops", "Tablets", "Mobile Phones", "Printers");
	
	// Key Vector Features - will likely need to be weighted during encoding
	//private static final String nullString = null;
	public static final String kPriceFeatureName = "Price";
	public static final String kBrandFeatureName = "Brand";
	public static final String kCategoryFeatureName = "Category";
	public static final String kProductFeaturesName = "features";
	public static final String kConditionFeature = "Condition";
	public static final String SEQUENCEFILE_DIR = "sequenceFiles";
	public static final String MERGE_SEQFILE_DIR = "merge-seqFiles";
	public static final String VECTORNAMESEPARATOR = ":";
	private static final String KNullValue = "null";
	private static String seqFileDirectory;						// Allows user to set alternate SEQUENCEFILE_DIR
	private static String mergeFileDirectory;						// Allows user to set alternate MERGE_SEQFILE_DIR
	
	// Vector Weights
	public static final double kXXLargeWeight = 150.0;
	public static final double kXLargeWeight = 10.0;
	public static final double kLargeWeight = 50.0;
	public static final double kMediumWeight = 2.0;
	public static final double kSmallWeight = 0.5;
	
	// ConcurentHashMap that stores ProductsResponse filenames as Keys and SequenceFile 
	// filenames as values. Note that this Map is updated concurrently by multiple Threads!
	//private static Map<String, String> sequenceFileDictionary = 
	//		new ConcurrentHashMap<String, String>();
	
	// Max Number of Threads
	private static final int kMAXNOTHREADS = 100;	
	
	/**
	 * 
	 * @param args two arguments, the first is the directory that contains the XML files 
	 * that each represent a ProductsResponse, the second argument is the Properties File
	 * whose Keys are the features that will be used in encoding each Product into a Vector
	 * 
	 * @throws RemixException - RemixException thrown if XML parsing fails for any of the XML file
	 * @throws IOException - Exception thrown if SequenceFile creation fails
	 */
	public static void main(String[] args) throws RemixException, IOException {
		
		// Check the validity of the arguments
		final File productsResponseDir;
		final String propertiesFile;
		if (args.length == 2) {
			productsResponseDir = new File(args[0]);
			assert productsResponseDir != null;
			propertiesFile = new String(args[1]);
			assert propertiesFile != null;
			PROPERTIESFILENAME = propertiesFile;
		} else if (args.length == 1){
			productsResponseDir = new File(args[0]);
			assert productsResponseDir != null;
			propertiesFile = PROPERTIESFILENAME;
			assert propertiesFile != null;
		} else {
			throw new IllegalArgumentException
			("Class MzSequenceFileGenerator.java takes TWO string arguments e.g /dir/productsDir features.txt" +
					"OR ONE string argument e.g /dir/productsDir");
		}

		// Check that the first argument is a valid directory
		if (!productsResponseDir.exists() || !productsResponseDir.isDirectory()) {
			throw new IllegalArgumentException("First argument must be a directory of XML files!");
		}
		// Log
		logger.log(Level.INFO, "Generating SequenceFiles for XML files in directory: " + productsResponseDir.toString());
		
		// Read in the Properties file
		MzSequenceFileGenerator.getProperties();
						
		// Iterate through the directory and create a SequenceFile for each ProductsResponse XML file
		FilenameFilter xmlFilter = new MzXMLFileFilter();
		List<File> xmlfiles = Arrays.asList(productsResponseDir.listFiles(xmlFilter));
		assert xmlfiles != null;
		
		// Get all the required Properties files
		MzAttributeMapper attributeMapper = new MzAttributeMapperImpl();
		assert attributeMapper != null;
		attributeMapper.setAttributesFile(null);	// Use the default ManziaAttributes file.
		MzSequenceFileGenerator.setAttributeMapper(attributeMapper);
		
		// Set the Feature Weights
		Properties featureWeights;
		featureWeights = MzWeightsUtil.getCurrentWeightsMap();
		if (featureWeights == null) {
			featureWeights = MzWeightsUtil.getInitialWeightsMap();
		}
		assert featureWeights != null;
		MzSequenceFileGenerator.setFeatureWeights(featureWeights);
		
		// Spawn a separate thread to process each XML file (if we have many XML files)
		//assert sequenceFileDictionary != null;
		boolean createdSeqFile = false;
		if (xmlfiles.isEmpty()) {
			logger.log(Level.WARNING, "No XML files were found in specified directory...no SequenceFiles generated");
			return;
		}
		if (xmlfiles.size() == 1) {
			createdSeqFile = generateSequenceFile(xmlfiles.get(0), MzSequenceFileGenerator.getProperties(), 
					MzSequenceFileGenerator.getAttributeMapper(), MzSequenceFileGenerator.getFeatureWeights());
			if (createdSeqFile) {
				logger.log(Level.INFO, "Successfully created SequenceFile for XML file: " + xmlfiles.get(0).getName());				
			}
		} else {
			
			// we have multiple XML file so we process them concurrently, we set the max number of threads
			// to 100
			int numThreads = xmlfiles.size() < kMAXNOTHREADS ? xmlfiles.size() : kMAXNOTHREADS;
			assert numThreads > 0;
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);
			Set<Future<Boolean>> set = new HashSet<Future<Boolean>>();
			logger.log(Level.INFO, "Number of threads scheduled is: " + Integer.toString(numThreads));
			logger.log(Level.INFO, "Number of XML files scheduled is: " + Integer.toString(xmlfiles.size()));
			
			for( File xmlFile : xmlfiles) {
				if (xmlFile.isFile() && !xmlFile.isHidden()) {
					Callable<Boolean> callable = new MzSingleGenerator(xmlFile);
					assert callable != null;
					Future<Boolean> future = executor.submit(callable);
					assert future != null;
					set.add(future);
				}				
			}
			
			// Get the results from the Future objects
			Boolean success;
			int count = 0;
			for( Future<Boolean> futureSeqFile : set) {
				//if (futureSeqFile.isDone()) {
					try {
						success = futureSeqFile.get();
						if (success.booleanValue()) {
							count++;
						}
					} catch (InterruptedException e) {
						logger.log(Level.WARNING, "Thread processing XML file was interrupted!");
						e.printStackTrace();
					} catch (ExecutionException e) {
						logger.log(Level.SEVERE, "Execution Exception while processing XML file");
						e.printStackTrace();
						throw new RuntimeException("Execution exception while getting Future object!" + e.getLocalizedMessage());
					}
				//}
			}
			logger.log(Level.INFO, "Number of SequenceFiles successfully created is: " + Integer.toString(count));
			
			// Shutdown thread executor
			executor.shutdown();
		}
		
		// Now merge the Sequence Files created by Category
		String seqDir = seqFileDirectory != null ? seqFileDirectory : MzSequenceFileGenerator.SEQUENCEFILE_DIR;
		assert seqDir != null;
		boolean success = mergeSequenceFilesByCategory(new File(seqDir));
		
		// Delete the created Sequence Files if the merge succeeded
		if (success) {
			logger.log(Level.INFO, "Merging Sequence Files succeeded!");
			boolean deleted = false;
			MzSeqFileFilter seqFilter = new MzSeqFileFilter();
			assert seqFilter != null;
			File sequenceFileDir = new File (seqDir);
			assert sequenceFileDir != null;
			List<File> sequenceFiles = Arrays.asList(sequenceFileDir.listFiles(seqFilter));
			assert sequenceFiles != null;
			int deletedFiles = sequenceFiles.size();
			
			// Iterate through the Sequence files and delete them
			int fileCount = 0;
			ListIterator<File> seqIterator = sequenceFiles.listIterator();
			assert seqIterator != null;
			while (seqIterator.hasNext()) {
				deleted = seqIterator.next().delete();
				if (deleted) { fileCount++; }
			}
			// Log
			logger.log(Level.INFO, "Deleted [{0}] files out of [{1}] total files", new Object[] { fileCount, deletedFiles });
		}
		
	}
	
	/**
	 * Helper method that reads the Properties file to be used during
	 * the encoding (feature-hashing) operation
	 * @param propsFilepath - File path
	 */
	private static Properties readPropertiesFile (String propsFilename ) {
		// check input
		if (propsFilename == null || propsFilename.isEmpty()) {
			logger.log(Level.SEVERE, "Invalid Properties Filename specified!");
			throw new IllegalArgumentException("Invalid Properties Filename was specified!");
		}
		
		// Result
		Properties vectorMap = new Properties();
				
		// read
		try {
			BufferedReader propertiesReader = 
					new BufferedReader(new FileReader(propsFilename));
			assert propertiesReader != null;
			vectorMap.load(propertiesReader);
			logger.log(Level.INFO, "Loaded Properties File..." + propsFilename);
			propertiesReader.close();
		} catch (FileNotFoundException fe) {
			logger.log(Level.SEVERE, "Input Properties File Not Found", fe);
			throw new RuntimeException("Input Properties File Not Found!" + fe.getLocalizedMessage());
		} catch (IOException ie) {
			logger.log(Level.SEVERE, "IO Exception loading Properties file", ie);
			throw new RuntimeException("IO Exception loading Properties file!" + ie.getLocalizedMessage());
		}
		
		return vectorMap;
	}
	
	/**
	 * Returns the Properties object that represents the Properties file
	 * whose Keys are all the features/variables whose values will be 
	 * feature-hashed to represent a Product as a Vector
	 * 
	 * @return
	 */
	public static Properties getProperties() {
		if (MzSequenceFileGenerator.vectorMap == null) {
			MzSequenceFileGenerator.vectorMap = MzSequenceFileGenerator.readPropertiesFile(PROPERTIESFILENAME);
			assert MzSequenceFileGenerator.vectorMap != null;
		}		
		return MzSequenceFileGenerator.vectorMap;
	}
	
	/**
	 * 
	 * @return - Returns name of PropertiesFile used to generate the SequenceFiles.
	 */
	public static String getPropertiesFileName() {
		return PROPERTIESFILENAME;
	}
	
	/**
	 * 	
	 * @return - Returns the MzAttributeMapper
	 */
	public static MzAttributeMapper getAttributeMapper() {
		return attributeMapper;
	}
	
	/**
	 * 
	 * @return - returns the FeatureWeights as a Properties object
	 */
	public static Properties getFeatureWeights() {
		return featureWeights;
	}
	
		
	public static void setAttributeMapper(MzAttributeMapper attributeMapper) {
		assert attributeMapper != null;
		MzSequenceFileGenerator.attributeMapper = attributeMapper;
	}

	public static void setFeatureWeights(Properties featureWeights) {
		assert featureWeights != null;
		MzSequenceFileGenerator.featureWeights = featureWeights;
	}

	/**
	 * 
	 * @return - List of all Categories for which SequenceFiles have been generated
	 */
	public static List<String> getCategoryList() {
		return CATEGORY_LIST;
	}
	
	public static void setSeqFileDirectory( String seqDirectory) {
		if (seqDirectory != null && seqDirectory.length() > 0) {
			seqFileDirectory = seqDirectory;
			logger.log(Level.INFO, "Setting SequenceFiles Directory to: {0}", new Object[]{seqDirectory});
		} else {
			logger.log(Level.WARNING, "Invalid directory name, SequencesFiles Directory was not set!");
		}
	}
	
	public static void setMergeFileDirectory( String mergeDirectory ) {
		if (mergeDirectory != null && mergeDirectory.length() > 0) {
			mergeFileDirectory = mergeDirectory;
			logger.log(Level.INFO, "Setting Merge Files Directory to: {0}", new Object[]{mergeDirectory});
		} else {
			logger.log(Level.WARNING, "Invalid directory name, Merge Directory was not set!");
		}
	}
	
	public static String getSeqFileDirectory() {
		String seqDirectory = seqFileDirectory != null ? seqFileDirectory : MzSequenceFileGenerator.SEQUENCEFILE_DIR;
		return seqDirectory;
	}
	
	public static String getMergeFileDirectory() {
		String mergeDirectory = mergeFileDirectory != null ? mergeFileDirectory : MzSequenceFileGenerator.MERGE_SEQFILE_DIR;
		return mergeDirectory;
	}
	
	/**
	 * Method that generates a NamedVector from a Product object <br>
	 * 
	 * @param product - the Product object from which the NamedVector is generated
	 * @param attributeMapper - the MzAttributeMapper object used to "generate" the Product's
	 * attribute values that will be encoded (feature-hashed) to create the NamedVector
	 * @param featureWeights - Map of weights that will be used in encoding the Product Vector features
	 * @param vectorAttrMap - Properties object that maps BestBuy attributes to Manzia attributes
	 * @param traceDictionary - traceDictionary used to encode the product Vector
	 * @return - the encoded NamedVector
	 * @throws IOException - thrown if TokenStream incrementToken() method fails
	 */
	public static NamedVector generateVectorFromProduct (Product product, MzAttributeMapper attributeMapper, 
			Properties featureWeights, Properties vectorAttrMap, Map<String, Set<Integer>> traceDictionary) throws IOException {
		
		// check Inputs
		assert product != null;
		assert attributeMapper != null;
		assert traceDictionary != null;
		if (vectorAttrMap == null || vectorAttrMap.isEmpty()) {
			logger.log(Level.WARNING, "Invalid Properties File, cannot map BestBuy attributes to Manzia attributes!");
			return null;
		}
		
		NamedVector productVector = null;	// Vector that encodes the the Product into a set of features
		Map<String, String> detailsMap;	// Map with the details.name as Keys and details.value as Values
		StringBuilder featuresBuilder;	// Builds a string of all the Product's feature values			
		Set<String> detailsSet;				// Set that holds the Keys of the detailsMap
		StringBuilder vectorName;
		//int nonZeroEntries = 0;
		//Map<String, String> relationsMap;	// Map with combined attributes as Keys and combined attributes values as Values

		/*
		 * Modifications: Nov 15, 2012
		 * 1- Limit the Products we process to those whose Brand is in the ManziaAttributes file
		 * 2- Ignore the BestBuy Product Features attributes/values and the Product Long Description
		 * attribute/value so that the resulting Vector is as "similar" in sparsity to the Search Vectors
		 * we shall be comparing these Product vectors against. 
		 */
		Set<String> validBrands = attributeMapper.allBrandAttributeValues();
		assert validBrands != null;
		// Skip all Product objects with "bad" values for the ModelNumber
		if (product.getModelNumber() == null || product.getModelNumber().equals(KNullValue)) 
			return null;

		// Skip all Product objects whose Brands are not "valid" i.e not in the Set of valid Brands
		boolean isValidBrand = false;
		if (validBrands.size() > 0) {
			for (String brand : validBrands) {
				if (product.getManufacturer().startsWith(brand)) {
					isValidBrand = true;
					break;
				}
			}
		} else {
			logger.log(Level.WARNING, "Set of all Brand Attribute values is EMPTY...invalid brands will NOT be skipped!");
			isValidBrand = true;
		}				

		if (!isValidBrand) return null;

		// Get the category Names associated with the product
		List<String> categoryNames = new ArrayList<String>();
		for (Category category : product.getCategoryPath()) {
			categoryNames.add(category.getName());
		}

		// Check if this product belongs to a valid category
		MzValidCategory category = containsListElement(categoryNames, CATEGORY_LIST);
		assert category != null;

		if (category.isValidCategory() && !product.getModelNumber().equals(KNullValue)) {

			// We can now create the Vector that will encode this Product and identify it by the Product
			// modelNumber
			// The naming convention for Vectors is category:modelNumber:brand e.g Laptop:2000-428dx:HP
			vectorName = new StringBuilder();	// Builds the Vector Name
			vectorName.append(category.getProductCategory())
			.append(VECTORNAMESEPARATOR)
			.append(product.getModelNumber())
			.append(VECTORNAMESEPARATOR)
			.append(product.getManufacturer());

			productVector = 
					new NamedVector( new SequentialAccessSparseVector(VECTOR_SIZE), vectorName.toString());
			assert productVector != null;

			// Assign the detailsMap and relationsMap
			detailsMap = product.getDetailsMap();
			assert detailsMap != null;
			
			// Add the Condition feature to the detailsMap
			if (product.isNew()) {
				detailsMap.put(kConditionFeature, "New");
			} else {
				detailsMap.put(kConditionFeature, "Used");
			}
			//relationsMap = Collections.synchronizedMap(new HashMap<String, String>());
			//assert relationsMap != null;

			// Iterate over detailsMap and encode each entry
			detailsSet = detailsMap.keySet();
			assert detailsSet != null;
			Iterator<String> detailsIterator = detailsSet.iterator();
			assert detailsIterator != null;
			while (detailsIterator.hasNext()) {

				// remove the whitespace from each Key 
				String detailsKey = new String(detailsIterator.next());
				assert detailsKey != null;
				String detailsValue = detailsMap.get(detailsKey);
				assert detailsValue != null;
				//String replacedKey = detailsKey.replaceAll("\\s", "");
				//assert replacedKey.isEmpty() == false;
				String featureName = vectorAttrMap.getProperty(detailsKey);
				//System.out.printf("BB Attribute: %s\n", replacedKey);

				// Encode only those details.name elements in our vectorMap. Also, map the retailer-specific
				// detailsValue to one of Manzia attribute values so we are comparing apples to apples as much
				// as possible.
				if (featureName != null && featureName.length() > 0) {
					String newDetailValue = 
							attributeMapper.mappedValueForAttribute(category.getProductCategory(), featureName, detailsValue);
					// Testing Only
					//System.out.printf("Feature Name: %s\t\t Mapped BB Attribute: %s\n", featureName, replacedKey);
					//System.out.printf("New attributeValue: %s\t\t Old attributeValue: %s\n", newDetailValue, detailsValue);
					detailsValue = newDetailValue != null ? newDetailValue : detailsValue;
					//relationsMap.put(featureName, detailsValue);
					FeatureVectorEncoder encoder = new StaticWordValueEncoder(detailsKey);
					encoder.setProbes(2);
					encoder.setTraceDictionary(traceDictionary);
					
					// Encode and set the weights
					if (featureWeights != null && featureWeights.size() > 0) {
						String weightFeature = featureWeights.getProperty(featureName);						
						if (weightFeature != null && !weightFeature.isEmpty()) {
							encoder.addToVector(detailsValue, Double.valueOf(weightFeature).doubleValue(), productVector);
						} else {
							encoder.addToVector(detailsValue, kMediumWeight, productVector);
						}
					} else {
						encoder.addToVector(detailsValue, kMediumWeight, productVector);
					}					
				}						
			}

			// We can now encode the price, brand, category and Product's "features" property
			// NOTE: we may eventually have to set some weights to these features so they have a 
			// bigger impact on the distance measure

			//Price
			float productPrice = product.getSalePrice() > 0.0f ? product.getSalePrice() : product.getRegularPrice();
			assert productPrice != Float.NaN;
			assert productPrice > 0;
			float medianPrice = MzFeatureUtils.getPriceRangeMedian(productPrice);
			assert medianPrice > 0;
			String productPriceString = Float.toString(medianPrice);
			//assert productPriceString != null;

			// If the Product has no Price, we do not encode it!!
			if (productPriceString != null && productPriceString.length() > 0) {

				// Set the Key attributes for generating the relationship features
				//relationsMap.put(kPriceFeatureName, productPriceString);
				//relationsMap.put(kBrandFeatureName, product.getManufacturer());

				// Product Features
				featuresBuilder = new StringBuilder();
				// Ignore the Product's "features" property and only use the LongDescription
				/*for (String productFeature : product.getFeatures()) {
							featuresBuilder.append(productFeature);							
						}*/
				featuresBuilder.append(product.getLongDescription());
				TokenStream filteredFeatures = filterProductFeatures(featuresBuilder);
				assert filteredFeatures != null;

				// Encode
				FeatureVectorEncoder priceEncoder = new ContinuousValueEncoder(kPriceFeatureName);
				priceEncoder.setTraceDictionary(traceDictionary);
				priceEncoder.setProbes(2);
				
				FeatureVectorEncoder brandEncoder = new StaticWordValueEncoder(kBrandFeatureName);
				brandEncoder.setTraceDictionary(traceDictionary);
				brandEncoder.setProbes(2);
				assert product.getManufacturer() != null;
				
				FeatureVectorEncoder categoryEncoder = new StaticWordValueEncoder(kCategoryFeatureName);
				categoryEncoder.setTraceDictionary(traceDictionary);
				categoryEncoder.setProbes(2);
				assert category.getProductCategory() != null;
				
				// Iterate over all the Tokens from the Product Features attribute and Description
				Set<String> uniqueToken = new HashSet<String>();
				CharTermAttribute termAttribute = 
						(CharTermAttribute)filteredFeatures.addAttribute(CharTermAttribute.class);
				FeatureVectorEncoder featureEncoder = new StaticWordValueEncoder(kProductFeaturesName);
				featureEncoder.setTraceDictionary(traceDictionary);
				featureEncoder.setProbes(2);

				while (filteredFeatures.incrementToken()) {
					String word = new String(termAttribute.buffer(), 0, termAttribute.length());
					if ( word != null && word.length() > 0 ) {
						uniqueToken.add(word);
					}														
				}
				Iterator<String> uniqueIterator = uniqueToken.iterator();
				assert uniqueIterator != null;
				while (uniqueIterator.hasNext()) {
					featureEncoder.addToVector(uniqueIterator.next(), kSmallWeight, productVector);
				}

				// Encode the combination of Brand & Price
				String brandPriceFeature = kBrandFeatureName + kPriceFeatureName;
				String brandPriceValue = product.getManufacturer() + productPriceString;
				FeatureVectorEncoder brandPriceEncoder = new StaticWordValueEncoder(brandPriceFeature);
				brandPriceEncoder.setTraceDictionary(traceDictionary);
				brandPriceEncoder.setProbes(2);
				
				
				// Encode and set the weights
				if (featureWeights != null && featureWeights.size() > 0) {
					String priceWeight = featureWeights.getProperty(kPriceFeatureName);
					if (priceWeight != null && !priceWeight.isEmpty()) {
						priceEncoder.addToVector(productPriceString, Double.valueOf(priceWeight).doubleValue() ,productVector);
					} else {
						priceEncoder.addToVector(productPriceString, kLargeWeight ,productVector);
					}
					String brandWeight = featureWeights.getProperty(kBrandFeatureName);
					if (brandWeight != null && !brandWeight.isEmpty()) {
						brandEncoder.addToVector(product.getManufacturer(), Double.valueOf(brandWeight).doubleValue() ,productVector);
					} else {
						brandEncoder.addToVector(product.getManufacturer(), kLargeWeight ,productVector);
					}
					String categoryWeight = featureWeights.getProperty(kCategoryFeatureName);
					if (categoryWeight != null && !categoryWeight.isEmpty()) {
						categoryEncoder.addToVector(category.getProductCategory(), Double.valueOf(categoryWeight).doubleValue(), productVector);
					} else {
						categoryEncoder.addToVector(category.getProductCategory(), kMediumWeight, productVector);
					}
					String brandPriceWeight = featureWeights.getProperty(brandPriceFeature);
					if (brandPriceWeight != null && !brandPriceWeight.isEmpty()) {
						brandPriceEncoder.addToVector(brandPriceValue, Double.valueOf(brandPriceWeight).doubleValue(), productVector);
					} else {
						brandPriceEncoder.addToVector(brandPriceValue, kMediumWeight, productVector);
					}
				} else {
					priceEncoder.addToVector(productPriceString, kLargeWeight ,productVector);
					brandEncoder.addToVector(product.getManufacturer(), kLargeWeight ,productVector);
					categoryEncoder.addToVector(category.getProductCategory(), kMediumWeight, productVector);
					brandPriceEncoder.addToVector(brandPriceValue, kMediumWeight, productVector);
				}			

				/* Generate and encode the relation Features
						Map<String, String> relatedFeatures = MzFeatureUtils.getRelationFeatures(relationsMap);
						assert relatedFeatures != null;
						if (!relatedFeatures.isEmpty()) {
							Set<String> relationSet = relatedFeatures.keySet();
							Iterator<String> relationIterator = relationSet.iterator();
							assert relationIterator != null;
							while (relationIterator.hasNext()) {
								String key = relationIterator.next();
								if (key.startsWith(kBrandFeatureName) && key.endsWith(kPriceFeatureName)) {
									FeatureVectorEncoder tripleEncoder = new StaticWordValueEncoder(key);
									tripleEncoder.setTraceDictionary(traceDictionary);
									tripleEncoder.setProbes(2);
									tripleEncoder.addToVector(relatedFeatures.get(key), kXXLargeWeight, productVector);
								} else if (key.startsWith(kBrandFeatureName)) {
									FeatureVectorEncoder doubleEncoder = new StaticWordValueEncoder(key);
									doubleEncoder.setTraceDictionary(traceDictionary);
									doubleEncoder.setProbes(2);
									doubleEncoder.addToVector(relatedFeatures.get(key), kXLargeWeight, productVector);
								}
							}
						} */						

				// Add the vector to the list
				//nonZeroEntries = productVector.getNumNondefaultElements();
			}							
		}
		// clear the StringBuilder for the Product's features and Vector Names
		//featuresBuilder = null;
		//vectorName = null;
		
		return productVector;					
		
	}
	
	/**
	 * Method that generates a List of NamedVector objects given a ProductsResponse object. A NamedVector
	 * is generated for each Product object contained in the ProductsResponse. <br>
	 * 
	 * @param response - ProductsResponse object for which to generate NamedVector objects
	 * @param vectorAttrMap - Properties file that maps BestBuy attributes to Manzia attributes
	 * @param attributeMapper - the MzAttributeMapper object used to "generate" the Product's
	 * attribute values that will be encoded (feature-hashed) to create the NamedVector
	 * @param featureWeights - Map of weights that will be used in encoding the Product Vector features
	 * @return - List of NamedVector objects
	 * @throws IOException - thrown by the TokenStream incrementToken() method
	 */
	public static List<NamedVector> generateVectorsFromProductsResponse (ProductsResponse productsXML, Properties vectorAttrMap, 
			MzAttributeMapper attributeMapper, Properties featureWeights) throws IOException {
		
		//ÊResult
		List<NamedVector> productVectorList = Collections.synchronizedList( new ArrayList<NamedVector>());	// List of the created Product Vectors
		
		// Check inputs
		if (vectorAttrMap == null || vectorAttrMap.isEmpty()) {
			logger.log(Level.WARNING, "Invalid Properties File, cannot map BestBuy attributes to Manzia attributes!");
			return productVectorList;
		}
		
		// Vectorize each Product in the ProductsResponse
		assert productsXML != null;
		List<Product> productList = productsXML.list();
		assert productList != null;		
		//int maxSparsity = 0;			// Variable to track sparsity of generated SequenceFiles
		if (productList.size() > 0) {
	
			/*
			 * Iterate through each Product in the productList and do the following
			 * 1- determine the product Category
			 * 2- Create a Mahout Vector if the product Category is one of TVs, Laptops, Tablets, Printers, Phones
			 * (the vector name is the ModelNumber of the Product)
			 * 3- Iterate through the details.name strings and compare to the features in the featuresList
			 * (we have to strip the whitespace from the details.name strings) 
			 * 4- If there is a match in 3 above, 
			 * 		- create a FeatureVectorEncoder using the matching feature
			 * 		- encode the details.value corresponding to the details.name to the named Vector
			 * 5- Encode string values for condition, price, category features to the named Vector with
			 * a "biased" weighting
			 * 6- Encode the long description element of the Product to the named Vector as a text variable 
			 * 7- Add the encoded Vector to a list of Vectors
			 */
			NamedVector productVector;
			Map<String, Set<Integer>> traceDictionary = new TreeMap<String, Set<Integer>>();
			
			//int nonZeroEntries = 0;
			//Map<String, String> relationsMap;	// Map with combined attributes as Keys and combined attributes values as Values
	
			/*
			 * Modifications: Nov 15, 2012
			 * 1- Limit the Products we process to those whose Brand is in the ManziaAttributes file
			 * 2- Ignore the BestBuy Product Features attributes/values and the Product Long Description
			 * attribute/value so that the resulting Vector is as "similar" in sparsity to the Search Vectors
			 * we shall be comparing these Product vectors against. 
			 */
				
			for (Product product : productList) {
				productVector = MzSequenceFileGenerator.generateVectorFromProduct(product, attributeMapper, featureWeights, vectorAttrMap, traceDictionary);
				if (productVector != null && productVector.getNumNondefaultElements() > 0) {
					productVectorList.add(productVector);
				}
			}			
		}		

		return productVectorList;
	}
	
	/**
	 * Method that generates a SequenceFile given an XML file representing a ProductsResponse
	 * specific to the BestBuy API
	 * 
	 * @param productsResponseFile - XML file representing a ProductsResponse object
	 * @param vectorAttrMap - Properties file that maps BestBuy attributes to Manzia attributes
	 * 
	 * @return - True if SequenceFile was successfully created and False otherwise
	 * @throws RemixException - RemixException thrown if XML parsing fails
	 * @throws IOException - IOException thrown if SequenceFile creation fails, i.e when the creation of the 
	 * temp File whose filename is used for the SequenceFile's name fails
	 * 
	 */
	public static boolean generateSequenceFile( File productsResponseFile, Properties vectorAttrMap, 
			MzAttributeMapper attributeMapper, Properties featureWeights) throws RemixException, IOException {
		
		// Check validity of the input Files
		if (productsResponseFile == null ) {
			throw new IllegalArgumentException("ProductResponse File is NULL!");
		}
		
		if (!productsResponseFile.exists() || !productsResponseFile.isFile()) {
			throw new IllegalArgumentException("Cannot find specified ProductsResponse file OR specified File is not a valid file!");
		}
		
		// Check that we have a valid product XML file
		if (!productsResponseFile.toString().endsWith("xml")) {
			throw new IllegalArgumentException("First argument must be xml file e.g products011.xml");
		}
		
		// Check inputs
		if (vectorAttrMap == null || vectorAttrMap.isEmpty()) {
			logger.log(Level.WARNING, "Invalid Properties File, cannot map BestBuy attributes to Manzia attributes!");
			return false;
		}

		// Read in the ProductsResponse
		ProductsResponse productsXML = 
				new ProductsResponse( productsResponseFile);
		assert productsXML != null;
		logger.log(Level.INFO, "Success parsing the ProductsResponse XML file!");
		
		// Create the List of NamedVectors
		List<NamedVector> namedVectorList = 
				MzSequenceFileGenerator.generateVectorsFromProductsResponse(productsXML, vectorAttrMap, attributeMapper, featureWeights);
		
		// Write the Vectors to a SequenceFile (Apache Hadoop)
		String seqPath = seqFileDirectory != null ? seqFileDirectory : MzSequenceFileGenerator.SEQUENCEFILE_DIR;
		assert seqPath != null;
		File sequencePath;
		if (seqPath.equals(MzSequenceFileGenerator.SEQUENCEFILE_DIR)) {
			sequencePath = new File( System.getProperty("user.dir"), seqPath);
			assert sequencePath != null;
		} else {
			sequencePath = new File( seqPath);
			assert sequencePath != null;
		}		
		
		// Create the Output Directory if it doesnot already exist
		if (!sequencePath.exists()) {
			sequencePath.mkdirs();
		}
		
		// Generate a unique filename
		// Note that the temp file created below will be overwritten by the SequenceFile.Writer
		File newSequencePath = File.createTempFile("seqFile", "seq", sequencePath);
		assert newSequencePath != null;
		
		boolean success = false;
		if (!namedVectorList.isEmpty()) {
			try {
				success = writeVectorToSequenceFile(namedVectorList, newSequencePath.getAbsolutePath());
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception while creating SequenceFile: " + e.getLocalizedMessage());
				e.printStackTrace();
				throw new RuntimeException("Exception while writing Vector to SequenceFile!" + e.getLocalizedMessage());
			}
			/*if (success) {
				String sequenceFileName = newSequencePath.getName();
				assert sequenceFileName != null;
				sequenceFileDictionary.put(productsResponseFile.getName(), sequenceFileName);
				//logger.log(Level.INFO, "Success creating SequenceFile with name: " + sequenceFileName);
			}*/
		} else {
			logger.log(Level.INFO, "Zero NamedVectors were created from ProductsResponse object!");
		}		
		
		return success;
	}
	
	/**
	 * Method creates a SequenceFile from a list of Vectors at a given filePath
	 * 
	 * @param productVectors - list of Vectors to store in SequenceFile
	 * @param filePath - filePath to store the created SequenceFile
	 * @return - true if success else false
	 */
	protected static boolean writeVectorToSequenceFile(List<NamedVector> productVectors, String filePath) 
			throws Exception {
		
		boolean didCreateSequenceFile = false;
		
		// check input List of Vectors
		if (productVectors == null || productVectors.isEmpty()) {
			logger.log(Level.WARNING, "List of Product Vectors is invalid...will abort SequenceFile creation");
			return false;
		}
		
		// Check the input filePath
		if (filePath == null || filePath.isEmpty()) {
			logger.log(Level.WARNING, "File Path is invalid...will abort SequenceFile creation");
			return false;
		}
		
		// Create the SequenceFile
		Configuration conf = new Configuration();
		FileSystem fs;
		try {
			fs = FileSystem.get(conf);
			Path path = new Path(filePath);
			SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, 
					path, Text.class, VectorWritable.class);
			assert writer != null;
			VectorWritable vec = new VectorWritable();
			
			// Serializes the vector data
			for (NamedVector vector : productVectors) {
				//assert vector.size() == MzSequenceFileGenerator.VECTOR_SIZE;
				vec.set(vector);
				writer.append(new Text(vector.getName()), vec);
			}
			File validSeqFile = new File (filePath);	// validate that SequenceFile was created
			assert validSeqFile != null;
			
			if (validSeqFile.exists() && validSeqFile.isFile()) {
				didCreateSequenceFile = true;
				logger.log(Level.INFO, "Success creating SequenceFile at Path: " + filePath);
			}			
			writer.close();			
						
		} catch (IOException e) {
			didCreateSequenceFile = false;
			logger.log(Level.SEVERE, "IO Exception while creating Sequence File", e);
			throw new RuntimeException("IO Exception while creating Sequence File" + e.getLocalizedMessage());
		}
		
		return didCreateSequenceFile;		
	}
	
	/**
	 * Compares each string in the secondList to the each string in the firstList
	 * and returns a MzValidCategory object whose boolean "validCategory" property indicates
	 * if any two compared strings were equal and productCategory property which holds the
	 * value of the equal string
	 * 
	 * @param firstList list of strings
	 * @param secondList list of strings
	 * @return returns MzValidCategory if any string in the secondList is equal to or contained "within"
	 * any of the strings in the firstList or null category if either input List is empty
	 */
	public static MzValidCategory containsListElement(List<String> firstList, List<String> secondList)
	{
		
		
		// This is a quadratic method but the List sizes are in single digits so performance
		// shouldn't be an issue
		MzValidCategory category = new MzValidCategory(false, null);
		//CharSequence categorySequence;
		
		// Check inputs
		if (firstList.isEmpty() || secondList.isEmpty()) {
			return category;
		}
		
		// Create a specific List to deal with the Laptop category
		String categoryLaptop = "Laptops";
		List<String> laptopCategory = 
				Arrays.asList("All Laptops", "PC Laptops", "MacBooks", "Chromebooks", "Ultrabooks", "Netbooks", "Refurbished Laptops");
		
		// Ignore Accessories categories
		final String accessoriesStr = "Accessories";
		CharSequence accessoriesSequence = accessoriesStr.subSequence(0, accessoriesStr.length()-1);
		assert accessoriesSequence != null;
		for (String firstString : firstList) {
			if (firstString.contains(accessoriesSequence)) {
				return category;
			}
		}
		
		// Iterate
		for (String firstString : firstList) {
			if (laptopCategory.contains(firstString)) {
				category.setValidCategory(true);
				category.setProductCategory(categoryLaptop);
				return category;
			}
			//Ignore the Strings with "Computer & Tablets" as this is the generic high-level
			// category used by Best Buy that encompasses the other categories. Otherwise we may end
			// up classifying Printers as Tablets etc.
			//if (!firstString.contains(computersSequence)) {
				for (String secondString : secondList) {
					if (firstString.equals(secondString)) {
						category.setValidCategory(true);
						category.setProductCategory(secondString);
						return category;
					}
					/*categorySequence = secondString.subSequence(0, secondString.length()-1);
					if (firstString.contains(categorySequence)) {
						category.setValidCategory(true);
						category.setProductCategory(secondString);
						return category;
					}*/
				}
			//}			
		}		
		return category;		
	}
	
	/**
	 * Method that uses Apache Lucene Filters to "clean up" the text comprising
	 * the Product's features. We remove stop words etc and return a filtered String
	 * 
	 * @param productFeatures StringBuilder object made up of all the Product's features
	 * @return returns a filtered String representation of the Product's features
	 */
	private static TokenStream filterProductFeatures(StringBuilder productFeatures)
	{
		Reader featuresReader = new StringReader(productFeatures.toString());
		assert featuresReader != null;
		
		// Start by stripping out any HTML markup
		CharStream charStream = CharReader.get(featuresReader);
		featuresReader = new HTMLStripCharFilter(charStream);
		TokenStream result = new StandardTokenizer(Version.LUCENE_36, featuresReader);
		assert result != null;
		
		// Use Lucene Filters to improve document vector generation and remove noise
		result = new StandardFilter(Version.LUCENE_36, result);
		result = new LowerCaseFilter(Version.LUCENE_36, result);
		result = new StopFilter(Version.LUCENE_36, result, StandardAnalyzer.STOP_WORDS_SET);
		//result = new LengthFilter(false, result, 3, 25);
		
		CharTermAttribute termAtt =
				(CharTermAttribute) result.addAttribute(CharTermAttribute.class);
		StringBuilder buf = new StringBuilder();
		try {
			while (result.incrementToken()) {
								
				String word = new String(termAtt.buffer(), 0, termAtt.length());
				buf.append(word).append(" ");				
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO Exception while filtering Product Features");
			throw new RuntimeException("Exception while filtering product features:" + e.getLocalizedMessage());
		}
		//return buf.toString();
		return new WhitespaceTokenizer(Version.LUCENE_36, new StringReader(buf.toString()));
	}
	
	/**
	 * After creating all the SequenceFiles, this methods iterates through the SequenceFiles directory
	 * reads each Vector in each SequenceFile and writes it to a new SequenceFile that stores Vectors
	 * of a specific category i.e all Vectors of the same category are stored in the same SequenceFile.
	 * This means that when we compare Search Vectors against Vectors in a SequenceFile we only need to
	 * compare Vectors of the same category, i.e apples to apples comparison.
	 * 
	 * NOTE: After the merging of the Sequence Files in the MERGE_SEQFILE_DIR directory, all the original
	 * sequence files are deleted from the SEQUENCEFILE_DIR. This is necessary so that if the MzSequenceFileGenerator
	 * is run again, the mergeSequenceFilesByCategory(...) method will not store duplicate Vectors i.e, Vectors encoding
	 * the same Products will be re-encoded to different Sequence Files on the next run so we'll end up with duplicate 
	 * Vectors in the merged Sequence Files every time we run the MzSequenceFileGenerator.
	 * 
	 * @param sequenceFileDir -directory of SequenceFiles each with prefix seqFile
	 * @return - true if merging was successful else false
	 * @throws IOException - Throws IOException if FileSystem object is not successfully instantiated
	 */
	public static boolean mergeSequenceFilesByCategory( File sequenceFileDir ) throws IOException
	{
		// Return value
		boolean success = false;
		MzSeqFileFilter seqFilter = new MzSeqFileFilter();
		assert seqFilter != null;
		
		// Check validity of input
		assert sequenceFileDir != null;
		
		if (sequenceFileDir.isDirectory()) {
			
			
									
			// Create the directory of Merged Sequence Files if it does not already exist
			String mergePath = mergeFileDirectory != null ? mergeFileDirectory : MzSequenceFileGenerator.MERGE_SEQFILE_DIR;
			assert mergePath != null;
			File mergeSeqPath;
			if (mergePath.equals(MzSequenceFileGenerator.MERGE_SEQFILE_DIR)) {
				mergeSeqPath = new File( System.getProperty("user.dir"), mergePath);
				assert mergeSeqPath != null;
			} else {
				mergeSeqPath = new File (MzSequenceFileGenerator.getMergeFileDirectory());
				assert mergeSeqPath != null;
			}
			
			
			// Create the Merge Directory if it doesnot already exist
			File[] categoryFiles;
			if (!mergeSeqPath.exists()) {
				mergeSeqPath.mkdirs();
			} else {
				
				// Check if we have an existing category-specific SequenceFiles and move them to directory of
				// SequenceFiles to be merged
				categoryFiles = mergeSeqPath.listFiles(seqFilter);
				assert categoryFiles != null;
				if (categoryFiles.length > 0) {
					logger.log(Level.INFO, "Re-merging {0} category-specific SequenceFiles", new Object[]{categoryFiles.length});
					for (int i=0; i<categoryFiles.length; i++) {
						MzSequenceFileGenerator.sequenceFileMove(categoryFiles[i], sequenceFileDir);
						
					}
					categoryFiles = mergeSeqPath.listFiles(seqFilter);
					if (categoryFiles.length == 0) {
						logger.log(Level.INFO, "Re-merged the category-specific SequenceFiles successfully!");
					}
					
				}
			}
			// Create a List of all the Sequence Files to be merged
			List<File> sequenceFiles = Arrays.asList(sequenceFileDir.listFiles(seqFilter));
			assert sequenceFiles != null;
			
			// Iterate through the Sequence files reading each
			Configuration conf = new Configuration();
			FileSystem fs;
			fs = FileSystem.get(conf);
			ListIterator<File> seqIterator = sequenceFiles.listIterator();
			assert seqIterator != null;
			
			// Create a Map of Sequence File Writers, one for each Category in CATEGORY_LIST
			StringBuilder mergeSeqFilename;
			Map<String, SequenceFile.Writer> sequenceWriters = 
					new ConcurrentHashMap<String, SequenceFile.Writer>();
			assert sequenceWriters != null;
			for (String category : CATEGORY_LIST) {
				mergeSeqFilename = new StringBuilder();
				mergeSeqFilename.append("seqFile").append("-").append(category);
				SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, 
						new Path(mergeSeqPath.getAbsolutePath(), mergeSeqFilename.toString()), Text.class, VectorWritable.class);
				assert writer != null;
				sequenceWriters.put(category, writer);
				mergeSeqFilename = null;	// clear for next run
			}
			
			// Iterate
			while (seqIterator.hasNext()) {
				try {
					
					Path seqFilePath = new Path( seqIterator.next().getAbsolutePath());
					assert seqFilePath != null;
					SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFilePath, conf);
					assert reader != null;
					Text key = new Text();
					VectorWritable value = new VectorWritable();
					
					// Deserializes the vector data
					while (reader.next(key, value)) {
						String vectorCategory = key.toString().split(VECTORNAMESEPARATOR)[0]; // category:modelNumber:brand format
						assert vectorCategory != null;
						
						// Depending on category of the Vector we write it to a new Category-Specific Sequence File
						sequenceWriters.get(vectorCategory).append(key, value);						
					}
					logger.log(Level.INFO, "Finished merging Sequence File at Path: {0}",new Object[]{seqFilePath.getName()});
					reader.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "IO Exception while reading Sequence File: " + e.getLocalizedMessage());
					e.printStackTrace();
					throw new RuntimeException("IO Exception reading Sequence File..." + e.getLocalizedMessage());
				}
			}
			
			// close the SequenceFile writers
			Set<String> writerKeySet = sequenceWriters.keySet();
			assert writerKeySet != null;
			Iterator<String> writerIterator = writerKeySet.iterator();
			assert writerIterator != null;
			while (writerIterator.hasNext()) {
				sequenceWriters.get(writerIterator.next()).close();
			}
			
			// At this point we assume all went well
			success = true;
			
		} else {
			//Log
			logger.log(Level.WARNING, "Not a valid Sequence File Directory..will NOT merge Sequence Files by Category! " +
					sequenceFileDir.toString());
			return false;
		}
		
		return success;		
	}
	
	/**
	 * Helper utility method to move a SequenceFile from one location to a Directory on the local fileSystem.
	 * 
	 * 
	 * @param source - source file
	 * @param destDir - destination Directory
	 * @throws IOException - thrown if error reading/writing the source/destination files
	 */
	public static void sequenceFileMove(File source, File destDir) throws IOException {
		//check inputs
		assert source != null;
		assert destDir != null;
		
		FileSystem fs;
		if (source.exists() && source.isFile()) {
			Configuration conf = new Configuration();
			fs = FileSystem.getLocal(conf);
			fs.copyFromLocalFile(true, true, new Path( source.getAbsolutePath()), new Path( destDir.getAbsolutePath()));
			if (destDir.exists() && destDir.isDirectory()) {
				logger.log(Level.INFO, "MOved file: {0} to Directory: {1}", 
						new Object[]{source.getAbsolutePath(), destDir.getAbsolutePath()});
			} else {
				// we have an invalid source file
				logger.log(Level.WARNING, "Invalid source file...cannot copy file: {0}", new Object[]{source.getAbsolutePath()});
			}
		}
	}
	
	/**
	 * Inner class used to filter out non-XML files from a specified directory
	 * @author Roy Manzi Tumubweinee, Oct. 1, 2012, Manzia Corporation
	 *
	 */
	public static class MzXMLFileFilter implements FilenameFilter {

		@Override
		public boolean accept(File dirName, String fileName) {
			if (fileName.endsWith(".xml") && new File( dirName, fileName).length() > 0) {
				return true;
			} else {
				return false;
			}			
		}
		
	}
	
	/**
	 * Inner class used to filter out non-SequenceFiles from specified directory
	 * @author Roy Manzi Tumubweinee, Oct 4, 2012, Manzia Corporation
	 *
	 */
	public static class MzSeqFileFilter implements FilenameFilter {

		@Override
		public boolean accept(File seqDir, String seqFile) {
			if (seqFile.startsWith("seqFile") && new File( seqDir, seqFile).length() > 0) {
				return true;
			} else {
			return false;
			}
		}
	}
	
	/**
	 * Inner helper class used to concurrently process XML files and
	 * generate a SequenceFile for each XML file
	 * 
	 * @author Roy Manzi Tumubweinee, Oct 1, 2012, Manzia Corporation
	 *
	 */
	private static class MzSingleGenerator implements Callable<Boolean> {
		private File productsFile;
		
		//Constructor
		public MzSingleGenerator( File productFile) {
			this.productsFile = productFile;
		}

		@Override
		public Boolean call() throws Exception {
			boolean success = false;
			if (productsFile.exists()) {
				success = generateSequenceFile(productsFile, MzSequenceFileGenerator.getProperties(), 
						MzSequenceFileGenerator.getAttributeMapper(), MzSequenceFileGenerator.getFeatureWeights());				
			} else {
				throw new FileNotFoundException("File not found is: " + productsFile.toString());
			}
			return Boolean.valueOf(success);			
		}		
	}

}
