package com.manzia.shopping.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the manzia_attributes database table.
 * 
 */
@Entity
@Table(name="manzia_attributes")
public class MzAttributeName implements Serializable {
	private static final long serialVersionUID = 1L;
	private String attributeName;
	private String attributeType;
	private String mzAttributeName;

    public MzAttributeName() {
    }


	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	public String getAttributeName() {
		return this.attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}


	public String getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}


	public String getMzAttributeName() {
		return this.mzAttributeName;
	}

	public void setMzAttributeName(String mzAttributeName) {
		this.mzAttributeName = mzAttributeName;
	}

}