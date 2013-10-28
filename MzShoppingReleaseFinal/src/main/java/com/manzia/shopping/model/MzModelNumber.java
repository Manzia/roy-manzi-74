package com.manzia.shopping.model;

import java.io.Serializable;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the model_numbers database table.
 * 
 */
@Entity
@Table(name="model_numbers")
@NamedQueries({
    @NamedQuery(name = "findAllModelNumbers",query = "SELECT m "
    + "FROM MzModelNumber m")
    , @NamedQuery(name = "findAllModelCategories", query = "SELECT DISTINCT m.modelCategory FROM MzModelNumber m "
    + "ORDER BY m.modelCategory")
    , @NamedQuery(name = "findModelNumByCategory", query = "SELECT m FROM MzModelNumber m " +
    		"WHERE m.modelCategory like :modelCategory " + "ORDER BY m.id.modelNum")
})
public class MzModelNumber implements Serializable {
	private static final long serialVersionUID = 1L;
	private MzModelNumberPK id;
	private String modelCategory;
	private String modelFileName;
	private String retailerCategory;
	private String retailerItemId;
	private MzSearchDetail searchItem;
	private List<MzProductResult> productResults;

    public MzModelNumber() {
    }

    // Main Constructor
    public MzModelNumber(
    		String modelNum,
    		String modelBrand,
    		String modelCategory,
    		String modelFileName,
    		String retailerCategory,
    		String retailerItemId,
    		MzSearchDetail searchItem ) {
    	this.id = new MzModelNumberPK(modelNum, modelBrand);
    	this.modelCategory = modelCategory;
    	this.modelFileName = modelFileName;
    	this.retailerCategory = retailerCategory;
    	this.retailerItemId = retailerItemId;
    	this.searchItem = searchItem;
    	this.productResults = new ArrayList<MzProductResult>();
    }
    
    // Alternate Constructor
    public MzModelNumber(
    		MzModelNumberPK id,
    		String modelCategory,
    		String modelFileName,
    		String retailerCategory,
    		String retailerItemId,
    		MzSearchDetail searchItem ) {
    	this.id = id;
    	this.modelCategory = modelCategory;
    	this.modelFileName = modelFileName;
    	this.retailerCategory = retailerCategory;
    	this.retailerItemId = retailerItemId;
    	this.searchItem = searchItem;
    	this.productResults = new ArrayList<MzProductResult>();
    }
    
	@EmbeddedId
	public MzModelNumberPK getId() {
		return this.id;
	}

	public void setId(MzModelNumberPK id) {
		this.id = id;
	}
	

	public String getModelCategory() {
		return this.modelCategory;
	}

	public void setModelCategory(String modelCategory) {
		this.modelCategory = modelCategory;
	}


	public String getModelFileName() {
		return this.modelFileName;
	}

	public void setModelFileName(String modelFileName) {
		this.modelFileName = modelFileName;
	}


	public String getRetailerCategory() {
		return this.retailerCategory;
	}

	public void setRetailerCategory(String retailerCategory) {
		this.retailerCategory = retailerCategory;
	}


	public String getRetailerItemId() {
		return this.retailerItemId;
	}

	public void setRetailerItemId(String retailerItemId) {
		this.retailerItemId = retailerItemId;
	}


	//bi-directional many-to-one association to MzSearchDetail
	@ManyToOne(cascade={CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name="searchItemID")
	public MzSearchDetail getSearchItem() {
		return this.searchItem;
	}

	public void setSearchItem(MzSearchDetail searchItem) {
		this.searchItem = searchItem;
	}
	

	//bi-directional many-to-one association to MzProductResult
	@OneToMany(mappedBy="modelNumber", cascade={CascadeType.ALL})
	public List<MzProductResult> getProductResults() {
		return this.productResults;
	}

	public void setProductResults(List<MzProductResult> productResults) {
		this.productResults = productResults;
	}
	
}