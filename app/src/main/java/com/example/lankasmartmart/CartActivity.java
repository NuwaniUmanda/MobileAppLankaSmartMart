package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    RecyclerView cartRecyclerView;
    CartAdapter cartAdapter;
    List<CartItem> cartItems = new ArrayList<>();

    TextView subtotalText, deliveryFeeText, discountText, totalText;
    TextView btnDelivery, btnPickup;
    EditText promoCodeInput;
    Button checkoutBtn, btnApplyPromo;
    View backBtn, emptyCartLayout, cartContentLayout;

    // Bottom Navigation
    AppCompatTextView navHome, navCategories, navCart, navProfile;

    DatabaseHelper dbHelper;
    int userId;
    double deliveryFee = 150.00;
    double discount = 0.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("USER_ID", -1);

        // Views
        backBtn           = findViewById(R.id.backBtn);
        emptyCartLayout   = findViewById(R.id.emptyCartLayout);
        cartContentLayout = findViewById(R.id.cartContentLayout);
        cartRecyclerView  = findViewById(R.id.cartRecyclerView);
        subtotalText      = findViewById(R.id.subtotalText);
        deliveryFeeText   = findViewById(R.id.deliveryFeeText);
        discountText      = findViewById(R.id.discountText);
        totalText         = findViewById(R.id.totalText);
        checkoutBtn       = findViewById(R.id.checkoutBtn);
        btnDelivery       = findViewById(R.id.btnDelivery);
        btnPickup         = findViewById(R.id.btnPickup);
        promoCodeInput    = findViewById(R.id.promoCodeInput);
        btnApplyPromo     = findViewById(R.id.btnApplyPromo);

        // Bottom Navigation
        navHome       = findViewById(R.id.navHome);
        navCategories = findViewById(R.id.navCategories);
        navCart       = findViewById(R.id.navCart);
        navProfile    = findViewById(R.id.navProfile);

        // RecyclerView
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartItems, () -> loadCart(), dbHelper, userId);
        cartRecyclerView.setAdapter(cartAdapter);

        deliveryFeeText.setText(String.format(Locale.getDefault(), "LKR %.2f", deliveryFee));

        backBtn.setOnClickListener(v -> finish());

        // Delivery / Pickup toggle
        btnDelivery.setOnClickListener(v -> {
            btnDelivery.setBackgroundResource(R.drawable.toggle_selected);
            btnDelivery.setTextColor(getColor(android.R.color.white));
            btnPickup.setBackground(null);
            btnPickup.setTextColor(getColor(android.R.color.holo_green_dark));
            deliveryFee = 150.00;
            updateSummary();
        });

        btnPickup.setOnClickListener(v -> {
            btnPickup.setBackgroundResource(R.drawable.toggle_selected);
            btnPickup.setTextColor(getColor(android.R.color.white));
            btnDelivery.setBackground(null);
            btnDelivery.setTextColor(getColor(android.R.color.holo_green_dark));
            deliveryFee = 0.00;
            updateSummary();
        });

        // Promo code
        btnApplyPromo.setOnClickListener(v -> {
            String code = promoCodeInput.getText().toString().trim();
            if (code.equalsIgnoreCase("SAVE200")) {
                discount = 200.00;
                Toast.makeText(this, "Promo applied! -LKR 200.00", Toast.LENGTH_SHORT).show();
            } else if (code.isEmpty()) {
                Toast.makeText(this, "Enter a promo code", Toast.LENGTH_SHORT).show();
            } else {
                discount = 0.00;
                Toast.makeText(this, "Invalid promo code", Toast.LENGTH_SHORT).show();
            }
            updateSummary();
        });

        checkoutBtn.setOnClickListener(v -> {
            if (!cartItems.isEmpty()) {
                startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom Navigation click listeners
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, MainActivity.class));
            finish();
        });

        navCategories.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ProductsActivity.class);
            intent.putExtra("CATEGORY", "All");
            startActivity(intent);
        });

        navCart.setOnClickListener(v ->
                Toast.makeText(this, "Already on Cart", Toast.LENGTH_SHORT).show()
        );

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(CartActivity.this, ProfileActivity.class))
        );

        loadCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        cartItems.clear();
        Cursor cursor = dbHelper.getCartItems(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int colProductId = cursor.getColumnIndex(DatabaseHelper.CART_PRODUCT_ID);
                int colName      = cursor.getColumnIndex("product_name");
                int colPrice     = cursor.getColumnIndex("product_price");
                int colQuantity  = cursor.getColumnIndex(DatabaseHelper.CART_QUANTITY);
                int colImage     = cursor.getColumnIndex("product_image");

                if (colProductId < 0 || colName < 0 || colPrice < 0
                        || colQuantity < 0 || colImage < 0) continue;

                cartItems.add(new CartItem(
                        cursor.getInt(colProductId),
                        cursor.getString(colName),
                        cursor.getDouble(colPrice),
                        cursor.getInt(colQuantity),
                        cursor.getString(colImage)
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }

        cartAdapter.notifyDataSetChanged();
        updateSummary();

        if (cartItems.isEmpty()) {
            emptyCartLayout.setVisibility(View.VISIBLE);
            cartContentLayout.setVisibility(View.GONE);
        } else {
            emptyCartLayout.setVisibility(View.GONE);
            cartContentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateSummary() {
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        double total = subtotal + deliveryFee - discount;
        if (total < 0) total = 0;

        subtotalText.setText(String.format(Locale.getDefault(), "LKR %.2f", subtotal));
        deliveryFeeText.setText(String.format(Locale.getDefault(), "LKR %.2f", deliveryFee));
        discountText.setText(String.format(Locale.getDefault(), "-LKR %.2f", discount));
        totalText.setText(String.format(Locale.getDefault(), "LKR %.2f", total));
    }
}