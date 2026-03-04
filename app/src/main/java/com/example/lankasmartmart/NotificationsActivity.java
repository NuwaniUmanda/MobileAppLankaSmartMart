package com.example.lankasmartmart;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private ImageView btnBack;
    private AppCompatTextView navHome, navCategories, navCart, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        btnBack = findViewById(R.id.btnBack);
        navHome = findViewById(R.id.navHome);
        navCategories = findViewById(R.id.navCategories);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile);

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        loadNotifications();

        notificationAdapter = new NotificationAdapter(this, notificationList);
        recyclerNotifications.setAdapter(notificationAdapter);

        btnBack.setOnClickListener(v -> finish());

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(NotificationsActivity.this, MainActivity.class));
            finish();
        });

        navCategories.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationsActivity.this, ProductsActivity.class);
            intent.putExtra("CATEGORY", "All");
            startActivity(intent);
        });

        navCart.setOnClickListener(v ->
                startActivity(new Intent(NotificationsActivity.this, CartActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(NotificationsActivity.this, ProfileActivity.class)));
    }

    private void loadNotifications() {
        notificationList = new ArrayList<>();

        notificationList.add(new Notification(
                1, "Weekend Special !",
                "20% off on personal care products",
                "5 min ago", R.drawable.ic_discount,
                Color.parseColor("#3D9970")));

        notificationList.add(new Notification(
                2, "Order Delivered",
                "Your order #LSM2024 has been delivered",
                "1 hour ago", R.drawable.ic_delivery,
                Color.parseColor("#FF9800")));

        notificationList.add(new Notification(
                3, "Flash Sale Alert",
                "Chocolate Bars Flying Off the Shelves",
                "Yesterday", R.drawable.ic_flash_sale,
                Color.parseColor("#E91E63")));
    }
}