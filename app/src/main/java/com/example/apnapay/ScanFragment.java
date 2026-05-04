package com.example.apnapay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class ScanFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    barcodeView.resume();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        barcodeView = view.findViewById(R.id.barcode_scanner);

        // Tell the scanner what to do when it finds a QR code
        barcodeView.decodeContinuous(result -> {
            if (result.getText() != null) {
                // We found a QR Code!
                // Pause scanner so it doesn't scan the same code 100 times a second
                barcodeView.pause();

                String scannedData = result.getText();
                Toast.makeText(getContext(), "Scanned: " + scannedData, Toast.LENGTH_SHORT).show();

                // TODO: Here is where you would pass this data to the "Send Money" screen
                // Intent intent = new Intent(getActivity(), SendMoneyActivity.class);
                // intent.putExtra("QR_ACCOUNT_NUMBER", scannedData);
                // startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the fragment opens, check if we have permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        } else {
            // Ask for permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Important: Pause the camera when the user goes to another tab, to save battery!
        barcodeView.pause();
    }
}