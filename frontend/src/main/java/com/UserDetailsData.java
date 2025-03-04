package com;

public class UserDetailsData {
    // The data to be shared in SearchProductsController
    private String selectedUsername;

    // Singleton instance
    private static final UserDetailsData instance = new UserDetailsData();

    // Private constructor to prevent external instantiation
    private UserDetailsData() {
    }

    // Method to get the singleton instance
    public static UserDetailsData getInstance() {
        return instance;
    }

    public void setSelectedUsername(String selectedUsername) {
        this.selectedUsername = selectedUsername;
    }

    public String getSelectedUsername() {
        return selectedUsername;
    }
}