package com.manzia.shopping.model;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the model_numbers database table.
 * 
 */
@Embeddable
public class MzModelNumberPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String modelNum;
	private String modelBrand;

    public MzModelNumberPK() {
    }
    
    // Main Constructor
    public MzModelNumberPK(String modelNum, String modelBrand) {
    	this.setModelNum(modelNum);
    	this.setModelBrand(modelBrand);
    }

	public String getModelNum() {
		return this.modelNum;
	}
	public void setModelNum(String modelNum) {
		this.modelNum = modelNum;
	}

	public String getModelBrand() {
		return this.modelBrand;
	}
	public void setModelBrand(String modelBrand) {
		this.modelBrand = modelBrand;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MzModelNumberPK)) {
			return false;
		}
		MzModelNumberPK castOther = (MzModelNumberPK)other;
		return 
			this.modelNum.equals(castOther.modelNum)
			&& this.modelBrand.equals(castOther.modelBrand);

    }
    
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.modelNum.hashCode();
		hash = hash * prime + this.modelBrand.hashCode();
		
		return hash;
    }
}