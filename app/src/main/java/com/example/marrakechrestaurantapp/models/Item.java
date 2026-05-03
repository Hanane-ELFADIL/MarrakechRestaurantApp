package com.example.marrakechrestaurantapp.models;

public class Item {

    private String platId;
    private String nom;
    private double prix;      // 🔴 DOIT s'appeler "prix"
    private int quantite;

    public Item() {}

    // ===== Getters =====
    public String getPlatId() {
        return platId;
    }

    public String getNom() {
        return nom;
    }

    public double getPrix() {
        return prix;
    }

    public int getQuantite() {
        return quantite;
    }

    // Calculé
    public double getPrixTotal() {
        return prix * quantite;
    }

    // ===== Setters =====
    public void setPlatId(String platId) {
        this.platId = platId;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
}
