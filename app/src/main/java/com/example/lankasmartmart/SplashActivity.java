package com.example.lankasmartmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private TextView tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tvAppName = findViewById(R.id.tvAppName);
        applyGradientToText();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            finish();
        }, 3000);
    }

    private void applyGradientToText() {
        tvAppName.post(() -> {
            float width = tvAppName.getPaint().measureText(tvAppName.getText().toString());

            Shader textShader = new LinearGradient(
                    0, 0, width, 0,
                    new int[]{
                            ContextCompat.getColor(SplashActivity.this, R.color.gradient_white_start),
                            ContextCompat.getColor(SplashActivity.this, R.color.gradient_white_end)
                    },
                    null,
                    Shader.TileMode.CLAMP
            );

            tvAppName.getPaint().setShader(textShader);
            tvAppName.invalidate();
        });
    }
}