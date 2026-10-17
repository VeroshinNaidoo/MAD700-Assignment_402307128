package com.example.a402307128_smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that shows the pantry (one card per ingredient,
 * layout item_ingredient.xml). Edit/delete taps are reported to the
 * fragment through the Listener interface.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface Listener {
        void onEditClicked(Ingredient ingredient);
        void onDeleteClicked(Ingredient ingredient);
    }

    private final Listener listener;
    private List<Ingredient> items = new ArrayList<>();
    private boolean highlightExpiring = true;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Ingredient> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setHighlightExpiring(boolean highlightExpiring) {
        this.highlightExpiring = highlightExpiring;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        final Ingredient item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(Utils.formatQuantity(item.getQuantity()) + " " + item.getUnit());

        String expiry = item.getExpiry();
        if (expiry == null || expiry.isEmpty()) {
            holder.tvExpiry.setVisibility(View.GONE);
        } else {
            boolean expiringSoon = highlightExpiring && Utils.daysUntil(expiry) <= Utils.EXPIRING_DAYS;
            if (expiringSoon) {
                holder.tvExpiry.setText(context.getString(R.string.expiring_soon) + ": " + expiry);
                holder.tvExpiry.setTextColor(ContextCompat.getColor(context, R.color.expiring_text));
            } else {
                holder.tvExpiry.setText("Expires: " + expiry);
                holder.tvExpiry.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
            holder.tvExpiry.setVisibility(View.VISIBLE);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditClicked(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the views of one row so they are looked up only once. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvQuantity;
        final TextView tvExpiry;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ingredient_name);
            tvQuantity = itemView.findViewById(R.id.tv_ingredient_qty);
            tvExpiry = itemView.findViewById(R.id.tv_expiry);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}

