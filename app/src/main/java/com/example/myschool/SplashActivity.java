package com.example.myschool;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.ivSchoolLogo);
        TextView schoolName = findViewById(R.id.tvSchoolName);
        TextView subtitle = findViewById(R.id.tvSubtitle);

        // Logo animation
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.75f, 1.0f,
                0.75f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(800);
        scaleAnimation.setFillAfter(true);
        logo.startAnimation(scaleAnimation);

        // Text fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(900);
        fadeIn.setStartOffset(300);
        schoolName.startAnimation(fadeIn);
        subtitle.startAnimation(fadeIn);

        // Navigate to LoginActivity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); // splash back stack madhun kadhun taakto
        }, SPLASH_DURATION);
    }
}