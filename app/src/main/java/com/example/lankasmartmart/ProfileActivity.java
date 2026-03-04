package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack, profilePicture;
    private TextView userName, userEmail, userEmailDetail, userPhone, userLocation;
    private TextView homeAddress, officeAddress;
    private LinearLayout homeAddressLayout, officeAddressLayout;
    private androidx.appcompat.widget.AppCompatTextView navHome, navCategories, navCart, navProfile;

    private DatabaseHelper databaseHelper;
    private int currentUserId;
    private String currentUserEmail;
    private String currentUserName;

    // Track saved address IDs for update vs insert
    private int homeAddressId   = -1;
    private int officeAddressId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId    = prefs.getInt("USER_ID",    -1);
        currentUserEmail = prefs.getString("USER_EMAIL", "");
        currentUserName  = prefs.getString("USER_NAME",  "");

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
        btnBack             = findViewById(R.id.btnBack);
        profilePicture      = findViewById(R.id.profilePicture);
        userName            = findViewById(R.id.userName);
        userEmail           = findViewById(R.id.userEmail);
        userPhone           = findViewById(R.id.userPhone);
        userEmailDetail     = findViewById(R.id.userEmailDetail);
        userLocation        = findViewById(R.id.userLocation);
        homeAddressLayout   = findViewById(R.id.homeAddressLayout);
        officeAddressLayout = findViewById(R.id.officeAddressLayout);
        homeAddress         = findViewById(R.id.homeAddress);
        officeAddress       = findViewById(R.id.officeAddress);
        navHome             = findViewById(R.id.navHome);
        navCategories       = findViewById(R.id.navCategories);
        navCart             = findViewById(R.id.navCart);
        navProfile          = findViewById(R.id.navProfile);
    }

    private void loadUserData() {
        if (!currentUserEmail.isEmpty()) {
            userEmail.setText(currentUserEmail);
            userEmailDetail.setText(currentUserEmail);
        }
        if (!currentUserName.isEmpty()) {
            userName.setText(currentUserName);
        }

        Cursor cursor = null;
        try {
            cursor = databaseHelper.getUserByEmail(currentUserEmail);
            if (cursor != null && cursor.moveToFirst()) {
                String nameFromDb = cursor.getString(
                        cursor.getColumnIndexOrThrow("full_name"));
                String phone = cursor.getString(
                        cursor.getColumnIndexOrThrow("phone"));

                userName.setText(nameFromDb);
                userPhone.setText((phone != null && !phone.isEmpty())
                        ? phone : "Tap to add phone number");
                userLocation.setText("Sri Lanka");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void loadAddresses() {
        homeAddressId   = -1;
        officeAddressId = -1;

        Cursor cursor = databaseHelper.getUserAddresses(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int colId   = cursor.getColumnIndex("address_id");
                int colType = cursor.getColumnIndex("type");
                int colLine = cursor.getColumnIndex("address_line");
                int colCity = cursor.getColumnIndex("city");

                if (colType < 0) continue;
                String type        = cursor.getString(colType);
                String addressLine = colLine >= 0 ? cursor.getString(colLine) : "";
                String city        = colCity >= 0 ? cursor.getString(colCity) : "";
                String full        = addressLine + ", " + city;
                int    id          = colId   >= 0 ? cursor.getInt(colId) : -1;

                if ("Home".equals(type)) {
                    homeAddressId = id;
                    homeAddress.setText(full);
                } else if ("Office".equals(type)) {
                    officeAddressId = id;
                    officeAddress.setText(full);
                }
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            homeAddress.setText("Tap to add home address");
            officeAddress.setText("Tap to add office address");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        profilePicture.setOnClickListener(v ->
                Toast.makeText(this, "Edit profile picture", Toast.LENGTH_SHORT).show());

        // ✅ Phone click → edit dialog
        userPhone.setOnClickListener(v -> showPhoneDialog());

        // ✅ Address clicks → edit dialogs
        homeAddressLayout.setOnClickListener(v ->
                showAddressDialog("Home", homeAddressId));
        officeAddressLayout.setOnClickListener(v ->
                showAddressDialog("Office", officeAddressId));

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

    // ── Phone dialog ─────────────────────────────────────────────────────────
    private void showPhoneDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_phone, null);
        EditText etPhone = view.findViewById(R.id.etPhone);

        // Pre-fill current phone
        String current = userPhone.getText().toString();
        if (!current.equals("Tap to add phone number")) {
            etPhone.setText(current);
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit Phone Number")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String phone = etPhone.getText().toString().trim();
                    if (phone.isEmpty()) {
                        Toast.makeText(this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Update in database
                    android.content.ContentValues values = new android.content.ContentValues();
                    values.put("phone", phone);
                    android.database.sqlite.SQLiteDatabase db =
                            databaseHelper.getWritableDatabase();
                    db.update("users", values, "user_id=?",
                            new String[]{String.valueOf(currentUserId)});
                    db.close();

                    userPhone.setText(phone);
                    Toast.makeText(this, "Phone number saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Address dialog ────────────────────────────────────────────────────────
    private void showAddressDialog(String type, int existingId) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_address, null);
        EditText etLine = view.findViewById(R.id.etAddressLine);
        EditText etCity = view.findViewById(R.id.etCity);

        // Pre-fill existing address
        if (existingId != -1) {
            Cursor cursor = databaseHelper.getUserAddresses(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int colId   = cursor.getColumnIndex("address_id");
                    int colType = cursor.getColumnIndex("type");
                    if (colId >= 0 && cursor.getInt(colId) == existingId) {
                        int colLine = cursor.getColumnIndex("address_line");
                        int colCity = cursor.getColumnIndex("city");
                        if (colLine >= 0) etLine.setText(cursor.getString(colLine));
                        if (colCity >= 0) etCity.setText(cursor.getString(colCity));
                        break;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit " + type + " Address")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String line = etLine.getText().toString().trim();
                    String city = etCity.getText().toString().trim();

                    if (line.isEmpty() || city.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (existingId == -1) {
                        // New address
                        long newId = databaseHelper.addAddress(
                                currentUserId, type, line, city);
                        if ("Home".equals(type))   homeAddressId   = (int) newId;
                        if ("Office".equals(type)) officeAddressId = (int) newId;
                    } else {
                        // Update existing
                        databaseHelper.updateAddress(existingId, type, line, city);
                    }

                    String full = line + ", " + city;
                    if ("Home".equals(type))   homeAddress.setText(full);
                    if ("Office".equals(type)) officeAddress.setText(full);

                    Toast.makeText(this, type + " address saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}