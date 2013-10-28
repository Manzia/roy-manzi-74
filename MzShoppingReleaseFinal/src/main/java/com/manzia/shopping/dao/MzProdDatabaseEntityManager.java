package com.manzia.shopping.dao;



import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

/**
 * Session Bean implementation class MzProdDatabaseEntityManager
 * 
 * The purpose of this singleton is to expose the EntityManager
 * resource via a Producer to all data accessing objects
 */
@Singleton
public class MzProdDatabaseEntityManager {
	
	@Produces
	@MzProdDatabase
	@PersistenceContext(name="manziaService") // Used for PRODUCTION (referenced in application.xml)
	//@PersistenceUnit(unitName = "ManziaShoppingRelease") // USED FOR TESTING (Embedded EJB Container)
	private EntityManager entityManager;	// using "static" generates a known WELD 000044 bug "cannot retrieve instance from null"
	
	/* Usage - @Inject @MzProdDatabase EntityManager em;
    @Produces
	@MzProdDatabase
    public final EntityManager create() {
		return entityManager;
	}*/
    
    /*public void close(@Disposes @MzProdDatabase EntityManager entityManager) {
        entityManager.close();
    }*/


}
