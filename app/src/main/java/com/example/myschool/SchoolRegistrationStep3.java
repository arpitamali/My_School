package com.example.myschool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SchoolRegistrationStep3 extends AppCompatActivity {

    private ImageButton btnBack;

    private ImageView ivSchoolLogo;

    private TextView tvSchoolInfo;

    private TextView tvAdminInfo;

    private CheckBox checkConfirm;

    private CheckBox checkTerms;

    private Button btnRegister;


    // FIREBASE

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;


    // STEP 1 DATA

    private String schoolName = "";

    private String udise = "";

    private String branch = "";

    private String principal = "";

    private String address = "";

    private String logoUri = "";


    // STEP 2 DATA

    private String adminName = "";

    private String email = "";

    private String mobile = "";

    private String password = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_school_registration_step3
        );


        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        initViews();

        loadRegistrationData();

        displaySchoolInformation();

        displayAdminInformation();

        displaySchoolLogo();

        setupBackButton();

        setupRegisterButton();

        setupSystemBackButton();
    }


    private void initViews() {

        btnBack =
                findViewById(R.id.btnBack);

        ivSchoolLogo =
                findViewById(R.id.ivSchoolLogo);

        tvSchoolInfo =
                findViewById(R.id.tvSchoolInfo);

        tvAdminInfo =
                findViewById(R.id.tvAdminInfo);

        checkConfirm =
                findViewById(R.id.checkConfirm);

        checkTerms =
                findViewById(R.id.checkTerms);

        btnRegister =
                findViewById(R.id.btnRegister);
    }


    // =========================================================
    // LOAD DATA
    // =========================================================

    private void loadRegistrationData() {

        Intent intent = getIntent();


        schoolName =
                getSafeString(
                        intent,
                        "schoolName"
                );


        udise =
                getSafeString(
                        intent,
                        "udise"
                );


        branch =
                getSafeString(
                        intent,
                        "branch"
                );


        principal =
                getSafeString(
                        intent,
                        "principal"
                );


        address =
                getSafeString(
                        intent,
                        "address"
                );


        logoUri =
                getSafeString(
                        intent,
                        "logoUri"
                );


        adminName =
                getSafeString(
                        intent,
                        "adminName"
                );


        email =
                getSafeString(
                        intent,
                        "email"
                );


        mobile =
                getSafeString(
                        intent,
                        "mobile"
                );


        password =
                getSafeString(
                        intent,
                        "password"
                );
    }


    private String getSafeString(
            Intent intent,
            String key) {

        String value =
                intent.getStringExtra(key);

        return value == null
                ? ""
                : value;
    }


    // =========================================================
    // SHOW SELECTED LOGO
    // =========================================================

    private void displaySchoolLogo() {

        if (!TextUtils.isEmpty(logoUri)) {

            try {

                Uri uri =
                        Uri.parse(logoUri);

                ivSchoolLogo.setImageURI(
                        uri
                );

            } catch (Exception e) {

                ivSchoolLogo.setImageResource(
                        R.drawable.ic_school
                );
            }

        } else {

            ivSchoolLogo.setImageResource(
                    R.drawable.ic_school
            );
        }
    }


    // =========================================================
    // SCHOOL INFORMATION
    // =========================================================

    private void displaySchoolInformation() {

        String info =
                "School Name     : "
                        + safeText(schoolName)
                        + "\n\n"

                        + "UDISE Number    : "
                        + safeText(udise)
                        + "\n\n"

                        + "School Branch   : "
                        + safeText(branch)
                        + "\n\n"

                        + "Principal Name  : "
                        + safeText(principal)
                        + "\n\n"

                        + "School Address  : "
                        + safeText(address);


        tvSchoolInfo.setText(
                info
        );
    }


    // =========================================================
    // ADMIN INFORMATION
    // =========================================================

    private void displayAdminInformation() {

        String info =
                "Admin Name      : "
                        + safeText(adminName)
                        + "\n\n"

                        + "Email Address   : "
                        + safeText(email)
                        + "\n\n"

                        + "Mobile Number   : "
                        + safeText(mobile);


        tvAdminInfo.setText(
                info
        );
    }


    private String safeText(
            String value) {

        if (TextUtils.isEmpty(value)) {

            return "Not available";
        }

        return value;
    }


    // =========================================================
    // BACK BUTTON
    // =========================================================

    private void setupBackButton() {

        btnBack.setOnClickListener(v ->
                goBackToStep2()
        );
    }


    private void setupSystemBackButton() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        goBackToStep2();
                    }
                }
        );
    }


    private void goBackToStep2() {

        Intent intent =
                new Intent(
                        SchoolRegistrationStep3.this,
                        SchoolRegistrationStep2.class
                );


        intent.putExtra(
                "schoolName",
                schoolName
        );

        intent.putExtra(
                "udise",
                udise
        );

        intent.putExtra(
                "branch",
                branch
        );

        intent.putExtra(
                "principal",
                principal
        );

        intent.putExtra(
                "address",
                address
        );

        intent.putExtra(
                "logoUri",
                logoUri
        );


        intent.putExtra(
                "adminName",
                adminName
        );

        intent.putExtra(
                "email",
                email
        );

        intent.putExtra(
                "mobile",
                mobile
        );

        intent.putExtra(
                "password",
                password
        );


        startActivity(intent);

        finish();
    }


    // =========================================================
    // REGISTER
    // =========================================================

    private void setupRegisterButton() {

        btnRegister.setOnClickListener(v ->
                registerSchool()
        );
    }


    private void registerSchool() {

        if (!checkConfirm.isChecked()) {

            Toast.makeText(
                    this,
                    "Please confirm that all information is correct.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (!checkTerms.isChecked()) {

            Toast.makeText(
                    this,
                    "Please agree to the Terms & Conditions.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (TextUtils.isEmpty(email)) {

            Toast.makeText(
                    this,
                    "Admin email is missing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (TextUtils.isEmpty(password)) {

            Toast.makeText(
                    this,
                    "Admin password is missing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        btnRegister.setEnabled(false);

        btnRegister.setText(
                "REGISTERING..."
        );


        // =====================================================
        // FIREBASE AUTH
        // =====================================================

        mAuth.createUserWithEmailAndPassword(
                        email,
                        password
                )

                .addOnSuccessListener(
                        authResult -> {

                            if (authResult.getUser()
                                    == null) {

                                registrationFailed(
                                        "Unable to create admin account."
                                );

                                return;
                            }


                            String uid =
                                    authResult.getUser()
                                            .getUid();


                            saveSchoolData(uid);
                        }
                )

                .addOnFailureListener(
                        e -> {

                            registrationFailed(
                                    getAuthErrorMessage(
                                            e.getMessage()
                                    )
                            );
                        }
                );
    }


    // =========================================================
    // SAVE FIRESTORE DATA
    // =========================================================

    private void saveSchoolData(
            String uid) {

        Map<String, Object> schoolData =
                new HashMap<>();


        // SCHOOL

        schoolData.put(
                "schoolName",
                schoolName
        );

        schoolData.put(
                "udiseNumber",
                udise
        );

        schoolData.put(
                "schoolBranch",
                branch
        );

        schoolData.put(
                "principalName",
                principal
        );

        schoolData.put(
                "schoolAddress",
                address
        );


        // ADMIN

        schoolData.put(
                "adminName",
                adminName
        );

        schoolData.put(
                "adminEmail",
                email
        );

        schoolData.put(
                "adminMobile",
                mobile
        );


        // ACCOUNT

        schoolData.put(
                "uid",
                uid
        );

        schoolData.put(
                "role",
                "Admin"
        );

        schoolData.put(
                "status",
                "active"
        );


        // DEFAULT COUNTS

        schoolData.put(
                "studentCount",
                0
        );

        schoolData.put(
                "teacherCount",
                0
        );

        schoolData.put(
                "presentCount",
                0
        );

        schoolData.put(
                "birthdayCount",
                0
        );


        // DATE

        schoolData.put(
                "registeredAt",
                FieldValue.serverTimestamp()
        );


        // =====================================================
        // FIRESTORE
        // =====================================================

        db.collection("schools")
                .document(uid)
                .set(schoolData)

                .addOnSuccessListener(
                        unused ->
                                registrationSuccessful()
                )

                .addOnFailureListener(
                        e ->
                                registrationFailed(
                                        "School data save failed: "
                                                + e.getMessage()
                                )
                );
    }


    // =========================================================
    // SUCCESS
    // =========================================================

    private void registrationSuccessful() {

        Toast.makeText(
                this,
                "Registration Successful!",
                Toast.LENGTH_LONG
        ).show();


        Intent intent =
                new Intent(
                        SchoolRegistrationStep3.this,
                        AdminDashboardActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }


    // =========================================================
    // FAILURE
    // =========================================================

    private void registrationFailed(
            String message) {

        btnRegister.setEnabled(true);

        btnRegister.setText(
                "REGISTER SCHOOL  ✓"
        );


        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }


    // =========================================================
    // AUTH ERROR
    // =========================================================

    private String getAuthErrorMessage(
            String error) {

        if (error == null) {

            return "Registration failed.";
        }


        if (error.contains(
                "email address is already in use")) {

            return "This email is already registered.";
        }


        if (error.contains(
                "badly formatted")) {

            return "Invalid email address.";
        }


        if (error.contains(
                "password is invalid")) {

            return "Password must be at least 6 characters.";
        }


        if (error.contains(
                "network")) {

            return "Network error. Check your internet connection.";
        }


        return "Registration failed: " + error;
    }
}