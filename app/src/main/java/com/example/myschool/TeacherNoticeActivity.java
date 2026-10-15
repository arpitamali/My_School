package com.example.myschool;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeacherNoticeActivity extends AppCompatActivity {

    private Spinner spinnerNoticeClass;

    private EditText etNoticeTitle;
    private EditText etNoticeDescription;

    private Button btnUploadNotice;

    private AppCompatImageButton btnNoticeBack;

    private ProgressBar noticeProgressBar;

    private RecyclerView recyclerNotices;

    private FirebaseFirestore db;

    private String staffId = "";
    private String schoolId = "";

    private final List<String> classList =
            new ArrayList<>();

    private final List<NoticeModel> noticeList =
            new ArrayList<>();

    private ArrayAdapter<String> classAdapter;

    private NoticeAdapter noticeAdapter;

    private ListenerRegistration noticeListener;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_notice
        );

        db = FirebaseFirestore.getInstance();

        staffId =
                getIntent().getStringExtra("staffId");

        if (staffId == null) {
            staffId = "";
        }

        initializeViews();

        setupClassSpinner();

        setupRecyclerView();

        setupClicks();

        loadTeacherData();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        spinnerNoticeClass =
                findViewById(
                        R.id.spinnerNoticeClass
                );

        etNoticeTitle =
                findViewById(
                        R.id.etNoticeTitle
                );

        etNoticeDescription =
                findViewById(
                        R.id.etNoticeDescription
                );

        btnUploadNotice =
                findViewById(
                        R.id.btnUploadNotice
                );

        btnNoticeBack =
                findViewById(
                        R.id.btnNoticeBack
                );

        noticeProgressBar =
                findViewById(
                        R.id.noticeProgressBar
                );

        recyclerNotices =
                findViewById(
                        R.id.recyclerNotices
                );
    }


    // =========================================================
    // CLASS SPINNER
    // =========================================================

    private void setupClassSpinner() {

        classAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        classList
                );

        spinnerNoticeClass.setAdapter(
                classAdapter
        );
    }


    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        noticeAdapter =
                new NoticeAdapter(
                        this,
                        noticeList
                );

        recyclerNotices.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerNotices.setAdapter(
                noticeAdapter
        );
    }


    // =========================================================
    // CLICKS
    // =========================================================

    private void setupClicks() {

        btnNoticeBack.setOnClickListener(
                v -> finish()
        );

        btnUploadNotice.setOnClickListener(
                v -> uploadNotice()
        );
    }


    // =========================================================
    // LOAD TEACHER
    // =========================================================

    private void loadTeacherData() {

        if (TextUtils.isEmpty(staffId)) {

            Toast.makeText(
                    this,
                    "Teacher ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!documentSnapshot.exists()) {

                                Toast.makeText(
                                        this,
                                        "Teacher profile not found",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            schoolId =
                                    documentSnapshot.getString(
                                            "schoolId"
                                    );

                            if (schoolId == null) {
                                schoolId = "";
                            }

                            loadClasses();

                            listenToNotices();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Unable to load teacher: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }


    // =========================================================
    // LOAD SCHOOL CLASSES
    // =========================================================

    private void loadClasses() {

        if (TextUtils.isEmpty(schoolId)) {
            return;
        }

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            Set<String> uniqueClasses =
                                    new HashSet<>();

                            for (DocumentSnapshot doc :
                                    querySnapshot.getDocuments()) {

                                String status =
                                        doc.getString(
                                                "status"
                                        );

                                if ("Deleted"
                                        .equalsIgnoreCase(
                                                status
                                        )) {

                                    continue;
                                }

                                String className =
                                        doc.getString(
                                                "className"
                                        );

                                if (className != null
                                        && !className
                                        .trim()
                                        .isEmpty()) {

                                    uniqueClasses.add(
                                            className.trim()
                                    );
                                }
                            }


                            classList.clear();


                            // First option
                            classList.add(
                                    "Select Class"
                            );


                            // Sort classes
                            List<String> sortedClasses =
                                    new ArrayList<>(
                                            uniqueClasses
                                    );

                            Collections.sort(
                                    sortedClasses,
                                    new Comparator<String>() {

                                        @Override
                                        public int compare(
                                                String a,
                                                String b) {

                                            try {

                                                String numberA =
                                                        a.replaceAll(
                                                                "[^0-9]",
                                                                ""
                                                        );

                                                String numberB =
                                                        b.replaceAll(
                                                                "[^0-9]",
                                                                ""
                                                        );

                                                if (!numberA.isEmpty()
                                                        && !numberB.isEmpty()) {

                                                    int numA =
                                                            Integer.parseInt(
                                                                    numberA
                                                            );

                                                    int numB =
                                                            Integer.parseInt(
                                                                    numberB
                                                            );

                                                    return Integer.compare(
                                                            numA,
                                                            numB
                                                    );
                                                }

                                            } catch (Exception ignored) {
                                            }

                                            return a.compareToIgnoreCase(
                                                    b
                                            );
                                        }
                                    }
                            );


                            // School classes
                            classList.addAll(
                                    sortedClasses
                            );


                            // All option at last
                            classList.add(
                                    "All"
                            );


                            classAdapter.notifyDataSetChanged();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Unable to load classes: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }


    // =========================================================
    // UPLOAD / SAVE NOTICE
    // =========================================================

    private void uploadNotice() {

        String selectedClass = "";

        if (spinnerNoticeClass.getSelectedItem() != null) {

            selectedClass =
                    spinnerNoticeClass
                            .getSelectedItem()
                            .toString();
        }


        String title =
                etNoticeTitle
                        .getText()
                        .toString()
                        .trim();


        String description =
                etNoticeDescription
                        .getText()
                        .toString()
                        .trim();


        // -----------------------------------------------------
        // CLASS VALIDATION
        // -----------------------------------------------------

        if (selectedClass.isEmpty()
                || selectedClass.equals("Select Class")) {

            Toast.makeText(
                    this,
                    "Please select class",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // TITLE VALIDATION
        // -----------------------------------------------------

        if (TextUtils.isEmpty(title)) {

            etNoticeTitle.setError(
                    "Enter notice title"
            );

            etNoticeTitle.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // DESCRIPTION VALIDATION
        // -----------------------------------------------------

        if (TextUtils.isEmpty(description)) {

            etNoticeDescription.setError(
                    "Enter notice description"
            );

            etNoticeDescription.requestFocus();

            return;
        }


        // -----------------------------------------------------
        // SCHOOL ID
        // -----------------------------------------------------

        if (TextUtils.isEmpty(schoolId)) {

            Toast.makeText(
                    this,
                    "School ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // LOADING
        // -----------------------------------------------------

        noticeProgressBar.setVisibility(
                View.VISIBLE
        );

        btnUploadNotice.setEnabled(
                false
        );


        // -----------------------------------------------------
        // NOTICE ID
        // -----------------------------------------------------

        String noticeId =
                db.collection("notices")
                        .document()
                        .getId();


        // -----------------------------------------------------
        // FIRESTORE DATA
        // -----------------------------------------------------

        Map<String, Object> notice =
                new HashMap<>();

        notice.put(
                "noticeId",
                noticeId
        );

        notice.put(
                "schoolId",
                schoolId
        );

        notice.put(
                "createdBy",
                staffId
        );

        notice.put(
                "className",
                selectedClass
        );

        notice.put(
                "title",
                title
        );

        notice.put(
                "description",
                description
        );

        notice.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );


        // -----------------------------------------------------
        // SAVE TO FIRESTORE
        // -----------------------------------------------------

        db.collection("notices")
                .document(noticeId)
                .set(notice)
                .addOnSuccessListener(
                        unused -> {

                            noticeProgressBar.setVisibility(
                                    View.GONE
                            );

                            btnUploadNotice.setEnabled(
                                    true
                            );

                            etNoticeTitle.setText("");

                            etNoticeDescription.setText("");


                            if (classList.size() > 0) {

                                spinnerNoticeClass
                                        .setSelection(0);
                            }


                            Toast.makeText(
                                    this,
                                    "Notice uploaded successfully",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            noticeProgressBar.setVisibility(
                                    View.GONE
                            );

                            btnUploadNotice.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    this,
                                    "Failed to save notice: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // REALTIME NOTICE LIST
    // =========================================================
    //
    // IMPORTANT:
    // orderBy("createdAt") intentionally removed.
    // This avoids Firestore composite index problem.
    //
    // =========================================================

    private void listenToNotices() {

        if (TextUtils.isEmpty(schoolId)) {
            return;
        }


        if (noticeListener != null) {

            noticeListener.remove();
        }


        noticeListener =
                db.collection("notices")
                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )
                        .addSnapshotListener(
                                (snapshots, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Failed to load notices: "
                                                        + error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }


                                    if (snapshots == null) {
                                        return;
                                    }


                                    noticeList.clear();


                                    for (DocumentSnapshot doc :
                                            snapshots.getDocuments()) {

                                        NoticeModel model =
                                                new NoticeModel();


                                        model.noticeId =
                                                doc.getString(
                                                        "noticeId"
                                                );


                                        model.schoolId =
                                                doc.getString(
                                                        "schoolId"
                                                );


                                        model.createdBy =
                                                doc.getString(
                                                        "createdBy"
                                                );


                                        model.className =
                                                doc.getString(
                                                        "className"
                                                );


                                        model.title =
                                                doc.getString(
                                                        "title"
                                                );


                                        model.description =
                                                doc.getString(
                                                        "description"
                                                );


                                        noticeList.add(
                                                model
                                        );
                                    }


                                    noticeAdapter
                                            .notifyDataSetChanged();
                                }
                        );
    }


    // =========================================================
    // NOTICE MODEL
    // =========================================================

    public static class NoticeModel {

        public String noticeId = "";

        public String schoolId = "";

        public String createdBy = "";

        public String className = "";

        public String title = "";

        public String description = "";


        public NoticeModel() {
        }
    }


    // =========================================================
    // ON DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (noticeListener != null) {

            noticeListener.remove();

            noticeListener = null;
        }
    }
}