package com.example.marrakechrestaurantapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.marrakechrestaurantapp.R;
import com.example.marrakechrestaurantapp.models.Commande;
import com.example.marrakechrestaurantapp.models.Item;

import java.util.List;

public class CommandesAdapter extends RecyclerView.Adapter<CommandesAdapter.CommandeViewHolder> {

    private List<Commande> commandesList;
    private OnStatusChangeListener listener;

    public interface OnStatusChangeListener {
        void onStatusChange(Commande commande);
    }

    public CommandesAdapter(List<Commande> commandesList, OnStatusChangeListener listener) {
        this.commandesList = commandesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_commande, parent, false);
        return new CommandeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandeViewHolder holder, int position) {
        Commande commande = commandesList.get(position);

        holder.tvCommandeId.setText("#" + commande.getIdCommande());
        holder.tvClientName.setText(commande.getClientName() != null ? commande.getClientName() : "Client inconnu");
        holder.tvDate.setText(commande.getDate() != null ? commande.getDate() : "Date inconnue");

        List<Item> items = commande.getItems();
        if (items != null && !items.isEmpty()) {
            StringBuilder itemsText = new StringBuilder();
            for (Item item : items) {
                itemsText.append("• ")
                        .append(item.getQuantite())
                        .append(" x ")
                        .append(item.getNom())
                        .append(" (")
                        .append(String.format("%.2f", item.getPrix()))
                        .append(" DH)\n");
            }
            holder.tvItems.setText(itemsText.toString().trim());
            holder.tvItems.setVisibility(View.VISIBLE);
        } else {
            holder.tvItems.setVisibility(View.GONE);
        }

        holder.tvPaymentMethod.setText(getPaymentMethodIcon(commande.getPaymentMethod()));
        holder.tvTotal.setText(String.format("%.2f DH", commande.getTotal()));

        setStatusBadge(holder.tvStatus, commande.getStatus());

        holder.btnChangeStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusChange(commande);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commandesList != null ? commandesList.size() : 0;
    }

    private void setStatusBadge(TextView tvStatus, String status) {
        if (status == null) status = "ATTENTE";

        switch (status) {
            case "ATTENTE":
                tvStatus.setText("⏳ En attente");
                tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                break;
            case "EN_COURS":
                tvStatus.setText("🔥 En cours");
                tvStatus.setBackgroundColor(Color.parseColor("#2196F3"));
                break;
            case "PRETE":
                tvStatus.setText("✅ Prête");
                tvStatus.setBackgroundColor(Color.parseColor("#9C27B0"));
                break;
            case "LIVREE":
                tvStatus.setText("🚚 Livrée");
                tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case "ANNULEE":
                tvStatus.setText("❌ Annulée");
                tvStatus.setBackgroundColor(Color.parseColor("#F44336"));
                break;
            default:
                tvStatus.setText(status);
                tvStatus.setBackgroundColor(Color.GRAY);
        }
    }

    private String getPaymentMethodIcon(String method) {
        if (method == null) return "💵 Cash";
        switch (method.toLowerCase()) {
            case "card":
            case "carte": return "💳 Carte";
            case "cash":
            case "espèces": return "💵 Cash";
            case "paypal": return "🅿️ PayPal";
            default: return "💰 " + method;
        }
    }

    public void updateCommandes(List<Commande> newCommandesList) {
        this.commandesList = newCommandesList;
        notifyDataSetChanged();
    }

    static class CommandeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCommandeId, tvStatus, tvClientName, tvDate, tvItems, tvPaymentMethod, tvTotal;
        Button btnChangeStatus;

        public CommandeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardCommande);
            tvCommandeId = itemView.findViewById(R.id.tvCommandeId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
        }
    }
}
