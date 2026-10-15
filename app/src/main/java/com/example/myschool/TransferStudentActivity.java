package com.example.myschool;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TransferStudentActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private Spinner spCurrentClass;
    private Spinner spNewClass;

    private EditText etNewAcademicYear;
    private EditText etNewDivision;

    private CheckBox cbSelectAll;

    private Button btnPromote;

    private LinearLayout studentContainer;

    private TextView tvStudentCount;
    private TextView tvNoStudents;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    // =========================================================
    // DATA
    // =========================================================

    private final List<DocumentSnapshot> allStudents =
            new ArrayList<>();

    private final Set<String> selectedStudentIds =
            new HashSet<>();


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_transfer_student
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =====================================================
        // INITIALIZE
        // =====================================================

        initViews();

        setupCurrentClassSpinner();

        setupNewClassSpinner();

        setupListeners();

        loadStudents();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );


        spCurrentClass =
                findViewById(
                        R.id.spCurrentClass
                );


        spNewClass =
                findViewById(
                        R.id.spNewClass
                );


        etNewAcademicYear =
                findViewById(
                        R.id.etNewAcademicYear
                );


        etNewDivision =
                findViewById(
                        R.id.etNewDivision
                );


        cbSelectAll =
                findViewById(
                        R.id.cbSelectAll
                );


        btnPromote =
                findViewById(
                        R.id.btnPromote
                );


        studentContainer =
                findViewById(
                        R.id.studentContainer
                );


        tvStudentCount =
                findViewById(
                        R.id.tvStudentCount
                );


        tvNoStudents =
                findViewById(
                        R.id.tvNoStudents
                );
    }


    // =========================================================
    // CURRENT CLASS SPINNER
    // =========================================================

    private void setupCurrentClassSpinner() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new ArrayList<>()
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCurrentClass.setAdapter(adapter);

        spCurrentClass.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        selectedStudentIds.clear();

                        cbSelectAll.setChecked(false);

                        showStudentsForSelectedClass();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );

        loadCurrentClassesFromSchool(adapter);
    }


    // =========================================================
    // LOAD CURRENT CLASSES FROM SCHOOL
    // =========================================================

    private void loadCurrentClassesFromSchool(
            ArrayAdapter<String> adapter
    ) {

        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    Set<String> uniqueClasses =
                            new HashSet<>();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        String status =
                                getString(
                                        doc,
                                        "status"
                                );

                        if ("Deleted".equalsIgnoreCase(status)
                                || "Left School".equalsIgnoreCase(status)) {
                            continue;
                        }

                        String className =
                                getString(
                                        doc,
                                        "className"
                                );

                        if (!className.isEmpty()) {
                            uniqueClasses.add(className.trim());
                        }
                    }

                    List<String> sortedClasses =
                            new ArrayList<>(uniqueClasses);

                    Collections.sort(
                            sortedClasses,
                            new Comparator<String>() {
                                @Override
                                public int compare(
                                        String a,
                                        String b) {
                                    return Integer.compare(
                                            extractNumber(a),
                                            extractNumber(b)
                                    );
                                }
                            }
                    );

                    adapter.clear();
                    adapter.add("Select Current Class");
                    adapter.addAll(sortedClasses);
                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Unable to load school classes",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // =========================================================
    // NEW CLASS SPINNER
    // =========================================================

    private void setupNewClassSpinner() {

        String[] classes = {

                "Select New Class",

                "Nursery",
                "LKG",
                "UKG",

                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        classes
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spNewClass.setAdapter(
                adapter
        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // SELECT ALL
        // =====================================================

        cbSelectAll.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    String selectedClass =
                            getSelectedCurrentClass();


                    if (selectedClass.equals(
                            "Select Current Class"
                    )) {

                        cbSelectAll.setChecked(false);

                        Toast.makeText(
                                this,
                                "Please select current class first",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    List<DocumentSnapshot> visibleStudents =
                            getVisibleStudents();


                    if (isChecked) {

                        for (
                                DocumentSnapshot student :
                                visibleStudents
                        ) {

                            selectedStudentIds.add(
                                    student.getId()
                            );
                        }

                    } else {

                        for (
                                DocumentSnapshot student :
                                visibleStudents
                        ) {

                            selectedStudentIds.remove(
                                    student.getId()
                            );
                        }
                    }


                    showStudentsForSelectedClass();
                }
        );


        // =====================================================
        // PROMOTE
        // =====================================================

        btnPromote.setOnClickListener(
                v -> validatePromotion()
        );
    }


    // =========================================================
    // LOAD STUDENTS
    // =========================================================

    private void loadStudents() {

        if (mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Admin login session expired",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            allStudents.clear();


                            for (
                                    DocumentSnapshot document :
                                    querySnapshot.getDocuments()
                            ) {

                                // =============================================
                                // DO NOT SHOW REMOVED STUDENTS
                                // =============================================

                                Boolean active =
                                        document.getBoolean(
                                                "active"
                                        );


                                String status =
                                        getString(
                                                document,
                                                "status"
                                        );


                                if (
                                        Boolean.FALSE.equals(active)
                                                ||
                                                status.equalsIgnoreCase(
                                                        "Left School"
                                                )
                                ) {

                                    continue;
                                }


                                allStudents.add(
                                        document
                                );
                            }


                            // =============================================
                            // SORT BY SURNAME A-Z
                            // =============================================

                            sortStudentsAlphabetically();


                            showStudentsForSelectedClass();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            allStudents.clear();

                            showStudentsForSelectedClass();


                            Toast.makeText(
                                    this,
                                    "Failed to load students:\n" +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // SORT STUDENTS
    // =========================================================

    private void sortStudentsAlphabetically() {

        Collections.sort(
                allStudents,
                new Comparator<DocumentSnapshot>() {

                    @Override
                    public int compare(
                            DocumentSnapshot first,
                            DocumentSnapshot second) {

                        String firstSurname =
                                getString(
                                        first,
                                        "lastName"
                                ).toLowerCase(
                                        Locale.getDefault()
                                );


                        String secondSurname =
                                getString(
                                        second,
                                        "lastName"
                                ).toLowerCase(
                                        Locale.getDefault()
                                );


                        int surnameResult =
                                firstSurname.compareTo(
                                        secondSurname
                                );


                        if (surnameResult != 0) {

                            return surnameResult;
                        }


                        String firstName =
                                getString(
                                        first,
                                        "firstName"
                                ).toLowerCase(
                                        Locale.getDefault()
                                );


                        String secondName =
                                getString(
                                        second,
                                        "firstName"
                                ).toLowerCase(
                                        Locale.getDefault()
                                );


                        int firstNameResult =
                                firstName.compareTo(
                                        secondName
                                );


                        if (firstNameResult != 0) {

                            return firstNameResult;
                        }


                        // =========================================
                        // SAME NAME -> ROLL NUMBER
                        // =========================================

                        int firstRoll =
                                getRollNumber(
                                        first
                                );


                        int secondRoll =
                                getRollNumber(
                                        second
                                );


                        return Integer.compare(
                                firstRoll,
                                secondRoll
                        );
                    }
                }
        );
    }


    // =========================================================
    // SHOW STUDENTS FOR SELECTED CLASS
    // =========================================================

    private void showStudentsForSelectedClass() {

        studentContainer.removeAllViews();


        String selectedClass =
                getSelectedCurrentClass();


        if (selectedClass.equals(
                "Select Current Class"
        )) {

            tvStudentCount.setText(
                    "Students: 0"
            );


            tvNoStudents.setVisibility(
                    View.VISIBLE
            );


            tvNoStudents.setText(
                    "Select a class to view students"
            );


            return;
        }


        List<DocumentSnapshot> visibleStudents =
                getVisibleStudents();


        tvStudentCount.setText(
                "Students: " +
                        visibleStudents.size()
        );


        if (visibleStudents.isEmpty()) {

            tvNoStudents.setVisibility(
                    View.VISIBLE
            );


            tvNoStudents.setText(
                    "No students found in this class"
            );


            cbSelectAll.setChecked(false);

            return;
        }


        tvNoStudents.setVisibility(
                View.GONE
        );


        // =====================================================
        // UPDATE SELECT ALL STATE
        // =====================================================

        boolean allSelected =
                true;


        for (
                DocumentSnapshot student :
                visibleStudents
        ) {

            if (
                    !selectedStudentIds.contains(
                            student.getId()
                    )
            ) {

                allSelected = false;

                break;
            }
        }


        cbSelectAll.setOnCheckedChangeListener(
                null
        );


        cbSelectAll.setChecked(
                allSelected
        );


        cbSelectAll.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        for (
                                DocumentSnapshot student :
                                visibleStudents
                        ) {

                            selectedStudentIds.add(
                                    student.getId()
                            );
                        }

                    } else {

                        for (
                                DocumentSnapshot student :
                                visibleStudents
                        ) {

                            selectedStudentIds.remove(
                                    student.getId()
                            );
                        }
                    }


                    showStudentsForSelectedClass();
                }
        );


        // =====================================================
        // CREATE CARDS
        // =====================================================

        for (
                DocumentSnapshot student :
                visibleStudents
        ) {

            addStudentCard(
                    student
            );
        }
    }


    // =========================================================
    // GET VISIBLE STUDENTS
    // =========================================================

    private List<DocumentSnapshot> getVisibleStudents() {

        List<DocumentSnapshot> result =
                new ArrayList<>();


        String selectedClass =
                getSelectedCurrentClass();


        if (selectedClass.equals(
                "Select Current Class"
        )) {

            return result;
        }


        for (
                DocumentSnapshot student :
                allStudents
        ) {

            String className =
                    getString(
                            student,
                            "className"
                    );


            if (
                    normalizeClass(className)
                            .equals(
                                    normalizeClass(selectedClass)
                            )
            ) {

                result.add(
                        student
                );
            }
        }


        return result;
    }


    // =========================================================
    // NORMALIZE CLASS
    // =========================================================

    private String normalizeClass(String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.getDefault())
                .replace("class", "")
                .replace("standard", "")
                .replace("std", "")
                .replace("-", "")
                .replace(" ", "")
                .trim();
    }


    // =========================================================
    // EXTRACT CLASS NUMBER
    // =========================================================

    private int extractNumber(String value) {

        if (value == null) {
            return 999;
        }

        String number =
                value.replaceAll("[^0-9]", "");

        if (number.isEmpty()) {
            return 999;
        }

        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            return 999;
        }
    }


    // =========================================================
    // ADD STUDENT CARD
    // =========================================================

    private void addStudentCard(
            DocumentSnapshot student) {

        String studentId =
                student.getId();

        String studentName =
                getStudentName(student);

        String surname =
                getString(
                        student,
                        "lastName"
                );

        String className =
                getString(
                        student,
                        "className"
                );

        String division =
                getString(
                        student,
                        "division"
                );

        String rollNumber =
                getString(
                        student,
                        "rollNumber"
                );

        String academicYear =
                getString(
                        student,
                        "academicYear"
                );


        // =====================================================
        // CARD
        // =====================================================

        MaterialCardView card =
                new MaterialCardView(this);

        card.setRadius(16);

        card.setCardElevation(2);

        card.setStrokeWidth(1);

        card.setStrokeColor(
                Color.rgb(
                        229,
                        224,
                        240
                )
        );

        card.setCardBackgroundColor(
                Color.WHITE
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                10
        );

        card.setLayoutParams(
                cardParams
        );


        // =====================================================
        // MAIN LAYOUT
        // =====================================================

        LinearLayout mainLayout =
                new LinearLayout(this);

        mainLayout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        mainLayout.setGravity(
                Gravity.CENTER_VERTICAL
        );

        mainLayout.setPadding(
                10,
                10,
                10,
                10
        );


        // =====================================================
        // CHECKBOX
        // =====================================================

        CheckBox checkBox =
                new CheckBox(this);

        checkBox.setButtonTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(
                                75,
                                31,
                                168
                        )
                )
        );

        checkBox.setChecked(
                selectedStudentIds.contains(
                        studentId
                )
        );


        LinearLayout.LayoutParams checkParams =
                new LinearLayout.LayoutParams(
                        80,
                        80
                );

        checkParams.setMargins(
                0,
                0,
                6,
                0
        );

        checkBox.setLayoutParams(
                checkParams
        );


        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        selectedStudentIds.add(
                                studentId
                        );

                    } else {

                        selectedStudentIds.remove(
                                studentId
                        );


                        cbSelectAll.setOnCheckedChangeListener(
                                null
                        );

                        cbSelectAll.setChecked(
                                false
                        );

                        cbSelectAll.setOnCheckedChangeListener(
                                (button, checked) -> {

                                    List<DocumentSnapshot> visible =
                                            getVisibleStudents();

                                    if (checked) {

                                        for (
                                                DocumentSnapshot doc :
                                                visible
                                        ) {

                                            selectedStudentIds.add(
                                                    doc.getId()
                                            );
                                        }

                                    } else {

                                        for (
                                                DocumentSnapshot doc :
                                                visible
                                        ) {

                                            selectedStudentIds.remove(
                                                    doc.getId()
                                            );
                                        }

                                    }

                                    showStudentsForSelectedClass();
                                }
                        );
                    }
                }
        );


        mainLayout.addView(
                checkBox
        );


        // =====================================================
        // INFO LAYOUT
        // =====================================================

        LinearLayout infoLayout =
                new LinearLayout(this);

        infoLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        infoLayout.setGravity(
                Gravity.CENTER_VERTICAL
        );


        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        infoParams.setMargins(
                0,
                0,
                6,
                0
        );

        infoLayout.setLayoutParams(
                infoParams
        );


        // =====================================================
        // NAME
        // =====================================================

        TextView tvName =
                new TextView(this);

        tvName.setText(
                studentName
        );

        tvName.setTextSize(
                16
        );

        tvName.setTextColor(
                Color.rgb(
                        45,
                        45,
                        45
                )
        );

        tvName.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        tvName.setSingleLine(
                true
        );

        tvName.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );


        infoLayout.addView(
                tvName
        );


        // =====================================================
        // DETAILS
        // =====================================================

        TextView tvDetails =
                new TextView(this);


        tvDetails.setText(

                "Class: " +
                        display(className) +

                        "   Division: " +
                        display(division) +

                        "\nRoll No: " +
                        display(rollNumber) +

                        "   Academic Year: " +
                        display(academicYear)
        );


        tvDetails.setTextSize(
                13
        );

        tvDetails.setTextColor(
                Color.rgb(
                        95,
                        95,
                        95
                )
        );

        tvDetails.setPadding(
                0,
                4,
                0,
                0
        );


        infoLayout.addView(
                tvDetails
        );


        mainLayout.addView(
                infoLayout
        );


        // =====================================================
        // REMOVE BUTTON
        // =====================================================

        Button btnRemove =
                new Button(this);


        btnRemove.setText(
                "REMOVE"
        );


        btnRemove.setTextSize(
                11
        );


        btnRemove.setAllCaps(
                true
        );


        btnRemove.setTextColor(
                Color.rgb(
                        198,
                        40,
                        40
                )
        );


        btnRemove.setGravity(
                Gravity.CENTER
        );


        btnRemove.setMinHeight(
                0
        );


        btnRemove.setMinWidth(
                0
        );


        btnRemove.setPadding(
                2,
                0,
                2,
                0
        );


        btnRemove.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(
                                255,
                                235,
                                235
                        )
                )
        );


        LinearLayout.LayoutParams removeParams =
                new LinearLayout.LayoutParams(
                        200,
                        80
                );


        btnRemove.setLayoutParams(
                removeParams
        );


        btnRemove.setOnClickListener(
                v -> confirmRemoveStudent(
                        student
                )
        );


        mainLayout.addView(
                btnRemove
        );


        // =====================================================
        // ADD CARD
        // =====================================================

        card.addView(
                mainLayout
        );


        studentContainer.addView(
                card
        );
    }


    // =========================================================
    // VALIDATE PROMOTION
    // =========================================================

    private void validatePromotion() {

        if (selectedStudentIds.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select at least one student",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String newAcademicYear =
                etNewAcademicYear
                        .getText()
                        .toString()
                        .trim();


        String newDivision =
                etNewDivision
                        .getText()
                        .toString()
                        .trim();


        String newClass =
                spNewClass
                        .getSelectedItem()
                        .toString();


        if (newAcademicYear.isEmpty()) {

            etNewAcademicYear.setError(
                    "Enter new academic year"
            );

            etNewAcademicYear.requestFocus();

            return;
        }


        if (newClass.equals(
                "Select New Class"
        )) {

            Toast.makeText(
                    this,
                    "Please select new class",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (newDivision.isEmpty()) {

            etNewDivision.setError(
                    "Enter new division"
            );

            etNewDivision.requestFocus();

            return;
        }


        String currentClass =
                getSelectedCurrentClass();


        String message =

                "Current Class: " +
                        currentClass +

                        "\n\nSelected Students: " +
                        selectedStudentIds.size() +

                        "\n\nNew Academic Year: " +
                        newAcademicYear +

                        "\nNew Class: " +
                        newClass +

                        "\nNew Division: " +
                        newDivision +

                        "\n\nSelected students will be promoted using the same student IDs.";


        new AlertDialog.Builder(this)

                .setTitle(
                        "Confirm Promotion"
                )

                .setMessage(
                        message
                )

                .setNegativeButton(
                        "CANCEL",
                        null
                )

                .setPositiveButton(
                        "PROMOTE",
                        (dialog, which) -> {

                            promoteSelectedStudents(
                                    newAcademicYear,
                                    newClass,
                                    newDivision
                            );
                        }
                )

                .show();
    }


    // =========================================================
    // PROMOTE SELECTED STUDENTS
    // =========================================================

    private void promoteSelectedStudents(
            String newAcademicYear,
            String newClass,
            String newDivision) {

        if (selectedStudentIds.isEmpty()) {

            return;
        }


        WriteBatch batch =
                db.batch();


        int count = 0;


        for (
                DocumentSnapshot student :
                allStudents
        ) {

            String studentId =
                    student.getId();


            if (
                    !selectedStudentIds.contains(
                            studentId
                    )
            ) {

                continue;
            }


            Map<String, Object> updates =
                    new HashMap<>();


            updates.put(
                    "academicYear",
                    newAcademicYear
            );


            updates.put(
                    "className",
                    newClass
            );


            updates.put(
                    "division",
                    newDivision
            );


            updates.put(
                    "active",
                    true
            );


            updates.put(
                    "status",
                    "Active"
            );


            updates.put(
                    "lastTransferDate",
                    FieldValue.serverTimestamp()
            );

            batch.update(
                    db.collection("students")
                            .document(studentId),
                    updates
            );

            count++;
        }


        final int promotedCount =
                count;


        if (promotedCount == 0) {

            Toast.makeText(
                    this,
                    "No students selected",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        btnPromote.setEnabled(
                false
        );


        btnPromote.setText(
                "PROMOTING..."
        );


        batch.commit()

                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    this,
                                    promotedCount +
                                            " students promoted successfully",
                                    Toast.LENGTH_LONG
                            ).show();


                            selectedStudentIds.clear();


                            cbSelectAll.setChecked(
                                    false
                            );


                            etNewAcademicYear.setText(
                                    ""
                            );


                            etNewDivision.setText(
                                    ""
                            );


                            spNewClass.setSelection(
                                    0
                            );


                            btnPromote.setEnabled(
                                    true
                            );


                            btnPromote.setText(
                                    "PROMOTE SELECTED STUDENTS"
                            );


                            loadStudents();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            btnPromote.setEnabled(
                                    true
                            );


                            btnPromote.setText(
                                    "PROMOTE SELECTED STUDENTS"
                            );


                            Toast.makeText(
                                    this,
                                    "Promotion failed:\n" +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // REMOVE STUDENT CONFIRMATION
    // =========================================================

    private void confirmRemoveStudent(
            DocumentSnapshot student) {

        String studentName =
                getStudentName(
                        student
                );


        new AlertDialog.Builder(this)

                .setTitle(
                        "Remove Student"
                )

                .setMessage(
                        "Are you sure you want to remove\n\n" +
                                studentName +
                                "\n\nfrom the active school students?"
                )

                .setNegativeButton(
                        "CANCEL",
                        null
                )

                .setPositiveButton(
                        "REMOVE",
                        (dialog, which) -> {

                            removeStudentFromSchool(
                                    student
                            );
                        }
                )

                .show();
    }


    // =========================================================
    // REMOVE STUDENT
    // =========================================================

    private void removeStudentFromSchool(
            DocumentSnapshot student) {

        final String studentId =
                student.getId();

        if (TextUtils.isEmpty(studentId)) {
            Toast.makeText(
                    this,
                    "Invalid student ID",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        btnPromote.setEnabled(false);

        // =====================================================
        // DELETE ALL STUDENT-RELATED DATA FIRST
        // =====================================================
        deleteStudentCollectionData(
                "studentAttendance",
                studentId,
                () -> deleteStudentCollectionData(
                        "studentMarks",
                        studentId,
                        () -> deleteStudentCollectionData(
                                "studentRecords",
                                studentId,
                                () -> deleteStudentDocument(
                                        studentId
                                )
                        )
                )
        );
    }


    // =========================================================
    // DELETE DATA FROM A STUDENT-BASED COLLECTION
    // =========================================================

    private void deleteStudentCollectionData(
            String collectionName,
            String studentId,
            Runnable onComplete) {

        db.collection(collectionName)
                .whereEqualTo(
                        "schoolId",
                        mAuth.getCurrentUser() == null
                                ? ""
                                : mAuth.getCurrentUser().getUid()
                )
                .whereEqualTo(
                        "studentId",
                        studentId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        onComplete.run();
                        return;
                    }

                    // Firestore batch limit is 500 writes.
                    // Delete this collection in safe chunks.
                    deleteStudentDocumentsInChunks(
                            collectionName,
                            snapshot.getDocuments(),
                            0,
                            () -> onComplete.run()
                    );
                })
                .addOnFailureListener(e -> {

                    btnPromote.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Unable to remove student data from "
                                    + collectionName
                                    + ":\n"
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =========================================================
    // DELETE COLLECTION DOCUMENTS IN 500-DOC CHUNKS
    // =========================================================

    private void deleteStudentDocumentsInChunks(
            String collectionName,
            List<DocumentSnapshot> documents,
            int startIndex,
            Runnable onComplete) {

        if (startIndex >= documents.size()) {
            onComplete.run();
            return;
        }

        WriteBatch batch = db.batch();

        int endIndex = Math.min(
                startIndex + 500,
                documents.size()
        );

        for (int i = startIndex; i < endIndex; i++) {
            batch.delete(
                    db.collection(collectionName)
                            .document(documents.get(i).getId())
            );
        }

        batch.commit()
                .addOnSuccessListener(unused ->
                        deleteStudentDocumentsInChunks(
                                collectionName,
                                documents,
                                endIndex,
                                onComplete
                        )
                )
                .addOnFailureListener(e -> {

                    btnPromote.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Delete failed in "
                                    + collectionName
                                    + ":\n"
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =========================================================
    // DELETE MAIN STUDENT DOCUMENT
    // =========================================================

    private void deleteStudentDocument(
            String studentId) {

        db.collection("students")
                .document(studentId)
                .delete()
                .addOnSuccessListener(unused ->
                        decreaseSchoolStudentCount(
                                studentId
                        )
                )
                .addOnFailureListener(e -> {

                    btnPromote.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Unable to remove student:\n"
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // =========================================================
    // DECREASE SCHOOL STUDENT COUNT
    // =========================================================

    private void decreaseSchoolStudentCount(
            String studentId) {

        if (mAuth.getCurrentUser() == null) {
            btnPromote.setEnabled(true);
            Toast.makeText(
                    this,
                    "School login session expired",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String schoolId =
                mAuth.getCurrentUser().getUid();

        db.collection("schools")
                .document(schoolId)
                .get()
                .addOnSuccessListener(school -> {

                    Long currentCount =
                            school.getLong("studentCount");

                    long newCount =
                            currentCount == null
                                    ? 0
                                    : Math.max(
                                    0,
                                    currentCount - 1
                            );

                    db.collection("schools")
                            .document(schoolId)
                            .update(
                                    "studentCount",
                                    newCount
                            )
                            .addOnSuccessListener(unused ->
                                    removeStudentFinished()
                            )
                            .addOnFailureListener(e -> {

                                btnPromote.setEnabled(true);

                                Toast.makeText(
                                        this,
                                        "Student removed, but student count could not be updated:\n"
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();

                                loadStudents();
                            });
                })
                .addOnFailureListener(e -> {

                    btnPromote.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Student removed, but school count could not be read:\n"
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    loadStudents();
                });
    }


    // =========================================================
    // REMOVE COMPLETE
    // =========================================================

    private void removeStudentFinished() {

        selectedStudentIds.clear();

        cbSelectAll.setChecked(false);

        btnPromote.setEnabled(true);

        Toast.makeText(
                this,
                "Student completely removed from school.",
                Toast.LENGTH_LONG
        ).show();

        loadStudents();
    }


    // =========================================================
    // GET SELECTED CURRENT CLASS
    // =========================================================

    private String getSelectedCurrentClass() {

        if (
                spCurrentClass == null ||
                        spCurrentClass.getSelectedItem() == null
        ) {

            return "Select Current Class";
        }


        return spCurrentClass
                .getSelectedItem()
                .toString();
    }


    // =========================================================
    // STUDENT NAME
    // =========================================================

    private String getStudentName(
            DocumentSnapshot document) {

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


        String name =
                (
                        firstName +
                                " " +
                                middleName +
                                " " +
                                lastName
                ).trim();


        if (name.isEmpty()) {

            return "Student";
        }


        return name;
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String key) {

        Object value =
                document.get(key);


        if (value == null) {

            return "";
        }


        return String.valueOf(
                value
        ).trim();
    }


    // =========================================================
    // GET ROLL NUMBER
    // =========================================================

    private int getRollNumber(
            DocumentSnapshot document) {

        Object value =
                document.get(
                        "rollNumber"
                );


        if (value == null) {

            return Integer.MAX_VALUE;
        }


        try {

            return Integer.parseInt(
                    String.valueOf(
                            value
                    ).trim()
            );

        } catch (Exception e) {

            return Integer.MAX_VALUE;
        }
    }


    // =========================================================
    // DISPLAY
    // =========================================================

    private String display(
            String value) {

        if (
                value == null ||
                        value.trim().isEmpty()
        ) {

            return "-";
        }


        return value;
    }
}