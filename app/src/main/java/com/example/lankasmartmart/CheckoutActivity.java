package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CheckoutActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout deliveryAddressCard;
    private TextView deliveryAddressText;
    private LinearLayout paymentCOD, paymentCard, paymentOnline;
    private CheckBox paymentCODIcon, paymentCardIcon, paymentOnlineIcon;
    private TextView itemsCountText, subtotalText, deliveryFeeText, discountText, totalText;
    private Button placeOrderButton;

    private DatabaseHelper databaseHelper;
    private int currentUserId;
    private String selectedPaymentMethod = "COD";
    private String deliveryAddress = "";
    private double subtotal  = 0.0;
    private double deliveryFee = 150.0;
    private double discount  = 200.0;
    private double total     = 0.0;
    private int itemCount    = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("USER_ID", -1);

        initializeViews();
        loadUserAddress();
        calculateOrderSummary();
        setupListeners();

        // Default: COD selected
        selectPaymentMethod("COD");
    }

    private void initializeViews() {
        btnBack             = findViewById(R.id.btnBack);
        deliveryAddressCard = findViewById(R.id.deliveryAddressCard);
        deliveryAddressText = findViewById(R.id.deliveryAddressText);
        paymentCOD          = findViewById(R.id.paymentCOD);
        paymentCard         = findViewById(R.id.paymentCard);
        paymentOnline       = findViewById(R.id.paymentOnline);
        paymentCODIcon      = findViewById(R.id.paymentCODIcon);
        paymentCardIcon     = findViewById(R.id.paymentCardIcon);
        paymentOnlineIcon   = findViewById(R.id.paymentOnlineIcon);
        itemsCountText      = findViewById(R.id.itemsCountText);
        subtotalText        = findViewById(R.id.subtotalText);
        deliveryFeeText     = findViewById(R.id.deliveryFeeText);
        discountText        = findViewById(R.id.discountText);
        totalText           = findViewById(R.id.totalText);
        placeOrderButton    = findViewById(R.id.placeOrderButton);
    }

    private void loadUserAddress() {
        Cursor cursor = databaseHelper.getUserAddresses(currentUserId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int typeCol = cursor.getColumnIndex(DatabaseHelper.ADDRESS_TYPE);
                if (typeCol < 0) break;
                String type = cursor.getString(typeCol);

                if ("Home".equals(type)) {
                    int lineCol = cursor.getColumnIndex(DatabaseHelper.ADDRESS_LINE);
                    int cityCol = cursor.getColumnIndex(DatabaseHelper.ADDRESS_CITY);
                    String line = lineCol >= 0 ? cursor.getString(lineCol) : "";
                    String city = cityCol >= 0 ? cursor.getString(cityCol) : "";
                    deliveryAddress = line + ", " + city;
                    deliveryAddressText.setText(deliveryAddress);
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            // No address saved — prompt user
            deliveryAddressText.setText("Go to Profile → add your address");
            deliveryAddress = "";
        }
    }

    private void calculateOrderSummary() {
        Cursor cursor = databaseHelper.getCartItems(currentUserId);

        subtotal  = 0.0;
        itemCount = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int priceCol    = cursor.getColumnIndex("product_price");
                int quantityCol = cursor.getColumnIndex(DatabaseHelper.CART_QUANTITY);

                double price    = priceCol    >= 0 ? cursor.getDouble(priceCol)  : 0.0;
                int    quantity = quantityCol >= 0 ? cursor.getInt(quantityCol)  : 0;

                subtotal  += (price * quantity);
                itemCount += quantity;

            } while (cursor.moveToNext());
            cursor.close();
        }

        total = subtotal + deliveryFee - discount;

        itemsCountText.setText(itemCount + " items");
        subtotalText.setText(String.format("LKR %.2f", subtotal));
        deliveryFeeText.setText(String.format("LKR %.2f", deliveryFee));
        discountText.setText(String.format("-LKR %.2f", discount));
        totalText.setText(String.format("LKR %.2f", total));
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;

        // Uncheck all
        paymentCODIcon.setChecked(false);
        paymentCardIcon.setChecked(false);
        paymentOnlineIcon.setChecked(false);

        // Check selected
        switch (method) {
            case "COD":
                paymentCODIcon.setChecked(true);
                break;
            case "Card":
                paymentCardIcon.setChecked(true);
                break;
            case "Online":
                paymentOnlineIcon.setChecked(true);
                break;
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        deliveryAddressCard.setOnClickListener(v ->
                Toast.makeText(this, "Change address", Toast.LENGTH_SHORT).show());

        paymentCOD.setOnClickListener(v    -> selectPaymentMethod("COD"));
        paymentCard.setOnClickListener(v   -> selectPaymentMethod("Card"));
        paymentOnline.setOnClickListener(v -> selectPaymentMethod("Online"));

        placeOrderButton.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        if (itemCount == 0) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        long orderId = databaseHelper.addOrder(currentUserId, total);

        if (orderId > 0) {
            Cursor cursor = databaseHelper.getCartItems(currentUserId);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int productIdCol = cursor.getColumnIndex(DatabaseHelper.CART_PRODUCT_ID);
                    int quantityCol  = cursor.getColumnIndex(DatabaseHelper.CART_QUANTITY);
                    int priceCol     = cursor.getColumnIndex("product_price");

                    int    productId = productIdCol >= 0 ? cursor.getInt(productIdCol)   : 0;
                    int    quantity  = quantityCol  >= 0 ? cursor.getInt(quantityCol)    : 0;
                    double price     = priceCol     >= 0 ? cursor.getDouble(priceCol)    : 0.0;

                    databaseHelper.addOrderItem((int) orderId, productId, quantity, price);

                } while (cursor.moveToNext());
                cursor.close();
            }

            databaseHelper.clearCart(currentUserId);

            Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
            intent.putExtra("ORDER_ID",        (int) orderId);
            intent.putExtra("TOTAL_AMOUNT",    total);
            intent.putExtra("DELIVERY_ADDRESS", deliveryAddress);
            intent.putExtra("PAYMENT_METHOD",  selectedPaymentMethod);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateOrderSummary();
    }
}