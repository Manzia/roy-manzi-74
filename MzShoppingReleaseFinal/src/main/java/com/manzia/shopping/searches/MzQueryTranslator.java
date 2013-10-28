package com.manzia.shopping.searches;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

/**
 * <p>MzQueryTranslator primarily converts a {@link MzSearchQuery} object into
 * the appropriate {@link SolrQuery} object that can be submitted to the Solr
 * search engine (Index). </p>
 * 
 * <p>The type of {@link SolrQuery} object created is flagged by the presence/absence
 * of a "sku" query parameter in the {@link MzSearchQuery} map of query parameters.
 * Presence implies creation of a SolrQuery with response MzQueryType.RETURN_REVIEWS
 * while absence implies creation of a SolrQuery with response MzQueryType.RETURN_SKUS</p>
 * 
 * @author Roy Manzi Tumubweinee, Jan 28, 2013, Manzia Corporation
 *
 */
public class MzQueryTranslator {
	
	// Key that determines Query TYpe
	private static final String skuKey = "sku";
	private static final String categoryField = "category"; // Note that this is LOWERCASE, as defined in the Solr Schema.xml file
	private static final String kQueryKey = "q";
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzQueryTranslator.class.getCanonicalName());
	
	// Alphanumeric pattern for search terms
		private final Pattern alphanums = Pattern.compile("[\\p{Alnum}]+");
	
	/**
	 * Main method that translates a MzSearchQuery to a SolrQuery. Note that this method
	 * generates a single {@link SolrQuery} object from a {@link MzSearchQuery} object by
	 * merging all sub-queries and query Terms into one Solr syntax query <br>
	 * @param searchQuery - {@link MzSearchQuery} object
	 * @return - {@link SolrQuery} object
	 */
	public SolrQuery translateQuery( MzSearchQuery searchQuery ) {
		
		// Output
		SolrQuery query = new SolrQuery();
		
		// check inputs
		if (searchQuery == null) {
			logger.log(Level.WARNING, "MzSearchQuery object provided to translate is NULL!");
			return query;
		}
		
		// determine the QueryType
		SolrParams solrParams;
		NamedList<String> nameValues = new NamedList<String>();
		MzQueryType queryType = 
				searchQuery.getQueryParameters().containsKey(skuKey) ? MzQueryType.RETURN_REVIEWS : MzQueryType.RETURN_SKUS;
		
		switch (queryType) {
		case RETURN_SKUS:
			
			// Create Search String
			String queryTerms = searchQuery.getQueryParameters().get(kQueryKey);
			if ( queryTerms == null || queryTerms.isEmpty()) {
				logger.log(Level.WARNING, "Specified MzSearchQuery object has no search Terms/Keywords!");
				return query;
			}
			String searchString = mergeSearchTermsForCategory(queryTerms, searchQuery.getCategoryName(), null);
			if (searchString.isEmpty()) {
				logger.log(Level.WARNING, "Cannot create a query String for query Terms: {0} and category: {1} ", 
						new Object[]{ queryTerms, searchQuery.getCategoryName()});
				return query;
			} else {
				// get the Solr query parameters
				nameValues.addAll(new MzSolrQueryParameters().getParameters(queryType));
				solrParams = SolrParams.toSolrParams(nameValues);
				assert solrParams != null;
				
				// set the SolrQuery
				query.setQuery(searchString);
				query.add(solrParams);
			}			
			break;
		case RETURN_REVIEWS:
			
			// In this case we have a sku, so we search ONLY for documents with that sku
			String skuString = searchQuery.getQueryParameters().get(skuKey);
			if (skuString.isEmpty()) {
				
				// throw an exception if we have a sku value thats an empty string
				logger.log(Level.WARNING, "Value for sku Field is empty string..cannot query Solr Index!");
				return query;
			} else {
				
				String skuSearchString = mergeSearchTermsForCategory(null, searchQuery.getCategoryName(), skuString);
				// get the Solr query parameters
				nameValues.addAll(new MzSolrQueryParameters().getParameters(queryType));
				solrParams = SolrParams.toSolrParams(nameValues);
				assert solrParams != null;
				
				// set the SolrQuery
				query.setQuery(skuSearchString);
				//query.setParam("fq", buffer.toString());
				query.add(solrParams);				
			}
			break;
		default:
			break;				
		}		
		return query;		
	}
	
	/**
	 * Method translates a {@link MzSearchQuery} into a {@link List} of {@link SolrQuery}
	 * objects, one for each sub-query (query Term/Phrase) in the private queryList property
	 * of the {@link MzSearchQuery} <br>
	 * Note: This method generates queries with type {@link MzQueryType.RETURN_RANKS} only
	 * 
	 * @param searchQuery - input {@link MzSearchQuery}
	 * @return - {@link List} of {@link SolrQuery}
	 */
	public List<SolrQuery> translateQueryList( MzSearchQuery searchQuery ) {
		
		// Output - use a Synchronized List as this may be accessed concurrently by stateless EJBs
		List<SolrQuery> solrList = Collections.synchronizedList(new ArrayList<SolrQuery>());
		
		// Check Input
		if (searchQuery == null || searchQuery.getQueryList().isEmpty()) {
			logger.log(Level.WARNING, "Cannot translate to List of SolrQuery from MzSearchQuery that's Null or has Empty List of Sub-Queries" );
			return solrList;
		}
		
		// determine the QueryType
		SolrParams solrParams;
		NamedList<String> nameValues = new NamedList<String>();
		MzQueryType queryType = MzQueryType.RETURN_RANKS;
		
		// get the Solr query parameters
		nameValues.addAll(new MzSolrQueryParameters().getParameters(queryType));
		solrParams = SolrParams.toSolrParams(nameValues);
		assert solrParams != null;
		
		// Translate to List
		for (String subQuery : searchQuery.getQueryList()) {
			SolrQuery query = new SolrQuery();
			String queryTerm = mergeSearchTermsForCategory(subQuery, searchQuery.getCategoryName(), null);
			assert queryTerm != null;
			if (queryTerm.isEmpty()) {
				logger.log(Level.WARNING, "Cannot create a query String for query Terms: {0} and category: {1} ", 
						new Object[]{ queryTerm, searchQuery.getCategoryName()});				
			} else {
				// set the SolrQuery
				query.setQuery(queryTerm);
				query.add(solrParams);
				solrList.add(query);
			}				
		}			

		return solrList;		
	}
	
	/**
	 * Method merges a {@link String} of keywords and
	 * appends the constraint to search for documents in the Index that match
	 * a specific categoryName in the category Field i.e all the SolrQuery
	 * objects submitted to the Index are category-specific
	 *  
	 * @param keywords - {@link String} of keywords
	 * @param categoryName - categoryName
	 * @return - query search string
	 */
	private String mergeSearchTermsForCategory( String keywords, String categoryName, String skuValue ) {
		
		StringBuffer buffer = new StringBuffer();
		// check inputs
		if (keywords == null || keywords.isEmpty()) {
			// Note we don't include keywords since we are searching for the Reviews associated
			// with a specific Sku
			buffer.append(skuKey)
			.append(":")	// equals ":"
			.append(skuValue);
			/*.append("%2B")	//
			.append(categoryField)
			.append(":")
			.append(categoryName);*/	
		} else if (skuValue == null || skuValue.isEmpty()) {
			buffer.append(categoryField)
			.append(":")
			.append(categoryName)
			.append(" ")
			.append(keywords);
		} 		
		return buffer.toString();
	}
	
	/**
     * Method encodes a given String to conform to standard URL specifications
     * by escaping illegal characters and reserved characters
     * @param urlToEscape - string to encode
     * @return - encoded String if any reserved characters were found or null if null passed
     */
    public final String escapeStringForURL( String urlToEscape ) {
		
    	// check input
    	if ( urlToEscape == null ){
    		return null;
    	} else if (urlToEscape.length() == 0) {
    		return urlToEscape;
    	}
    	
    	/* If its all alphanumeric, just return
    	if (alphanums.matcher(urlToEscape).matches()) {
    		return urlToEscape;
    	}*/
    	
    	// Escape and quote
    	String out;
    	try {
    		out = URLEncoder.encode(urlToEscape, "UTF-8")
    			//.replaceAll("$", "%24")
    			.replace("+", "%2B")
    			.replace(".", "%2E")
    			.replace("*", "%2A")
        		.replace(",", "%2C")
        		.replace(";", "%3B")
        		.replace("^", "%5E")
        		.replace("<", "%3C")
        		//.replace("%", "%25")
        		.replaceAll(":", "%3A")
        		.replaceAll("&", "%26")
        		.replaceAll("=", "%3D")
        		//.replaceAll("?", "%3F")
        		.replace("/", "%2F")
        		//.replaceAll("[", "%5B")
        		//.replaceAll("]", "%5D")
        		.replaceAll("@", "%40");
        		//.replaceAll("\\", "%5C");    		
    		
    	} catch (UnsupportedEncodingException e) {
    		logger.log(Level.WARNING, "Unsupported character for URL encoding for String: {0}", 
    				new Object[] {urlToEscape});
    		out = urlToEscape;
    	}    	
    	
    	return out;    	
    }
    

}
