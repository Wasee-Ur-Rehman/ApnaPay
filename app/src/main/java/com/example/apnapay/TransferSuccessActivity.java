package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class TransferSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transfer_success);

        // Get the amount that was transferred
        double amount = getIntent().getDoubleExtra("AMOUNT", 0.0);
        String receiverName = getIntent().getStringExtra("RECEIVER_NAME");

        // Display the amount (Assuming you have a TextView with id tvSuccessAmount in your XML)
        TextView tvSuccessAmount = findViewById(R.id.tvSuccessAmount);
        if (tvSuccessAmount != null) {
            tvSuccessAmount.setText(String.format(java.util.Locale.US, "Rs. %.2f", amount));
        }
        TextView tvSuccessRecipient = findViewById(R.id.tvSuccessRecipient);
        if (tvSuccessRecipient != null && receiverName != null) {
            tvSuccessRecipient.setText(receiverName);
        }

        MaterialButton btnBackToHome = findViewById(R.id.btnBackToHome);
        if (btnBackToHome != null) {
            btnBackToHome.setOnClickListener(v -> {
                Intent intent = new Intent(TransferSuccessActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}