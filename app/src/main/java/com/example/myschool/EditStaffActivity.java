package com.example.myschool;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditStaffActivity extends AppCompatActivity {

    // =========================================================
    // HEADER
    // =========================================================

    private ImageButton btnBack;


    // =========================================================
    // BUTTON
    // =========================================================

    private Button btnSave;


    // =========================================================
    // BASIC INFORMATION
    // =========================================================

    private EditText etFirstName;
    private EditText etMiddleName;
    private EditText etLastName;
    private EditText etDateOfBirth;
    private EditText etGender;
    private EditText etBloodGroup;
    private EditText etAadhaar;
    private EditText etBirthPlace;
    private EditText etNationality;
    private EditText etMotherTongue;
    private EditText etReligion;
    private EditText etCategory;
    private EditText etCaste;
    private EditText etSubCaste;
    private EditText etSpecialNeeds;


    // =========================================================
    // PROFESSIONAL INFORMATION
    // =========================================================

    private EditText etEmployeeId;
    private EditText etJoiningDate;
    private EditText etQualification;
    private EditText etExperience;
    private EditText etEmploymentType;


    // =========================================================
    // STAFF TYPE & ROLE
    // =========================================================

    private EditText etStaffType;
    private EditText etRole;
    private EditText etSubject;
    private EditText etAssignedClass;
    private EditText etAssignedDivision;


    // =========================================================
    // ADDRESS
    // =========================================================

    private EditText etCurrentAddress;
    private EditText etPermanentAddress;
    private EditText etCity;
    private EditText etDistrict;
    private EditText etState;
    private EditText etPinCode;


    // =========================================================
    // EMERGENCY CONTACT
    // =========================================================

    private EditText etEmergencyName;
    private EditText etEmergencyRelation;
    private EditText etEmergencyMobile;


    // =========================================================
    // LOGIN INFORMATION
    // =========================================================

    private EditText etStaffEmail;
    private EditText etStaffPhone;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    // =========================================================
    // STAFF ID
    // =========================================================

    private String staffId;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_edit_staff
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        db =
                FirebaseFirestore.getInstance();

        mAuth =
                FirebaseAuth.getInstance();


        // =====================================================
        // GET STAFF ID
        // =====================================================

        staffId =
                getIntent().getStringExtra(
                        "staffId"
                );


        if (
                staffId == null
                        || staffId.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Staff ID not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // =====================================================
        // INITIALIZE
        // =====================================================

        initViews();

        setupListeners();

        loadStaffData();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnSave =
                findViewById(
                        R.id.btnSave
                );


        // =====================================================
        // BASIC
        // =====================================================

        etFirstName =
                findViewById(
                        R.id.etFirstName
                );

        etMiddleName =
                findViewById(
                        R.id.etMiddleName
                );

        etLastName =
                findViewById(
                        R.id.etLastName
                );

        etDateOfBirth =
                findViewById(
                        R.id.etDateOfBirth
                );

        etGender =
                findViewById(
                        R.id.etGender
                );

        etBloodGroup =
                findViewById(
                        R.id.etBloodGroup
                );

        etAadhaar =
                findViewById(
                        R.id.etAadhaar
                );

        etBirthPlace =
                findViewById(
                        R.id.etBirthPlace
                );

        etNationality =
                findViewById(
                        R.id.etNationality
                );

        etMotherTongue =
                findViewById(
                        R.id.etMotherTongue
                );

        etReligion =
                findViewById(
                        R.id.etReligion
                );

        etCategory =
                findViewById(
                        R.id.etCategory
                );

        etCaste =
                findViewById(
                        R.id.etCaste
                );

        etSubCaste =
                findViewById(
                        R.id.etSubCaste
                );

        etSpecialNeeds =
                findViewById(
                        R.id.etSpecialNeeds
                );


        // =====================================================
        // PROFESSIONAL
        // =====================================================

        etEmployeeId =
                findViewById(
                        R.id.etEmployeeId
                );

        etJoiningDate =
                findViewById(
                        R.id.etJoiningDate
                );

        etQualification =
                findViewById(
                        R.id.etQualification
                );

        etExperience =
                findViewById(
                        R.id.etExperience
                );

        etEmploymentType =
                findViewById(
                        R.id.etEmploymentType
                );


        // =====================================================
        // ROLE
        // =====================================================

        etStaffType =
                findViewById(
                        R.id.etStaffType
                );

        etRole =
                findViewById(
                        R.id.etRole
                );

        etSubject =
                findViewById(
                        R.id.etSubject
                );

        etAssignedClass =
                findViewById(
                        R.id.etAssignedClass
                );

        etAssignedDivision =
                findViewById(
                        R.id.etAssignedDivision
                );


        // =====================================================
        // ADDRESS
        // =====================================================

        etCurrentAddress =
                findViewById(
                        R.id.etCurrentAddress
                );

        etPermanentAddress =
                findViewById(
                        R.id.etPermanentAddress
                );

        etCity =
                findViewById(
                        R.id.etCity
                );

        etDistrict =
                findViewById(
                        R.id.etDistrict
                );

        etState =
                findViewById(
                        R.id.etState
                );

        etPinCode =
                findViewById(
                        R.id.etPinCode
                );


        // =====================================================
        // EMERGENCY
        // =====================================================

        etEmergencyName =
                findViewById(
                        R.id.etEmergencyName
                );

        etEmergencyRelation =
                findViewById(
                        R.id.etEmergencyRelation
                );

        etEmergencyMobile =
                findViewById(
                        R.id.etEmergencyMobile
                );


        // =====================================================
        // LOGIN
        // =====================================================

        etStaffEmail =
                findViewById(
                        R.id.etStaffEmail
                );

        etStaffPhone =
                findViewById(
                        R.id.etStaffPhone
                );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );


        btnSave.setOnClickListener(
                v -> saveChanges()
        );


        // =====================================================
        // DATE PICKER
        // =====================================================

        etDateOfBirth.setOnClickListener(
                v -> showDatePicker(
                        etDateOfBirth
                )
        );


        etJoiningDate.setOnClickListener(
                v -> showDatePicker(
                        etJoiningDate
                )
        );
    }


    // =========================================================
    // LOAD STAFF
    // =========================================================

    private void loadStaffData() {

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                Toast.makeText(
                                        this,
                                        "Staff record not found.",
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();

                                return;
                            }


                            StaffModel staff =
                                    document.toObject(
                                            StaffModel.class
                                    );


                            if (staff == null) {

                                Toast.makeText(
                                        this,
                                        "Unable to read staff data.",
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();

                                return;
                            }


                            fillStaffData(
                                    staff
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to load staff data.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // FILL DATA
    // =========================================================

    private void fillStaffData(
            StaffModel staff
    ) {

        // =====================================================
        // BASIC
        // =====================================================

        etFirstName.setText(
                safe(staff.getFirstName())
        );

        etMiddleName.setText(
                safe(staff.getMiddleName())
        );

        etLastName.setText(
                safe(staff.getLastName())
        );

        etDateOfBirth.setText(
                safe(staff.getDateOfBirth())
        );

        etGender.setText(
                safe(staff.getGender())
        );

        etBloodGroup.setText(
                safe(staff.getBloodGroup())
        );

        etAadhaar.setText(
                safe(staff.getAadhaar())
        );

        etBirthPlace.setText(
                safe(staff.getBirthPlace())
        );

        etNationality.setText(
                safe(staff.getNationality())
        );

        etMotherTongue.setText(
                safe(staff.getMotherTongue())
        );

        etReligion.setText(
                safe(staff.getReligion())
        );

        etCategory.setText(
                safe(staff.getCategory())
        );

        etCaste.setText(
                safe(staff.getCaste())
        );

        etSubCaste.setText(
                safe(staff.getSubCaste())
        );

        etSpecialNeeds.setText(
                safe(staff.getSpecialNeeds())
        );


        // =====================================================
        // PROFESSIONAL
        // =====================================================

        etEmployeeId.setText(
                safe(staff.getEmployeeId())
        );

        etJoiningDate.setText(
                safe(staff.getJoiningDate())
        );

        etQualification.setText(
                safe(staff.getQualification())
        );

        etExperience.setText(
                safe(staff.getExperience())
        );

        etEmploymentType.setText(
                safe(staff.getEmploymentType())
        );


        // =====================================================
        // ROLE
        // =====================================================

        etStaffType.setText(
                safe(staff.getStaffType())
        );

        etRole.setText(
                safe(staff.getRole())
        );

        etSubject.setText(
                safe(staff.getSubject())
        );

        etAssignedClass.setText(
                safe(staff.getAssignedClass())
        );

        etAssignedDivision.setText(
                safe(staff.getAssignedDivision())
        );


        // =====================================================
        // ADDRESS
        // =====================================================

        etCurrentAddress.setText(
                safe(staff.getCurrentAddress())
        );

        etPermanentAddress.setText(
                safe(staff.getPermanentAddress())
        );

        etCity.setText(
                safe(staff.getCity())
        );

        etDistrict.setText(
                safe(staff.getDistrict())
        );

        etState.setText(
                safe(staff.getState())
        );

        etPinCode.setText(
                safe(staff.getPinCode())
        );


        // =====================================================
        // EMERGENCY
        // =====================================================

        etEmergencyName.setText(
                safe(staff.getEmergencyName())
        );

        etEmergencyRelation.setText(
                safe(staff.getEmergencyRelation())
        );

        etEmergencyMobile.setText(
                safe(staff.getEmergencyMobile())
        );


        // =====================================================
        // LOGIN
        // =====================================================

        etStaffEmail.setText(
                safe(staff.getStaffEmail())
        );

        etStaffPhone.setText(
                safe(staff.getStaffPhone())
        );
    }


    // =========================================================
    // SAVE CHANGES
    // =========================================================

    private void saveChanges() {

        if (btnSave != null) {

            btnSave.setEnabled(false);

            btnSave.setText(
                    "SAVING..."
            );
        }


        // =====================================================
        // VALIDATION
        // =====================================================

        String firstName =
                getText(etFirstName);

        String lastName =
                getText(etLastName);

        String qualification =
                getText(etQualification);

        String mobile =
                getText(etStaffPhone);

        String email =
                getText(etStaffEmail);


        if (firstName.isEmpty()) {

            showValidationError(
                    etFirstName,
                    "First name is required"
            );

            return;
        }


        if (lastName.isEmpty()) {

            showValidationError(
                    etLastName,
                    "Last name is required"
            );

            return;
        }


        if (qualification.isEmpty()) {

            showValidationError(
                    etQualification,
                    "Qualification is required"
            );

            return;
        }


        if (
                !mobile.isEmpty()
                        &&
                        !mobile.matches(
                                "^[6-9][0-9]{9}$"
                        )
        ) {

            showValidationError(
                    etStaffPhone,
                    "Enter valid 10 digit mobile number"
            );

            return;
        }


        if (
                !email.isEmpty()
                        &&
                        !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(email)
                                .matches()
        ) {

            showValidationError(
                    etStaffEmail,
                    "Enter valid email address"
            );

            return;
        }


        // =====================================================
        // UPDATE MAP
        // =====================================================

        Map<String, Object> update =
                new HashMap<>();


        // =====================================================
        // BASIC INFORMATION
        // =====================================================

        update.put(
                "firstName",
                firstName
        );

        update.put(
                "middleName",
                getText(etMiddleName)
        );

        update.put(
                "lastName",
                lastName
        );

        update.put(
                "dateOfBirth",
                getText(etDateOfBirth)
        );

        update.put(
                "gender",
                getText(etGender)
        );

        update.put(
                "bloodGroup",
                getText(etBloodGroup)
        );

        update.put(
                "aadhaar",
                getText(etAadhaar)
        );

        update.put(
                "birthPlace",
                getText(etBirthPlace)
        );

        update.put(
                "nationality",
                getText(etNationality)
        );

        update.put(
                "motherTongue",
                getText(etMotherTongue)
        );

        update.put(
                "religion",
                getText(etReligion)
        );

        update.put(
                "category",
                getText(etCategory)
        );

        update.put(
                "caste",
                getText(etCaste)
        );

        update.put(
                "subCaste",
                getText(etSubCaste)
        );

        update.put(
                "specialNeeds",
                getText(etSpecialNeeds)
        );


        // =====================================================
        // PROFESSIONAL INFORMATION
        // =====================================================

        update.put(
                "employeeId",
                getText(etEmployeeId)
        );

        update.put(
                "joiningDate",
                getText(etJoiningDate)
        );

        update.put(
                "qualification",
                qualification
        );

        update.put(
                "experience",
                getText(etExperience)
        );

        update.put(
                "employmentType",
                getText(etEmploymentType)
        );


        // =====================================================
        // STAFF TYPE & ROLE
        // =====================================================

        update.put(
                "staffType",
                getText(etStaffType)
        );

        update.put(
                "role",
                getText(etRole)
        );

        update.put(
                "subject",
                getText(etSubject)
        );

        update.put(
                "assignedClass",
                getText(etAssignedClass)
        );

        update.put(
                "assignedDivision",
                getText(etAssignedDivision)
        );


        // =====================================================
        // ADDRESS
        // =====================================================

        update.put(
                "currentAddress",
                getText(etCurrentAddress)
        );

        update.put(
                "permanentAddress",
                getText(etPermanentAddress)
        );

        update.put(
                "city",
                getText(etCity)
        );

        update.put(
                "district",
                getText(etDistrict)
        );

        update.put(
                "state",
                getText(etState)
        );

        update.put(
                "pinCode",
                getText(etPinCode)
        );


        // =====================================================
        // EMERGENCY
        // =====================================================

        update.put(
                "emergencyName",
                getText(etEmergencyName)
        );

        update.put(
                "emergencyRelation",
                getText(etEmergencyRelation)
        );

        update.put(
                "emergencyMobile",
                getText(etEmergencyMobile)
        );


        // =====================================================
        // LOGIN INFORMATION
        // =====================================================

        update.put(
                "staffEmail",
                email
        );

        update.put(
                "staffPhone",
                mobile
        );


        // =====================================================
        // FIRESTORE UPDATE
        // =====================================================

        db.collection("staff")
                .document(staffId)
                .update(update)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Staff information updated successfully.",
                                    Toast.LENGTH_LONG
                            ).show();


                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (btnSave != null) {

                                btnSave.setEnabled(true);

                                btnSave.setText(
                                        "SAVE CHANGES"
                                );
                            }


                            String message =
                                    "Unable to update staff information.";


                            if (
                                    e.getMessage() != null
                            ) {

                                message =
                                        e.getMessage();
                            }


                            Toast.makeText(
                                    this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // VALIDATION ERROR
    // =========================================================

    private void showValidationError(
            EditText editText,
            String message
    ) {

        if (btnSave != null) {

            btnSave.setEnabled(true);

            btnSave.setText(
                    "SAVE CHANGES"
            );
        }


        editText.setError(
                message
        );

        editText.requestFocus();
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void showDatePicker(
            EditText target
    ) {

        Calendar calendar =
                Calendar.getInstance();


        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            String formattedDate =

                                    String.format(
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );


                            target.setText(
                                    formattedDate
                            );
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );


        dialog.show();
    }


    // =========================================================
    // GET TEXT
    // =========================================================

    private String getText(
            EditText editText
    ) {

        if (editText == null) {
            return "";
        }


        return editText.getText()
                .toString()
                .trim();
    }


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}