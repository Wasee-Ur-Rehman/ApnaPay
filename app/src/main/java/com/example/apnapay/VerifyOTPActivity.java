package com.example.apnapay;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
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

        String email = getIntent().getStringExtra("email");
        TextView tvInstructions = findViewById(R.id.tvInstructions);
        if (email != null) {
            tvInstructions.setText("We emailed a password reset link to " + email + ". Tap the link in that email to reset your password.");
        }

        MaterialButton btnConfirmOTP = findViewById(R.id.btnConfirmOTP);
        btnConfirmOTP.setText("Open email app");
        btnConfirmOTP.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "Open email app"));
        });

        TextView tvResend = findViewById(R.id.tvResend);
        tvResend.setText("Didn’t get the email? Resend");
        tvResend.setOnClickListener(v -> {
            // Navigate back to Forgot Password to resend
            startActivity(new Intent(VerifyOTPActivity.this, ForgotPasswordActivity.class));
            finish();
        });
    }
}