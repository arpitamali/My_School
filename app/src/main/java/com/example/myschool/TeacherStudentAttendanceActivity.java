package com.example.myschool;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TeacherStudentAttendanceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText etDate;
    private TextView tvClass;
    private TextView tvSummary;
    private LinearLayout attendanceContainer;
    private MaterialButton btnAllPresent;
    private MaterialButton btnHoliday;
    private MaterialButton btnSave;

    private String staffId = "";
    private String schoolId = "";

    private String className = "";
    private String division = "";

    private String selectedDate;

    /*
     * Modes:
     *
     * my_class
     *      -> Teacher's assigned class/division direct
     *
     * quick_action
     *      -> Select Class -> Select Division
     */
    private String attendanceMode = "my_class";

    private final List<StudentModel> students = new ArrayList<>();
    private final Map<String, String> statusMap = new HashMap<>();

    private ListenerRegistration studentsListener;
    private ListenerRegistration attendanceListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_teacher_student_attendance);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();

        selectedDate = todayKey();
        etDate.setText(displayDate(selectedDate));

        /*
         * Get mode from previous screen.
         *
         * My Class:
         * attendanceMode = "my_class"
         *
         * Quick Actions:
         * attendanceMode = "quick_action"
         */
        attendanceMode = safe(
                getIntent().getStringExtra("attendanceMode"),
                "my_class"
        );

        setupClicks();

        loadTeacherProfile();
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        etDate = findViewById(R.id.etStudentAttendanceDate);

        tvClass = findViewById(R.id.tvStudentAttendanceClass);

        tvSummary = findViewById(R.id.tvStudentAttendanceSummary);

        attendanceContainer =
                findViewById(R.id.studentAttendanceContainer);

        btnAllPresent =
                findViewById(R.id.btnAllStudentsPresent);

        btnHoliday =
                findViewById(R.id.btnClassHoliday);

        btnSave =
                findViewById(R.id.btnSaveStudentAttendance);
    }

    // =========================================================
    // CLICK LISTENERS
    // =========================================================

    private void setupClicks() {

        findViewById(R.id.btnStudentAttendanceBack)
                .setOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> showDatePicker());

        btnAllPresent.setOnClickListener(v -> markAll("Present"));

        btnHoliday.setOnClickListener(v -> confirmHoliday());

        btnSave.setOnClickListener(v -> saveAttendance());
    }

    // =========================================================
    // LOAD TEACHER PROFILE
    // =========================================================

    private void loadTeacherProfile() {

        if (auth.getCurrentUser() == null) {
            finishWithMessage("Please login again.");
            return;
        }

        staffId = safe(getIntent().getStringExtra("staffId"));

        if (staffId.isEmpty()) {

            /*
             * Fallback:
             * Firebase logged-in user UID
             */
            staffId = auth.getCurrentUser().getUid();
        }

        if (staffId.isEmpty()) {
            finishWithMessage("Teacher profile ID is missing.");
            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(staff -> {

                    if (!staff.exists()) {

                        finishWithMessage(
                                "Teacher profile not found."
                        );

                        return;
                    }

                    schoolId = safe(
                            staff.getString("schoolId")
                    );

                    String assignedClass = safe(
                            staff.getString("assignedClass")
                    );

                    String assignedDivision = safe(
                            staff.getString("assignedDivision")
                    );

                    if (schoolId.isEmpty()) {

                        finishWithMessage(
                                "School ID not found in teacher profile."
                        );

                        return;
                    }

                    /*
                     * =========================================
                     * MY CLASS MODE
                     * =========================================
                     *
                     * Teacher opens attendance from My Class.
                     *
                     * Directly use assigned class/division.
                     */
                    if ("my_class".equalsIgnoreCase(attendanceMode)) {

                        className = assignedClass;
                        division = assignedDivision;

                        if (className.isEmpty()) {

                            finishWithMessage(
                                    "Please ask admin to assign your class."
                            );

                            return;
                        }

                        tvClass.setText(
                                className +
                                        (
                                                division.isEmpty()
                                                        ? ""
                                                        : " - " + division
                                        )
                        );

                        loadStudents();

                    }

                    /*
                     * =========================================
                     * QUICK ACTION MODE
                     * =========================================
                     *
                     * Teacher opens Attendance from Quick Actions.
                     *
                     * First:
                     * Select Class
                     *
                     * Then:
                     * Select Division
                     */
                    else {

                        tvClass.setText(
                                "Select Class & Division"
                        );

                        showClassSelection();
                    }

                })
                .addOnFailureListener(e ->
                        finishWithMessage(
                                "Unable to load teacher profile."
                        )
                );
    }

    // =========================================================
    // QUICK ACTION - SELECT CLASS
    // =========================================================

    private void showClassSelection() {

        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    Set<String> classSet = new HashSet<>();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        StudentModel student =
                                doc.toObject(StudentModel.class);

                        if (student == null) {
                            continue;
                        }

                        if ("Deleted".equalsIgnoreCase(
                                safe(student.getStatus())
                        )) {
                            continue;
                        }

                        String studentClass =
                                safe(student.getClassName());

                        if (!studentClass.isEmpty()) {
                            classSet.add(studentClass);
                        }
                    }

                    if (classSet.isEmpty()) {

                        toast(
                                "No classes found in your school."
                        );

                        return;
                    }

                    List<String> classList =
                            new ArrayList<>(classSet);

                    Collections.sort(
                            classList,
                            (a, b) -> {

                                int numberA =
                                        extractClassNumber(a);

                                int numberB =
                                        extractClassNumber(b);

                                if (numberA != numberB) {
                                    return Integer.compare(
                                            numberA,
                                            numberB
                                    );
                                }

                                return a.compareToIgnoreCase(b);
                            }
                    );

                    showClassDialog(classList);

                })
                .addOnFailureListener(e ->
                        toast(
                                "Unable to load classes: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================================================
    // CLASS DIALOG
    // =========================================================

    private void showClassDialog(List<String> classList) {

        String[] classes =
                classList.toArray(new String[0]);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Select Class")
                        .setSingleChoiceItems(
                                classes,
                                -1,
                                (d, which) -> {

                                    className =
                                            classes[which];

                                    d.dismiss();

                                    showDivisionSelection();
                                }
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();

        dialog.show();
    }

    // =========================================================
    // QUICK ACTION - SELECT DIVISION
    // =========================================================

    private void showDivisionSelection() {

        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    Set<String> divisionSet =
                            new HashSet<>();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        StudentModel student =
                                doc.toObject(StudentModel.class);

                        if (student == null) {
                            continue;
                        }

                        if ("Deleted".equalsIgnoreCase(
                                safe(student.getStatus())
                        )) {
                            continue;
                        }

                        /*
                         * Only selected class.
                         */
                        if (!normalizeClass(className)
                                .equals(
                                        normalizeClass(
                                                student.getClassName()
                                        )
                                )) {

                            continue;
                        }

                        String studentDivision =
                                safe(student.getDivision());

                        if (!studentDivision.isEmpty()) {

                            divisionSet.add(
                                    studentDivision
                            );
                        }
                    }

                    /*
                     * If no division is stored,
                     * allow All Divisions.
                     */
                    if (divisionSet.isEmpty()) {

                        divisionSet.add(
                                "All Divisions"
                        );
                    }

                    List<String> divisionList =
                            new ArrayList<>(divisionSet);

                    Collections.sort(
                            divisionList,
                            String.CASE_INSENSITIVE_ORDER
                    );

                    showDivisionDialog(divisionList);

                })
                .addOnFailureListener(e ->
                        toast(
                                "Unable to load divisions: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================================================
    // DIVISION DIALOG
    // =========================================================

    private void showDivisionDialog(
            List<String> divisionList
    ) {

        String[] divisions =
                divisionList.toArray(new String[0]);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Select Division"
                        )
                        .setSingleChoiceItems(
                                divisions,
                                -1,
                                (d, which) -> {

                                    division =
                                            divisions[which];

                                    d.dismiss();

                                    tvClass.setText(
                                            className
                                                    + (
                                                    division
                                                            .isEmpty()
                                                            ? ""
                                                            : " - "
                                                            + division
                                            )
                                    );

                                    loadStudents();
                                }
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();

        dialog.show();
    }

    // =========================================================
    // LOAD STUDENTS
    // =========================================================

    private void loadStudents() {

        if (className.isEmpty()) {
            toast("Please select class.");
            return;
        }

        if (studentsListener != null) {
            studentsListener.remove();
        }

        studentsListener =
                db.collection("students")
                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null
                                            || snapshot == null) {

                                        toast(
                                                "Unable to load students: "
                                                        + (
                                                        error == null
                                                                ? "Unknown error"
                                                                : error.getMessage()
                                                )
                                        );

                                        return;
                                    }

                                    students.clear();

                                    for (DocumentSnapshot doc :
                                            snapshot.getDocuments()) {

                                        StudentModel student =
                                                doc.toObject(
                                                        StudentModel.class
                                                );

                                        if (student == null) {
                                            continue;
                                        }

                                        /*
                                         * Ignore deleted students.
                                         */
                                        if ("Deleted"
                                                .equalsIgnoreCase(
                                                        safe(
                                                                student.getStatus()
                                                        )
                                                )) {

                                            continue;
                                        }

                                        /*
                                         * Class filter.
                                         */
                                        if (!normalizeClass(
                                                className
                                        ).equals(
                                                normalizeClass(
                                                        student.getClassName()
                                                )
                                        )) {

                                            continue;
                                        }

                                        /*
                                         * Division filter.
                                         */
                                        if (!divisionMatches(
                                                division,
                                                student.getDivision()
                                        )) {

                                            continue;
                                        }

                                        /*
                                         * If StudentModel does not have
                                         * studentId, use Firestore ID.
                                         */
                                        if (safe(
                                                student.getStudentId()
                                        ).isEmpty()) {

                                            student.setStudentId(
                                                    doc.getId()
                                            );
                                        }

                                        students.add(student);
                                    }

                                    /*
                                     * Sort by roll number.
                                     */
                                    students.sort((a, b) -> {

                                        int rollA =
                                                parseRoll(
                                                        a.getRollNumber()
                                                );

                                        int rollB =
                                                parseRoll(
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
                                    });

                                    loadExistingAttendanceThenRender();
                                }
                        );
    }

    // =========================================================
    // REAL-TIME ATTENDANCE
    // =========================================================

    private void loadExistingAttendanceThenRender() {

        if (attendanceListener != null) {
            attendanceListener.remove();
        }

        attendanceListener =
                db.collection("studentAttendance")
                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )
                        .whereEqualTo(
                                "date",
                                selectedDate
                        )
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null
                                            || snapshot == null) {

                                        renderStudents();

                                        return;
                                    }

                                    statusMap.clear();

                                    for (DocumentSnapshot doc :
                                            snapshot.getDocuments()) {

                                        String studentId =
                                                safe(
                                                        doc.getString(
                                                                "studentId"
                                                        )
                                                );

                                        String status =
                                                safe(
                                                        doc.getString(
                                                                "status"
                                                        )
                                                );

                                        if (studentId.isEmpty()
                                                || status.isEmpty()) {

                                            continue;
                                        }

                                        /*
                                         * Old Half Day records:
                                         * treat them as Absent.
                                         */
                                        if (status.equalsIgnoreCase(
                                                "Half Day"
                                        )) {

                                            status = "Absent";
                                        }

                                        if (
                                                status.equalsIgnoreCase(
                                                        "Present"
                                                )
                                                        ||
                                                        status.equalsIgnoreCase(
                                                                "Absent"
                                                        )
                                                        ||
                                                        status.equalsIgnoreCase(
                                                                "Holiday"
                                                        )
                                        ) {

                                            statusMap.put(
                                                    studentId,
                                                    status
                                            );
                                        }
                                    }

                                    renderStudents();
                                }
                        );
    }

    // =========================================================
    // RENDER STUDENTS
    // =========================================================

    private void renderStudents() {

        attendanceContainer.removeAllViews();

        if (students.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "No students found in this class."
            );

            empty.setGravity(
                    Gravity.CENTER
            );

            empty.setTextSize(15);

            empty.setTextColor(
                    0xFF77727F
            );

            empty.setPadding(
                    dp(20),
                    dp(50),
                    dp(20),
                    dp(50)
            );

            attendanceContainer.addView(
                    empty
            );

            updateSummary();

            return;
        }

        for (StudentModel student :
                students) {

            attendanceContainer.addView(
                    createStudentRow(student)
            );
        }

        updateSummary();
    }

    // =========================================================
    // CREATE STUDENT ROW
    // =========================================================

    private View createStudentRow(
            StudentModel student
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(10)
        );

        card.setLayoutParams(
                cardParams
        );

        card.setRadius(
                dp(16)
        );

        card.setCardElevation(
                dp(1)
        );

        card.setStrokeWidth(
                dp(1)
        );

        card.setStrokeColor(
                0xFFE6E2F0
        );

        card.setCardBackgroundColor(
                0xFFFFFFFF
        );

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        /*
         * Roll Number
         */
        TextView roll =
                new TextView(this);

        roll.setText(
                "#" + safe(
                        student.getRollNumber(),
                        "-"
                )
        );

        roll.setTextSize(12);

        roll.setTextColor(
                0xFF77727F
        );

        roll.setGravity(
                Gravity.CENTER
        );

        roll.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        row.addView(
                roll,
                new LinearLayout.LayoutParams(
                        dp(50),
                        dp(44)
                )
        );

        /*
         * Student Name
         */
        TextView name =
                new TextView(this);

        name.setText(
                fullName(student)
        );

        name.setTextSize(
                14.5f
        );

        name.setTextColor(
                0xFF25252B
        );

        name.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        name.setGravity(
                Gravity.CENTER_VERTICAL
        );

        name.setSingleLine(true);

        name.setEllipsize(
                TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1f
                );

        nameParams.setMargins(
                dp(8),
                0,
                dp(12),
                0
        );

        row.addView(
                name,
                nameParams
        );

        /*
         * Present
         */
        MaterialButton presentButton =
                createCompactAttendanceButton(
                        "P",
                        0xFF1B8A4A
                );

        /*
         * Absent
         */
        MaterialButton absentButton =
                createCompactAttendanceButton(
                        "A",
                        0xFFD83C3C
                );

        String currentStatus =
                safe(
                        statusMap.get(
                                student.getStudentId()
                        )
                );

        styleCompactAttendanceButton(
                presentButton,
                "Present".equalsIgnoreCase(
                        currentStatus
                ),
                0xFF1B8A4A
        );

        styleCompactAttendanceButton(
                absentButton,
                "Absent".equalsIgnoreCase(
                        currentStatus
                ),
                0xFFD83C3C
        );

        /*
         * Present click
         */
        presentButton.setOnClickListener(v -> {

            statusMap.put(
                    student.getStudentId(),
                    "Present"
            );

            styleCompactAttendanceButton(
                    presentButton,
                    true,
                    0xFF1B8A4A
            );

            styleCompactAttendanceButton(
                    absentButton,
                    false,
                    0xFFD83C3C
            );

            updateSummary();
        });

        /*
         * Absent click
         */
        absentButton.setOnClickListener(v -> {

            statusMap.put(
                    student.getStudentId(),
                    "Absent"
            );

            styleCompactAttendanceButton(
                    presentButton,
                    false,
                    0xFF1B8A4A
            );

            styleCompactAttendanceButton(
                    absentButton,
                    true,
                    0xFFD83C3C
            );

            updateSummary();
        });

        row.addView(
                presentButton
        );

        row.addView(
                absentButton
        );

        card.addView(
                row
        );

        return card;
    }

    // =========================================================
    // ATTENDANCE BUTTON
    // =========================================================

    private MaterialButton createCompactAttendanceButton(
            String text,
            int selectedColor
    ) {

        MaterialButton button =
                new MaterialButton(this);

        button.setText(text);

        button.setAllCaps(false);

        button.setTextSize(12);

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setMinWidth(0);

        button.setMinHeight(0);

        button.setInsetTop(0);

        button.setInsetBottom(0);

        button.setPadding(
                0,
                0,
                0,
                0
        );

        button.setCornerRadius(
                dp(10)
        );

        button.setStrokeWidth(
                dp(1)
        );

        button.setRippleColor(
                ColorStateList.valueOf(
                        0x22000000
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(44),
                        dp(42)
                );

        params.setMargins(
                dp(4),
                0,
                0,
                0
        );

        button.setLayoutParams(
                params
        );

        styleCompactAttendanceButton(
                button,
                false,
                selectedColor
        );

        return button;
    }

    // =========================================================
    // STYLE ATTENDANCE BUTTON
    // =========================================================

    private void styleCompactAttendanceButton(
            MaterialButton button,
            boolean selected,
            int selectedColor
    ) {

        if (selected) {

            button.setBackgroundTintList(
                    ColorStateList.valueOf(
                            selectedColor
                    )
            );

            button.setStrokeColor(
                    ColorStateList.valueOf(
                            selectedColor
                    )
            );

            button.setTextColor(
                    0xFFFFFFFF
            );

        } else {

            button.setBackgroundTintList(
                    ColorStateList.valueOf(
                            0xFFEDEBF2
                    )
            );

            button.setStrokeColor(
                    ColorStateList.valueOf(
                            0xFFD8D4E0
                    )
            );

            button.setTextColor(
                    0xFF55515E
            );
        }

        button.setCornerRadius(
                dp(10)
        );
    }

    // =========================================================
    // MARK ALL
    // =========================================================

    private void markAll(String status) {

        for (StudentModel student :
                students) {

            statusMap.put(
                    student.getStudentId(),
                    status
            );
        }

        renderStudents();
    }

    // =========================================================
    // HOLIDAY
    // =========================================================

    private void confirmHoliday() {

        if (students.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(this)

                .setTitle(
                        "Class Holiday"
                )

                .setMessage(
                        "Mark "
                                + displayDate(selectedDate)
                                + " as Holiday for all students?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Mark Holiday",
                        (d, w) ->
                                markAll("Holiday")
                )

                .show();
    }

    // =========================================================
    // SAVE ATTENDANCE
    // =========================================================

    private void saveAttendance() {

        if (students.isEmpty()) {

            toast(
                    "No students to save."
            );

            return;
        }

        /*
         * Check every student is marked.
         */
        for (StudentModel student :
                students) {

            String status =
                    safe(
                            statusMap.get(
                                    student.getStudentId()
                            )
                    );

            if (status.isEmpty()) {

                toast(
                        "Please mark attendance for every student."
                );

                return;
            }
        }

        btnSave.setEnabled(false);

        btnSave.setText(
                "Saving..."
        );

        WriteBatch batch =
                db.batch();

        for (StudentModel student :
                students) {

            String studentId =
                    student.getStudentId();

            String status =
                    safe(
                            statusMap.get(
                                    studentId
                            )
                    );

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "schoolId",
                    schoolId
            );

            data.put(
                    "studentId",
                    studentId
            );

            data.put(
                    "studentName",
                    fullName(student)
            );

            data.put(
                    "className",
                    student.getClassName()
            );

            data.put(
                    "division",
                    student.getDivision()
            );

            data.put(
                    "date",
                    selectedDate
            );

            data.put(
                    "status",
                    status
            );

            data.put(
                    "markedBy",
                    auth.getCurrentUser() == null
                            ? ""
                            : auth.getCurrentUser().getUid()
            );

            data.put(
                    "markedAt",
                    FieldValue.serverTimestamp()
            );

            /*
             * Same student + same date
             * = same document.
             *
             * So attendance won't create
             * duplicate documents.
             */
            String docId =
                    schoolId
                            + "_"
                            + studentId
                            + "_"
                            + selectedDate;

            batch.set(
                    db.collection(
                                    "studentAttendance"
                            )
                            .document(docId),
                    data
            );
        }

        batch.commit()

                .addOnSuccessListener(v -> {

                    btnSave.setEnabled(
                            true
                    );

                    btnSave.setText(
                            "Save Attendance"
                    );

                    toast(
                            "Student attendance saved successfully."
                    );
                })

                .addOnFailureListener(e -> {

                    btnSave.setEnabled(
                            true
                    );

                    btnSave.setText(
                            "Save Attendance"
                    );

                    toast(
                            "Save failed: "
                                    + e.getMessage()
                    );
                });
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    private void updateSummary() {

        int present = 0;
        int absent = 0;
        int holiday = 0;
        int marked = 0;

        for (String status :
                statusMap.values()) {

            if (status == null) {
                continue;
            }

            if (status.equalsIgnoreCase(
                    "Present"
            )) {

                present++;

            } else if (status.equalsIgnoreCase(
                    "Absent"
            )) {

                absent++;

            } else if (status.equalsIgnoreCase(
                    "Holiday"
            )) {

                holiday++;
            }

            marked++;
        }

        tvSummary.setText(
                "Present "
                        + present
                        + "  •  Absent "
                        + absent
                        + "  •  Holiday "
                        + holiday
                        + "  •  Marked "
                        + marked
                        + "/"
                        + students.size()
        );
    }

    // =========================================================
    // DATE PICKER
    // =========================================================

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();

        try {

            calendar.setTime(
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    ).parse(
                            selectedDate
                    )
            );

        } catch (Exception ignored) {
        }

        new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    selectedDate =
                            String.format(
                                    Locale.getDefault(),
                                    "%04d-%02d-%02d",
                                    year,
                                    month + 1,
                                    day
                            );

                    etDate.setText(
                            displayDate(
                                    selectedDate
                            )
                    );

                    loadExistingAttendanceThenRender();

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
        ).show();
    }

    // =========================================================
    // TODAY
    // =========================================================

    public static String todayKey() {

        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        )
                .format(
                        Calendar.getInstance()
                                .getTime()
                );
    }

    // =========================================================
    // DISPLAY DATE
    // =========================================================

    private String displayDate(
            String key
    ) {

        try {

            Calendar calendar =
                    Calendar.getInstance();

            calendar.setTime(
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    ).parse(
                            key
                    )
            );

            return new SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
            )
                    .format(
                            calendar.getTime()
                    );

        } catch (Exception e) {

            return key;
        }
    }

    // =========================================================
    // DIVISION MATCH
    // =========================================================

    private boolean divisionMatches(
            String teacherDivision,
            String studentDivision
    ) {

        return teacherDivision.isEmpty()
                || teacherDivision.equalsIgnoreCase(
                "All Divisions"
        )
                || teacherDivision.equalsIgnoreCase(
                safe(studentDivision)
        );
    }

    // =========================================================
    // NORMALIZE CLASS
    // =========================================================

    private String normalizeClass(
            String value
    ) {

        return safe(value)
                .toLowerCase(Locale.ROOT)
                .replace(
                        "class",
                        ""
                )
                .replace(
                        "standard",
                        ""
                )
                .replace(
                        "std",
                        ""
                )
                .replace(
                        "-",
                        ""
                )
                .replaceAll(
                        "\\s+",
                        ""
                );
    }

    // =========================================================
    // EXTRACT CLASS NUMBER
    // =========================================================

    private int extractClassNumber(
            String value
    ) {

        String number = safe(value)
                .replaceAll(
                        "[^0-9]",
                        ""
                );

        try {

            if (number.isEmpty()) {
                return 999;
            }

            return Integer.parseInt(
                    number
            );

        } catch (Exception e) {

            return 999;
        }
    }

    // =========================================================
    // PARSE ROLL NUMBER
    // =========================================================

    private int parseRoll(
            String value
    ) {

        try {

            return Integer.parseInt(
                    safe(value)
            );

        } catch (Exception e) {

            return 999999;
        }
    }

    // =========================================================
    // FULL STUDENT NAME
    // =========================================================

    private String fullName(
            StudentModel student
    ) {

        String value =
                (
                        safe(
                                student.getFirstName()
                        )
                                + " "
                                + safe(
                                student.getMiddleName()
                        )
                                + " "
                                + safe(
                                student.getLastName()
                        )
                ).trim();

        if (value.isEmpty()) {
            return "Student";
        }

        return value.replaceAll(
                "\\s+",
                " "
        );
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    private String safe(
            String value,
            String fallback
    ) {

        String result =
                safe(value);

        return result.isEmpty()
                ? fallback
                : result;
    }

    // =========================================================
    // DP
    // =========================================================

    private int dp(
            int value
    ) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =========================================================
    // TOAST
    // =========================================================

    private void toast(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (studentsListener != null) {
            studentsListener.remove();
        }

        if (attendanceListener != null) {
            attendanceListener.remove();
        }

        studentsListener = null;
        attendanceListener = null;

        super.onDestroy();
    }

    // =========================================================
    // FINISH WITH MESSAGE
    // =========================================================

    private void finishWithMessage(
            String message
    ) {

        toast(message);

        finish();
    }
}
