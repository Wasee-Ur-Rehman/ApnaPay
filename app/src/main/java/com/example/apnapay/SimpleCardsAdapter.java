package com.example.apnapay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class SimpleCardsAdapter extends RecyclerView.Adapter<SimpleCardsAdapter.VH> {

    private final List<Map<String,Object>> cards;

    public SimpleCardsAdapter(List<Map<String, Object>> cards) { this.cards = cards; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_bank_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        java.util.Map<String, Object> m = cards.get(position);

        // Use String.valueOf to avoid NullPointerExceptions if data is missing
        String number = m.getOrDefault("cardNumber", "•••• •••• •••• ••••").toString();
        String holder = m.getOrDefault("cardHolderName", "User").toString();
        String expiry = m.getOrDefault("expiryDate", "••/••").toString();
        String cvv = m.getOrDefault("cvv", "•••").toString();

        // Find views by ID
        ((TextView)h.itemView.findViewById(R.id.tvCardNumber)).setText(number);
        ((TextView)h.itemView.findViewById(R.id.tvCardHolder)).setText(holder);
        ((TextView)h.itemView.findViewById(R.id.tvExpiry)).setText("Exp " + expiry);
        ((TextView)h.itemView.findViewById(R.id.tvCvv)).setText("CVV " + cvv);
    }

    @Override
    public int getItemCount() { return cards.size(); }

    static class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View itemView) { super(itemView); }
    }
}

