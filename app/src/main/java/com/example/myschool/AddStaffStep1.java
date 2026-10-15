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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AddStaffStep1 extends AppCompatActivity {

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

    private boolean isEdit = false;
    private String staffId = "";

    private FirebaseFirestore db;

    private final ActivityResultLauncher<String[]> imagePicker =
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
                        }

                        photoUri = uri.toString();

                        btnSelectPhoto.setText("✓ Photo Selected");

                        Toast.makeText(
                                this,
                                "Staff photo selected",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(R.layout.activity_add_staff_step1);

        db = FirebaseFirestore.getInstance();

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        staffId = getIntent().getStringExtra("staffId");

        initViews();
        setupSpinners();
        setupDatePicker();
        setupListeners();
        setupSystemBack();

        if (isEdit && staffId != null && !staffId.trim().isEmpty()) {
            loadExistingStaff();
        }
    }

    private void initViews() {

        scrollView = findViewById(R.id.scrollView);

        btnBack = findViewById(R.id.btnBack);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnNext = findViewById(R.id.btnNext);

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etAadhaar = findViewById(R.id.etAadhaar);
        etBirthPlace = findViewById(R.id.etBirthPlace);
        etNationality = findViewById(R.id.etNationality);
        etMotherTongue = findViewById(R.id.etMotherTongue);
        etCaste = findViewById(R.id.etCaste);
        etSubCaste = findViewById(R.id.etSubCaste);

        spGender = findViewById(R.id.spGender);
        spBloodGroup = findViewById(R.id.spBloodGroup);
        spReligion = findViewById(R.id.spReligion);
        spCategory = findViewById(R.id.spCategory);
        spSpecialNeeds = findViewById(R.id.spSpecialNeeds);
    }

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
            String[] values
    ) {

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

    private void setupListeners() {

        btnBack.setOnClickListener(v ->
                finish()
        );

        btnSelectPhoto.setOnClickListener(v ->
                imagePicker.launch(
                        new String[]{"image/*"}
                )
        );

        btnNext.setOnClickListener(v -> {

            if (!validateForm()) {
                return;
            }

            goToNextStep();
        });
    }

    private boolean validateForm() {

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

        String aadhaar =
                etAadhaar.getText()
                        .toString()
                        .trim();

        String nationality =
                etNationality.getText()
                        .toString()
                        .trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }

        if (middleName.isEmpty()) {
            etMiddleName.setError("Middle name is required");
            etMiddleName.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return false;
        }

        if (dob.isEmpty()) {
            etDateOfBirth.setError("Date of birth is required");
            etDateOfBirth.requestFocus();
            return false;
        }

        if (spGender.getSelectedItemPosition() == 0) {
            Toast.makeText(
                    this,
                    "Please select gender",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (!aadhaar.isEmpty() && aadhaar.length() != 12) {
            etAadhaar.setError(
                    "Aadhaar must contain 12 digits"
            );
            etAadhaar.requestFocus();
            return false;
        }

        if (nationality.isEmpty()) {
            etNationality.setError(
                    "Nationality is required"
            );
            etNationality.requestFocus();
            return false;
        }

        return true;
    }

    private void loadExistingStaff() {

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(
                                this,
                                "Staff record not found.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    etFirstName.setText(getString(document, "firstName"));
                    etMiddleName.setText(getString(document, "middleName"));
                    etLastName.setText(getString(document, "lastName"));
                    etDateOfBirth.setText(getString(document, "dateOfBirth"));
                    etAadhaar.setText(getString(document, "aadhaar"));
                    etBirthPlace.setText(getString(document, "birthPlace"));
                    etNationality.setText(getString(document, "nationality"));
                    etMotherTongue.setText(getString(document, "motherTongue"));
                    etCaste.setText(getString(document, "caste"));
                    etSubCaste.setText(getString(document, "subCaste"));

                    setSpinnerSelection(
                            spGender,
                            getString(document, "gender")
                    );

                    setSpinnerSelection(
                            spBloodGroup,
                            getString(document, "bloodGroup")
                    );

                    setSpinnerSelection(
                            spReligion,
                            getString(document, "religion")
                    );

                    setSpinnerSelection(
                            spCategory,
                            getString(document, "category")
                    );

                    setSpinnerSelection(
                            spSpecialNeeds,
                            getString(document, "specialNeeds")
                    );

                    photoUri =
                            getString(
                                    document,
                                    "staffPhoto"
                            );

                    if (!photoUri.isEmpty()) {
                        btnSelectPhoto.setText(
                                "✓ Existing Photo"
                        );
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load staff details.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void setSpinnerSelection(
            Spinner spinner,
            String value
    ) {

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        ArrayAdapter adapter =
                (ArrayAdapter) spinner.getAdapter();

        int position =
                adapter.getPosition(value);

        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private String getString(
            DocumentSnapshot document,
            String key
    ) {

        Object value =
                document.get(key);

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }

    private void goToNextStep() {

        Intent intent =
                new Intent(
                        AddStaffStep1.this,
                        AddStaffStep2.class
                );

        Bundle extras =
                getIntent().getExtras();

        if (extras != null) {
            intent.putExtras(extras);
        }

        intent.putExtra("isEdit", isEdit);
        intent.putExtra("staffId", staffId);

        intent.putExtra(
                "staffPhoto",
                photoUri
        );

        intent.putExtra(
                "firstName",
                etFirstName.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "middleName",
                etMiddleName.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "lastName",
                etLastName.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "dateOfBirth",
                etDateOfBirth.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "gender",
                spGender.getSelectedItem().toString()
        );

        intent.putExtra(
                "bloodGroup",
                spBloodGroup.getSelectedItem().toString()
        );

        intent.putExtra(
                "aadhaar",
                etAadhaar.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "birthPlace",
                etBirthPlace.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "nationality",
                etNationality.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "motherTongue",
                etMotherTongue.getText()
                        .toString()
                        .trim()
        );

        intent.putExtra(
                "religion",
                spReligion.getSelectedItem().toString()
        );

        intent.putExtra(
                "category",
                spCategory.getSelectedItem().toString()
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
                spSpecialNeeds.getSelectedItem().toString()
        );

        startActivity(intent);
    }

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