package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    ImageView ivProduct, btnBack, btnWishlist, btnShare;
    TextView tvProductName, tvPrice, tvTopCategory, tvDescription, tvStockBadge;
    RatingBar ratingBar;
    Button btnAddToCart, btnGoToCart;

    DatabaseHelper dbHelper;
    int userId;

    int productId, productStock, productImage;
    String productName, productCategory, productDescription;
    double productPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);

        // Read product data from intent
        Intent intent      = getIntent();
        productId          = intent.getIntExtra("PRODUCT_ID", 0);
        productName        = intent.getStringExtra("PRODUCT_NAME");
        productPrice       = intent.getDoubleExtra("PRODUCT_PRICE", 0.0);
        productCategory    = intent.getStringExtra("PRODUCT_CATEGORY");
        productDescription = intent.getStringExtra("PRODUCT_DESCRIPTION");
        productStock       = intent.getIntExtra("PRODUCT_STOCK", 0);
        productImage       = intent.getIntExtra("PRODUCT_IMAGE", R.drawable.ic_shopping_cart);

        // Init views
        btnBack        = findViewById(R.id.btnBack);
        btnWishlist    = findViewById(R.id.btnWishlist);
        btnShare       = findViewById(R.id.btnShare);
        ivProduct      = findViewById(R.id.ivProduct);
        tvProductName  = findViewById(R.id.tvProductName);
        tvPrice        = findViewById(R.id.tvPrice);
        tvTopCategory  = findViewById(R.id.tvTopCategory);
        tvDescription  = findViewById(R.id.tvDescription);
        tvStockBadge   = findViewById(R.id.tvStockBadge);
        ratingBar      = findViewById(R.id.ratingBar);
        btnAddToCart   = findViewById(R.id.btnAddToCart);
        btnGoToCart    = findViewById(R.id.btnGoToCart);

        // Populate UI
        ivProduct.setImageResource(productImage);
        tvProductName.setText(productName);
        tvPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", productPrice));
        tvTopCategory.setText(productCategory);
        tvDescription.setText(productDescription);

        // Rating (same logic as adapter)
        float rating = 3.0f + (productId % 5) * 0.5f;
        ratingBar.setRating(rating);

        // Stock badge
        boolean inStock = productStock > 0;
        if (!inStock) {
            tvStockBadge.setVisibility(View.VISIBLE);
        } else {
            tvStockBadge.setVisibility(View.GONE);
        }

        // Disable Add to Cart if out of stock
        btnAddToCart.setEnabled(inStock);
        btnAddToCart.setAlpha(inStock ? 1.0f : 0.5f);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Wishlist (placeholder)
        btnWishlist.setOnClickListener(v ->
                Toast.makeText(this, "Added to wishlist!", Toast.LENGTH_SHORT).show());

        // Share (placeholder)
        btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Share feature coming soon!", Toast.LENGTH_SHORT).show());

        // Add to Cart
        btnAddToCart.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(this, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.addToCart(userId, productId, 1);
            Toast.makeText(this, productName + " added to cart!", Toast.LENGTH_SHORT).show();
            btnGoToCart.setVisibility(View.VISIBLE);
        });

        // Buy Now → CartActivity
        btnGoToCart.setOnClickListener(v ->
                startActivity(new Intent(ProductDetailsActivity.this, CartActivity.class)));

        // Bottom Navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });
        findViewById(R.id.navCategories).setOnClickListener(v ->
                startActivity(new Intent(this, ProductsActivity.class)));
        findViewById(R.id.navCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}