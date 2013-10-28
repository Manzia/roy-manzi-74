package com.manzia.shopping.core;

import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;

import com.manzia.shopping.products.RankedProducts;
import com.manzia.shopping.reviews.ReviewMatches;

/**
 *  Local business interface for MzTaskManagerService implementations that
 *  handle search requests, and mediate between the different back-end and
 *  front-end components. Interface is required since we are exposing the
 *  MzTaskManagerService EJB's in another module (i.e ManziaWebServices)
 *  
 * @author Roy Manzi Tumubweinee, Feb 04, 2012, Manzia Corporation
 *
 */
public interface MzTaskManagerServiceInterface {
	
	/**
	 * @see MzTaskManagerService#getRelevantProducts(Map)
	 * @param queryMap - {@link Map} of query parameters
	 * @return - {@link RankedProducts}
	 */
	public RankedProducts getRelevantProducts( Map<String, String> queryMap ) throws SolrServerException;
	
	/**
     * Given a {@link Map} of query parameters, method retrieves a {@link SolrDocumentList} of matching
     * documents/reviews from the Index associated with the MzSolrService argument and converts it to
     * a {@link ReviewMatches} JAXB-compliant instance. <br>
     * @see MzTaskManagerService#getConvertedRelevantReviews(Map, MzSolrService)
     * @param queryMap - {@link Map} of query parameters
     * @param indexService - {@link MzSolrService} Solr Server instance. If NULL, will use the default Solr Server
     * i.e annotated with MzSolrServerOne
     * @return - {@link ReviewMatches} of matching reviews
     * @throws SolrServerException - thrown by Solr Server
     */
    public ReviewMatches getConvertedRelevantReviews( Map<String, String> queryMap, MzSolrService indexService ) 
    		throws SolrServerException; 

}
