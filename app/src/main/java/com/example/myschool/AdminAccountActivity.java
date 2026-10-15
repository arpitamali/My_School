package com.example.myschool;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class AdminAccountActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private TextView tvAdminName;
    private TextView tvAdminEmail;

    private TextView tvNameValue;
    private TextView tvEmailValue;
    private TextView tvMobileValue;

    private Button btnEditAdmin;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListenerRegistration schoolListener;

    // =========================================================
    // ADMIN DATA
    // =========================================================

    private String adminName = "";
    private String adminMobile = "";
    private String adminEmail = "";

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_account
        );

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        initViews();

        // Listeners
        setupListeners();

        // Real-time admin data
        startAdminRealtimeListener();
    }

    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        tvAdminName =
                findViewById(
                        R.id.tvAdminName
                );

        tvAdminEmail =
                findViewById(
                        R.id.tvAdminEmail
                );

        tvNameValue =
                findViewById(
                        R.id.tvNameValue
                );

        tvEmailValue =
                findViewById(
                        R.id.tvEmailValue
                );

        tvMobileValue =
                findViewById(
                        R.id.tvMobileValue
                );

        btnEditAdmin =
                findViewById(
                        R.id.btnEditAdmin
                );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnEditAdmin.setOnClickListener(
                v -> showEditAdminDialog()
        );
    }

    // =========================================================
    // REAL-TIME ADMIN LISTENER
    // =========================================================

    private void startAdminRealtimeListener() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String adminUid =
                currentUser.getUid();

        schoolListener =
                db.collection("schools")
                        .document(adminUid)
                        .addSnapshotListener(
                                (document, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Unable to load admin information.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }

                                    if (document == null
                                            || !document.exists()) {

                                        loadAuthEmailOnly();

                                        tvAdminName.setText(
                                                "Admin"
                                        );

                                        tvNameValue.setText(
                                                "Admin"
                                        );

                                        tvMobileValue.setText(
                                                "Not available"
                                        );

                                        return;
                                    }

                                    updateAdminUI(
                                            document
                                    );
                                }
                        );
    }

    // =========================================================
    // UPDATE UI FROM FIRESTORE
    // =========================================================

    private void updateAdminUI(
            DocumentSnapshot document
    ) {

        adminName =
                getString(
                        document,
                        "principalName"
                );

        adminMobile =
                getString(
                        document,
                        "adminMobile"
                );

        /*
         * First preference:
         * email stored in schools document.
         *
         * If not available:
         * Firebase Authentication email.
         */

        adminEmail =
                getString(
                        document,
                        "adminEmail"
                );

        if (adminEmail.isEmpty()) {

            FirebaseUser user =
                    auth.getCurrentUser();

            if (user != null
                    && user.getEmail() != null) {

                adminEmail =
                        user.getEmail();
            }
        }

        if (adminName.isEmpty()) {
            adminName = "Admin";
        }

        if (adminMobile.isEmpty()) {
            adminMobile = "Not available";
        }

        if (adminEmail.isEmpty()) {
            adminEmail = "Not available";
        }

        // Update UI
        tvAdminName.setText(
                adminName
        );

        tvNameValue.setText(
                adminName
        );

        tvAdminEmail.setText(
                adminEmail
        );

        tvEmailValue.setText(
                adminEmail
        );

        tvMobileValue.setText(
                adminMobile
        );
    }

    // =========================================================
    // AUTH EMAIL FALLBACK
    // =========================================================

    private void loadAuthEmailOnly() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user != null
                && user.getEmail() != null
                && !user.getEmail()
                .trim()
                .isEmpty()) {

            adminEmail =
                    user.getEmail();

            tvAdminEmail.setText(
                    adminEmail
            );

            tvEmailValue.setText(
                    adminEmail
            );

        } else {

            tvAdminEmail.setText(
                    "Not available"
            );

            tvEmailValue.setText(
                    "Not available"
            );
        }
    }

    // =========================================================
    // EDIT ADMIN DIALOG
    // =========================================================

    private void showEditAdminDialog() {

        View view =
                LayoutInflater
                        .from(this)
                        .inflate(
                                R.layout.dialog_edit_admin,
                                null
                        );

        EditText etAdminName =
                view.findViewById(
                        R.id.etAdminName
                );

        EditText etAdminEmail =
                view.findViewById(
                        R.id.etAdminEmail
                );

        EditText etAdminMobile =
                view.findViewById(
                        R.id.etAdminMobile
                );

        // Existing values
        etAdminName.setText(
                adminName.equals("Admin")
                        ? ""
                        : adminName
        );

        if (!adminEmail.equals(
                "Not available"
        )) {

            etAdminEmail.setText(
                    adminEmail
            );
        }

        if (!adminMobile.equals(
                "Not available"
        )) {

            etAdminMobile.setText(
                    adminMobile
            );
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Admin")
                        .setView(view)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(
                            v -> {

                                String name =
                                        etAdminName
                                                .getText()
                                                .toString()
                                                .trim();

                                String email =
                                        etAdminEmail
                                                .getText()
                                                .toString()
                                                .trim();

                                String mobile =
                                        etAdminMobile
                                                .getText()
                                                .toString()
                                                .trim();

                                // ==========================
                                // VALIDATION
                                // ==========================

                                if (name.isEmpty()) {

                                    etAdminName.setError(
                                            "Enter admin name"
                                    );

                                    return;
                                }

                                if (email.isEmpty()) {

                                    etAdminEmail.setError(
                                            "Enter admin email"
                                    );

                                    return;
                                }

                                if (!android.util.Patterns.EMAIL_ADDRESS
                                        .matcher(email)
                                        .matches()) {

                                    etAdminEmail.setError(
                                            "Enter valid email"
                                    );

                                    return;
                                }

                                if (mobile.isEmpty()) {

                                    etAdminMobile.setError(
                                            "Enter mobile number"
                                    );

                                    return;
                                }

                                if (mobile.length() != 10) {

                                    etAdminMobile.setError(
                                            "Enter valid 10 digit mobile number"
                                    );

                                    return;
                                }

                                saveAdmin(
                                        name,
                                        email,
                                        mobile,
                                        dialog
                                );
                            }
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // SAVE ADMIN
    // =========================================================

    private void saveAdmin(
            String name,
            String email,
            String mobile,
            AlertDialog dialog
    ) {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String adminUid =
                currentUser.getUid();

        String oldEmail =
                currentUser.getEmail();

        if (oldEmail == null) {
            oldEmail = "";
        }

        // =====================================================
        // EMAIL CHANGE
        // =====================================================

        if (!email.equalsIgnoreCase(
                oldEmail
        )) {

            currentUser.updateEmail(email)
                    .addOnSuccessListener(
                            unused -> {

                                saveAdminToFirestore(
                                        adminUid,
                                        name,
                                        email,
                                        mobile,
                                        dialog
                                );
                            }
                    )
                    .addOnFailureListener(
                            e -> {

                                Toast.makeText(
                                        this,
                                        getEmailUpdateErrorMessage(
                                                e
                                        ),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                    );

        } else {

            saveAdminToFirestore(
                    adminUid,
                    name,
                    email,
                    mobile,
                    dialog
            );
        }
    }

    // =========================================================
    // SAVE TO FIRESTORE
    // =========================================================

    private void saveAdminToFirestore(
            String adminUid,
            String name,
            String email,
            String mobile,
            AlertDialog dialog
    ) {

        Map<String, Object> update =
                new HashMap<>();

        update.put(
                "principalName",
                name
        );

        update.put(
                "adminEmail",
                email
        );

        update.put(
                "adminMobile",
                mobile
        );

        update.put(
                "updatedAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        db.collection("schools")
                .document(adminUid)
                .update(update)
                .addOnSuccessListener(
                        unused -> {

                            /*
                             * UI will also be updated automatically
                             * by addSnapshotListener().
                             */

                            dialog.dismiss();

                            Toast.makeText(
                                    this,
                                    "Admin information updated successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to update admin information: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // EMAIL ERROR
    // =========================================================

    private String getEmailUpdateErrorMessage(
            Exception e
    ) {

        String message =
                e.getMessage();

        if (message == null) {

            return "Unable to update email.";
        }

        String lower =
                message.toLowerCase();

        if (lower.contains(
                "requires-recent-login"
        )) {

            return "For security, please login again and then change the email.";
        }

        if (lower.contains(
                "email-already-in-use"
        )) {

            return "This email is already used by another account.";
        }

        if (lower.contains(
                "invalid-email"
        )) {

            return "Invalid email address.";
        }

        return "Unable to update email: "
                + message;
    }

    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key
    ) {

        Object value =
                document.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(
                value
        ).trim();
    }

    // =========================================================
    // REMOVE FIRESTORE LISTENER
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (schoolListener != null) {

            schoolListener.remove();

            schoolListener = null;
        }
    }
}