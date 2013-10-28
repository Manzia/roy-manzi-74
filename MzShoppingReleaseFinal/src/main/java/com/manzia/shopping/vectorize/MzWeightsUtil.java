package com.manzia.shopping.vectorize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that contains methods that adminster the Properties files
 * that contain the Feature Weights. Operations include, retrieval,
 * creation and deletion.
 * 
 * @author Roy Manzi Tumubweinee, Nov 23, 2012, Manzia Corporation
 *
 */
public final class MzWeightsUtil {
	
	// Initial Weights File name
	private static final String kInitialWeightsMapName = "InitialAttributeWeights.txt";
	private static final String kCurrentWeightsMapName = "CurrentAttributeWeights";
	public static final String kCurrentCategoryWeightsName = "CurrentCategoryWeights";
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzWeightsUtil.class.getCanonicalName());
	
	// Properties 
	private static Properties initialWeightsMap;	// contains initial Feature Weights
	private static Properties currentWeightsMap;	// contains "best" Feature Weights
	
	/**
	 * Getter for the InitialAttributeWeights
	 * 
	 * @return - Properties object containing the InitialAttributeWeights
	 */
	public static Properties getInitialWeightsMap() {
		
		// Disk I/O only if we do not already have the weightsMap
		if (initialWeightsMap == null) {
			File initialWeightsFile = new File( System.getProperty("user.dir"), kInitialWeightsMapName);
			if (initialWeightsFile != null && initialWeightsFile.isFile()) {
				initialWeightsMap = MzWeightsUtil.readWeightsMapFromFile(initialWeightsFile);
			} else {
				logger.log(Level.SEVERE, "InitialAttributeWeights file does not exist!");
				throw new RuntimeException("InitialAttributeWeights file does not exist!");
			}
		}
		return initialWeightsMap;		
	}
	
	/**
	 * Getter for the CurrentAttributeWeights
	 * 
	 * @return - Properties object containing the CurrentAttributeWeights
	 */
	public static Properties getCurrentWeightsMap() {
		
		// Disk I/O only if we do not already have the weightsMap
		if (currentWeightsMap == null) {
			File currentWeightsFile = new File( System.getProperty( "user.dir"), kCurrentWeightsMapName);
			if (currentWeightsFile != null & currentWeightsFile.isFile()) {
				currentWeightsMap = MzWeightsUtil.readWeightsMapFromFile(currentWeightsFile);
			} else {
				// Just return null since its OK for the CurrentAttributeWeights file not to exist.
				logger.log(Level.WARNING, "CurrentAttributeWeights file does not exist!");
				return null;
			}
		}		
		return currentWeightsMap;		
	}
	
	/**
	 * Method prints a given java.util.Map to System.out. If maxNumEntries = 0, prints the
	 * entire Map. If maxNumEntries <= 0 or greater than the Map size, it prints
	 * the entire Map.
	 * @param printedMap - Map to print to System.out
	 * @param maxNumEntries - Number of Map entries to print
	 */
	public static void printMap (Map<?, ?> printedMap, int maxNumEntries) {
		
		// check inputs
		if( maxNumEntries <= 0 || maxNumEntries > printedMap.size()) {
			maxNumEntries = 0;
		}
		
		if (printedMap == null || printedMap.isEmpty()) {
			logger.log(Level.INFO, "Cannot print Map...Map is NULL or EMPTY!");
			return;
		}
		// Print
		if (maxNumEntries == 0) {
			// Print the entire Map
			for (Entry<?, ?> e : printedMap.entrySet())
		    {
		        System.out.println("Key: " + e.getKey() + ", Value: " + e.getValue());
		    }
		} else {
			// Print the specified number of entries
			int numEntries = 0;
			System.out.println();
			for (Entry<?, ?> e : printedMap.entrySet())
		    {
		        if (numEntries >= maxNumEntries) break;
		        System.out.println("Key: " + e.getKey() + ", Value: " + e.getValue());
		        numEntries++;		        
		    }
		}		
	}
	
	/**
	 * Method saves the Properties object to the provided File path
	 * 
	 * @param weightMap - Properties object to be saved
	 * @param weightsMapPath - File path where to save
	 */
	public static void saveWeightsMapToFile( Properties weightMap, File weightsMapPath ) {
		
		// check inputs
		if (weightsMapPath == null || weightsMapPath.isDirectory()) {
			logger.log(Level.SEVERE, "Invalid Properties file specified!");
			throw new IllegalArgumentException("Cannot save..Invalid Properties file specified!");
		}
		
		if (weightMap == null || weightMap.isEmpty()) {
			logger.log(Level.WARNING, "Cannot save...Properties file is null or empty!");
			return;
		}
		
		// Save the Properties file
		try {
			BufferedWriter propertiesWriter = 
					new BufferedWriter(new FileWriter(weightsMapPath));
			assert propertiesWriter != null;
			weightMap.store(propertiesWriter, null);
			logger.log(Level.INFO, "Saved WeightsMap File..." + weightsMapPath.getName());
			propertiesWriter.close();
		} catch (FileNotFoundException fe) {
			logger.log(Level.SEVERE, "Input WeightsMap File to save Not Found", fe);
			throw new RuntimeException("Input WeightsMap file Not Found!" + fe.getLocalizedMessage());
		} catch (IOException ie) {
			logger.log(Level.SEVERE, "IO Exception saving WeightsMap file", ie);
			ie.printStackTrace();
			throw new RuntimeException("IO Exception saving WeightsMap file!" + ie.getLocalizedMessage());
		}				
				
	}
	
	/**
	 * Method reads in a Properties file at the given File path
	 * 
	 * @param weightsMapPath - file path to read Properties object
	 * @return - Properties object that was read
	 */
	private static Properties readWeightsMapFromFile( File weightsMapPath ) {
		// check inputs
		if (weightsMapPath == null || !weightsMapPath.isFile()) {
			logger.log(Level.SEVERE, "Invalid Properties file specified!");
			throw new IllegalArgumentException("Cannot read..Invalid Properties file specified!");
		}
		
		// Result
		Properties result = new Properties();
		
		// Read in the Properties file
		try {
			BufferedReader propertiesReader = 
					new BufferedReader(new FileReader(weightsMapPath));
			assert propertiesReader != null;
			result.load(propertiesReader);
			logger.log(Level.INFO, "Loaded WeightsMap File..." + weightsMapPath.getName());
			propertiesReader.close();
		} catch (FileNotFoundException fe) {
			logger.log(Level.SEVERE, "Input WeightsMap File Not Found", fe);
			// We'll re-catch the File NotFound in the Getter since its OK for the CurrentAttributeWeights to not exist!!
			//throw new RuntimeException("Input WeightsMap file Not Found!" + fe.getLocalizedMessage());
		} catch (IOException ie) {
			logger.log(Level.SEVERE, "IO Exception loading WeightsMap file", ie);
			throw new RuntimeException("IO Exception loading WeightsMap file!" + ie.getLocalizedMessage());
		}				
		
		return result;
	}
	
	public static void deleteDirectoryFiles( File filesDir ) {
		
		//check input
		if (filesDir == null || !filesDir.isDirectory()) {
			logger.log(Level.INFO, "Invalid Directory specified...cannot delete Files!");
			return;
		}
		// Delete
		File[] deleteFiles = filesDir.listFiles();
		assert deleteFiles != null;
		if (deleteFiles.length > 0) {
			for (int i=0; i<deleteFiles.length; i++) {
				if (deleteFiles[i].isFile()) {
					deleteFiles[i].delete();
				}
			}
		}		
	}

}
