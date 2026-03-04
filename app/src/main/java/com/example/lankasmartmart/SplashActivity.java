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
import android.graphics.Color;

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
            Shader textShader = new LinearGradient(
                    0, 0, 0, tvAppName.getTextSize(),   // vertical gradient (same as LoginActivity)
                    new int[]{
                            Color.parseColor("#8DF3BF"),
                            Color.parseColor("#2FA769")
                    },
                    null,
                    Shader.TileMode.CLAMP
            );
            tvAppName.getPaint().setShader(textShader);
            tvAppName.invalidate();
        });
    }
}