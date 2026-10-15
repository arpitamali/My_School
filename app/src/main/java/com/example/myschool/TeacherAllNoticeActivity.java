package com.example.myschool;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TeacherAllNoticeActivity extends AppCompatActivity {

    private AppCompatImageButton btnBack;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;

    private String staffId = "";
    private String schoolId = "";

    private final List<TeacherNoticeActivity.NoticeModel> noticeList =
            new ArrayList<>();

    private NoticeAdapter noticeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_teacher_all_notice);

        db = FirebaseFirestore.getInstance();

        staffId = getIntent().getStringExtra("staffId");

        if (staffId == null) {
            staffId = "";
        }

        initializeViews();
        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());

        loadTeacherData();
    }

    private void initializeViews() {

        btnBack = findViewById(R.id.btnAllNoticeBack);

        progressBar = findViewById(
                R.id.allNoticeProgressBar
        );

        recyclerView = findViewById(
                R.id.recyclerAllNotices
        );
    }

    private void setupRecyclerView() {

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        noticeAdapter = new NoticeAdapter(
                this,
                noticeList
        );

        recyclerView.setAdapter(noticeAdapter);
    }

    private void loadTeacherData() {

        if (staffId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Teacher information not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(
                                this,
                                "Teacher profile not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    schoolId = documentSnapshot.getString("schoolId");

                    if (schoolId == null) {
                        schoolId = "";
                    }

                    listenToAllNotices();
                })
                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(
                            this,
                            "Failed to load teacher data: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void listenToAllNotices() {

        if (schoolId.isEmpty()) {

            progressBar.setVisibility(View.GONE);

            Toast.makeText(
                    this,
                    "School information not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * IMPORTANT:
         * No orderBy("createdAt") here.
         *
         * This avoids Firestore composite-index
         * problems with whereEqualTo().
         */
        db.collection("notices")
                .whereEqualTo("schoolId", schoolId)
                .addSnapshotListener((snapshots, error) -> {

                    progressBar.setVisibility(View.GONE);

                    if (error != null) {

                        Toast.makeText(
                                this,
                                "Failed to load notices: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    noticeList.clear();

                    if (snapshots != null) {

                        for (DocumentSnapshot document :
                                snapshots.getDocuments()) {

                            TeacherNoticeActivity.NoticeModel model =
                                    new TeacherNoticeActivity.NoticeModel();

                            model.noticeId =
                                    document.getString("noticeId");

                            model.schoolId =
                                    document.getString("schoolId");

                            model.createdBy =
                                    document.getString("createdBy");

                            model.className =
                                    document.getString("className");

                            model.title =
                                    document.getString("title");

                            model.description =
                                    document.getString("description");

                            noticeList.add(model);
                        }
                    }

                    noticeAdapter.notifyDataSetChanged();
                });
    }
}