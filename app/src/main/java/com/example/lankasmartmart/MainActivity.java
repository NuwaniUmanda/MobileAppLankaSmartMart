package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView welcomeText, profileName, profileEmail;
    EditText searchEditText;
    ImageView menuIcon, closeMenu;
    RelativeLayout profileOverlay;
    LinearLayout groceriesCategory, householdCategory, personalCareCategory, stationeryCategory;
    LinearLayout menuNotifications, menuSettings, menuBarcode, menuOrderHistory;
    View viewProfileButton;

    // Search results
    RecyclerView searchResultsRecycler;
    View searchResultsContainer;
    TextView searchResultsTitle;

    AppCompatTextView navHome, navCategories, navCart, navProfile;

    List<Product> allProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        welcomeText          = findViewById(R.id.welcomeText);
        searchEditText       = findViewById(R.id.searchEditText);
        menuIcon             = findViewById(R.id.menuIcon);
        closeMenu            = findViewById(R.id.closeMenu);
        profileOverlay       = findViewById(R.id.profileOverlay);
        profileName          = findViewById(R.id.profileName);
        profileEmail         = findViewById(R.id.profileEmail);
        viewProfileButton    = findViewById(R.id.viewProfileButton);
        searchResultsContainer = findViewById(R.id.searchResultsContainer);
        searchResultsRecycler  = findViewById(R.id.searchResultsRecycler);
        searchResultsTitle     = findViewById(R.id.searchResultsTitle);

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

        // Load all products
        loadAllProducts();

        // Setup search results recycler
        searchResultsRecycler.setLayoutManager(new GridLayoutManager(this, 2));

        // Get user data
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName  = prefs.getString("USER_NAME", "");
        String userEmail = prefs.getString("USER_EMAIL", "");

        if (userName.isEmpty()) {
            if (!userEmail.isEmpty()) {
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

        welcomeText.setText(getString(R.string.hello_user, userName));
        profileName.setText(userName);
        profileEmail.setText((userEmail != null && !userEmail.isEmpty())
                ? userEmail : "No email provided");

        // ── SEARCH ──────────────────────────────────────────────────────────
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    searchResultsContainer.setVisibility(View.GONE);
                } else {
                    searchProducts(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Search on keyboard "Search" button press
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                // Navigate to ProductsActivity with search query
                Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
                intent.putExtra("CATEGORY", "All");
                intent.putExtra("SEARCH_QUERY", query);
                startActivity(intent);
            }
            return true;
        });

        // Overlay & nav listeners
        menuIcon.setOnClickListener(v -> profileOverlay.setVisibility(View.VISIBLE));
        closeMenu.setOnClickListener(v -> profileOverlay.setVisibility(View.GONE));
        profileOverlay.setOnClickListener(v -> profileOverlay.setVisibility(View.GONE));

        viewProfileButton.setOnClickListener(v -> {
            profileOverlay.setVisibility(View.GONE);
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        groceriesCategory.setOnClickListener(v -> openCategory("Groceries"));
        householdCategory.setOnClickListener(v -> openCategory("Household"));
        personalCareCategory.setOnClickListener(v -> openCategory("Personal Care"));
        stationeryCategory.setOnClickListener(v -> openCategory("Stationery"));

        navHome.setOnClickListener(v ->
                Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show());
        navCategories.setOnClickListener(v -> openCategory("All"));
        navCart.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CartActivity.class)));
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        menuNotifications.setOnClickListener(v -> {
            profileOverlay.setVisibility(View.GONE);
            startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
        });
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

    private void searchProducts(String query) {
        List<Product> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(lowerQuery)
                    || p.getCategory().toLowerCase().contains(lowerQuery)
                    || p.getDescription().toLowerCase().contains(lowerQuery)) {
                results.add(p);
            }
        }

        if (results.isEmpty()) {
            searchResultsTitle.setText("No results for \"" + query + "\"");
            searchResultsContainer.setVisibility(View.VISIBLE);
            searchResultsRecycler.setAdapter(null);
        } else {
            searchResultsTitle.setText("Results for \"" + query + "\" (" + results.size() + ")");
            searchResultsContainer.setVisibility(View.VISIBLE);

            ProductAdapter adapter = new ProductAdapter(this, results, product -> {
                // Hide search and go to product details
                searchEditText.setText("");
                searchResultsContainer.setVisibility(View.GONE);

                Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
                intent.putExtra("PRODUCT_ID",          product.getId());
                intent.putExtra("PRODUCT_NAME",        product.getName());
                intent.putExtra("PRODUCT_PRICE",       product.getPrice());
                intent.putExtra("PRODUCT_CATEGORY",    product.getCategory());
                intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());
                intent.putExtra("PRODUCT_STOCK",       product.getStock());
                intent.putExtra("PRODUCT_IMAGE",       product.getImageResource());
                startActivity(intent);
            });
            searchResultsRecycler.setAdapter(adapter);
        }
    }

    private void loadAllProducts() {
        allProducts.add(new Product(1, "Basmati Rice - 1kg", 350.00,
                "Groceries", "Premium quality basmati rice", 10, R.drawable.img_rice));
        allProducts.add(new Product(2, "Imorich French Vanilla - 1L", 1290.00,
                "Groceries", "Rich and creamy French vanilla ice cream", 0, R.drawable.img_ice_cream));
        allProducts.add(new Product(3, "Munchee Choc Shock - 90g", 300.00,
                "Groceries", "Delicious chocolate biscuits", 20, R.drawable.img_chocolate));
        allProducts.add(new Product(4, "Tiara Sponge Layer Cake - 310g", 550.00,
                "Groceries", "Soft and fluffy sponge cake", 8, R.drawable.img_cake));
        allProducts.add(new Product(5, "Vim Dishwash Liquid Anti Smell 500ml", 450.00,
                "Household", "Anti smell dishwash liquid 500ml", 50, R.drawable.img_vim_dishwash));
        allProducts.add(new Product(6, "Lysol Lavender Disinfectant 500ml", 500.00,
                "Household", "Lavender disinfectant kills 99.9% germs", 30, R.drawable.img_lysol));
        allProducts.add(new Product(7, "Lux Soap Jasmine And Vitamin E 100g", 170.00,
                "Personal Care", "Jasmine and Vitamin E moisturizing soap", 40, R.drawable.img_lux_soap));
        allProducts.add(new Product(8, "Sunsilk Onion & Jojoba Oil Shampoo 200ml", 750.00,
                "Personal Care", "Hair fall resist shampoo", 15, R.drawable.img_sunsilk));
        allProducts.add(new Product(9, "Promate Notebook Single A6 80P", 90.00,
                "Stationery", "Single ruled A6 notebook 80 pages", 100, R.drawable.img_promate_notebook));
        allProducts.add(new Product(10, "Atlas Pen Chooty II Assorted 3Pkt", 85.00,
                "Stationery", "Assorted color pen pack of 3", 75, R.drawable.img_atlas_pen));
    }

    private void openCategory(String category) {
        Intent i = new Intent(MainActivity.this, ProductsActivity.class);
        i.putExtra("CATEGORY", category);
        startActivity(i);
    }
}