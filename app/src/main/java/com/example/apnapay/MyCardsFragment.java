package com.example.apnapay;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseException;

import com.example.apnapay.data.Db;

public class MyCardsFragment extends Fragment {

    // Card include refs (same pattern as homeFragment)
    private TextView tvBalance, tvCardNumber, tvCardHolder, tvExpiry, tvCvv;

    public MyCardsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_cards, container, false);

        // Find views from included layout (same IDs as in home)
        tvBalance = v.findViewById(R.id.tvBalance);
        tvCardNumber = v.findViewById(R.id.tvCardNumber);
        tvCardHolder = v.findViewById(R.id.tvCardHolder);
        tvExpiry = v.findViewById(R.id.tvExpiry);
        tvCvv = v.findViewById(R.id.tvCvv);

        // Placeholders (match homeFragment defaults)
        tvBalance.setText("Rs. 0.00");
        tvCardNumber.setText("•••• •••• •••• ••••");
        tvCardHolder.setText("LOADING...");
        tvExpiry.setText("••/••");
        tvCvv.setText("•••");

        // Request Card Button (unchanged)
        MaterialCardView cvRequestCard = v.findViewById(R.id.cvRequestCard);
        cvRequestCard.setOnClickListener(view -> confirmCardRequest());

        // Bind user balance and default card (same pattern as homeFragment)
        bindUserBalance();
        bindDefaultCard();

        return v;
    }

    private void bindUserBalance() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference userRef = Db.db().getReference("Users").child(user.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double bal = null;
                try {
                    bal = snapshot.child("balance").getValue(Double.class);
                } catch (DatabaseException ignored) { }
                if (bal == null) {
                    Long l = null;
                    try {
                        l = snapshot.child("balance").getValue(Long.class);
                    } catch (DatabaseException ignored) { }
                    if (l != null) bal = l.doubleValue();
                }
                if (bal != null) {
                    tvBalance.setText(getString(R.string.rs_amount, bal));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void bindDefaultCard() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference cardsRef = Db.db().getReference("Cards").child(user.getUid());
        cardsRef.limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String number = child.child("cardNumber").getValue(String.class);
                    String holder = child.child("cardHolderName").getValue(String.class);
                    String expiry = child.child("expiryDate").getValue(String.class);
                    String cvv = child.child("cvv").getValue(String.class);
                    if (number != null) tvCardNumber.setText(number);
                    if (holder != null) tvCardHolder.setText(holder);
                    if (expiry != null) tvExpiry.setText(getString(R.string.expiry_fmt, expiry));
                    if (cvv != null) tvCvv.setText(getString(R.string.cvv_fmt, cvv));
                    break;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void confirmCardRequest() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Request New Card")
                .setMessage("Would you like to email ApnaPay support to issue a new virtual/physical card for your account?")
                .setPositiveButton("Send Request", (dialog, which) -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822"); // ensures only email apps

                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@apnapay.com"});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New Card Request - " + user.getEmail());
                    emailIntent.putExtra(Intent.EXTRA_TEXT,
                            "Hello Admin,\n\nI would like to request a new ApnaPay card for my account.\n\n" +
                                    "User ID: " + user.getUid() +
                                    "\nEmail: " + user.getEmail() +
                                    "\n\nPlease let me know the next steps.\n\nThank you.");
                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send Request via Email..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(requireContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}