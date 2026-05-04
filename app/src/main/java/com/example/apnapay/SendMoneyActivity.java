package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class SendMoneyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_money);
        MaterialButton btnSendMoney = findViewById(R.id.btnSendMoney);
        btnSendMoney.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransferSuccessActivity.class);
            startActivity(intent);
        });


    }
}