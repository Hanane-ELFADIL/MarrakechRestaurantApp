package com.example.marrakechrestaurantapp;

import java.io.Serializable;

public class Restaurant implements Serializable {

    private String restaurantId;
    private String name;
    private String category;
    private String description;
    private String imageName;   // ✅ Nom du drawable (correspond à Firebase)
    private int deliveryTime;
    private double rating;
    private boolean isOpen;

    // Constructeur vide requis par Firebase
    public Restaurant() {
    }

    public Restaurant(String restaurantId, String name, String category,
                      String description, String imageName,
                      int deliveryTime, double rating, boolean isOpen) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.imageName = imageName;
        this.deliveryTime = deliveryTime;
        this.rating = rating;
        this.isOpen = isOpen;
    }

    // =================== GETTERS ===================

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getImageName() {
        return imageName;
    }

    // ✅ CORRECTION : Alias pour getImageName() - Retourne imageName au lieu de null
    public String getImageUrl() {
        return imageName;  // ✅ Pointe vers imageName
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }

    public double getRating() {
        return rating;
    }

    public boolean isOpen() {
        return isOpen;
    }

    // =================== SETTERS ===================

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    // ✅ CORRECTION : Alias pour setImageName()
    public void setImageUrl(String imageUrl) {
        this.imageName = imageUrl;  // ✅ Définit imageName
    }

    public void setDeliveryTime(int deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}