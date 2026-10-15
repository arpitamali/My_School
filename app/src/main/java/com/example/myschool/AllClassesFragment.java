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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllClassesFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Main screen
    private RecyclerView recyclerClasses;
    private AllClassesAdapter classAdapter;

    // Students screen
    private RecyclerView recyclerStudents;
    private MyClassStudentAdapter studentAdapter;

    private EditText etStudentSearch;

    private TextView tvTitle;
    private TextView tvEmpty;

    private ImageButton btnBack;

    private final List<String> classList = new ArrayList<>();
    private final Map<String, Integer> classStudentCount = new HashMap<>();

    private final List<StudentModel> allStudents = new ArrayList<>();
    private final List<StudentModel> filteredStudents = new ArrayList<>();

    private ListenerRegistration studentsListener;

    private String staffId = "";
    private String schoolId = "";

    private String selectedClass = "";

    private boolean showingStudents = false;

    public AllClassesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_all_classes,
                container,
                false
        );

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews(view);

        setupClassRecycler();
        setupStudentRecycler();
        setupSearch();
        setupBackButton();

        loadTeacher();

        return view;
    }

    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews(View view) {

        recyclerClasses = view.findViewById(
                R.id.recyclerAllClasses
        );

        recyclerStudents = view.findViewById(
                R.id.recyclerAllClassStudents
        );

        etStudentSearch = view.findViewById(
                R.id.etAllClassStudentSearch
        );

        tvTitle = view.findViewById(
                R.id.tvAllClassesTitle
        );

        tvEmpty = view.findViewById(
                R.id.tvAllClassesEmpty
        );

        btnBack = view.findViewById(
                R.id.btnAllClassesBack
        );
    }

    // =========================================================
    // CLASS RECYCLER
    // =========================================================

    private void setupClassRecycler() {

        recyclerClasses.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        classAdapter = new AllClassesAdapter(
                requireContext(),
                classList,
                classStudentCount,
                className -> openClassStudents(className)
        );

        recyclerClasses.setAdapter(classAdapter);
    }

    // =========================================================
    // STUDENT RECYCLER
    // SAME ADAPTER AS MY CLASS
    // =========================================================

    private void setupStudentRecycler() {

        recyclerStudents.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        studentAdapter = new MyClassStudentAdapter(
                requireContext(),
                filteredStudents,
                new MyClassStudentAdapter.Listener() {

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
                }
        );

        recyclerStudents.setAdapter(studentAdapter);
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        etStudentSearch.addTextChangedListener(
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

                        filterStudents(
                                s == null ? "" : s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    private void setupBackButton() {

        btnBack.setOnClickListener(v -> showClassList());

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                if (showingStudents) {
                                    showClassList();
                                } else {
                                    setEnabled(false);
                                    requireActivity()
                                            .onBackPressed();
                                }
                            }
                        }
                );
    }

    // =========================================================
    // LOAD TEACHER
    // =========================================================

    private void loadTeacher() {

        if (auth.getCurrentUser() == null) {

            showError(
                    "Teacher login session not found."
            );

            return;
        }

        String id = requireActivity()
                .getIntent()
                .getStringExtra("staffId");

        if (id == null) {
            id = "";
        }

        staffId = id.trim();

        if (staffId.isEmpty()) {

            showError(
                    "Teacher ID not found. Please login again."
            );

            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        showError(
                                "Teacher profile not found."
                        );

                        return;
                    }

                    schoolId = safe(
                            document.getString("schoolId")
                    );

                    if (schoolId.isEmpty()) {

                        showError(
                                "School ID not found."
                        );

                        return;
                    }

                    loadAllClasses();

                })
                .addOnFailureListener(e ->
                        showError(
                                "Unable to load teacher profile."
                        )
                );
    }

    // =========================================================
    // LOAD ALL UNIQUE CLASSES
    // =========================================================

    private void loadAllClasses() {

        if (schoolId.isEmpty()) {
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

                        showError(
                                "Unable to load classes."
                        );

                        return;
                    }

                    classList.clear();
                    classStudentCount.clear();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String status = safe(
                                document.getString("status")
                        );

                        if ("Deleted".equalsIgnoreCase(status)) {
                            continue;
                        }

                        String className = safe(
                                document.getString("className")
                        );

                        if (className.isEmpty()) {
                            continue;
                        }

                        className = normalizeDisplayClass(
                                className
                        );

                        if (!classList.contains(className)) {
                            classList.add(className);
                        }

                        int count = classStudentCount.containsKey(
                                className
                        )
                                ? classStudentCount.get(className)
                                : 0;

                        classStudentCount.put(
                                className,
                                count + 1
                        );
                    }

                    sortClasses();

                    classAdapter.notifyDataSetChanged();

                    if (classList.isEmpty()) {

                        tvEmpty.setText(
                                "No classes found."
                        );

                        tvEmpty.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        tvEmpty.setVisibility(
                                View.GONE
                        );
                    }
                });
    }

    // =========================================================
    // SORT CLASSES
    // =========================================================

    private void sortClasses() {

        Collections.sort(
                classList,
                new Comparator<String>() {

                    @Override
                    public int compare(
                            String a,
                            String b) {

                        int numberA = extractClassNumber(a);
                        int numberB = extractClassNumber(b);

                        if (numberA != -1 &&
                                numberB != -1) {

                            return Integer.compare(
                                    numberA,
                                    numberB
                            );
                        }

                        return a.compareToIgnoreCase(b);
                    }
                }
        );
    }

    // =========================================================
    // OPEN SELECTED CLASS
    // =========================================================

    private void openClassStudents(
            String className) {

        selectedClass = className;

        showingStudents = true;

        tvTitle.setText(
                "Students - " + className
        );

        btnBack.setVisibility(
                View.VISIBLE
        );

        recyclerClasses.setVisibility(
                View.GONE
        );

        tvEmpty.setVisibility(
                View.GONE
        );

        recyclerStudents.setVisibility(
                View.VISIBLE
        );

        etStudentSearch.setVisibility(
                View.VISIBLE
        );

        etStudentSearch.setText("");

        loadStudentsForClass(
                className
        );
    }

    // =========================================================
    // LOAD STUDENTS OF SELECTED CLASS
    // =========================================================

    private void loadStudentsForClass(
            String className) {

        allStudents.clear();
        filteredStudents.clear();

        studentAdapter.notifyDataSetChanged();

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    allStudents.clear();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        StudentModel student =
                                document.toObject(
                                        StudentModel.class
                                );

                        if (student == null) {
                            continue;
                        }

                        String status = safe(
                                student.getStatus()
                        );

                        if ("Deleted".equalsIgnoreCase(status)) {
                            continue;
                        }

                        String studentClass = safe(
                                student.getClassName()
                        );

                        if (!classMatches(
                                className,
                                studentClass
                        )) {
                            continue;
                        }

                        if (safe(
                                student.getStudentId()
                        ).isEmpty()) {

                            student.setStudentId(
                                    document.getId()
                            );
                        }

                        allStudents.add(student);
                    }

                    sortStudents();

                    filterStudents(
                            etStudentSearch
                                    .getText()
                                    .toString()
                    );

                })
                .addOnFailureListener(e -> {

                    showError(
                            "Unable to load students: "
                                    + e.getMessage()
                    );
                });
    }

    // =========================================================
    // FILTER STUDENTS
    // =========================================================

    private void filterStudents(
            String query) {

        String q = query == null
                ? ""
                : query.trim()
                .toLowerCase(Locale.ROOT);

        filteredStudents.clear();

        for (StudentModel student :
                allStudents) {

            String name = fullName(student)
                    .toLowerCase(Locale.ROOT);

            String roll = safe(
                    student.getRollNumber()
            ).toLowerCase(Locale.ROOT);

            String admission = safe(
                    student.getAdmissionNumber()
            ).toLowerCase(Locale.ROOT);

            String phone = safe(
                    student.getStudentPhone()
            ).toLowerCase(Locale.ROOT);

            if (q.isEmpty()
                    || name.contains(q)
                    || roll.contains(q)
                    || admission.contains(q)
                    || phone.contains(q)) {

                filteredStudents.add(student);
            }
        }

        studentAdapter.notifyDataSetChanged();

        if (filteredStudents.isEmpty()) {

            tvEmpty.setText(
                    q.isEmpty()
                            ? "No students found in this class."
                            : "No student matches your search."
            );

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
    // SHOW CLASS LIST AGAIN
    // =========================================================

    private void showClassList() {

        showingStudents = false;
        selectedClass = "";

        tvTitle.setText(
                "All Classes"
        );

        btnBack.setVisibility(
                View.GONE
        );

        etStudentSearch.setText("");

        etStudentSearch.setVisibility(
                View.GONE
        );

        recyclerStudents.setVisibility(
                View.GONE
        );

        recyclerClasses.setVisibility(
                View.VISIBLE
        );

        tvEmpty.setVisibility(
                classList.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    // =========================================================
    // STUDENT ACTIONS
    // SAME AS MY CLASS
    // =========================================================

    private void openStudentDetails(
            StudentModel student) {

        Intent intent = new Intent(
                requireActivity(),
                StudentDetailsActivity.class
        );

        intent.putExtra(
                "studentId",
                student.getStudentId()
        );

        startActivity(intent);
    }

    private void openStudentAttendance(
            StudentModel student) {

        Intent intent = new Intent(
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

    private void openStudentMarks(
            StudentModel student) {

        Intent intent = new Intent(
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

    private void openStudentProgress(
            StudentModel student) {

        Intent intent = new Intent(
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

    private void openReportCard(
            StudentModel student) {

        Intent intent = new Intent(
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

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean classMatches(
            String selected,
            String studentClass) {

        return normalizeClass(selected)
                .equals(
                        normalizeClass(studentClass)
                );
    }

    private String normalizeClass(
            String value) {

        return safe(value)
                .toLowerCase(Locale.ROOT)
                .replace("class", "")
                .replace("standard", "")
                .replace("std", "")
                .replace("-", "")
                .replace(" ", "")
                .trim();
    }

    private String normalizeDisplayClass(
            String value) {

        return safe(value)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int extractClassNumber(
            String value) {

        String normalized = normalizeClass(value);

        StringBuilder number = new StringBuilder();

        for (int i = 0; i < normalized.length(); i++) {

            char c = normalized.charAt(i);

            if (Character.isDigit(c)) {
                number.append(c);
            }
        }

        if (number.length() == 0) {
            return -1;
        }

        try {
            return Integer.parseInt(
                    number.toString()
            );
        } catch (Exception e) {
            return -1;
        }
    }

    private void sortStudents() {

        Collections.sort(
                allStudents,
                (a, b) -> {

                    int rollA = parseRoll(
                            a.getRollNumber()
                    );

                    int rollB = parseRoll(
                            b.getRollNumber()
                    );

                    if (rollA != rollB) {
                        return Integer.compare(
                                rollA,
                                rollB
                        );
                    }

                    return fullName(a)
                            .compareToIgnoreCase(
                                    fullName(b)
                            );
                }
        );
    }

    private int parseRoll(
            String value) {

        try {

            String clean = safe(value)
                    .replaceAll(
                            "[^0-9]",
                            ""
                    );

            if (clean.isEmpty()) {
                return Integer.MAX_VALUE;
            }

            return Integer.parseInt(clean);

        } catch (Exception e) {

            return Integer.MAX_VALUE;
        }
    }

    private String fullName(
            StudentModel student) {

        String name =
                (
                        safe(student.getFirstName())
                                + " "
                                + safe(student.getMiddleName())
                                + " "
                                + safe(student.getLastName())
                ).trim();

        return name.isEmpty()
                ? "Student"
                : name.replaceAll(
                "\\s+",
                " "
        );
    }

    private String safe(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private void showError(
            String message) {

        if (isAdded()) {

            Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public void onDestroyView() {

        if (studentsListener != null) {
            studentsListener.remove();
            studentsListener = null;
        }

        super.onDestroyView();
    }
}