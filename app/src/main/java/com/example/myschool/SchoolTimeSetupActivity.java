package com.example.myschool;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import java.util.Date;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SchoolTimeSetupActivity extends AppCompatActivity {

    private AppCompatImageButton btnBack;

    private EditText etStartTime;
    private EditText etEndTime;
    private EditText etPeriodMinutes;

    private Spinner spinnerBreakCount;
    private Spinner spinnerSaturdayMode;

    private EditText etSaturdayEndTime;
    private EditText etMonthEndEndTime;

    private CheckBox cbMonthEndHalfDay;

    private LinearLayout breakContainer;

    private Button btnSave;

    private FirebaseFirestore db;

    private String staffId = "";
    private String schoolId = "";

    private final List<BreakRow> breakRows =
            new ArrayList<>();

    private static final String[] BREAK_COUNTS = {
            "0",
            "1",
            "2",
            "3"
    };

    private static final String[] SATURDAY_MODES = {
            "Full Day",
            "Half Day",
            "Holiday"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_school_time_setup
        );

        db = FirebaseFirestore.getInstance();

        staffId =
                getIntent().getStringExtra("staffId");

        if (staffId == null) {
            staffId = "";
        }

        initializeViews();
        setupSpinners();
        setupClicks();
        loadTeacherSchool();
    }

    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initializeViews() {

        btnBack =
                findViewById(
                        R.id.btnSchoolTimeBack
                );

        etStartTime =
                findViewById(
                        R.id.etSchoolStartTime
                );

        etEndTime =
                findViewById(
                        R.id.etSchoolEndTime
                );

        etPeriodMinutes =
                findViewById(
                        R.id.etPeriodMinutes
                );

        spinnerBreakCount =
                findViewById(
                        R.id.spinnerBreakCount
                );

        spinnerSaturdayMode =
                findViewById(
                        R.id.spinnerSaturdayMode
                );

        etSaturdayEndTime =
                findViewById(
                        R.id.etSaturdayEndTime
                );

        etMonthEndEndTime =
                findViewById(
                        R.id.etMonthEndEndTime
                );

        cbMonthEndHalfDay =
                findViewById(
                        R.id.cbMonthEndHalfDay
                );

        breakContainer =
                findViewById(
                        R.id.breakContainer
                );

        btnSave =
                findViewById(
                        R.id.btnSaveSchoolTime
                );

        etStartTime.setInputType(
                InputType.TYPE_NULL
        );

        etEndTime.setInputType(
                InputType.TYPE_NULL
        );

        etSaturdayEndTime.setInputType(
                InputType.TYPE_NULL
        );

        etMonthEndEndTime.setInputType(
                InputType.TYPE_NULL
        );
    }

    // =========================================================
    // SPINNERS
    // =========================================================

    private void setupSpinners() {

        ArrayAdapter<String> breakAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        BREAK_COUNTS
                );

        breakAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerBreakCount.setAdapter(
                breakAdapter
        );

        ArrayAdapter<String> saturdayAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        SATURDAY_MODES
                );

        saturdayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerSaturdayMode.setAdapter(
                saturdayAdapter
        );

        spinnerBreakCount.setSelection(0);
        spinnerSaturdayMode.setSelection(0);

        spinnerBreakCount.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        int count =
                                Integer.parseInt(
                                        BREAK_COUNTS[position]
                                );

                        createBreakRows(count);
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        spinnerSaturdayMode.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        String mode =
                                SATURDAY_MODES[position];

                        etSaturdayEndTime.setEnabled(
                                "Half Day".equalsIgnoreCase(mode)
                        );
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

        etStartTime.setOnClickListener(
                v -> showTimePicker(etStartTime)
        );

        etEndTime.setOnClickListener(
                v -> showTimePicker(etEndTime)
        );

        etSaturdayEndTime.setOnClickListener(
                v -> showTimePicker(etSaturdayEndTime)
        );

        etMonthEndEndTime.setOnClickListener(
                v -> showTimePicker(etMonthEndEndTime)
        );

        btnSave.setOnClickListener(
                v -> saveSettings()
        );
    }

    // =========================================================
    // LOAD SCHOOL
    // =========================================================

    private void loadTeacherSchool() {

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
                        doc -> {

                            if (!doc.exists()) {

                                Toast.makeText(
                                        this,
                                        "Teacher profile not found",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            schoolId =
                                    safe(
                                            doc.getString(
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

                            loadExistingSettings();
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                this,
                                "Error: " +
                                        e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // LOAD EXISTING
    // =========================================================

    private void loadExistingSettings() {

        db.collection("schoolTimeSettings")
                .document(schoolId)
                .get()
                .addOnSuccessListener(
                        doc -> {

                            if (!doc.exists()) {
                                return;
                            }

                            String start =
                                    safe(
                                            doc.getString(
                                                    "startTime"
                                            )
                                    );

                            String end =
                                    safe(
                                            doc.getString(
                                                    "endTime"
                                            )
                                    );

                            /*
                             * Legacy support
                             */
                            if (start.isEmpty()) {
                                start =
                                        safe(
                                                doc.getString(
                                                        "schoolStart"
                                                )
                                        );
                            }

                            if (end.isEmpty()) {
                                end =
                                        safe(
                                                doc.getString(
                                                        "schoolEnd"
                                                )
                                        );
                            }

                            if (!start.isEmpty()) {
                                etStartTime.setText(
                                        displayTime(start)
                                );
                            }

                            if (!end.isEmpty()) {
                                etEndTime.setText(
                                        displayTime(end)
                                );
                            }

                            Object periodObject =
                                    doc.get(
                                            "periodMinutes"
                                    );

                            int period =
                                    getInt(
                                            periodObject,
                                            45
                                    );

                            etPeriodMinutes.setText(
                                    String.valueOf(period)
                            );

                            loadBreaks(doc);

                            String saturday =
                                    safe(
                                            doc.getString(
                                                    "saturdayMode"
                                            )
                                    );

                            if (!saturday.isEmpty()) {

                                setSpinnerValue(
                                        spinnerSaturdayMode,
                                        saturday
                                );
                            }

                            String satEnd =
                                    safe(
                                            doc.getString(
                                                    "saturdayEndTime"
                                            )
                                    );

                            if (!satEnd.isEmpty()) {

                                etSaturdayEndTime.setText(
                                        displayTime(satEnd)
                                );
                            }

                            Boolean monthHalf =
                                    doc.getBoolean(
                                            "monthEndHalfDay"
                                    );

                            cbMonthEndHalfDay.setChecked(
                                    Boolean.TRUE.equals(
                                            monthHalf
                                    )
                            );

                            String monthEnd =
                                    safe(
                                            doc.getString(
                                                    "monthEndEndTime"
                                            )
                                    );

                            if (!monthEnd.isEmpty()) {

                                etMonthEndEndTime.setText(
                                        displayTime(monthEnd)
                                );
                            }
                        }
                );
    }

    private void loadBreaks(
            DocumentSnapshot doc
    ) {

        Object object =
                doc.get("breaks");

        if (!(object instanceof List)) {
            createBreakRows(0);
            return;
        }

        List<?> list =
                (List<?>) object;

        if (list.isEmpty()) {
            createBreakRows(0);
            return;
        }

        int count =
                Math.min(
                        list.size(),
                        3
                );

        spinnerBreakCount.setSelection(
                count
        );

        breakContainer.post(
                () -> {

                    for (int i = 0;
                         i < count &&
                                 i < breakRows.size();
                         i++) {

                        Object item =
                                list.get(i);

                        if (!(item instanceof Map)) {
                            continue;
                        }

                        Map<?, ?> map =
                                (Map<?, ?>) item;

                        int after =
                                getInt(
                                        map.get(
                                                "afterPeriod"
                                        ),
                                        i + 1
                                );

                        int minutes =
                                getInt(
                                        map.get(
                                                "minutes"
                                        ),
                                        15
                                );

                        breakRows.get(i)
                                .afterPeriod
                                .setText(
                                        String.valueOf(
                                                after
                                        )
                                );

                        breakRows.get(i)
                                .minutes
                                .setText(
                                        String.valueOf(
                                                minutes
                                        )
                                );
                    }
                }
        );
    }

    // =========================================================
    // BREAK ROWS
    // =========================================================

    private void createBreakRows(
            int count
    ) {

        breakContainer.removeAllViews();
        breakRows.clear();

        for (int i = 0;
             i < count;
             i++) {

            BreakRow row =
                    new BreakRow();

            LinearLayout box =
                    new LinearLayout(this);

            box.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            box.setPadding(
                    dp(8),
                    dp(6),
                    dp(8),
                    dp(6)
            );

            TextView title =
                    new TextView(this);

            title.setText(
                    "Break " + (i + 1)
            );

            title.setTextSize(14);
            title.setTypeface(null, 1);

            EditText after =
                    new EditText(this);

            after.setHint(
                    "After Period"
            );

            after.setInputType(
                    InputType.TYPE_CLASS_NUMBER
            );

            EditText minutes =
                    new EditText(this);

            minutes.setHint(
                    "Minutes"
            );

            minutes.setInputType(
                    InputType.TYPE_CLASS_NUMBER
            );

            LinearLayout.LayoutParams p1 =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(52),
                            1.0f
                    );

            p1.setMargins(
                    0,
                    0,
                    dp(6),
                    0
            );

            LinearLayout.LayoutParams p2 =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(52),
                            1.4f
                    );

            p2.setMargins(
                    0,
                    0,
                    dp(6),
                    0
            );

            LinearLayout.LayoutParams p3 =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(52),
                            1.0f
                    );

            box.addView(
                    title,
                    p1
            );

            box.addView(
                    after,
                    p2
            );

            box.addView(
                    minutes,
                    p3
            );

            breakContainer.addView(
                    box
            );

            row.afterPeriod = after;
            row.minutes = minutes;

            breakRows.add(row);
        }
    }

    // =========================================================
    // SAVE
    // =========================================================

    private void saveSettings() {

        if (schoolId.isEmpty()) {

            Toast.makeText(
                    this,
                    "School ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String start =
                parseTime(
                        etStartTime.getText()
                                .toString()
                                .trim()
                );

        String end =
                parseTime(
                        etEndTime.getText()
                                .toString()
                                .trim()
                );

        if (start.isEmpty()) {

            etStartTime.setError(
                    "Select start time"
            );

            return;
        }

        if (end.isEmpty()) {

            etEndTime.setError(
                    "Select end time"
            );

            return;
        }

        int periodMinutes =
                getInt(
                        etPeriodMinutes
                                .getText()
                                .toString()
                                .trim(),
                        0
                );

        if (periodMinutes <= 0) {

            etPeriodMinutes.setError(
                    "Enter valid minutes"
            );

            return;
        }

        if (!isEndAfterStart(
                start,
                end
        )) {

            Toast.makeText(
                    this,
                    "School end time must be after start time",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        List<Map<String, Object>> breaks =
                new ArrayList<>();

        for (int i = 0;
             i < breakRows.size();
             i++) {

            String afterText =
                    breakRows.get(i)
                            .afterPeriod
                            .getText()
                            .toString()
                            .trim();

            String minutesText =
                    breakRows.get(i)
                            .minutes
                            .getText()
                            .toString()
                            .trim();

            if (afterText.isEmpty()) {

                breakRows.get(i)
                        .afterPeriod
                        .setError(
                                "Required"
                        );

                return;
            }

            if (minutesText.isEmpty()) {

                breakRows.get(i)
                        .minutes
                        .setError(
                                "Required"
                        );

                return;
            }

            int after =
                    getInt(
                            afterText,
                            0
                    );

            int minutes =
                    getInt(
                            minutesText,
                            0
                    );

            if (after <= 0 ||
                    after > 20) {

                breakRows.get(i)
                        .afterPeriod
                        .setError(
                                "Enter 1-20"
                        );

                return;
            }

            if (minutes <= 0) {

                breakRows.get(i)
                        .minutes
                        .setError(
                                "Invalid"
                        );

                return;
            }

            Map<String, Object> map =
                    new HashMap<>();

            map.put(
                    "afterPeriod",
                    after
            );

            map.put(
                    "minutes",
                    minutes
            );

            breaks.add(map);
        }

        String saturdayMode =
                String.valueOf(
                        spinnerSaturdayMode
                                .getSelectedItem()
                );

        String saturdayEnd = "";

        if ("Half Day".equalsIgnoreCase(
                saturdayMode
        )) {

            saturdayEnd =
                    parseTime(
                            etSaturdayEndTime
                                    .getText()
                                    .toString()
                                    .trim()
                    );

            if (saturdayEnd.isEmpty()) {

                etSaturdayEndTime.setError(
                        "Select end time"
                );

                return;
            }
        }

        String monthEnd = "";

        if (cbMonthEndHalfDay.isChecked()) {

            monthEnd =
                    parseTime(
                            etMonthEndEndTime
                                    .getText()
                                    .toString()
                                    .trim()
                    );

            if (monthEnd.isEmpty()) {

                etMonthEndEndTime.setError(
                        "Select end time"
                );

                return;
            }
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "schoolId",
                schoolId
        );

        data.put(
                "startTime",
                start
        );

        data.put(
                "endTime",
                end
        );

        data.put(
                "periodMinutes",
                periodMinutes
        );

        data.put(
                "breaks",
                breaks
        );

        data.put(
                "saturdayMode",
                saturdayMode
        );

        data.put(
                "saturdayEndTime",
                saturdayEnd
        );

        data.put(
                "monthEndHalfDay",
                cbMonthEndHalfDay.isChecked()
        );

        data.put(
                "monthEndEndTime",
                monthEnd
        );

        data.put(
                "updatedBy",
                staffId
        );

        data.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        btnSave.setEnabled(false);

        db.collection("schoolTimeSettings")
                .document(schoolId)
                .set(data)
                .addOnSuccessListener(
                        unused -> {

                            btnSave.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    this,
                                    "School Time Saved Successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            btnSave.setEnabled(
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

    // =========================================================
    // TIME PICKER
    // =========================================================

    private void showTimePicker(
            EditText target
    ) {

        Calendar calendar =
                Calendar.getInstance();

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, hour, minute) -> {

                            String value =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d",
                                            hour,
                                            minute
                                    );

                            target.setText(
                                    displayTime(value)
                            );
                        },
                        calendar.get(
                                Calendar.HOUR_OF_DAY
                        ),
                        calendar.get(
                                Calendar.MINUTE
                        ),
                        false
                );

        dialog.show();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String parseTime(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {
            return "";
        }

        String[] formats = {
                "HH:mm",
                "H:mm",
                "hh:mm a",
                "h:mm a"
        };

        for (String format : formats) {

            try {

                SimpleDateFormat input =
                        new SimpleDateFormat(
                                format,
                                Locale.getDefault()
                        );

                input.setLenient(false);

                Date date =
                        input.parse(
                                value.trim()
                        );

                if (date != null) {

                    return new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    ).format(date);
                }

            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private String displayTime(
            String value
    ) {

        String parsed =
                parseTime(value);

        if (parsed.isEmpty()) {
            return value;
        }

        try {

            Date date =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    ).parse(parsed);

            return new SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
            ).format(date);

        } catch (Exception e) {

            return value;
        }
    }

    private boolean isEndAfterStart(
            String start,
            String end
    ) {

        try {

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    );

            Date s = sdf.parse(start);
            Date e = sdf.parse(end);

            return e != null &&
                    s != null &&
                    e.after(s);

        } catch (Exception e) {

            return false;
        }
    }

    private void setSpinnerValue(
            Spinner spinner,
            String value
    ) {

        ArrayAdapter adapter =
                (ArrayAdapter)
                        spinner.getAdapter();

        if (adapter == null) {
            return;
        }

        for (int i = 0;
             i < adapter.getCount();
             i++) {

            if (String.valueOf(
                    adapter.getItem(i)
            ).equalsIgnoreCase(value)) {

                spinner.setSelection(i);
                return;
            }
        }
    }

    private int getInt(
            Object value,
            int fallback
    ) {

        if (value == null) {
            return fallback;
        }

        try {

            if (value instanceof Number) {

                return ((Number) value)
                        .intValue();
            }

            return Integer.parseInt(
                    String.valueOf(value)
                            .trim()
            );

        } catch (Exception e) {

            return fallback;
        }
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // =========================================================
    // MODEL
    // =========================================================

    private static class BreakRow {

        EditText afterPeriod;
        EditText minutes;
    }
}