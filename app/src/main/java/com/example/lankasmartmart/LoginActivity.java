package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView signUpText, forgotPasswordText, googleSignInText;
    TextView appTitleText;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        emailEditText      = findViewById(R.id.emailEditText);
        passwordEditText   = findViewById(R.id.passwordEditText);
        loginButton        = findViewById(R.id.loginButton);
        signUpText         = findViewById(R.id.signUpText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        googleSignInText   = findViewById(R.id.googleSignInText);

        // Gradient
        appTitleText = findViewById(R.id.appTitleText);
        TextPaint paint = appTitleText.getPaint();
        float width = paint.measureText("Lanka Smart Mart");
        Shader textShader = new LinearGradient(
                0, 0, 0, appTitleText.getTextSize(),
                new int[]{
                        Color.parseColor("#205C3D"),
                        Color.parseColor("#42D78A"),
                },
                null,
                Shader.TileMode.CLAMP
        );
        appTitleText.getPaint().setShader(textShader);

        // Login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email    = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validation
                if (email.isEmpty()) {
                    emailEditText.setError("Please enter your email!");
                    return;
                }
                if (password.isEmpty()) {
                    passwordEditText.setError("Please enter your password!");
                    return;
                }

                // Check database with plain password
                boolean userExists = databaseHelper.checkUser(email, sha256(password));

                if (userExists) {
                    // SUCCESS - Get user data from database
                    Cursor cursor = databaseHelper.getUserByEmail(email);

                    if (cursor != null && cursor.moveToFirst()) {
                        try {
                            // Get user details
                            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                            String userName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                            String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));

                            // Save to SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("USER_ID", userId);
                            editor.putString("USER_NAME", userName);
                            editor.putString("USER_EMAIL", userEmail);
                            editor.apply();

                            cursor.close();

                            // Show success message
                            Toast.makeText(LoginActivity.this,
                                    "Welcome, " + userName + "!",
                                    Toast.LENGTH_SHORT).show();

                            // Navigate to MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } catch (IllegalArgumentException e) {
                            Toast.makeText(LoginActivity.this,
                                    "Error loading user data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    // FAILED
                    Toast.makeText(LoginActivity.this,
                            "Wrong email or password!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sign up click
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Forgot password click
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,
                        "Forgot Password clicked",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Google sign in click
        googleSignInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,
                        "Google Sign-In clicked",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}