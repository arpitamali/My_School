package com.example.myschool;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalaryPaymentActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private Button btnPreviousMonth;
    private Button btnNextMonth;

    private TextView tvMonth;

    private RecyclerView recyclerSalary;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;

    private FirebaseAuth auth;


    // =========================================================
    // MONTH
    // =========================================================

    private Calendar selectedMonth;


    // =========================================================
    // DATA
    // =========================================================

    private final List<SalaryTeacher> salaryTeachers =
            new ArrayList<>();


    // =========================================================
    // REAL TIME ATTENDANCE LISTENER
    // =========================================================

    private ListenerRegistration attendanceListener;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_salary_payment
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        db =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();


        // =====================================================
        // INIT VIEWS
        // =====================================================

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnPreviousMonth =
                findViewById(
                        R.id.btnPreviousMonth
                );

        btnNextMonth =
                findViewById(
                        R.id.btnNextMonth
                );

        tvMonth =
                findViewById(
                        R.id.tvMonth
                );

        recyclerSalary =
                findViewById(
                        R.id.recyclerSalary
                );


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // RECYCLER
        // =====================================================

        recyclerSalary.setLayoutManager(
                new LinearLayoutManager(this)
        );


        // =====================================================
        // CURRENT MONTH
        // =====================================================

        selectedMonth =
                Calendar.getInstance();


        updateMonthTitle();


        // =====================================================
        // PREVIOUS MONTH
        // =====================================================

        btnPreviousMonth.setOnClickListener(v -> {

            selectedMonth.add(
                    Calendar.MONTH,
                    -1
            );

            updateMonthTitle();

            startAttendanceListener();
        });


        // =====================================================
        // NEXT MONTH
        // =====================================================

        btnNextMonth.setOnClickListener(v -> {

            selectedMonth.add(
                    Calendar.MONTH,
                    1
            );

            updateMonthTitle();

            startAttendanceListener();
        });


        // =====================================================
        // LOAD
        // =====================================================

        loadTeachersAndSalary();
    }


    // =========================================================
    // MONTH TITLE
    // =========================================================

    private void updateMonthTitle() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                );

        tvMonth.setText(
                format.format(
                        selectedMonth.getTime()
                )
        );
    }


    // =========================================================
    // MONTH KEY
    // =========================================================

    private String getMonthKey() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM",
                        Locale.getDefault()
                );

        return format.format(
                selectedMonth.getTime()
        );
    }


    // =========================================================
    // MONTH NUMBER
    // =========================================================

    private String getMonthNumber() {

        return String.format(
                Locale.getDefault(),
                "%02d",
                selectedMonth.get(
                        Calendar.MONTH
                ) + 1
        );
    }


    // =========================================================
    // YEAR
    // =========================================================

    private String getYear() {

        return String.valueOf(
                selectedMonth.get(
                        Calendar.YEAR
                )
        );
    }


    // =========================================================
    // LOAD TEACHERS + SALARY
    // =========================================================

    private void loadTeachersAndSalary() {

        if (
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


        db.collection("staff")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .whereEqualTo(
                        "active",
                        true
                )
                .get()
                .addOnSuccessListener(
                        staffResult -> {

                            salaryTeachers.clear();


                            for (
                                    DocumentSnapshot doc :
                                    staffResult.getDocuments()
                            ) {

                                String staffType =
                                        getString(
                                                doc,
                                                "staffType"
                                        );


                                if (!staffType
                                        .equalsIgnoreCase(
                                                "Teacher"
                                        )) {

                                    continue;
                                }


                                String firstName =
                                        getString(
                                                doc,
                                                "firstName"
                                        );

                                String middleName =
                                        getString(
                                                doc,
                                                "middleName"
                                        );

                                String lastName =
                                        getString(
                                                doc,
                                                "lastName"
                                        );


                                String fullName =
                                        (
                                                firstName
                                                        + " "
                                                        + middleName
                                                        + " "
                                                        + lastName
                                        )
                                                .replaceAll(
                                                        "\\s+",
                                                        " "
                                                )
                                                .trim();


                                if (fullName.isEmpty()) {

                                    fullName =
                                            "Teacher";
                                }


                                SalaryTeacher teacher =
                                        new SalaryTeacher();


                                teacher.staffId =
                                        doc.getId();


                                teacher.name =
                                        fullName;


                                salaryTeachers.add(
                                        teacher
                                );
                            }


                            loadSalarySettings(
                                    0
                            );
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to load teachers.",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }


    // =========================================================
    // LOAD SALARY SETTINGS
    // =========================================================

    private void loadSalarySettings(
            int index
    ) {

        if (
                index >= salaryTeachers.size()
        ) {

            startAttendanceListener();

            return;
        }


        SalaryTeacher teacher =
                salaryTeachers.get(index);


        db.collection("salarySettings")
                .document(
                        teacher.staffId
                )
                .get()
                .addOnSuccessListener(
                        doc -> {

                            if (doc.exists()) {

                                teacher.paymentType =
                                        getString(
                                                doc,
                                                "paymentType"
                                        );


                                teacher.amount =
                                        getDouble(
                                                doc,
                                                "amount"
                                        );

                            } else {

                                teacher.paymentType =
                                        "";

                                teacher.amount =
                                        0;
                            }


                            loadSalarySettings(
                                    index + 1
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            teacher.paymentType =
                                    "";

                            teacher.amount =
                                    0;


                            loadSalarySettings(
                                    index + 1
                            );
                        }
                );
    }


    // =========================================================
    // REAL TIME ATTENDANCE
    // =========================================================

    private void startAttendanceListener() {

        if (
                auth.getCurrentUser() == null
        ) {

            return;
        }


        if (attendanceListener != null) {

            attendanceListener.remove();

            attendanceListener =
                    null;
        }


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        String month =
                getMonthNumber();


        String year =
                getYear();


        attendanceListener =
                db.collection("attendance")
                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )
                        .whereEqualTo(
                                "month",
                                month
                        )
                        .whereEqualTo(
                                "year",
                                year
                        )
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Unable to load attendance.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }


                                    if (snapshot == null) {

                                        return;
                                    }


                                    resetWorkingDays();


                                    for (
                                            DocumentSnapshot doc :
                                            snapshot.getDocuments()
                                    ) {

                                        String staffId =
                                                getString(
                                                        doc,
                                                        "staffId"
                                                );


                                        String status =
                                                getString(
                                                        doc,
                                                        "status"
                                                );


                                        String date =
                                                getString(
                                                        doc,
                                                        "date"
                                                );


                                        if (
                                                staffId == null
                                                        ||
                                                        staffId.trim().isEmpty()
                                        ) {

                                            continue;
                                        }


                                        // =========================================
                                        // CURRENT MONTH:
                                        // DON'T COUNT FUTURE DATES
                                        // =========================================

                                        if (
                                                isFutureDateForSelectedMonth(
                                                        date
                                                )
                                        ) {

                                            continue;
                                        }


                                        SalaryTeacher teacher =
                                                findTeacher(
                                                        staffId
                                                );


                                        if (teacher == null) {

                                            continue;
                                        }


                                        // =========================================
                                        // PRESENT
                                        // =========================================

                                        if (
                                                "P".equalsIgnoreCase(
                                                        status
                                                )
                                        ) {

                                            teacher.workingDays +=
                                                    1.0;
                                        }


                                        // =========================================
                                        // HALF DAY
                                        // =========================================

                                        else if (
                                                "HD".equalsIgnoreCase(
                                                        status
                                                )
                                        ) {

                                            teacher.workingDays +=
                                                    0.5;
                                        }


                                        // =========================================
                                        // ABSENT = 0
                                        // HOLIDAY = 0
                                        // =========================================
                                    }


                                    loadPaymentHistory();
                                }
                        );
    }


    // =========================================================
    // RESET WORKING DAYS
    // =========================================================

    private void resetWorkingDays() {

        for (
                SalaryTeacher teacher :
                salaryTeachers
        ) {

            teacher.workingDays =
                    0.0;
        }
    }


    // =========================================================
    // FUTURE DATE CHECK
    // =========================================================

    private boolean isFutureDateForSelectedMonth(
            String attendanceDate
    ) {

        if (
                attendanceDate == null ||
                        attendanceDate.trim().isEmpty()
        ) {

            return false;
        }


        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    );

            format.setLenient(false);


            Calendar attendanceCalendar =
                    Calendar.getInstance();


            attendanceCalendar.setTime(
                    format.parse(
                            attendanceDate
                    )
            );


            Calendar today =
                    Calendar.getInstance();


            // =====================================================
            // ONLY BLOCK FUTURE DATES WHEN SELECTED MONTH IS
            // CURRENT MONTH
            // =====================================================

            boolean sameMonth =
                    attendanceCalendar.get(
                            Calendar.YEAR
                    ) ==
                            today.get(
                                    Calendar.YEAR
                            )
                            &&
                            attendanceCalendar.get(
                                    Calendar.MONTH
                            ) ==
                                    today.get(
                                            Calendar.MONTH
                                    );


            boolean selectedIsCurrentMonth =
                    selectedMonth.get(
                            Calendar.YEAR
                    ) ==
                            today.get(
                                    Calendar.YEAR
                            )
                            &&
                            selectedMonth.get(
                                    Calendar.MONTH
                            ) ==
                                    today.get(
                                            Calendar.MONTH
                                    );


            if (
                    sameMonth
                            &&
                            selectedIsCurrentMonth
            ) {

                return attendanceCalendar.after(
                        today
                );
            }


            return false;

        } catch (Exception e) {

            return false;
        }
    }


    // =========================================================
    // LOAD PAYMENT HISTORY
    // =========================================================

    private void loadPaymentHistory() {

        if (salaryTeachers.isEmpty()) {

            recyclerSalary.setAdapter(
                    new SalaryAdapter()
            );

            return;
        }


        final int total =
                salaryTeachers.size();


        final int[] completed =
                {0};


        for (
                SalaryTeacher teacher :
                salaryTeachers
        ) {

            String documentId =
                    getPaymentDocumentId(
                            teacher.staffId
                    );


            db.collection(
                            "salaryPayments"
                    )
                    .document(
                            documentId
                    )
                    .get()
                    .addOnSuccessListener(
                            paymentDoc -> {

                                if (
                                        paymentDoc.exists()
                                ) {

                                    teacher.paid =
                                            "PAID".equalsIgnoreCase(
                                                    getString(
                                                            paymentDoc,
                                                            "status"
                                                    )
                                            );

                                } else {

                                    teacher.paid =
                                            false;
                                }


                                completed[0]++;


                                if (
                                        completed[0] >=
                                                total
                                ) {

                                    showSalaryList();
                                }
                            }
                    )
                    .addOnFailureListener(
                            e -> {

                                teacher.paid =
                                        false;

                                completed[0]++;


                                if (
                                        completed[0] >=
                                                total
                                ) {

                                    showSalaryList();
                                }
                            }
                    );
        }
    }


    // =========================================================
    // SHOW LIST
    // =========================================================

    private void showSalaryList() {

        recyclerSalary.setAdapter(
                new SalaryAdapter()
        );
    }


    // =========================================================
    // FIND TEACHER
    // =========================================================

    private SalaryTeacher findTeacher(
            String staffId
    ) {

        for (
                SalaryTeacher teacher :
                salaryTeachers
        ) {

            if (
                    staffId.equals(
                            teacher.staffId
                    )
            ) {

                return teacher;
            }
        }


        return null;
    }


    // =========================================================
    // CALCULATE SALARY
    // =========================================================

    private double calculateSalary(
            SalaryTeacher teacher
    ) {

        if (teacher == null) {

            return 0;
        }


        // =====================================================
        // FIXED
        // =====================================================

        if (
                "FIXED".equalsIgnoreCase(
                        teacher.paymentType
                )
        ) {

            return teacher.amount;
        }


        // =====================================================
        // DAY WISE
        // =====================================================

        if (
                "DAY_WISE".equalsIgnoreCase(
                        teacher.paymentType
                )
        ) {

            return
                    teacher.workingDays
                            * teacher.amount;
        }


        return 0;
    }


    // =========================================================
    // PAYMENT DOCUMENT ID
    // =========================================================

    private String getPaymentDocumentId(
            String staffId
    ) {

        return
                getMonthKey()
                        + "_"
                        + staffId;
    }


    // =========================================================
    // MARK PAID
    // =========================================================

    private void confirmPayment(
            SalaryTeacher teacher
    ) {

        double salary =
                calculateSalary(
                        teacher
                );


        if (salary <= 0) {

            if (
                    "DAY_WISE".equalsIgnoreCase(
                            teacher.paymentType
                    )
            ) {

                Toast.makeText(
                        this,
                        "No working days found.",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "Salary is not set.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return;
        }


        new AlertDialog.Builder(this)

                .setTitle(
                        "Mark Salary as Paid?"
                )

                .setMessage(
                        teacher.name
                                + "\n\n"
                                + "Month: "
                                + tvMonth.getText().toString()
                                + "\n\n"
                                + "Salary: ₹"
                                + formatAmount(
                                salary
                        )
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Mark Paid",
                        (dialog, which) ->
                                markAsPaid(
                                        teacher,
                                        salary
                                )
                )

                .show();
    }


    // =========================================================
    // SAVE PAYMENT
    // =========================================================

    private void markAsPaid(
            SalaryTeacher teacher,
            double salary
    ) {

        if (
                auth.getCurrentUser() == null
        ) {

            return;
        }


        String schoolId =
                auth.getCurrentUser()
                        .getUid();


        String month =
                getMonthKey();


        String documentId =
                getPaymentDocumentId(
                        teacher.staffId
                );


        Map<String, Object> payment =
                new HashMap<>();


        payment.put(
                "staffId",
                teacher.staffId
        );


        payment.put(
                "schoolId",
                schoolId
        );


        payment.put(
                "month",
                month
        );


        payment.put(
                "paymentType",
                teacher.paymentType
        );


        payment.put(
                "workingDays",
                teacher.workingDays
        );


        payment.put(
                "rate",
                teacher.amount
        );


        payment.put(
                "salary",
                salary
        );


        payment.put(
                "status",
                "PAID"
        );


        payment.put(
                "paidAt",
                FieldValue.serverTimestamp()
        );


        db.collection("salaryPayments")
                .document(
                        documentId
                )
                .set(
                        payment
                )
                .addOnSuccessListener(
                        unused -> {

                            teacher.paid =
                                    true;


                            Toast.makeText(
                                    this,
                                    "Salary marked as paid.",
                                    Toast.LENGTH_SHORT
                            ).show();


                            showSalaryList();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to save payment.",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }


    // =========================================================
    // FORMAT AMOUNT
    // =========================================================

    private String formatAmount(
            double value
    ) {

        if (
                value ==
                        Math.floor(value)
        ) {

            return String.valueOf(
                    (long) value
            );
        }


        return String.format(
                Locale.getDefault(),
                "%.2f",
                value
        );
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


        return String.valueOf(value);
    }


    // =========================================================
    // GET DOUBLE
    // =========================================================

    private double getDouble(
            DocumentSnapshot document,
            String key
    ) {

        Object value =
                document.get(key);


        if (value == null) {

            return 0;
        }


        if (
                value instanceof Number
        ) {

            return ((Number) value)
                    .doubleValue();
        }


        try {

            return Double.parseDouble(
                    String.valueOf(value)
            );

        } catch (Exception e) {

            return 0;
        }
    }


    // =========================================================
    // MODEL
    // =========================================================

    private static class SalaryTeacher {

        String staffId = "";

        String name = "";

        String paymentType = "";

        double amount = 0;

        double workingDays = 0;

        boolean paid = false;
    }


    // =========================================================
    // ADAPTER
    // =========================================================

    private class SalaryAdapter
            extends RecyclerView.Adapter<
            SalaryAdapter.ViewHolder> {


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {

            View view =
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(
                                    R.layout.item_salary_payment,
                                    parent,
                                    false
                            );


            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder holder,
                int position
        ) {

            SalaryTeacher teacher =
                    salaryTeachers.get(
                            position
                    );


            // =====================================================
            // NAME
            // =====================================================

            holder.tvTeacherName.setText(
                    teacher.name
            );


            // =====================================================
            // SALARY NOT SET
            // =====================================================

            if (
                    teacher.paymentType == null
                            ||
                            teacher.paymentType.isEmpty()
            ) {

                holder.tvSalaryType.setText(
                        "Salary not set"
                );


                holder.tvWorkingDays.setText(
                        ""
                );


                holder.tvSalary.setText(
                        "Salary: ₹0"
                );


                holder.btnMarkPaid.setVisibility(
                        View.GONE
                );


                return;
            }


            // =====================================================
            // FIXED PAYMENT
            // =====================================================

            if (
                    "FIXED".equalsIgnoreCase(
                            teacher.paymentType
                    )
            ) {

                holder.tvSalaryType.setText(
                        "Fixed Payment • ₹"
                                + formatAmount(
                                teacher.amount
                        )
                                + " / month"
                );


                holder.tvWorkingDays.setText(
                        "Monthly fixed salary"
                );
            }


            // =====================================================
            // DAY WISE
            // =====================================================

            else if (
                    "DAY_WISE".equalsIgnoreCase(
                            teacher.paymentType
                    )
            ) {

                holder.tvSalaryType.setText(
                        "Day Wise • ₹"
                                + formatAmount(
                                teacher.amount
                        )
                                + " / day"
                );


                holder.tvWorkingDays.setText(
                        "Working Days Till Date: "
                                + formatAmount(
                                teacher.workingDays
                        )
                                + " × ₹"
                                + formatAmount(
                                teacher.amount
                        )
                );
            }


            // =====================================================
            // SALARY
            // =====================================================

            double salary =
                    calculateSalary(
                            teacher
                    );


            holder.tvSalary.setText(
                    "Current Salary: ₹"
                            + formatAmount(
                            salary
                    )
            );


            // =====================================================
            // PAID
            // =====================================================

            if (teacher.paid) {

                holder.btnMarkPaid.setText(
                        "PAID"
                );


                holder.btnMarkPaid.setEnabled(
                        false
                );


            } else {

                holder.btnMarkPaid.setText(
                        "Mark as Paid"
                );


                holder.btnMarkPaid.setEnabled(
                        true
                );


                holder.btnMarkPaid.setOnClickListener(
                        v ->
                                confirmPayment(
                                        teacher
                                )
                );
            }


            holder.btnMarkPaid.setVisibility(
                    View.VISIBLE
            );
        }


        @Override
        public int getItemCount() {

            return salaryTeachers.size();
        }


        // =====================================================
        // VIEW HOLDER
        // =====================================================

        class ViewHolder
                extends RecyclerView.ViewHolder {

            TextView tvTeacherName;

            TextView tvSalaryType;

            TextView tvWorkingDays;

            TextView tvSalary;

            Button btnMarkPaid;


            ViewHolder(
                    @NonNull View itemView
            ) {

                super(itemView);


                tvTeacherName =
                        itemView.findViewById(
                                R.id.tvTeacherName
                        );


                tvSalaryType =
                        itemView.findViewById(
                                R.id.tvSalaryType
                        );


                tvWorkingDays =
                        itemView.findViewById(
                                R.id.tvWorkingDays
                        );


                tvSalary =
                        itemView.findViewById(
                                R.id.tvSalary
                        );


                btnMarkPaid =
                        itemView.findViewById(
                                R.id.btnMarkPaid
                        );
            }
        }
    }


    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();


        if (attendanceListener != null) {

            attendanceListener.remove();

            attendanceListener = null;
        }
    }
}