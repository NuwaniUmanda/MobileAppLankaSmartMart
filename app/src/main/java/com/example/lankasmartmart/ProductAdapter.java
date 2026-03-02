package com.example.lankasmartmart;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

        // Out of stock handling
        if (!product.isInStock()) {
            holder.tvOutOfStock.setVisibility(View.VISIBLE);
            holder.btnAddToCart.setEnabled(false);
            holder.btnAddToCart.setAlpha(0.5f);
        } else {
            holder.tvOutOfStock.setVisibility(View.GONE);
            holder.btnAddToCart.setEnabled(true);
            holder.btnAddToCart.setAlpha(1.0f);
        }

        // Whole card click → ProductDetailsActivity
        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));

        // Add to Cart button click
        holder.btnAddToCart.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("USER_ID", -1);

            if (userId == -1) {
                Toast.makeText(context, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            dbHelper.addToCart(userId, product.getId(), 1);
            dbHelper.close(); // close after use to avoid resource leak

            Toast.makeText(context, product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvPrice, tvOutOfStock;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct     = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice       = itemView.findViewById(R.id.tvPrice);
            tvOutOfStock  = itemView.findViewById(R.id.tvOutOfStock);
            btnAddToCart  = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}