package com.example.lankasmartmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;
    private final CartUpdateListener listener;
    private final DatabaseHelper databaseHelper;
    private final int userId;

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartUpdateListener listener,
                       DatabaseHelper databaseHelper, int userId) {
        this.context        = context;
        this.cartItems      = cartItems;
        this.listener       = listener;
        this.databaseHelper = databaseHelper;
        this.userId         = userId;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.productName.setText(item.getProductName());
        holder.productPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", item.getPrice()));
        holder.quantity.setText(String.valueOf(item.getQuantity()));

        // Rating
        float rating = 3.0f + (item.getProductId() % 5) * 0.5f;
        holder.itemRating.setRating(rating);

        // Resolve image
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            int resId = context.getResources().getIdentifier(
                    imageUrl, "drawable", context.getPackageName());
            holder.productImage.setImageResource(resId != 0 ? resId : R.drawable.ic_shopping_cart);
        } else {
            holder.productImage.setImageResource(R.drawable.ic_shopping_cart);
        }

        // Checkbox (visual only)
        holder.itemCheckbox.setChecked(true);

        // Plus
        holder.btnPlus.setOnClickListener(v -> {
            databaseHelper.updateCartQuantity(userId, item.getProductId(), item.getQuantity() + 1);
            if (listener != null) listener.onCartUpdated();
        });

        // Minus
        holder.btnMinus.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            databaseHelper.updateCartQuantity(userId, item.getProductId(), Math.max(newQty, 0));
            if (listener != null) listener.onCartUpdated();
        });

        // Delete
        holder.btnDelete.setOnClickListener(v -> {
            databaseHelper.updateCartQuantity(userId, item.getProductId(), 0);
            if (listener != null) listener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox itemCheckbox;
        ImageView productImage, btnPlus, btnMinus, btnDelete;
        TextView productName, productPrice, quantity;
        RatingBar itemRating;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckbox  = itemView.findViewById(R.id.itemCheckbox);
            productImage  = itemView.findViewById(R.id.productImage);
            productName   = itemView.findViewById(R.id.productName);
            productPrice  = itemView.findViewById(R.id.productPrice);
            quantity      = itemView.findViewById(R.id.quantity);
            itemRating    = itemView.findViewById(R.id.itemRating);
            btnPlus       = itemView.findViewById(R.id.btnPlus);
            btnMinus      = itemView.findViewById(R.id.btnMinus);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
        }
    }
}