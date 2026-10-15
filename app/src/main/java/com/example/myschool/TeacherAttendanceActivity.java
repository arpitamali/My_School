package com.example.myschool;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherAttendanceActivity
        extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private EditText etAttendanceDate;

    private RecyclerView recyclerAttendance;

    private Button btnMarkHoliday;
    private Button btnSaveAttendance;

    private TextView tvPresentCount;
    private TextView tvAbsentCount;
    private TextView tvHalfDayCount;
    private TextView tvHolidayCount;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    private ListenerRegistration attendanceListener;


    // =========================================================
    // ADAPTER
    // =========================================================

    private AttendanceAdapter adapter;

    private final List<StaffModel> staffList =
            new ArrayList<>();


    // =========================================================
    // DATE
    // =========================================================

    private String selectedDate;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_attendance
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();


        // =====================================================
        // INITIALIZE
        // =====================================================

        initViews();

        setupRecycler();

        setupListeners();

        setTodayDate();

        loadStaff();
    }


    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        btnBack =
                findViewById(
                        R.id.btnBack
                );

        etAttendanceDate =
                findViewById(
                        R.id.etAttendanceDate
                );

        recyclerAttendance =
                findViewById(
                        R.id.recyclerAttendance
                );

        btnMarkHoliday =
                findViewById(
                        R.id.btnMarkHoliday
                );

        btnSaveAttendance =
                findViewById(
                        R.id.btnSaveAttendance
                );

        tvPresentCount =
                findViewById(
                        R.id.tvPresentCount
                );

        tvAbsentCount =
                findViewById(
                        R.id.tvAbsentCount
                );

        tvHalfDayCount =
                findViewById(
                        R.id.tvHalfDayCount
                );

        tvHolidayCount =
                findViewById(
                        R.id.tvHolidayCount
                );
    }


    // =========================================================
    // RECYCLER
    // =========================================================

    private void setupRecycler() {

        adapter =
                new AttendanceAdapter(
                        staffList
                );


        recyclerAttendance.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );


        recyclerAttendance.setAdapter(
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


        etAttendanceDate.setOnClickListener(
                v -> showDatePicker()
        );


        btnSaveAttendance.setOnClickListener(
                v -> saveAttendance()
        );


        // =====================================================
        // MARK HOLIDAY
        // =====================================================

        btnMarkHoliday.setOnClickListener(
                v -> confirmHoliday()
        );
    }


    // =========================================================
    // CONFIRM HOLIDAY
    // =========================================================

    private void confirmHoliday() {

        if (
                selectedDate == null ||
                        selectedDate.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Please select date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        new AlertDialog.Builder(this)
                .setTitle("Mark Holiday")
                .setMessage(
                        "Mark "
                                + selectedDate
                                + " as Holiday for all staff?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Mark Holiday",
                        (dialog, which) -> {

                            adapter.markAllHoliday();

                            updateCounts();

                            saveAttendance();
                        }
                )
                .show();
    }


    // =========================================================
    // SET TODAY
    // =========================================================

    private void setTodayDate() {

        Calendar calendar =
                Calendar.getInstance();


        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );


        selectedDate =
                formatter.format(
                        calendar.getTime()
                );


        etAttendanceDate.setText(
                selectedDate
        );
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void showDatePicker() {

        Calendar calendar =
                Calendar.getInstance();


        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,

                        (view, year, month, dayOfMonth) -> {

                            selectedDate =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );


                            etAttendanceDate.setText(
                                    selectedDate
                            );


                            adapter.clearAttendance();

                            listenSelectedDateAttendance();
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


        dialog.show();
    }


    // =========================================================
    // LOAD STAFF
    // =========================================================

    private void loadStaff() {

        if (
                mAuth == null ||
                        mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("staff")

                .whereEqualTo(
                        "schoolId",
                        schoolId
                )

                .whereEqualTo(
                        "active",
                        true
                )

                .get()

                .addOnSuccessListener(
                        snapshot -> {

                            staffList.clear();


                            for (
                                    DocumentSnapshot document
                                    : snapshot.getDocuments()
                            ) {

                                StaffModel staff =
                                        document.toObject(
                                                StaffModel.class
                                        );


                                if (staff != null) {

                                    staff.setDocumentId(
                                            document.getId()
                                    );

                                    staffList.add(
                                            staff
                                    );
                                }
                            }


                            adapter.updateList(
                                    staffList
                            );


                            listenSelectedDateAttendance();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Unable to load staff.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // REAL TIME SELECTED DATE ATTENDANCE
    // =========================================================

    private void listenSelectedDateAttendance() {

        if (
                mAuth == null ||
                        mAuth.getCurrentUser() == null
        ) {

            return;
        }


        if (
                selectedDate == null ||
                        selectedDate.trim().isEmpty()
        ) {

            return;
        }


        if (attendanceListener != null) {

            attendanceListener.remove();

            attendanceListener = null;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        attendanceListener =
                db.collection("attendance")

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

                                    if (error != null) {
                                        return;
                                    }


                                    if (snapshot == null) {
                                        return;
                                    }


                                    adapter.clearAttendance();


                                    for (
                                            DocumentSnapshot document
                                            : snapshot.getDocuments()
                                    ) {

                                        String staffId =
                                                document.getString(
                                                        "staffId"
                                                );


                                        String status =
                                                document.getString(
                                                        "status"
                                                );


                                        if (
                                                staffId != null
                                                        &&
                                                        status != null
                                                        &&
                                                        !status.isEmpty()
                                        ) {

                                            adapter.setAttendance(
                                                    staffId,
                                                    status
                                            );
                                        }
                                    }


                                    updateCounts();
                                }
                        );
    }


    // =========================================================
    // SAVE ATTENDANCE
    // =========================================================

    private void saveAttendance() {

        if (
                mAuth == null ||
                        mAuth.getCurrentUser() == null
        ) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (
                selectedDate == null ||
                        selectedDate.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Please select date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (staffList.isEmpty()) {

            Toast.makeText(
                    this,
                    "No staff found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // ALL STAFF MUST BE MARKED
        // =====================================================

        if (!adapter.areAllMarked()) {

            Toast.makeText(
                    this,
                    "Please mark attendance for all staff.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        btnSaveAttendance.setEnabled(
                false
        );

        btnMarkHoliday.setEnabled(
                false
        );


        btnSaveAttendance.setText(
                "SAVING..."
        );


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        Map<String, String> attendanceMap =
                adapter.getAttendanceMap();


        saveAttendanceItemByItem(
                schoolId,
                attendanceMap,
                0
        );
    }


    // =========================================================
    // SAVE ITEM BY ITEM
    // =========================================================

    private void saveAttendanceItemByItem(
            String schoolId,
            Map<String, String> attendanceMap,
            int index
    ) {

        List<String> staffIds =
                new ArrayList<>(
                        attendanceMap.keySet()
                );


        if (
                index >= staffIds.size()
        ) {

            Toast.makeText(
                    this,
                    "Attendance saved successfully.",
                    Toast.LENGTH_LONG
            ).show();


            resetSaveButton();

            return;
        }


        String staffId =
                staffIds.get(
                        index
                );


        String status =
                attendanceMap.get(
                        staffId
                );


        StaffModel staff =
                findStaffById(
                        staffId
                );


        if (staff == null) {

            saveAttendanceItemByItem(
                    schoolId,
                    attendanceMap,
                    index + 1
            );

            return;
        }


        String staffName =
                buildName(
                        staff
                );


        // =====================================================
        // ATTENDANCE DATA
        // =====================================================

        Map<String, Object> data =
                new HashMap<>();


        data.put(
                "schoolId",
                schoolId
        );


        data.put(
                "staffId",
                staffId
        );


        data.put(
                "staffName",
                staffName
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
                "month",
                getMonthFromDate()
        );


        data.put(
                "year",
                getYearFromDate()
        );


        data.put(
                "markedAt",
                FieldValue.serverTimestamp()
        );


        // =====================================================
        // UNIQUE DOCUMENT ID
        // =====================================================

        String documentId =
                schoolId
                        + "_"
                        + staffId
                        + "_"
                        + selectedDate.replace(
                        "/",
                        "-"
                );


        // =====================================================
        // FIRESTORE SAVE
        // =====================================================

        db.collection("attendance")
                .document(
                        documentId
                )
                .set(
                        data
                )

                .addOnSuccessListener(
                        unused -> {

                            saveAttendanceItemByItem(
                                    schoolId,
                                    attendanceMap,
                                    index + 1
                            );
                        }
                )

                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    this,
                                    "Failed to save attendance.",
                                    Toast.LENGTH_LONG
                            ).show();


                            resetSaveButton();
                        }
                );
    }


    // =========================================================
    // FIND STAFF
    // =========================================================

    private StaffModel findStaffById(
            String staffId
    ) {

        for (StaffModel staff : staffList) {

            if (
                    staffId != null
                            &&
                            staffId.equals(
                                    staff.getDocumentId()
                            )
            ) {

                return staff;
            }
        }


        return null;
    }


    // =========================================================
    // UPDATE COUNTS
    // =========================================================

    private void updateCounts() {

        if (adapter == null) {
            return;
        }


        int present = 0;

        int absent = 0;

        int halfDay = 0;

        int holiday = 0;


        for (StaffModel staff :
                staffList) {

            String status =
                    adapter.getAttendance(
                            staff.getDocumentId()
                    );


            if (status.equalsIgnoreCase("P")) {

                present++;

            } else if (
                    status.equalsIgnoreCase("A")
            ) {

                absent++;

            } else if (
                    status.equalsIgnoreCase("HD")
            ) {

                halfDay++;

            } else if (
                    status.equalsIgnoreCase("HDY")
            ) {

                holiday++;
            }
        }


        tvPresentCount.setText(
                String.valueOf(
                        present
                )
        );


        tvAbsentCount.setText(
                String.valueOf(
                        absent
                )
        );


        tvHalfDayCount.setText(
                String.valueOf(
                        halfDay
                )
        );


        tvHolidayCount.setText(
                String.valueOf(
                        holiday
                )
        );
    }


    // =========================================================
    // MONTH
    // =========================================================

    private String getMonthFromDate() {

        try {

            String[] parts =
                    selectedDate.split(
                            "/"
                    );


            return parts[1];

        } catch (Exception e) {

            return "";
        }
    }


    // =========================================================
    // YEAR
    // =========================================================

    private String getYearFromDate() {

        try {

            String[] parts =
                    selectedDate.split(
                            "/"
                    );


            return parts[2];

        } catch (Exception e) {

            return "";
        }
    }


    // =========================================================
    // BUILD NAME
    // =========================================================

    private String buildName(
            StaffModel staff
    ) {

        String name =
                (
                        safe(staff.getFirstName())
                                + " "
                                + safe(staff.getMiddleName())
                                + " "
                                + safe(staff.getLastName())
                )
                        .trim();


        if (name.isEmpty()) {

            return "Staff / Teacher";
        }


        return name;
    }


    // =========================================================
    // RESET BUTTON
    // =========================================================

    private void resetSaveButton() {

        btnSaveAttendance.setEnabled(
                true
        );

        btnMarkHoliday.setEnabled(
                true
        );


        btnSaveAttendance.setText(
                "SAVE ATTENDANCE"
        );
    }


    // =========================================================
    // SAFE
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();


        if (attendanceListener != null) {

            attendanceListener.remove();

            attendanceListener = null;
        }
    }
}