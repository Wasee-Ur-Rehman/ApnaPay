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

        // Get the data passed from SendMoneyActivity
        double amount = getIntent().getDoubleExtra("AMOUNT", 0.0);
        String receiverName = getIntent().getStringExtra("RECEIVER_NAME");

        // Format the amount once
        String formattedAmount = String.format(java.util.Locale.US, "Rs. %.2f", amount);

        // Map all the TextViews
        TextView tvSuccessAmount = findViewById(R.id.tvSuccessAmount);
        TextView tvDetailTransferAmount = findViewById(R.id.tvDetailTransferAmount);
        TextView tvDetailTotalAmount = findViewById(R.id.tvDetailTotalAmount);
        TextView tvSuccessRecipient = findViewById(R.id.tvSuccessRecipient);

        // Set the amounts
        if (tvSuccessAmount != null) tvSuccessAmount.setText(formattedAmount);
        if (tvDetailTransferAmount != null) tvDetailTransferAmount.setText(formattedAmount);
        if (tvDetailTotalAmount != null) tvDetailTotalAmount.setText(formattedAmount);

        // Set the recipient
        if (tvSuccessRecipient != null && receiverName != null) {
            tvSuccessRecipient.setText(receiverName);
        }

        // Home Button Logic
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