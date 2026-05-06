package com.example.apnapay;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apnapay.data.Db;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistory extends AppCompatActivity {

    private RecyclerView rvAllTransactions;
    private TransactionAdapter txAdapter;
    private List<itemTransaction> txData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_history);

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        rvAllTransactions = findViewById(R.id.rvAllTransactions);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            txAdapter = new TransactionAdapter(txData, user.getUid());
            rvAllTransactions.setAdapter(txAdapter);
            fetchAllTransactions(user.getUid());
        }
    }

    private void fetchAllTransactions(String currentUid) {
        DatabaseReference txRef = Db.db().getReference("Transactions");

        // Fetch all transactions ordered by time
        txRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txData.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String sender = child.child("senderUid").getValue(String.class);
                    String receiver = child.child("receiverUid").getValue(String.class);
                    Double amount = child.child("amount").getValue(Double.class);
                    Long ts = child.child("timestamp").getValue(Long.class);
                    String type = child.child("type").getValue(String.class);

                    if (sender == null || receiver == null || amount == null || ts == null) continue;

                    // Only add if the current user is involved
                    if (currentUid.equals(sender) || currentUid.equals(receiver)) {
                        txData.add(new itemTransaction(sender, receiver, amount, ts, type));
                    }
                }

                // Sort descending (newest first)
                txData.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                txAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionHistory.this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}