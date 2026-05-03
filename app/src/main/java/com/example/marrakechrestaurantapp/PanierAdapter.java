package com.example.marrakechrestaurantapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter pour le RecyclerView
 */
public class PanierAdapter extends RecyclerView.Adapter<PanierAdapter.PanierViewHolder> {

    private List<ArticlePanier> articles;
    private PanierListener listener;
    // Déclaré comme final car il n'est pas censé changer
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    public interface PanierListener {
        void onQuantiteChange(int position, int nouvelleQuantite);
        void onSupprimer(int position);
    }

    public PanierAdapter(List<ArticlePanier> articles, PanierListener listener) {
        this.articles = articles;
        this.listener = listener;
    }

    @Override
    public PanierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article_panier, parent, false); // <-- Si c'est rouge, vérifiez 'item_article_panier'
        return new PanierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PanierViewHolder holder, int position) {
        ArticlePanier article = articles.get(position);

        // Utilisation du formateur monétaire
        holder.textViewNomPlat.setText(article.getNom());
        holder.textViewPrixUnitaire.setText(currencyFormat.format(article.getPrixUnitaire()) + "/u");
        holder.textViewQuantite.setText(String.valueOf(article.getQuantite()));
        holder.textViewPrixTotalLigne.setText(currencyFormat.format(article.getPrixTotal()));

        holder.buttonMoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantiteActuelle = article.getQuantite();
                if (quantiteActuelle > 1) {
                    listener.onQuantiteChange(holder.getAdapterPosition(), quantiteActuelle - 1);
                } else if (quantiteActuelle == 1) {
                    listener.onSupprimer(holder.getAdapterPosition());
                }
            }
        });

        holder.buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nouvelleQuantite = article.getQuantite() + 1;
                listener.onQuantiteChange(holder.getAdapterPosition(), nouvelleQuantite);
            }
        });

        holder.buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSupprimer(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class PanierViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNomPlat;
        TextView textViewPrixUnitaire;
        TextView textViewQuantite;
        TextView textViewPrixTotalLigne;
        Button buttonMoins;
        Button buttonPlus;
        ImageButton buttonRemove;

        public PanierViewHolder(View itemView) {
            super(itemView);
            textViewNomPlat = itemView.findViewById(R.id.textViewNomPlat);
            textViewPrixUnitaire = itemView.findViewById(R.id.textViewPrixUnitaire);
            textViewQuantite = itemView.findViewById(R.id.textViewQuantite);
            textViewPrixTotalLigne = itemView.findViewById(R.id.textViewPrixTotalLigne);
            buttonMoins = itemView.findViewById(R.id.buttonMoins);
            buttonPlus = itemView.findViewById(R.id.buttonPlus);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }
    }
}