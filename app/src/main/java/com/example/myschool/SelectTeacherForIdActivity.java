package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectTeacherForIdActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private EditText etSearchTeacher;

    private RecyclerView recyclerTeachers;

    private TextView tvEmpty;

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;


    // =========================================================
    // ALL TEACHERS
    // =========================================================

    private final List<TeacherItem> teacherList =
            new ArrayList<>();


    // =========================================================
    // FILTERED TEACHERS
    // =========================================================

    private final List<TeacherItem> filteredTeacherList =
            new ArrayList<>();


    private TeacherIdListAdapter adapter;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_select_teacher_for_id
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();


        // =====================================================
        // VIEWS
        // =====================================================

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        etSearchTeacher =
                findViewById(
                        R.id.etSearchTeacher
                );

        recyclerTeachers =
                findViewById(
                        R.id.recyclerTeachers
                );

        tvEmpty =
                findViewById(
                        R.id.tvEmpty
                );


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        recyclerTeachers.setLayoutManager(
                new LinearLayoutManager(this)
        );


        adapter =
                new TeacherIdListAdapter(
                        filteredTeacherList,

                        staffId -> {

                            openTeacherIdCard(
                                    staffId
                            );

                        }
                );


        recyclerTeachers.setAdapter(
                adapter
        );


        // =====================================================
        // SEARCH
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


        // =====================================================
        // LOAD TEACHERS
        // =====================================================

        loadTeachers();
    }


    // =========================================================
    // OPEN TEACHER ID CARD
    // =========================================================

    private void openTeacherIdCard(
            String staffId
    ) {

        if (
                staffId == null ||
                        staffId.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Teacher ID not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        SelectTeacherForIdActivity.this,
                        TeacherIDCardActivity.class
                );


        intent.putExtra(
                "staffId",
                staffId
        );


        startActivity(intent);
    }


    // =========================================================
    // LOAD TEACHERS
    // =========================================================

    private void loadTeachers() {

        if (
                mAuth == null ||
                        mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
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
                        snapshot -> {

                            teacherList.clear();


                            for (
                                    DocumentSnapshot document
                                    : snapshot.getDocuments()
                            ) {

                                String staffType =
                                        getString(
                                                document,
                                                "staffType"
                                        );


                                // =================================================
                                // ONLY TEACHERS
                                // =================================================

                                if (
                                        !staffType.equalsIgnoreCase(
                                                "Teacher"
                                        )
                                ) {

                                    continue;
                                }


                                String firstName =
                                        getString(
                                                document,
                                                "firstName"
                                        );


                                String middleName =
                                        getString(
                                                document,
                                                "middleName"
                                        );


                                String lastName =
                                        getString(
                                                document,
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


                                // =================================================
                                // FIRESTORE DOCUMENT ID
                                // =================================================

                                String staffId =
                                        document.getId();


                                teacherList.add(
                                        new TeacherItem(
                                                staffId,
                                                fullName
                                        )
                                );
                            }


                            // =================================================
                            // SHOW ALL
                            // =================================================

                            filteredTeacherList.clear();

                            filteredTeacherList.addAll(
                                    teacherList
                            );


                            adapter.notifyDataSetChanged();


                            updateEmptyView();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to load teachers.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // SEARCH FILTER
    // =========================================================

    private void filterTeachers(
            String searchText
    ) {

        String query =
                searchText
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );


        filteredTeacherList.clear();


        if (query.isEmpty()) {

            filteredTeacherList.addAll(
                    teacherList
            );

        } else {

            for (
                    TeacherItem teacher
                    : teacherList
            ) {

                String teacherName =
                        teacher.getTeacherName()
                                .toLowerCase(
                                        Locale.getDefault()
                                );


                if (
                        teacherName.contains(
                                query
                        )
                ) {

                    filteredTeacherList.add(
                            teacher
                    );
                }
            }
        }


        adapter.notifyDataSetChanged();


        updateEmptyView();
    }


    // =========================================================
    // EMPTY VIEW
    // =========================================================

    private void updateEmptyView() {

        if (filteredTeacherList.isEmpty()) {

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


        return String.valueOf(
                value
        );
    }


    // =========================================================
    // MODEL
    // =========================================================

    public static class TeacherItem {

        private final String staffId;

        private final String teacherName;


        public TeacherItem(
                String staffId,
                String teacherName
        ) {

            this.staffId =
                    staffId;

            this.teacherName =
                    teacherName;
        }


        public String getStaffId() {

            return staffId;
        }


        public String getTeacherName() {

            return teacherName;
        }
    }


    // =========================================================
    // ADAPTER
    // =========================================================

    public static class TeacherIdListAdapter
            extends RecyclerView.Adapter<
            TeacherIdListAdapter.ViewHolder> {


        // =====================================================
        // CLICK LISTENER
        // =====================================================

        public interface OnIdCardClickListener {

            void onIdCardClick(
                    String staffId
            );
        }


        private final List<TeacherItem> list;

        private final OnIdCardClickListener listener;


        public TeacherIdListAdapter(
                List<TeacherItem> list,
                OnIdCardClickListener listener
        ) {

            this.list =
                    list;

            this.listener =
                    listener;
        }


        // =====================================================
        // CREATE VIEW HOLDER
        // =====================================================

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {

            View view =
                    LayoutInflater.from(
                            parent.getContext()
                    ).inflate(
                            R.layout.item_teacher_id_list,
                            parent,
                            false
                    );


            return new ViewHolder(
                    view
            );
        }


        // =====================================================
        // BIND
        // =====================================================

        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder holder,
                int position
        ) {

            TeacherItem teacher =
                    list.get(position);


            holder.tvTeacherName.setText(
                    teacher.getTeacherName()
            );


            // =================================================
            // FULL CARD CLICK
            // =================================================

            holder.cardTeacher.setOnClickListener(
                    v -> {

                        if (listener != null) {

                            listener.onIdCardClick(
                                    teacher.getStaffId()
                            );
                        }
                    }
            );


            // =================================================
            // VIEW ID CARD CLICK
            // =================================================

            holder.tvSeeIdCard.setOnClickListener(
                    v -> {

                        if (listener != null) {

                            listener.onIdCardClick(
                                    teacher.getStaffId()
                            );
                        }
                    }
            );
        }


        // =====================================================
        // ITEM COUNT
        // =====================================================

        @Override
        public int getItemCount() {

            return list.size();
        }


        // =====================================================
        // VIEW HOLDER
        // =====================================================

        public static class ViewHolder
                extends RecyclerView.ViewHolder {


            TextView tvTeacherName;

            TextView tvSeeIdCard;


            com.google.android.material.card.MaterialCardView
                    cardTeacher;


            public ViewHolder(
                    @NonNull View itemView
            ) {

                super(itemView);


                cardTeacher =
                        itemView.findViewById(
                                R.id.cardTeacher
                        );


                tvTeacherName =
                        itemView.findViewById(
                                R.id.tvTeacherName
                        );


                tvSeeIdCard =
                        itemView.findViewById(
                                R.id.tvSeeIdCard
                        );
            }
        }
    }
}