package com.example.lankasmartmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {

    RecyclerView recyclerProducts;
    TextView tvCategoryTitle;
    android.view.View btnBack;
    LinearLayout navHome, navCategories, navCart, navProfile;

    List<Product> productList;
    List<Product> filteredList;
    String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        // Init views
        recyclerProducts = findViewById(R.id.recyclerProducts);
        tvCategoryTitle  = findViewById(R.id.tvCategoryTitle);
        btnBack          = findViewById(R.id.btnBack);
        navHome          = findViewById(R.id.navHome);
        navCategories    = findViewById(R.id.navCategories);
        navCart          = findViewById(R.id.navCart);
        navProfile       = findViewById(R.id.navProfile);

        // Get category from intent
        selectedCategory = getIntent().getStringExtra("CATEGORY");
        if (selectedCategory == null) selectedCategory = "All";
        tvCategoryTitle.setText(selectedCategory.equals("All") ? "All Products" : selectedCategory);

        // Load and filter products
        loadProducts();
        filterByCategory();

        // Setup RecyclerView with 2-column grid
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));

        // Wire up the adapter — product click → ProductDetailsActivity
        ProductAdapter adapter = new ProductAdapter(this, filteredList, product -> {
            Intent intent = new Intent(ProductsActivity.this, ProductDetailsActivity.class);
            intent.putExtra("PRODUCT_ID",          product.getId());
            intent.putExtra("PRODUCT_NAME",        product.getName());
            intent.putExtra("PRODUCT_PRICE",       product.getPrice());
            intent.putExtra("PRODUCT_CATEGORY",    product.getCategory());
            intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());
            intent.putExtra("PRODUCT_STOCK",       product.getStock());
            intent.putExtra("PRODUCT_IMAGE",       product.getImageResource());
            startActivity(intent);
        });
        recyclerProducts.setAdapter(adapter);

        // Back button uses finish() → returns to previous screen naturally
        btnBack.setOnClickListener(v -> finish());

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProductsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Categories nav → show all products
        navCategories.setOnClickListener(v ->
                Toast.makeText(this, "Already browsing categories", Toast.LENGTH_SHORT).show());

        navCart.setOnClickListener(v ->
                startActivity(new Intent(ProductsActivity.this, CartActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(ProductsActivity.this, ProfileActivity.class)));
    }

    private void loadProducts() {
        productList = new ArrayList<>();

        // Groceries
        productList.add(new Product(1, "Basmati Rice - 1kg", 350.00,
                "Groceries", "Premium quality basmati rice", 10, R.drawable.img_rice));
        productList.add(new Product(2, "Imorich French Vanilla - 1L", 1290.00,
                "Groceries", "Rich and creamy French vanilla ice cream", 5, R.drawable.img_ice_cream));
        productList.add(new Product(3, "Munchee Choc Shock - 90g", 300.00,
                "Groceries", "Delicious chocolate biscuits", 20, R.drawable.img_chocolate));
        productList.add(new Product(4, "Tiara Sponge Layer Cake - 310g", 550.00,
                "Groceries", "Soft and fluffy sponge cake", 8, R.drawable.img_cake));

        // Household
        productList.add(new Product(5, "Vim Dishwash Bar", 120.00,
                "Household", "Effective grease-cutting dishwash bar", 50, R.drawable.img_rice));
        productList.add(new Product(6, "Dettol Floor Cleaner", 450.00,
                "Household", "Kills 99.9% of germs", 30, R.drawable.img_rice));

        // Personal Care
        productList.add(new Product(7, "Dove Soap - 100g", 280.00,
                "Personal Care", "Moisturizing beauty bar", 40, R.drawable.img_rice));
        productList.add(new Product(8, "Head & Shoulders Shampoo", 890.00,
                "Personal Care", "Anti-dandruff shampoo 400ml", 15, R.drawable.img_rice));

        // Stationery
        productList.add(new Product(9, "Ballpoint Pen Set", 150.00,
                "Stationery", "Pack of 10 pens", 100, R.drawable.img_rice));
        productList.add(new Product(10, "A4 Notebook", 320.00,
                "Stationery", "200 pages ruled notebook", 25, R.drawable.img_rice));
    }

    private void filterByCategory() {
        filteredList = new ArrayList<>();
        if (selectedCategory.equals("All")) {
            filteredList.addAll(productList);
        } else {
            for (Product p : productList) {
                if (p.getCategory().equals(selectedCategory)) {
                    filteredList.add(p);
                }
            }
        }
    }
}