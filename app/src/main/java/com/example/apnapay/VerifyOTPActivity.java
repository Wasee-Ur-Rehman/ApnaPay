package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyOTPActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_otp);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        MaterialButton btnConfirmOTP = findViewById(R.id.btnConfirmOTP);
        btnConfirmOTP.setOnClickListener(v -> {
            Intent intent = new Intent(VerifyOTPActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}