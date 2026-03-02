package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack, profilePicture;
    private TextView userName, userEmail, userEmailDetail, userPhone, userLocation;
    private TextView homeAddress, officeAddress;
    private LinearLayout homeAddressLayout, officeAddressLayout;
    private LinearLayout navHome, navCategories, navCart, navProfile;

    // Data
    private DatabaseHelper databaseHelper;
    private int currentUserId;
    private String currentUserEmail;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);

        // Get data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId    = prefs.getInt("USER_ID", -1);
        currentUserEmail = prefs.getString("USER_EMAIL", "");
        currentUserName  = prefs.getString("USER_NAME", "");

        // If user is not logged in, redirect to login (optional)
        if (currentUserId == -1) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadUserData();
        loadAddresses();
        setupListeners();
    }

    private void initializeViews() {
        // Top bar
        btnBack = findViewById(R.id.btnBack);

        // Profile info
        profilePicture  = findViewById(R.id.profilePicture);
        userName        = findViewById(R.id.userName);
        userEmail       = findViewById(R.id.userEmail);

        // Contact info
        userPhone       = findViewById(R.id.userPhone);
        userEmailDetail = findViewById(R.id.userEmailDetail);
        userLocation    = findViewById(R.id.userLocation);

        // Addresses
        homeAddressLayout   = findViewById(R.id.homeAddressLayout);
        officeAddressLayout = findViewById(R.id.officeAddressLayout);
        homeAddress         = findViewById(R.id.homeAddress);
        officeAddress       = findViewById(R.id.officeAddress);

        // Bottom navigation
        navHome       = findViewById(R.id.navHome);
        navCategories = findViewById(R.id.navCategories);
        navCart       = findViewById(R.id.navCart);
        navProfile    = findViewById(R.id.navProfile);
    }

    private void loadUserData() {
        // Set email from SharedPreferences (always available if logged in)
        if (!currentUserEmail.isEmpty()) {
            userEmail.setText(currentUserEmail);
            userEmailDetail.setText(currentUserEmail);
        }

        // Set name from SharedPreferences if available
        if (!currentUserName.isEmpty()) {
            userName.setText(currentUserName);
        }

        // Now fetch from database to ensure we have the latest (and to get phone)
        Cursor cursor = null;
        try {
            cursor = databaseHelper.getUserByEmail(currentUserEmail);
            if (cursor != null && cursor.moveToFirst()) {
                // Get the name from database
                String nameFromDb = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));

                // If SharedPreferences name is empty or different, update it
                if (currentUserName.isEmpty() || !currentUserName.equals(nameFromDb)) {
                    userName.setText(nameFromDb);
                    // Also update SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("USER_NAME", nameFromDb);
                    editor.apply();
                    currentUserName = nameFromDb; // update local variable
                }

                // Set phone
                userPhone.setText((phone != null && !phone.isEmpty()) ? phone : "Not provided");
                userLocation.setText("Default Location"); // You might want to get this from somewhere
            } else {
                // If database query fails, but we have name from prefs, keep it
                if (currentUserName.isEmpty()) {
                    userName.setText("User"); // Fallback
                }
                Toast.makeText(this, "Could not load user details from database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void loadAddresses() {
        Cursor cursor = databaseHelper.getUserAddresses(currentUserId);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                do {
                    String type        = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    String addressLine = cursor.getString(cursor.getColumnIndexOrThrow("address_line"));
                    String city        = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                    String fullAddress = addressLine + ", " + city;

                    if ("Home".equals(type)) {
                        homeAddress.setText(fullAddress);
                    } else if ("Office".equals(type)) {
                        officeAddress.setText(fullAddress);
                    }
                } while (cursor.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading addresses", Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close();
            }
        } else {
            homeAddress.setText(R.string.no_home_address);
            officeAddress.setText(R.string.no_office_address);
        }
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Profile picture click
        profilePicture.setOnClickListener(v ->
                Toast.makeText(this, "Edit profile picture", Toast.LENGTH_SHORT).show());

        // Address clicks
        homeAddressLayout.setOnClickListener(v ->
                Toast.makeText(this, "Edit home address", Toast.LENGTH_SHORT).show());
        officeAddressLayout.setOnClickListener(v ->
                Toast.makeText(this, "Edit office address", Toast.LENGTH_SHORT).show());

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        navCategories.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProductsActivity.class);
            intent.putExtra("CATEGORY", "All");
            startActivity(intent);
        });

        navCart.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, CartActivity.class)));

        navProfile.setOnClickListener(v ->
                Toast.makeText(this, "Already on profile", Toast.LENGTH_SHORT).show());
    }
}