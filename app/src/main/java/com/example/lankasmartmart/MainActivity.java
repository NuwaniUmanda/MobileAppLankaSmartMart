package com.example.lankasmartmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView welcomeText, profileName, profileEmail;
    EditText searchEditText;
    ImageView menuIcon, closeMenu;
    RelativeLayout profileOverlay;
    LinearLayout groceriesCategory, householdCategory, personalCareCategory, stationeryCategory;
    LinearLayout navHome, navCategories, navCart, navProfile;
    LinearLayout menuNotifications, menuSettings, menuBarcode, menuOrderHistory;
    View viewProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        welcomeText       = findViewById(R.id.welcomeText);
        searchEditText    = findViewById(R.id.searchEditText);
        menuIcon          = findViewById(R.id.menuIcon);
        closeMenu         = findViewById(R.id.closeMenu);
        profileOverlay    = findViewById(R.id.profileOverlay);
        profileName       = findViewById(R.id.profileName);
        profileEmail      = findViewById(R.id.profileEmail);
        viewProfileButton = findViewById(R.id.viewProfileButton);

        groceriesCategory    = findViewById(R.id.groceriesCategory);
        householdCategory    = findViewById(R.id.householdCategory);
        personalCareCategory = findViewById(R.id.personalCareCategory);
        stationeryCategory   = findViewById(R.id.stationeryCategory);

        navHome       = findViewById(R.id.navHome);
        navCategories = findViewById(R.id.navCategories);
        navCart       = findViewById(R.id.navCart);
        navProfile    = findViewById(R.id.navProfile);

        menuNotifications = findViewById(R.id.menuNotifications);
        menuSettings      = findViewById(R.id.menuSettings);
        menuBarcode       = findViewById(R.id.menuBarcode);
        menuOrderHistory  = findViewById(R.id.menuOrderHistory);

        // Get user data from intent
        Intent intent   = getIntent();
        String userName = intent.getStringExtra("USER_NAME");
        String userEmail = intent.getStringExtra("USER_EMAIL");

        // Extract name from email if USER_NAME is not provided
        if (userName == null || userName.isEmpty()) {
            if (userEmail != null && !userEmail.isEmpty()) {
                String emailName = userEmail.split("@")[0];
                emailName = emailName.replace(".", " ").replace("_", " ");
                String[] words = emailName.split(" ");
                StringBuilder capitalizedName = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        capitalizedName.append(word.substring(0, 1).toUpperCase())
                                .append(word.substring(1).toLowerCase())
                                .append(" ");
                    }
                }
                userName = capitalizedName.toString().trim();
            } else {
                userName = "User";
            }
        }

        // Set welcome text and profile info
        welcomeText.setText("Hello, " + userName + " !");
        profileName.setText(userName);
        profileEmail.setText((userEmail != null && !userEmail.isEmpty()) ? userEmail : "No email provided");

        // Menu icon: show overlay
        menuIcon.setOnClickListener(v -> profileOverlay.setVisibility(View.VISIBLE));

        // Close menu
        closeMenu.setOnClickListener(v -> profileOverlay.setVisibility(View.GONE));

        // Hide overlay when clicking dim background
        profileOverlay.setOnClickListener(v -> profileOverlay.setVisibility(View.GONE));

        // View Profile button in overlay → ProfileActivity
        viewProfileButton.setOnClickListener(v -> {
            profileOverlay.setVisibility(View.GONE);
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        // Category cards → ProductsActivity
        groceriesCategory.setOnClickListener(v -> openCategory("Groceries"));
        householdCategory.setOnClickListener(v -> openCategory("Household"));
        personalCareCategory.setOnClickListener(v -> openCategory("Personal Care"));
        stationeryCategory.setOnClickListener(v -> openCategory("Stationery"));

        // Bottom Navigation
        navHome.setOnClickListener(v ->
                Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show());

        // Categories nav now opens ProductsActivity showing all products
        navCategories.setOnClickListener(v -> openCategory("All"));

        navCart.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CartActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // Profile Menu Items
        menuNotifications.setOnClickListener(v -> {
            profileOverlay.setVisibility(View.GONE);
            startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
        });

        // Settings in overlay → SettingsActivity
        menuSettings.setOnClickListener(v -> {
            profileOverlay.setVisibility(View.GONE);
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        menuBarcode.setOnClickListener(v -> {
            profileOverlay.setVisibility(View.GONE);
            startActivity(new Intent(MainActivity.this, BarcodeScannerActivity.class));
        });

        menuOrderHistory.setOnClickListener(v -> {
            profileOverlay.setVisibility(View.GONE);
            startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
        });
    }

    /** Opens ProductsActivity */
    private void openCategory(String category) {
        Intent i = new Intent(MainActivity.this, ProductsActivity.class);
        i.putExtra("CATEGORY", category);
        startActivity(i);
    }
}