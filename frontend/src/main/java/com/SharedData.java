package com;

public class SharedData {
    // The data to be shared
    private String searchQuery;

    // Singleton instance
    private static final SharedData instance = new SharedData();

    // Private constructor to prevent external instantiation
    private SharedData() {}

    // Method to get the singleton instance
    public static SharedData getInstance() {
        return instance;
    }

    // Getter for searchQuery
    public String getSearchQuery() {
        return searchQuery;
    }

    // Setter for searchQuery
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}