package com.example.myschool;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchoolFacilitiesActivity
        extends AppCompatActivity {

    private ImageButton btnBack;
    private android.widget.Button btnAddFacility;
    private RecyclerView recyclerFacilities;
    private TextView tvEmpty;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private SchoolFacilityAdapter adapter;

    private final List<SchoolFacilityModel> facilityList =
            new ArrayList<>();

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_school_facilities
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecycler();
        setupListeners();
        loadFacilities();
    }

    // =========================================================
    // INIT
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(R.id.btnBack);

        btnAddFacility =
                findViewById(R.id.btnAddFacility);

        recyclerFacilities =
                findViewById(
                        R.id.recyclerFacilities
                );

        tvEmpty =
                findViewById(R.id.tvEmpty);
    }

    // =========================================================
    // RECYCLER
    // =========================================================

    private void setupRecycler() {

        recyclerFacilities.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new SchoolFacilityAdapter(
                        this::showEditFacilityDialog,
                        this::confirmDelete
                );

        recyclerFacilities.setAdapter(adapter);
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBack.setOnClickListener(v ->
                finish()
        );

        btnAddFacility.setOnClickListener(v ->
                showAddFacilityDialog()
        );
    }

    // =========================================================
    // LOAD FACILITIES
    // =========================================================

    private void loadFacilities() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String schoolId =
                auth.getCurrentUser().getUid();

        db.collection("schoolFacilities")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    facilityList.clear();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String name =
                                getString(
                                        document,
                                        "name"
                                );

                        String description =
                                getString(
                                        document,
                                        "description"
                                );

                        if (name.isEmpty()) {
                            continue;
                        }

                        SchoolFacilityModel facility =
                                new SchoolFacilityModel(
                                        document.getId(),
                                        name,
                                        description,
                                        schoolId
                                );

                        facilityList.add(facility);
                    }

                    adapter.setFacilityList(
                            facilityList
                    );

                    updateEmptyState();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load facilities: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (facilityList.isEmpty()) {

            tvEmpty.setVisibility(
                    View.VISIBLE
            );

            recyclerFacilities.setVisibility(
                    View.GONE
            );

        } else {

            tvEmpty.setVisibility(
                    View.GONE
            );

            recyclerFacilities.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =========================================================
    // ADD FACILITY
    // =========================================================

    private void showAddFacilityDialog() {

        View view =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_school_facility,
                                null
                        );

        EditText etName =
                view.findViewById(
                        R.id.etFacilityName
                );

        EditText etDescription =
                view.findViewById(
                        R.id.etFacilityDescription
                );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Add Facility")
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
                d -> {

                    dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            )
                            .setOnClickListener(v -> {

                                String name =
                                        etName.getText()
                                                .toString()
                                                .trim();

                                String description =
                                        etDescription
                                                .getText()
                                                .toString()
                                                .trim();

                                if (name.isEmpty()) {

                                    etName.setError(
                                            "Enter facility name"
                                    );

                                    return;
                                }

                                saveFacility(
                                        name,
                                        description,
                                        dialog
                                );
                            });
                }
        );

        dialog.show();
    }

    // =========================================================
    // SAVE
    // =========================================================

    private void saveFacility(
            String name,
            String description,
            AlertDialog dialog
    ) {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String schoolId =
                auth.getCurrentUser().getUid();

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "schoolId",
                schoolId
        );

        data.put(
                "name",
                name
        );

        data.put(
                "description",
                description
        );

        data.put(
                "createdAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        db.collection("schoolFacilities")
                .add(data)
                .addOnSuccessListener(
                        documentReference -> {

                            dialog.dismiss();

                            Toast.makeText(
                                    this,
                                    "Facility added successfully.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadFacilities();
                        }
                )
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to save facility: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // EDIT FACILITY
    // =========================================================

    private void showEditFacilityDialog(
            SchoolFacilityModel facility
    ) {

        View view =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_school_facility,
                                null
                        );

        EditText etName =
                view.findViewById(
                        R.id.etFacilityName
                );

        EditText etDescription =
                view.findViewById(
                        R.id.etFacilityDescription
                );

        etName.setText(
                facility.getName()
        );

        etDescription.setText(
                facility.getDescription()
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Facility")
                        .setView(view)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Update",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                d -> {

                    dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            )
                            .setOnClickListener(v -> {

                                String name =
                                        etName.getText()
                                                .toString()
                                                .trim();

                                String description =
                                        etDescription
                                                .getText()
                                                .toString()
                                                .trim();

                                if (name.isEmpty()) {

                                    etName.setError(
                                            "Enter facility name"
                                    );

                                    return;
                                }

                                updateFacility(
                                        facility,
                                        name,
                                        description,
                                        dialog
                                );
                            });
                }
        );

        dialog.show();
    }

    // =========================================================
    // UPDATE
    // =========================================================

    private void updateFacility(
            SchoolFacilityModel facility,
            String name,
            String description,
            AlertDialog dialog
    ) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "name",
                name
        );

        data.put(
                "description",
                description
        );

        db.collection("schoolFacilities")
                .document(
                        facility.getId()
                )
                .update(data)
                .addOnSuccessListener(
                        unused -> {

                            dialog.dismiss();

                            Toast.makeText(
                                    this,
                                    "Facility updated.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadFacilities();
                        }
                )
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to update facility: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void confirmDelete(
            SchoolFacilityModel facility
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Facility")
                .setMessage(
                        "Delete "
                                + facility.getName()
                                + "?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteFacility(facility)
                )
                .show();
    }

    // =========================================================
    // DELETE
    // =========================================================

    private void deleteFacility(
            SchoolFacilityModel facility
    ) {

        db.collection("schoolFacilities")
                .document(
                        facility.getId()
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    "Facility deleted.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadFacilities();
                        }
                )
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to delete facility: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String field
    ) {

        Object value =
                document.get(field);

        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }
}