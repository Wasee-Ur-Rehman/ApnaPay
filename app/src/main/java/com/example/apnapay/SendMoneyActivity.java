package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apnapay.data.Db; // Assuming this returns your FirebaseDatabase instance
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class SendMoneyActivity extends AppCompatActivity {

    private EditText etAmount, etRecipient;
    private TextView tvAvailableBalance;
    private double currentSenderBalance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_money);

        etAmount = findViewById(R.id.etAmount);
        etRecipient = findViewById(R.id.etRecipient);
        tvAvailableBalance = findViewById(R.id.tvAvailableBalance); // Make sure this ID exists in your XML!

        MaterialButton btnSendMoney = findViewById(R.id.btnSendMoney);
        btnSendMoney.setOnClickListener(v -> attemptTransfer());

        // Back button (assuming you have an ImageView with id btnBack)
        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Fetch and display live balance
        setupLiveBalance();
    }

    private void setupLiveBalance() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = Db.db().getReference("Users").child(user.getUid()).child("balance");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double b = snapshot.getValue(Double.class);
                    currentSenderBalance = (b != null) ? b : 0.0;
                    if (tvAvailableBalance != null) {
                        tvAvailableBalance.setText(String.format(java.util.Locale.US, "Rs. %.2f", currentSenderBalance));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SendMoneyActivity.this, "Failed to load balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptTransfer() {
        String acc = etRecipient.getText().toString().trim();
        String amtStr = etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(acc) || acc.length() != 10) {
            Toast.makeText(this, "Enter a valid 10-digit account number", Toast.LENGTH_SHORT).show();
            return;
        }

        final double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (Exception e) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > currentSenderBalance) {
            Toast.makeText(this, "Insufficient balance!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser sender = FirebaseAuth.getInstance().getCurrentUser();
        if (sender == null) return;

        // Find the recipient by Account Number
        DatabaseReference usersRef = Db.db().getReference("Users");
        usersRef.orderByChild("accountNumber").equalTo(acc).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(SendMoneyActivity.this, "Recipient account not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    String receiverUid = child.getKey();
                    String receiverName = child.child("name").getValue(String.class);
                    Double receiverBalance = child.child("balance").getValue(Double.class);
                    if (receiverBalance == null) receiverBalance = 0.0;

                    if (sender.getUid().equals(receiverUid)) {
                        Toast.makeText(SendMoneyActivity.this, "You cannot send money to yourself", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    confirmAndTransfer(sender.getUid(), receiverUid, receiverName, receiverBalance, amount);
                    break; // Only process the first match
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SendMoneyActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmAndTransfer(String senderUid, String receiverUid, String receiverName, double receiverBalance, double amount) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Transfer")
                .setMessage("Send Rs. " + amount + " to " + receiverName + "?")
                .setPositiveButton("Send", (dialog, which) -> performAtomicTransfer(senderUid, receiverUid, receiverBalance, amount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performAtomicTransfer(String senderUid, String receiverUid, double receiverBalance, double amount) {
        DatabaseReference root = Db.db().getReference();
        String txId = root.child("Transactions").push().getKey();

        // Prepare the transaction record
        Map<String, Object> tx = new HashMap<>();
        tx.put("transactionId", txId);
        tx.put("senderUid", senderUid);
        tx.put("receiverUid", receiverUid);
        tx.put("amount", amount);
        tx.put("timestamp", System.currentTimeMillis());
        tx.put("type", "P2P_TRANSFER");

        // Prepare the ATOMIC multi-path update
        Map<String, Object> updates = new HashMap<>();
        updates.put("Users/" + senderUid + "/balance", currentSenderBalance - amount);
        updates.put("Users/" + receiverUid + "/balance", receiverBalance + amount);
        updates.put("Transactions/" + txId, tx);

        // Execute all updates simultaneously
        root.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SendMoneyActivity.this, "Transfer Successful!", Toast.LENGTH_SHORT).show();

                // Go to Success Screen and pass the amount
                Intent intent = new Intent(SendMoneyActivity.this, TransferSuccessActivity.class);
                intent.putExtra("AMOUNT", amount);
                startActivity(intent);
                finish(); // Close SendMoneyActivity
            } else {
                Toast.makeText(SendMoneyActivity.this, "Transfer Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}