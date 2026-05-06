package com.example.apnapay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    private final List<itemTransaction> items;
    private final String currentUid;

    public TransactionAdapter(List<itemTransaction> items, String currentUid) {
        this.items = items;
        this.currentUid = currentUid;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        itemTransaction it = items.get(position);
        boolean incoming = currentUid.equals(it.receiverUid);

        // Determine the name to show
        String displayName = incoming ? it.senderName : it.receiverName;
        if (displayName == null) displayName = "Unknown";

        // Set Title
        h.tvTitle.setText(incoming ? "Received from " + displayName : "Sent to " + displayName);

        // Set Amount
        String amtTxt = String.format(Locale.US, (incoming ? "+Rs. %.2f" : "-Rs. %.2f"), it.amount);
        h.tvAmount.setText(amtTxt);
        int color = h.itemView.getResources().getColor(incoming ? R.color.success_green : R.color.error_red);
        h.tvAmount.setTextColor(color);

        // Set Date
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("d MMM, yyyy  •  HH:mm", Locale.US);
        h.tvDate.setText(df.format(new java.util.Date(it.timestamp)));

        // Generate Initials (e.g., "John Doe" -> "JD")
        String initials = "TX";
        if (!displayName.equals("Unknown") && !displayName.isEmpty()) {
            String[] parts = displayName.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
            } else {
                initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            }
        }
        h.tvInitials.setText(initials);
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvAmount, tvInitials;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvInitials = itemView.findViewById(R.id.tvInitials);
        }
    }
}
