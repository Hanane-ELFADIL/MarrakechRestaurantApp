package com.example.marrakechrestaurantapp.models;

public class Plat {
    private String platId;
    private String restaurantId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private boolean available;

    public Plat() {}

    public Plat(String platId, String restaurantId, String name,
                String description, double price, String imageUrl, boolean available) {
        this.platId = platId;
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    // Getters et Setters
    public String getPlatId() { return platId; }
    public void setPlatId(String platId) { this.platId = platId; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}