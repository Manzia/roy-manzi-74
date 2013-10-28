package com.manzia.shopping.dao;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import javax.inject.Qualifier;

/**
 * Qualifier that produces an EntityManager configured to
 * work with the Production Database.
 * @author Roy Manzi Tumubweinee
 *
 */

@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface MzProdDatabase {

}
