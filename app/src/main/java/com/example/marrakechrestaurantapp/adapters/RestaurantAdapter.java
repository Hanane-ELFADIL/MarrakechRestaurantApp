package com.example.marrakechrestaurantapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.R;
import com.example.marrakechrestaurantapp.models.Restaurant;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private Context context;
    private List<Restaurant> restaurants;
    private OnRestaurantClickListener listener;

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    public RestaurantAdapter(Context context,
                             List<Restaurant> restaurants,
                             OnRestaurantClickListener listener) {
        this.context = context;
        this.restaurants = restaurants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);

        holder.tvName.setText(restaurant.name);
        holder.tvDescription.setText(restaurant.description);
        holder.tvRating.setText("★ " + restaurant.rating);
        holder.tvDeliveryTime.setText(restaurant.deliveryTime + " min");

        int resId = context.getResources().getIdentifier(
                restaurant.imageUrl,
                "drawable",
                context.getPackageName()
        );

        if (resId != 0) {
            holder.imgRestaurant.setImageResource(resId);
        }

        holder.itemView.setOnClickListener(v -> listener.onRestaurantClick(restaurant));
    }


    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgRestaurant;
        TextView tvName, tvDescription, tvRating, tvDeliveryTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRestaurant = itemView.findViewById(R.id.img_restaurant);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvDeliveryTime = itemView.findViewById(R.id.tv_delivery_time);
        }
    }
}
