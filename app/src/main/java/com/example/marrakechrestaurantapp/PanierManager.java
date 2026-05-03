package com.example.marrakechrestaurantapp;

import java.util.ArrayList;
import java.util.List;

public class PanierManager {

    private static PanierManager instance;
    private final List<ArticlePanier> articles = new ArrayList<>();

    private PanierManager() {}

    public static PanierManager getInstance() {
        if (instance == null) {
            instance = new PanierManager();
        }
        return instance;
    }

    // ⚠️ Adapté pour comparer des String
    public void ajouterArticle(ArticlePanier nouvelArticle) {
        boolean articleExiste = false;

        for (ArticlePanier a : articles) {
            if (a.getPlatId().equals(nouvelArticle.getPlatId())) {  // ⚠️ equals() pour String
                a.setQuantite(a.getQuantite() + nouvelArticle.getQuantite());
                articleExiste = true;
                break;
            }
        }

        if (!articleExiste) {
            articles.add(nouvelArticle);
        }
    }

    public List<ArticlePanier> getArticles() {
        return articles;
    }

    public void viderPanier() {
        articles.clear();
    }

    // ⚠️ Méthode utile pour compter les articles
    public int getNombreArticles() {
        int total = 0;
        for (ArticlePanier a : articles) {
            total += a.getQuantite();
        }
        return total;
    }

    // ⚠️ Méthode utile pour calculer le total
    public double getTotal() {
        double total = 0;
        for (ArticlePanier a : articles) {
            total += a.getPrixTotal();
        }
        return total;
    }
}