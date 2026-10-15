package com.example.myschool;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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

public class TeacherHomeFragment extends Fragment {

    private TextView tvHelloTeacher;
    private TextView tvTeacherSubtitle;
    private TextView tvHomeAssignedClass;
    private TextView tvHomeStudentCount;
    private TextView tvHomeAttendance;

    private ImageView ivTeacherHomeProfile;

    private LinearLayout layoutTodaySchedule;

    private FirebaseFirestore db;

    private String staffId = "";
    private String schoolId = "";
    private String assignedClass = "";
    private String assignedDivision = "";

    private ListenerRegistration studentListener;
    private ListenerRegistration attendanceListener;
    private ListenerRegistration timetableListener;

    private static final int COLOR_PRIMARY = Color.rgb(32, 33, 36);
    private static final int COLOR_SECONDARY = Color.rgb(133, 138, 147);
    private static final int COLOR_DIVIDER = Color.rgb(238, 238, 238);

    // =========================================================
    // SCHEDULE MODELS
    // =========================================================

    private static class BreakItem {

        int afterPeriod;
        int durationMinutes;
        int breakOrder;

        String label;
        String icon;

        int startMinutes;
        int endMinutes;

        BreakItem(
                int afterPeriod,
                int durationMinutes,
                int breakOrder,
                String label,
                String icon
        ) {
            this.afterPeriod = afterPeriod;
            this.durationMinutes = durationMinutes;
            this.breakOrder = breakOrder;
            this.label = label;
            this.icon = icon;
        }
    }

    private static class ScheduleItem {

        int periodNumber;

        boolean isBreak;

        DocumentSnapshot periodDocument;

        BreakItem breakItem;

        double orderKey;

        static ScheduleItem period(
                int periodNumber,
                DocumentSnapshot document
        ) {
            ScheduleItem item = new ScheduleItem();

            item.periodNumber = periodNumber;
            item.periodDocument = document;
            item.isBreak = false;
            item.orderKey = periodNumber;

            return item;
        }

        static ScheduleItem breakRow(BreakItem breakItem) {
            ScheduleItem item = new ScheduleItem();

            item.breakItem = breakItem;
            item.isBreak = true;

            // Break always appears after its configured period.
            item.orderKey =
                    breakItem.afterPeriod
                            + 0.5
                            + (breakItem.breakOrder * 0.001);

            return item;
        }
    }

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_teacher_home,
                container,
                false
        );

        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupClicks(view);
        loadTeacherData();

        return view;
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews(View view) {

        tvHelloTeacher =
                view.findViewById(R.id.tvHelloTeacher);

        tvTeacherSubtitle =
                view.findViewById(R.id.tvTeacherSubtitle);

        tvHomeAssignedClass =
                view.findViewById(R.id.tvHomeAssignedClass);

        tvHomeStudentCount =
                view.findViewById(R.id.tvHomeStudentCount);

        tvHomeAttendance =
                view.findViewById(R.id.tvHomeAttendance);

        ivTeacherHomeProfile =
                view.findViewById(R.id.ivTeacherHomeProfile);

        layoutTodaySchedule =
                view.findViewById(R.id.layoutTodaySchedule);
    }

    // =========================================================
    // CLICK ACTIONS
    // =========================================================

    private void setupClicks(View view) {

        View attendance =
                view.findViewById(R.id.cardMarkAttendance);

        if (attendance != null) {
            attendance.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherStudentAttendanceActivity.class
                );

                intent.putExtra("staffId", staffId);
                intent.putExtra("attendanceMode", "quick_action");

                startActivity(intent);
            });
        }

        View attendanceSummary =
                view.findViewById(R.id.cardTodayAttendance);

        if (attendanceSummary != null) {
            attendanceSummary.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherStudentAttendanceActivity.class
                );

                intent.putExtra("staffId", staffId);
                intent.putExtra("attendanceMode", "my_class");

                startActivity(intent);
            });
        }

        View homework =
                view.findViewById(R.id.cardHomework);

        if (homework != null) {
            homework.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherHomeworkActivity.class
                );

                intent.putExtra("staffId", staffId);

                startActivity(intent);
            });
        }

        View assignment =
                view.findViewById(R.id.cardAssignment);

        if (assignment != null) {
            assignment.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherAssignmentActivity.class
                );

                intent.putExtra("staffId", staffId);

                startActivity(intent);
            });
        }

        View conductTest = view.findViewById(R.id.cardConductTest);

        if (conductTest != null) {
            conductTest.setOnClickListener(v -> {
                Intent intent = new Intent(
                        requireContext(),
                        TeacherConductTestActivity.class
                );

                intent.putExtra("staffId", staffId);

                startActivity(intent);
            });
        }
        View fillMarks =
                view.findViewById(R.id.cardFillMarks);

        if (fillMarks != null) {
            fillMarks.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherFillMarksActivity.class
                );

                intent.putExtra("staffId", staffId);

                startActivity(intent);
            });
        }

        View timetable =
                view.findViewById(R.id.cardTimetable);

        if (timetable != null) {
            timetable.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherTimetableActivity.class
                );

                intent.putExtra("staffId", staffId);

                startActivity(intent);
            });
        }

        View notice =
                view.findViewById(R.id.cardNotice);

        if (notice != null) {
            notice.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherNoticeActivity.class
                );

                intent.putExtra("staffId", staffId);

                startActivity(intent);
            });
        }

        View seeAllNotice =
                view.findViewById(R.id.cardSeeAllNotice);

        if (seeAllNotice != null) {
            seeAllNotice.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherAllNoticeActivity.class
                );

                intent.putExtra("staffId", staffId);

                startActivity(intent);
            });
        }

        View parentMessages =
                view.findViewById(R.id.cardParentMessages);

        if (parentMessages != null) {
            parentMessages.setOnClickListener(v -> {

                Intent intent = new Intent(
                        requireContext(),
                        TeacherParentMessagesActivity.class
                );

                intent.putExtra("staffId", staffId);

                startActivity(intent);
            });
        }
    }

    // =========================================================
    // LOAD TEACHER DATA
    // =========================================================

    private void loadTeacherData() {

        if (getActivity() == null) {
            return;
        }

        staffId = safe(
                requireActivity()
                        .getIntent()
                        .getStringExtra("staffId")
        );

        if (staffId.isEmpty()) {

            tvHomeAssignedClass.setText("Not Assigned");
            tvHomeStudentCount.setText("0");

            renderEmptySchedule("Teacher information not available");

            return;
        }

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(staff -> {

                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    if (!staff.exists()) {

                        tvHomeAssignedClass.setText("Not Assigned");
                        tvHomeStudentCount.setText("0");

                        renderEmptySchedule("Teacher not found");

                        return;
                    }

                    String firstName =
                            safe(staff.getString("firstName"));

                    String middleName =
                            safe(staff.getString("middleName"));

                    String lastName =
                            safe(staff.getString("lastName"));

                    String teacherName = buildFullName(
                            firstName,
                            middleName,
                            lastName
                    );

                    if (teacherName.isEmpty()) {
                        teacherName = "Teacher";
                    }

                    tvHelloTeacher.setText(
                            "Hello, " + teacherName + " 👋"
                    );

                    tvTeacherSubtitle.setText(
                            "Manage your classroom easily"
                    );

                    assignedClass =
                            safe(staff.getString("assignedClass"));

                    assignedDivision =
                            safe(staff.getString("assignedDivision"));

                    if (assignedClass.isEmpty()) {

                        tvHomeAssignedClass.setText("Not Assigned");

                    } else if (
                            !assignedDivision.isEmpty()
                                    && !assignedDivision.equalsIgnoreCase(
                                    "All Divisions"
                            )
                    ) {

                        tvHomeAssignedClass.setText(
                                assignedClass + "-" + assignedDivision
                        );

                    } else {

                        tvHomeAssignedClass.setText(assignedClass);
                    }

                    schoolId =
                            safe(staff.getString("schoolId"));

                    loadTeacherPhoto(staff);

                    listenStudents();
                    listenTodayAttendance();
                    listenTodayTimetable();
                })
                .addOnFailureListener(e -> {

                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    tvHomeAssignedClass.setText("Not Available");
                    tvHomeStudentCount.setText("0");

                    renderEmptySchedule("Unable to load schedule");
                });
    }

    // =========================================================
    // PROFILE PHOTO
    // =========================================================

    private void loadTeacherPhoto(DocumentSnapshot staff) {

        String photo =
                safe(staff.getString("photoUrl"));

        if (photo.isEmpty()) {
            photo = safe(staff.getString("profileImageUrl"));
        }

        if (photo.isEmpty()) {
            photo = safe(staff.getString("profilePhoto"));
        }

        if (!photo.isEmpty() && ivTeacherHomeProfile != null) {

            Glide.with(this)
                    .load(photo)
                    .placeholder(R.drawable.teacher_profile)
                    .error(R.drawable.teacher_profile)
                    .circleCrop()
                    .into(ivTeacherHomeProfile);
        }
    }

    // =========================================================
    // LIVE STUDENT COUNT
    // =========================================================

    private void listenStudents() {

        if (studentListener != null) {
            studentListener.remove();
            studentListener = null;
        }

        if (schoolId.isEmpty() || assignedClass.isEmpty()) {

            tvHomeStudentCount.setText("0");

            return;
        }

        studentListener =
                db.collection("students")
                        .whereEqualTo("schoolId", schoolId)
                        .addSnapshotListener((snapshot, error) -> {

                            if (!isAdded() || getView() == null) {
                                return;
                            }

                            if (error != null || snapshot == null) {

                                tvHomeStudentCount.setText("0");

                                return;
                            }

                            int count = 0;

                            for (DocumentSnapshot document :
                                    snapshot.getDocuments()) {

                                String status =
                                        safe(document.getString("status"));

                                if (status.equalsIgnoreCase("Deleted")) {
                                    continue;
                                }

                                String studentClass =
                                        safe(document.getString("className"));

                                String studentDivision =
                                        safe(document.getString("division"));

                                if (!classMatches(
                                        assignedClass,
                                        studentClass
                                )) {
                                    continue;
                                }

                                if (!divisionMatches(
                                        assignedDivision,
                                        studentDivision
                                )) {
                                    continue;
                                }

                                count++;
                            }

                            tvHomeStudentCount.setText(
                                    String.valueOf(count)
                            );
                        });
    }

    // =========================================================
    // LIVE TODAY ATTENDANCE
    // =========================================================

    private void listenTodayAttendance() {

        if (attendanceListener != null) {
            attendanceListener.remove();
            attendanceListener = null;
        }

        if (schoolId.isEmpty() || assignedClass.isEmpty()) {

            tvHomeAttendance.setText("0%");

            return;
        }

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        attendanceListener =
                db.collection("studentAttendance")
                        .whereEqualTo("schoolId", schoolId)
                        .whereEqualTo("date", today)
                        .addSnapshotListener((snapshot, error) -> {

                            if (!isAdded() || getView() == null) {
                                return;
                            }

                            if (error != null || snapshot == null) {

                                tvHomeAttendance.setText("0%");

                                return;
                            }

                            int present = 0;
                            int marked = 0;

                            for (DocumentSnapshot document :
                                    snapshot.getDocuments()) {

                                String className =
                                        safe(document.getString("className"));

                                String division =
                                        safe(document.getString("division"));

                                if (!classMatches(
                                        assignedClass,
                                        className
                                )) {
                                    continue;
                                }

                                if (!divisionMatches(
                                        assignedDivision,
                                        division
                                )) {
                                    continue;
                                }

                                String status =
                                        safe(document.getString("status"));

                                if (status.equalsIgnoreCase("Present")) {

                                    present++;
                                    marked++;

                                } else if (
                                        status.equalsIgnoreCase("Absent")
                                ) {

                                    marked++;
                                }
                            }

                            if (marked == 0) {

                                tvHomeAttendance.setText("0%");

                                return;
                            }

                            int percentage = Math.round(
                                    present * 100f / marked
                            );

                            tvHomeAttendance.setText(
                                    percentage + "%"
                            );
                        });
    }

    // =========================================================
    // TODAY'S TIMETABLE - REAL TIME
    // =========================================================

    private void listenTodayTimetable() {

        if (timetableListener != null) {
            timetableListener.remove();
            timetableListener = null;
        }

        if (schoolId.isEmpty() || staffId.isEmpty()) {

            renderEmptySchedule("No schedule available");

            return;
        }

        String today = new SimpleDateFormat(
                "EEEE",
                Locale.ENGLISH
        ).format(new Date());

        timetableListener =
                db.collection("timetables")
                        .whereEqualTo("schoolId", schoolId)
                        .whereEqualTo("teacherId", staffId)
                        .addSnapshotListener((snapshot, error) -> {

                            if (!isAdded() || getView() == null) {
                                return;
                            }

                            if (error != null || snapshot == null) {

                                renderEmptySchedule(
                                        "Unable to load schedule"
                                );

                                return;
                            }

                            Map<Integer, DocumentSnapshot> todayPeriods =
                                    new HashMap<>();

                            for (DocumentSnapshot document :
                                    snapshot.getDocuments()) {

                                String day =
                                        safe(document.getString("day"));

                                if (!day.equalsIgnoreCase(today)) {
                                    continue;
                                }

                                // Skip any separate break documents.
                                if (Boolean.TRUE.equals(
                                        document.getBoolean("recess")
                                )) {
                                    continue;
                                }

                                int periodNumber =
                                        getPeriodNumber(document);

                                if (periodNumber <= 0) {
                                    continue;
                                }

                                todayPeriods.put(
                                        periodNumber,
                                        document
                                );
                            }

                            // Load School Time Setup before rendering.
                            db.collection("schoolTimeSettings")
                                    .document(schoolId)
                                    .get()
                                    .addOnSuccessListener(settings -> {

                                        if (!isAdded()
                                                || getView() == null) {
                                            return;
                                        }

                                        renderSchedule(
                                                todayPeriods,
                                                settings
                                        );
                                    })
                                    .addOnFailureListener(e -> {

                                        if (!isAdded()
                                                || getView() == null) {
                                            return;
                                        }

                                        // Still show periods if settings
                                        // cannot be loaded.
                                        renderSchedule(
                                                todayPeriods,
                                                null
                                        );
                                    });
                        });
    }

    // =========================================================
    // BUILD DYNAMIC SCHEDULE
    // =========================================================

    private void renderSchedule(
            Map<Integer, DocumentSnapshot> periods,
            DocumentSnapshot settings
    ) {

        if (layoutTodaySchedule == null) {
            return;
        }

        layoutTodaySchedule.removeAllViews();

        List<BreakItem> breaks = new ArrayList<>();

        String schoolStartTime = "";
        int periodMinutes = 0;

        if (settings != null && settings.exists()) {

            schoolStartTime =
                    safe(settings.getString("startTime"));

            periodMinutes =
                    getIntValue(settings.get("periodMinutes"));

            Object rawBreaks = settings.get("breaks");

            if (rawBreaks instanceof List<?>) {

                List<?> breakList = (List<?>) rawBreaks;

                int breakIndex = 0;

                for (Object object : breakList) {

                    if (!(object instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<?, ?> map = (Map<?, ?>) object;

                    int afterPeriod = getIntValue(
                            map.get("afterPeriod")
                    );

                    int duration = getIntValue(
                            map.get("minutes")
                    );

                    if (afterPeriod <= 0 || duration <= 0) {
                        continue;
                    }

                    breakIndex++;

                    String configuredLabel =
                            getMapString(map, "name");

                    if (configuredLabel.isEmpty()) {
                        configuredLabel =
                                getMapString(map, "type");
                    }

                    String label;
                    String icon;

                    if (configuredLabel.toLowerCase(Locale.ROOT)
                            .contains("lunch")) {

                        label = "LUNCH";
                        icon = "🍱";

                    } else if (
                            configuredLabel.toLowerCase(Locale.ROOT)
                                    .contains("recess")
                    ) {

                        label = "RECESS";
                        icon = "☕";

                    } else if (breakIndex == 1) {

                        label = "RECESS";
                        icon = "☕";

                    } else if (breakIndex == 2) {

                        label = "LUNCH";
                        icon = "🍱";

                    } else {

                        label = "BREAK " + breakIndex;
                        icon = "☕";
                    }

                    breaks.add(new BreakItem(
                            afterPeriod,
                            duration,
                            breakIndex,
                            label,
                            icon
                    ));
                }
            }
        }

        // Sort breaks by configured period.
        Collections.sort(
                breaks,
                Comparator.comparingInt(b -> b.afterPeriod)
        );

        // Calculate actual break times using school start time,
        // period duration, and all previous breaks.
        int schoolStartMinutes =
                parseTimeToMinutes(schoolStartTime);

        int accumulatedBreakMinutes = 0;

        for (BreakItem breakItem : breaks) {

            if (schoolStartMinutes >= 0 && periodMinutes > 0) {

                breakItem.startMinutes =
                        schoolStartMinutes
                                + (breakItem.afterPeriod * periodMinutes)
                                + accumulatedBreakMinutes;

                breakItem.endMinutes =
                        breakItem.startMinutes
                                + breakItem.durationMinutes;

            } else {

                breakItem.startMinutes = -1;
                breakItem.endMinutes = -1;
            }

            accumulatedBreakMinutes +=
                    breakItem.durationMinutes;
        }

        List<ScheduleItem> items = new ArrayList<>();

        // Add teacher's assigned periods.
        for (Map.Entry<Integer, DocumentSnapshot> entry :
                periods.entrySet()) {

            items.add(
                    ScheduleItem.period(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        // Add breaks into the same sequence.
        for (BreakItem breakItem : breaks) {

            items.add(
                    ScheduleItem.breakRow(breakItem)
            );
        }

        // Periods and breaks are sorted together.
        Collections.sort(
                items,
                Comparator.comparingDouble(item -> item.orderKey)
        );

        if (items.isEmpty()) {

            renderEmptySchedule("No classes scheduled for today");

            return;
        }

        for (int i = 0; i < items.size(); i++) {

            ScheduleItem item = items.get(i);

            if (item.isBreak) {

                addBreakRow(item.breakItem);

            } else {

                addPeriodRow(
                        item.periodNumber,
                        item.periodDocument
                );
            }

            if (i < items.size() - 1) {
                addScheduleDivider();
            }
        }
    }

    // =========================================================
    // ADD PERIOD ROW
    // =========================================================

    private void addPeriodRow(
            int periodNumber,
            DocumentSnapshot document
    ) {

        String startTime =
                safe(document.getString("startTime"));

        String endTime =
                safe(document.getString("endTime"));

        String subject =
                safe(document.getString("subject"));

        String className =
                safe(document.getString("className"));

        String division =
                safe(document.getString("division"));

        String time;

        if (startTime.isEmpty() && endTime.isEmpty()) {

            time = "--:--";

        } else {

            time = startTime + " - " + endTime;
        }

        LinearLayout row = new LinearLayout(
                requireContext()
        );

        row.setOrientation(LinearLayout.VERTICAL);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView top = new TextView(
                requireContext()
        );

        top.setText(
                time + "  •  P" + periodNumber
        );

        top.setTextColor(COLOR_PRIMARY);

        top.setTextSize(13);

        top.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        top.setIncludeFontPadding(true);

        row.addView(top);

        TextView bottom = new TextView(
                requireContext()
        );

        StringBuilder bottomText =
                new StringBuilder();

        if (subject.isEmpty()) {
            bottomText.append("Free Period");
        } else {
            bottomText.append(subject);
        }

        if (!className.isEmpty()) {

            bottomText.append(" • Class ")
                    .append(className);
        }

        if (!division.isEmpty()
                && !division.equalsIgnoreCase("All Divisions")) {

            bottomText.append("-")
                    .append(division);
        }

        bottom.setText(bottomText.toString());

        bottom.setTextColor(COLOR_SECONDARY);

        bottom.setTextSize(12);

        LinearLayout.LayoutParams bottomParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        bottomParams.topMargin = dp(3);

        row.addView(bottom, bottomParams);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        layoutTodaySchedule.addView(row, rowParams);
    }

    // =========================================================
    // ADD BREAK ROW AT THE CORRECT POSITION
    // =========================================================

    private void addBreakRow(BreakItem breakItem) {

        LinearLayout row = new LinearLayout(
                requireContext()
        );

        row.setOrientation(LinearLayout.HORIZONTAL);

        row.setGravity(Gravity.CENTER_VERTICAL);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView icon = new TextView(
                requireContext()
        );

        icon.setText(breakItem.icon);

        icon.setTextSize(14);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        row.addView(icon, iconParams);

        TextView text = new TextView(
                requireContext()
        );

        String timeRange;

        if (breakItem.startMinutes >= 0
                && breakItem.endMinutes >= 0) {

            timeRange =
                    formatTime(breakItem.startMinutes)
                            + " - "
                            + formatTime(breakItem.endMinutes);

        } else {

            timeRange = "Time not available";
        }

        text.setText(
                timeRange + " • " + breakItem.label
        );

        text.setTextColor(
                Color.rgb(119, 119, 119)
        );

        text.setTextSize(12);

        text.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        textParams.leftMargin = dp(8);

        row.addView(text, textParams);

        layoutTodaySchedule.addView(row);
    }

    // =========================================================
    // DIVIDER
    // =========================================================

    private void addScheduleDivider() {

        View divider = new View(
                requireContext()
        );

        divider.setBackgroundColor(COLOR_DIVIDER);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(1)
                );

        params.topMargin = dp(11);
        params.bottomMargin = dp(11);

        layoutTodaySchedule.addView(
                divider,
                params
        );
    }

    // =========================================================
    // EMPTY SCHEDULE
    // =========================================================

    private void renderEmptySchedule(String message) {

        if (layoutTodaySchedule == null
                || !isAdded()) {
            return;
        }

        layoutTodaySchedule.removeAllViews();

        TextView empty = new TextView(
                requireContext()
        );

        empty.setText(message);

        empty.setTextColor(COLOR_SECONDARY);

        empty.setTextSize(12);

        empty.setGravity(Gravity.CENTER);

        empty.setPadding(
                dp(8),
                dp(12),
                dp(8),
                dp(12)
        );

        layoutTodaySchedule.addView(
                empty,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    // =========================================================
    // GET PERIOD NUMBER
    // =========================================================

    private int getPeriodNumber(DocumentSnapshot document) {

        Object value = document.get("periodNumber");

        if (value instanceof Number) {

            return ((Number) value).intValue();
        }

        String period = safe(
                document.getString("period")
        );

        if (period.isEmpty()) {

            period = safe(
                    document.getString("periodName")
            );
        }

        if (period.isEmpty()) {
            return -1;
        }

        String digits = period.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return -1;
        }

        try {

            return Integer.parseInt(digits);

        } catch (NumberFormatException e) {

            return -1;
        }
    }

    // =========================================================
    // SAFE INTEGER
    // =========================================================

    private int getIntValue(Object value) {

        if (value instanceof Number) {

            return ((Number) value).intValue();
        }

        if (value instanceof String) {

            try {

                return Integer.parseInt(
                        ((String) value).trim()
                );

            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }

    // =========================================================
    // GET STRING FROM MAP
    // =========================================================

    private String getMapString(
            Map<?, ?> map,
            String key
    ) {

        Object value = map.get(key);

        return value == null
                ? ""
                : String.valueOf(value).trim();
    }

    // =========================================================
    // PARSE SCHOOL TIME
    // Supports 09:00, 09:00 AM and 9:00 am
    // =========================================================

    private int parseTimeToMinutes(String value) {

        if (value == null || value.trim().isEmpty()) {
            return -1;
        }

        String time = value.trim();

        String[] patterns = {
                "hh:mm a",
                "h:mm a",
                "HH:mm",
                "H:mm"
        };

        for (String pattern : patterns) {

            try {

                SimpleDateFormat format =
                        new SimpleDateFormat(
                                pattern,
                                Locale.US
                        );

                format.setLenient(false);

                Date date = format.parse(time);

                if (date != null) {

                    Calendar calendar =
                            Calendar.getInstance();

                    calendar.setTime(date);

                    return calendar.get(Calendar.HOUR_OF_DAY) * 60
                            + calendar.get(Calendar.MINUTE);
                }

            } catch (Exception ignored) {
                // Try the next supported format.
            }
        }

        return -1;
    }

    // =========================================================
    // FORMAT TIME
    // =========================================================

    private String formatTime(int totalMinutes) {

        int normalized =
                ((totalMinutes % 1440) + 1440) % 1440;

        Calendar calendar = Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                normalized / 60
        );

        calendar.set(
                Calendar.MINUTE,
                normalized % 60
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        return new SimpleDateFormat(
                "hh:mm a",
                Locale.US
        ).format(calendar.getTime());
    }

    // =========================================================
    // DP TO PIXELS
    // =========================================================

    private int dp(float value) {

        if (getResources() == null) {
            return (int) value;
        }

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =========================================================
    // CLASS MATCH
    // =========================================================

    private boolean classMatches(
            String teacherClass,
            String studentClass
    ) {

        if (teacherClass.isEmpty()
                || studentClass.isEmpty()) {

            return false;
        }

        return normalizeClass(teacherClass)
                .equals(normalizeClass(studentClass));
    }

    private String normalizeClass(String value) {

        String text = value.trim()
                .toLowerCase(Locale.ROOT);

        text = text.replace("class", "");
        text = text.replace("standard", "");
        text = text.replace("std", "");

        return text.trim();
    }

    // =========================================================
    // DIVISION MATCH
    // =========================================================

    private boolean divisionMatches(
            String teacherDivision,
            String studentDivision
    ) {

        if (teacherDivision.isEmpty()
                || teacherDivision.equalsIgnoreCase(
                "All Divisions"
        )) {

            return true;
        }

        if (studentDivision.isEmpty()) {
            return false;
        }

        return teacherDivision.trim()
                .equalsIgnoreCase(studentDivision.trim());
    }

    // =========================================================
    // BUILD TEACHER NAME
    // =========================================================

    private String buildFullName(
            String firstName,
            String middleName,
            String lastName
    ) {

        StringBuilder name = new StringBuilder();

        if (!firstName.isEmpty()) {
            name.append(firstName);
        }

        if (!middleName.isEmpty()) {

            if (name.length() > 0) {
                name.append(" ");
            }

            name.append(middleName);
        }

        if (!lastName.isEmpty()) {

            if (name.length() > 0) {
                name.append(" ");
            }

            name.append(lastName);
        }

        return name.toString();
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    // =========================================================
    // REMOVE FIRESTORE LISTENERS
    // =========================================================

    @Override
    public void onDestroyView() {

        if (studentListener != null) {
            studentListener.remove();
            studentListener = null;
        }

        if (attendanceListener != null) {
            attendanceListener.remove();
            attendanceListener = null;
        }

        if (timetableListener != null) {
            timetableListener.remove();
            timetableListener = null;
        }

        layoutTodaySchedule = null;

        super.onDestroyView();
    }
}