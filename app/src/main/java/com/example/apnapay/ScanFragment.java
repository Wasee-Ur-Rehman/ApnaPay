package com.example.apnapay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class ScanFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;
    private boolean isHandlingResult = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (barcodeView != null) barcodeView.resume();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // Lock orientation to avoid Activity restart during scan
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        barcodeView = view.findViewById(R.id.barcode_scanner);

        // Continuous decode with single-handling guard
        barcodeView.decodeContinuous(result -> {
            if (result.getText() != null && !isHandlingResult) {
                isHandlingResult = true;
                if (barcodeView != null) barcodeView.pause();

                String scannedData = result.getText().trim();

                if (isValidAccount(scannedData)) {
                    // Go to SendMoney screen with scanned account number
                    Intent intent = new Intent(getActivity(), SendMoneyActivity.class);
                    intent.putExtra("QR_ACCOUNT_NUMBER", scannedData);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Invalid QR. Please scan a valid ApnaPay ID", Toast.LENGTH_SHORT).show();
                    // Resume after a short delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (barcodeView != null) barcodeView.resume();
                        isHandlingResult = false;
                    }, 1200);
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Ensure back goes home (pop this fragment) instead of exiting the app
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                // Pop this fragment; if none, let Activity handle default
                if (!getParentFragmentManager().popBackStackImmediate()) {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (barcodeView != null) barcodeView.resume();
            isHandlingResult = false;
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) barcodeView.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unlock orientation back to system default when leaving scanner
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        barcodeView = null;
        isHandlingResult = false;
    }

    // Basic validation for ApnaPay account numbers: digits 8..20
    private boolean isValidAccount(@NonNull String s) {
        if (s.length() < 8 || s.length() > 20) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }
}