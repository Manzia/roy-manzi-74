package com.manzia.shopping.attributes;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

/**
 * Class that represents an (attribute, value) pair
 * 
 * @author Roy Manzi Tumubweinee, Nov 02, 2012. Manzia Corporation
 *
 */
public final class MzAttributeItem {
	
	//Logger
		public static final Logger logger = 
					Logger.getLogger(MzAttributeItem.class.getCanonicalName());
	
	// Attribute Options Separator
	private static final String kOptionSeparator = ",";
	
	// Attribute name
	private String attributeName;
	
	// Attribute Options
	private List<String> optionsList;
	
	// TokenStream of attributeOptions
	//TokenStream attributeOptions;
	
	// Boolean Values
	private static final String kStringYes = "Yes";
	private static final String kStringNo = "No";
	
	// Constructor
	public MzAttributeItem( String attribute, String value) {
		// check inputs
		if (attribute != null && value != null) {
			if (attribute.isEmpty() || value.isEmpty()) {
				attributeName = null;
				logger.log(Level.SEVERE, "Created NULL MzAttributeItem...attribute and/or value is EMPTY!");
				
			} else {
				// Split the value string using "," separator
				attributeName = attribute;
				String[] valueArray = value.split(kOptionSeparator);
				assert valueArray != null;
				optionsList = Arrays.asList(valueArray);
				assert optionsList != null;				
			}
		} else {
			attributeName = null;
			logger.log(Level.SEVERE, "Created NULL MzAttributeItem...provided attribute and/or value is NULL!");
		}
	}
	
	// Getters and Setters
	public final String getAttributeName() {
		return attributeName;
	}
	
	public final List<String> getOptionsList() {
		return optionsList;
	}

	// Indicates if the (attribute, value) has boolean values, i.e Yes, No
	public boolean isBooleanAttribute() {
		assert optionsList != null;
		
		// Result
		boolean success = false;
		
		// optionsList can only have 2 values if boolean attribute
		if (optionsList.size() == 2) {
			ListIterator<String> optionsIterator = this.optionsList.listIterator();
			while (optionsIterator.hasNext()) {
				String optionValue = optionsIterator.next();
				if (optionValue.equalsIgnoreCase(kStringYes) || optionValue.equalsIgnoreCase(kStringNo)) {
					success = true;
				}
			}
		}		
		return success;		
	}
	
	// Indicates if the (attribute, value) pair has numeric integer values,
	public boolean isNumericAttribute() {
		assert optionsList != null;
		
		// Result
		boolean isNumeric = false;
		int count = 0;
		
		// Alphanumeric pattern for Brand
		Pattern numPattern = Pattern.compile("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
		
		// check regex pattern
		ListIterator<String> optionsIterator = this.optionsList.listIterator();
		while (optionsIterator.hasNext()) {
			String optionValue = optionsIterator.next();
			if (numPattern.matcher(optionValue).matches()) {
				count++;
			}
		}
		isNumeric = this.optionsList.size() == count ? true : false;						
						
		return isNumeric;		
	}
	
	// Indicates if the (attribute, value) pair has a single "word" or token as its value
	// More specifically, each attribute option value can only be "decomposed" into one and only one Token
	public boolean isSingleWordOption() {
		// check instance variable
		assert this.optionsList != null;
		
		// Result
		boolean success = false;
		
		// Split each String
		if (this.optionsList.size() > 0) {
			for (String option : this.optionsList) {
				if (!option.isEmpty()) {
					String[] splitOption = option.split("\\s");
					assert splitOption != null;
					if (splitOption.length != 1) {
						return success = false;
					}
					success = true;		
				}						
			}
		}				
		return success;		
	}
	
	// Tokenizes a string into unique Tokens. Each attribute Option is tokenized and
	// and each Token "compared" to the value of an input (attribute, value) pair.
	public String uniqueMatchingOption( String value) {
		assert this.optionsList != null;
		
		// Result
		String result = null;
		SortedMap<Integer, String> optionVotes = Collections.synchronizedSortedMap( new TreeMap<Integer, String>());

		// check input		
		if (value == null || value.isEmpty()) {
			logger.log(Level.WARNING, "Invalid String value provided for unique Token Matching..will return NULL!");
			return null;
		} else  {
						
			// Iterate over all the Strings in the optionsList
			int optionVote;
			for (String option : optionsList) {
				optionVote = 0;
				if (!option.isEmpty()) {
					optionVote = this.countCommonCharsInStrings(option, value);
					
					// Add the "votes" to the SortedMap
					//System.out.printf("Option Token Value: %s Option Vote: %d", option, optionVote);
					//System.out.println();
					if (optionVote > 0) {
						// Ignore option strings for which we will have a tie in the optionsVote
						// i.e such that optionVotes does not have Keys with the same value
						if (!optionVotes.containsKey(Integer.valueOf(optionVote))) {
							optionVotes.put(Integer.valueOf(optionVote), option);
						}						
					}														
				} else {
					logger.log(Level.WARNING, "Cannot match Empty attribute option!");
				}				
			}
		}
		// Consider all scenarios
		// 1- optionVotes is empty, in which we simply return the String passed to us in the method argument
		// 2- optionVotes has one or more option Keys with the maximum optionVote value
		// 3- optionVotes has one option Key with the maximum optionVote value
		
		if (optionVotes.size() > 0) {
			// Determine the Key(s) whose value is max
			Integer highKey = optionVotes.lastKey();
			assert highKey != null;
			result = optionVotes.get(highKey);
			assert result != null;			
		} else {
			// Return the string passed in as the method argument
			result = value;
		}			
		return result;		
	}
	/*
	 * Helper method that returns the count of how many characters two strings
	 * have in common
	 */
	private int countCommonCharsInStrings( String first, String second) {
		// Result
		int commonChars = 0;
		
		//check input
		if (first == null || second == null || first.isEmpty() || second.isEmpty()) {
			return commonChars;
		} else {
			
			// Find common chars
			char[] firstArray = first.toCharArray();
			assert firstArray != null;
			char[] secondArray = second.toCharArray();
			assert secondArray != null;
			Set<Character> firstSet = new HashSet<Character>();
			Set<Character> secondSet = new HashSet<Character>();
			
			// Iterate
			for (char c : firstArray) {
				firstSet.add(c);
			}
			for (char c : secondArray) {
				secondSet.add(c);
			}
			
			// Get Count
			firstSet.retainAll(secondSet);
			commonChars = firstSet.size();
		}
		
		return commonChars;		
	}
	
	// Token the given String
	public TokenStream tokenizeString( String value ) {
		
		// check Input
		if (value == null || value.isEmpty()) {
			logger.log(Level.WARNING, "Invalid String value provided for Tokenization..will return NULL!");
			return null;
		}
		// Tokenize
		Reader valueReader = new StringReader(value);
		assert valueReader != null;
		TokenStream result = new WhitespaceTokenizer(Version.LUCENE_36, valueReader);
		assert result != null;
		
		return result;		
	}

}
