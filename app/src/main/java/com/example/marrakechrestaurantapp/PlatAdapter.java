package com.example.marrakechrestaurantapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class PlatAdapter extends RecyclerView.Adapter<PlatAdapter.PlatViewHolder> {

    private Context context;
    private List<Plat> platList;
    private FavoriteManager favoriteManager;

    public PlatAdapter(Context context, List<Plat> platList) {
        this.context = context;
        this.platList = platList;
        this.favoriteManager = FavoriteManager.getInstance();
    }

    @NonNull
    @Override
    public PlatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plat, parent, false);
        return new PlatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatViewHolder holder, int position) {
        Plat plat = platList.get(position);

        holder.tvName.setText(plat.getName());
        holder.tvDescription.setText(plat.getDescription());
        holder.tvPrice.setText("💰 " + plat.getPrice() + " DH");
        holder.tvCategory.setText(plat.getCategory() != null ? plat.getCategory() : "Plat");

        // Charger l'image avec Glide
        loadImage(holder.ivImage, plat.getImageName());

        // Vérifier si le plat est dans les favoris
        favoriteManager.isPlatFavorite(plat.getPlatId(), isFavorite -> {
            holder.btnFavorite.setText(isFavorite ? "❤️" : "🤍");
        });

        // Gérer le clic sur le bouton favori
        holder.btnFavorite.setOnClickListener(v -> {
            favoriteManager.isPlatFavorite(plat.getPlatId(), isFavorite -> {
                if (isFavorite) {
                    favoriteManager.removePlatFromFavorites(plat.getPlatId(), new FavoriteManager.FavoriteCallback() {
                        @Override
                        public void onSuccess() {
                            holder.btnFavorite.setText("🤍");
                            Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "Erreur: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    favoriteManager.addPlatToFavorites(plat.getPlatId(), new FavoriteManager.FavoriteCallback() {
                        @Override
                        public void onSuccess() {
                            holder.btnFavorite.setText("❤️");
                            Toast.makeText(context, "Ajouté aux favoris ❤️", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "Erreur: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });


        // Gérer le clic sur le bouton d'ajout au panier
        holder.btnAddToCart.setOnClickListener(v -> {
            Toast.makeText(context, plat.getName() + " ajouté au panier", Toast.LENGTH_SHORT).show();

            // Créer un article panier depuis le plat
            ArticlePanier article = ArticlePanier.fromPlat(plat, 1);  // Quantité = 1

            // Ajouter au panier
            PanierManager.getInstance().ajouterArticle(article);

            // Afficher un message
            Toast.makeText(context, plat.getName() + " ajouté au panier !", Toast.LENGTH_SHORT).show();

            // Optionnel : Animer l'icône panier
            animatePanierIcon();
        });
    }

    @Override
    public int getItemCount() {
        return platList.size();
    }

    // Méthode pour charger les images depuis drawable
    private void loadImage(ImageView imageView, String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }

        try {
            int resourceId = context.getResources().getIdentifier(
                    imageName,
                    "drawable",
                    context.getPackageName()
            );

            if (resourceId != 0) {
                Glide.with(context)
                        .load(resourceId)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void animatePanierIcon() {
        // TODO: ajouter animation si nécessaire
    }

    public static class PlatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvDescription, tvPrice, tvCategory;
        TextView btnFavorite, btnAddToCart;

        public PlatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivPlatImage);
            tvName = itemView.findViewById(R.id.tvPlatName);
            tvDescription = itemView.findViewById(R.id.tvPlatDescription);
            tvPrice = itemView.findViewById(R.id.tvPlatPrice);
            tvCategory = itemView.findViewById(R.id.tvPlatCategory);
            btnFavorite = itemView.findViewById(R.id.btnFavoritePlat);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
