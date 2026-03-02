package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack;
    private LinearLayout profileSettings, savedAddresses, paymentMethods;
    private LinearLayout languageSettings, helpSupport, termsConditions, privacyPolicy, aboutApp;
    private SwitchCompat pushNotificationSwitch, emailNotificationSwitch, smsNotificationSwitch;
    private SwitchCompat languageSwitch, darkModeSwitch;
    private Button logoutButton;
    private TextView navHome, navCategories, navCart, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupListeners();
        loadSettings();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);

        // Account
        profileSettings = findViewById(R.id.profileSettings);
        savedAddresses = findViewById(R.id.savedAddresses);
        paymentMethods = findViewById(R.id.paymentMethods);

        // Notifications
        pushNotificationSwitch = findViewById(R.id.pushNotificationSwitch);
        emailNotificationSwitch = findViewById(R.id.emailNotificationSwitch);
        smsNotificationSwitch = findViewById(R.id.smsNotificationSwitch);

        // Preferences
        languageSettings = findViewById(R.id.languageSettings);
        languageSwitch = findViewById(R.id.languageSwitch);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        // Other
        helpSupport = findViewById(R.id.helpSupport);
        termsConditions = findViewById(R.id.termsConditions);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        aboutApp = findViewById(R.id.aboutApp);

        // Logout
        logoutButton = findViewById(R.id.logoutButton);

        // Bottom navigation
        navHome = findViewById(R.id.navHome);
        navCategories = findViewById(R.id.navCategories);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Account
        profileSettings.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        savedAddresses.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        paymentMethods.setOnClickListener(v ->
                Toast.makeText(this, "Payment Methods", Toast.LENGTH_SHORT).show());

        // Notification Switches
        pushNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings("push_notifications", isChecked);
            Toast.makeText(this, "Push notifications " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        emailNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings("email_notifications", isChecked);
            Toast.makeText(this, "Email notifications " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        smsNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings("sms_notifications", isChecked);
            Toast.makeText(this, "SMS notifications " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        // Preferences
        languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings("language_english", isChecked);
            Toast.makeText(this, isChecked ? "English" : "සිංහල", Toast.LENGTH_SHORT).show();
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings("dark_mode", isChecked);
            Toast.makeText(this, "Dark mode " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        // Other
        helpSupport.setOnClickListener(v ->
                Toast.makeText(this, "Help & Support", Toast.LENGTH_SHORT).show());

        termsConditions.setOnClickListener(v ->
                Toast.makeText(this, "Terms & Conditions", Toast.LENGTH_SHORT).show());

        privacyPolicy.setOnClickListener(v ->
                Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show());

        aboutApp.setOnClickListener(v ->
                Toast.makeText(this, "Lanka Smart Mart v1.0", Toast.LENGTH_SHORT).show());

        // Logout
        logoutButton.setOnClickListener(v -> logout());

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        });

        navCategories.setOnClickListener(v ->
                Toast.makeText(this, "Categories", Toast.LENGTH_SHORT).show());

        navCart.setOnClickListener(v ->
                Toast.makeText(this, "Cart", Toast.LENGTH_SHORT).show());

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
            finish();
        });
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);

        pushNotificationSwitch.setChecked(prefs.getBoolean("push_notifications", true));
        emailNotificationSwitch.setChecked(prefs.getBoolean("email_notifications", true));
        smsNotificationSwitch.setChecked(prefs.getBoolean("sms_notifications", false));
        languageSwitch.setChecked(prefs.getBoolean("language_english", true));
        darkModeSwitch.setChecked(prefs.getBoolean("dark_mode", false));
    }

    private void saveSettings(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
    }

    private void logout() {
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        SharedPreferences settingsPrefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        settingsPrefs.edit().clear().apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}