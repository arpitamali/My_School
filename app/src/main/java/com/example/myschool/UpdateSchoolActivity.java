package com.example.myschool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class UpdateSchoolActivity
        extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private ImageView ivSchoolLogo;

    private Button btnChangeLogo;
    private Button btnSaveChanges;

    private EditText etSchoolName;
    private EditText etUdiseNumber;
    private EditText etSchoolBranch;
    private EditText etPrincipalName;
    private EditText etSchoolAddress;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    // =========================================================
    // SCHOOL LOGO URI
    // =========================================================

    private String schoolLogoUri = "";


    // =========================================================
    // IMAGE PICKER
    // =========================================================

    private final ActivityResultLauncher<String[]>
            logoPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {

                        if (uri == null) {
                            return;
                        }

                        try {

                            getContentResolver()
                                    .takePersistableUriPermission(
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    );

                        } catch (Exception ignored) {
                            // Some providers may not support
                            // persistable permissions.
                        }


                        schoolLogoUri =
                                uri.toString();


                        ivSchoolLogo.setImageURI(
                                uri
                        );
                    }
            );


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_update_school
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =====================================================
        // INIT
        // =====================================================

        initViews();


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // CHANGE LOGO
        // =====================================================

        btnChangeLogo.setOnClickListener(
                v ->
                        logoPickerLauncher.launch(
                                new String[]{"image/*"}
                        )
        );


        // =====================================================
        // SAVE
        // =====================================================

        btnSaveChanges.setOnClickListener(
                v -> saveSchool()
        );


        // =====================================================
        // LOAD EXISTING SCHOOL DATA
        // =====================================================

        loadSchool();
    }


    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        ivSchoolLogo =
                findViewById(
                        R.id.ivSchoolLogo
                );

        btnChangeLogo =
                findViewById(
                        R.id.btnChangeLogo
                );

        btnSaveChanges =
                findViewById(
                        R.id.btnSaveChanges
                );

        etSchoolName =
                findViewById(
                        R.id.etSchoolName
                );

        etUdiseNumber =
                findViewById(
                        R.id.etUdiseNumber
                );

        etSchoolBranch =
                findViewById(
                        R.id.etSchoolBranch
                );

        etPrincipalName =
                findViewById(
                        R.id.etPrincipalName
                );

        etSchoolAddress =
                findViewById(
                        R.id.etSchoolAddress
                );
    }


    // =========================================================
    // LOAD SCHOOL
    // =========================================================

    private void loadSchool() {

        if (
                auth == null ||
                        auth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        db.collection("schools")
                .document(schoolId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                Toast.makeText(
                                        this,
                                        "School information not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            // =================================================
                            // SCHOOL NAME
                            // =================================================

                            etSchoolName.setText(
                                    getString(
                                            document,
                                            "schoolName"
                                    )
                            );


                            // =================================================
                            // UDISE NUMBER
                            // =================================================

                            etUdiseNumber.setText(
                                    getString(
                                            document,
                                            "udiseNumber"
                                    )
                            );


                            // =================================================
                            // SCHOOL BRANCH
                            // =================================================

                            etSchoolBranch.setText(
                                    getString(
                                            document,
                                            "schoolBranch"
                                    )
                            );


                            // =================================================
                            // PRINCIPAL NAME
                            // =================================================

                            etPrincipalName.setText(
                                    getString(
                                            document,
                                            "principalName"
                                    )
                            );


                            // =================================================
                            // SCHOOL ADDRESS
                            // =================================================

                            String address =
                                    getString(
                                            document,
                                            "schoolAddress"
                                    );


                            if (
                                    address.isEmpty()
                            ) {

                                address =
                                        getString(
                                                document,
                                                "address"
                                        );
                            }


                            etSchoolAddress.setText(
                                    address
                            );


                            // =================================================
                            // SCHOOL LOGO
                            // =================================================

                            schoolLogoUri =
                                    getString(
                                            document,
                                            "schoolLogoUri"
                                    );


                            loadLogo(
                                    schoolLogoUri
                            );
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to load school information.",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }


    // =========================================================
    // LOAD LOGO
    // =========================================================

    private void loadLogo(
            String logoUri
    ) {

        if (
                logoUri == null ||
                        logoUri.trim().isEmpty()
        ) {

            ivSchoolLogo.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );

            return;
        }


        try {

            Uri uri =
                    Uri.parse(
                            logoUri
                    );


            // =====================================================
            // TRY TO TAKE PERSISTENT PERMISSION AGAIN
            // =====================================================

            try {

                getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

            } catch (Exception ignored) {
            }


            // =====================================================
            // SHOW IMAGE
            // =====================================================

            ivSchoolLogo.setImageURI(
                    uri
            );

        } catch (Exception e) {

            ivSchoolLogo.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }
    }


    // =========================================================
    // SAVE SCHOOL
    // =========================================================

    private void saveSchool() {

        if (
                auth == null ||
                        auth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // GET VALUES
        // =====================================================

        String schoolName =
                etSchoolName.getText()
                        .toString()
                        .trim();


        String udiseNumber =
                etUdiseNumber.getText()
                        .toString()
                        .trim();


        String schoolBranch =
                etSchoolBranch.getText()
                        .toString()
                        .trim();


        String principalName =
                etPrincipalName.getText()
                        .toString()
                        .trim();


        String schoolAddress =
                etSchoolAddress.getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATION
        // =====================================================

        if (schoolName.isEmpty()) {

            etSchoolName.setError(
                    "Enter school name"
            );

            etSchoolName.requestFocus();

            return;
        }


        if (udiseNumber.isEmpty()) {

            etUdiseNumber.setError(
                    "Enter UDISE number"
            );

            etUdiseNumber.requestFocus();

            return;
        }


        if (principalName.isEmpty()) {

            etPrincipalName.setError(
                    "Enter principal name"
            );

            etPrincipalName.requestFocus();

            return;
        }


        if (schoolAddress.isEmpty()) {

            etSchoolAddress.setError(
                    "Enter school address"
            );

            etSchoolAddress.requestFocus();

            return;
        }


        // =====================================================
        // DISABLE SAVE BUTTON
        // =====================================================

        btnSaveChanges.setEnabled(
                false
        );

        btnSaveChanges.setText(
                "SAVING..."
        );


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        // =====================================================
        // UPDATE DATA
        // =====================================================

        Map<String, Object> update =
                new HashMap<>();


        update.put(
                "schoolName",
                schoolName
        );


        update.put(
                "udiseNumber",
                udiseNumber
        );


        update.put(
                "schoolBranch",
                schoolBranch
        );


        update.put(
                "principalName",
                principalName
        );


        update.put(
                "schoolAddress",
                schoolAddress
        );


        update.put(
                "schoolLogoUri",
                schoolLogoUri
        );


        update.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );


        // =====================================================
        // FIRESTORE UPDATE
        // =====================================================

        db.collection("schools")
                .document(schoolId)
                .update(update)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "School information updated successfully.",
                                    Toast.LENGTH_LONG
                            ).show();


                            setResult(
                                    RESULT_OK
                            );


                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            btnSaveChanges.setEnabled(
                                    true
                            );

                            btnSaveChanges.setText(
                                    "SAVE CHANGES"
                            );


                            Toast.makeText(
                                    this,
                                    "Unable to update school information.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            com.google.firebase.firestore.DocumentSnapshot document,
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
}