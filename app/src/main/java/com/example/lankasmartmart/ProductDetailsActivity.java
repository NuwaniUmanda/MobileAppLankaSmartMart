package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    ImageView ivProduct, btnBack;
    TextView tvProductName, tvPrice, tvCategory, tvDescription, tvStockStatus;
    Button btnAddToCart, btnGoToCart;

    DatabaseHelper dbHelper;
    int userId;

    // Product data received from intent
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
        ivProduct      = findViewById(R.id.ivProduct);
        tvProductName  = findViewById(R.id.tvProductName);
        tvPrice        = findViewById(R.id.tvPrice);
        tvCategory     = findViewById(R.id.tvCategory);
        tvDescription  = findViewById(R.id.tvDescription);
        tvStockStatus  = findViewById(R.id.tvStockStatus);
        btnAddToCart   = findViewById(R.id.btnAddToCart);
        btnGoToCart    = findViewById(R.id.btnGoToCart);

        // Populate UI
        ivProduct.setImageResource(productImage);
        tvProductName.setText(productName);
        tvPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", productPrice));
        tvCategory.setText(productCategory);
        tvDescription.setText(productDescription);

        boolean inStock = productStock > 0;
        tvStockStatus.setText(inStock
                ? "In Stock (" + productStock + " available)"
                : "Out of Stock");
        tvStockStatus.setTextColor(getResources().getColor(
                inStock ? android.R.color.holo_green_dark : android.R.color.holo_red_dark, null));

        btnAddToCart.setEnabled(inStock);
        btnAddToCart.setAlpha(inStock ? 1.0f : 0.5f);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Add to Cart
        btnAddToCart.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(this, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
                return;
            }
            // addToCart(userId, productId, quantity)
            dbHelper.addToCart(userId, productId, 1);
            Toast.makeText(this, productName + " added to cart!", Toast.LENGTH_SHORT).show();

            // Show Go to Cart button after adding
            btnGoToCart.setVisibility(android.view.View.VISIBLE);
        });

        // Go to Cart → CartActivity
        btnGoToCart.setOnClickListener(v ->
                startActivity(new Intent(ProductDetailsActivity.this, CartActivity.class)));
    }
}