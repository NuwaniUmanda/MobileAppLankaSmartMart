package com.example.lankasmartmart;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;

    View btnBack;
    Button btnManualEntry, btnHelp;
    View scannerLine;
    TextView scanningStatus;
    PreviewView cameraPreview;
    AppCompatTextView navHome, navCategories, navCart, navProfile;

    ObjectAnimator scanAnimator;
    ExecutorService cameraExecutor;
    boolean barcodeDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        btnBack        = findViewById(R.id.btnBack);
        btnManualEntry = findViewById(R.id.btnManualEntry);
        btnHelp        = findViewById(R.id.btnHelp);
        scannerLine    = findViewById(R.id.scannerLine);
        scanningStatus = findViewById(R.id.scanningStatus);
        cameraPreview  = findViewById(R.id.cameraPreview);
        navHome        = findViewById(R.id.navHome);
        navCategories  = findViewById(R.id.navCategories);
        navCart        = findViewById(R.id.navCart);
        navProfile     = findViewById(R.id.navProfile);

        cameraExecutor = Executors.newSingleThreadExecutor();

        startScanAnimation();

        // Request camera permission or start camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }

        btnBack.setOnClickListener(v -> finish());
        btnManualEntry.setOnClickListener(v -> showManualEntryDialog());
        btnHelp.setOnClickListener(v -> showHelpDialog());

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        navCategories.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductsActivity.class);
            intent.putExtra("CATEGORY", "All");
            startActivity(intent);
        });
        navCart.setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_EAN_13,
                                Barcode.FORMAT_EAN_8,
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_CODE_128,
                                Barcode.FORMAT_CODE_39,
                                Barcode.FORMAT_UPC_A,
                                Barcode.FORMAT_UPC_E)
                        .build();

                BarcodeScanner scanner = BarcodeScanning.getClient(options);

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (barcodeDetected) {
                        imageProxy.close();
                        return;
                    }

                    @SuppressWarnings("UnsafeOptInUsageError")
                    InputImage image = InputImage.fromMediaImage(
                            imageProxy.getImage(),
                            imageProxy.getImageInfo().getRotationDegrees());

                    scanner.process(image)
                            .addOnSuccessListener(barcodes -> {
                                for (Barcode barcode : barcodes) {
                                    String value = barcode.getRawValue();
                                    if (value != null && !value.isEmpty()) {
                                        barcodeDetected = true;
                                        runOnUiThread(() -> handleBarcodeResult(value));
                                        break;
                                    }
                                }
                            })
                            .addOnCompleteListener(task -> imageProxy.close());
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Camera permission is required to scan barcodes",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleBarcodeResult(String barcode) {
        if (scanAnimator != null) scanAnimator.cancel();
        scanningStatus.setText("Found: " + barcode);

        Product found = findProductByBarcode(barcode);

        if (found != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Product Found!")
                    .setMessage("📦 " + found.getName()
                            + "\n💰 LKR " + String.format("%.2f", found.getPrice())
                            + "\n🏷️ " + found.getCategory()
                            + "\n\n" + (found.getStock() > 0
                            ? "✅ In Stock (" + found.getStock() + " available)"
                            : "❌ Out of Stock"))
                    .setPositiveButton("View Details", (dialog, which) -> {
                        Intent intent = new Intent(this, ProductDetailsActivity.class);
                        intent.putExtra("PRODUCT_ID",          found.getId());
                        intent.putExtra("PRODUCT_NAME",        found.getName());
                        intent.putExtra("PRODUCT_PRICE",       found.getPrice());
                        intent.putExtra("PRODUCT_CATEGORY",    found.getCategory());
                        intent.putExtra("PRODUCT_DESCRIPTION", found.getDescription());
                        intent.putExtra("PRODUCT_STOCK",       found.getStock());
                        intent.putExtra("PRODUCT_IMAGE",       found.getImageResource());
                        startActivity(intent);
                        barcodeDetected = false;
                    })
                    .setNegativeButton("Scan Again", (dialog, which) -> {
                        barcodeDetected = false;
                        scanningStatus.setText("Scanning...");
                        startScanAnimation();
                    })
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Product Not Found")
                    .setMessage("No product matched barcode:\n" + barcode
                            + "\n\nThis product is not in our catalogue.")
                    .setPositiveButton("Scan Again", (dialog, which) -> {
                        barcodeDetected = false;
                        scanningStatus.setText("Scanning...");
                        startScanAnimation();
                    })
                    .setNegativeButton("Manual Entry", (dialog, which) ->
                            showManualEntryDialog())
                    .show();
        }
    }

    private Product findProductByBarcode(String barcode) {
        HashMap<String, Integer> barcodeMap = new HashMap<>();
        barcodeMap.put("4890008100015", 1);   // Basmati Rice
        barcodeMap.put("4890008100022", 2);   // Imorich French Vanilla
        barcodeMap.put("4890008100039", 3);   // Munchee Choc Shock
        barcodeMap.put("4890008100046", 4);   // Tiara Sponge Cake
        barcodeMap.put("4890008100053", 5);   // Vim Dishwash
        barcodeMap.put("4890008100060", 6);   // Lysol Disinfectant
        barcodeMap.put("4890008100077", 7);   // Lux Soap
        barcodeMap.put("4890008100084", 8);   // Sunsilk Shampoo
        barcodeMap.put("4890008100091", 9);   // Promate Notebook
        barcodeMap.put("4890008100107", 10);  // Atlas Pen

        Integer productId = barcodeMap.get(barcode);
        if (productId == null) return null;

        List<Product> products = new ArrayList<>();
        products.add(new Product(1, "Basmati Rice - 1kg", 350.00,
                "Groceries", "Premium quality basmati rice", 10,
                R.drawable.img_rice));
        products.add(new Product(2, "Imorich French Vanilla - 1L", 1290.00,
                "Groceries", "Rich and creamy French vanilla ice cream", 0,
                R.drawable.img_ice_cream));
        products.add(new Product(3, "Munchee Choc Shock - 90g", 300.00,
                "Groceries", "Delicious chocolate biscuits", 20,
                R.drawable.img_chocolate));
        products.add(new Product(4, "Tiara Sponge Layer Cake - 310g", 550.00,
                "Groceries", "Soft and fluffy sponge cake", 8,
                R.drawable.img_cake));
        products.add(new Product(5, "Vim Dishwash Liquid Anti Smell 500ml", 450.00,
                "Household", "Anti smell dishwash liquid 500ml", 50,
                R.drawable.img_vim_dishwash));
        products.add(new Product(6, "Lysol Lavender Disinfectant 500ml", 500.00,
                "Household", "Lavender disinfectant kills 99.9% germs", 30,
                R.drawable.img_lysol));
        products.add(new Product(7, "Lux Soap Jasmine And Vitamin E 100g", 170.00,
                "Personal Care", "Jasmine and Vitamin E moisturizing soap", 40,
                R.drawable.img_lux_soap));
        products.add(new Product(8, "Sunsilk Onion & Jojoba Oil Shampoo 200ml", 750.00,
                "Personal Care", "Hair fall resist shampoo", 15,
                R.drawable.img_sunsilk));
        products.add(new Product(9, "Promate Notebook Single A6 80P", 90.00,
                "Stationery", "Single ruled A6 notebook 80 pages", 100,
                R.drawable.img_promate_notebook));
        products.add(new Product(10, "Atlas Pen Chooty II Assorted 3Pkt", 85.00,
                "Stationery", "Assorted color pen pack of 3", 75,
                R.drawable.img_atlas_pen));

        for (Product p : products) {
            if (p.getId() == productId) return p;
        }
        return null;
    }

    private void startScanAnimation() {
        scannerLine.post(() -> {
            int parentHeight = ((View) scannerLine.getParent()).getHeight();
            scanAnimator = ObjectAnimator.ofFloat(
                    scannerLine, "translationY", 0f, parentHeight - 4f);
            scanAnimator.setDuration(2000);
            scanAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scanAnimator.setRepeatMode(ValueAnimator.REVERSE);
            scanAnimator.setInterpolator(new LinearInterpolator());
            scanAnimator.start();
        });
    }

    private void showManualEntryDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter barcode number");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Manual Entry")
                .setMessage("Enter the product barcode manually:")
                .setView(input)
                .setPositiveButton("Search", (dialog, which) -> {
                    String barcode = input.getText().toString().trim();
                    if (!barcode.isEmpty()) {
                        handleBarcodeResult(barcode);
                    } else {
                        Toast.makeText(this,
                                "Please enter a barcode", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("How to Scan")
                .setMessage("1. Allow camera permission when prompted\n\n"
                        + "2. Hold your phone steady\n\n"
                        + "3. Point camera at any barcode or QR code\n\n"
                        + "4. The app will automatically detect it\n\n"
                        + "5. If scanning fails, use Manual Entry\n\n"
                        + "📋 Test barcodes:\n"
                        + "Basmati Rice: 4890008100015\n"
                        + "Munchee Choc: 4890008100039\n"
                        + "Lux Soap: 4890008100077")
                .setPositiveButton("Got it", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanAnimator != null) scanAnimator.cancel();
        cameraExecutor.shutdown();
    }
}