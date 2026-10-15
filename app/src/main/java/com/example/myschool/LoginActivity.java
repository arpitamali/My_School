package com.example.myschool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private TextView tabAdmin;
    private TextView tabTeacher;
    private TextView tabParentStudent;

    private EditText etUsername;
    private EditText etPassword;

    private ImageView ivTogglePassword;

    private Button btnLogin;

    private TextView tvTrial;
    private TextView tvRegisterSchool;
    private TextView tvContactUs;


    // =========================================================
    // LOGIN STATE
    // =========================================================

    private String selectedRole = "Admin";

    private boolean isPasswordVisible = false;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;


    // =========================================================
    // SHARED PREFERENCES
    // =========================================================

    private SharedPreferences preferences;

    private static final String PREF_NAME =
            "MySchoolPrefs";

    private static final String KEY_IS_LOGGED_IN =
            "isLoggedIn";

    private static final String KEY_ROLE =
            "role";

    private static final String KEY_STUDENT_ID =
            "studentId";

    private static final String KEY_STAFF_ID =
            "staffId";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        // Always use light theme
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);


        // -----------------------------------------------------
        // PREFERENCES
        // -----------------------------------------------------

        preferences =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );


        // -----------------------------------------------------
        // CHECK SAVED LOGIN
        // -----------------------------------------------------

        if (preferences.getBoolean(
                KEY_IS_LOGGED_IN,
                false
        )) {

            openSavedDashboard();

            return;
        }


        // -----------------------------------------------------
        // LAYOUT
        // -----------------------------------------------------

        setContentView(
                R.layout.activity_login
        );


        // -----------------------------------------------------
        // FIREBASE
        // -----------------------------------------------------

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // -----------------------------------------------------
        // INITIALIZE
        // -----------------------------------------------------

        initViews();

        setupRoleTabs();

        setupPasswordToggle();

        setupClickListeners();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        tabAdmin =
                findViewById(
                        R.id.tabAdmin
                );

        tabTeacher =
                findViewById(
                        R.id.tabTeacher
                );

        tabParentStudent =
                findViewById(
                        R.id.tabParentStudent
                );


        etUsername =
                findViewById(
                        R.id.etUsername
                );

        etPassword =
                findViewById(
                        R.id.etPassword
                );


        ivTogglePassword =
                findViewById(
                        R.id.ivTogglePassword
                );


        btnLogin =
                findViewById(
                        R.id.btnLogin
                );




        tvTrial =
                findViewById(
                        R.id.tvTrial
                );

        tvRegisterSchool =
                findViewById(
                        R.id.tvRegisterSchool
                );

        tvContactUs =
                findViewById(
                        R.id.tvContactUs
                );
    }


    // =========================================================
    // ROLE TABS
    // =========================================================

    private void setupRoleTabs() {

        View.OnClickListener listener =
                v -> {

                    resetTabs();


                    v.setBackgroundResource(
                            R.drawable.bg_tab_selected
                    );


                    ((TextView) v)
                            .setTextColor(
                                    getResources()
                                            .getColor(
                                                    android.R.color.white
                                            )
                            );


                    if (v.getId() ==
                            R.id.tabAdmin) {

                        selectedRole =
                                "Admin";

                    } else if (
                            v.getId() ==
                                    R.id.tabTeacher
                    ) {

                        selectedRole =
                                "Teacher";

                    } else {

                        selectedRole =
                                "Parent/Student";
                    }
                };


        tabAdmin.setOnClickListener(
                listener
        );

        tabTeacher.setOnClickListener(
                listener
        );

        tabParentStudent.setOnClickListener(
                listener
        );
    }


    // =========================================================
    // RESET TABS
    // =========================================================

    private void resetTabs() {

        tabAdmin.setBackground(null);

        tabTeacher.setBackground(null);

        tabParentStudent.setBackground(null);


        int grey =
                getResources()
                        .getColor(
                                android.R.color.darker_gray
                        );


        tabAdmin.setTextColor(grey);

        tabTeacher.setTextColor(grey);

        tabParentStudent.setTextColor(grey);
    }


    // =========================================================
    // PASSWORD TOGGLE
    // =========================================================

    private void setupPasswordToggle() {

        ivTogglePassword.setOnClickListener(
                v -> {

                    isPasswordVisible =
                            !isPasswordVisible;


                    if (isPasswordVisible) {

                        etPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );


                        ivTogglePassword.setImageResource(
                                R.drawable.ic_eye
                        );

                    } else {

                        etPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );


                        ivTogglePassword.setImageResource(
                                R.drawable.ic_eye_off
                        );
                    }


                    etPassword.setSelection(
                            etPassword
                                    .getText()
                                    .length()
                    );
                }
        );
    }


    // =========================================================
    // CLICK LISTENERS
    // =========================================================

    private void setupClickListeners() {

        // LOGIN
        btnLogin.setOnClickListener(
                v -> attemptLogin()
        );

// APP DEMO
        tvTrial.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    AppDemoActivity.class
            );

            startActivity(intent);
        });
        // REGISTER SCHOOL
        tvRegisterSchool.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    LoginActivity.this,
                                    SchoolRegistrationStep1.class
                            );

                    startActivity(intent);
                }
        );


        // CONTACT US
        tvContactUs.setOnClickListener(v -> {

            new androidx.appcompat.app.AlertDialog.Builder(
                    LoginActivity.this
            )
                    .setTitle("Contact Us")
                    .setMessage(
                            "Don't have an account?\n\n"
                                    + "Please contact your school "
                                    + "administrator to create your "
                                    + "Admin, Teacher or Parent account.\n\n"
                                    + "Your school administrator can "
                                    + "provide the correct login details."
                    )
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

    }


    // =========================================================
    // ATTEMPT LOGIN
    // =========================================================

    private void attemptLogin() {

        String username =
                etUsername
                        .getText()
                        .toString()
                        .trim();


        String password =
                etPassword
                        .getText()
                        .toString();


        // -----------------------------------------------------
        // USERNAME
        // -----------------------------------------------------

        if (TextUtils.isEmpty(username)) {

            etUsername.setError(
                    "Email or mobile number required"
            );

            etUsername.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // PASSWORD
        // -----------------------------------------------------

        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    "Password required"
            );

            etPassword.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // DISABLE BUTTON
        // -----------------------------------------------------

        btnLogin.setEnabled(false);

        btnLogin.setText(
                "LOGGING IN..."
        );


        // =====================================================
        // ADMIN
        // =====================================================

        if (selectedRole.equals("Admin")) {

            loginAdmin(
                    username,
                    password
            );

            return;
        }


        // =====================================================
        // TEACHER
        // =====================================================

        if (selectedRole.equals("Teacher")) {

            loginTeacher(
                    username,
                    password
            );

            return;
        }


        // =====================================================
        // PARENT / STUDENT
        // =====================================================

        if (
                selectedRole.equals(
                        "Parent/Student"
                )
        ) {

            loginParentStudent(
                    username,
                    password
            );
        }
    }


    // =========================================================
    // ADMIN LOGIN
    // =========================================================

    private void loginAdmin(
            String email,
            String password
    ) {

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            resetLoginButton();

            etUsername.setError(
                    "Enter registered admin email"
            );

            etUsername.requestFocus();

            return;
        }


        signInAdmin(
                email,
                password
        );
    }


    // =========================================================
    // ADMIN AUTH
    // =========================================================

    private void signInAdmin(
            String email,
            String password
    ) {

        mAuth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                showLoginError(
                                        task,
                                        "Invalid admin email or password"
                                );

                                return;
                            }


                            loginSuccess(
                                    "Admin",
                                    ""
                            );
                        }
                );
    }


    // =========================================================
    // TEACHER LOGIN
    // =========================================================

    private void loginTeacher(
            String username,
            String password
    ) {

        // -----------------------------------------------------
        // MOBILE LOGIN
        // -----------------------------------------------------

        if (isPhone(username)) {

            String phone =
                    normalizedPhone(username);


            db.collection("staff")
                    .whereEqualTo(
                            "mobile",
                            phone
                    )
                    .whereEqualTo(
                            "active",
                            true
                    )
                    .limit(1)
                    .get()
                    .addOnSuccessListener(
                            snapshot -> {

                                if (snapshot.isEmpty()) {

                                    resetLoginButton();

                                    Toast.makeText(
                                            this,
                                            "Teacher mobile number not found.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }


                                DocumentSnapshot staff =
                                        snapshot
                                                .getDocuments()
                                                .get(0);


                                String email =
                                        staff.getString(
                                                "email"
                                        );


                                if (TextUtils.isEmpty(
                                        email
                                )) {

                                    resetLoginButton();

                                    Toast.makeText(
                                            this,
                                            "Teacher email is not linked.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }


                                signInTeacher(
                                        email.trim(),
                                        password
                                );
                            }
                    )
                    .addOnFailureListener(
                            e -> {

                                resetLoginButton();

                                Toast.makeText(
                                        this,
                                        "Unable to find teacher account.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                    );


            return;
        }


        // -----------------------------------------------------
        // EMAIL LOGIN
        // -----------------------------------------------------

        if (!Patterns.EMAIL_ADDRESS
                .matcher(username)
                .matches()) {

            resetLoginButton();

            etUsername.setError(
                    "Enter valid teacher email or mobile number"
            );

            etUsername.requestFocus();

            return;
        }


        signInTeacher(
                username,
                password
        );
    }


    // =========================================================
    // TEACHER AUTH
    // =========================================================

    private void signInTeacher(
            String email,
            String password
    ) {

        mAuth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                showLoginError(
                                        task,
                                        "Invalid teacher email/mobile or password"
                                );

                                return;
                            }


                            verifyTeacherAccount();
                        }
                );
    }


    // =========================================================
    // VERIFY TEACHER
    // =========================================================

    private void verifyTeacherAccount() {

        if (mAuth.getCurrentUser() == null) {

            resetLoginButton();

            Toast.makeText(
                    this,
                    "Login session not found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        String authUid =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("staff")
                .whereEqualTo(
                        "authUid",
                        authUid
                )
                .whereEqualTo(
                        "active",
                        true
                )
                .limit(1)
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            if (snapshot.isEmpty()) {

                                mAuth.signOut();

                                resetLoginButton();

                                Toast.makeText(
                                        this,
                                        "Teacher account is not linked with school.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            DocumentSnapshot staff =
                                    snapshot
                                            .getDocuments()
                                            .get(0);


                            String staffId =
                                    staff.getId();


                            String staffType =
                                    staff.getString(
                                            "staffType"
                                    );


                            String role =
                                    staff.getString(
                                            "role"
                                    );


                            // -------------------------------------------------
                            // ALLOW TEACHER STAFF
                            // -------------------------------------------------

                            boolean isTeacher =
                                    "Teacher".equalsIgnoreCase(
                                            staffType
                                    )
                                            ||
                                            "Teacher".equalsIgnoreCase(
                                                    role
                                            );


                            if (!isTeacher) {

                                mAuth.signOut();

                                resetLoginButton();

                                Toast.makeText(
                                        this,
                                        "This account is not a teacher account.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            // -------------------------------------------------
                            // SUCCESS
                            // -------------------------------------------------

                            loginSuccess(
                                    "Teacher",
                                    staffId
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            mAuth.signOut();

                            resetLoginButton();

                            Toast.makeText(
                                    this,
                                    "Unable to verify teacher account.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // PARENT / STUDENT LOGIN
    // =========================================================

    private void loginParentStudent(
            String username,
            String password
    ) {

        // Parent / Student login = EMAIL ONLY
        if (!Patterns.EMAIL_ADDRESS
                .matcher(username)
                .matches()) {

            resetLoginButton();
            etUsername.setError("Enter valid email");
            etUsername.requestFocus();
            return;
        }

        signInParentStudent(username, password);
    }


    // =========================================================
    // PARENT / STUDENT AUTH
    // =========================================================

    private void signInParentStudent(
            String email,
            String password
    ) {

        mAuth.signInWithEmailAndPassword(
                        email.trim(),
                        password
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {
                                showLoginError(
                                        task,
                                        "Invalid email or password"
                                );
                                return;
                            }

                            verifyStudentAccount();
                        }
                );
    }


    // =========================================================
    // VERIFY STUDENT
    // =========================================================

    private void verifyStudentAccount() {

        if (mAuth.getCurrentUser() == null) {
            resetLoginButton();
            Toast.makeText(
                    this,
                    "Login session not found.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String authUid =
                mAuth.getCurrentUser().getUid();

        db.collection("students")
                .whereEqualTo("authUid", authUid)
                .whereEqualTo("status", "Active")
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        mAuth.signOut();
                        resetLoginButton();
                        Toast.makeText(
                                this,
                                "Student account is not linked with school.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    DocumentSnapshot student =
                            snapshot.getDocuments().get(0);

                    loginSuccess(
                            "Parent/Student",
                            student.getId()
                    );
                })
                .addOnFailureListener(e -> {

                    mAuth.signOut();
                    resetLoginButton();

                    Toast.makeText(
                            this,
                            "Unable to verify student account.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

// =========================================================
// CHECK PHONE
// =========================================================

    private boolean isPhone(String value) {

        if (value == null) {
            return false;
        }

        String digits = value.replaceAll(
                "[^0-9]",
                ""
        );

        return digits.length() == 10
                && digits.matches(
                "[6-9][0-9]{9}"
        );
    }


// =========================================================
// NORMALIZED PHONE
// =========================================================

    private String normalizedPhone(String value) {

        if (value == null) {
            return "";
        }

        String digits = value.replaceAll(
                "[^0-9]",
                ""
        );

        if (digits.length() > 10) {
            digits = digits.substring(
                    digits.length() - 10
            );
        }

        return digits;
    }
    // =========================================================
    // LOGIN SUCCESS
    // =========================================================

    private void loginSuccess(
            String role,
            String id
    ) {

        // -----------------------------------------------------
        // SAVE LOGIN
        // -----------------------------------------------------

        SharedPreferences.Editor editor =
                preferences.edit();


        editor.putBoolean(
                KEY_IS_LOGGED_IN,
                true
        );


        editor.putString(
                KEY_ROLE,
                role
        );


        // -----------------------------------------------------
        // STUDENT ID
        // -----------------------------------------------------

        if (role.equals(
                "Parent/Student"
        )) {

            editor.putString(
                    KEY_STUDENT_ID,
                    id
            );

            editor.remove(
                    KEY_STAFF_ID
            );

        }

        // -----------------------------------------------------
        // STAFF ID
        // -----------------------------------------------------

        else if (role.equals(
                "Teacher"
        )) {

            editor.putString(
                    KEY_STAFF_ID,
                    id
            );

            editor.remove(
                    KEY_STUDENT_ID
            );

        }

        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

        else {

            editor.remove(
                    KEY_STUDENT_ID
            );

            editor.remove(
                    KEY_STAFF_ID
            );
        }


        editor.apply();


        // =====================================================
        // OPEN DASHBOARD
        // =====================================================

        Intent intent;


        if (role.equals("Admin")) {

            intent =
                    new Intent(
                            this,
                            AdminDashboardActivity.class
                    );

        } else if (
                role.equals("Teacher")
        ) {

            intent =
                    new Intent(
                            this,
                            TeacherDashboardActivity.class
                    );

            intent.putExtra(
                    "staffId",
                    id
            );

        } else {

            intent =
                    new Intent(
                            this,
                            ParentDashboardActivity.class
                    );

            intent.putExtra(
                    "studentId",
                    id
            );
        }


        intent.putExtra(
                "role",
                role
        );


        // -----------------------------------------------------
        // CLEAR LOGIN SCREEN
        // -----------------------------------------------------

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }


    // =========================================================
    // OPEN SAVED DASHBOARD
    // =========================================================

    private void openSavedDashboard() {

        String savedRole =
                preferences.getString(
                        KEY_ROLE,
                        ""
                );


        Intent intent;


        // =====================================================
        // ADMIN
        // =====================================================

        if (savedRole.equals(
                "Admin"
        )) {

            intent =
                    new Intent(
                            this,
                            AdminDashboardActivity.class
                    );
        }


        // =====================================================
        // TEACHER
        // =====================================================

        else if (
                savedRole.equals(
                        "Teacher"
                )
        ) {

            String staffId =
                    preferences.getString(
                            KEY_STAFF_ID,
                            ""
                    );


            if (staffId.isEmpty()) {

                preferences.edit()
                        .clear()
                        .apply();

                return;
            }


            intent =
                    new Intent(
                            this,
                            TeacherDashboardActivity.class
                    );


            intent.putExtra(
                    "staffId",
                    staffId
            );
        }


        // =====================================================
        // PARENT / STUDENT
        // =====================================================

        else if (
                savedRole.equals(
                        "Parent/Student"
                )
        ) {

            String studentId =
                    preferences.getString(
                            KEY_STUDENT_ID,
                            ""
                    );


            if (studentId.isEmpty()) {

                preferences.edit()
                        .clear()
                        .apply();

                return;
            }


            intent =
                    new Intent(
                            this,
                            ParentDashboardActivity.class
                    );


            intent.putExtra(
                    "studentId",
                    studentId
            );
        }


        // =====================================================
        // UNKNOWN ROLE
        // =====================================================

        else {

            preferences.edit()
                    .clear()
                    .apply();

            return;
        }


        // =====================================================
        // ROLE EXTRA
        // =====================================================

        intent.putExtra(
                "role",
                savedRole
        );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }


    // =========================================================
    // RESET LOGIN BUTTON
    // =========================================================

    private void resetLoginButton() {

        if (btnLogin != null) {

            btnLogin.setEnabled(true);

            btnLogin.setText(
                    "LOGIN"
            );
        }
    }


    // =========================================================
    // LOGIN ERROR
    // =========================================================

    private void showLoginError(
            com.google.android.gms.tasks.Task<?> task,
            String defaultMessage
    ) {

        resetLoginButton();


        String error =
                defaultMessage;


        if (task.getException() != null &&
                task.getException().getMessage() != null) {

            error =
                    task.getException()
                            .getMessage();
        }


        Toast.makeText(
                this,
                error,
                Toast.LENGTH_LONG
        ).show();
    }
}
