package com.example.apnapay;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyCardsFragment extends Fragment {

    private RecyclerView rv;
    private final List<java.util.Map<String,Object>> cards = new ArrayList<>();
    private SimpleCardsAdapter adapter;

    public MyCardsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_cards, container, false);
        RecyclerView rvLocal = new RecyclerView(requireContext());
        ((ViewGroup)v).addView(rvLocal);
        rvLocal.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SimpleCardsAdapter(cards);
        rvLocal.setAdapter(adapter);
        loadCards();
        return v;
    }

    private void loadCards() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Cards").child(user.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                cards.clear();
                for (DataSnapshot c : snapshot.getChildren()) {
                    java.util.Map<String,Object> m = (java.util.Map<String, Object>) c.getValue();
                    if (m != null) cards.add(m);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}