package com.example.myschool;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class AddStudentStep5 extends AppCompatActivity {

    private EditText etTotalFees;
    private EditText etPaidFees;
    private EditText etInstallmentAmount;
    private EditText etPaymentDate;

    private TextView tvRemainingFees;

    private Spinner spPaymentMode;
    private Spinner spInstallment;

    private Button btnNext;

    private FirebaseFirestore db;

    private boolean editMode = false;
    private String studentId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_student_step5
        );

        db =
                FirebaseFirestore.getInstance();

        initViews();

        setupSpinners();

        setupFeeCalculation();

        setupPaymentDate();

        setupListeners();

        setupSystemBack();

        checkEditMode();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initViews() {

        etTotalFees =
                findViewById(
                        R.id.etTotalFees
                );

        etPaidFees =
                findViewById(
                        R.id.etPaidFees
                );

        etInstallmentAmount =
                findViewById(
                        R.id.etInstallmentAmount
                );

        etPaymentDate =
                findViewById(
                        R.id.etPaymentDate
                );

        tvRemainingFees =
                findViewById(
                        R.id.tvRemainingFees
                );

        spPaymentMode =
                findViewById(
                        R.id.spPaymentMode
                );

        spInstallment =
                findViewById(
                        R.id.spInstallment
                );

        btnNext =
                findViewById(
                        R.id.btnNext
                );
    }


    // =========================================================
    // SPINNERS
    // =========================================================

    private void setupSpinners() {

        String[] paymentModes = {

                "Select Payment Mode",
                "Cash",
                "UPI",
                "Credit Card",
                "Debit Card",
                "Bank Transfer",
                "Cheque",
                "Online Payment"
        };


        ArrayAdapter<String> paymentAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        paymentModes
                );


        paymentAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spPaymentMode.setAdapter(
                paymentAdapter
        );


        String[] installments = {

                "Select Installment",
                "Full Payment",
                "Monthly",
                "Quarterly",
                "Half-Yearly",
                "Yearly"
        };


        ArrayAdapter<String> installmentAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        installments
                );


        installmentAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spInstallment.setAdapter(
                installmentAdapter
        );
    }


    // =========================================================
    // FEE CALCULATION
    // =========================================================

    private void setupFeeCalculation() {

        TextWatcher watcher =
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        calculateRemaining();
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                };


        etTotalFees.addTextChangedListener(
                watcher
        );


        etPaidFees.addTextChangedListener(
                watcher
        );
    }


    private void calculateRemaining() {

        try {

            String totalText =
                    etTotalFees
                            .getText()
                            .toString()
                            .trim();


            String paidText =
                    etPaidFees
                            .getText()
                            .toString()
                            .trim();


            if (totalText.isEmpty() ||
                    paidText.isEmpty()) {

                tvRemainingFees.setText(
                        "₹ 0.00"
                );

                return;
            }


            double total =
                    Double.parseDouble(
                            totalText
                    );


            double paid =
                    Double.parseDouble(
                            paidText
                    );


            double remaining =
                    total - paid;


            if (remaining < 0) {

                remaining = 0;
            }


            tvRemainingFees.setText(
                    String.format(
                            Locale.getDefault(),
                            "₹ %.2f",
                            remaining
                    )
            );


        } catch (Exception e) {

            tvRemainingFees.setText(
                    "₹ 0.00"
            );
        }
    }


    // =========================================================
    // PAYMENT DATE
    // =========================================================

    private void setupPaymentDate() {

        etPaymentDate.setOnClickListener(
                v -> {

                    Calendar calendar =
                            Calendar.getInstance();


                    DatePickerDialog dialog =
                            new DatePickerDialog(
                                    this,
                                    (
                                            view,
                                            year,
                                            month,
                                            day
                                    ) -> {

                                        etPaymentDate.setText(
                                                String.format(
                                                        Locale.getDefault(),
                                                        "%02d/%02d/%04d",
                                                        day,
                                                        month + 1,
                                                        year
                                                )
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


                    dialog.getDatePicker()
                            .setMaxDate(
                                    System.currentTimeMillis()
                            );


                    dialog.show();
                }
        );
    }


    // =========================================================
    // EDIT MODE
    // =========================================================

    private void checkEditMode() {

        Intent intent =
                getIntent();


        if (intent != null) {

            editMode =
                    intent.getBooleanExtra(
                            "editMode",
                            false
                    );


            String id =
                    intent.getStringExtra(
                            "studentId"
                    );


            if (id != null) {

                studentId =
                        id.trim();
            }
        }


        if (editMode &&
                !studentId.isEmpty()) {

            loadFeeData();
        }
    }


    // =========================================================
    // LOAD EXISTING FEES
    // =========================================================

    private void loadFeeData() {

        db.collection(
                        "students"
                )
                .document(
                        studentId
                )
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                Toast.makeText(
                                        this,
                                        "Student record not found",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }


                            // TOTAL
                            etTotalFees.setText(
                                    getString(
                                            document,
                                            "totalFees"
                                    )
                            );


                            // PAID
                            etPaidFees.setText(
                                    getString(
                                            document,
                                            "paidFees"
                                    )
                            );


                            // INSTALLMENT
                            etInstallmentAmount.setText(
                                    getString(
                                            document,
                                            "installmentAmount"
                                    )
                            );


                            // PAYMENT DATE
                            etPaymentDate.setText(
                                    getString(
                                            document,
                                            "paymentDate"
                                    )
                            );


                            // PAYMENT MODE
                            setSpinnerValue(
                                    spPaymentMode,
                                    getString(
                                            document,
                                            "paymentMode"
                                    )
                            );


                            // INSTALLMENT TYPE
                            setSpinnerValue(
                                    spInstallment,
                                    getString(
                                            document,
                                            "installmentType"
                                    )
                            );


                            calculateRemaining();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Could not load fees",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // SET SPINNER
    // =========================================================

    private void setSpinnerValue(
            Spinner spinner,
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return;
        }


        ArrayAdapter adapter =
                (ArrayAdapter)
                        spinner.getAdapter();


        if (adapter == null) {

            return;
        }


        int position =
                adapter.getPosition(
                        value
                );


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

        ImageButton btnBack =
                findViewById(
                        R.id.btnBack
                );


        btnBack.setOnClickListener(
                v -> finish()
        );


        btnNext.setOnClickListener(
                v -> validateAndNext()
        );
    }


    // =========================================================
    // VALIDATE + NEXT
    // =========================================================

    private void validateAndNext() {

        String totalFees =
                etTotalFees
                        .getText()
                        .toString()
                        .trim();


        String paidFees =
                etPaidFees
                        .getText()
                        .toString()
                        .trim();


        String installmentAmount =
                etInstallmentAmount
                        .getText()
                        .toString()
                        .trim();


        String paymentDate =
                etPaymentDate
                        .getText()
                        .toString()
                        .trim();


        // =====================================================
        // TOTAL
        // =====================================================

        if (totalFees.isEmpty()) {

            etTotalFees.setError(
                    "Enter total fees"
            );

            etTotalFees.requestFocus();

            return;
        }


        // =====================================================
        // PAID
        // =====================================================

        if (paidFees.isEmpty()) {

            etPaidFees.setError(
                    "Enter paid fees"
            );

            etPaidFees.requestFocus();

            return;
        }


        double total;
        double paid;


        try {

            total =
                    Double.parseDouble(
                            totalFees
                    );


            paid =
                    Double.parseDouble(
                            paidFees
                    );


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Enter valid fee amount",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // PAID CANNOT EXCEED TOTAL
        // =====================================================

        if (paid < 0 ||
                paid > total) {

            etPaidFees.setError(
                    "Paid fees cannot exceed total fees"
            );

            etPaidFees.requestFocus();

            return;
        }


        // =====================================================
        // PAYMENT MODE
        // =====================================================

        if (spPaymentMode
                .getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select payment mode",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // INSTALLMENT
        // =====================================================

        if (spInstallment
                .getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select installment type",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String paymentMode =
                spPaymentMode
                        .getSelectedItem()
                        .toString();


        String installmentType =
                spInstallment
                        .getSelectedItem()
                        .toString();


        if (installmentAmount.isEmpty()) {

            installmentAmount =
                    "0";
        }


        if (paymentDate.isEmpty()) {

            paymentDate =
                    "Not provided";
        }


        double remaining =
                total -
                        paid;


        if (remaining < 0) {

            remaining = 0;
        }


        // =====================================================
        // OPEN STEP 6
        // =====================================================

        Intent intent =
                new Intent(
                        AddStudentStep5.this,
                        AddStudentStep6.class
                );


        // =====================================================
        // KEEP EDIT MODE
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
        // COPY ALL PREVIOUS DATA
        // =====================================================

        copyData(
                getIntent(),
                intent
        );


        // =====================================================
        // FEES DATA
        // =====================================================

        intent.putExtra(
                "totalFees",
                totalFees
        );


        intent.putExtra(
                "paidFees",
                paidFees
        );


        intent.putExtra(
                "remainingFees",
                String.valueOf(
                        remaining
                )
        );


        intent.putExtra(
                "paymentMode",
                paymentMode
        );


        intent.putExtra(
                "installmentType",
                installmentType
        );


        intent.putExtra(
                "installmentAmount",
                installmentAmount
        );


        intent.putExtra(
                "paymentDate",
                paymentDate
        );


        startActivity(
                intent
        );
    }


    // =========================================================
    // COPY PREVIOUS DATA
    // =========================================================

    private void copyData(
            Intent from,
            Intent to) {


        if (from == null) {

            return;
        }


        String[] keys = {

                // STEP 1
                "studentPhoto",
                "firstName",
                "middleName",
                "lastName",
                "dateOfBirth",
                "gender",
                "bloodGroup",
                "aadhaar",
                "birthPlace",
                "nationality",
                "motherTongue",
                "religion",
                "category",
                "caste",
                "subCaste",
                "specialNeeds",

                // STEP 2
                "admissionNumber",
                "admissionDate",
                "academicYear",
                "className",
                "division",
                "rollNumber",
                "previousSchool",
                "previousClass",
                "leavingCertificate",

                // STEP 3
                "fatherName",
                "fatherMobile",
                "fatherEmail",
                "fatherOccupation",
                "motherName",
                "motherMobile",
                "motherEmail",
                "motherOccupation",
                "guardianName",
                "guardianRelation",
                "guardianMobile",

                // STEP 4
                "currentAddress",
                "permanentAddress",
                "city",
                "district",
                "state",
                "pinCode",
                "emergencyName",
                "emergencyRelation",
                "emergencyMobile"
        };


        for (String key :
                keys) {


            String value =
                    from.getStringExtra(
                            key
                    );


            if (value != null) {

                to.putExtra(
                        key,
                        value
                );
            }
        }
    }


    // =========================================================
    // GET FIRESTORE STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key) {


        Object value =
                document.get(
                        key
                );


        if (value == null) {

            return "";
        }


        return String.valueOf(
                value
        ).trim();
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