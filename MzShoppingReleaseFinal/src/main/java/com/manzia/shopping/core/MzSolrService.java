package com.manzia.shopping.core;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 * Session Bean implementation class MzSolrService
 * 
 * MzSolrService singleton bean maintains a single {@link SolrServer}
 * instance that is shared across search requests to avoid connection
 * leaks and other performance issues.
 * 
 * NOTE: An Elastic IP is used to resolve the Solr Server name in the AWS cloud
 * so that changes in the hosting Solr Servers will not "break" the code
 */
@Singleton
@LocalBean
@MzSolrServerOne
public class MzSolrService {
	
	// SolrServer URL
	private static final String urlString = "http://ec2-54-241-22-223.us-west-1.compute.amazonaws.com:8983/solr";
	
	// SolrServer instance
	private SolrServer solrServer;

    /**
     * Default constructor. 
     */
    public MzSolrService() {
        
    	// Instantiate SolrServer
    	solrServer = new HttpSolrServer(urlString);
    }
    
    public SolrServer getSolrServer() {
		
    	return solrServer;
    	
    }

}
