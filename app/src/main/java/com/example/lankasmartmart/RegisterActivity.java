package com.example.lankasmartmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity {

    EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    CheckBox termsCheckBox;
    Button registerButton;
    TextView logInText;
    TextView appTitleText;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper          = new DatabaseHelper(this);
        nameEditText            = findViewById(R.id.nameEditText);
        emailEditText           = findViewById(R.id.emailEditText);
        passwordEditText        = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        termsCheckBox           = findViewById(R.id.termsCheckBox);
        registerButton          = findViewById(R.id.registerButton);
        logInText               = findViewById(R.id.logInText);

        // Gradient — FIX: use measured width on the x-axis so the gradient
        // actually spreads across the text (previously width was computed but
        // passed as 0, making it a vertical-only gradient).
        appTitleText = findViewById(R.id.appTitleText);
        TextPaint paint = appTitleText.getPaint();
        float width = paint.measureText("Lanka Smart Mart"); // now actually used ↓
        Shader textShader = new LinearGradient(
                0, 0, width, appTitleText.getTextSize(),
                new int[]{
                        Color.parseColor("#205C3D"),
                        Color.parseColor("#42D78A"),
                },
                null,
                Shader.TileMode.CLAMP
        );
        appTitleText.getPaint().setShader(textShader);

        // FIX: lambda instead of anonymous class (warning at line 60)
        registerButton.setOnClickListener(v -> {
            String name            = nameEditText.getText().toString().trim();
            String email           = emailEditText.getText().toString().trim();
            String password        = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (name.isEmpty()) {
                nameEditText.setError("Please enter your name!");
                return;
            }
            if (email.isEmpty()) {
                emailEditText.setError("Please enter your email!");
                return;
            }
            if (password.isEmpty()) {
                passwordEditText.setError("Please enter your password!");
                return;
            }
            if (confirmPassword.isEmpty()) {
                confirmPasswordEditText.setError("Please confirm password!");
                return;
            }
            if (password.length() < 6) {
                passwordEditText.setError("Password must be 6+ characters!");
                return;
            }
            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Passwords don't match!");
                return;
            }
            if (!termsCheckBox.isChecked()) {
                Toast.makeText(this, "Please accept Terms & Conditions",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (databaseHelper.checkEmailExists(email)) {
                emailEditText.setError("Email already registered!");
                return;
            }

            // hash the password before storing it
            String hashedPassword = sha256(password);

            boolean success = databaseHelper.addUser(name, email, hashedPassword, "");

            if (success) {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                int newUserId = databaseHelper.getUserId(email); // get the saved user's ID

                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putInt("USER_ID", newUserId)
                        .putString("USER_NAME", name)
                        .putString("USER_EMAIL", email)
                        .apply();

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    // Terms checkbox toggle — referenced by android:onClick in XML, not "unused"
    public void onTermsTextClick(View view) {
        termsCheckBox.setChecked(!termsCheckBox.isChecked());
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