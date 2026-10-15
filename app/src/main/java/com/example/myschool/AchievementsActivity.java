package com.example.myschool;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AchievementsActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;
    private Button btnAddAchievement;

    private RecyclerView recyclerAchievements;
    private TextView tvEmpty;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth auth;
    private FirebaseFirestore db;


    // =========================================================
    // DATA
    // =========================================================

    private final List<AchievementModel> achievementList =
            new ArrayList<>();

    private AchievementAdapter adapter;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_achievements
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =====================================================
        // INIT
        // =====================================================

        initViews();

        setupRecycler();

        setupListeners();

        loadAchievements();
    }


    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnAddAchievement =
                findViewById(
                        R.id.btnAddAchievement
                );

        recyclerAchievements =
                findViewById(
                        R.id.recyclerAchievements
                );

        tvEmpty =
                findViewById(
                        R.id.tvEmpty
                );
    }


    // =========================================================
    // RECYCLER
    // =========================================================

    private void setupRecycler() {

        recyclerAchievements.setLayoutManager(
                new LinearLayoutManager(this)
        );


        adapter =
                new AchievementAdapter();


        recyclerAchievements.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );


        btnAddAchievement.setOnClickListener(
                v ->
                        showAchievementDialog(
                                null
                        )
        );
    }


    // =========================================================
    // LOAD ACHIEVEMENTS
    // =========================================================

    private void loadAchievements() {

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


        db.collection(
                        "schoolAchievements"
                )
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            achievementList.clear();


                            for (
                                    DocumentSnapshot doc :
                                    snapshot.getDocuments()
                            ) {

                                achievementList.add(
                                        new AchievementModel(
                                                doc.getId(),
                                                getString(
                                                        doc,
                                                        "title"
                                                ),
                                                getString(
                                                        doc,
                                                        "description"
                                                ),
                                                getString(
                                                        doc,
                                                        "date"
                                                )
                                        )
                                );
                            }


                            adapter.notifyDataSetChanged();

                            updateEmptyState();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to load achievements.",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }


    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (
                achievementList.isEmpty()
        ) {

            tvEmpty.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvEmpty.setVisibility(
                    View.GONE
            );
        }
    }


    // =========================================================
    // SHOW ADD / EDIT DIALOG
    // =========================================================

    private void showAchievementDialog(
            @Nullable AchievementModel existing
    ) {

        View view =
                LayoutInflater
                        .from(this)
                        .inflate(
                                R.layout.dialog_add_achievement,
                                null
                        );


        EditText etTitle =
                view.findViewById(
                        R.id.etTitle
                );


        EditText etDate =
                view.findViewById(
                        R.id.etDate
                );


        EditText etDescription =
                view.findViewById(
                        R.id.etDescription
                );


        // =====================================================
        // EXISTING DATA FOR EDIT
        // =====================================================

        if (
                existing != null
        ) {

            etTitle.setText(
                    existing.getTitle()
            );


            etDescription.setText(
                    existing.getDescription()
            );


            etDate.setText(
                    existing.getDate()
            );
        }


        // =====================================================
        // DEFAULT DATE FOR NEW
        // =====================================================

        if (
                existing == null
        ) {

            etDate.setText(
                    getTodayDate()
            );
        }


        // =====================================================
        // DATE PICKER
        // =====================================================

        etDate.setOnClickListener(
                v ->
                        showDatePicker(
                                etDate
                        )
        );


        // =====================================================
        // DIALOG
        // =====================================================

        AlertDialog dialog =
                new AlertDialog.Builder(
                        this
                )
                        .setTitle(
                                existing == null
                                        ? "Add Achievement"
                                        : "Edit Achievement"
                        )
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


        dialog.setOnShowListener(
                ignored -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(
                            v -> {

                                String title =
                                        etTitle.getText()
                                                .toString()
                                                .trim();


                                String description =
                                        etDescription.getText()
                                                .toString()
                                                .trim();


                                String date =
                                        etDate.getText()
                                                .toString()
                                                .trim();


                                // =========================================
                                // VALIDATION
                                // =========================================

                                if (
                                        title.isEmpty()
                                ) {

                                    etTitle.setError(
                                            "Enter achievement title"
                                    );

                                    etTitle.requestFocus();

                                    return;
                                }


                                if (
                                        date.isEmpty()
                                ) {

                                    etDate.setError(
                                            "Select achievement date"
                                    );

                                    etDate.requestFocus();

                                    return;
                                }


                                if (
                                        description.isEmpty()
                                ) {

                                    etDescription.setError(
                                            "Enter achievement description"
                                    );

                                    etDescription.requestFocus();

                                    return;
                                }


                                saveAchievement(
                                        existing,
                                        title,
                                        description,
                                        date,
                                        dialog
                                );
                            }
                    );
                }
        );


        dialog.show();
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void showDatePicker(
            EditText etDate
    ) {

        Calendar calendar =
                Calendar.getInstance();


        String currentDate =
                etDate.getText()
                        .toString()
                        .trim();


        // =====================================================
        // TRY EXISTING DATE
        // =====================================================

        if (
                !currentDate.isEmpty()
        ) {

            try {

                SimpleDateFormat format =
                        new SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                        );


                format.setLenient(
                        false
                );


                java.util.Date parsedDate =
                        format.parse(
                                currentDate
                        );


                if (
                        parsedDate != null
                ) {

                    calendar.setTime(
                            parsedDate
                    );
                }

            } catch (Exception ignored) {
            }
        }


        // =====================================================
        // DATE PICKER
        // =====================================================

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,

                        (
                                view,
                                year,
                                month,
                                dayOfMonth
                        ) -> {

                            String selectedDate =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );


                            etDate.setText(
                                    selectedDate
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


        datePickerDialog.show();
    }


    // =========================================================
    // TODAY DATE
    // =========================================================

    private String getTodayDate() {

        return new SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
        ).format(
                Calendar.getInstance()
                        .getTime()
        );
    }


    // =========================================================
    // SAVE ACHIEVEMENT
    // =========================================================

    private void saveAchievement(
            AchievementModel existing,
            String title,
            String description,
            String date,
            AlertDialog dialog
    ) {

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


        Map<String, Object> data =
                new HashMap<>();


        data.put(
                "schoolId",
                schoolId
        );


        data.put(
                "title",
                title
        );


        data.put(
                "description",
                description
        );


        data.put(
                "date",
                date
        );


        data.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );


        // =====================================================
        // ADD
        // =====================================================

        if (
                existing == null
        ) {

            data.put(
                    "createdAt",
                    FieldValue.serverTimestamp()
            );


            db.collection(
                            "schoolAchievements"
                    )
                    .add(
                            data
                    )
                    .addOnSuccessListener(
                            unused -> {

                                dialog.dismiss();


                                Toast.makeText(
                                        this,
                                        "Achievement saved.",
                                        Toast.LENGTH_SHORT
                                ).show();


                                loadAchievements();
                            }
                    )
                    .addOnFailureListener(
                            e ->
                                    Toast.makeText(
                                            this,
                                            "Unable to save achievement.",
                                            Toast.LENGTH_SHORT
                                    ).show()
                    );


            // =====================================================
            // UPDATE
            // =====================================================

        } else {

            db.collection(
                            "schoolAchievements"
                    )
                    .document(
                            existing.getDocumentId()
                    )
                    .update(
                            data
                    )
                    .addOnSuccessListener(
                            unused -> {

                                dialog.dismiss();


                                Toast.makeText(
                                        this,
                                        "Achievement updated.",
                                        Toast.LENGTH_SHORT
                                ).show();


                                loadAchievements();
                            }
                    )
                    .addOnFailureListener(
                            e ->
                                    Toast.makeText(
                                            this,
                                            "Unable to update achievement.",
                                            Toast.LENGTH_SHORT
                                    ).show()
                    );
        }
    }


    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void confirmDelete(
            AchievementModel achievement
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Delete Achievement"
                )
                .setMessage(
                        "Are you sure you want to delete this achievement?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (
                                dialog,
                                which
                        ) ->
                                deleteAchievement(
                                        achievement
                                )
                )
                .show();
    }


    // =========================================================
    // DELETE
    // =========================================================

    private void deleteAchievement(
            AchievementModel achievement
    ) {

        db.collection(
                        "schoolAchievements"
                )
                .document(
                        achievement.getDocumentId()
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Achievement deleted.",
                                    Toast.LENGTH_SHORT
                            ).show();


                            loadAchievements();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        this,
                                        "Unable to delete achievement.",
                                        Toast.LENGTH_SHORT
                                ).show()
                );
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot doc,
            String key
    ) {

        Object value =
                doc.get(key);


        if (
                value == null
        ) {

            return "";
        }


        return String.valueOf(
                value
        ).trim();
    }


    // =========================================================
    // ADAPTER
    // =========================================================

    private class AchievementAdapter
            extends RecyclerView.Adapter<
            AchievementAdapter.ViewHolder> {


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
                                    R.layout.item_achievement,
                                    parent,
                                    false
                            );


            return new ViewHolder(
                    view
            );
        }


        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder holder,
                int position
        ) {

            AchievementModel item =
                    achievementList.get(
                            position
                    );


            holder.tvTitle.setText(
                    item.getTitle()
            );


            holder.tvDate.setText(
                    item.getDate()
            );


            holder.tvDescription.setText(
                    item.getDescription()
            );


            holder.btnEdit.setOnClickListener(
                    v ->
                            showAchievementDialog(
                                    item
                            )
            );


            holder.btnDelete.setOnClickListener(
                    v ->
                            confirmDelete(
                                    item
                            )
            );
        }


        @Override
        public int getItemCount() {

            return achievementList.size();
        }


        // =====================================================
        // VIEW HOLDER
        // =====================================================

        class ViewHolder
                extends RecyclerView.ViewHolder {

            TextView tvTitle;
            TextView tvDate;
            TextView tvDescription;

            Button btnEdit;
            Button btnDelete;


            ViewHolder(
                    @NonNull View itemView
            ) {

                super(itemView);


                tvTitle =
                        itemView.findViewById(
                                R.id.tvTitle
                        );


                tvDate =
                        itemView.findViewById(
                                R.id.tvDate
                        );


                tvDescription =
                        itemView.findViewById(
                                R.id.tvDescription
                        );


                btnEdit =
                        itemView.findViewById(
                                R.id.btnEdit
                        );


                btnDelete =
                        itemView.findViewById(
                                R.id.btnDelete
                        );
            }
        }
    }
}