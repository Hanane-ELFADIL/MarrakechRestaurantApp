package com.example.marrakechrestaurantapp.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Commande {

    private String idCommande;
    private String clientId;
    private String clientName;
    private String restaurantId;
    private String restaurantName;

    private Map<String, Item> itemsMap; // Map si Firebase stocke un objet
    private List<Item> itemsList;       // List si Firebase stocke une liste
    private double total;
    private String status;
    private String date;
    private String paymentMethod;

    public Commande() {}

    // ===== Getters =====
    public String getIdCommande() { return idCommande; }
    public String getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    private List<Item> items; // 🔹 ajoute si pas déjà présent

    public String getPaymentMethod() { return paymentMethod; }

    // Unified getter pour les items
    public List<Item> getItems() {
        List<Item> combined = new ArrayList<>();
        if (itemsList != null) combined.addAll(itemsList);
        if (itemsMap != null) combined.addAll(itemsMap.values());
        return combined;
    }

    // ===== Setters =====
    public void setIdCommande(String idCommande) { this.idCommande = idCommande; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setTotal(double total) { this.total = total; }
    public void setStatus(String status) { this.status = status; }
    public void setDate(String date) { this.date = date; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    // Firebase setters
    public void setItemsMap(Map<String, Item> itemsMap) { this.itemsMap = itemsMap; }
    public void setItemsList(List<Item> itemsList) { this.itemsList = itemsList; }

    public void setItems(List<Item> itemsList) {
        this.items = itemsList;
    }
//    public List<Item> getItems() {
//        return items;
//    }


}
