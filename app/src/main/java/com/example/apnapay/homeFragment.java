package com.example.apnapay;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

public class homeFragment extends Fragment {

    public homeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 1. FIRST: Inflate the layout and save it to a variable named 'view'
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        MaterialCardView cvRequestMoney = view.findViewById(R.id.cvRequestMoney);
        MaterialCardView cvLoadMoney = view.findViewById(R.id.cvLoadMoney);

        cvRequestMoney.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SendMoneyActivity.class);
            startActivity(intent);
        });

        cvLoadMoney.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoadMoney.class);
            startActivity(intent);
        });


        // 2. NOW: You can find the views inside that 'view' variable
        TextView tvSeeMore = view.findViewById(R.id.tvSeeMore);

        // 3. Set the click listener
        tvSeeMore.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TransactionHistory.class);
            startActivity(intent);
        });

        // 4. FINALLY: Return the fully prepared view to the screen
        return view;
    }
}