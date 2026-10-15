package com.example.myschool;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllStudentAttendanceActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageButton btnRefresh;

    private Button btnSelectClass;
    private Button btnSelectDivision;

    private TextView tvSelectedFilter;
    private TextView tvStudentCount;
    private TextView tvEmpty;

    private ProgressBar progressBar;
    private RecyclerView recyclerStudents;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private AllStudentAttendanceAdapter adapter;

    private final List<AllStudentAttendanceAdapter.AttendanceStudent> studentList =
            new ArrayList<>();

    private final List<String> classKeys = new ArrayList<>();
    private final Map<String, String> classDisplayNames = new LinkedHashMap<>();

    private final List<String> divisions = new ArrayList<>();

    private String schoolId = "";

    private String selectedClassKey = "ALL";
    private String selectedClassDisplay = "All Classes";

    private String selectedDivision = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_student_attendance);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        setupListeners();

        getSchoolId();
    }

    private void initViews() {

        btnBack = findViewById(R.id.btnAllStudentAttendanceBack);
        btnRefresh = findViewById(R.id.btnAllStudentAttendanceRefresh);

        btnSelectClass = findViewById(R.id.btnSelectAttendanceClass);
        btnSelectDivision = findViewById(R.id.btnSelectAttendanceDivision);

        tvSelectedFilter = findViewById(R.id.tvAttendanceSelectedFilter);
        tvStudentCount = findViewById(R.id.tvAllStudentAttendanceCount);
        tvEmpty = findViewById(R.id.tvAllStudentAttendanceEmpty);

        progressBar = findViewById(R.id.progressAllStudentAttendance);
        recyclerStudents = findViewById(R.id.recyclerAllStudentAttendance);
    }

    private void setupRecyclerView() {

        adapter = new AllStudentAttendanceAdapter(
                this,
                studentList,
                student -> {

                    /*
                     * Existing single-student attendance history screen.
                     * Nothing is changed in StudentAttendanceActivity.
                     */

                    android.content.Intent intent =
                            new android.content.Intent(
                                    AllStudentAttendanceActivity.this,
                                    StudentAttendanceActivity.class
                            );

                    intent.putExtra("studentId", student.studentId);
                    intent.putExtra("schoolId", schoolId);

                    startActivity(intent);
                }
        );

        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStudents.setAdapter(adapter);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnRefresh.setOnClickListener(v -> {

            if (TextUtils.isEmpty(schoolId)) {
                getSchoolId();
            } else {
                loadAllStudents();
            }
        });

        btnSelectClass.setOnClickListener(v -> showClassDialog());

        btnSelectDivision.setOnClickListener(v -> showDivisionDialog());
    }

    private void getSchoolId() {

        String intentSchoolId = getIntent().getStringExtra("schoolId");

        if (!TextUtils.isEmpty(intentSchoolId)) {

            schoolId = intentSchoolId;

            loadAllStudents();
            return;
        }

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        /*
         * Fallback.
         * In the admin side of the app the schoolId is normally available
         * through the Intent. If not, current Firebase UID is used.
         */

        schoolId = auth.getCurrentUser().getUid();

        loadAllStudents();
    }

    private void loadAllStudents() {

        if (TextUtils.isEmpty(schoolId)) {
            return;
        }

        showLoading(true);

        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    studentList.clear();

                    classKeys.clear();
                    classDisplayNames.clear();

                    divisions.clear();

                    /*
                     * Temporary map:
                     * studentId -> student document
                     */
                    List<DocumentSnapshot> studentDocuments =
                            new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {

                        String status = getString(document, "status");

                        if ("Deleted".equalsIgnoreCase(status)) {
                            continue;
                        }

                        String studentId =
                                getString(document, "studentId");

                        if (TextUtils.isEmpty(studentId)) {
                            studentId = document.getId();
                        }

                        String className =
                                getString(document, "className");

                        String division =
                                getString(document, "division");

                        if (TextUtils.isEmpty(studentId)) {
                            continue;
                        }

                        studentDocuments.add(document);

                        addClass(
                                className
                        );

                        if (!TextUtils.isEmpty(division)) {
                            if (!containsIgnoreCase(divisions, division)) {
                                divisions.add(division);
                            }
                        }
                    }

                    Collections.sort(
                            divisions,
                            String.CASE_INSENSITIVE_ORDER
                    );

                    /*
                     * Load attendance records once.
                     * Then calculate each student's summary locally.
                     */
                    loadAttendanceAndBuildList(studentDocuments);

                })
                .addOnFailureListener(e -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "Unable to load students: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void loadAttendanceAndBuildList(
            List<DocumentSnapshot> studentDocuments
    ) {

        db.collection("studentAttendance")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(attendanceSnapshot -> {

                    /*
                     * studentId -> attendance records
                     */
                    Map<String, List<AttendanceRecord>> attendanceMap =
                            new HashMap<>();

                    for (DocumentSnapshot attendanceDoc :
                            attendanceSnapshot.getDocuments()) {

                        String studentId =
                                getString(attendanceDoc, "studentId");

                        if (TextUtils.isEmpty(studentId)) {
                            continue;
                        }

                        String status =
                                getString(attendanceDoc, "status");

                        String date =
                                getString(attendanceDoc, "date");

                        AttendanceRecord record =
                                new AttendanceRecord(
                                        status,
                                        date
                                );

                        if (!attendanceMap.containsKey(studentId)) {

                            attendanceMap.put(
                                    studentId,
                                    new ArrayList<>()
                            );
                        }

                        attendanceMap
                                .get(studentId)
                                .add(record);
                    }

                    /*
                     * Create student summaries.
                     */
                    for (DocumentSnapshot studentDoc :
                            studentDocuments) {

                        String studentId =
                                getString(studentDoc, "studentId");

                        if (TextUtils.isEmpty(studentId)) {
                            studentId = studentDoc.getId();
                        }

                        String className =
                                getString(studentDoc, "className");

                        String division =
                                getString(studentDoc, "division");

                        /*
                         * Apply selected class filter.
                         */
                        if (!"ALL".equals(selectedClassKey)) {

                            String studentClassKey =
                                    normalizeClass(className);

                            if (!selectedClassKey.equals(
                                    studentClassKey
                            )) {
                                continue;
                            }
                        }

                        /*
                         * Apply selected division filter.
                         */
                        if (!"ALL".equals(selectedDivision)) {

                            if (!selectedDivision.equalsIgnoreCase(
                                    division
                            )) {
                                continue;
                            }
                        }

                        String firstName =
                                getString(studentDoc, "firstName");

                        String middleName =
                                getString(studentDoc, "middleName");

                        String lastName =
                                getString(studentDoc, "lastName");

                        String fullName =
                                buildFullName(
                                        firstName,
                                        middleName,
                                        lastName
                                );

                        if (TextUtils.isEmpty(fullName)) {

                            fullName =
                                    getString(
                                            studentDoc,
                                            "studentName"
                                    );
                        }

                        if (TextUtils.isEmpty(fullName)) {
                            fullName = "Student";
                        }

                        String rollNumber =
                                getString(
                                        studentDoc,
                                        "rollNumber"
                                );

                        int present = 0;
                        int absent = 0;
                        int holiday = 0;

                        List<AttendanceRecord> records =
                                attendanceMap.get(studentId);

                        if (records != null) {

                            for (AttendanceRecord record : records) {

                                if ("Present".equalsIgnoreCase(
                                        record.status
                                )) {

                                    present++;

                                } else if ("Absent".equalsIgnoreCase(
                                        record.status
                                )) {

                                    absent++;

                                } else if ("Holiday".equalsIgnoreCase(
                                        record.status
                                )) {

                                    holiday++;
                                }
                            }
                        }

                        int workingDays = present + absent;

                        double percentage = 0;

                        if (workingDays > 0) {

                            percentage =
                                    (present * 100.0) / workingDays;
                        }

                        AllStudentAttendanceAdapter.AttendanceStudent
                                student =
                                new AllStudentAttendanceAdapter.AttendanceStudent();

                        student.studentId = studentId;
                        student.studentName = fullName;
                        student.className = className;
                        student.division = division;
                        student.rollNumber = rollNumber;

                        student.present = present;
                        student.absent = absent;
                        student.holiday = holiday;
                        student.percentage = percentage;

                        studentList.add(student);
                    }

                    sortStudents();

                    adapter.notifyDataSetChanged();

                    tvStudentCount.setText(
                            studentList.size() + " Students"
                    );

                    updateFilterText();

                    tvEmpty.setVisibility(
                            studentList.isEmpty()
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    recyclerStudents.setVisibility(
                            studentList.isEmpty()
                                    ? View.GONE
                                    : View.VISIBLE
                    );

                    showLoading(false);

                })
                .addOnFailureListener(e -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "Unable to load attendance: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showClassDialog() {

        final List<String> displayList =
                new ArrayList<>();

        displayList.add("All Classes");

        for (String key : classKeys) {

            String display =
                    classDisplayNames.get(key);

            if (!TextUtils.isEmpty(display)) {
                displayList.add(display);
            }
        }

        String[] items =
                displayList.toArray(
                        new String[0]
                );

        int checkedItem = 0;

        if (!"ALL".equals(selectedClassKey)) {

            for (int i = 1; i < items.length; i++) {

                String key =
                        normalizeClass(items[i]);

                if (key.equals(selectedClassKey)) {

                    checkedItem = i;
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Class")
                .setSingleChoiceItems(
                        items,
                        checkedItem,
                        (dialog, which) -> {

                            if (which == 0) {

                                selectedClassKey = "ALL";
                                selectedClassDisplay = "All Classes";

                            } else {

                                selectedClassDisplay =
                                        items[which];

                                selectedClassKey =
                                        normalizeClass(
                                                items[which]
                                        );
                            }

                            /*
                             * Reset division when class changes.
                             */
                            selectedDivision = "ALL";

                            btnSelectClass.setText(
                                    selectedClassDisplay
                            );

                            btnSelectDivision.setText(
                                    "All Divisions"
                            );

                            dialog.dismiss();

                            loadAllStudents();
                        }
                )
                .show();
    }

    private void showDivisionDialog() {

        /*
         * Build divisions according to selected class.
         */
        if (!"ALL".equals(selectedClassKey)) {

            loadDivisionsForSelectedClass();
            return;
        }

        showDivisionSelection(
                new ArrayList<>(divisions)
        );
    }

    private void loadDivisionsForSelectedClass() {

        showLoading(true);

        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    Set<String> divisionSet =
                            new HashSet<>();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        String status =
                                getString(doc, "status");

                        if ("Deleted".equalsIgnoreCase(status)) {
                            continue;
                        }

                        String className =
                                getString(doc, "className");

                        if (!selectedClassKey.equals(
                                normalizeClass(className)
                        )) {
                            continue;
                        }

                        String division =
                                getString(doc, "division");

                        if (!TextUtils.isEmpty(division)) {
                            divisionSet.add(division);
                        }
                    }

                    List<String> list =
                            new ArrayList<>(divisionSet);

                    Collections.sort(
                            list,
                            String.CASE_INSENSITIVE_ORDER
                    );

                    showLoading(false);

                    showDivisionSelection(list);

                })
                .addOnFailureListener(e -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            "Unable to load divisions",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showDivisionSelection(
            List<String> divisionList
    ) {

        final List<String> items =
                new ArrayList<>();

        items.add("All Divisions");
        items.addAll(divisionList);

        String[] array =
                items.toArray(
                        new String[0]
                );

        int checkedItem = 0;

        if (!"ALL".equals(selectedDivision)) {

            for (int i = 1; i < array.length; i++) {

                if (array[i].equalsIgnoreCase(
                        selectedDivision
                )) {

                    checkedItem = i;
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Division")
                .setSingleChoiceItems(
                        array,
                        checkedItem,
                        (dialog, which) -> {

                            if (which == 0) {

                                selectedDivision = "ALL";

                                btnSelectDivision.setText(
                                        "All Divisions"
                                );

                            } else {

                                selectedDivision =
                                        array[which];

                                btnSelectDivision.setText(
                                        selectedDivision
                                );
                            }

                            dialog.dismiss();

                            loadAllStudents();
                        }
                )
                .show();
    }

    private void addClass(String className) {

        if (TextUtils.isEmpty(className)) {
            return;
        }

        String key =
                normalizeClass(className);

        if (TextUtils.isEmpty(key)) {
            return;
        }

        if (!classKeys.contains(key)) {

            classKeys.add(key);

            classDisplayNames.put(
                    key,
                    className.trim()
            );
        }

        /*
         * Sort classes naturally.
         */
        Collections.sort(
                classKeys,
                (a, b) -> {

                    try {

                        int x =
                                Integer.parseInt(
                                        a.replaceAll(
                                                "[^0-9]",
                                                ""
                                        )
                                );

                        int y =
                                Integer.parseInt(
                                        b.replaceAll(
                                                "[^0-9]",
                                                ""
                                        )
                                );

                        return Integer.compare(x, y);

                    } catch (Exception e) {

                        return a.compareToIgnoreCase(b);
                    }
                }
        );
    }

    private void sortStudents() {

        Collections.sort(
                studentList,
                (a, b) -> {

                    /*
                     * First sort by class.
                     */
                    int classCompare =
                            normalizeClass(
                                    a.className
                            ).compareToIgnoreCase(
                                    normalizeClass(
                                            b.className
                                    )
                            );

                    if (classCompare != 0) {
                        return classCompare;
                    }

                    /*
                     * Then division.
                     */
                    int divisionCompare =
                            a.division.compareToIgnoreCase(
                                    b.division
                            );

                    if (divisionCompare != 0) {
                        return divisionCompare;
                    }

                    /*
                     * Then roll number.
                     */
                    try {

                        int r1 =
                                Integer.parseInt(
                                        a.rollNumber
                                                .replaceAll(
                                                        "[^0-9]",
                                                        ""
                                                )
                                );

                        int r2 =
                                Integer.parseInt(
                                        b.rollNumber
                                                .replaceAll(
                                                        "[^0-9]",
                                                        ""
                                                )
                                );

                        return Integer.compare(r1, r2);

                    } catch (Exception e) {

                        return a.studentName
                                .compareToIgnoreCase(
                                        b.studentName
                                );
                    }
                }
        );
    }

    private void updateFilterText() {

        String text =
                selectedClassDisplay
                        + " • "
                        + (
                        "ALL".equals(selectedDivision)
                                ? "All Divisions"
                                : selectedDivision
                );

        tvSelectedFilter.setText(text);
    }

    private void showLoading(boolean show) {

        progressBar.setVisibility(
                show ? View.VISIBLE : View.GONE
        );
    }

    private boolean containsIgnoreCase(
            List<String> list,
            String value
    ) {

        for (String item : list) {

            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    private String getString(
            DocumentSnapshot document,
            String field
    ) {

        String value =
                document.getString(field);

        return value == null ? "" : value.trim();
    }

    private String buildFullName(
            String first,
            String middle,
            String last
    ) {

        StringBuilder builder =
                new StringBuilder();

        if (!TextUtils.isEmpty(first)) {
            builder.append(first);
        }

        if (!TextUtils.isEmpty(middle)) {

            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(middle);
        }

        if (!TextUtils.isEmpty(last)) {

            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(last);
        }

        return builder.toString().trim();
    }

    private String normalizeClass(
            String className
    ) {

        if (className == null) {
            return "";
        }

        String value =
                className.trim()
                        .toLowerCase()
                        .replace("class", "")
                        .trim();

        value =
                value.replaceAll(
                        "\\s+",
                        ""
                );

        return value;
    }

    private static class AttendanceRecord {

        String status;
        String date;

        AttendanceRecord(
                String status,
                String date
        ) {

            this.status = status;
            this.date = date;
        }
    }
}