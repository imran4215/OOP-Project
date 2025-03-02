package com;

public class UserData {
    private String userName;
    private int totalOrders;
    private int pendingOrders;
    private int canceledOrders;
    private int deliveredOrders;

    // Constructor
    public UserData(String userName, int totalOrders, int pendingOrders, int canceledOrders, int deliveredOrders) {
        this.userName = userName;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.canceledOrders = canceledOrders;
        this.deliveredOrders = deliveredOrders;
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public int getCancelledOrders() {
        return canceledOrders;
    }

    public int getDeliveredOrders() {
        return deliveredOrders;
    }
}
