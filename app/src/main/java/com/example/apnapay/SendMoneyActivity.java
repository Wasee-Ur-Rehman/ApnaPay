package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SendMoneyActivity extends AppCompatActivity {

    private EditText etAmount, etRecipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_money);

        etAmount = findViewById(R.id.etAmount);
        etRecipient = findViewById(R.id.etRecipient);
        MaterialButton btnSendMoney = findViewById(R.id.btnSendMoney);
        btnSendMoney.setOnClickListener(v -> attemptTransfer());
    }

    private void attemptTransfer() {
        String acc = etRecipient.getText().toString().trim();
        String amtStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(acc) || acc.length() != 10) {
            Toast.makeText(this, "Enter valid 10-digit account number", Toast.LENGTH_SHORT).show();
            return;
        }
        final double amount = amountFrom(amtStr);
        if (amount <= 0) {
            Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser sender = FirebaseAuth.getInstance().getCurrentUser();
        if (sender == null) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.orderByChild("accountNumber").equalTo(acc).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(SendMoneyActivity.this, "Recipient not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (DataSnapshot child : snapshot.getChildren()) {
                    String receiverUid = child.getKey();
                    String receiverName = child.child("name").getValue(String.class);
                    if (sender.getUid().equals(receiverUid)) {
                        Toast.makeText(SendMoneyActivity.this, "Cannot send to yourself", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    confirmAndTransfer(sender.getUid(), receiverUid, receiverName, amount);
                    break;
                }
            }
            @Override public void onCancelled(DatabaseError error) { Toast.makeText(SendMoneyActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show(); }
        });
    }

    private void confirmAndTransfer(String senderUid, String receiverUid, String receiverName, double amount) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Transfer")
                .setMessage("Send Rs. " + amount + " to " + receiverName + "?")
                .setPositiveButton("Send", (d, w) -> performAtomicTransfer(senderUid, receiverUid, amount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private double amountFrom(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private void performAtomicTransfer(String senderUid, String receiverUid, double amount) {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference senderBalRef = root.child("Users").child(senderUid).child("balance");
        DatabaseReference receiverBalRef = root.child("Users").child(receiverUid).child("balance");
        DatabaseReference txRef = root.child("Transactions").push();
        String txId = txRef.getKey();

        senderBalRef.runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Double cur = currentData.getValue(Double.class);
                if (cur == null) cur = 0.0;
                if (cur < amount) {
                    return Transaction.abort();
                }
                currentData.setValue(cur - amount);
                return Transaction.success(currentData);
            }
            @Override public void onComplete(@NonNull DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (!committed) {
                    Toast.makeText(SendMoneyActivity.this, "Insufficient balance", Toast.LENGTH_SHORT).show();
                    return;
                }
                receiverBalRef.runTransaction(new Transaction.Handler() {
                    @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Double cur = currentData.getValue(Double.class);
                        if (cur == null) cur = 0.0;
                        currentData.setValue(cur + amount);
                        return Transaction.success(currentData);
                    }
                    @Override public void onComplete(@NonNull DatabaseError error2, boolean committed2, DataSnapshot currentData2) {
                        if (!committed2) {
                            senderBalRef.runTransaction(new Transaction.Handler() {
                                @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData d) {
                                    Double cur2 = d.getValue(Double.class); if (cur2 == null) cur2 = 0.0; d.setValue(cur2 + amount); return Transaction.success(d);
                                }
                                @Override public void onComplete(@NonNull DatabaseError e3, boolean c3, DataSnapshot s3) { }
                            });
                            Toast.makeText(SendMoneyActivity.this, "Transfer failed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Map<String, Object> tx = new HashMap<>();
                        tx.put("transactionId", txId);
                        tx.put("senderUid", senderUid);
                        tx.put("receiverUid", receiverUid);
                        tx.put("amount", amount);
                        tx.put("timestamp", System.currentTimeMillis());
                        tx.put("type", "P2P_TRANSFER");
                        txRef.updateChildren(tx).addOnCompleteListener(t -> Toast.makeText(SendMoneyActivity.this, "Transfer successful", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
    }
}