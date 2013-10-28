package com.manzia.shopping.vectorize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.common.distance.TanimotoDistanceMeasure;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterator;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.hadoop.similarity.VectorDistanceSimilarityJob;

/**
 * Session Bean implementation class MzRankingServiceCosine
 * This bean performs the following main tasks:
 * 1- read a SequenceFile that stores NamedVectors
 * 2- computes the Cosine Distance between the searchVector and each
 * of the stored Vectors in the SequenceFile
 * 3- uses the Mahout VectorDistanceSimilarityJob class to compute distances
 * 
 * NOTE: we use the name of the Search Vector i.e its Category to determine
 * which SequenceFiles
 */
@Stateless
//@Local(MzRankingService.class)
@LocalBean
@MzCosineDistance
public class MzRankingServiceCosine implements MzRankingService {
	
	//Logger
			public static final Logger logger = 
						Logger.getLogger(MzRankingServiceCosine.class.getCanonicalName());
	
	// String Arguments
	private String INPUTDIR;			// Input Directory of the VectorDistanceSimilarityJob
	private String OUTPUTDIR;			// Output Directory of the VectorDistanceSimilarityJob
	private static final DistanceMeasure DefaultMeasure = new CosineDistanceMeasure();
	private static final String OUTTYPE = "v";						// Format of output SequenceFile [pw | v]
	private int topKModelNumbers = 5;					// Number of TopK modelNumbers to return
	private static final int TopKMaxValue = 25;			// Max number of TopK ModelNumbers
	private static final String kMergeSeqFileSuffix = "-merge";	// suffix added to indicate file created from merging a bunch of Sequence Files
	
		
	// Name of Sequence File containing Search Vectors
	private File sequenceFilename;	

    /**
     * Default constructor. 
     */
    public MzRankingServiceCosine() {
       
    	// Create the args
    	//arguments = new String[]{ "-i", INPUTDIR, "-o", OUTPUTDIR, "-dm", DISTANCE_MEASURE, "-s", SEED_DIR, "-ot", OUTTYPE };
    }
    
    // Getter    
    public final DistanceMeasure getMeasure() {
		return DefaultMeasure;
	}

    // Getter and Setter for the TopK value
	public final int getTopKModelNumbers() {
		return topKModelNumbers;
	}

	public final void setTopKModelNumbers(int topKModelNumbers) {
		this.topKModelNumbers = topKModelNumbers;
	}


	/**
     * @see MzRankingService#computeModelNumbersForVector(Vector, int)
     */
    public List<String> computeModelNumbersForVector(NamedVector searchVector, int numClosest) {
        
    	List<String> modelNumbers = new ArrayList<String>();
    	
    	// Check for valid inputs
    	if (searchVector == null) {
    		logger.log(Level.SEVERE, "Search Vector specified is NULL!");
    		return modelNumbers;	// return empty List
    	}
    	if (searchVector.getNumNondefaultElements() < 1) {
    		logger.log(Level.WARNING, "Search Vector has all Zero Values...cannot compute ModelNumbers!");
    		return modelNumbers;	// return empty List
    	}
    	// Bound the integer between 0 and 25 with default of 5.
    	numClosest = numClosest <= 0 ? topKModelNumbers : numClosest;
    	numClosest = numClosest > TopKMaxValue ? TopKMaxValue : numClosest;
    	    	
    	// Create the Sequence File that stores the search Vector
    	List<NamedVector> searchVectorList = new ArrayList<NamedVector>();
    	searchVectorList.add(searchVector);
    	
    	// Make sure we have an INPUTDIR
    	INPUTDIR = MzRankingServiceUtils.createInputDirectory(searchVector.getName()).getAbsolutePath();
    	assert INPUTDIR != null;
    	
    	// Ensure we have a SEED_DIR
    	File seedDir = MzRankingServiceUtils.createSeedDirectory();
    	assert seedDir != null;
    	
    	// Note that the temp file created below will be overwritten by the SequenceFile.Writer
    	sequenceFilename = MzRankingServiceUtils.generateSequenceFilename();
		
    	OUTPUTDIR = MzRankingServiceUtils.createOutputDirectory(sequenceFilename);	// name the Output Directory according SequenceFile name
    	assert OUTPUTDIR != null;
    	
    	// Create the Sequence File
    	boolean success = false;
    	try {
			success = MzSequenceFileGenerator.writeVectorToSequenceFile(searchVectorList, sequenceFilename.getAbsolutePath());
			//assert success == true;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception while writing Search Vector to Sequence File at Path: {0}", 
					new Object[] {sequenceFilename.getAbsolutePath()} );
			MzRankingServiceUtils.cleanUp(sequenceFilename);
			throw new RuntimeException(e.getLocalizedMessage());
		}    	
    	
    	// We can now compute the ModelNumbers for this Search Vector
    	boolean didComputeDistances = false;
    	if (success) {
    		didComputeDistances = computeVectorDistances(new Path(INPUTDIR), new Path(seedDir.getAbsolutePath()), new Path(OUTPUTDIR), this.getMeasure());
    		
    		// Get the Output Sequence File that was created by the VectorDistanceSimilarityJob above
        	Configuration config = new Configuration();
        	File distanceSeqFile = loadDistanceVectorSequenceFile(OUTPUTDIR, config);
        	if (distanceSeqFile == null) {
        		
        		logger.log(Level.WARNING, "Failed to load Sequence File from OUTPUTDIR at Path: {0}",
    					new Object[]{OUTPUTDIR});
        		MzRankingServiceUtils.cleanUp(sequenceFilename);
        		return modelNumbers;
        	}
        	
    		if (didComputeDistances) {
    			modelNumbers = computeTopKModelNumbers(distanceSeqFile, numClosest);
    		} else {
    			logger.log(Level.WARNING, "Did not compute top ModelNumbers for Search Vector at Path: {0}", 
    					new Object[]{sequenceFilename.getAbsolutePath()} );
    		}
    	} else {
    		logger.log(Level.WARNING, "Failed to write Search Vector to Sequence File at Path: {0}", 
    				new Object[]{sequenceFilename.getAbsolutePath()});
    	}
    	
    	// Clean up directories for the next run/threads etc.
    	MzRankingServiceUtils.cleanUp(sequenceFilename);
    	MzRankingServiceUtils.clearSeedDirectory(seedDir);    	
    	
		return modelNumbers;
    }
    
   /**
    * Loads a SequenceFile from the OUTPUTDIR generated by running the VectorDistanceSimilarityJob
    * If there are multiple Sequence Files, they are merged first and the merged File loaded.
    * 
    * @param outputDir - directory passed in the "-o" option to the VectorDistanceSimilarityJob
    * @param conf - Hadoop configuration
    * @return - File instance representing the SequenceFile or NULL if none was found
    */
    public File loadDistanceVectorSequenceFile( String outputDir, Configuration conf) {
		
    	File distanceSeqFile;
    	FileSystem fs;
    	SequenceFile.Sorter mergeFile;
    	
    	// Set the parameters
    	Path outputPath = new Path (outputDir);
    	assert outputPath != null;
    	PathFilter pathFilter = PathFilters.partFilter();
    	assert pathFilter != null;
    	
    	// Set FileSystem
    	try {
			fs = FileSystem.get(conf);			
		} catch (IOException e1) {
			logger.log(Level.SEVERE, "IO Exception creating FileSystem to load SequenceFile from OUTPUTDIR at Path: {0}", 
					new Object[]{outputPath.toString()});
			throw new RuntimeException("IO Exception creating FileSystem to load SequenceFile from OUTPUTDIR" + e1.getLocalizedMessage());			
		}
    	assert fs != null;
    	
    	// Get the Files
    	try {
			FileStatus[] statuses = HadoopUtil.getFileStatus(outputPath, PathType.LIST, pathFilter, null, conf);
			assert statuses != null;
			
			// Get the first Sequence File, we expect to always have 1 Sequence File in the OUTPUTDIR at any one time
			if (statuses.length == 1) {
				distanceSeqFile = new File (outputDir, statuses[0].getPath().getName());
				assert distanceSeqFile != null;
			} else if (statuses.length > 1) {
				logger.log(Level.INFO, "Merging {0} Vector Distance Sequence Files found in OUTPUTDIR: {1}", 
						new Object[]{Integer.toString(statuses.length), outputDir});
				
				// Merge all the Sequence Files into one file
				Path[] seqFilePaths = new Path[statuses.length];
				assert seqFilePaths != null;
				for (int i=0; i<statuses.length; i++) {
					seqFilePaths[i] = statuses[i].getPath();
				}
				
				// Use the first Sequence Filename to generate the name of the outFile
				String outFileStr = statuses[0].getPath().getName() + kMergeSeqFileSuffix;
				assert outFileStr != null;
				Path outFilePath = new Path(outputPath, outFileStr);
				assert outFilePath != null;
				
				// Merge
				mergeFile = new SequenceFile.Sorter(fs, Text.class, VectorWritable.class, conf);
				assert mergeFile != null;
				mergeFile.merge(seqFilePaths, outFilePath);
				
				distanceSeqFile = new File (outputDir, outFileStr);
				assert distanceSeqFile != null;
				
			} else {
				logger.log(Level.WARNING, "NO Sequence File in OUTPUTDIR, expected 1 file at Path: {0}",
						new Object[]{outputDir});
				distanceSeqFile = null;
			}
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO Exception while loading SequenceFile from OUTPUTDIR at Path: {0}", 
					new Object[]{outputPath.toString()});
			e.printStackTrace();
			throw new RuntimeException("IO Exception while loading SequenceFile from OUTPUTDIR" + e.getLocalizedMessage());
		}
    	return distanceSeqFile;
    	
    }
    
    /**
     * Given a SequenceFile of the format <Text, VectorWritable> where the VectorWritable is a dense Vector with
     * a double value representing the distance between the Vector named by the Text value and a search Vector, this
     * method computes the Top-K "closest" modelNumbers (Vectors) to the search Vector in the SEED_DIR directory.
     * 
     * @param sequenceFile - SequenceFile generated from running the VectorDistanceSimilarityJob with the "v" value for
     * the "-ot" (OUTTYPE) option. This file is loaded from the OUTPUTDIR.
     * 
     * @param topK - number of K "closest" modelNumbers to return, Default value is 5 and the max is TopKMaxValue constant
     * 
     * @return - List of Strings each of the format category:modelNumber:modelBrand for the "closest" K Vectors to search Vector.
     */
    public List<String> computeTopKModelNumbers( File sequenceFile, int topK) {
    	
    	List<String> modelNumbers = new ArrayList<String>();
    	
    	//note we use a LinkedList becoz we are making alot of insertions and deletions
		// at any point in the List which are operations typically done in constant time in a LinkedList
    	List<Pair<String, Double>> modelNumList = Collections.synchronizedList( new LinkedList<Pair<String, Double>>());
    	
    	// Return top 5 (K = 5) ModelNumber strings by default with a MAX of 25
    	topK = topK <= 0 ? 5 : topK;
    	topK = topK > TopKMaxValue ? TopKMaxValue : topK;
    	
    	// check inputs and iterate
    	Configuration conf = new Configuration();
    	FileSystem fs;
    	if (sequenceFile.exists() && sequenceFile.isFile()) {
    		try {
    			fs = FileSystem.get(conf);
				Path qualifiedSeqFile = fs.makeQualified(new Path( sequenceFile.getAbsolutePath()));
				assert qualifiedSeqFile != null;
    			SequenceFileIterator<Text, VectorWritable> seqIterator = 
						new SequenceFileIterator<Text, VectorWritable>(qualifiedSeqFile, true, conf);
				assert seqIterator != null;
				Pair<Text, VectorWritable> currElement;
				Pair<String, Double> listElement;
				int count = 0;		// keep track of how many elements we have traversed in the Sequence File				
				int greaterIndex = -1;		// Index of largest element greater than value in the list
				
				/*while (seqIterator.hasNext()) {
					currElement = seqIterator.computeNext();
					if (currElement != null) {
						
						//assert currElement != null;
						listElement = 
								new Pair<String, Double>(currElement.getFirst().toString(), Double.valueOf(currElement.getSecond().get().get(0)));
						assert listElement != null;
						if (count < topK) {
							
							// we populate our LinkedList, note we use a LinkedList becoz we are making alot of insertions and deletions
							// at any point in the List which are operations typically done in constant time in a LinkedList
							modelNumList.add(listElement);
							//System.out.printf("Added Element to List of Model Numbers with String: %s, Double: %.4f",
								//	currElement.getFirst().toString(), Double.valueOf(currElement.getSecond().get().get(0)));
							//System.out.println();
							count++;
							
						} else {
							assert modelNumList.size() == topK;
							double distanceValue = currElement.getSecond().get().get(0);
							Integer computedIndex = indexOfLargestElementGreaterthanValue(distanceValue, modelNumList);
							if (computedIndex != null) {
								greaterIndex = computedIndex.intValue();
							}
							if (greaterIndex >= 0) {
								
								// we can now insert the "closer" Vector in the SequenceFile to our List of ModelNumbers
								// and delete the "larger" Vector as determined by the indexOfLargestElementGreaterthanValue method
								modelNumList.remove(greaterIndex);
								modelNumList.add(listElement);
								greaterIndex = -1;
							}
						}	
					
					}
				}*/
				seqIterator.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "IO Exception while iterating over Sequence file at Path: {0}", 
						new Object[] {sequenceFile.getAbsolutePath()});
				e.printStackTrace();
				throw new RuntimeException("IO Exception while iterating over Sequence File!");
			}
    		logger.log(Level.INFO, "Success computing ModelNumbers for Sequence File: {0}", new Object[]{sequenceFile.getName()});
    	}
    	
    	// Iterate over the LinkedList of Pair<String, Double> and populate the modelNumbers List
    	for (Pair<String, Double> topModelNums : modelNumList) {
    		modelNumbers.add(topModelNums.getFirst());
    	}    	
    	
    	// These are "guaranteed" to be the closest modelNumbers to the Search Vector provided
    	return modelNumbers;
    }
    
    /**
     * Helper method that given a LinkedList of Pair<String, Double) elements will return the index of the element
     * whose Double value has the largest absolute difference. If two elements have the same absolute we randomly
     * choose one of the two.
     *  
     * @param value - double value we compare to each Double in the List
     * @param theList - the list of Pair<String, Double> elements
     * @return - the index of the largest element greater than the provided value
     */
    public Integer indexOfLargestElementGreaterthanValue( double value, List<Pair<String, Double>>theList )
    {
		// check inputs
    	assert theList != null;
    	if (theList.isEmpty()) {
    		logger.log(Level.WARNING, "Empty List of Pair<T1, T2> elements provided!");
    		return null;
    	}
    	
    	// For the CosineDistanceMeasure, the possible distance values range from 0 to 2.
    	//this.dmeasure = this.dmeasure != null ? this.dmeasure : DefaultMeasure;
    	//assert this.dmeasure != null;
    	//System.out.printf("Using Distance Measure (Index method): %s\n", dmeasure.getClass().getName());
    	
    	if (this.getMeasure().getClass().getName().equals(CosineDistanceMeasure.class.getName())) {
    		if (value > 2.0 || value < 0.0 || Double.isNaN(value) ) {
        		logger.log(Level.SEVERE, "Specified double value lies outside expected range (0.0 to 2.0) for Distance Measure: {0}",
        				new Object[]{this.getMeasure().getClass().getCanonicalName()});
        		throw new IllegalArgumentException("Specified double value lies outside expected range for Distance Measure!" +
        				". Value should range from 0.0 to 2.0");    		
        	}
    	} else if (Double.isNaN(value)) {
    		logger.log(Level.SEVERE, "Specified double value lies outside expected range for Distance Measure: {0}",
    				new Object[]{this.getMeasure().getClass().getName()});
    		throw new IllegalArgumentException("Specified double value lies outside expected range for Distance Measure!");   
    	}
    	
    	// Algorithm
    	int elementIndex;		// keeps track of the elements and their indexes
    	int greaterIndex = -1;	// set an "impossible" value to avoid returning first [0] index by default
    	double currentValue;
    	double currentDiff;
    	double largestDiff = 0.0;
    	ListIterator<Pair<String, Double>> elementIterator = theList.listIterator();
    	assert elementIterator != null;
    	
    	while (elementIterator.hasNext()) {
    		elementIndex = elementIterator.nextIndex();
    		currentValue = elementIterator.next().getSecond().doubleValue();
    		if (value < currentValue) {
    			currentDiff = currentValue - value;
    			if (currentDiff > largestDiff) {
    				largestDiff = currentDiff;
    				greaterIndex = elementIndex;
    			}    			
    		}
    	}
    	
    	// Return the Index otherwise return null if no Element is greater than provided value.
    	if (greaterIndex >= 0) {
    		return Integer.valueOf(greaterIndex);
    	} else {
    		return null;    		
    	}    	
    }
    
    /**
     * Method computes the distances between the SearchVector and a set of Input Vectors stored
     * in a SequenceFile in the INPUTDIR. The search Vector's sequenceFile is expected to be in
     * the SEED_DIR. The result of the computation is written to SequenceFile in the OUTPUTDIR
     * @param searchVector - Vector for which distances are being computed
     * @return - true is computation succeeded else false
     */
    public boolean computeVectorDistances( Path inputDir, Path seedsDir, Path outputDir, DistanceMeasure measure) {
    	
    	//check inputs
    	assert inputDir != null;
    	assert seedsDir != null;
    	assert outputDir != null;
    	assert measure != null;
    	
    	/* Verify distance measure is not null
    	this.dmeasure = this.dmeasure != null ? this.dmeasure : DefaultMeasure;
    	assert this.dmeasure != null; */
    	//System.out.printf("Using Distance Measure (VectorDistances method): %s\n", this.getMeasure().getClass().getName());
    	boolean didCompute = false;
    	
    	Configuration conf = new Configuration();
    	FileSystem fs;
    	try {
			fs = FileSystem.get(conf);
			assert fs != null;
			boolean success = fs.getFileStatus(inputDir).isDir();
			
			if (success) {
				// we can now run the VectorDistanceSimilarityJob
				VectorDistanceSimilarityJob.run(conf, inputDir, seedsDir, outputDir, measure, OUTTYPE);
				didCompute = true;
			} else {
				logger.log(Level.SEVERE, "Invalid Input Directory! " + INPUTDIR);
				throw new FileNotFoundException("Input Directory not Found:" + INPUTDIR);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO exception while instantiating FileSystem!");
			e.printStackTrace();
			throw new RuntimeException("IO Exception while creating FileSystem" + e.getLocalizedMessage());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "VectorDistanceSimilarityJob threw an exception!");
			e.printStackTrace();
			throw new RuntimeException("Exception while running VectorDistanceSimilarityJob " + e.getLocalizedMessage());
		}
    	return didCompute;
    }
    
    /**
     * Inner class that filters the Sequence Files in the specified directory
     * whose filenames do not contain the specified category
     * @author admin
     *
     */
    protected static class MzCategoryFileFilter implements FilenameFilter {
    	
    	private String categoryLabel;	//Sequence Files with this string in their filenames will be returned
    	
    	// constructor
    	public MzCategoryFileFilter(String categoryName) {
    		this.categoryLabel = categoryName;
    	}

		@Override
		public boolean accept(File seqDir, String seqFile) {
			CharSequence category = categoryLabel.subSequence(0, categoryLabel.length()-1);
			assert category != null;
			if (seqFile.contains(category) && seqFile.startsWith("seqFile") && new File( seqDir, seqFile).length() > 0) {
				return true;
			} else {
				return false;
			}			
		}    	
    }

}
