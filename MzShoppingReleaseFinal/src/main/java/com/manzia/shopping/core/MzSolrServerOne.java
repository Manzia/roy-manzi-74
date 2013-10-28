package com.manzia.shopping.core;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Annotation that indicates which static {@link SolrServer} we
 * are injecting. It is recommended that all requests from the
 * share the same SolrServer instance
 * 
 * @author Roy Manzi Tumubweinee, Jan 28, 2013, Manzia Corporation
 *
 */
@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface MzSolrServerOne {

}
