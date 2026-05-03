package com.example.marrakechrestaurantapp;

public class ArticlePanier {

    private String platId;          // ⚠️ String au lieu de int
    private String nom;
    private double prixUnitaire;
    private int quantite;
    private String imageName;       // ⚠️ Ajouté pour l'affichage

    // Constructeur vide OBLIGATOIRE pour Firebase
    public ArticlePanier() {}

    public ArticlePanier(String platId, String nom, double prixUnitaire, int quantite, String imageName) {
        this.platId = platId;
        this.nom = nom;
        this.prixUnitaire = prixUnitaire;
        this.quantite = quantite;
        this.imageName = imageName;
    }

    // Factory method pour créer depuis un Plat
    public static ArticlePanier fromPlat(Plat plat, int quantite) {
        return new ArticlePanier(
                plat.getPlatId(),
                plat.getName(),
                plat.getPrice(),
                quantite,
                plat.getImageName()
        );
    }

    // Getters
    public String getPlatId() { return platId; }
    public String getNom() { return nom; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public int getQuantite() { return quantite; }
    public String getImageName() { return imageName; }
    public double getPrixTotal() { return prixUnitaire * quantite; }

    // Setters
    public void setPlatId(String platId) { this.platId = platId; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    // Pour compatibilité avec l'ancien code d'Imane
    public String getId() { return platId; }
    public void setId(String id) { this.platId = id; }
}