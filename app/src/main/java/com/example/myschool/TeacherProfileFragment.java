package com.example.myschool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TeacherProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvProfileTeacherName;
    private TextView tvProfileTeacherSubject;
    private TextView tvProfileEmail;
    private TextView tvProfileMobile;

    private View rowMyTimetable;
    private View rowHelpSupport;
    private View rowAboutSchool;

    private View btnTeacherLogout;

    private String staffId = "";
    private String schoolId = "";

    private String teacherName = "Teacher Name";
    private String teacherSubject = "Subject Teacher";
    private String teacherMobile = "Not Available";

    public TeacherProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_teacher_profile,
                container,
                false
        );

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);

        loadTeacherProfile();

        setupClicks();

        return view;
    }

    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews(View view) {

        tvProfileTeacherName =
                view.findViewById(R.id.tvProfileTeacherName);

        tvProfileTeacherSubject =
                view.findViewById(R.id.tvProfileTeacherSubject);

        tvProfileEmail =
                view.findViewById(R.id.tvProfileEmail);

        tvProfileMobile =
                view.findViewById(R.id.tvProfileMobile);

        rowMyTimetable =
                view.findViewById(R.id.rowMyTimetable);

        rowHelpSupport =
                view.findViewById(R.id.rowHelpSupport);

        rowAboutSchool =
                view.findViewById(R.id.rowAboutSchool);

        btnTeacherLogout =
                view.findViewById(R.id.btnTeacherLogout);
    }

    // =========================================================
    // CLICK LISTENERS
    // =========================================================

    private void setupClicks() {

        // -----------------------------------------------------
        // MY TIMETABLE
        // -----------------------------------------------------

        if (rowMyTimetable != null) {

            rowMyTimetable.setOnClickListener(
                    v -> showMyTimetable()
            );
        }

        // -----------------------------------------------------
        // HELP & SUPPORT
        // -----------------------------------------------------

        if (rowHelpSupport != null) {

            rowHelpSupport.setOnClickListener(
                    v -> showHelpSupport()
            );
        }

        // -----------------------------------------------------
        // ABOUT SCHOOL
        // -----------------------------------------------------

        if (rowAboutSchool != null) {

            rowAboutSchool.setOnClickListener(
                    v -> showAboutSchool()
            );
        }

        // -----------------------------------------------------
        // LOGOUT
        // -----------------------------------------------------

        if (btnTeacherLogout != null) {

            btnTeacherLogout.setOnClickListener(
                    v -> showLogoutDialog()
            );
        }
    }

    // =========================================================
    // LOAD TEACHER PROFILE
    // =========================================================

    private void loadTeacherProfile() {

        if (auth.getCurrentUser() == null) {

            showDefaultProfile();

            return;
        }

        // Firebase Auth Email

        String email = auth
                .getCurrentUser()
                .getEmail();

        if (email != null && !email.trim().isEmpty()) {

            tvProfileEmail.setText(
                    email.trim()
            );

        } else {

            tvProfileEmail.setText(
                    "Not Available"
            );
        }

        // -----------------------------------------------------
        // STAFF ID
        // -----------------------------------------------------

        String id = null;

        if (requireActivity().getIntent() != null) {

            id = requireActivity()
                    .getIntent()
                    .getStringExtra("staffId");
        }

        if (id == null) {
            id = "";
        }

        staffId = id.trim();

        // -----------------------------------------------------
        // IF STAFF ID NOT FOUND
        // -----------------------------------------------------

        if (staffId.isEmpty()) {

            loadTeacherUsingAuthEmail();

            return;
        }

        // -----------------------------------------------------
        // LOAD STAFF DOCUMENT
        // -----------------------------------------------------

        db.collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(
                        this::applyTeacherData
                )
                .addOnFailureListener(
                        e -> {

                            loadTeacherUsingAuthEmail();

                        }
                );
    }

    // =========================================================
    // LOAD USING AUTH EMAIL
    // =========================================================

    private void loadTeacherUsingAuthEmail() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String email = auth
                .getCurrentUser()
                .getEmail();

        if (email == null || email.trim().isEmpty()) {
            return;
        }

        db.collection("staff")
                .whereEqualTo(
                        "email",
                        email.trim()
                )
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {

                        showDefaultProfile();

                        return;
                    }

                    DocumentSnapshot document =
                            snapshot.getDocuments().get(0);

                    staffId = document.getId();

                    applyTeacherData(document);

                })
                .addOnFailureListener(
                        e -> showDefaultProfile()
                );
    }

    // =========================================================
    // APPLY STAFF DATA
    // =========================================================

    private void applyTeacherData(
            DocumentSnapshot document) {

        if (document == null || !document.exists()) {

            showDefaultProfile();

            return;
        }

        // SCHOOL ID

        schoolId = firstNonEmpty(
                document.getString("schoolId"),
                ""
        );

        // -----------------------------------------------------
        // NAME
        // -----------------------------------------------------

        String firstName =
                firstNonEmpty(
                        document.getString("firstName"),
                        document.getString("first_name"),
                        ""
                );

        String middleName =
                firstNonEmpty(
                        document.getString("middleName"),
                        document.getString("middle_name"),
                        ""
                );

        String lastName =
                firstNonEmpty(
                        document.getString("lastName"),
                        document.getString("last_name"),
                        ""
                );

        String completeName =
                (
                        firstName
                                + " "
                                + middleName
                                + " "
                                + lastName
                )
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();

        if (!completeName.isEmpty()) {

            teacherName = completeName;

        } else {

            String name =
                    firstNonEmpty(
                            document.getString("name"),
                            document.getString("teacherName"),
                            ""
                    );

            if (!name.isEmpty()) {
                teacherName = name;
            }
        }

        // -----------------------------------------------------
        // SUBJECT
        // -----------------------------------------------------

        String subject =
                firstNonEmpty(
                        document.getString("subject"),
                        document.getString("subjects"),
                        document.getString("specialization"),
                        document.getString("department"),
                        ""
                );

        if (!subject.isEmpty()) {

            teacherSubject = subject;
        }

        // -----------------------------------------------------
        // MOBILE
        // -----------------------------------------------------

        String mobile =
                firstNonEmpty(
                        document.getString("staffPhone"),
                        document.getString("mobile"),
                        document.getString("mobileNumber"),
                        document.getString("phone"),
                        document.getString("phoneNumber"),
                        ""
                );

        if (!mobile.isEmpty()) {

            teacherMobile = mobile;
        }

        // -----------------------------------------------------
        // DISPLAY
        // -----------------------------------------------------

        tvProfileTeacherName.setText(
                teacherName
        );

        tvProfileTeacherSubject.setText(
                teacherSubject
        );

        tvProfileMobile.setText(
                teacherMobile
        );
    }

    // =========================================================
    // DEFAULT PROFILE
    // =========================================================

    private void showDefaultProfile() {

        tvProfileTeacherName.setText(
                teacherName
        );

        tvProfileTeacherSubject.setText(
                teacherSubject
        );

        tvProfileMobile.setText(
                teacherMobile
        );
    }

    // =========================================================
    // MY TIMETABLE
    // =========================================================

    private void showMyTimetable() {

        if (schoolId.isEmpty() || staffId.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Teacher timetable information is loading.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final String[] days = {
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday"
        };

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "My Timetable"
                )
                .setItems(
                        days,
                        (dialog, which) -> {

                            loadDayTimetable(
                                    days[which]
                            );
                        }
                )
                .setNegativeButton(
                        "Close",
                        null
                )
                .show();
    }

    // =========================================================
    // LOAD DAY TIMETABLE
    // =========================================================

    private void loadDayTimetable(
            String selectedDay) {

        db.collection("timetables")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .whereEqualTo(
                        "teacherId",
                        staffId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            List<TimetableItem> list =
                                    new ArrayList<>();

                            for (
                                    DocumentSnapshot document
                                    : snapshot.getDocuments()
                            ) {

                                String day =
                                        safe(
                                                document.getString(
                                                        "day"
                                                )
                                        );

                                if (!day.equalsIgnoreCase(
                                        selectedDay
                                )) {
                                    continue;
                                }

                                boolean recess =
                                        Boolean.TRUE.equals(
                                                document.getBoolean(
                                                        "recess"
                                                )
                                        );

                                String startTime =
                                        safe(
                                                document.getString(
                                                        "startTime"
                                                )
                                        );

                                String endTime =
                                        safe(
                                                document.getString(
                                                        "endTime"
                                                )
                                        );

                                String subject =
                                        safe(
                                                document.getString(
                                                        "subject"
                                                )
                                        );

                                String className =
                                        safe(
                                                document.getString(
                                                        "className"
                                                )
                                        );

                                String division =
                                        safe(
                                                document.getString(
                                                        "division"
                                                )
                                        );

                                Long periodLong =
                                        document.getLong(
                                                "periodNumber"
                                        );

                                int period =
                                        periodLong == null
                                                ? 999
                                                : periodLong.intValue();

                                list.add(
                                        new TimetableItem(
                                                startTime,
                                                endTime,
                                                subject,
                                                className,
                                                division,
                                                period,
                                                recess
                                        )
                                );
                            }

                            Collections.sort(
                                    list,
                                    Comparator.comparingInt(
                                            item -> item.period
                                    )
                            );

                            showTimetableDialog(
                                    selectedDay,
                                    list
                            );
                        }
                )
                .addOnFailureListener(
                        e -> Toast.makeText(
                                requireContext(),
                                "Unable to load timetable.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // =========================================================
    // TIMETABLE DIALOG
    // =========================================================

    private void showTimetableDialog(
            String day,
            List<TimetableItem> list) {

        if (list.isEmpty()) {

            new AlertDialog.Builder(
                    requireContext()
            )
                    .setTitle(
                            day + " Timetable"
                    )
                    .setMessage(
                            "No timetable assigned for this day."
                    )
                    .setPositiveButton(
                            "OK",
                            null
                    )
                    .show();

            return;
        }

        StringBuilder builder =
                new StringBuilder();

        for (TimetableItem item : list) {

            if (item.recess) {

                builder.append(
                        "☕ "
                );

            } else {

                builder.append(
                        "📚 "
                );
            }

            if (!item.startTime.isEmpty()) {

                builder.append(
                        item.startTime
                );

                if (!item.endTime.isEmpty()) {

                    builder.append(
                            " - "
                    );

                    builder.append(
                            item.endTime
                    );
                }

                builder.append(
                        "\n"
                );
            }

            if (item.recess) {

                builder.append(
                        "BREAK / RECESS"
                );

            } else {

                if (!item.subject.isEmpty()) {

                    builder.append(
                            item.subject
                    );

                } else {

                    builder.append(
                            "Subject"
                    );
                }

                if (!item.className.isEmpty()) {

                    builder.append(
                            "\nClass: "
                    );

                    builder.append(
                            item.className
                    );
                }

                if (!item.division.isEmpty()
                        && !item.division.equalsIgnoreCase(
                        "All Divisions"
                )) {

                    builder.append(
                            " - "
                    );

                    builder.append(
                            item.division
                    );
                }
            }

            builder.append(
                    "\n\n"
            );
        }

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        day + " Timetable"
                )
                .setMessage(
                        builder.toString().trim()
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =========================================================
    // HELP & SUPPORT
    // =========================================================

    private void showHelpSupport() {

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Help & Support"
                )
                .setMessage(
                        "For any issue related to:\n\n"
                                + "• Attendance\n"
                                + "• Homework\n"
                                + "• Marks\n"
                                + "• Timetable\n"
                                + "• Student information\n"
                                + "• Notices / Messages\n\n"
                                + "Please contact your school administrator."
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =========================================================
    // ABOUT SCHOOL
    // =========================================================

    private void showAboutSchool() {

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "About MySchool"
                )
                .setMessage(
                        "MySchool\n\n"
                                + "Teacher Management Portal\n\n"
                                + "Use this section to manage your "
                                + "class, timetable, attendance, "
                                + "homework and academic activities."
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =========================================================
    // LOGOUT CONFIRMATION
    // =========================================================

    private void showLogoutDialog() {

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Logout"
                )
                .setMessage(
                        "Are you sure you want to logout?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Logout",
                        (dialog, which) ->
                                logoutTeacher()
                )
                .show();
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    private void logoutTeacher() {

        // Firebase logout
        if (auth != null) {

            auth.signOut();
        }

        // Clear local login session
        SharedPreferences preferences =
                requireActivity()
                        .getSharedPreferences(
                                "MySchoolPrefs",
                                Context.MODE_PRIVATE
                        );

        preferences.edit()
                .clear()
                .apply();

        Toast.makeText(
                requireContext(),
                "Logged out successfully",
                Toast.LENGTH_SHORT
        ).show();

        // Open Login
        Intent intent =
                new Intent(
                        requireActivity(),
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String firstNonEmpty(
            String... values) {

        if (values == null) {
            return "";
        }

        for (String value : values) {

            if (value != null
                    && !value.trim().isEmpty()) {

                return value.trim();
            }
        }

        return "";
    }

    private String safe(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    // =========================================================
    // TIMETABLE MODEL
    // =========================================================

    private static class TimetableItem {

        String startTime;
        String endTime;
        String subject;
        String className;
        String division;

        int period;

        boolean recess;

        TimetableItem(
                String startTime,
                String endTime,
                String subject,
                String className,
                String division,
                int period,
                boolean recess) {

            this.startTime = startTime;
            this.endTime = endTime;
            this.subject = subject;
            this.className = className;
            this.division = division;
            this.period = period;
            this.recess = recess;
        }
    }
}