/*
 * COPYRIGHT
 * Copyright (c) 2009, Matt Williams <matt@mattwilliamsnyc.com>
 *
 * LICENSE
 *
 * This source file is subject to the new BSD license bundled with this package
 * in the file, LICENSE. This license is also available through the web at:
 * {@link http://www.opensource.org/licenses/bsd-license.php}. If you did not
 * receive a copy of the license, and are unable to obtain it through the web,
 * please send an email to matt@mattwilliamsnyc.com, and I will send you a copy.
 */
package com.manzia.shopping.bestbuy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a Best Buy catalog product.
 * 
 * Note that not all fields are populated on every response.
 * Be sure to check for null values, especially for lists, or complex child elements.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class Product extends Entity {
    /**
     * Creates a new Product containing no data.
     */
    public Product() {
        super();
    }

    /**
     * Creates a new Product from an document representation.
     * 
     * @param element Root element of a product representation
     */
    public Product(Element element) {
        super(element);
    }

    /**
     * Returns the "SKU" property associated with this product.
     * 
     * @return SKU # of this product
     */
    public String getSku() {
        return (String) getField("sku");
    }

    /**
     * Returns the "Product ID" property associated with this product.
     * 
     * @return Product ID of this product
     */
    public String getProductId() {
        return (String) getField("productId");
    }

    /**
     * Returns the "name" property associated with this product.
     * 
     * @return Name of this product
     */
    public String getName() {
        return (String) getField("name");
    }

    /**
     * Returns the "type" property associated with this product.
     * 
     * @return Type of this product
     */
    public String getType() {
        return (String) getField("type");
    }

    /**
     * Returns the "start date" property associated with this product.
     * 
     * @return Start date of this product
     */
    public String getStartDate() {
        return (String) getField("startDate");
    }

    /**
     * Returns the "new" property associated with this product.
     * 
     * @return Is this product new?
     */
    public boolean isNew() {
        return Boolean.valueOf((String) getField("new"));
    }

    /**
     * Returns the "active" property associated with this product.
     * 
     * @return Is this product active?
     */
    public boolean isActive() {
        return Boolean.valueOf((String) getField("active"));
    }

    /**
     * Returns the "active update date" property associated with this product.
     * 
     * @return Date that this product's "active" flag was last updated
     */
    public String getActiveUpdateDate() {
        return (String) getField("activeUpdateDate");
    }

    /**
     * Returns the "regular price" property associated with this product.
     * Modified the {@code getRegularPrice()} method to return 0 instead of null
     * which creates NPEs <br>
     * Modified By: Roy Manzi Tumubweinee, Oct 9, 2012, Manzia Corporation  
     * 
     * @return Regular price of this product
     */
    public float getRegularPrice() {
        String regularPrice = (String) getField("regularPrice");
        return null == regularPrice ? 0.0f : Float.valueOf(regularPrice);
    }

    /**
     * Returns the "sale price" property associated with this product.
     * Modified the {@code getSalePrice()} method to return 0 instead of null
     * which creates NPEs <br>
     * Modified By: Roy Manzi Tumubweinee, Oct 9, 2012, Manzia Corporation  
     * 
     * @return Current sale price for this product
     */
    public float getSalePrice() {
        String salePrice = (String) getField("salePrice");
        return null == salePrice ? 0.0f : Float.valueOf(salePrice);
    }

    /**
     * Returns the "price update date" property associated with this product.
     * 
     * @return Date that this product's price was last updated
     */
    public String getPriceUpdateDate() {
        return (String) getField("priceUpdateDate");
    }

    /**
     * Returns the "URL" property associated with this product.
     * 
     * @return URL of this product on bestbuy.com
     */
    public String getUrl() {
        return (String) getField("url");
    }

    /**
     * Returns the "buy URL" property associated with this product.
     * 
     * @return Buy URL of this product on bestbuy.com
     */
    public String getBuyUrl() {
        return (String) getField("buyUrl");
    }

    /**
     * Returns the "affiliate URL" property associated with this product.
     * 
     * @return Affiliate URL for this product
     */
    public String getAffiliateUrl() {
        return (String) getField("affiliateUrl");
    }

    /**
     * Returns the "add-to-cart URL" property associated with this product.
     * 
     * @return URL to add this product to the shopping cart on bestbuy.com
     */
    public String getAddToCartUrl() {
        return (String) getField("addToCartUrl");
    }

    /**
     * Returns the "affiliate add-to-cart URL" property associated with this product.
     * 
     * @return Affiliate add-to-cart URL for this product
     */
    public String getAffiliateAddToCartUrl() {
        return (String) getField("affiliateAddToCartUrl");
    }

    /**
     * Returns the "CJ affiliate URL" property associated with this product.
     * 
     * @return CJ affiliate URL of this product
     */
    public String getCjAffiliateUrl() {
        return (String) getField("cjAffiliateUrl");
    }
    
    /**
     * Returns the "mobile URL" property associated with this product.<br>
     * Modified By: Roy Manzi Tumubweinee, Oct 24, 2012, Manzia Corporation
     * 
     * @return mobile URL of this product
     */
    public String getMobileUrl() {
        return (String) getField("mobileUrl");
    }

    /**
     * Returns the "UPC" property associated with this product.
     * 
     * @return UPC identifier for this product
     */
    public String getUpc() {
        return (String) getField("upc");
    }

    /**
     * Returns a list of {@link Category categories} that this product belongs to.
     * 
     * @return List of categories that this product belongs to
     */
    public List<Category> getCategoryPath() {
        Object categoryPath = getField("categoryPath");
        List<Category> categories = new ArrayList<Category>();
        if(null != categoryPath && categoryPath instanceof Element && ((Element) categoryPath).hasChildren()) {
            categories = new ArrayList<Category>();
            for(Element child : ((Element) categoryPath).getChildren()) {
                categories.add(new Category(child));
            }
        }       
        return categories;
    }

    /**
     * Returns the "customer review count" property associated with this product.
     * 
     * @return Number of customer reviews associated with this product 
     */
    public int getCustomerReviewCount() {
        String customerReviewCount = (String) getField("customerReviewCount");
        return Integer.valueOf(customerReviewCount);
    }

    /**
     * Returns the "customer review average" property associated with this product.
     * 
     * @return Average customer review rating for this product
     */
    public float getCustomerReviewAverage() {
        String customerReviewAverage = (String) getField("customerReviewAverage");
        return Float.valueOf(customerReviewAverage);
    }

    /**
     * Returns the "format" property associated with this product.
     * 
     * @return Format of this product
     */
    public String getFormat() {
        return (String) getField("format");
    }

    /**
     * Returns the "free shipping" property associated with this product.
     * 
     * @return Does this product qualify for free shipping?
     */
    public boolean hasFreeShipping() {
        String freeShipping = (String) getField("freeShipping");
        return Boolean.valueOf(freeShipping);
    }

    /**
     * Returns the "in-store availability" property associated with this product.
     * 
     * @return Is this product generally available in Best Buy stores?
     */
    public boolean hasInStoreAvailability() {
        String inStoreAvailability = (String) getField("inStoreAvailability");
        return Boolean.valueOf(inStoreAvailability);
    }

    /**
     * Returns the "in-store availability text" property associated with this product.
     * 
     * @return Text describing in-store availability for this product
     */
    public String getInStoreAvailabilityText() {
        return (String) getField("inStoreAvailabilityText");
    }

    /**
     * Returns the "in-store availability update date" property associated with this product.
     * 
     * @return Date that this product's in-store availability was last updated
     */
    public String getInStoreAvailabilityUpdateDate() {
        return (String) getField("inStoreAvailabilityUpdateDate");
    }

    /**
     * Returns the "item update" property associated with this product.
     * 
     * @return Date that this product was last updated
     */
    public String getItemUpdateDate() {
        return (String) getField("itemUpdateDate");
    }

    /**
     * Returns the "online availability" property associated with this product.
     * 
     * @return Is this product available online through bestbuy.com?
     */
    public boolean getOnlineAvailability() {
        String onlineAvailability = (String) getField("onlineAvailability");
        return Boolean.valueOf(onlineAvailability);
    }

    /**
     * Returns the "online availability text" property associated with this product.
     * 
     * @return Text describing online availability for this product
     */
    public String getOnlineAvailabilityText() {
        return (String) getField("onlineAvailabilityText");
    }

    /**
     * Returns the "online availability update date" property associated with this product.
     * 
     * @return Date that this product's online availability was last updated
     */
    public String getOnlineAvailabilityUpdateDate() {
        return (String) getField("onlineAvailabilityUpdateDate");
    }

    /**
     * Returns the "print only" property associated with this product.
     * 
     * @return Is this product print only?
     */
    public boolean isPrintOnly() {
        String printOnly = (String) getField("printOnly");
        return Boolean.valueOf(printOnly);
    }

    /**
     * Returns the "release date" property associated with this product.
     * 
     * @return Release date for this product
     */
    public String getReleaseDate() {
        return (String) getField("releaseDate");
    }

    /**
     * Returns the "shipping cost" property associated with this product.
     * 
     * @return Shipping cost for this product
     */
    public float getShippingCost() {
        return Float.valueOf((String) getField("shippingCost"));
    }

    /**
     * Returns the "special order" property associated with this product.
     * 
     * @return Is this product a special order?
     */
    public boolean isSpecialOrder() {
        return Boolean.valueOf((String) getField("specialOrder"));
    }

    /**
     * Returns the "short description" property associated with this product.
     * 
     * @return Short description of this product
     */
    public String getShortDescription() {
        return (String) getField("shortDescription");
    }

    /**
     * Returns the "class" property associated with this product.
     * 
     * @return Class of this product
     */
    public String getProductClass() {
        return (String) getField("class");
    }

    /**
     * Returns the "" property associated with this product.
     * 
     * @return Class ID for this product
     */
    public int getProductClassId() {
        return Integer.valueOf((String) getField("classId"));
    }

    /**
     * Returns the "subclass" property associated with this product.
     * 
     * @return Subclass of this product
     */
    public String getSubclass() {
        return (String) getField("subclass");
    }

    /**
     * Returns the "subclass ID" property associated with this product.
     * 
     * @return Subclass ID of this product
     */
    public int getSubclassId() {
        return Integer.valueOf((String) getField("subclassId"));
    }

    /**
     * Returns the "department" property associated with this product.
     * 
     * @return Department of this product
     */
    public String getDepartment() {
        return (String) getField("department");
    }

    /**
     * Returns the "department ID" property associated with this product.
     * 
     * @return ID of this product's department
     */
    public int getDepartmentId() {
        return Integer.valueOf((String) getField("departmentId"));
    }

    /**
     * Returns the "description" property associated with this product.
     * 
     * @return Description of this product
     */
    public String getDescription() {
        return (String) getField("description");
    }

    /**
     * Returns the "manufacturer" property associated with this product.
     * 
     * @return Manufacturer of this product
     */
    public String getManufacturer() {
        return (String) getField("manufacturer");
    }

    /**
     * Returns the "model number" property associated with this product.
     * 
     * @return Model number of this product
     */
    public String getModelNumber() {
        return (String) getField("modelNumber");
    }

    /**
     * Returns the "image" property associated with this product.
     * 
     * @return Image URL for this product
     */
    public String getImage() {
        return (String) getField("image");
    }

    /**
     * Returns the "large front image" property associated with this product.
     * 
     * @return Large front image URL for this product
     */
    public String getLargeFrontImage() {
        return (String) getField("largeFrontImage");
    }

    /**
     * Returns the "medium image" property associated with this product.
     * 
     * @return Medium image URL for this product
     */
    public String getMediumImage() {
        return (String) getField("mediumImage");
    }

    /**
     * Returns the "thumbnail image" property associated with this product.
     * 
     * @return Thumbnail image URL for this product
     */
    public String getThumbnailImage() {
        return (String) getField("thumbnailImage");
    }

    /**
     * Returns the "large image" property associated with this product.
     * 
     * @return Large image URL for this product
     */
    public String getLargeImage() {
        return (String) getField("largeImage");
    }

    /**
     * Returns the "alternative views image" property associated with this product.
     * 
     * @return Alternative-views image URL for this product
     */
    public String getAlternateViewsImage() {
        return (String) getField("alternateViewsImage");
    }

    /**
     * Returns the "angle" property associated with this product.
     * 
     * @return Angle image URL for this product
     */
    public String getAngleImage() {
        return (String) getField("angleImage");
    }

    /**
     * Returns the "back-view image" property associated with this product.
     * 
     * @return Back-view image URL for this product
     */
    public String getBackViewImage() {
        return (String) getField("backViewImage");
    }

    /**
     * Returns the "energy guide image" property associated with this product.
     * 
     * @return Energy guide image URL for this product
     */
    public String getEnergyGuideImage() {
        return (String) getField("energyGuideImage");
    }

    /**
     * Returns the "left-view image" property associated with this product.
     * 
     * @return Left-view image URL for this product
     */
    public String getLeftViewImage() {
        return (String) getField("leftViewImage");
    }

    /**
     * Returns the "accessories image" property associated with this product.
     * 
     * @return Accessories image URL for this product
     */
    public String getAccessoriesImage() {
        return (String) getField("accessoriesImage");
    }

    /**
     * Returns the "remote control image" property associated with this product.
     * 
     * @return Remote control image URL for this product
     */
    public String getRemoteControlImage() {
        return (String) getField("remoteControlImage");
    }

    /**
     * Returns the "right-view image" property associated with this product.
     * 
     * @return Right-view image URL for this product
     */
    public String getRightViewImage() {
        return (String) getField("rightViewImage");
    }

    /**
     * Returns the "top-view image" property associated with this product.
     * 
     * @return Top-view image URL for this product
     */
    public String getTopViewImage() {
        return (String) getField("topViewImage");
    }

    /**
     * Returns the "color" property associated with this product.
     * 
     * @return Color of this product
     */
    public String getColor() {
        return (String) getField("color");
    }

    /**
     * Returns the "depth" property associated with this product.
     * 
     * @return Depth of this product
     */
    public String getDepth() {
        return (String) getField("depth");
    }

    /**
     * Returns the "dollar savings" property associated with this product.
     * 
     * @return Dollar savings associated with this product
     */
    public float getDollarSavings() {
        return Float.valueOf((String) getField("dollarSavings"));
    }

    /**
     * Returns the "height" property associated with this product.
     * 
     * @return Height of this product
     */
    public String getHeight() {
        return (String) getField("height");
    }

    /**
     * Returns the "navigability" property associated with this product.
     * 
     * @return Navigability of this product
     */
    public String getNavigability() {
        return (String) getField("navigability");
    }

    /**
     * Returns the "orderable" property associated with this product.
     * 
     * @return Orderable status of this product
     */
    public String getOrderable() {
        return (String) getField("orderable");
    }

    /**
     * Returns the "weight" property associated with this product.
     * 
     * @return Weight of this product
     */
    public float getWeight() {
        return Float.valueOf((String) getField("weight"));
    }

    /**
     * Returns the "shipping weight" property associated with this product.
     * 
     * @return Shipping weight of this product
     */
    public float getShippingWeight() {
        return Float.valueOf((String) getField("shippingWeight"));
    }

    /**
     * Returns the "width" property associated with this product.
     * 
     * @return Width of this product
     */
    public String getWidth() {
        return (String) getField("width");
    }

    /**
     * Returns the "warranty labor" property associated with this product.
     * 
     * @return Warranty labor associated with this product
     */
    public String getWarrantyLabor() {
        return (String) getField("warrantyLabor");
    }

    /**
     * Returns the "warranty parts" property associated with this product.
     * 
     * @return Warranty parts associated with this product
     */
    public String getWarrantyParts() {
        return (String) getField("warrantyParts");
    }

    /**
     * Returns the "long description" property associated with this product.
     * 
     * @return Long description of this product
     */
    public String getLongDescription() {
        return (String) getField("longDescription");
    }

    /**
     * Returns the "details" property associated with this product.
     * 
     * @return Details associated with this product
     */
    public List<String> getDetails() {
        List<String> details = new ArrayList<String>();
        Object field = getField("details");
        if(field instanceof Element && ((Element) field).hasChildren()) {
            for(Element example : ((Element) field).getChildren()) {
                details.add((String) example.getValue());
            }
        }
        return details;
    }
    
    /**
     * The "details" property has two children ie. "name" property
     * and the "value" property. This method returns a Map with the 
     * "name" property value as key and the "value" property as the
     * value
     * 
     * @return returns Map with "name" property as Key and "value"
     * property as Value.
     * @author Roy Manzi Tumubweinee, Sept. 29, 2012, Manzia Corporation
     */
    public Map<String, String> getDetailsMap() {
    	Map<String, String> detailsMap = Collections.synchronizedMap(new HashMap<String, String>());
    	assert detailsMap != null;
    	Object field = getField("details");
    	if (field instanceof Element && ((Element) field).hasChildren()) {
    		for (Element example : ((Element) field).getChildren()) {
    			
    			// In this case, example corresponds to the "detail" property
    			// which also has children
    			if (example.hasChildren() && example.getChildren().size() == 2) {
    				String detailKey = new String();
    				String detailValue = new String();
    				for (Element nameValues : example.getChildren()) {
    					if (nameValues.getName().equalsIgnoreCase("name")) {
    						detailKey = (String)nameValues.getValue();
    					}
    					if (nameValues.getName().equalsIgnoreCase("value")) {
    						detailValue = (String)nameValues.getValue();
    					}
    				}
    				if (!detailKey.isEmpty() && !detailValue.isEmpty()) {
    					detailsMap.put(detailKey, detailValue);
    				}    				
    			}
    		}
    	}
    	return detailsMap;
    }

    /**
     * Returns the "features" property associated with this product.
     * 
     * @return Features associated with this product
     */
    public List<String> getFeatures() {
        List<String> features = new ArrayList<String>();
        Object field = getField("features");
        if(field instanceof Element && ((Element) field).hasChildren()) {
            for(Element example : ((Element) field).getChildren()) {
                features.add((String) example.getValue());
            }
        }
        return features;
    }

    /**
     * Returns the "offers" property associated with this product.
     * 
     * @return Offers associated with this product
     */
    public List<Offer> getOffers() {
        Object field = getField("offers");
        List<Offer> offers = new ArrayList<Offer>();
        if(null != field && field instanceof Element && ((Element) field).hasChildren()) {
            for(Element child : ((Element) field).getChildren()) {
                offers.add(new Offer(child));
            }
        }       
        return offers;
    }

    /**
     * Returns the "related items" property associated with this product.
     * 
     * @return Items related to this product
     */
    public List<RelatedItem> getRelated() {
        Object field = getField("related");
        List<RelatedItem> items = new ArrayList<RelatedItem>();
        if(null != field && field instanceof Element && ((Element) field).hasChildren()) {
            for(Element child : ((Element) field).getChildren()) {
                items.add(new RelatedItem(child));
            }
        }
        return items;
    }
}
