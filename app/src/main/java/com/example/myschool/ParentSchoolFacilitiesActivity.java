package com.example.myschool;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ParentSchoolFacilitiesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerFacilities;
    private TextView tvEmpty;
    private SchoolFacilityAdapter adapter;
    private final List<SchoolFacilityModel> facilityList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_school_facilities);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnParentFacilitiesBack);
        recyclerFacilities = findViewById(R.id.recyclerParentFacilities);
        tvEmpty = findViewById(R.id.tvParentFacilitiesEmpty);

        btnBack.setOnClickListener(v -> finish());

        recyclerFacilities.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SchoolFacilityAdapter(null, null);
        recyclerFacilities.setAdapter(adapter);

        loadFacilities();
    }

    private void loadFacilities() {
        String schoolId = getIntent().getStringExtra("schoolId");

        if (schoolId == null || schoolId.trim().isEmpty()) {
            schoolId = getSharedPreferences("MySchoolPrefs", MODE_PRIVATE)
                    .getString("schoolId", "");
        }

        if (schoolId == null || schoolId.trim().isEmpty()) {
            String studentId = getIntent().getStringExtra("studentId");
            if (studentId != null && !studentId.trim().isEmpty()) {
                final String sid = studentId;
                db.collection("students").document(sid).get()
                        .addOnSuccessListener(student -> {
                            String id = student.getString("schoolId");
                            if (id != null && !id.trim().isEmpty()) {
                                loadFacilitiesForSchool(id);
                            } else {
                                showEmpty();
                            }
                        })
                        .addOnFailureListener(e -> showEmpty());
                return;
            }
        }

        loadFacilitiesForSchool(schoolId);
    }

    private void loadFacilitiesForSchool(String schoolId) {
        if (schoolId == null || schoolId.trim().isEmpty()) {
            showEmpty();
            return;
        }

        db.collection("schoolFacilities")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    facilityList.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = safe(doc.getString("name"));
                        if (name.isEmpty()) continue;

                        facilityList.add(new SchoolFacilityModel(
                                doc.getId(),
                                name,
                                safe(doc.getString("description")),
                                schoolId
                        ));
                    }

                    adapter.setFacilityList(facilityList);

                    if (facilityList.isEmpty()) {
                        showEmpty();
                    } else {
                        tvEmpty.setVisibility(android.view.View.GONE);
                        recyclerFacilities.setVisibility(android.view.View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Unable to load facilities: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    showEmpty();
                });
    }

    private void showEmpty() {
        tvEmpty.setVisibility(android.view.View.VISIBLE);
        recyclerFacilities.setVisibility(android.view.View.GONE);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
