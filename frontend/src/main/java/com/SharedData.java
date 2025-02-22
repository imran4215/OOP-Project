package com;

public class SharedData {
    // The data to be shared in SearchProductsController
    private String searchQuery;
    // The data to be shared in ProductDetailsController
    private String selectedProduct;

    // Singleton instance
    private static final SharedData instance = new SharedData();

    // Private constructor to prevent external instantiation
    private SharedData() {}

    // Method to get the singleton instance
    public static SharedData getInstance() {
        return instance;
    }

    // Setter for searchQuery in SearchProductsController
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    // Getter for searchQuery in SearchProductsController
    public String getSearchQuery() {
        return searchQuery;
    }

    // Setter for selectedProduct in ProductDetailsController
    public void setSelectedProductData(String productDetails) {
        this.selectedProduct = productDetails;
    }
    // Getter for selectedProduct in ProductDetailsController
    public String getSelectedProductData() {
        return selectedProduct;
    }
}