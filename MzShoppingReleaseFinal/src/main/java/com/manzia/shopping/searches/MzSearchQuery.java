package com.manzia.shopping.searches;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

import com.manzia.shopping.core.MzTaskManagerService;
import com.manzia.shopping.vectorize.MzSequenceFileGenerator;
import com.manzia.shopping.vectorize.MzValidCategory;

/**
 * Class that wraps a search Query as expected from UI devices. It includes a 
 * Map of Query Parameters, a List of search Keywords, and a product Category associated
 * with the query
 * 
 * @author Roy Manzi Tumubweinee, Jan 28, 2013, Manzia Corporation
 *
 */

public class MzSearchQuery {
	
	private Map<String, String> queryParameters;	// query parameters e.g brand=HP
	private String categoryName;	// Category associated with the query
	private List<String> queryList; // list of sub-queries i.e query Terms
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzSearchQuery.class.getCanonicalName());
	
	// Map Keys
	private static final String categoryKey = "Category";
	private static final String skuKey = "sku";
	private static final String kQueryKey = "q";
	
	// Query Key Pattern, i.e q1=term1 term2&q2=term3...
	protected static final Pattern QUERY_PATTERN = Pattern.compile("q[\\d]+");
	
	/**
	 * Default Constructor
	 */
	private MzSearchQuery() {
		
	}
	
	/**
	 * MzSearchQuery constructor
	 * @param categoryName - product category associated with query. This is a REQUIRED argument
	 * @param queryMap - map of query parameters e.g brand=HP, can be NULL
	 * @deprecated - use {@link MzSearchQuery#generateSearchQuery(Map)}
	 */
	
	public MzSearchQuery(String categoryName, Map<String, String> queryMap 
			/*List<String> keywords*/) {
		// We must have a valid categoryName
		if (categoryName == null || categoryName.isEmpty()) {
			throw new IllegalArgumentException("Invalid constructor argument [categoryName] while instantiating object of class: " 
		+ MzSearchQuery.class.getName() );
		}
		if(queryMap == null) {
			this.queryParameters = new HashMap<String, String>();
		} else {
			this.queryParameters = queryMap;
		}
		/*if (keywords == null) {
			this.keywordList = new ArrayList<String>();
		} else {
			this.keywordList = keywords;
		}*/
		this.categoryName = categoryName;		
	}
	
	/**
	 * Static method that generates a MzSearchQuery object from a Map generated
	 * from the getQueryParameters() method of a {@link javax.ws.rs.core.UriInfo} instance.
	 * The Map generated from the method above is a {@link javax.ws.rs.core.MultivaluedMap}
	 * that will need to converted to a {@link java.util.Map} that will be passed as
	 * an argument to the generateSearchQuery() method <br>
	 * 
	 * @param queryMap - {@link java.util.Map} with query parameters
	 * @return - MzSearchQuery object or NULL if Map of query parameters contains no Category Key/Value
	 * or the Category value in the Map is not valid i.e not one of the Categories specified in the
	 * {@link MzSequenceFileGenerator.CATEGORY_LIST} list.
	 */
	public static MzSearchQuery generateSearchQuery( Map<String, String> queryMap ) {
		
		// check input
		if (queryMap == null || queryMap.isEmpty()) {
			logger.log(Level.SEVERE, "Illegal Map of query parameters specified...cannot generate MzSearchQuery object!");
			throw new IllegalArgumentException("Invalid Map of query Parameters specified..cannot generate MzSearchQuery object");
		}
		
		// Output
		MzSearchQuery searchQuery;
		
		// check again we have a Category value
		String categoryValue = queryMap.get(categoryKey);
		if (categoryValue == null || categoryValue.isEmpty()) {
			logger.log(Level.WARNING, "Map of Query Parameters does not contain a Category Key/Value");
			return null;
		} else {
								
			Map<String, String> queryParams = new HashMap<String, String>();
			queryParams.put(categoryKey, categoryValue);
			
			// get the keywords
			List<String> termList = getQueryTerms(queryMap);
			assert termList != null;
			String queryTerms = convertToSolrSyntax(termList);
			if (queryTerms != null && !queryTerms.isEmpty()) {
				queryParams.put(kQueryKey, queryTerms);
			} 
			// Create the MzSearchQuery
			if (queryMap.containsKey(skuKey)) {
				queryParams.put(skuKey, queryMap.get(skuKey));
			}
			//searchQuery = new MzSearchQuery(categoryValue, queryParams);
			searchQuery = new MzSearchQuery();
			searchQuery.setCategoryName(categoryValue);
			searchQuery.setQueryParameters(queryParams);
			
			// Add sub-queries
			List<String> formattedList = new ArrayList<String>();
			for (String term : termList) {
				String newTerm = doubleQuoteString(term);
				if (newTerm != null && !newTerm.isEmpty()) {
					formattedList.add(newTerm);
				}
			}
			searchQuery.setQueryList(formattedList);
			
			assert searchQuery != null;			
		}				
		return searchQuery;		
	}
	
	
	// Getter and Setters
	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}
	
	public void setQueryParameters(Map<String, String> queryParameters) {
		if (queryParameters != null) {
			this.queryParameters = queryParameters;
		}		
	}
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		if (categoryName != null & !categoryName.isEmpty()) {
			this.categoryName = categoryName;
		}		
	}
	
	public List<String> getQueryList() {
		return queryList;
	}

	public void setQueryList(List<String> queryList) {
		if (queryList != null) {
			this.queryList = queryList;
		}		
	}

	/**
	 * Method converts a {@link List} of {@link String} query Terms into
	 * the Solr Query Syntax. In particular: <br>
	 * 1- all query Terms that are Phrases are double-quoted <br>
	 * 2- query Terms are concatenated into one query string with whitespace in between
	 * 
	 * @param termList
	 * @return
	 */
	protected static String convertToSolrSyntax ( List<String> termList )
	{
		// Check Input
		if (termList == null) {
			throw new IllegalArgumentException("Specified List of query Terms is Null");
		}
				
		// Iterate
		StringBuffer strBuffer = new StringBuffer();
		for (Iterator<String> termIter = termList.listIterator(); termIter.hasNext();) {
			String term = termIter.next();
			if (queryStringIsPhrase(term)) {
				strBuffer.append("\"").append(term).append("\"");
				if (termIter.hasNext()) {
					strBuffer.append(" ");
				}
			} else {
				strBuffer.append(term);
				if (termIter.hasNext()) {
					strBuffer.append(" ");
				}
			}
		}		
		return strBuffer.toString();		
	}
	
	/**
	 * Helper method that double quotes phrases, ie. {@link String} attractive display
	 * is converted to {@link String} "attractive display"
	 * 
	 * @param term - {@link String} to double quote
	 * @return - double quoted {@link String}
	 */
	protected static String doubleQuoteString ( String term ) {
		
		// check input
		if (term == null) return term;
		
		// double Quote
		StringBuffer strBuffer = new StringBuffer();
		if (queryStringIsPhrase(term)) {
			strBuffer.append("\"").append(term).append("\"");
			return strBuffer.toString();
		} else {
			return term;
		}		
	}

	/**
	 * Given a {@link Map} of query Parameters, extracts the query Terms/Phrases i.e values
	 * for Keys that match the Pattern "q[0-9]+" e.g q1=termX termY, q25=termZ etc. <br>
	 * 
	 * @param queryMap - {@link Map} of query parameters
	 * @return - {@link List} of {@link String} of extracted query Terms/Phrases
	 */
	protected static List<String> getQueryTerms( Map<String, String> queryMap) {
		
		// Output
		List<String> termList = new ArrayList<String>();
		
		// Iterate over the Map
		for (Iterator<Map.Entry<String, String>> queryIter = queryMap.entrySet().iterator(); queryIter.hasNext();) {
    		Map.Entry<String, String> mapEntry = queryIter.next();
    		if (QUERY_PATTERN.matcher(mapEntry.getKey()).matches()) {
    			termList.add(mapEntry.getValue());
    		}
    	}
		
		return termList;		
	}
	
	/**
	 * Method that uses Apache Lucene {@link StandardTokenizer} to generate a TokenStream
	 * 
	 * @param queryString {@link String} to be tokenized
	 * @return returns {@link TokenStream} from the queryString	 
	 */
	protected static TokenStream tokenizeQueryString(String queryString)
	{
		Reader featuresReader = new StringReader(queryString);
		assert featuresReader != null;
		
		TokenStream result = new StandardTokenizer(Version.LUCENE_36, featuresReader);
		assert result != null;
				
		return result;
	}
	
	/**
	 * Given a queryString {@link String} determines if its composed of a single Term
	 * or is a Phrase (mulitple Terms) <br>
	 * 
	 * @param queryString - {@link String} to be analyzed
	 * @return - true if queryString is a Phrase else false if its a single Token
	 */
	protected static boolean queryStringIsPhrase ( String queryString ) {
		
		// check inputs
		if (queryString == null || queryString.isEmpty()) {
			throw new IllegalArgumentException("Specified queryString is Null or Empty...cannot determine if Phrase");
		}
		
		// Tokenize
		TokenStream queryStream = tokenizeQueryString(queryString);
		assert queryStream != null;
		CharTermAttribute termAtt =
				(CharTermAttribute) queryStream.addAttribute(CharTermAttribute.class);
		assert termAtt != null;
		
		// Count Tokens
		int count = 0;
		try {
			while (queryStream.incrementToken()) {
				count++;							
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO Exception while Tokenizing Query String: {0}", new Object[]{queryString});
			throw new RuntimeException("IO Exception while Tokening Query String:" + e.getLocalizedMessage());
		} finally {
			try {
				queryStream.close();
			} catch (IOException e) {
				throw new RuntimeException("IO Exception while closing TokenStream during tokenizing of Query String: " + queryString);				
			}
		}
				
		return count > 1 ? true : false;
	}
	

}
