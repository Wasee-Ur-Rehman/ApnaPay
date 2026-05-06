package com.example.apnapay;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apnapay.data.Db;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import android.util.Log;

import java.util.Locale;

public class homeFragment extends Fragment {

    private TextView tvGreeting, tvBalance, tvCardNumber, tvCardHolder, tvExpiry, tvCvv, tvAccountNumber;
    private RecyclerView rvTransactions;
    private java.util.List<itemTransaction> txData = new java.util.ArrayList<>();
    private TransactionAdapter txAdapter;
    private String currentAccountNumber = null; // store account number for share/copy
    private double currentBalance = 0.0;

    public homeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Existing
        tvGreeting = view.findViewById(R.id.tvGreeting);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Card include refs
        tvBalance = view.findViewById(R.id.tvBalance);
        tvCardNumber = view.findViewById(R.id.tvCardNumber);
        tvCardHolder = view.findViewById(R.id.tvCardHolder);
        tvExpiry = view.findViewById(R.id.tvExpiry);
        tvCvv = view.findViewById(R.id.tvCvv);

        // Add Account No label under greeting
        tvAccountNumber = view.findViewById(R.id.tvAccountNumber);

        // Add this right after finding your views in onCreateView:
        tvGreeting.setText("Loading...");
        tvBalance.setText("Rs. 0.00");
        tvCardNumber.setText("•••• •••• •••• ••••");
        tvCardHolder.setText("LOADING...");
        tvExpiry.setText("••/••");
        tvCvv.setText("•••");

        // Ensure the card tile text shows 'Send Money' (some devices may keep old resources cached)
        View cvRequest = view.findViewById(R.id.cvRequestMoney);
        if (cvRequest instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) cvRequest;
            for (int i=0;i<vg.getChildCount();i++) {
                View child = vg.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView)child).setText("Send\nMoney");
                    break;
                }
            }
        }

        // REQUEST MONEY should open SendMoneyActivity (not share)
        view.findViewById(R.id.cvRequestMoney).setOnClickListener(v -> {
            Intent it = new Intent(getActivity(), SendMoneyActivity.class);
            it.putExtra("balance", currentBalance);
            it.putExtra("accountNumber", currentAccountNumber);
            startActivity(it);
        });
        view.findViewById(R.id.cvLoadMoney).setOnClickListener(v -> startActivity(new android.content.Intent(getActivity(), LoadMoney.class)));
        view.findViewById(R.id.tvSeeMore).setOnClickListener(v -> startActivity(new android.content.Intent(getActivity(), TransactionHistory.class)));

        txAdapter = new TransactionAdapter(txData, FirebaseAuth.getInstance().getCurrentUser()!=null?FirebaseAuth.getInstance().getCurrentUser().getUid():"");
        rvTransactions.setAdapter(txAdapter);

        bindUserData();
        bindDefaultCard();
        bindRecentTransactions();

        return view;
    }

    private void bindUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference userRef = Db.db().getReference("Users").child(user.getUid());
        Log.d("ApnaPay", "Listening Users/"+user.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ApnaPay", "User snapshot exists="+snapshot.exists());
                Double balance = snapshot.child("balance").getValue(Double.class);
                String accountNumber = snapshot.child("accountNumber").getValue(String.class);
                if (balance != null) {
                    currentBalance = balance;
                    tvBalance.setText(getString(R.string.rs_amount, balance));
                }
                if (accountNumber != null) {
                    currentAccountNumber = accountNumber;
                    tvAccountNumber.setText(getString(R.string.account_no_fmt, accountNumber));
                    tvAccountNumber.setOnClickListener(v -> {
                        ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(ClipData.newPlainText("accountNumber", accountNumber));
                        Toast.makeText(requireContext(), R.string.copied_account_no, Toast.LENGTH_SHORT).show();
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e("ApnaPay", "User listen cancelled: "+error.getMessage()); }
        });
    }

    private void bindDefaultCard() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference cardsRef = Db.db().getReference("Cards").child(user.getUid());
        cardsRef.limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ApnaPay", "Cards size="+snapshot.getChildrenCount());
                for (DataSnapshot child : snapshot.getChildren()) {
                    String number = child.child("cardNumber").getValue(String.class);
                    String holder = child.child("cardHolderName").getValue(String.class);
                    String expiry = child.child("expiryDate").getValue(String.class);
                    String cvv = child.child("cvv").getValue(String.class);
                    if (number != null) tvCardNumber.setText(number);
                    if (holder != null) {
                        tvCardHolder.setText(holder);
                        // Update top greeting to card holder name in uppercase as requested
                        tvGreeting.setText(holder.toUpperCase(Locale.US));
                    }
                    if (expiry != null) tvExpiry.setText(getString(R.string.expiry_fmt, expiry));
                    if (cvv != null) tvCvv.setText(getString(R.string.cvv_fmt, cvv));
                    break;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void bindRecentTransactions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference txRef = Db.db().getReference("Transactions");

        txRef.orderByChild("timestamp").limitToLast(20).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                txData.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String sender = child.child("senderUid").getValue(String.class);
                    String receiver = child.child("receiverUid").getValue(String.class);
                    Double amount = child.child("amount").getValue(Double.class);
                    Long ts = child.child("timestamp").getValue(Long.class);
                    String type = child.child("type").getValue(String.class);
                    if (sender == null || receiver == null || amount == null || ts == null) continue;
                    if (user.getUid().equals(sender) || user.getUid().equals(receiver)) {
                        txData.add(new itemTransaction(sender, receiver, amount, ts, type));
                    }
                }
                txData.sort((a,b) -> Long.compare(b.timestamp, a.timestamp));
                txAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}