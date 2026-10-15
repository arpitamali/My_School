package com.example.myschool;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TeacherTimetableActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private AppCompatImageButton btnBack;
    private Button btnSchoolTimeSetup;
    private Button btnSaveTimetable;

    private TextView tvScheduleInfo;
    private TextView tvNoPeriods;

    private Spinner spinnerClass;
    private Spinner spinnerDivision;
    private Spinner spinnerDay;

    private LinearLayout timetableContainer;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore db;

    // =========================================================
    // TEACHER / SCHOOL
    // =========================================================

    private String staffId = "";
    private String schoolId = "";

    // =========================================================
    // CLASS / DIVISION / DAY
    // =========================================================

    private final List<String> classList =
            new ArrayList<>();

    private final List<String> divisionList =
            new ArrayList<>();

    private final List<String> dayList =
            new ArrayList<>();

    private ArrayAdapter<String> classAdapter;
    private ArrayAdapter<String> divisionAdapter;
    private ArrayAdapter<String> dayAdapter;

    // =========================================================
    // STAFF / TEACHERS
    // =========================================================

    private final List<TeacherOption> teacherList =
            new ArrayList<>();

    // =========================================================
    // SCHOOL TIME
    // =========================================================

    private int periodMinutes = 0;

    private String schoolStartTime = "";
    private String schoolEndTime = "";

    private String saturdayMode = "Full Day";
    private String saturdayEndTime = "";

    private boolean monthEndHalfDay = false;
    private String monthEndEndTime = "";

    private final List<BreakSetting> breakSettings =
            new ArrayList<>();

    // =========================================================
    // TIMETABLE ROWS
    // =========================================================

    private final List<TimetableRow> timetableRows =
            new ArrayList<>();

    private static final int PURPLE =
            Color.rgb(123, 97, 255);

    private static final int LIGHT_PURPLE =
            Color.rgb(239, 235, 255);

    private static final int BORDER =
            Color.rgb(225, 225, 230);

    private static final int TEXT =
            Color.rgb(35, 35, 40);

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_timetable
        );

        db = FirebaseFirestore.getInstance();

        staffId =
                getIntent().getStringExtra(
                        "staffId"
                );

        if (staffId == null) {
            staffId = "";
        }

        initializeViews();
        setupSpinners();
        setupClicks();
        loadTeacher();
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnTimetableBack
                );

        btnSchoolTimeSetup =
                findViewById(
                        R.id.btnSchoolTimeSetup
                );

        btnSaveTimetable =
                findViewById(
                        R.id.btnSaveTimetable
                );

        tvScheduleInfo =
                findViewById(
                        R.id.tvTimetableScheduleInfo
                );

        tvNoPeriods =
                findViewById(
                        R.id.tvNoPeriods
                );

        spinnerClass =
                findViewById(
                        R.id.spinnerFillTimetableClass
                );

        spinnerDivision =
                findViewById(
                        R.id.spinnerFillTimetableDivision
                );

        spinnerDay =
                findViewById(
                        R.id.spinnerFillTimetableDay
                );

        timetableContainer =
                findViewById(
                        R.id.timetableEntryContainer
                );

        btnSaveTimetable.setEnabled(false);
    }

    // =========================================================
    // SPINNERS
    // =========================================================

    private void setupSpinners() {

        classAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        classList
                );

        classAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerClass.setAdapter(
                classAdapter
        );

        divisionAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        divisionList
                );

        divisionAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerDivision.setAdapter(
                divisionAdapter
        );

        dayList.clear();

        dayList.add("Monday");
        dayList.add("Tuesday");
        dayList.add("Wednesday");
        dayList.add("Thursday");
        dayList.add("Friday");
        dayList.add("Saturday");

        dayAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        dayList
                );

        dayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerDay.setAdapter(
                dayAdapter
        );

        // CLASS
        spinnerClass.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        if (position >= 0 &&
                                position < classList.size()) {

                            loadDivisions(
                                    classList.get(position)
                            );
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        // DIVISION
        spinnerDivision.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        refreshTimetableRows();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        // DAY
        spinnerDay.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        refreshTimetableRows();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );
    }

    // =========================================================
    // CLICKS
    // =========================================================

    private void setupClicks() {

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnSchoolTimeSetup.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    TeacherTimetableActivity.this,
                                    SchoolTimeSetupActivity.class
                            );

                    intent.putExtra(
                            "staffId",
                            staffId
                    );

                    startActivity(intent);
                }
        );

        btnSaveTimetable.setOnClickListener(
                v -> saveTimetable()
        );
    }

    // =========================================================
    // LOAD TEACHER
    // =========================================================

    private void loadTeacher() {

        if (staffId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Teacher ID not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (!document.exists()) {

                                Toast.makeText(
                                        this,
                                        "Teacher profile not found",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            schoolId =
                                    safe(
                                            document.getString(
                                                    "schoolId"
                                            )
                                    );

                            if (schoolId.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "School ID not found",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            loadTeachers();
                            loadClasses();
                            loadSchoolTimeSettings();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Teacher load failed: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // LOAD STAFF / TEACHERS
    // =========================================================

    private void loadTeachers() {

        teacherList.clear();

        db.collection("staff")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            for (
                                    DocumentSnapshot doc :
                                    snapshot.getDocuments()
                            ) {

                                String id =
                                        safe(
                                                doc.getString(
                                                        "staffId"
                                                )
                                        );

                                /*
                                 * Usually staff document ID is the
                                 * staff ID. If staffId field exists,
                                 * use it.
                                 */
                                if (id.isEmpty()) {
                                    id = doc.getId();
                                }

                                String name =
                                        buildTeacherName(
                                                doc
                                        );

                                String status =
                                        safe(
                                                doc.getString(
                                                        "status"
                                                )
                                        );

                                if (status.equalsIgnoreCase(
                                        "Deleted"
                                )) {
                                    continue;
                                }

                                if (name.isEmpty()) {
                                    continue;
                                }

                                teacherList.add(
                                        new TeacherOption(
                                                id,
                                                name
                                        )
                                );
                            }

                            Collections.sort(
                                    teacherList,
                                    (a, b) ->
                                            a.name.compareToIgnoreCase(
                                                    b.name
                                            )
                            );

                            /*
                             * Remove duplicate staff IDs.
                             */
                            removeDuplicateTeachers();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Teacher list failed: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void removeDuplicateTeachers() {

        Set<String> used =
                new HashSet<>();

        List<TeacherOption> unique =
                new ArrayList<>();

        for (
                TeacherOption teacher :
                teacherList
        ) {

            if (used.contains(
                    teacher.staffId
            )) {
                continue;
            }

            used.add(
                    teacher.staffId
            );

            unique.add(
                    teacher
            );
        }

        teacherList.clear();
        teacherList.addAll(unique);
    }

    // =========================================================
    // LOAD CLASSES
    // =========================================================

    private void loadClasses() {

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            Set<String> unique =
                                    new HashSet<>();

                            for (
                                    DocumentSnapshot doc :
                                    snapshot.getDocuments()
                            ) {

                                String status =
                                        safe(
                                                doc.getString(
                                                        "status"
                                                )
                                        );

                                if (status.equalsIgnoreCase(
                                        "Deleted"
                                )) {
                                    continue;
                                }

                                String className =
                                        safe(
                                                doc.getString(
                                                        "className"
                                                )
                                        );

                                if (!className.isEmpty()) {
                                    unique.add(
                                            className
                                    );
                                }
                            }

                            List<String> sorted =
                                    new ArrayList<>(
                                            unique
                                    );

                            Collections.sort(
                                    sorted,
                                    this::compareClasses
                            );

                            classList.clear();

                            classList.add(
                                    "Select Class"
                            );

                            classList.addAll(
                                    sorted
                            );

                            classAdapter.notifyDataSetChanged();

                            spinnerClass.setSelection(
                                    0
                            );
                        }
                );
    }

    // =========================================================
    // LOAD DIVISIONS
    // =========================================================

    private void loadDivisions(
            String className
    ) {

        divisionList.clear();

        divisionList.add(
                "Select Division"
        );

        divisionAdapter.notifyDataSetChanged();

        clearTable();

        if (className.isEmpty() ||
                className.equalsIgnoreCase(
                        "Select Class"
                )) {

            tvNoPeriods.setVisibility(
                    View.VISIBLE
            );

            tvNoPeriods.setText(
                    "Select Class"
            );

            btnSaveTimetable.setEnabled(
                    false
            );

            return;
        }

        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            Set<String> unique =
                                    new HashSet<>();

                            for (
                                    DocumentSnapshot doc :
                                    snapshot.getDocuments()
                            ) {

                                String status =
                                        safe(
                                                doc.getString(
                                                        "status"
                                                )
                                        );

                                if (status.equalsIgnoreCase(
                                        "Deleted"
                                )) {
                                    continue;
                                }

                                String studentClass =
                                        safe(
                                                doc.getString(
                                                        "className"
                                                )
                                        );

                                if (!studentClass.equalsIgnoreCase(
                                        className
                                )) {
                                    continue;
                                }

                                String division =
                                        safe(
                                                doc.getString(
                                                        "division"
                                                )
                                        );

                                if (!division.isEmpty()) {
                                    unique.add(
                                            division
                                    );
                                }
                            }

                            List<String> sorted =
                                    new ArrayList<>(
                                            unique
                                    );

                            Collections.sort(
                                    sorted,
                                    String.CASE_INSENSITIVE_ORDER
                            );

                            divisionList.addAll(
                                    sorted
                            );

                            divisionAdapter.notifyDataSetChanged();

                            if (divisionList.size() > 1) {

                                spinnerDivision.setSelection(
                                        1
                                );
                            }
                        }
                );
    }

    // =========================================================
    // SCHOOL TIME
    // =========================================================

    private void loadSchoolTimeSettings() {

        db.collection("schoolTimeSettings")
                .document(schoolId)
                .get()
                .addOnSuccessListener(
                        doc -> {

                            if (!doc.exists()) {

                                tvScheduleInfo.setText(
                                        "Configure School Time first."
                                );

                                return;
                            }

                            schoolStartTime =
                                    safe(
                                            doc.getString(
                                                    "startTime"
                                            )
                                    );

                            schoolEndTime =
                                    safe(
                                            doc.getString(
                                                    "endTime"
                                            )
                                    );

                            periodMinutes =
                                    getIntValue(
                                            doc.get(
                                                    "periodMinutes"
                                            )
                                    );

                            saturdayMode =
                                    safe(
                                            doc.getString(
                                                    "saturdayMode"
                                            )
                                    );

                            if (saturdayMode.isEmpty()) {
                                saturdayMode =
                                        "Full Day";
                            }

                            saturdayEndTime =
                                    safe(
                                            doc.getString(
                                                    "saturdayEndTime"
                                            )
                                    );

                            monthEndHalfDay =
                                    Boolean.TRUE.equals(
                                            doc.getBoolean(
                                                    "monthEndHalfDay"
                                            )
                                    );

                            monthEndEndTime =
                                    safe(
                                            doc.getString(
                                                    "monthEndEndTime"
                                            )
                                    );

                            loadBreaks(
                                    doc
                            );

                            if (periodMinutes <= 0) {

                                tvScheduleInfo.setText(
                                        "Please configure School Time."
                                );

                                return;
                            }

                            updateScheduleInfo();

                            refreshTimetableRows();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "School Time error: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // LOAD BREAKS
    // =========================================================

    private void loadBreaks(
            DocumentSnapshot doc
    ) {

        breakSettings.clear();

        Object object =
                doc.get("breaks");

        if (!(object instanceof List)) {
            return;
        }

        List<?> list =
                (List<?>) object;

        for (
                Object item :
                list
        ) {

            if (!(item instanceof Map)) {
                continue;
            }

            Map<?, ?> map =
                    (Map<?, ?>) item;

            int afterPeriod =
                    getIntValue(
                            map.get(
                                    "afterPeriod"
                            )
                    );

            int minutes =
                    getIntValue(
                            map.get(
                                    "minutes"
                            )
                    );

            if (afterPeriod > 0 &&
                    minutes > 0) {

                breakSettings.add(
                        new BreakSetting(
                                afterPeriod,
                                minutes
                        )
                );
            }
        }

        Collections.sort(
                breakSettings,
                (a, b) ->
                        Integer.compare(
                                a.afterPeriod,
                                b.afterPeriod
                        )
        );
    }

    // =========================================================
    // SCHEDULE INFO
    // =========================================================

    private void updateScheduleInfo() {

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                "School Time: "
        );

        builder.append(
                formatDisplayTime(
                        schoolStartTime
                )
        );

        builder.append(
                " - "
        );

        builder.append(
                formatDisplayTime(
                        schoolEndTime
                )
        );

        builder.append(
                "\nPeriod Duration: "
        );

        builder.append(
                periodMinutes
        );

        builder.append(
                " minutes"
        );

        if (!breakSettings.isEmpty()) {

            builder.append(
                    "\nBreaks: "
            );

            for (
                    int i = 0;
                    i < breakSettings.size();
                    i++
            ) {

                if (i > 0) {
                    builder.append(
                            " • "
                    );
                }

                BreakSetting setting =
                        breakSettings.get(i);

                builder.append(
                        "Break "
                );

                builder.append(
                        i + 1
                );

                builder.append(
                        " after P"
                );

                builder.append(
                        setting.afterPeriod
                );

                builder.append(
                        " ("
                );

                builder.append(
                        setting.minutes
                );

                builder.append(
                        " min)"
                );
            }
        }

        tvScheduleInfo.setText(
                builder.toString()
        );
    }

    // =========================================================
    // REFRESH TABLE
    // =========================================================

    private void refreshTimetableRows() {

        clearTable();

        if (periodMinutes <= 0 ||
                schoolStartTime.isEmpty() ||
                schoolEndTime.isEmpty()) {

            tvNoPeriods.setVisibility(
                    View.VISIBLE
            );

            tvNoPeriods.setText(
                    "Configure School Time first."
            );

            return;
        }

        String selectedClass =
                getSpinnerValue(
                        spinnerClass
                );

        String selectedDivision =
                getSpinnerValue(
                        spinnerDivision
                );

        String selectedDay =
                getSpinnerValue(
                        spinnerDay
                );

        if (selectedClass.isEmpty() ||
                selectedClass.equalsIgnoreCase(
                        "Select Class"
                )) {

            tvNoPeriods.setVisibility(
                    View.VISIBLE
            );

            tvNoPeriods.setText(
                    "Select Class"
            );

            return;
        }

        if (selectedDivision.isEmpty() ||
                selectedDivision.equalsIgnoreCase(
                        "Select Division"
                )) {

            tvNoPeriods.setVisibility(
                    View.VISIBLE
            );

            tvNoPeriods.setText(
                    "Select Division"
            );

            return;
        }

        if (selectedDay.isEmpty()) {

            tvNoPeriods.setVisibility(
                    View.VISIBLE
            );

            tvNoPeriods.setText(
                    "Select Day"
            );

            return;
        }

        // Saturday Holiday
        if (selectedDay.equalsIgnoreCase(
                "Saturday"
        )
                &&
                saturdayMode.equalsIgnoreCase(
                        "Holiday"
                )) {

            addHolidayRow(
                    "Saturday - Holiday"
            );

            return;
        }

        tvNoPeriods.setVisibility(
                View.GONE
        );

        addTableHeader();

        String effectiveEnd =
                getEffectiveEndTime(
                        selectedDay
                );

        int current =
                toMinutes(
                        schoolStartTime
                );

        int end =
                toMinutes(
                        effectiveEnd
                );

        if (current < 0 ||
                end <= current) {

            tvNoPeriods.setVisibility(
                    View.VISIBLE
            );

            tvNoPeriods.setText(
                    "Invalid School Time."
            );

            return;
        }

        int periodNumber = 1;

        while (
                current < end &&
                        periodNumber <= 30
        ) {

            // BREAK
            if (periodNumber > 1) {

                BreakSetting breakSetting =
                        findBreakAfter(
                                periodNumber - 1
                        );

                if (breakSetting != null) {

                    int breakStart =
                            current;

                    int breakEnd =
                            current
                                    + breakSetting.minutes;

                    if (breakEnd <= end) {

                        addBreakRow(
                                breakSetting,
                                breakStart,
                                breakEnd
                        );

                        current =
                                breakEnd;
                    }
                }
            }

            int periodStart =
                    current;

            int periodEnd =
                    current
                            + periodMinutes;

            if (periodEnd > end) {
                break;
            }

            TimetableRow row =
                    addPeriodRow(
                            periodNumber,
                            periodStart,
                            periodEnd
                    );

            timetableRows.add(
                    row
            );

            current =
                    periodEnd;

            periodNumber++;
        }

        btnSaveTimetable.setEnabled(
                !timetableRows.isEmpty()
        );

        /*
         * IMPORTANT:
         * After creating empty rows,
         * load saved Firebase data.
         */
        loadSavedTimetable(
                selectedClass,
                selectedDivision,
                selectedDay
        );
    }

    // =========================================================
    // EFFECTIVE END TIME
    // =========================================================

    private String getEffectiveEndTime(
            String selectedDay
    ) {

        String result =
                schoolEndTime;

        if (selectedDay.equalsIgnoreCase(
                "Saturday"
        )) {

            if (saturdayMode.equalsIgnoreCase(
                    "Half Day"
            )
                    &&
                    !saturdayEndTime.isEmpty()) {

                result =
                        saturdayEndTime;
            }
        }

        Calendar calendar =
                Calendar.getInstance();

        int lastDay =
                calendar.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                );

        String today =
                new SimpleDateFormat(
                        "EEEE",
                        Locale.ENGLISH
                ).format(
                        new Date()
                );

        if (monthEndHalfDay &&
                selectedDay.equalsIgnoreCase(
                        today
                )
                &&
                calendar.get(
                        Calendar.DAY_OF_MONTH
                ) == lastDay
                &&
                !monthEndEndTime.isEmpty()) {

            result =
                    monthEndEndTime;
        }

        return result;
    }

    // =========================================================
    // TABLE HEADER
    // =========================================================

    private void addTableHeader() {

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                dp(5),
                0,
                dp(5),
                0
        );

        header.setBackgroundColor(
                PURPLE
        );

        header.addView(
                createHeader(
                        "PERIOD",
                        0.8f
                )
        );

        header.addView(
                createHeader(
                        "TIME",
                        1.5f
                )
        );

        header.addView(
                createHeader(
                        "SUBJECT",
                        2.1f
                )
        );

        header.addView(
                createHeader(
                        "TEACHER",
                        2.1f
                )
        );

        timetableContainer.addView(
                header
        );
    }

    private TextView createHeader(
            String text,
            float weight
    ) {

        TextView view =
                new TextView(this);

        view.setText(
                text
        );

        view.setTextColor(
                Color.WHITE
        );

        view.setTextSize(
                9
        );

        view.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        view.setGravity(
                Gravity.CENTER
        );

        view.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        weight
                )
        );

        return view;
    }

    // =========================================================
    // ADD PERIOD ROW
    // =========================================================

    private TimetableRow addPeriodRow(
            int periodNumber,
            int startMinutes,
            int endMinutes
    ) {

        TimetableRow row =
                new TimetableRow();

        row.periodNumber =
                periodNumber;

        row.isBreak =
                false;

        row.startTime =
                minutesToTime(
                        startMinutes
                );

        row.endTime =
                minutesToTime(
                        endMinutes
                );

        LinearLayout rowLayout =
                new LinearLayout(this);

        rowLayout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        rowLayout.setGravity(
                Gravity.CENTER_VERTICAL
        );

        rowLayout.setPadding(
                dp(4),
                dp(3),
                dp(4),
                dp(3)
        );

        rowLayout.setBackgroundColor(
                Color.WHITE
        );

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(66)
                );

        rowParams.setMargins(
                0,
                1,
                0,
                1
        );

        rowLayout.setLayoutParams(
                rowParams
        );

        // -----------------------------------------------------
        // PERIOD
        // -----------------------------------------------------

        TextView period =
                new TextView(this);

        period.setText(
                "P" + periodNumber
        );

        period.setTextColor(
                PURPLE
        );

        period.setTextSize(
                12
        );

        period.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        period.setGravity(
                Gravity.CENTER
        );

        rowLayout.addView(
                period,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.8f
                )
        );

        // -----------------------------------------------------
        // TIME
        // -----------------------------------------------------

        TextView time =
                new TextView(this);

        time.setText(
                formatDisplayTime(
                        row.startTime
                )
                        + "\n-\n"
                        +
                        formatDisplayTime(
                                row.endTime
                        )
        );

        time.setTextColor(
                Color.DKGRAY
        );

        time.setTextSize(
                8
        );

        time.setGravity(
                Gravity.CENTER
        );

        rowLayout.addView(
                time,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.5f
                )
        );

        // -----------------------------------------------------
        // SUBJECT
        // -----------------------------------------------------

        EditText subject =
                new EditText(this);

        subject.setHint(
                "Subject"
        );

        subject.setTextSize(
                11
        );

        subject.setSingleLine(
                true
        );

        subject.setInputType(
                InputType.TYPE_CLASS_TEXT
                        |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        subject.setPadding(
                dp(5),
                0,
                dp(5),
                0
        );

        subject.setBackgroundColor(
                Color.rgb(
                        248,
                        248,
                        250
                )
        );

        row.subjectEditText =
                subject;

        rowLayout.addView(
                subject,
                new LinearLayout.LayoutParams(
                        0,
                        dp(46),
                        2.1f
                )
        );

        // -----------------------------------------------------
        // TEACHER SPINNER
        // -----------------------------------------------------

        Spinner teacherSpinner =
                new Spinner(this);

        ArrayList<String> teacherNames =
                new ArrayList<>();

        teacherNames.add(
                "Select Teacher"
        );

        for (
                TeacherOption teacher :
                teacherList
        ) {

            teacherNames.add(
                    teacher.name
            );
        }

        ArrayAdapter<String> teacherAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        teacherNames
                );

        teacherAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        teacherSpinner.setAdapter(
                teacherAdapter
        );

        row.teacherSpinner =
                teacherSpinner;

        rowLayout.addView(
                teacherSpinner,
                new LinearLayout.LayoutParams(
                        0,
                        dp(46),
                        2.1f
                )
        );

        timetableContainer.addView(
                rowLayout
        );

        return row;
    }

    // =========================================================
    // BREAK ROW
    // =========================================================

    private void addBreakRow(
            BreakSetting setting,
            int startMinutes,
            int endMinutes
    ) {

        LinearLayout breakRow =
                new LinearLayout(this);

        breakRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        breakRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        breakRow.setPadding(
                dp(8),
                0,
                dp(8),
                0
        );

        breakRow.setBackgroundColor(
                LIGHT_PURPLE
        );

        TextView breakName =
                new TextView(this);

        breakName.setText(
                "☕ Break "
                        + setting.afterPeriod
        );

        breakName.setTextSize(
                10
        );

        breakName.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        breakName.setTextColor(
                Color.rgb(
                        75,
                        58,
                        168
                )
        );

        breakName.setGravity(
                Gravity.CENTER
        );

        breakRow.addView(
                breakName,
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1.5f
                )
        );

        TextView time =
                new TextView(this);

        time.setText(
                formatDisplayTime(
                        minutesToTime(
                                startMinutes
                        )
                )
                        + " - "
                        +
                        formatDisplayTime(
                                minutesToTime(
                                        endMinutes
                                )
                        )
        );

        time.setTextSize(
                9
        );

        time.setGravity(
                Gravity.CENTER
        );

        breakRow.addView(
                time,
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1.5f
                )
        );

        TextView duration =
                new TextView(this);

        duration.setText(
                setting.minutes
                        + " min"
        );

        duration.setTextSize(
                9
        );

        duration.setGravity(
                Gravity.CENTER
        );

        breakRow.addView(
                duration,
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1.0f
                )
        );

        timetableContainer.addView(
                breakRow
        );
    }

    // =========================================================
    // HOLIDAY
    // =========================================================

    private void addHolidayRow(
            String text
    ) {

        TextView holiday =
                new TextView(this);

        holiday.setText(
                text
        );

        holiday.setTextSize(
                18
        );

        holiday.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        holiday.setGravity(
                Gravity.CENTER
        );

        holiday.setPadding(
                dp(20),
                dp(40),
                dp(20),
                dp(40)
        );

        timetableContainer.addView(
                holiday
        );

        btnSaveTimetable.setEnabled(
                true
        );
    }

    // =========================================================
    // LOAD SAVED TIMETABLE
    // =========================================================

    private void loadSavedTimetable(
            String className,
            String division,
            String day
    ) {

        if (schoolId.isEmpty()) {
            return;
        }

        db.collection("timetables")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            for (
                                    DocumentSnapshot doc :
                                    snapshot.getDocuments()
                            ) {

                                String savedClass =
                                        safe(
                                                doc.getString(
                                                        "className"
                                                )
                                        );

                                String savedDivision =
                                        safe(
                                                doc.getString(
                                                        "division"
                                                )
                                        );

                                String savedDay =
                                        safe(
                                                doc.getString(
                                                        "day"
                                                )
                                        );

                                if (!savedClass.equalsIgnoreCase(
                                        className
                                )) {
                                    continue;
                                }

                                if (!savedDivision.equalsIgnoreCase(
                                        division
                                )) {
                                    continue;
                                }

                                if (!savedDay.equalsIgnoreCase(
                                        day
                                )) {
                                    continue;
                                }

                                boolean recess =
                                        Boolean.TRUE.equals(
                                                doc.getBoolean(
                                                        "recess"
                                                )
                                        );

                                if (recess) {
                                    continue;
                                }

                                int period =
                                        getIntValue(
                                                doc.get(
                                                        "periodNumber"
                                                )
                                        );

                                if (period <= 0) {
                                    continue;
                                }

                                String subject =
                                        safe(
                                                doc.getString(
                                                        "subject"
                                                )
                                        );

                                String savedTeacherId =
                                        safe(
                                                doc.getString(
                                                        "teacherId"
                                                )
                                        );

                                for (
                                        TimetableRow row :
                                        timetableRows
                                ) {

                                    if (row.periodNumber !=
                                            period) {
                                        continue;
                                    }

                                    row.subjectEditText.setText(
                                            subject
                                    );

                                    selectTeacher(
                                            row.teacherSpinner,
                                            savedTeacherId
                                    );

                                    break;
                                }
                            }
                        }
                );
    }

    // =========================================================
    // SELECT TEACHER IN SPINNER
    // =========================================================

    private void selectTeacher(
            Spinner spinner,
            String staffId
    ) {

        if (spinner == null ||
                staffId.isEmpty()) {
            return;
        }

        for (
                int i = 0;
                i < teacherList.size();
                i++
        ) {

            TeacherOption teacher =
                    teacherList.get(i);

            if (teacher.staffId.equals(
                    staffId
            )) {

                /*
                 * +1 because first item is
                 * "Select Teacher".
                 */
                spinner.setSelection(
                        i + 1
                );

                return;
            }
        }
    }

    // =========================================================
    // SAVE TIMETABLE
    // =========================================================

    private void saveTimetable() {

        String selectedClass =
                getSpinnerValue(
                        spinnerClass
                );

        String selectedDivision =
                getSpinnerValue(
                        spinnerDivision
                );

        String selectedDay =
                getSpinnerValue(
                        spinnerDay
                );

        if (selectedClass.isEmpty() ||
                selectedClass.equalsIgnoreCase(
                        "Select Class"
                )) {

            Toast.makeText(
                    this,
                    "Select Class",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (selectedDivision.isEmpty() ||
                selectedDivision.equalsIgnoreCase(
                        "Select Division"
                )) {

            Toast.makeText(
                    this,
                    "Select Division",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (selectedDay.isEmpty()) {

            Toast.makeText(
                    this,
                    "Select Day",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // SATURDAY HOLIDAY
        if (selectedDay.equalsIgnoreCase(
                "Saturday"
        )
                &&
                saturdayMode.equalsIgnoreCase(
                        "Holiday"
                )) {

            saveSaturdayHoliday(
                    selectedClass,
                    selectedDivision,
                    selectedDay
            );

            return;
        }

        // VALIDATE
        for (
                TimetableRow row :
                timetableRows
        ) {

            String subject =
                    row.subjectEditText
                            .getText()
                            .toString()
                            .trim();

            int teacherPosition =
                    row.teacherSpinner
                            .getSelectedItemPosition();

            if (subject.isEmpty()) {

                row.subjectEditText.setError(
                        "Enter subject"
                );

                row.subjectEditText.requestFocus();

                return;
            }

            if (teacherPosition <= 0 ||
                    teacherPosition >
                            teacherList.size()) {

                Toast.makeText(
                        this,
                        "Select teacher for P"
                                + row.periodNumber,
                        Toast.LENGTH_SHORT
                ).show();

                row.teacherSpinner.requestFocus();

                return;
            }
        }

        btnSaveTimetable.setEnabled(
                false
        );

        db.collection("timetables")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            WriteBatch batch =
                                    db.batch();

                            // DELETE OLD
                            for (
                                    DocumentSnapshot doc :
                                    snapshot.getDocuments()
                            ) {

                                String oldClass =
                                        safe(
                                                doc.getString(
                                                        "className"
                                                )
                                        );

                                String oldDivision =
                                        safe(
                                                doc.getString(
                                                        "division"
                                                )
                                        );

                                String oldDay =
                                        safe(
                                                doc.getString(
                                                        "day"
                                                )
                                        );

                                if (oldClass.equalsIgnoreCase(
                                        selectedClass
                                )
                                        &&
                                        oldDivision.equalsIgnoreCase(
                                                selectedDivision
                                        )
                                        &&
                                        oldDay.equalsIgnoreCase(
                                                selectedDay
                                        )) {

                                    batch.delete(
                                            doc.getReference()
                                    );
                                }
                            }

                            // SAVE NEW
                            for (
                                    TimetableRow row :
                                    timetableRows
                            ) {

                                String subject =
                                        row.subjectEditText
                                                .getText()
                                                .toString()
                                                .trim();

                                int position =
                                        row.teacherSpinner
                                                .getSelectedItemPosition();

                                TeacherOption teacher =
                                        teacherList.get(
                                                position - 1
                                        );

                                Map<String, Object> data =
                                        new HashMap<>();

                                data.put(
                                        "schoolId",
                                        schoolId
                                );

                                /*
                                 * VERY IMPORTANT:
                                 * Actual selected teacher's
                                 * staffId is saved.
                                 */
                                data.put(
                                        "teacherId",
                                        teacher.staffId
                                );

                                data.put(
                                        "teacherName",
                                        teacher.name
                                );

                                data.put(
                                        "className",
                                        selectedClass
                                );

                                data.put(
                                        "division",
                                        selectedDivision
                                );

                                data.put(
                                        "subject",
                                        subject
                                );

                                data.put(
                                        "startTime",
                                        row.startTime
                                );

                                data.put(
                                        "endTime",
                                        row.endTime
                                );

                                data.put(
                                        "day",
                                        selectedDay
                                );

                                data.put(
                                        "periodNumber",
                                        row.periodNumber
                                );

                                data.put(
                                        "recess",
                                        false
                                );

                                data.put(
                                        "breakMinutes",
                                        0
                                );

                                data.put(
                                        "createdBy",
                                        staffId
                                );

                                data.put(
                                        "createdAt",
                                        FieldValue.serverTimestamp()
                                );

                                batch.set(
                                        db.collection(
                                                "timetables"
                                        ).document(),
                                        data
                                );
                            }

                            batch.commit()
                                    .addOnSuccessListener(
                                            unused -> {

                                                btnSaveTimetable
                                                        .setEnabled(
                                                                true
                                                        );

                                                Toast.makeText(
                                                        this,
                                                        "Timetable Saved Successfully",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                /*
                                                 * Reload the exact
                                                 * saved table.
                                                 */
                                                refreshTimetableRows();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                btnSaveTimetable
                                                        .setEnabled(
                                                                true
                                                        );

                                                Toast.makeText(
                                                        this,
                                                        "Save failed: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            btnSaveTimetable
                                    .setEnabled(
                                            true
                                    );

                            Toast.makeText(
                                    this,
                                    "Unable to update timetable: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // SATURDAY HOLIDAY
    // =========================================================

    private void saveSaturdayHoliday(
            String className,
            String division,
            String day
    ) {

        btnSaveTimetable.setEnabled(
                false
        );

        db.collection("timetables")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            WriteBatch batch =
                                    db.batch();

                            for (
                                    DocumentSnapshot doc :
                                    snapshot.getDocuments()
                            ) {

                                String oldClass =
                                        safe(
                                                doc.getString(
                                                        "className"
                                                )
                                        );

                                String oldDivision =
                                        safe(
                                                doc.getString(
                                                        "division"
                                                )
                                        );

                                String oldDay =
                                        safe(
                                                doc.getString(
                                                        "day"
                                                )
                                        );

                                if (oldClass.equalsIgnoreCase(
                                        className
                                )
                                        &&
                                        oldDivision.equalsIgnoreCase(
                                                division
                                        )
                                        &&
                                        oldDay.equalsIgnoreCase(
                                                day
                                        )) {

                                    batch.delete(
                                            doc.getReference()
                                    );
                                }
                            }

                            Map<String, Object> data =
                                    new HashMap<>();

                            data.put(
                                    "schoolId",
                                    schoolId
                            );

                            data.put(
                                    "teacherId",
                                    staffId
                            );

                            data.put(
                                    "teacherName",
                                    ""
                            );

                            data.put(
                                    "className",
                                    className
                            );

                            data.put(
                                    "division",
                                    division
                            );

                            data.put(
                                    "subject",
                                    "Holiday"
                            );

                            data.put(
                                    "startTime",
                                    ""
                            );

                            data.put(
                                    "endTime",
                                    ""
                            );

                            data.put(
                                    "day",
                                    day
                            );

                            data.put(
                                    "periodNumber",
                                    0
                            );

                            data.put(
                                    "recess",
                                    false
                            );

                            data.put(
                                    "breakMinutes",
                                    0
                            );

                            data.put(
                                    "createdBy",
                                    staffId
                            );

                            data.put(
                                    "createdAt",
                                    FieldValue.serverTimestamp()
                            );

                            batch.set(
                                    db.collection(
                                            "timetables"
                                    ).document(),
                                    data
                            );

                            batch.commit()
                                    .addOnSuccessListener(
                                            unused -> {

                                                btnSaveTimetable
                                                        .setEnabled(
                                                                true
                                                        );

                                                Toast.makeText(
                                                        this,
                                                        "Saturday Holiday Saved",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                btnSaveTimetable
                                                        .setEnabled(
                                                                true
                                                        );

                                                Toast.makeText(
                                                        this,
                                                        "Save failed: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                );
    }

    // =========================================================
    // CLEAR TABLE
    // =========================================================

    private void clearTable() {

        timetableContainer.removeAllViews();

        timetableRows.clear();

        tvNoPeriods.setVisibility(
                View.VISIBLE
        );

        tvNoPeriods.setText(
                "Select Class and Division"
        );

        btnSaveTimetable.setEnabled(
                false
        );
    }

    // =========================================================
    // FIND BREAK
    // =========================================================

    private BreakSetting findBreakAfter(
            int period
    ) {

        for (
                BreakSetting setting :
                breakSettings
        ) {

            if (setting.afterPeriod ==
                    period) {

                return setting;
            }
        }

        return null;
    }

    // =========================================================
    // GET SPINNER VALUE
    // =========================================================

    private String getSpinnerValue(
            Spinner spinner
    ) {

        if (spinner == null ||
                spinner.getSelectedItem() == null) {

            return "";
        }

        return spinner
                .getSelectedItem()
                .toString()
                .trim();
    }

    // =========================================================
    // INTEGER
    // =========================================================

    private int getIntValue(
            Object value
    ) {

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {

            return ((Number) value)
                    .intValue();
        }

        try {

            return Integer.parseInt(
                    String.valueOf(
                            value
                    ).trim()
            );

        } catch (Exception e) {

            return 0;
        }
    }

    // =========================================================
    // TIME TO MINUTES
    // =========================================================

    private int toMinutes(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return -1;
        }

        try {

            String[] parts =
                    value.split(":");

            if (parts.length != 2) {
                return -1;
            }

            int hour =
                    Integer.parseInt(
                            parts[0]
                    );

            int minute =
                    Integer.parseInt(
                            parts[1]
                    );

            if (hour < 0 ||
                    hour > 23 ||
                    minute < 0 ||
                    minute > 59) {

                return -1;
            }

            return hour * 60 + minute;

        } catch (Exception e) {

            return -1;
        }
    }

    // =========================================================
    // MINUTES TO TIME
    // =========================================================

    private String minutesToTime(
            int totalMinutes
    ) {

        int hour =
                totalMinutes / 60;

        int minute =
                totalMinutes % 60;

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                hour,
                minute
        );
    }

    // =========================================================
    // DISPLAY TIME
    // =========================================================

    private String formatDisplayTime(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "";
        }

        try {

            SimpleDateFormat input =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.ENGLISH
                    );

            input.setLenient(false);

            SimpleDateFormat output =
                    new SimpleDateFormat(
                            "hh:mm a",
                            Locale.ENGLISH
                    );

            Date date =
                    input.parse(
                            value
                    );

            if (date == null) {
                return value;
            }

            return output.format(
                    date
            );

        } catch (Exception e) {

            return value;
        }
    }

    // =========================================================
    // CLASS SORT
    // =========================================================

    private int compareClasses(
            String a,
            String b
    ) {

        int first =
                extractNumber(a);

        int second =
                extractNumber(b);

        if (first != second) {

            return Integer.compare(
                    first,
                    second
            );
        }

        return a.compareToIgnoreCase(
                b
        );
    }

    private int extractNumber(
            String value
    ) {

        if (value == null) {
            return 999;
        }

        StringBuilder number =
                new StringBuilder();

        for (
                int i = 0;
                i < value.length();
                i++
        ) {

            char c =
                    value.charAt(i);

            if (Character.isDigit(c)) {

                number.append(c);
            }
        }

        if (number.length() == 0) {
            return 999;
        }

        try {

            return Integer.parseInt(
                    number.toString()
            );

        } catch (Exception e) {

            return 999;
        }
    }

    // =========================================================
    // TEACHER NAME
    // =========================================================

    private String buildTeacherName(
            DocumentSnapshot document
    ) {

        String first =
                safe(
                        document.getString(
                                "firstName"
                        )
                );

        String middle =
                safe(
                        document.getString(
                                "middleName"
                        )
                );

        String last =
                safe(
                        document.getString(
                                "lastName"
                        )
                );

        String full =
                (
                        first
                                + " "
                                + middle
                                + " "
                                + last
                )
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        if (full.isEmpty()) {

            full =
                    safe(
                            document.getString(
                                    "name"
                            )
                    );
        }

        if (full.isEmpty()) {

            full =
                    safe(
                            document.getString(
                                    "teacherName"
                            )
                    );
        }

        return full;
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

    // =========================================================
    // DP
    // =========================================================

    private int dp(
            int value
    ) {

        return (int) (
                value
                        *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // =========================================================
    // MODELS
    // =========================================================

    private static class TeacherOption {

        String staffId;
        String name;

        TeacherOption(
                String staffId,
                String name
        ) {

            this.staffId =
                    staffId;

            this.name =
                    name;
        }
    }

    private static class BreakSetting {

        int afterPeriod;
        int minutes;

        BreakSetting(
                int afterPeriod,
                int minutes
        ) {

            this.afterPeriod =
                    afterPeriod;

            this.minutes =
                    minutes;
        }
    }

    private static class TimetableRow {

        int periodNumber;

        boolean isBreak;

        String startTime;
        String endTime;

        EditText subjectEditText;

        Spinner teacherSpinner;
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (db != null &&
                !schoolId.isEmpty()) {

            loadTeachers();
            loadSchoolTimeSettings();
        }
    }
}