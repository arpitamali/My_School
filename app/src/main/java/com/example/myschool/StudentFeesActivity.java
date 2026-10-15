package com.example.myschool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class StudentFeesActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private Spinner spClassFilter;
    private Spinner spFeeFilter;
    private EditText etSearchStudent;

    private LinearLayout studentContainer;

    private TextView tvStudentCount;
    private TextView tvNoStudents;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    // =========================================================
    // STUDENTS
    // =========================================================

    private final List<DocumentSnapshot> allStudents =
            new ArrayList<>();


    // =========================================================
    // SCHOOL
    // =========================================================

    private String schoolName =
            "MY SCHOOL";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(
                savedInstanceState
        );


        setContentView(
                R.layout.activity_student_fees
        );


        db =
                FirebaseFirestore.getInstance();


        auth =
                FirebaseAuth.getInstance();


        initViews();

        setupBackButton();

        setupClassFilter();

        setupFeeFilter();
        setupSearch();

        loadSchoolData();

        loadStudents();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initViews() {

        spClassFilter =
                findViewById(
                        R.id.spClassFilter
                );


        spFeeFilter =
                findViewById(
                        R.id.spFeeFilter
                );

        etSearchStudent =
                findViewById(
                        R.id.etSearchStudent
                );


        studentContainer =
                findViewById(
                        R.id.studentContainer
                );


        tvStudentCount =
                findViewById(
                        R.id.tvStudentCount
                );


        tvNoStudents =
                findViewById(
                        R.id.tvNoStudents
                );
    }


    // =========================================================
    // BACK
    // =========================================================

    private void setupBackButton() {

        ImageButton btnBack =
                findViewById(
                        R.id.btnBack
                );


        btnBack.setOnClickListener(
                v -> finish()
        );
    }


    // =========================================================
    // SCHOOL DATA
    // =========================================================

    private void loadSchoolData() {

        if (auth.getCurrentUser() == null) {

            schoolName =
                    "MY SCHOOL";

            return;
        }


        String uid =
                auth.getCurrentUser()
                        .getUid();


        db.collection("schools")
                .document(uid)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                schoolName =
                                        "MY SCHOOL";

                                return;
                            }


                            String fetchedName =
                                    document.getString(
                                            "schoolName"
                                    );


                            if (fetchedName != null &&
                                    !fetchedName
                                            .trim()
                                            .isEmpty()) {

                                schoolName =
                                        fetchedName.trim();

                            } else {

                                schoolName =
                                        "MY SCHOOL";
                            }
                        }
                )
                .addOnFailureListener(
                        e -> schoolName =
                                "MY SCHOOL"
                );
    }


    // =========================================================
    // CLASS FILTER
    // =========================================================

    private void setupClassFilter() {

        String[] classes = {

                "All Classes",

                "Nursery",
                "LKG",
                "UKG",

                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_item,
                        classes
                );


        adapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
        );


        spClassFilter.setAdapter(
                adapter
        );


        spClassFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        applyFilters();
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }


    // =========================================================
    // FEE FILTER
    // =========================================================

    private void setupFeeFilter() {

        String[] filters = {

                "All Fees",
                "Fully Paid",
                "Remaining"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_item,
                        filters
                );


        adapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
        );


        spFeeFilter.setAdapter(
                adapter
        );


        spFeeFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        applyFilters();
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }


    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        etSearchStudent.addTextChangedListener(
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

                        applyFilters();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );
    }


    // =========================================================
    // LOAD STUDENTS
    // =========================================================

    private void loadStudents() {

        if (auth.getCurrentUser() == null) {

            showEmpty();

            return;
        }


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            allStudents.clear();


                            allStudents.addAll(
                                    snapshot.getDocuments()
                            );


                            applyFilters();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            showEmpty();


                            Toast.makeText(
                                    this,
                                    "Failed to load students.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // FILTER
    // =========================================================

    private void applyFilters() {

        if (studentContainer == null) {
            return;
        }


        studentContainer.removeAllViews();


        String selectedClass =
                spClassFilter
                        .getSelectedItem()
                        .toString();


        String selectedFee =
                spFeeFilter
                        .getSelectedItem()
                        .toString();

        String searchText = "";

        if (etSearchStudent != null) {
            searchText = etSearchStudent
                    .getText()
                    .toString()
                    .trim()
                    .toLowerCase(Locale.getDefault());
        }


        int count = 0;


        for (DocumentSnapshot student :
                allStudents) {


            String className =
                    getString(
                            student,
                            "className"
                    );


            if (!selectedClass.equals(
                    "All Classes"
            )) {

                if (!className.equals(
                        selectedClass
                )) {

                    continue;
                }
            }

            String firstName =
                    getString(student, "firstName");

            String middleName =
                    getString(student, "middleName");

            String lastName =
                    getString(student, "lastName");

            String studentNameForSearch =
                    (firstName + " " + middleName + " " + lastName).trim();

            if (studentNameForSearch.isEmpty()) {
                studentNameForSearch =
                        getString(student, "studentName");
            }

            String rollNumberForSearch =
                    getString(student, "rollNumber");

            String divisionForSearch =
                    getString(student, "division");

            String searchableText =
                    (studentNameForSearch + " " +
                            rollNumberForSearch + " " +
                            className + " " +
                            divisionForSearch)
                            .toLowerCase(Locale.getDefault());

            if (!searchText.isEmpty() &&
                    !searchableText.contains(searchText)) {

                continue;
            }


            double totalFees =
                    getDouble(
                            student,
                            "totalFees"
                    );


            double paidFees =
                    getDouble(
                            student,
                            "paidFees"
                    );


            double remainingFees =
                    totalFees -
                            paidFees;


            if (remainingFees < 0) {

                remainingFees = 0;
            }


            if (selectedFee.equals(
                    "Fully Paid"
            )) {

                if (remainingFees > 0.01) {
                    continue;
                }
            }


            if (selectedFee.equals(
                    "Remaining"
            )) {

                if (remainingFees <= 0.01) {
                    continue;
                }
            }


            addStudentCard(
                    student,
                    totalFees,
                    paidFees,
                    remainingFees
            );


            count++;
        }


        if (count == 1) {

            tvStudentCount.setText(
                    "1 Student"
            );

        } else {

            tvStudentCount.setText(
                    count +
                            " Students"
            );
        }


        tvNoStudents.setVisibility(
                count == 0
                        ? View.VISIBLE
                        : View.GONE
        );
    }


    // =========================================================
    // STUDENT CARD
    // =========================================================

    private void addStudentCard(
            DocumentSnapshot student,
            double totalFees,
            double paidFees,
            double remainingFees) {


        LinearLayout card =
                new LinearLayout(this);


        card.setOrientation(
                LinearLayout.VERTICAL
        );


        card.setPadding(
                16,
                15,
                16,
                15
        );


        card.setBackgroundResource(
                R.drawable.fee_student_card
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        cardParams.setMargins(
                0,
                0,
                0,
                14
        );


        card.setLayoutParams(
                cardParams
        );


        // =====================================================
        // NAME
        // =====================================================

        String firstName =
                getString(
                        student,
                        "firstName"
                );


        String middleName =
                getString(
                        student,
                        "middleName"
                );


        String lastName =
                getString(
                        student,
                        "lastName"
                );


        String studentName =
                (
                        firstName +
                                " " +
                                middleName +
                                " " +
                                lastName
                ).trim();


        if (studentName.isEmpty()) {

            studentName =
                    getString(
                            student,
                            "studentName"
                    );
        }


        if (studentName.isEmpty()) {

            studentName =
                    "Student";
        }


        TextView tvName =
                new TextView(this);


        tvName.setText(
                studentName
        );


        tvName.setTextSize(
                18
        );


        tvName.setTextColor(
                Color.rgb(
                        30,
                        25,
                        55
                )
        );


        tvName.setTypeface(
                null,
                Typeface.BOLD
        );


        card.addView(
                tvName
        );


        // =====================================================
        // CLASS
        // =====================================================

        String className =
                getString(
                        student,
                        "className"
                );


        String division =
                getString(
                        student,
                        "division"
                );


        String displayClass =
                "Class: " +
                        className;


        if (!division.isEmpty()) {

            displayClass +=
                    " - " +
                            division;
        }


        card.addView(
                createInfoText(
                        displayClass
                )
        );


        // =====================================================
        // FEE SUMMARY
        // =====================================================

        LinearLayout feeRow =
                new LinearLayout(this);


        feeRow.setOrientation(
                LinearLayout.HORIZONTAL
        );


        feeRow.setGravity(
                Gravity.CENTER
        );


        feeRow.setPadding(
                0,
                7,
                0,
                7
        );


        feeRow.addView(
                createFeeBox(
                        "TOTAL",
                        "₹ " +
                                formatAmount(
                                        totalFees
                                ),
                        Color.rgb(
                                75,
                                31,
                                168
                        )
                )
        );


        feeRow.addView(
                createFeeBox(
                        "PAID",
                        "₹ " +
                                formatAmount(
                                        paidFees
                                ),
                        Color.rgb(
                                35,
                                125,
                                65
                        )
                )
        );


        feeRow.addView(
                createFeeBox(
                        "REMAINING",
                        "₹ " +
                                formatAmount(
                                        remainingFees
                                ),
                        Color.rgb(
                                200,
                                40,
                                40
                        )
                )
        );


        card.addView(
                feeRow
        );


        // =====================================================
        // BUTTON ROW
        // =====================================================

        LinearLayout buttonRow =
                new LinearLayout(this);


        buttonRow.setOrientation(
                LinearLayout.HORIZONTAL
        );


        buttonRow.setGravity(
                Gravity.CENTER
        );


        // FINAL VARIABLES
        final String finalStudentId =
                student.getId();


        final String finalStudentName =
                studentName;


        final double finalTotalFees =
                totalFees;


        final double finalPaidFees =
                paidFees;


        final double finalRemainingFees =
                remainingFees;


        // =====================================================
        // UPDATE
        // =====================================================

        Button btnUpdate =
                createActionButton(
                        "✎  UPDATE FEES",
                        true
                );


        LinearLayout.LayoutParams updateParams =
                new LinearLayout.LayoutParams(
                        0,
                        76,
                        1
                );


        updateParams.setMargins(
                0,
                8,
                6,
                0
        );


        btnUpdate.setLayoutParams(
                updateParams
        );


        btnUpdate.setOnClickListener(
                v -> openUpdateFeesDialog(
                        finalStudentId,
                        finalStudentName,
                        finalTotalFees,
                        finalPaidFees,
                        finalRemainingFees
                )
        );


        buttonRow.addView(
                btnUpdate
        );


        // =====================================================
        // RECEIPT
        // =====================================================

        Button btnReceipt =
                createActionButton(
                        "▣  GENERATE RECEIPT",
                        false
                );


        LinearLayout.LayoutParams receiptParams =
                new LinearLayout.LayoutParams(
                        0,
                        76,
                        1
                );


        receiptParams.setMargins(
                6,
                8,
                0,
                0
        );


        btnReceipt.setLayoutParams(
                receiptParams
        );


        btnReceipt.setOnClickListener(
                v -> showFeeReceipt(
                        student,
                        finalStudentName,
                        finalTotalFees,
                        finalPaidFees,
                        finalRemainingFees
                )
        );


        buttonRow.addView(
                btnReceipt
        );


        card.addView(
                buttonRow
        );


        studentContainer.addView(
                card
        );
    }


    // =========================================================
    // FEE BOX
    // =========================================================

    private TextView createFeeBox(
            String title,
            String amount,
            int color) {


        TextView tv =
                new TextView(this);


        tv.setText(
                title +
                        "\n" +
                        amount
        );


        tv.setTextSize(
                12
        );


        tv.setTypeface(
                null,
                Typeface.BOLD
        );


        tv.setTextColor(
                color
        );


        tv.setGravity(
                Gravity.CENTER
        );


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        74,
                        1
                );


        tv.setLayoutParams(
                params
        );


        return tv;
    }


    // =========================================================
    // ACTION BUTTON
    // =========================================================

    private Button createActionButton(
            String text,
            boolean purple) {


        Button button =
                new Button(this);


        button.setText(
                text
        );


        button.setTextSize(
                14
        );


        button.setAllCaps(
                false
        );


        button.setMinHeight(
                0
        );


        button.setMinWidth(
                0
        );


        button.setGravity(
                Gravity.CENTER
        );


        button.setTypeface(
                null,
                Typeface.BOLD
        );


        button.setPadding(
                28,
                16,
                28,
                16
        );


        if (purple) {

            button.setTextColor(
                    Color.WHITE
            );


            button.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    75,
                                    31,
                                    168
                            )
                    )
            );

        } else {

            button.setTextColor(
                    Color.rgb(
                            75,
                            31,
                            168
                    )
            );


            button.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    242,
                                    235,
                                    253
                            )
                    )
            );
        }


        return button;
    }


    // =========================================================
    // UPDATE FEES
    // =========================================================

    private void openUpdateFeesDialog(
            String studentId,
            String studentName,
            double totalFees,
            double oldPaidFees,
            double oldRemainingFees) {


        LinearLayout layout =
                new LinearLayout(this);


        layout.setOrientation(
                LinearLayout.VERTICAL
        );


        layout.setPadding(
                24,
                12,
                24,
                8
        );


        TextView title =
                createInfoText(
                        "UPDATE FEES"
                );


        title.setTextSize(
                21
        );


        title.setTextColor(
                Color.rgb(
                        75,
                        31,
                        168
                )
        );


        title.setTypeface(
                null,
                Typeface.BOLD
        );


        layout.addView(
                title
        );


        TextView student =
                createInfoText(
                        studentName
                );


        student.setTextSize(
                16
        );


        student.setTypeface(
                null,
                Typeface.BOLD
        );


        layout.addView(
                student
        );


        layout.addView(
                createInfoText(
                        "Total Fees: ₹ " +
                                formatAmount(
                                        totalFees
                                )
                )
        );


        TextView alreadyPaid =
                createInfoText(
                        "Already Paid: ₹ " +
                                formatAmount(
                                        oldPaidFees
                                )
                );


        alreadyPaid.setTextColor(
                Color.rgb(
                        40,
                        125,
                        60
                )
        );


        layout.addView(
                alreadyPaid
        );


        TextView currentRemaining =
                createInfoText(
                        "Current Remaining: ₹ " +
                                formatAmount(
                                        oldRemainingFees
                                )
                );


        currentRemaining.setTextColor(
                Color.rgb(
                        198,
                        40,
                        40
                )
        );


        layout.addView(
                currentRemaining
        );


        TextView paymentLabel =
                createInfoText(
                        "New Payment"
                );


        paymentLabel.setTextSize(
                14
        );


        paymentLabel.setTextColor(
                Color.rgb(
                        75,
                        31,
                        168
                )
        );


        paymentLabel.setTypeface(
                null,
                Typeface.BOLD
        );


        paymentLabel.setPadding(
                0,
                10,
                0,
                2
        );


        layout.addView(
                paymentLabel
        );


        EditText etNewPayment =
                new EditText(this);


        etNewPayment.setHint(
                "Enter new payment amount"
        );


        etNewPayment.setSingleLine(
                true
        );


        etNewPayment.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );


        layout.addView(
                etNewPayment
        );


        TextView tvNewPaid =
                createInfoText(
                        "New Total Paid: ₹ " +
                                formatAmount(
                                        oldPaidFees
                                )
                );


        tvNewPaid.setTextColor(
                Color.rgb(
                        40,
                        125,
                        60
                )
        );


        tvNewPaid.setTypeface(
                null,
                Typeface.BOLD
        );


        layout.addView(
                tvNewPaid
        );


        TextView tvNewRemaining =
                createInfoText(
                        "New Remaining: ₹ " +
                                formatAmount(
                                        oldRemainingFees
                                )
                );


        tvNewRemaining.setTextColor(
                Color.rgb(
                        198,
                        40,
                        40
                )
        );


        tvNewRemaining.setTypeface(
                null,
                Typeface.BOLD
        );


        layout.addView(
                tvNewRemaining
        );


        etNewPayment.addTextChangedListener(
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

                        updateLiveFeeValues(
                                etNewPayment,
                                oldPaidFees,
                                totalFees,
                                tvNewPaid,
                                tvNewRemaining
                        );
                    }


                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(layout)
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "UPDATE",
                                null
                        )
                        .create();


        dialog.setOnShowListener(
                d -> {

                    Button updateButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );


                    updateButton.setOnClickListener(
                            v -> {

                                String paymentText =
                                        etNewPayment
                                                .getText()
                                                .toString()
                                                .trim();


                                if (paymentText.isEmpty()) {

                                    etNewPayment.setError(
                                            "Enter new payment"
                                    );

                                    return;
                                }


                                double newPayment;


                                try {

                                    newPayment =
                                            Double.parseDouble(
                                                    paymentText
                                            );

                                } catch (Exception e) {

                                    etNewPayment.setError(
                                            "Enter valid amount"
                                    );

                                    return;
                                }


                                if (newPayment <= 0) {

                                    etNewPayment.setError(
                                            "Payment must be greater than 0"
                                    );

                                    return;
                                }


                                double newPaid =
                                        oldPaidFees +
                                                newPayment;


                                if (newPaid > totalFees) {

                                    etNewPayment.setError(
                                            "Payment cannot exceed remaining fees"
                                    );

                                    return;
                                }


                                double newRemaining =
                                        totalFees -
                                                newPaid;


                                String paymentDate =
                                        new SimpleDateFormat(
                                                "dd/MM/yyyy",
                                                Locale.getDefault()
                                        ).format(
                                                new Date()
                                        );


                                db.collection("students")
                                        .document(studentId)
                                        .update(
                                                "paidFees",
                                                newPaid,

                                                "remainingFees",
                                                newRemaining,

                                                "lastPaymentAmount",
                                                newPayment,

                                                "lastPaymentDate",
                                                paymentDate
                                        )
                                        .addOnSuccessListener(
                                                unused -> {

                                                    dialog.dismiss();


                                                    Toast.makeText(
                                                            this,
                                                            "Payment added successfully",
                                                            Toast.LENGTH_SHORT
                                                    ).show();


                                                    loadStudents();
                                                }
                                        )
                                        .addOnFailureListener(
                                                e -> {

                                                    Toast.makeText(
                                                            this,
                                                            "Update failed: " +
                                                                    e.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    ).show();
                                                }
                                        );
                            }
                    );
                }
        );


        dialog.show();


        Window window =
                dialog.getWindow();


        if (window != null) {

            window.setLayout(
                    (int)
                            (
                                    getResources()
                                            .getDisplayMetrics()
                                            .widthPixels *
                                            0.92
                            ),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }


    // =========================================================
    // LIVE CALCULATION
    // =========================================================

    private void updateLiveFeeValues(
            EditText etNewPayment,
            double oldPaidFees,
            double totalFees,
            TextView tvNewPaid,
            TextView tvNewRemaining) {


        String text =
                etNewPayment
                        .getText()
                        .toString()
                        .trim();


        if (text.isEmpty()) {

            tvNewPaid.setText(
                    "New Total Paid: ₹ " +
                            formatAmount(
                                    oldPaidFees
                            )
            );


            tvNewRemaining.setText(
                    "New Remaining: ₹ " +
                            formatAmount(
                                    totalFees -
                                            oldPaidFees
                            )
            );


            return;
        }


        try {

            double newPayment =
                    Double.parseDouble(
                            text
                    );


            double newPaid =
                    oldPaidFees +
                            newPayment;


            double newRemaining =
                    Math.max(
                            0,
                            totalFees -
                                    newPaid
                    );


            tvNewPaid.setText(
                    "New Total Paid: ₹ " +
                            formatAmount(
                                    newPaid
                            )
            );


            tvNewRemaining.setText(
                    "New Remaining: ₹ " +
                            formatAmount(
                                    newRemaining
                            )
            );


        } catch (Exception ignored) {
        }
    }


    // =========================================================
    // RECEIPT
    // =========================================================

    private void showFeeReceipt(
            DocumentSnapshot student,
            String studentName,
            double totalFees,
            double paidFees,
            double remainingFees) {


        // =====================================================
        // FINAL VALUES
        // =====================================================

        final String finalSchoolName =
                schoolName == null ||
                        schoolName.trim().isEmpty()
                        ? "MY SCHOOL"
                        : schoolName.trim();


        final String receiptNo =
                "FR-" +
                        new SimpleDateFormat(
                                "yyyyMMddHHmmss",
                                Locale.getDefault()
                        ).format(
                                new Date()
                        );


        final String receiptDate =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


        final String className =
                getString(
                        student,
                        "className"
                );


        final String division =
                getString(
                        student,
                        "division"
                );


        String tempClass =
                className;


        if (!division.isEmpty()) {

            tempClass +=
                    " - " +
                            division;
        }


        final String finalClassText =
                tempClass;


        String tempPayment =
                getString(
                        student,
                        "paymentMode"
                );


        if (tempPayment.isEmpty()) {

            tempPayment =
                    "Cash";
        }


        final String finalPaymentMode =
                tempPayment;


        // =====================================================
        // OUTER SCROLL VIEW
        // =====================================================

        ScrollView scrollView =
                new ScrollView(this);


        scrollView.setFillViewport(
                false
        );


        scrollView.setBackgroundColor(
                Color.WHITE
        );


        // =====================================================
        // RECEIPT CONTAINER
        // =====================================================

        LinearLayout receipt =
                new LinearLayout(this);


        receipt.setOrientation(
                LinearLayout.VERTICAL
        );


        receipt.setPadding(
                22,
                18,
                22,
                18
        );


        scrollView.addView(
                receipt
        );


        // =====================================================
        // SCHOOL NAME
        // =====================================================

        TextView tvSchool =
                new TextView(this);


        tvSchool.setText(
                finalSchoolName
        );


        tvSchool.setTextSize(
                22
        );


        tvSchool.setTextColor(
                Color.rgb(
                        75,
                        31,
                        168
                )
        );


        tvSchool.setTypeface(
                null,
                Typeface.BOLD
        );


        tvSchool.setGravity(
                Gravity.CENTER
        );


        tvSchool.setSingleLine(
                false
        );


        receipt.addView(
                tvSchool
        );


        // =====================================================
        // TITLE
        // =====================================================

        TextView tvTitle =
                new TextView(this);


        tvTitle.setText(
                "FEE PAYMENT RECEIPT"
        );


        tvTitle.setTextSize(
                14
        );


        tvTitle.setTextColor(
                Color.rgb(
                        45,
                        45,
                        45
                )
        );


        tvTitle.setTypeface(
                null,
                Typeface.BOLD
        );


        tvTitle.setGravity(
                Gravity.CENTER
        );


        tvTitle.setPadding(
                0,
                3,
                0,
                8
        );


        receipt.addView(
                tvTitle
        );


        // =====================================================
        // LINE
        // =====================================================

        View line =
                new View(this);


        line.setBackgroundColor(
                Color.rgb(
                        91,
                        33,
                        182
                )
        );


        LinearLayout.LayoutParams lineParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                );


        lineParams.setMargins(
                0,
                0,
                0,
                10
        );


        line.setLayoutParams(
                lineParams
        );


        receipt.addView(
                line
        );


        // =====================================================
        // RECEIPT DETAILS
        // =====================================================

        receipt.addView(
                createReceiptRow(
                        "Receipt No",
                        receiptNo,
                        false
                )
        );


        receipt.addView(
                createReceiptRow(
                        "Date",
                        receiptDate,
                        false
                )
        );


        receipt.addView(
                createReceiptRow(
                        "Student",
                        studentName,
                        true
                )
        );


        receipt.addView(
                createReceiptRow(
                        "Class",
                        finalClassText,
                        false
                )
        );


        // =====================================================
        // DIVIDER
        // =====================================================

        View divider =
                new View(this);


        divider.setBackgroundColor(
                Color.rgb(
                        215,
                        210,
                        225
                )
        );


        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );


        dividerParams.setMargins(
                0,
                10,
                0,
                10
        );


        divider.setLayoutParams(
                dividerParams
        );


        receipt.addView(
                divider
        );


        // =====================================================
        // FEE BOX
        // =====================================================

        LinearLayout feeBox =
                new LinearLayout(this);


        feeBox.setOrientation(
                LinearLayout.VERTICAL
        );


        feeBox.setPadding(
                12,
                8,
                12,
                8
        );


        feeBox.setBackgroundColor(
                Color.rgb(
                        248,
                        246,
                        252
                )
        );


        feeBox.addView(
                createFeeReceiptRow(
                        "Total Fees",
                        "₹ " +
                                formatAmount(
                                        totalFees
                                ),
                        Color.rgb(
                                75,
                                31,
                                168
                        )
                )
        );


        feeBox.addView(
                createFeeReceiptRow(
                        "Total Paid",
                        "₹ " +
                                formatAmount(
                                        paidFees
                                ),
                        Color.rgb(
                                40,
                                125,
                                60
                        )
                )
        );


        feeBox.addView(
                createFeeReceiptRow(
                        "Remaining",
                        "₹ " +
                                formatAmount(
                                        remainingFees
                                ),
                        Color.rgb(
                                198,
                                40,
                                40
                        )
                )
        );


        feeBox.addView(
                createFeeReceiptRow(
                        "Payment Mode",
                        finalPaymentMode,
                        Color.rgb(
                                55,
                                55,
                                55
                        )
                )
        );


        receipt.addView(
                feeBox
        );


        // =====================================================
        // SIGNATURES
        // =====================================================

        LinearLayout signatureRow =
                new LinearLayout(this);


        signatureRow.setOrientation(
                LinearLayout.HORIZONTAL
        );


        signatureRow.setGravity(
                Gravity.CENTER
        );


        signatureRow.setPadding(
                0,
                14,
                0,
                4
        );


        TextView adminSign =
                createSignatureText(
                        "________________\n" +
                                "Admin Signature"
                );


        TextView cashierSign =
                createSignatureText(
                        "________________\n" +
                                "Cashier Signature"
                );


        signatureRow.addView(
                adminSign,
                new LinearLayout.LayoutParams(
                        0,
                        65,
                        1
                )
        );


        signatureRow.addView(
                cashierSign,
                new LinearLayout.LayoutParams(
                        0,
                        65,
                        1
                )
        );


        receipt.addView(
                signatureRow
        );


        // =====================================================
        // PRINT BUTTON 80dp
        // =====================================================

        Button btnPrint =
                createReceiptButton(
                        "🖨  PRINT RECEIPT",
                        true
                );


        LinearLayout.LayoutParams printParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        80
                );


        printParams.setMargins(
                0,
                10,
                0,
                6
        );


        btnPrint.setLayoutParams(
                printParams
        );


        receipt.addView(
                btnPrint
        );


        // =====================================================
        // CANCEL BUTTON 80dp
        // =====================================================

        Button btnCancel =
                createReceiptButton(
                        "CANCEL",
                        false
                );


        LinearLayout.LayoutParams cancelParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        80
                );


        cancelParams.setMargins(
                0,
                6,
                0,
                0
        );


        btnCancel.setLayoutParams(
                cancelParams
        );


        receipt.addView(
                btnCancel
        );


        // =====================================================
        // DIALOG
        // =====================================================

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(
                                scrollView
                        )
                        .create();


        // =====================================================
        // PRINT CLICK
        // =====================================================

        btnPrint.setOnClickListener(
                v -> printReceipt(
                        finalSchoolName,
                        studentName,
                        finalClassText,
                        totalFees,
                        paidFees,
                        remainingFees,
                        finalPaymentMode,
                        receiptNo,
                        receiptDate
                )
        );


        // =====================================================
        // CANCEL CLICK
        // =====================================================

        btnCancel.setOnClickListener(
                v -> dialog.dismiss()
        );


        // =====================================================
        // SHOW DIALOG
        // =====================================================

        dialog.show();


        // =====================================================
        // CENTER + PROPER SIZE
        // =====================================================

        Window window =
                dialog.getWindow();


        if (window != null) {

            int screenWidth =
                    getResources()
                            .getDisplayMetrics()
                            .widthPixels;


            window.setBackgroundDrawableResource(
                    android.R.color.white
            );


            // WIDTH ONLY
            // HEIGHT = CONTENT SIZE

            window.setLayout(
                    (int)
                            (
                                    screenWidth *
                                            0.92
                            ),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );


            // CENTER OF SCREEN

            WindowManager.LayoutParams params =
                    window.getAttributes();


            params.gravity =
                    Gravity.CENTER;


            window.setAttributes(
                    params
            );
        }
    }


    // =========================================================
    // RECEIPT NORMAL ROW
    // =========================================================

    private TextView createReceiptRow(
            String label,
            String value,
            boolean boldValue) {


        TextView tv =
                new TextView(this);


        tv.setText(
                label +
                        ":  " +
                        value
        );


        tv.setTextSize(
                13
        );


        tv.setTextColor(
                Color.rgb(
                        55,
                        55,
                        55
                )
        );


        tv.setPadding(
                0,
                4,
                0,
                4
        );


        if (boldValue) {

            tv.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }


        return tv;
    }


    // =========================================================
    // RECEIPT FEE ROW
    // =========================================================

    private TextView createFeeReceiptRow(
            String label,
            String value,
            int valueColor) {


        TextView tv =
                new TextView(this);


        tv.setText(
                label +
                        "          " +
                        value
        );


        tv.setTextSize(
                13
        );


        tv.setTypeface(
                null,
                Typeface.BOLD
        );


        tv.setTextColor(
                valueColor
        );


        tv.setPadding(
                0,
                6,
                0,
                6
        );


        return tv;
    }


    // =========================================================
    // SIGNATURE
    // =========================================================

    private TextView createSignatureText(
            String text) {


        TextView tv =
                new TextView(this);


        tv.setText(
                text
        );


        tv.setTextSize(
                11
        );


        tv.setTextColor(
                Color.rgb(
                        90,
                        90,
                        90
                )
        );


        tv.setGravity(
                Gravity.CENTER
        );


        return tv;
    }


    // =========================================================
    // RECEIPT BUTTON
    // =========================================================

    private Button createReceiptButton(
            String text,
            boolean purple) {


        Button button =
                new Button(this);


        button.setText(
                text
        );


        button.setTextSize(
                16
        );


        button.setAllCaps(
                false
        );


        button.setMinHeight(
                0
        );


        button.setMinWidth(
                0
        );


        button.setGravity(
                Gravity.CENTER
        );


        button.setTypeface(
                null,
                Typeface.BOLD
        );


        button.setPadding(
                8,
                0,
                8,
                0
        );


        if (purple) {

            button.setTextColor(
                    Color.WHITE
            );


            button.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    75,
                                    31,
                                    168
                            )
                    )
            );

        } else {

            button.setTextColor(
                    Color.rgb(
                            75,
                            31,
                            168
                    )
            );


            button.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.rgb(
                                    245,
                                    240,
                                    252
                            )
                    )
            );
        }


        return button;
    }


    // =========================================================
    // PRINT RECEIPT
    // =========================================================

    private void printReceipt(
            String schoolName,
            String studentName,
            String classText,
            double totalFees,
            double paidFees,
            double remainingFees,
            String paymentMode,
            String receiptNo,
            String date) {


        WebView webView =
                new WebView(this);


        String html =

                "<html>" +

                        "<head>" +

                        "<meta name='viewport' " +
                        "content='width=device-width, initial-scale=1'>" +

                        "</head>" +

                        "<body style='" +
                        "font-family:sans-serif;" +
                        "padding:40px;" +
                        "color:#222'>" +


                        "<div style='" +
                        "text-align:center'>" +

                        "<h1 style='" +
                        "color:#4B1FA8'>" +
                        escapeHtml(
                                schoolName
                        ) +
                        "</h1>" +

                        "<h2>" +
                        "FEE PAYMENT RECEIPT" +
                        "</h2>" +

                        "</div>" +


                        "<hr>" +


                        "<p><b>Receipt No:</b> " +
                        escapeHtml(
                                receiptNo
                        ) +
                        "</p>" +


                        "<p><b>Date:</b> " +
                        escapeHtml(
                                date
                        ) +
                        "</p>" +


                        "<p><b>Student:</b> " +
                        escapeHtml(
                                studentName
                        ) +
                        "</p>" +


                        "<p><b>" +
                        escapeHtml(
                                classText
                        ) +
                        "</b></p>" +


                        "<hr>" +


                        "<p><b>Total Fees:</b> ₹ " +
                        formatAmount(
                                totalFees
                        ) +
                        "</p>" +


                        "<p style='color:green'>" +
                        "<b>Total Paid:</b> ₹ " +
                        formatAmount(
                                paidFees
                        ) +
                        "</p>" +


                        "<p style='color:red'>" +
                        "<b>Remaining:</b> ₹ " +
                        formatAmount(
                                remainingFees
                        ) +
                        "</p>" +


                        "<p><b>Payment Mode:</b> " +
                        escapeHtml(
                                paymentMode
                        ) +
                        "</p>" +


                        "<br><br><br>" +


                        "<table width='100%'>" +

                        "<tr>" +

                        "<td style='text-align:center'>" +
                        "________________<br>" +
                        "Admin Signature" +
                        "</td>" +

                        "<td style='text-align:center'>" +
                        "________________<br>" +
                        "Cashier Signature" +
                        "</td>" +

                        "</tr>" +

                        "</table>" +


                        "</body>" +

                        "</html>";


        webView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url) {


                        PrintManager printManager =
                                (PrintManager)
                                        getSystemService(
                                                Context.PRINT_SERVICE
                                        );


                        PrintAttributes attributes =
                                new PrintAttributes.Builder()
                                        .setMediaSize(
                                                PrintAttributes
                                                        .MediaSize
                                                        .ISO_A4
                                        )
                                        .build();


                        printManager.print(
                                "Fee Receipt - " +
                                        receiptNo,
                                view.createPrintDocumentAdapter(
                                        "FeeReceipt"
                                ),
                                attributes
                        );
                    }
                }
        );


        webView.loadDataWithBaseURL(
                null,
                html,
                "text/html",
                "UTF-8",
                null
        );
    }


    // =========================================================
    // INFO TEXT
    // =========================================================

    private TextView createInfoText(
            String text) {


        TextView tv =
                new TextView(this);


        tv.setText(
                text
        );


        tv.setTextSize(
                13
        );


        tv.setTextColor(
                Color.rgb(
                        90,
                        90,
                        90
                )
        );


        tv.setPadding(
                0,
                3,
                0,
                3
        );


        return tv;
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key) {


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
    // GET DOUBLE
    // =========================================================

    private double getDouble(
            DocumentSnapshot document,
            String key) {


        Object value =
                document.get(key);


        if (value == null) {

            return 0;
        }


        try {

            return Double.parseDouble(
                    String.valueOf(
                            value
                    )
            );


        } catch (Exception e) {

            return 0;
        }
    }


    // =========================================================
    // FORMAT
    // =========================================================

    private String formatAmount(
            double amount) {


        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }


    // =========================================================
    // ESCAPE HTML
    // =========================================================

    private String escapeHtml(
            String text) {


        if (text == null) {

            return "";
        }


        return text
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }


    // =========================================================
    // EMPTY
    // =========================================================

    private void showEmpty() {


        allStudents.clear();


        if (studentContainer != null) {

            studentContainer.removeAllViews();
        }


        if (tvStudentCount != null) {

            tvStudentCount.setText(
                    "0 Students"
            );
        }


        if (tvNoStudents != null) {

            tvNoStudents.setVisibility(
                    View.VISIBLE
            );
        }
    }
}

