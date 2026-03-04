package com.example.lankasmartmart;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnGetStarted;
    private TextView tvWelcome;
    private TextView tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize views
        btnGetStarted = findViewById(R.id.btnGetStarted);
        tvWelcome     = findViewById(R.id.tvWelcome);
        tvAppName     = findViewById(R.id.tvAppName);

        // Apply gradient to "Welcome" text
        tvWelcome.setTextColor(Color.BLACK);

        // Apply gradient to app name
        applyGradientToAppName(tvAppName);

        // Set click listener
        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });
    }
    private void applyGradientToAppName(TextView textView) {
        textView.post(() -> {
            Shader textShader = new LinearGradient(
                    0, 0, 0, textView.getTextSize(),   // vertical gradient (same as LoginActivity)
                    new int[]{
                            Color.parseColor("#205C3D"),
                            Color.parseColor("#42D78A")
                    },
                    null,
                    Shader.TileMode.CLAMP
            );
            textView.getPaint().setShader(textShader);
            textView.invalidate();
        });
    }
}