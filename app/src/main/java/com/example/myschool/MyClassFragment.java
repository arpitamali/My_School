package com.example.myschool;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyClassFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView tvClassName;
    private TextView tvStudentCount;
    private TextView tvPresentCount;
    private TextView tvAbsentCount;
    private TextView tvAttendancePercent;
    private TextView tvEmpty;
    private EditText etStudentSearch;

    private MaterialButton btnAddStudent;
    private MaterialButton btnAttendance;
    private MaterialButton btnHomework;
    private MaterialButton btnAnnouncements;
    private MaterialButton btnRefresh;

    private RecyclerView recyclerStudents;
    private MyClassStudentAdapter adapter;
    private final List<StudentModel> students = new ArrayList<>();
    private final List<StudentModel> allStudents = new ArrayList<>();

    private ListenerRegistration studentsListener;
    private ListenerRegistration todayAttendanceListener;

    private String staffId = "";
    private String schoolId = "";
    private String assignedClass = "";
    private String assignedDivision = "";

    public MyClassFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_class, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews(view);
        setupRecycler();
        setupClicks();
        loadTeacherAssignment();

        return view;
    }

    private void initViews(View view) {
        tvClassName = view.findViewById(R.id.tvMyClassName);
        tvStudentCount = view.findViewById(R.id.tvMyStudentCount);
        tvPresentCount = view.findViewById(R.id.tvMyPresentCount);
        tvAbsentCount = view.findViewById(R.id.tvMyAbsentCount);
        tvAttendancePercent = view.findViewById(R.id.tvMyAttendancePercent);


        tvEmpty = view.findViewById(R.id.tvMyClassEmpty);
        etStudentSearch = view.findViewById(R.id.etMyClassSearch);

        btnAddStudent = view.findViewById(R.id.btnMyClassAddStudent);
        btnAttendance = view.findViewById(R.id.btnMyClassAttendance);
        btnHomework = view.findViewById(R.id.btnMyClassHomework);
        btnAnnouncements = view.findViewById(R.id.btnMyClassAnnouncements);
        btnRefresh = view.findViewById(R.id.btnMyClassRefresh);

        recyclerStudents = view.findViewById(R.id.recyclerMyClassStudents);
    }

    private void setupRecycler() {
        recyclerStudents.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MyClassStudentAdapter(requireContext(), students, new MyClassStudentAdapter.Listener() {

            @Override
            public void onView(StudentModel student) {
                openStudentDetails(student);
            }

            @Override
            public void onAttendance(StudentModel student) {
                openStudentAttendance(student);
            }

            @Override
            public void onMarks(StudentModel student) {
                openStudentMarks(student);
            }

            @Override
            public void onProgress(StudentModel student) {
                openStudentProgress(student);
            }

            @Override
            public void onReport(StudentModel student) {
                openReportCard(student);
            }
        });

        recyclerStudents.setAdapter(adapter);
    }

    private void setupClicks() {
        btnAddStudent.setOnClickListener(v -> openAddStudent());
        btnAttendance.setOnClickListener(v -> openClassAttendance());
        btnHomework.setOnClickListener(v -> openClassWork("homework"));
        btnAnnouncements.setOnClickListener(v -> openClassWork("announcement"));
        btnRefresh.setOnClickListener(v -> loadStudents());

        etStudentSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadTeacherAssignment() {
        if (auth.getCurrentUser() == null) {
            showError("Teacher login session not found.");
            return;
        }

        staffId = requireActivity().getIntent().getStringExtra("staffId");

        if (staffId == null) {
            staffId = "";
        }

        staffId = staffId.trim();

        if (staffId.isEmpty()) {
            showError("Teacher profile ID is missing. Please login again.");
            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        showError("Teacher profile not found.");
                        return;
                    }

                    schoolId = safe(document.getString("schoolId"));
                    assignedClass = safe(document.getString("assignedClass"));
                    assignedDivision = safe(document.getString("assignedDivision"));

                    String displayClass = assignedClass;

                    if (displayClass.isEmpty()) {
                        displayClass = "Class not assigned";
                    }

                    if (!assignedDivision.isEmpty()) {
                        displayClass += " - " + assignedDivision;
                    }

                    tvClassName.setText(displayClass);

                    if (schoolId.isEmpty() || assignedClass.isEmpty()) {
                        showError("Please ask admin to assign your class.");
                        return;
                    }

                    loadStudents();
                })
                .addOnFailureListener(e ->
                        showError("Unable to load teacher profile."));
    }

    private void loadStudents() {
        if (schoolId.isEmpty() || assignedClass.isEmpty()) {
            return;
        }

        if (studentsListener != null) {
            studentsListener.remove();
        }

        studentsListener = db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .addSnapshotListener((snapshot, error) -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (error != null || snapshot == null) {
                        showError("Unable to load students: "
                                + (error == null
                                ? "Unknown error"
                                : error.getMessage()));
                        return;
                    }

                    allStudents.clear();

                    for (DocumentSnapshot document : snapshot.getDocuments()) {

                        StudentModel student =
                                document.toObject(StudentModel.class);

                        if (student == null) {
                            continue;
                        }

                        student.setStudentId(
                                safe(student.getStudentId()).isEmpty()
                                        ? document.getId()
                                        : student.getStudentId()
                        );

                        if ("Deleted".equalsIgnoreCase(
                                safe(student.getStatus()))) {
                            continue;
                        }

                        if (!classMatches(
                                assignedClass,
                                student.getClassName())) {
                            continue;
                        }

                        if (!divisionMatches(
                                assignedDivision,
                                student.getDivision())) {
                            continue;
                        }

                        allStudents.add(student);
                    }

                    sortList(allStudents);

                    applySearchFilter(
                            etStudentSearch == null
                                    ? ""
                                    : etStudentSearch.getText().toString()
                    );

                    updateOverview();
                });

        listenTodayAttendance();
    }

    private void applySearchFilter(String query) {

        String q = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        students.clear();

        for (StudentModel student : allStudents) {

            if (q.isEmpty()
                    || fullName(student)
                    .toLowerCase(Locale.ROOT)
                    .contains(q)
                    || safe(student.getRollNumber())
                    .toLowerCase(Locale.ROOT)
                    .contains(q)
                    || safe(student.getAdmissionNumber())
                    .toLowerCase(Locale.ROOT)
                    .contains(q)
                    || safe(student.getStudentPhone())
                    .toLowerCase(Locale.ROOT)
                    .contains(q)) {

                students.add(student);
            }
        }

        sortList(students);
        adapter.notifyDataSetChanged();

        tvEmpty.setText(
                q.isEmpty()
                        ? "No students found in your assigned class."
                        : "No student matches your search."
        );

        tvEmpty.setVisibility(
                students.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void updateOverview() {

        int count = allStudents.size();
        int boys = 0;
        int girls = 0;

        for (StudentModel student : allStudents) {

            String gender =
                    safe(student.getGender())
                            .toLowerCase(Locale.ROOT);

            if (gender.equals("male")
                    || gender.equals("boy")
                    || gender.equals("m")) {

                boys++;

            } else if (gender.equals("female")
                    || gender.equals("girl")
                    || gender.equals("f")) {

                girls++;
            }
        }

        tvStudentCount.setText(String.valueOf(count));


        if (count == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void listenTodayAttendance() {

        if (schoolId.isEmpty()) {
            return;
        }

        if (todayAttendanceListener != null) {
            todayAttendanceListener.remove();
        }

        String today =
                TeacherStudentAttendanceActivity.todayKey();

        todayAttendanceListener =
                db.collection("studentAttendance")
                        .whereEqualTo("schoolId", schoolId)
                        .whereEqualTo("date", today)
                        .addSnapshotListener((snapshot, error) -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (error != null || snapshot == null) {

                                tvPresentCount.setText("0");
                                tvAbsentCount.setText("0");
                                tvAttendancePercent.setText("--");

                                return;
                            }

                            int present = 0;
                            int absent = 0;
                            int holiday = 0;
                            int applicable = 0;

                            for (DocumentSnapshot document :
                                    snapshot.getDocuments()) {

                                String studentId =
                                        safe(document.getString("studentId"));

                                if (!containsStudentInAll(studentId)) {
                                    continue;
                                }

                                String status =
                                        safe(document.getString("status"));

                                if (status.equalsIgnoreCase("Half Day")) {
                                    status = "Absent";
                                }

                                if (status.equalsIgnoreCase("Present")) {

                                    present++;
                                    applicable++;

                                } else if (status.equalsIgnoreCase("Absent")) {

                                    absent++;
                                    applicable++;

                                } else if (status.equalsIgnoreCase("Holiday")) {

                                    holiday++;
                                }
                            }

                            tvPresentCount.setText(
                                    String.valueOf(present)
                            );

                            tvAbsentCount.setText(
                                    String.valueOf(absent)
                            );

                            if (allStudents.isEmpty()) {

                                tvAttendancePercent.setText("--");

                            } else if (holiday == allStudents.size()) {

                                tvAttendancePercent.setText("Holiday");

                            } else if (applicable == 0 && holiday == 0) {

                                tvAttendancePercent.setText("Not marked");

                            } else {

                                int percent =
                                        Math.round(
                                                (present * 100f)
                                                        / Math.max(
                                                        1,
                                                        applicable
                                                )
                                        );

                                tvAttendancePercent.setText(
                                        percent + "%"
                                );
                            }
                        });
    }

    private boolean containsStudentInAll(String studentId) {

        for (StudentModel student : allStudents) {

            if (safe(student.getStudentId())
                    .equals(studentId)) {

                return true;
            }
        }

        return false;
    }

    private void sortList(List<StudentModel> list) {

        list.sort((a, b) -> {

            int rollA =
                    parseInt(a.getRollNumber(), 999999);

            int rollB =
                    parseInt(b.getRollNumber(), 999999);

            if (rollA != rollB) {
                return Integer.compare(rollA, rollB);
            }

            return fullName(a)
                    .compareToIgnoreCase(fullName(b));
        });
    }

    private int parseInt(String value, int fallback) {

        try {
            return Integer.parseInt(safe(value));

        } catch (Exception ignored) {
            return fallback;
        }
    }

    private boolean classMatches(
            String teacherClass,
            String studentClass) {

        String a = normalizeClass(teacherClass);
        String b = normalizeClass(studentClass);

        return a.equals(b);
    }

    private String normalizeClass(String value) {

        String v =
                safe(value)
                        .toLowerCase(Locale.ROOT)
                        .replace("class", "")
                        .replace("standard", "")
                        .replace("std", "")
                        .replace("-", "")
                        .trim();

        return v.replaceAll("\\s+", "");
    }

    private boolean divisionMatches(
            String teacherDivision,
            String studentDivision) {

        if (teacherDivision.isEmpty()
                || teacherDivision.equalsIgnoreCase(
                "All Divisions")) {

            return true;
        }

        return teacherDivision.equalsIgnoreCase(
                safe(studentDivision)
        );
    }

    private void openAddStudent() {

        Intent intent =
                new Intent(
                        requireActivity(),
                        AddStudentStep1.class
                );

        intent.putExtra(
                "teacherStaffId",
                staffId
        );

        intent.putExtra(
                "staffId",
                staffId
        );

        startActivity(intent);
    }

    private void openClassAttendance() {

        Intent intent =
                new Intent(
                        requireActivity(),
                        TeacherStudentAttendanceActivity.class
                );

        intent.putExtra(
                "staffId",
                staffId
        );

        // FIX: My Class should directly open assigned class.
        intent.putExtra(
                "attendanceMode",
                "my_class"
        );

        startActivity(intent);
    }

    private void openClassWork(String mode) {

        Intent intent =
                new Intent(
                        requireActivity(),
                        TeacherClassWorkActivity.class
                );

        intent.putExtra(
                "staffId",
                staffId
        );

        intent.putExtra(
                "mode",
                mode
        );

        startActivity(intent);
    }

    private void openStudentDetails(StudentModel student) {

        Intent intent =
                new Intent(
                        requireActivity(),
                        StudentDetailsActivity.class
                );

        intent.putExtra(
                "studentId",
                student.getStudentId()
        );

        startActivity(intent);
    }

    private void openStudentAttendance(StudentModel student) {

        Intent intent =
                new Intent(
                        requireActivity(),
                        StudentAttendanceActivity.class
                );

        intent.putExtra(
                "studentId",
                student.getStudentId()
        );

        intent.putExtra(
                "schoolId",
                schoolId
        );

        startActivity(intent);
    }

    private void openStudentMarks(StudentModel student) {

        Intent intent =
                new Intent(
                        requireActivity(),
                        TeacherMarksActivity.class
                );

        intent.putExtra(
                "staffId",
                staffId
        );

        intent.putExtra(
                "studentId",
                student.getStudentId()
        );

        intent.putExtra(
                "studentName",
                fullName(student)
        );

        intent.putExtra(
                "schoolId",
                schoolId
        );

        intent.putExtra(
                "className",
                student.getClassName()
        );

        intent.putExtra(
                "division",
                student.getDivision()
        );

        startActivity(intent);
    }

    private void openStudentProgress(StudentModel student) {

        Intent intent =
                new Intent(
                        requireActivity(),
                        StudentProgressActivity.class
                );

        intent.putExtra(
                "staffId",
                staffId
        );

        intent.putExtra(
                "studentId",
                student.getStudentId()
        );

        intent.putExtra(
                "studentName",
                fullName(student)
        );

        intent.putExtra(
                "schoolId",
                schoolId
        );

        intent.putExtra(
                "className",
                student.getClassName()
        );

        intent.putExtra(
                "division",
                student.getDivision()
        );

        startActivity(intent);
    }

    private void openReportCard(StudentModel student) {

        Intent intent =
                new Intent(
                        requireActivity(),
                        StudentReportCardActivity.class
                );

        intent.putExtra(
                "studentId",
                student.getStudentId()
        );

        intent.putExtra(
                "schoolId",
                schoolId
        );

        startActivity(intent);
    }

    private String fullName(StudentModel student) {

        String name =
                (safe(student.getFirstName())
                        + " "
                        + safe(student.getMiddleName())
                        + " "
                        + safe(student.getLastName()))
                        .trim();

        return name.isEmpty()
                ? "Student"
                : name.replaceAll("\\s+", " ");
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value.trim();
    }

    @Override
    public void onDestroyView() {

        if (studentsListener != null) {
            studentsListener.remove();
        }

        if (todayAttendanceListener != null) {
            todayAttendanceListener.remove();
        }

        studentsListener = null;
        todayAttendanceListener = null;

        super.onDestroyView();
    }

    private void showError(String message) {

        if (isAdded()) {

            Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}