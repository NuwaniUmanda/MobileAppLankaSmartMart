package com.example.lankasmartmart;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final Context context;
    private final List<Product> products;
    private final OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> products, OnProductClickListener listener) {
        this.context  = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", product.getPrice()));
        holder.ivProduct.setImageResource(product.getImageResource());

        // Random rating between 3.0 and 5.0 for display
        float rating = 3.0f + (product.getId() % 5) * 0.5f;
        holder.ratingBar.setRating(rating);

        // Stock badge
        if (product.isInStock()) {
            holder.tvStockBadge.setText("In Stock");
            holder.tvStockBadge.setBackgroundResource(R.drawable.badge_in_stock);
            holder.btnAddToCart.setEnabled(true);
            holder.btnAddToCart.setAlpha(1.0f);
            holder.btnMinus.setEnabled(true);
            holder.btnPlus.setEnabled(true);
        } else {
            holder.tvStockBadge.setText("Out of Stock");
            holder.tvStockBadge.setBackgroundResource(R.drawable.badge_out_of_stock);
            holder.btnAddToCart.setEnabled(false);
            holder.btnAddToCart.setAlpha(0.5f);
            holder.btnMinus.setEnabled(false);
            holder.btnPlus.setEnabled(false);
        }

        // Quantity controls
        final int[] quantity = {1};
        holder.tvQuantity.setText(String.valueOf(quantity[0]));

        holder.btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                holder.tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            holder.tvQuantity.setText(String.valueOf(quantity[0]));
        });

        // Card click → ProductDetailsActivity
        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));

        // Add to Cart
        holder.btnAddToCart.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("USER_ID", -1);

            if (userId == -1) {
                Toast.makeText(context, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            dbHelper.addToCart(userId, product.getId(), quantity[0]);
            dbHelper.close();

            Toast.makeText(context, quantity[0] + "x " + product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();

            // Reset quantity after adding
            quantity[0] = 1;
            holder.tvQuantity.setText("1");
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct, btnMinus, btnPlus;
        TextView tvProductName, tvPrice, tvStockBadge, tvQuantity;
        Button btnAddToCart;
        RatingBar ratingBar;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct     = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice       = itemView.findViewById(R.id.tvPrice);
            tvStockBadge  = itemView.findViewById(R.id.tvStockBadge);
            tvQuantity    = itemView.findViewById(R.id.tvQuantity);
            btnAddToCart  = itemView.findViewById(R.id.btnAddToCart);
            btnMinus      = itemView.findViewById(R.id.btnMinus);
            btnPlus       = itemView.findViewById(R.id.btnPlus);
            ratingBar     = itemView.findViewById(R.id.ratingBar);
        }
    }
}