package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOTPActivity.class);
            startActivity(intent);
        });
    }
}