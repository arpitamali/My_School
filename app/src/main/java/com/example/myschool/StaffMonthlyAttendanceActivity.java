package com.example.myschool;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StaffMonthlyAttendanceActivity
        extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private ImageButton btnBack;

    private Spinner spinnerStaff;
    private Spinner spinnerMonth;
    private Spinner spinnerYear;

    private TextView tvSelectedStaff;

    private TextView tvWorkingDays;
    private TextView tvPresent;
    private TextView tvAbsent;
    private TextView tvHalfDay;

    private TextView tvAttendancePercentage;

    private RecyclerView recyclerMonthlyAttendance;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ListenerRegistration staffListener;
    private ListenerRegistration attendanceListener;


    // =========================================================
    // DATA
    // =========================================================

    private final List<StaffModel> staffList =
            new ArrayList<>();

    private final List<String> staffNames =
            new ArrayList<>();

    private final List<String> staffIds =
            new ArrayList<>();

    private final List<String> monthNames =
            new ArrayList<>();

    private final List<Integer> monthNumbers =
            new ArrayList<>();

    private final List<Integer> years =
            new ArrayList<>();


    // =========================================================
    // SELECTED
    // =========================================================

    private String selectedStaffId = "";
    private String selectedStaffName = "";

    private int selectedMonth;
    private int selectedYear;


    // =========================================================
    // ATTENDANCE
    // =========================================================

    private final Map<String, String> attendanceMap =
            new HashMap<>();


    // =========================================================
    // ADAPTER
    // =========================================================

    private MonthlyAttendanceAdapter monthlyAdapter;

    private final List<AttendanceRecord> attendanceRecords =
            new ArrayList<>();


    // =========================================================
    // DATE FORMAT
    // =========================================================

    /*
     * TeacherAttendanceActivity saves:
     *
     * dd/MM/yyyy
     *
     * Example:
     * 10/09/2026
     */

    private final SimpleDateFormat attendanceDateFormat =
            new SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
            );


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_staff_monthly_attendance
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =====================================================
        // INIT
        // =====================================================

        initViews();

        setupRecycler();

        setupMonthSpinner();

        setupYearSpinner();

        setupListeners();

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

        spinnerStaff =
                findViewById(
                        R.id.spinnerStaff
                );

        spinnerMonth =
                findViewById(
                        R.id.spinnerMonth
                );

        spinnerYear =
                findViewById(
                        R.id.spinnerYear
                );

        tvSelectedStaff =
                findViewById(
                        R.id.tvSelectedStaff
                );

        tvWorkingDays =
                findViewById(
                        R.id.tvWorkingDays
                );

        tvPresent =
                findViewById(
                        R.id.tvPresent
                );

        tvAbsent =
                findViewById(
                        R.id.tvAbsent
                );

        tvHalfDay =
                findViewById(
                        R.id.tvHalfDay
                );

        tvAttendancePercentage =
                findViewById(
                        R.id.tvAttendancePercentage
                );

        recyclerMonthlyAttendance =
                findViewById(
                        R.id.recyclerMonthlyAttendance
                );
    }


    // =========================================================
    // RECYCLER
    // =========================================================

    private void setupRecycler() {

        monthlyAdapter =
                new MonthlyAttendanceAdapter(
                        attendanceRecords
                );

        recyclerMonthlyAttendance.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerMonthlyAttendance.setAdapter(
                monthlyAdapter
        );
    }


    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        btnBack.setOnClickListener(
                v -> finish()
        );


        spinnerStaff.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {

                        if (staffIds.isEmpty()) {
                            return;
                        }

                        if (
                                position < 0 ||
                                        position >= staffIds.size()
                        ) {
                            return;
                        }


                        selectedStaffId =
                                staffIds.get(position);

                        selectedStaffName =
                                staffNames.get(position);

                        tvSelectedStaff.setText(
                                selectedStaffName
                        );

                        loadMonthlyAttendance();
                    }


                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );


        spinnerMonth.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {

                        if (monthNumbers.isEmpty()) {
                            return;
                        }

                        if (
                                position < 0 ||
                                        position >= monthNumbers.size()
                        ) {
                            return;
                        }


                        selectedMonth =
                                monthNumbers.get(position);

                        loadMonthlyAttendance();
                    }


                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );


        spinnerYear.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {

                        if (years.isEmpty()) {
                            return;
                        }

                        if (
                                position < 0 ||
                                        position >= years.size()
                        ) {
                            return;
                        }


                        selectedYear =
                                years.get(position);

                        loadMonthlyAttendance();
                    }


                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
        );
    }


    // =========================================================
    // MONTH SPINNER
    // =========================================================

    private void setupMonthSpinner() {

        monthNames.clear();
        monthNumbers.clear();


        String[] months = {

                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        };


        for (
                int i = 0;
                i < months.length;
                i++
        ) {

            monthNames.add(
                    months[i]
            );

            monthNumbers.add(
                    i + 1
            );
        }


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        monthNames
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spinnerMonth.setAdapter(
                adapter
        );


        Calendar calendar =
                Calendar.getInstance();


        selectedMonth =
                calendar.get(
                        Calendar.MONTH
                ) + 1;


        spinnerMonth.setSelection(
                selectedMonth - 1
        );
    }


    // =========================================================
    // YEAR SPINNER
    // =========================================================

    private void setupYearSpinner() {

        years.clear();


        Calendar calendar =
                Calendar.getInstance();


        int currentYear =
                calendar.get(
                        Calendar.YEAR
                );


        /*
         * Current year - 5
         * up to current year + 1
         */

        for (
                int year = currentYear - 5;
                year <= currentYear + 1;
                year++
        ) {

            years.add(year);
        }


        List<String> yearStrings =
                new ArrayList<>();


        for (
                Integer year :
                years
        ) {

            yearStrings.add(
                    String.valueOf(year)
            );
        }


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        yearStrings
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spinnerYear.setAdapter(
                adapter
        );


        int position =
                years.indexOf(
                        currentYear
                );


        if (position >= 0) {

            spinnerYear.setSelection(
                    position
            );
        }


        selectedYear =
                currentYear;
    }


    // =========================================================
    // LOAD STAFF - REAL TIME
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


        if (staffListener != null) {

            staffListener.remove();

            staffListener = null;
        }


        staffListener =
                db.collection("staff")

                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )

                        .whereEqualTo(
                                "active",
                                true
                        )

                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Unable to load staff.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }


                                    if (snapshot == null) {
                                        return;
                                    }


                                    staffList.clear();

                                    staffNames.clear();

                                    staffIds.clear();


                                    for (
                                            DocumentSnapshot document
                                            : snapshot.getDocuments()
                                    ) {

                                        StaffModel staff =
                                                document.toObject(
                                                        StaffModel.class
                                                );


                                        if (staff == null) {
                                            continue;
                                        }


                                        staff.setDocumentId(
                                                document.getId()
                                        );


                                        staffList.add(
                                                staff
                                        );


                                        String name =
                                                buildName(
                                                        staff
                                                );


                                        staffNames.add(
                                                name
                                        );


                                        staffIds.add(
                                                document.getId()
                                        );
                                    }


                                    /*
                                     * Keep staff ordered by name.
                                     */

                                    List<Integer> indexes =
                                            new ArrayList<>();


                                    for (
                                            int i = 0;
                                            i < staffNames.size();
                                            i++
                                    ) {

                                        indexes.add(i);
                                    }


                                    Collections.sort(
                                            indexes,
                                            (
                                                    a,
                                                    b
                                            ) ->
                                                    staffNames
                                                            .get(a)
                                                            .compareToIgnoreCase(
                                                                    staffNames.get(b)
                                                            )
                                    );


                                    List<String> sortedNames =
                                            new ArrayList<>();

                                    List<String> sortedIds =
                                            new ArrayList<>();


                                    for (
                                            Integer index :
                                            indexes
                                    ) {

                                        sortedNames.add(
                                                staffNames.get(index)
                                        );

                                        sortedIds.add(
                                                staffIds.get(index)
                                        );
                                    }


                                    staffNames.clear();

                                    staffIds.clear();


                                    staffNames.addAll(
                                            sortedNames
                                    );

                                    staffIds.addAll(
                                            sortedIds
                                    );


                                    if (!staffIds.isEmpty()) {

                                        /*
                                         * Try to keep previously
                                         * selected staff.
                                         */

                                        int selectedPosition =
                                                staffIds.indexOf(
                                                        selectedStaffId
                                                );


                                        if (
                                                selectedPosition < 0
                                        ) {

                                            selectedPosition = 0;
                                        }


                                        ArrayAdapter<String> adapter =
                                                new ArrayAdapter<>(
                                                        this,
                                                        android.R.layout.simple_spinner_item,
                                                        staffNames
                                                );


                                        adapter.setDropDownViewResource(
                                                android.R.layout.simple_spinner_dropdown_item
                                        );


                                        spinnerStaff.setAdapter(
                                                adapter
                                        );


                                        spinnerStaff.setSelection(
                                                selectedPosition
                                        );


                                        selectedStaffId =
                                                staffIds.get(
                                                        selectedPosition
                                                );


                                        selectedStaffName =
                                                staffNames.get(
                                                        selectedPosition
                                                );


                                        tvSelectedStaff.setText(
                                                selectedStaffName
                                        );


                                        loadMonthlyAttendance();

                                    } else {

                                        tvSelectedStaff.setText(
                                                "No staff found"
                                        );


                                        attendanceRecords.clear();

                                        monthlyAdapter.notifyDataSetChanged();

                                        clearSummary();
                                    }
                                }
                        );
    }


    // =========================================================
    // LOAD MONTHLY ATTENDANCE
    // =========================================================

    private void loadMonthlyAttendance() {

        if (
                mAuth == null ||
                        mAuth.getCurrentUser() == null
        ) {

            return;
        }


        if (
                selectedStaffId == null ||
                        selectedStaffId.trim().isEmpty()
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


        /*
         * Listen to all attendance documents
         * for this school and filter locally.
         *
         * Fields:
         * schoolId
         * staffId
         * date = dd/MM/yyyy
         * status = P/A/HD/HDY
         */

        attendanceListener =
                db.collection("attendance")

                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )

                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                this,
                                                "Unable to load attendance.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }


                                    if (snapshot == null) {
                                        return;
                                    }


                                    attendanceMap.clear();


                                    for (
                                            DocumentSnapshot document :
                                            snapshot.getDocuments()
                                    ) {

                                        String staffId =
                                                document.getString(
                                                        "staffId"
                                                );


                                        String date =
                                                document.getString(
                                                        "date"
                                                );


                                        String status =
                                                document.getString(
                                                        "status"
                                                );


                                        if (
                                                staffId == null ||
                                                        date == null ||
                                                        status == null
                                        ) {

                                            continue;
                                        }


                                        /*
                                         * Only selected staff.
                                         */

                                        if (
                                                !staffId.equals(
                                                        selectedStaffId
                                                )
                                        ) {

                                            continue;
                                        }


                                        /*
                                         * Make sure date belongs
                                         * to selected month/year.
                                         */

                                        Date parsedDate =
                                                parseAttendanceDate(
                                                        date
                                                );


                                        if (
                                                parsedDate == null
                                        ) {

                                            continue;
                                        }


                                        Calendar cal =
                                                Calendar.getInstance();


                                        cal.setTime(
                                                parsedDate
                                        );


                                        int docMonth =
                                                cal.get(
                                                        Calendar.MONTH
                                                ) + 1;


                                        int docYear =
                                                cal.get(
                                                        Calendar.YEAR
                                                );


                                        if (
                                                docMonth != selectedMonth ||
                                                        docYear != selectedYear
                                        ) {

                                            continue;
                                        }


                                        /*
                                         * Normalize status.
                                         */

                                        String normalizedStatus =
                                                normalizeStatus(
                                                        status
                                                );


                                        if (
                                                normalizedStatus.isEmpty()
                                        ) {

                                            continue;
                                        }


                                        attendanceMap.put(
                                                date,
                                                normalizedStatus
                                        );
                                    }


                                    buildMonthlyReport();
                                }
                        );
    }


    // =========================================================
    // BUILD REPORT
    // =========================================================

    private void buildMonthlyReport() {

        attendanceRecords.clear();


        Calendar start =
                Calendar.getInstance();


        start.set(
                selectedYear,
                selectedMonth - 1,
                1,
                0,
                0,
                0
        );


        start.set(
                Calendar.MILLISECOND,
                0
        );


        Calendar end =
                Calendar.getInstance();


        end.set(
                selectedYear,
                selectedMonth - 1,
                getDaysInMonth(
                        selectedYear,
                        selectedMonth
                ),
                23,
                59,
                59
        );


        end.set(
                Calendar.MILLISECOND,
                999
        );


        /*
         * Current month:
         * show only dates up to today.
         *
         * Previous month:
         * show complete month.
         *
         * Future month:
         * show no dates.
         */

        Calendar today =
                Calendar.getInstance();


        today.set(
                Calendar.HOUR_OF_DAY,
                23
        );


        today.set(
                Calendar.MINUTE,
                59
        );


        today.set(
                Calendar.SECOND,
                59
        );


        today.set(
                Calendar.MILLISECOND,
                999
        );


        /*
         * Future month.
         */

        if (
                selectedYear >
                        today.get(
                                Calendar.YEAR
                        )
                        ||
                        (
                                selectedYear ==
                                        today.get(
                                                Calendar.YEAR
                                        )
                                        &&
                                        selectedMonth >
                                                today.get(
                                                        Calendar.MONTH
                                                ) + 1
                        )
        ) {

            clearSummary();

            monthlyAdapter.notifyDataSetChanged();

            return;
        }


        /*
         * Current month:
         * end = today.
         */

        if (
                selectedYear ==
                        today.get(
                                Calendar.YEAR
                        )
                        &&
                        selectedMonth ==
                                today.get(
                                        Calendar.MONTH
                                ) + 1
        ) {

            end =
                    (Calendar) today.clone();
        }


        /*
         * Generate every elapsed date.
         */

        Calendar loop =
                (Calendar) start.clone();


        int workingDays = 0;

        int present = 0;

        int absent = 0;

        int halfDay = 0;

        int holiday = 0;


        while (
                !loop.after(end)
        ) {

            String dateString =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    ).format(
                            loop.getTime()
                    );


            String status =
                    attendanceMap.get(
                            dateString
                    );


            if (status == null) {

                /*
                 * Attendance not marked.
                 */

                attendanceRecords.add(
                        new AttendanceRecord(
                                convertDisplayDate(
                                        dateString
                                ),
                                "NOT_MARKED"
                        )
                );


            } else {

                // =================================================
                // PRESENT
                // =================================================

                if (
                        status.equals("P")
                ) {

                    present++;

                    workingDays++;
                }


                // =================================================
                // ABSENT
                // =================================================

                else if (
                        status.equals("A")
                ) {

                    absent++;

                    workingDays++;
                }


                // =================================================
                // HALF DAY
                // =================================================

                else if (
                        status.equals("HD")
                ) {

                    halfDay++;

                    workingDays++;
                }


                // =================================================
                // HOLIDAY
                // =================================================

                else if (
                        status.equals("HDY")
                ) {

                    holiday++;
                }


                attendanceRecords.add(
                        new AttendanceRecord(
                                convertDisplayDate(
                                        dateString
                                ),
                                status
                        )
                );
            }


            loop.add(
                    Calendar.DAY_OF_MONTH,
                    1
            );
        }


        /*
         * Sort oldest -> newest
         */

        Collections.sort(
                attendanceRecords,
                Comparator.comparing(
                        AttendanceRecord::getDate
                )
        );


        monthlyAdapter.notifyDataSetChanged();


        // =====================================================
        // SUMMARY
        // =====================================================

        tvWorkingDays.setText(
                String.valueOf(
                        workingDays
                )
        );


        tvPresent.setText(
                String.valueOf(
                        present
                )
        );


        tvAbsent.setText(
                String.valueOf(
                        absent
                )
        );


        tvHalfDay.setText(
                String.valueOf(
                        halfDay
                )
        );


        /*
         * Holiday is intentionally NOT included
         * in working days.
         */


        double percentage = 0;


        if (
                workingDays > 0
        ) {

            percentage =
                    (
                            present
                                    +
                                    (halfDay * 0.5)
                    )
                            /
                            workingDays
                            *
                            100.0;
        }


        tvAttendancePercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "Attendance: %.1f%%",
                        percentage
                )
        );
    }


    // =========================================================
    // PARSE DATE
    // =========================================================

    private Date parseAttendanceDate(
            String dateString
    ) {

        try {

            return attendanceDateFormat.parse(
                    dateString
            );

        } catch (ParseException e) {

            return null;
        }
    }


    // =========================================================
    // DISPLAY DATE
    // =========================================================

    private String convertDisplayDate(
            String dateString
    ) {

        try {

            Date date =
                    attendanceDateFormat.parse(
                            dateString
                    );


            if (date == null) {

                return dateString;
            }


            return new SimpleDateFormat(
                    "dd-MM-yyyy",
                    Locale.getDefault()
            ).format(
                    date
            );

        } catch (Exception e) {

            return dateString;
        }
    }


    // =========================================================
    // NORMALIZE STATUS
    // =========================================================

    private String normalizeStatus(
            String status
    ) {

        if (status == null) {

            return "";
        }


        String value =
                status.trim()
                        .toUpperCase(
                                Locale.getDefault()
                        );


        // =====================================================
        // PRESENT
        // =====================================================

        if (
                value.equals("P") ||
                        value.equals("PRESENT")
        ) {

            return "P";
        }


        // =====================================================
        // ABSENT
        // =====================================================

        if (
                value.equals("A") ||
                        value.equals("ABSENT") ||
                        value.equals("LEAVE") ||
                        value.equals("ABSENT/LEAVE")
        ) {

            return "A";
        }


        // =====================================================
        // HALF DAY
        // =====================================================

        if (
                value.equals("HD") ||
                        value.equals("HALF DAY") ||
                        value.equals("HALFDAY") ||
                        value.equals("HALF_DAY")
        ) {

            return "HD";
        }


        // =====================================================
        // HOLIDAY
        // =====================================================

        if (
                value.equals("HDY") ||
                        value.equals("HOLIDAY")
        ) {

            return "HDY";
        }


        return "";
    }


    // =========================================================
    // DAYS IN MONTH
    // =========================================================

    private int getDaysInMonth(
            int year,
            int month
    ) {

        Calendar calendar =
                Calendar.getInstance();


        calendar.set(
                year,
                month - 1,
                1
        );


        return calendar.getActualMaximum(
                Calendar.DAY_OF_MONTH
        );
    }


    // =========================================================
    // BUILD NAME
    // =========================================================

    private String buildName(
            StaffModel staff
    ) {

        String name =
                (
                        safe(
                                staff.getFirstName()
                        )
                                + " "
                                + safe(
                                staff.getMiddleName()
                        )
                                + " "
                                + safe(
                                staff.getLastName()
                        )
                )
                        .trim();


        if (
                name.isEmpty()
        ) {

            return "Staff / Teacher";
        }


        return name;
    }


    // =========================================================
    // CLEAR SUMMARY
    // =========================================================

    private void clearSummary() {

        tvWorkingDays.setText(
                "0"
        );


        tvPresent.setText(
                "0"
        );


        tvAbsent.setText(
                "0"
        );


        tvHalfDay.setText(
                "0"
        );


        tvAttendancePercentage.setText(
                "Attendance: 0.0%"
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


        if (
                staffListener != null
        ) {

            staffListener.remove();

            staffListener = null;
        }


        if (
                attendanceListener != null
        ) {

            attendanceListener.remove();

            attendanceListener = null;
        }
    }
}