package com.example.myschool;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SetSalaryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etSearchTeacher;
    private RecyclerView recyclerTeachers;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // All teachers
    private final List<TeacherItem> teachers =
            new ArrayList<>();

    // Filtered teachers
    private final List<TeacherItem> filteredTeachers =
            new ArrayList<>();

    private TeacherAdapter teacherAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_salary);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        etSearchTeacher = findViewById(R.id.etSearchTeacher);
        recyclerTeachers = findViewById(R.id.recyclerTeachers);
        tvEmpty = findViewById(R.id.tvEmpty);

        btnBack.setOnClickListener(v -> finish());

        recyclerTeachers.setLayoutManager(
                new LinearLayoutManager(this)
        );

        teacherAdapter = new TeacherAdapter();

        recyclerTeachers.setAdapter(
                teacherAdapter
        );

        // =====================================================
        // SEARCH TEACHER
        // =====================================================

        etSearchTeacher.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        filterTeachers(
                                s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        loadTeachers();
    }


    // =========================================================
    // LOAD TEACHERS
    // =========================================================

    private void loadTeachers() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String schoolId =
                auth.getCurrentUser().getUid();

        db.collection("staff")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(result -> {

                    teachers.clear();

                    for (
                            com.google.firebase.firestore.DocumentSnapshot doc
                            : result.getDocuments()
                    ) {

                        String staffType =
                                String.valueOf(
                                        doc.get("staffType") == null
                                                ? ""
                                                : doc.get("staffType")
                                );

                        // ONLY TEACHERS
                        if (!staffType.equalsIgnoreCase("Teacher")) {
                            continue;
                        }

                        String firstName =
                                get(doc, "firstName");

                        String middleName =
                                get(doc, "middleName");

                        String lastName =
                                get(doc, "lastName");

                        String name =
                                (
                                        firstName + " "
                                                + middleName + " "
                                                + lastName
                                )
                                        .replaceAll(
                                                "\\s+",
                                                " "
                                        )
                                        .trim();

                        if (name.isEmpty()) {
                            name = "Teacher";
                        }

                        teachers.add(
                                new TeacherItem(
                                        doc.getId(),
                                        name
                                )
                        );
                    }

                    // =================================================
                    // INITIAL FILTERED LIST = ALL TEACHERS
                    // =================================================

                    filteredTeachers.clear();

                    filteredTeachers.addAll(
                            teachers
                    );

                    teacherAdapter.notifyDataSetChanged();

                    updateEmptyView();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load teachers.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }


    // =========================================================
    // SEARCH FILTER
    // =========================================================

    private void filterTeachers(String searchText) {

        String query =
                searchText
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        filteredTeachers.clear();

        if (query.isEmpty()) {

            filteredTeachers.addAll(
                    teachers
            );

        } else {

            for (
                    TeacherItem teacher
                    : teachers
            ) {

                String teacherName =
                        teacher.name
                                .toLowerCase(
                                        Locale.getDefault()
                                );

                if (
                        teacherName.contains(
                                query
                        )
                ) {

                    filteredTeachers.add(
                            teacher
                    );
                }
            }
        }

        teacherAdapter.notifyDataSetChanged();

        updateEmptyView();
    }


    // =========================================================
    // EMPTY VIEW
    // =========================================================

    private void updateEmptyView() {

        if (filteredTeachers.isEmpty()) {

            tvEmpty.setVisibility(
                    View.VISIBLE
            );

            recyclerTeachers.setVisibility(
                    View.GONE
            );

        } else {

            tvEmpty.setVisibility(
                    View.GONE
            );

            recyclerTeachers.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // SALARY DIALOG
    // =========================================================

    private void showSalaryDialog(
            TeacherItem teacher
    ) {

        View view =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_set_salary,
                                null
                        );

        RadioGroup rgPaymentType =
                view.findViewById(
                        R.id.rgPaymentType
                );

        RadioButton rbFixed =
                view.findViewById(
                        R.id.rbFixed
                );

        RadioButton rbDayWise =
                view.findViewById(
                        R.id.rbDayWise
                );

        EditText etAmount =
                view.findViewById(
                        R.id.etAmount
                );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Set Salary")
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

        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String amountText =
                        etAmount.getText()
                                .toString()
                                .trim();

                if (amountText.isEmpty()) {

                    etAmount.setError(
                            "Enter amount"
                    );

                    return;
                }

                double amount;

                try {

                    amount =
                            Double.parseDouble(
                                    amountText
                            );

                } catch (Exception e) {

                    etAmount.setError(
                            "Invalid amount"
                    );

                    return;
                }

                if (amount <= 0) {

                    etAmount.setError(
                            "Enter valid amount"
                    );

                    return;
                }

                String paymentType;

                if (rbFixed.isChecked()) {

                    paymentType = "FIXED";

                } else {

                    paymentType = "DAY_WISE";
                }

                saveSalary(
                        teacher.staffId,
                        paymentType,
                        amount,
                        dialog
                );
            });
        });

        rbFixed.setChecked(true);

        dialog.show();
    }


    // =========================================================
    // SAVE SALARY
    // =========================================================

    private void saveSalary(
            String staffId,
            String paymentType,
            double amount,
            AlertDialog dialog
    ) {

        String schoolId =
                auth.getCurrentUser().getUid();

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "staffId",
                staffId
        );

        data.put(
                "schoolId",
                schoolId
        );

        data.put(
                "paymentType",
                paymentType
        );

        data.put(
                "amount",
                amount
        );

        data.put(
                "active",
                true
        );

        data.put(
                "updatedAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        db.collection("salarySettings")
                .document(staffId)
                .set(
                        data,
                        com.google.firebase.firestore.SetOptions
                                .merge()
                )
                .addOnSuccessListener(v -> {

                    dialog.dismiss();

                    Toast.makeText(
                            this,
                            "Salary saved successfully.",
                            Toast.LENGTH_SHORT
                    ).show();

                    teacherAdapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to save salary.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String get(
            com.google.firebase.firestore.DocumentSnapshot doc,
            String key
    ) {

        Object value =
                doc.get(key);

        return value == null
                ? ""
                : String.valueOf(value);
    }


    // =========================================================
    // TEACHER MODEL
    // =========================================================

    private static class TeacherItem {

        String staffId;
        String name;

        TeacherItem(
                String staffId,
                String name
        ) {

            this.staffId = staffId;
            this.name = name;
        }
    }


    // =========================================================
    // TEACHER ADAPTER
    // =========================================================

    private class TeacherAdapter
            extends RecyclerView.Adapter<
            TeacherAdapter.ViewHolder> {


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {

            View view =
                    LayoutInflater.from(
                                    parent.getContext()
                            )
                            .inflate(
                                    R.layout.item_teacher_salary,
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

            TeacherItem teacher =
                    filteredTeachers.get(position);

            holder.tvTeacherName.setText(
                    teacher.name
            );


            // =================================================
            // LOAD SALARY
            // =================================================

            db.collection("salarySettings")
                    .document(teacher.staffId)
                    .get()
                    .addOnSuccessListener(doc -> {

                        if (doc.exists()) {

                            String type =
                                    String.valueOf(
                                            doc.get("paymentType")
                                    );

                            Object amountObj =
                                    doc.get("amount");

                            String amount =
                                    amountObj == null
                                            ? "0"
                                            : String.valueOf(
                                            amountObj
                                    );

                            if ("DAY_WISE"
                                    .equals(type)) {

                                holder.tvSalaryInfo.setText(
                                        "Day Wise • ₹"
                                                + amount
                                                + " / day"
                                );

                            } else {

                                holder.tvSalaryInfo.setText(
                                        "Fixed • ₹"
                                                + amount
                                                + " / month"
                                );
                            }

                        } else {

                            holder.tvSalaryInfo.setText(
                                    "Salary not set"
                            );
                        }
                    });


            // =================================================
            // SET SALARY BUTTON
            // =================================================

            holder.tvSetSalary.setOnClickListener(
                    v -> showSalaryDialog(
                            teacher
                    )
            );
        }


        @Override
        public int getItemCount() {

            return filteredTeachers.size();
        }


        // =====================================================
        // VIEW HOLDER
        // =====================================================

        class ViewHolder
                extends RecyclerView.ViewHolder {

            TextView tvTeacherName;
            TextView tvSalaryInfo;
            TextView tvSetSalary;

            ViewHolder(
                    @NonNull View itemView
            ) {

                super(itemView);

                tvTeacherName =
                        itemView.findViewById(
                                R.id.tvTeacherName
                        );

                tvSalaryInfo =
                        itemView.findViewById(
                                R.id.tvSalaryInfo
                        );

                tvSetSalary =
                        itemView.findViewById(
                                R.id.tvSetSalary
                        );
            }
        }
    }
}