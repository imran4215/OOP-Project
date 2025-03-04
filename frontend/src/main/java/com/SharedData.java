package com;

public class SharedData {
    // The data to be shared in SearchProductsController
    private String searchQuery;

    // The data to be shared in ProductDetailsController
    private String selectedProduct;

    private String selectedUsername = "imran4215";
    private String selectedOrderId = "67c595cae539291a0e585f1f";

    // Singleton instance
    private static final SharedData instance = new SharedData();

    // Private constructor to prevent external instantiation
    private SharedData() {
    }

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

    public void setSelectedUsername(String selectedUsername) {
        this.selectedUsername = selectedUsername;
    }

    public void setSelectedOrderId(String selectedOrderId) {
        this.selectedOrderId = selectedOrderId;
    }

    public String getSelectedUsername() {
        return selectedUsername;
    }

    public String getSelectedOrderId() {
        return selectedOrderId;
    }
}