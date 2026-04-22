package com.example.marrakechrestaurantapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.R;
import com.example.marrakechrestaurantapp.models.Plat;

import java.util.List;

public class PlatAdapter extends RecyclerView.Adapter<PlatAdapter.ViewHolder> {

    private Context context;
    private List<Plat> plats;
    private OnPlatClickListener listener;

    public interface OnPlatClickListener {
        void onAddToCart(Plat plat);
    }

    public PlatAdapter(Context context, List<Plat> plats, OnPlatClickListener listener) {
        this.context = context;
        this.plats = plats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plat plat = plats.get(position);

        holder.tvName.setText(plat.getName());
        holder.tvDescription.setText(plat.getDescription());
        holder.tvPrice.setText(plat.getPrice() + " DH");

        // Charger l'image depuis drawable
        int resId = context.getResources().getIdentifier(
                plat.getImageUrl(),
                "drawable",
                context.getPackageName()
        );
        if (resId != 0) {
            holder.imgPlat.setImageResource(resId);
        }

        holder.btnAdd.setOnClickListener(v -> listener.onAddToCart(plat));
    }

    @Override
    public int getItemCount() {
        return plats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlat;
        TextView tvName, tvDescription, tvPrice;
        Button btnAdd;

        ViewHolder(View itemView) {
            super(itemView);
            imgPlat = itemView.findViewById(R.id.img_plat);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }
}