package com.example.myschool;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AddStudentStep1 extends AppCompatActivity {

    private NestedScrollView scrollView;

    private ImageButton btnBack;
    private Button btnSelectPhoto;
    private Button btnNext;

    private EditText etFirstName;
    private EditText etMiddleName;
    private EditText etLastName;
    private EditText etDateOfBirth;
    private EditText etAadhaar;
    private EditText etBirthPlace;
    private EditText etNationality;
    private EditText etMotherTongue;
    private EditText etCaste;
    private EditText etSubCaste;

    private Spinner spGender;
    private Spinner spBloodGroup;
    private Spinner spReligion;
    private Spinner spCategory;
    private Spinner spSpecialNeeds;

    private String photoUri = "";

    // =========================================================
    // EDIT MODE
    // =========================================================

    private boolean editMode = false;
    private String studentId = "";

    private FirebaseFirestore db;

    // =========================================================
    // IMAGE PICKER
    // =========================================================

    private final ActivityResultLauncher<String[]> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {

                        if (uri != null) {

                            try {

                                getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );

                            } catch (Exception ignored) {
                            }

                            photoUri = uri.toString();

                            btnSelectPhoto.setText(
                                    "✓ Photo Selected"
                            );

                            Toast.makeText(
                                    this,
                                    "Student photo selected",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(
                R.layout.activity_add_student_step1
        );

        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinners();
        setupDatePicker();
        setupListeners();
        setupSystemBack();

        // Check whether this is Add or Edit
        checkEditMode();
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        scrollView =
                findViewById(R.id.scrollView);

        btnBack =
                findViewById(R.id.btnBack);

        btnSelectPhoto =
                findViewById(R.id.btnSelectPhoto);

        btnNext =
                findViewById(R.id.btnNext);

        etFirstName =
                findViewById(R.id.etFirstName);

        etMiddleName =
                findViewById(R.id.etMiddleName);

        etLastName =
                findViewById(R.id.etLastName);

        etDateOfBirth =
                findViewById(R.id.etDateOfBirth);

        etAadhaar =
                findViewById(R.id.etAadhaar);

        etBirthPlace =
                findViewById(R.id.etBirthPlace);

        etNationality =
                findViewById(R.id.etNationality);

        etMotherTongue =
                findViewById(R.id.etMotherTongue);

        etCaste =
                findViewById(R.id.etCaste);

        etSubCaste =
                findViewById(R.id.etSubCaste);

        spGender =
                findViewById(R.id.spGender);

        spBloodGroup =
                findViewById(R.id.spBloodGroup);

        spReligion =
                findViewById(R.id.spReligion);

        spCategory =
                findViewById(R.id.spCategory);

        spSpecialNeeds =
                findViewById(R.id.spSpecialNeeds);
    }

    // =========================================================
    // SPINNERS
    // =========================================================

    private void setupSpinners() {

        setSpinner(
                spGender,
                new String[]{
                        "Select Gender",
                        "Male",
                        "Female",
                        "Other"
                }
        );

        setSpinner(
                spBloodGroup,
                new String[]{
                        "Select Blood Group",
                        "A+",
                        "A-",
                        "B+",
                        "B-",
                        "AB+",
                        "AB-",
                        "O+",
                        "O-",
                        "Unknown"
                }
        );

        setSpinner(
                spReligion,
                new String[]{
                        "Select Religion",
                        "Hindu",
                        "Muslim",
                        "Christian",
                        "Sikh",
                        "Buddhist",
                        "Jain",
                        "Other",
                        "Prefer not to say"
                }
        );

        setSpinner(
                spCategory,
                new String[]{
                        "Select Category",
                        "General",
                        "OBC",
                        "SC",
                        "ST",
                        "EWS",
                        "Other"
                }
        );

        setSpinner(
                spSpecialNeeds,
                new String[]{
                        "Select",
                        "No",
                        "Yes"
                }
        );
    }

    private void setSpinner(
            Spinner spinner,
            String[] values) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        values
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private void setupDatePicker() {

        etDateOfBirth.setOnClickListener(v -> {

            Calendar calendar =
                    Calendar.getInstance();

            DatePickerDialog dialog =
                    new DatePickerDialog(
                            this,
                            (view, year, month, day) -> {

                                String date =
                                        String.format(
                                                "%02d/%02d/%04d",
                                                day,
                                                month + 1,
                                                year
                                        );

                                etDateOfBirth.setText(date);
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

            dialog.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            dialog.show();
        });
    }

    // =========================================================
    // CHECK ADD / EDIT MODE
    // =========================================================

    private void checkEditMode() {

        Intent intent = getIntent();

        editMode =
                intent.getBooleanExtra(
                        "editMode",
                        false
                );

        studentId =
                intent.getStringExtra(
                        "studentId"
                );

        if (studentId == null) {
            studentId = "";
        }

        // Normal Add Student
        if (!editMode || studentId.isEmpty()) {
            return;
        }

        // Edit Student
        loadStudentForEdit();
    }

    // =========================================================
    // LOAD STUDENT FROM FIRESTORE
    // =========================================================

    private void loadStudentForEdit() {

        Toast.makeText(
                this,
                "Loading student information...",
                Toast.LENGTH_SHORT
        ).show();

        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        Toast.makeText(
                                this,
                                "Student not found",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                        return;
                    }

                    // =================================================
                    // PHOTO
                    // =================================================

                    String photo =
                            getString(
                                    documentSnapshot,
                                    "studentPhoto"
                            );

                    if (!photo.isEmpty()) {

                        photoUri = photo;

                        btnSelectPhoto.setText(
                                "✓ Photo Selected"
                        );
                    }

                    // =================================================
                    // PERSONAL INFORMATION
                    // =================================================

                    etFirstName.setText(
                            getString(
                                    documentSnapshot,
                                    "firstName"
                            )
                    );

                    etMiddleName.setText(
                            getString(
                                    documentSnapshot,
                                    "middleName"
                            )
                    );

                    etLastName.setText(
                            getString(
                                    documentSnapshot,
                                    "lastName"
                            )
                    );

                    etDateOfBirth.setText(
                            getString(
                                    documentSnapshot,
                                    "dateOfBirth"
                            )
                    );

                    etAadhaar.setText(
                            getString(
                                    documentSnapshot,
                                    "aadhaar"
                            )
                    );

                    etBirthPlace.setText(
                            getString(
                                    documentSnapshot,
                                    "birthPlace"
                            )
                    );

                    etNationality.setText(
                            getString(
                                    documentSnapshot,
                                    "nationality"
                            )
                    );

                    etMotherTongue.setText(
                            getString(
                                    documentSnapshot,
                                    "motherTongue"
                            )
                    );

                    etCaste.setText(
                            getString(
                                    documentSnapshot,
                                    "caste"
                            )
                    );

                    etSubCaste.setText(
                            getString(
                                    documentSnapshot,
                                    "subCaste"
                            )
                    );

                    // =================================================
                    // SPINNERS
                    // =================================================

                    setSpinnerValue(
                            spGender,
                            getString(
                                    documentSnapshot,
                                    "gender"
                            )
                    );

                    setSpinnerValue(
                            spBloodGroup,
                            getString(
                                    documentSnapshot,
                                    "bloodGroup"
                            )
                    );

                    setSpinnerValue(
                            spReligion,
                            getString(
                                    documentSnapshot,
                                    "religion"
                            )
                    );

                    setSpinnerValue(
                            spCategory,
                            getString(
                                    documentSnapshot,
                                    "category"
                            )
                    );

                    setSpinnerValue(
                            spSpecialNeeds,
                            getString(
                                    documentSnapshot,
                                    "specialNeeds"
                            )
                    );

                    Toast.makeText(
                            this,
                            "Student information loaded",
                            Toast.LENGTH_SHORT
                    ).show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load student:\n" +
                                    e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
    }

    // =========================================================
    // GET STRING FROM FIRESTORE
    // =========================================================

    private String getString(
            com.google.firebase.firestore.DocumentSnapshot document,
            String key) {

        String value =
                document.getString(key);

        if (value == null) {
            return "";
        }

        return value;
    }

    // =========================================================
    // SET SPINNER VALUE
    // =========================================================

    private void setSpinnerValue(
            Spinner spinner,
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return;
        }

        ArrayAdapter adapter =
                (ArrayAdapter) spinner.getAdapter();

        if (adapter == null) {
            return;
        }

        int position =
                adapter.getPosition(value);

        if (position >= 0) {

            spinner.setSelection(
                    position
            );
        }
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnSelectPhoto.setOnClickListener(v ->
                imagePicker.launch(
                        new String[]{"image/*"}
                )
        );

        btnNext.setOnClickListener(v ->
                validateAndNext()
        );

        btnBack.setOnClickListener(v ->
                finish()
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateAndNext() {

        String firstName =
                etFirstName.getText()
                        .toString()
                        .trim();

        String middleName =
                etMiddleName.getText()
                        .toString()
                        .trim();

        String lastName =
                etLastName.getText()
                        .toString()
                        .trim();

        String dob =
                etDateOfBirth.getText()
                        .toString()
                        .trim();

        String gender =
                spGender.getSelectedItem()
                        .toString();

        String aadhaar =
                etAadhaar.getText()
                        .toString()
                        .trim();

        String nationality =
                etNationality.getText()
                        .toString()
                        .trim();

        // =====================================================
        // VALIDATION
        // =====================================================

        if (firstName.isEmpty()) {

            etFirstName.setError(
                    "Enter first name"
            );

            etFirstName.requestFocus();
            return;
        }

        if (middleName.isEmpty()) {

            etMiddleName.setError(
                    "Enter middle name"
            );

            etMiddleName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {

            etLastName.setError(
                    "Enter last name"
            );

            etLastName.requestFocus();
            return;
        }

        if (dob.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select date of birth",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (gender.equals("Select Gender")) {

            Toast.makeText(
                    this,
                    "Please select gender",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (nationality.isEmpty()) {

            etNationality.setError(
                    "Enter nationality"
            );

            etNationality.requestFocus();
            return;
        }

        if (!aadhaar.isEmpty() &&
                aadhaar.length() != 12) {

            etAadhaar.setError(
                    "Aadhaar number must be 12 digits"
            );

            etAadhaar.requestFocus();
            return;
        }

        // =====================================================
        // OPEN STEP 2
        // =====================================================

        Intent intent =
                new Intent(
                        AddStudentStep1.this,
                        AddStudentStep2.class
                );

        // =====================================================
        // IMPORTANT:
        // KEEP EDIT MODE + STUDENT ID
        // =====================================================

        intent.putExtra(
                "editMode",
                editMode
        );

        intent.putExtra(
                "studentId",
                studentId
        );

        // =====================================================
        // PERSONAL DATA
        // =====================================================

        intent.putExtra(
                "studentPhoto",
                photoUri
        );

        intent.putExtra(
                "firstName",
                firstName
        );

        intent.putExtra(
                "middleName",
                middleName
        );

        intent.putExtra(
                "lastName",
                lastName
        );

        intent.putExtra(
                "dateOfBirth",
                dob
        );

        intent.putExtra(
                "gender",
                gender
        );

        intent.putExtra(
                "bloodGroup",
                spBloodGroup.getSelectedItem()
                        .toString()
        );

        intent.putExtra(
                "aadhaar",
                aadhaar
        );

        intent.putExtra(
                "birthPlace",
                etBirthPlace.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "nationality",
                nationality
        );

        intent.putExtra(
                "motherTongue",
                etMotherTongue.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "religion",
                spReligion.getSelectedItem()
                        .toString()
        );

        intent.putExtra(
                "category",
                spCategory.getSelectedItem()
                        .toString()
        );

        intent.putExtra(
                "caste",
                etCaste.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "subCaste",
                etSubCaste.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "specialNeeds",
                spSpecialNeeds.getSelectedItem()
                        .toString()
        );

        startActivity(intent);
    }

    // =========================================================
    // SYSTEM BACK
    // =========================================================

    private void setupSystemBack() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                finish();
                            }
                        }
                );
    }
}