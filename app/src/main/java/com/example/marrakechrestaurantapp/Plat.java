package com.example.marrakechrestaurantapp;

import java.io.Serializable;

/**
 * Classe Plat - Compatible avec la structure Firebase
 */
public class Plat implements Serializable {

    private String platId;
    private String name;
    private String description;
    private double price;
    private String imageName;
    private String category;
    private String restaurantId;
    private boolean available;

    // Constructeur vide OBLIGATOIRE pour Firebase
    public Plat() {
    }

    // Constructeur complet (8 paramètres)
    public Plat(String platId, String name, String description, double price,
                String imageName, String category, String restaurantId, boolean available) {
        this.platId = platId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageName = imageName;
        this.category = category;
        this.restaurantId = restaurantId;
        this.available = available;
    }

    // ✅ CORRECTION : Constructeur à 7 paramètres (utilisé dans RestaurantDetailActivity et FavoriteActivity)
    public Plat(String platId, String name, String description, Double price,
                String imageName, String category, String restaurantId) {
        this.platId = platId;           // ✅ Définir platId
        this.name = name;
        this.description = description;
        this.price = (price != null) ? price : 0.0;
        this.imageName = imageName;
        this.category = category;
        this.restaurantId = restaurantId;
        this.available = true;          // ✅ Définir disponible par défaut
    }

    // =================== GETTERS ===================

    public String getPlatId() {
        return platId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageName() {
        return imageName;
    }

    public String getCategory() {
        return category;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public boolean isAvailable() {
        return available;
    }

    // Alias pour compatibilité
    public String getId() {
        return platId;
    }

    public String getImageUrl() {
        return imageName;
    }

    // =================== SETTERS ===================

    public void setPlatId(String platId) {
        this.platId = platId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Alias pour compatibilité
    public void setId(String id) {
        this.platId = id;
    }

    public void setImageUrl(String imageUrl) {
        this.imageName = imageUrl;
    }

    @Override
    public String toString() {
        return "Plat{" +
                "platId='" + platId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                '}';
    }
}