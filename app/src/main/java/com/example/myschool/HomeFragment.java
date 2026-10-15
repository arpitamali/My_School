package com.example.myschool;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    // =========================================================
    // VIEWS
    // =========================================================

    private TextView tvSchoolName;
    private TextView tvSchoolAddress;
    private TextView tvAdminName;

    private TextView tvStudentCount;
    private TextView tvTeacherCount;
    private TextView tvPresentCount;
    private TextView tvBirthdayCount;

    private TextView tvActivity1;
    private TextView tvActivity2;
    private TextView tvActivity3;
    private TextView tvActivity4;

    private TextView tvSpecialOccasion1;
    private TextView tvSpecialOccasion2;

    private TextView tvMeeting1;
    private TextView tvMeeting2;

    private TextView btnClearActivities;

    private Button btnLogout;

    private View btnAddStudent;
    private View btnAddStaff;
    private View btnPostNotice;
    private View btnMessage;
    private View btnTeacherAttendance;
    private View btnAdminTimetable;
    private View btnAchievement;
    private View btnSalaryPayment;

    private View cardSpecialOccasions;
    private View cardMeeting;
    private View cardAttendance;
    private View cardBirthday;

    private ListenerRegistration todayStudentAttendanceListener;
    private ListenerRegistration todayStaffAttendanceListener;

    private int todayStudentPresent = 0;
    private int todayStaffPresent = 0;
    private int todayGirlsPresent = 0;
    private int todayBoysPresent = 0;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    // =========================================================
    // ACTIVITY DATA
    // =========================================================

    private final List<DocumentSnapshot> recentActivities =
            new ArrayList<>();


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    public void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();
    }


    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable android.view.ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view =
                inflater.inflate(
                        R.layout.fragment_home,
                        container,
                        false
                );

        initViews(view);

        setupClickListeners();

        return view;
    }


    // =========================================================
    // VIEW CREATED
    // =========================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        loadSchoolData();
        loadRecentActivities();
        loadSpecialOccasions();
        loadMeetings();
        loadTodayBirthdayCount();
        listenTodayPresent();
    }


    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (mAuth != null &&
                mAuth.getCurrentUser() != null) {

            loadSchoolData();
            loadRecentActivities();
            loadSpecialOccasions();
            loadMeetings();
            loadTodayBirthdayCount();
        }
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews(
            View view
    ) {

        tvSchoolName =
                view.findViewById(
                        R.id.tvSchoolName
                );

        tvSchoolAddress =
                view.findViewById(
                        R.id.tvSchoolAddress
                );

        tvAdminName =
                view.findViewById(
                        R.id.tvAdminName
                );


        tvStudentCount =
                view.findViewById(
                        R.id.tvStudentCount
                );

        tvTeacherCount =
                view.findViewById(
                        R.id.tvTeacherCount
                );

        tvPresentCount =
                view.findViewById(
                        R.id.tvPresentCount
                );

        tvBirthdayCount =
                view.findViewById(
                        R.id.tvBirthdayCount
                );


        tvActivity1 =
                view.findViewById(
                        R.id.tvActivity1
                );

        tvActivity2 =
                view.findViewById(
                        R.id.tvActivity2
                );

        tvActivity3 =
                view.findViewById(
                        R.id.tvActivity3
                );

        tvActivity4 =
                view.findViewById(
                        R.id.tvActivity4
                );


        tvSpecialOccasion1 =
                view.findViewById(
                        R.id.tvSpecialOccasion1
                );

        tvSpecialOccasion2 =
                view.findViewById(
                        R.id.tvSpecialOccasion2
                );


        tvMeeting1 =
                view.findViewById(
                        R.id.tvMeeting1
                );

        tvMeeting2 =
                view.findViewById(
                        R.id.tvMeeting2
                );


        btnClearActivities =
                view.findViewById(
                        R.id.btnClearActivities
                );


        btnLogout =
                view.findViewById(
                        R.id.btnLogout
                );


        btnAddStudent =
                view.findViewById(
                        R.id.btnAddStudent
                );

        btnAddStaff =
                view.findViewById(
                        R.id.btnAddStaff
                );

        btnPostNotice =
                view.findViewById(
                        R.id.btnPostNotice
                );

        btnMessage =
                view.findViewById(
                        R.id.btnMessage
                );

        btnTeacherAttendance =
                view.findViewById(
                        R.id.btnTeacherAttendance
                );

        btnAdminTimetable =
                view.findViewById(
                        R.id.btnAdminTimetable
                );

        btnAchievement =
                view.findViewById(
                        R.id.btnAchievement
                );

        btnSalaryPayment =
                view.findViewById(
                        R.id.btnSalaryPayment
                );


        cardSpecialOccasions =
                view.findViewById(
                        R.id.cardSpecialOccasions
                );

        cardMeeting =
                view.findViewById(
                        R.id.cardMeeting
                );

        cardAttendance =
                view.findViewById(
                        R.id.cardAttendance
                );

        cardBirthday =
                view.findViewById(
                        R.id.cardBirthday
                );
    }


    // =========================================================
    // SCHOOL DATA
    // =========================================================

    private void loadSchoolData() {

        if (mAuth == null ||
                mAuth.getCurrentUser() == null) {

            if (isAdded()) {

                Toast.makeText(
                        requireContext(),
                        "User is not logged in.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return;
        }


        String uid =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("schools")
                .document(uid)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }


                            if (!documentSnapshot.exists()) {

                                showDefaultValues();

                                loadActualStudentCount(
                                        uid
                                );

                                return;
                            }


                            // =================================================
                            // SCHOOL NAME
                            // =================================================

                            String schoolName =
                                    documentSnapshot.getString(
                                            "schoolName"
                                    );


                            if (!TextUtils.isEmpty(
                                    schoolName
                            )) {

                                tvSchoolName.setText(
                                        schoolName
                                );

                            } else {

                                tvSchoolName.setText(
                                        "School Name"
                                );
                            }


                            // =================================================
                            // ADDRESS
                            // =================================================

                            String schoolAddress =
                                    documentSnapshot.getString(
                                            "schoolAddress"
                                    );


                            if (TextUtils.isEmpty(
                                    schoolAddress
                            )) {

                                schoolAddress =
                                        documentSnapshot.getString(
                                                "address"
                                        );
                            }


                            if (TextUtils.isEmpty(
                                    schoolAddress
                            )) {

                                String city =
                                        documentSnapshot.getString(
                                                "city"
                                        );

                                String district =
                                        documentSnapshot.getString(
                                                "district"
                                        );

                                String state =
                                        documentSnapshot.getString(
                                                "state"
                                        );


                                StringBuilder builder =
                                        new StringBuilder();


                                if (!TextUtils.isEmpty(city)) {

                                    builder.append(
                                            city
                                    );
                                }


                                if (!TextUtils.isEmpty(district)) {

                                    if (builder.length() > 0) {
                                        builder.append(", ");
                                    }

                                    builder.append(
                                            district
                                    );
                                }


                                if (!TextUtils.isEmpty(state)) {

                                    if (builder.length() > 0) {
                                        builder.append(", ");
                                    }

                                    builder.append(
                                            state
                                    );
                                }


                                schoolAddress =
                                        builder.toString();
                            }


                            if (!TextUtils.isEmpty(
                                    schoolAddress
                            )) {

                                tvSchoolAddress.setText(
                                        "📍 " +
                                                schoolAddress
                                );

                            } else {

                                tvSchoolAddress.setText(
                                        "📍 Address not available"
                                );
                            }


                            // =================================================
                            // ADMIN
                            // =================================================

                            String adminName =
                                    documentSnapshot.getString(
                                            "adminName"
                                    );


                            if (!TextUtils.isEmpty(
                                    adminName
                            )) {

                                tvAdminName.setText(
                                        "👤 " +
                                                adminName
                                );

                            } else {

                                tvAdminName.setText(
                                        "👤 Admin"
                                );
                            }


                            // =================================================
                            // TEACHERS
                            // =================================================

                            Long teacherCount =
                                    documentSnapshot.getLong(
                                            "teacherCount"
                                    );


                            if (teacherCount != null) {

                                tvTeacherCount.setText(
                                        String.valueOf(
                                                teacherCount
                                        )
                                );

                            } else {

                                tvTeacherCount.setText(
                                        "0"
                                );
                            }


                            // =================================================
                            // PRESENT
                            // =================================================

                            // Today's Present is calculated live from
                            // studentAttendance + staff attendance listeners.
                            // Do NOT set tvPresentCount from the old
                            // schools.presentCount field here, otherwise
                            // it can overwrite the live count.



                            // =================================================
                            // TODAY'S BIRTHDAY
                            // =================================================
                            // Birthday is calculated from students.dateOfBirth.
                            // Only DAY + MONTH are compared.
                            // DOB year is ignored.

                            loadTodayBirthdayCount();


                            loadActualStudentCount(
                                    uid
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            showDefaultValues();

                            loadActualStudentCount(
                                    uid
                            );


                            Toast.makeText(
                                    requireContext(),
                                    "Unable to load school data.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // STUDENT COUNT
    // =========================================================

    private void loadActualStudentCount(
            String schoolId
    ) {

        if (!isAdded()) {
            return;
        }


        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        studentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }


                            int actualCount =
                                    studentSnapshot.size();


                            tvStudentCount.setText(
                                    String.valueOf(
                                            actualCount
                                    )
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            tvStudentCount.setText(
                                    "0"
                            );
                        }
                );
    }


    // =========================================================
    // DEFAULT VALUES
    // =========================================================

    private void showDefaultValues() {

        if (tvSchoolName != null) {
            tvSchoolName.setText(
                    "My School"
            );
        }


        if (tvSchoolAddress != null) {
            tvSchoolAddress.setText(
                    "School Address"
            );
        }


        if (tvAdminName != null) {
            tvAdminName.setText(
                    "👤 Admin"
            );
        }


        if (tvStudentCount != null) {
            tvStudentCount.setText(
                    "0"
            );
        }


        if (tvTeacherCount != null) {
            tvTeacherCount.setText(
                    "0"
            );
        }


        if (tvPresentCount != null) {
            tvPresentCount.setText(
                    "0"
            );
        }


        if (tvBirthdayCount != null) {
            tvBirthdayCount.setText(
                    "0"
            );
        }
    }


    // =========================================================
    // CLICK LISTENERS
    // =========================================================

    private void setupClickListeners() {

        // =====================================================
        // LOGOUT
        // =====================================================

        // LOGOUT
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        }

        // =====================================================
        // ADD STUDENT
        // =====================================================

        if (btnAddStudent != null) {

            btnAddStudent.setOnClickListener(
                    v -> {

                        logActivity(
                                "Student section opened",
                                "Student"
                        );


                        Intent intent =
                                new Intent(
                                        requireActivity(),
                                        AddStudentStep1.class
                                );


                        startActivity(
                                intent
                        );
                    }
            );
        }


        // =====================================================
        // ADD STAFF
        // =====================================================

        if (btnAddStaff != null) {

            btnAddStaff.setOnClickListener(v -> {

                logActivity(
                        "Add Staff section opened",
                        "Staff"
                );

                Intent intent =
                        new Intent(
                                requireActivity(),
                                AddStaffStep1.class
                        );

                startActivity(intent);
            });
        }


        // =====================================================
        // POST NOTICE
        // =====================================================

        if (btnPostNotice != null) {

            btnPostNotice.setOnClickListener(
                    v -> showPostNoticeDialog()
            );
        }


        // =====================================================
        // SEE ALL NOTICES
        // =====================================================

        if (btnMessage != null) {

            btnMessage.setOnClickListener(
                    v -> showAllNoticesDialog()
            );
        }


        // =====================================================
        // TEACHER ATTENDANCE
        // =====================================================

        if (btnTeacherAttendance != null) {
            btnTeacherAttendance.setOnClickListener(v -> {
                logActivity("Teacher attendance opened", "Staff");
                startActivity(new Intent(
                        requireActivity(),
                        TeacherAttendanceActivity.class
                ));
            });
        }


        // =====================================================
        // SCHOOL TIMETABLE
        // =====================================================

        if (btnAdminTimetable != null) {
            btnAdminTimetable.setOnClickListener(v -> {
                logActivity("School timetable opened", "Timetable");
                startActivity(new Intent(
                        requireActivity(),
                        AdminTimetableActivity.class
                ));
            });
        }


        // =====================================================
        // STUDENT ACHIEVEMENT
        // =====================================================

        if (btnAchievement != null) {
            btnAchievement.setOnClickListener(v -> {
                logActivity("School achievement opened", "Achievement");
                startActivity(new Intent(
                        requireActivity(),
                        AchievementsActivity.class
                ));
            });
        }


        // =====================================================
        // SALARY / PAYMENT
        // =====================================================

        if (btnSalaryPayment != null) {
            btnSalaryPayment.setOnClickListener(v -> {
                logActivity("Salary / payment opened", "Salary");
                startActivity(new Intent(
                        requireActivity(),
                        SalaryPaymentActivity.class
                ));
            });
        }


        // =====================================================
        // TODAY'S PRESENT - CLICKABLE
        // =====================================================

        if (cardAttendance != null) {
            cardAttendance.setOnClickListener(v -> {
                logActivity("Today's Present opened", "All");
                showTodayPresentDialog();
            });
        }


        // =====================================================
        // TODAY'S BIRTHDAY - CLICKABLE
        // =====================================================

        if (cardBirthday != null) {

            cardBirthday.setOnClickListener(v ->
                    showTodayBirthdaysDialog()
            );
        }


        // =====================================================
        // SPECIAL OCCASIONS
        // =====================================================

        if (cardSpecialOccasions != null) {

            cardSpecialOccasions.setOnClickListener(
                    v -> showSpecialOccasionDialog()
            );
        }


        // =====================================================
        // MEETING
        // =====================================================

        if (cardMeeting != null) {

            cardMeeting.setOnClickListener(
                    v -> showScheduleMeetingDialog()
            );
        }


        // =====================================================
        // CLEAR ALL ACTIVITIES
        // =====================================================

        if (btnClearActivities != null) {

            btnClearActivities.setOnClickListener(
                    v -> showClearActivitiesDialog()
            );
        }
    }


    // =========================================================
    // POST NOTICE DIALOG
    // =========================================================

    private void showPostNoticeDialog() {

        if (!isAdded()) {
            return;
        }


        LinearLayout layout =
                createDialogLayout();


        // TITLE

        EditText etTitle =
                createSingleLineEditText(
                        "Notice Title",
                        48
                );


        addField(
                layout,
                etTitle,
                8,
                8
        );


        // DESCRIPTION

        EditText etDescription =
                createMultiLineEditText(
                        "Write notice message...",
                        72
                );


        addField(
                layout,
                etDescription,
                0,
                8
        );


        // AUDIENCE LABEL

        TextView tvAudience =
                createLabel(
                        "Who should receive this notice?"
                );


        layout.addView(
                tvAudience
        );


        // AUDIENCE

        Spinner spAudience =
                createAudienceSpinner();


        layout.addView(
                spAudience,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(48)
                )
        );


        // DIALOG

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "📢 Post Notice"
                        )
                        .setView(
                                layout
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "POST NOTICE",
                                null
                        )
                        .create();


        dialog.setOnShowListener(
                dialogInterface -> {

                    Button positiveButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );


                    positiveButton.setOnClickListener(
                            v -> {

                                String title =
                                        etTitle.getText()
                                                .toString()
                                                .trim();


                                String description =
                                        etDescription.getText()
                                                .toString()
                                                .trim();


                                String audience =
                                        spAudience
                                                .getSelectedItem()
                                                .toString();


                                if (title.isEmpty()) {

                                    etTitle.setError(
                                            "Enter notice title"
                                    );

                                    etTitle.requestFocus();

                                    return;
                                }


                                if (description.isEmpty()) {

                                    etDescription.setError(
                                            "Enter notice message"
                                    );

                                    etDescription.requestFocus();

                                    return;
                                }


                                postNoticeToFirestore(
                                        title,
                                        description,
                                        audience,
                                        dialog
                                );
                            }
                    );
                }
        );


        dialog.show();
    }


    // =========================================================
    // SAVE NOTICE
    // =========================================================

    private void postNoticeToFirestore(
            String title,
            String description,
            String audience,
            AlertDialog dialog
    ) {

        if (!isAdded()) {
            return;
        }


        if (mAuth == null ||
                mAuth.getCurrentUser() == null) {

            Toast.makeText(
                    requireContext(),
                    "Admin login required.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        String createdBy =
                mAuth.getCurrentUser()
                        .getEmail();


        if (TextUtils.isEmpty(createdBy)) {
            createdBy = "Admin";
        }


        String currentDate =
                new java.text.SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                ).format(
                        new Date()
                );


        Map<String, Object> notice =
                new HashMap<>();


        notice.put(
                "schoolId",
                schoolId
        );


        notice.put(
                "title",
                title
        );


        notice.put(
                "description",
                description
        );


        notice.put(
                "date",
                currentDate
        );


        notice.put(
                "audience",
                audience
        );


        notice.put(
                "createdBy",
                createdBy
        );


        notice.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );


        notice.put(
                "active",
                true
        );


        db.collection("notices")
                .add(
                        notice
                )
                .addOnSuccessListener(
                        documentReference -> {

                            if (!isAdded()) {
                                return;
                            }


                            logActivity(
                                    "Posted notice: " +
                                            title,
                                    audience
                            );


                            dialog.dismiss();


                            Toast.makeText(
                                    requireContext(),
                                    "📢 Notice posted successfully!",
                                    Toast.LENGTH_LONG
                            ).show();


                            loadRecentActivities();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            Toast.makeText(
                                    requireContext(),
                                    "Failed to post notice:\n" +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // SEE ALL NOTICES
    // =========================================================

    private void showAllNoticesDialog() {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        LinearLayout mainLayout =
                new LinearLayout(
                        requireContext()
                );


        mainLayout.setOrientation(
                LinearLayout.VERTICAL
        );


        int padding =
                dpToPx(16);


        mainLayout.setPadding(
                padding,
                padding,
                padding,
                padding
        );


        TextView loadingText =
                new TextView(
                        requireContext()
                );


        loadingText.setText(
                "Loading notices..."
        );


        loadingText.setTextSize(
                13
        );


        loadingText.setTextColor(
                0xFF777777
        );


        loadingText.setGravity(
                Gravity.CENTER
        );


        mainLayout.addView(
                loadingText,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(60)
                )
        );


        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "📋 All Notices"
                        )
                        .setView(
                                mainLayout
                        )
                        .setNegativeButton(
                                "CLOSE",
                                null
                        )
                        .create();


        dialog.show();


        db.collection("notices")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            if (!isAdded()) {
                                return;
                            }


                            mainLayout.removeAllViews();


                            List<DocumentSnapshot> notices =
                                    new ArrayList<>(
                                            snapshot.getDocuments()
                                    );


                            Collections.sort(
                                    notices,
                                    (a, b) -> {

                                        Timestamp ta =
                                                a.getTimestamp(
                                                        "createdAt"
                                                );


                                        Timestamp tb =
                                                b.getTimestamp(
                                                        "createdAt"
                                                );


                                        if (ta == null &&
                                                tb == null) {
                                            return 0;
                                        }


                                        if (ta == null) {
                                            return 1;
                                        }


                                        if (tb == null) {
                                            return -1;
                                        }


                                        return tb.compareTo(
                                                ta
                                        );
                                    }
                            );


                            if (notices.isEmpty()) {

                                TextView emptyText =
                                        new TextView(
                                                requireContext()
                                        );


                                emptyText.setText(
                                        "No notices found"
                                );


                                emptyText.setTextSize(
                                        14
                                );


                                emptyText.setTextColor(
                                        0xFF777777
                                );


                                emptyText.setGravity(
                                        Gravity.CENTER
                                );


                                mainLayout.addView(
                                        emptyText,
                                        new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                dpToPx(80)
                                        )
                                );


                                return;
                            }


                            for (DocumentSnapshot notice :
                                    notices) {

                                addNoticeRow(
                                        mainLayout,
                                        notice,
                                        dialog
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            mainLayout.removeAllViews();


                            TextView errorText =
                                    new TextView(
                                            requireContext()
                                    );


                            errorText.setText(
                                    "Unable to load notices"
                            );


                            errorText.setTextSize(
                                    14
                            );


                            errorText.setTextColor(
                                    0xFF777777
                            );


                            errorText.setGravity(
                                    Gravity.CENTER
                            );


                            mainLayout.addView(
                                    errorText,
                                    new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            dpToPx(80)
                                    )
                            );
                        }
                );
    }


    // =========================================================
    // NOTICE ROW
    // =========================================================

    private void addNoticeRow(
            LinearLayout parent,
            DocumentSnapshot notice,
            AlertDialog allNoticesDialog
    ) {

        LinearLayout row =
                new LinearLayout(
                        requireContext()
                );


        row.setOrientation(
                LinearLayout.VERTICAL
        );


        row.setPadding(
                dpToPx(10),
                dpToPx(10),
                dpToPx(10),
                dpToPx(10)
        );


        // TITLE

        TextView title =
                new TextView(
                        requireContext()
                );


        title.setText(
                "📢 " +
                        getString(
                                notice,
                                "title"
                        )
        );


        title.setTextSize(
                14
        );


        title.setTextColor(
                0xFF24105F
        );


        row.addView(
                title
        );


        // DESCRIPTION

        TextView description =
                new TextView(
                        requireContext()
                );


        String descriptionText =
                getString(
                        notice,
                        "description"
                );


        if (descriptionText.isEmpty()) {

            descriptionText =
                    "No description";
        }


        description.setText(
                descriptionText
        );


        description.setTextSize(
                12
        );


        description.setTextColor(
                0xFF555555
        );


        description.setPadding(
                0,
                dpToPx(4),
                0,
                0
        );


        row.addView(
                description
        );


        // DATE + AUDIENCE

        TextView info =
                new TextView(
                        requireContext()
                );


        String date =
                getString(
                        notice,
                        "date"
                );


        String audience =
                getString(
                        notice,
                        "audience"
                );


        String infoText =
                "📅 " +
                        date;


        if (!audience.isEmpty()) {

            infoText +=
                    "   •   " +
                            audience;
        }


        info.setText(
                infoText
        );


        info.setTextSize(
                10
        );


        info.setTextColor(
                0xFF777777
        );


        info.setPadding(
                0,
                dpToPx(4),
                0,
                0
        );


        row.addView(
                info
        );


        // BUTTON ROW

        LinearLayout buttonRow =
                new LinearLayout(
                        requireContext()
                );


        buttonRow.setOrientation(
                LinearLayout.HORIZONTAL
        );


        buttonRow.setGravity(
                Gravity.END
        );


        buttonRow.setPadding(
                0,
                dpToPx(6),
                0,
                0
        );


        // EDIT BUTTON

        Button editButton =
                new Button(
                        requireContext()
                );


        editButton.setText(
                "✏️ EDIT"
        );


        editButton.setTextSize(
                10
        );


        editButton.setTextColor(
                0xFF42179B
        );


        editButton.setAllCaps(
                false
        );


        editButton.setOnClickListener(
                v -> showEditNoticeDialog(
                        notice,
                        allNoticesDialog
                )
        );


        buttonRow.addView(
                editButton,
                new LinearLayout.LayoutParams(
                        dpToPx(95),
                        dpToPx(42)
                )
        );


        // DELETE BUTTON

        Button deleteButton =
                new Button(
                        requireContext()
                );


        deleteButton.setText(
                "🗑 DELETE"
        );


        deleteButton.setTextSize(
                10
        );


        deleteButton.setTextColor(
                0xFFD32F2F
        );


        deleteButton.setAllCaps(
                false
        );


        deleteButton.setOnClickListener(
                v -> showDeleteNoticeDialog(
                        notice,
                        allNoticesDialog
                )
        );


        buttonRow.addView(
                deleteButton,
                new LinearLayout.LayoutParams(
                        dpToPx(100),
                        dpToPx(42)
                )
        );


        row.addView(
                buttonRow
        );


        // DIVIDER

        View divider =
                new View(
                        requireContext()
                );


        divider.setBackgroundColor(
                0xFFE5E5E5
        );


        parent.addView(
                row,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );


        parent.addView(
                divider,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                )
        );
    }


    // =========================================================
    // EDIT NOTICE
    // =========================================================

    private void showEditNoticeDialog(
            DocumentSnapshot notice,
            AlertDialog allNoticesDialog
    ) {

        if (!isAdded()) {
            return;
        }


        LinearLayout layout =
                createDialogLayout();


        // TITLE

        EditText etTitle =
                createSingleLineEditText(
                        "Notice Title",
                        48
                );


        etTitle.setText(
                getString(
                        notice,
                        "title"
                )
        );


        addField(
                layout,
                etTitle,
                8,
                8
        );


        // DESCRIPTION

        EditText etDescription =
                createMultiLineEditText(
                        "Write notice message...",
                        72
                );


        etDescription.setText(
                getString(
                        notice,
                        "description"
                )
        );


        addField(
                layout,
                etDescription,
                0,
                8
        );


        // AUDIENCE LABEL

        TextView audienceLabel =
                createLabel(
                        "Who should receive this notice?"
                );


        layout.addView(
                audienceLabel
        );


        // IMPORTANT:
        // ArrayAdapter<String> fixes CAP#1 error.

        String[] audienceOptions = {
                "All",
                "Only Student",
                "Only Staff"
        };


        ArrayAdapter<String> audienceAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        audienceOptions
                );


        audienceAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        Spinner spAudience =
                new Spinner(
                        requireContext()
                );


        spAudience.setAdapter(
                audienceAdapter
        );


        String savedAudience =
                getString(
                        notice,
                        "audience"
                );


        int selectedPosition =
                audienceAdapter.getPosition(
                        savedAudience
                );


        if (selectedPosition >= 0) {

            spAudience.setSelection(
                    selectedPosition
            );
        }


        layout.addView(
                spAudience,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(48)
                )
        );


        // DIALOG

        AlertDialog editDialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "✏️ Edit Notice"
                        )
                        .setView(
                                layout
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "UPDATE",
                                null
                        )
                        .create();


        editDialog.setOnShowListener(
                dialogInterface -> {

                    Button updateButton =
                            editDialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );


                    updateButton.setOnClickListener(
                            v -> {

                                String title =
                                        etTitle.getText()
                                                .toString()
                                                .trim();


                                String description =
                                        etDescription.getText()
                                                .toString()
                                                .trim();


                                String audience =
                                        spAudience
                                                .getSelectedItem()
                                                .toString();


                                if (title.isEmpty()) {

                                    etTitle.setError(
                                            "Enter notice title"
                                    );

                                    etTitle.requestFocus();

                                    return;
                                }


                                if (description.isEmpty()) {

                                    etDescription.setError(
                                            "Enter notice message"
                                    );

                                    etDescription.requestFocus();

                                    return;
                                }


                                updateNotice(
                                        notice.getId(),
                                        title,
                                        description,
                                        audience,
                                        editDialog,
                                        allNoticesDialog
                                );
                            }
                    );
                }
        );


        editDialog.show();
    }


    // =========================================================
    // UPDATE NOTICE
    // =========================================================

    private void updateNotice(
            String noticeId,
            String title,
            String description,
            String audience,
            AlertDialog editDialog,
            AlertDialog allNoticesDialog
    ) {

        if (!isAdded()) {
            return;
        }


        Map<String, Object> updates =
                new HashMap<>();


        updates.put(
                "title",
                title
        );


        updates.put(
                "description",
                description
        );


        updates.put(
                "audience",
                audience
        );


        updates.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );


        db.collection("notices")
                .document(noticeId)
                .update(
                        updates
                )
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }


                            editDialog.dismiss();

                            allNoticesDialog.dismiss();


                            Toast.makeText(
                                    requireContext(),
                                    "✏️ Notice updated successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();


                            logActivity(
                                    "Updated notice: " +
                                            title,
                                    audience
                            );


                            loadRecentActivities();


                            showAllNoticesDialog();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            Toast.makeText(
                                    requireContext(),
                                    "Failed to update notice:\n" +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // DELETE NOTICE CONFIRMATION
    // =========================================================

    private void showDeleteNoticeDialog(
            DocumentSnapshot notice,
            AlertDialog allNoticesDialog
    ) {

        if (!isAdded()) {
            return;
        }


        String title =
                getString(
                        notice,
                        "title"
                );


        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "🗑 Delete Notice"
                )
                .setMessage(
                        "Are you sure you want to delete\n\"" +
                                title +
                                "\"?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "DELETE",
                        (dialog, which) ->
                                deleteNotice(
                                        notice.getId(),
                                        title,
                                        allNoticesDialog
                                )
                )
                .show();
    }


    // =========================================================
    // DELETE NOTICE
    // =========================================================

    private void deleteNotice(
            String noticeId,
            String title,
            AlertDialog allNoticesDialog
    ) {

        if (!isAdded()) {
            return;
        }


        db.collection("notices")
                .document(noticeId)
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            if (!isAdded()) {
                                return;
                            }


                            allNoticesDialog.dismiss();


                            Toast.makeText(
                                    requireContext(),
                                    "🗑 Notice deleted successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();


                            logActivity(
                                    "Deleted notice: " +
                                            title,
                                    "All"
                            );


                            loadRecentActivities();


                            showAllNoticesDialog();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            Toast.makeText(
                                    requireContext(),
                                    "Failed to delete notice:\n" +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // SPECIAL OCCASION
    // =========================================================

    private void showSpecialOccasionDialog() {

        if (!isAdded()) {
            return;
        }


        LinearLayout layout =
                createDialogLayout();


        // TITLE

        EditText etTitle =
                createSingleLineEditText(
                        "Occasion Title",
                        48
                );


        addField(
                layout,
                etTitle,
                8,
                8
        );


        // DATE

        EditText etDate =
                createSingleLineEditText(
                        "Select Date",
                        48
                );


        etDate.setFocusable(false);
        etDate.setClickable(true);


        addField(
                layout,
                etDate,
                0,
                8
        );


        etDate.setOnClickListener(
                v -> showDatePicker(
                        etDate
                )
        );


        // DESCRIPTION

        EditText etDescription =
                createSingleLineEditText(
                        "Description",
                        48
                );


        addField(
                layout,
                etDescription,
                0,
                8
        );


        // AUDIENCE

        TextView audienceLabel =
                createLabel(
                        "Audience"
                );


        layout.addView(
                audienceLabel
        );


        Spinner spAudience =
                createAudienceSpinner();


        layout.addView(
                spAudience,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(48)
                )
        );


        // DIALOG

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "🎉 Special Occasion"
                        )
                        .setView(
                                layout
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "ADD OCCASION",
                                null
                        )
                        .create();


        dialog.setOnShowListener(
                dialogInterface -> {

                    Button positiveButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );


                    positiveButton.setOnClickListener(
                            v -> {

                                String title =
                                        etTitle.getText()
                                                .toString()
                                                .trim();


                                String date =
                                        etDate.getText()
                                                .toString()
                                                .trim();


                                String description =
                                        etDescription.getText()
                                                .toString()
                                                .trim();


                                String audience =
                                        spAudience
                                                .getSelectedItem()
                                                .toString();


                                if (title.isEmpty()) {

                                    etTitle.setError(
                                            "Enter occasion title"
                                    );

                                    etTitle.requestFocus();

                                    return;
                                }


                                if (date.isEmpty()) {

                                    etDate.setError(
                                            "Select date"
                                    );

                                    return;
                                }


                                saveSpecialOccasion(
                                        title,
                                        date,
                                        description,
                                        audience,
                                        dialog
                                );
                            }
                    );
                }
        );


        dialog.show();
    }


    // =========================================================
    // SAVE SPECIAL OCCASION
    // =========================================================

    private void saveSpecialOccasion(
            String title,
            String date,
            String description,
            String audience,
            AlertDialog dialog
    ) {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        Map<String, Object> data =
                new HashMap<>();


        data.put(
                "schoolId",
                schoolId
        );


        data.put(
                "title",
                title
        );


        data.put(
                "date",
                date
        );


        data.put(
                "description",
                description
        );


        data.put(
                "audience",
                audience
        );


        data.put(
                "status",
                "Upcoming"
        );


        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );


        db.collection("specialOccasions")
                .add(
                        data
                )
                .addOnSuccessListener(
                        documentReference -> {

                            if (!isAdded()) {
                                return;
                            }


                            logActivity(
                                    "Added special occasion: " +
                                            title,
                                    audience
                            );


                            dialog.dismiss();


                            Toast.makeText(
                                    requireContext(),
                                    "🎉 Occasion added successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();


                            loadSpecialOccasions();

                            loadRecentActivities();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            Toast.makeText(
                                    requireContext(),
                                    "Failed to add occasion:\n" +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // LOAD SPECIAL OCCASIONS
    // =========================================================

    private void loadSpecialOccasions() {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();

        db.collection("specialOccasions")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    Date today = startOfToday();
                    DocumentSnapshot nearest = null;
                    Date nearestDate = null;

                    for (DocumentSnapshot item : snapshot.getDocuments()) {

                        Date itemDate = parseDdMmYyyy(
                                getString(item, "date")
                        );

                        if (itemDate == null) {
                            continue;
                        }

                        // Expired dates are ignored.
                        if (itemDate.before(today)) {
                            continue;
                        }

                        if (nearestDate == null || itemDate.before(nearestDate)) {
                            nearestDate = itemDate;
                            nearest = item;
                        }
                    }

                    if (nearest == null) {

                        tvSpecialOccasion1.setText(
                                "• No occasions scheduled"
                        );

                        tvSpecialOccasion2.setText("");
                        return;
                    }

                    String title =
                            getString(nearest, "title");

                    String date =
                            getString(nearest, "date");

                    String audience =
                            getString(nearest, "audience");

                    tvSpecialOccasion1.setText(
                            "• " + title
                    );

                    String secondLine =
                            "📅 " + date;

                    if (!audience.isEmpty() &&
                            !audience.equalsIgnoreCase("All")) {

                        secondLine +=
                                "  • " + audience;
                    }

                    tvSpecialOccasion2.setText(
                            secondLine
                    );
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    tvSpecialOccasion1.setText(
                            "• No occasions scheduled"
                    );

                    tvSpecialOccasion2.setText("");
                });
    }


    // =========================================================
    // MEETING DIALOG
    // =========================================================

    private void showScheduleMeetingDialog() {

        if (!isAdded()) {
            return;
        }


        LinearLayout layout =
                createDialogLayout();


        // MEETING TYPE

        TextView meetingTypeLabel =
                createLabel(
                        "Meeting Type"
                );


        layout.addView(
                meetingTypeLabel
        );


        Spinner spMeetingType =
                new Spinner(
                        requireContext()
                );


        String[] meetingTypes = {
                "Parent-Teacher Meeting",
                "Staff Meeting",
                "Teacher Meeting",
                "Management Meeting",
                "Other"
        };


        ArrayAdapter<String> meetingAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        meetingTypes
                );


        meetingAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spMeetingType.setAdapter(
                meetingAdapter
        );


        layout.addView(
                spMeetingType,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(48)
                )
        );


        // DATE

        EditText etDate =
                createSingleLineEditText(
                        "Select Date",
                        48
                );


        etDate.setFocusable(false);
        etDate.setClickable(true);


        addField(
                layout,
                etDate,
                4,
                8
        );


        etDate.setOnClickListener(
                v -> showDatePicker(
                        etDate
                )
        );


        // TIME

        EditText etTime =
                createSingleLineEditText(
                        "Select Time",
                        48
                );


        etTime.setFocusable(false);
        etTime.setClickable(true);


        addField(
                layout,
                etTime,
                0,
                8
        );


        etTime.setOnClickListener(
                v -> showTimePicker(
                        etTime
                )
        );


        // VENUE

        EditText etVenue =
                createSingleLineEditText(
                        "Venue",
                        48
                );


        addField(
                layout,
                etVenue,
                0,
                8
        );


        // DESCRIPTION

        EditText etDescription =
                createSingleLineEditText(
                        "Description",
                        48
                );


        addField(
                layout,
                etDescription,
                0,
                8
        );


        // AUDIENCE

        TextView audienceLabel =
                createLabel(
                        "Audience"
                );


        layout.addView(
                audienceLabel
        );


        Spinner spAudience =
                createAudienceSpinner();


        layout.addView(
                spAudience,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(48)
                )
        );


        // DIALOG

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "🗓️ Schedule Meeting"
                        )
                        .setView(
                                layout
                        )
                        .setNegativeButton(
                                "CANCEL",
                                null
                        )
                        .setPositiveButton(
                                "SCHEDULE",
                                null
                        )
                        .create();


        dialog.setOnShowListener(
                dialogInterface -> {

                    Button positiveButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );


                    positiveButton.setOnClickListener(
                            v -> {

                                String type =
                                        spMeetingType
                                                .getSelectedItem()
                                                .toString();


                                String date =
                                        etDate.getText()
                                                .toString()
                                                .trim();


                                String time =
                                        etTime.getText()
                                                .toString()
                                                .trim();


                                String venue =
                                        etVenue.getText()
                                                .toString()
                                                .trim();


                                String description =
                                        etDescription.getText()
                                                .toString()
                                                .trim();


                                String audience =
                                        spAudience
                                                .getSelectedItem()
                                                .toString();


                                if (date.isEmpty()) {

                                    etDate.setError(
                                            "Select date"
                                    );

                                    return;
                                }


                                if (time.isEmpty()) {

                                    etTime.setError(
                                            "Select time"
                                    );

                                    return;
                                }


                                saveMeeting(
                                        type,
                                        date,
                                        time,
                                        venue,
                                        description,
                                        audience,
                                        dialog
                                );
                            }
                    );
                }
        );


        dialog.show();
    }


    // =========================================================
    // SAVE MEETING
    // =========================================================

    private void saveMeeting(
            String type,
            String date,
            String time,
            String venue,
            String description,
            String audience,
            AlertDialog dialog
    ) {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        Map<String, Object> data =
                new HashMap<>();


        data.put(
                "schoolId",
                schoolId
        );


        data.put(
                "type",
                type
        );


        data.put(
                "title",
                type
        );


        data.put(
                "date",
                date
        );


        data.put(
                "time",
                time
        );


        data.put(
                "venue",
                venue
        );


        data.put(
                "description",
                description
        );


        data.put(
                "audience",
                audience
        );


        data.put(
                "status",
                "Upcoming"
        );


        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );


        db.collection("meetings")
                .add(
                        data
                )
                .addOnSuccessListener(
                        documentReference -> {

                            if (!isAdded()) {
                                return;
                            }


                            logActivity(
                                    "Scheduled meeting: " +
                                            type,
                                    audience
                            );


                            dialog.dismiss();


                            Toast.makeText(
                                    requireContext(),
                                    "🗓️ Meeting scheduled successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();


                            loadMeetings();

                            loadRecentActivities();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            Toast.makeText(
                                    requireContext(),
                                    "Failed to schedule meeting:\n" +
                                            e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // LOAD MEETINGS
    // =========================================================

    private void loadMeetings() {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();

        db.collection("meetings")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    Date today = startOfToday();
                    DocumentSnapshot nearest = null;
                    Date nearestDate = null;

                    for (DocumentSnapshot item : snapshot.getDocuments()) {

                        Date itemDate = parseDdMmYyyy(
                                getString(item, "date")
                        );

                        if (itemDate == null) {
                            continue;
                        }

                        // Expired dates are ignored.
                        if (itemDate.before(today)) {
                            continue;
                        }

                        // If multiple meetings are today, keep the nearest
                        // time when the date is the same.
                        if (nearestDate == null || itemDate.before(nearestDate)) {
                            nearestDate = itemDate;
                            nearest = item;
                        } else if (itemDate.equals(nearestDate) && nearest != null) {

                            String currentTime = getString(item, "time");
                            String oldTime = getString(nearest, "time");

                            if (!currentTime.isEmpty() &&
                                    (oldTime.isEmpty() ||
                                            currentTime.compareTo(oldTime) < 0)) {
                                nearest = item;
                            }
                        }
                    }

                    if (nearest == null) {

                        tvMeeting1.setText(
                                "• No meetings scheduled"
                        );

                        tvMeeting2.setText("");
                        return;
                    }

                    String title =
                            getString(nearest, "title");

                    String date =
                            getString(nearest, "date");

                    String time =
                            getString(nearest, "time");

                    String venue =
                            getString(nearest, "venue");

                    tvMeeting1.setText(
                            "• " + title
                    );

                    String secondLine =
                            "📅 " + date;

                    if (!time.isEmpty()) {
                        secondLine += "  " + time;
                    }

                    if (!venue.isEmpty()) {
                        secondLine += "  • " + venue;
                    }

                    tvMeeting2.setText(
                            secondLine
                    );
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    tvMeeting1.setText(
                            "• No meetings scheduled"
                    );

                    tvMeeting2.setText("");
                });
    }


    // =========================================================
    // LOAD TODAY'S BIRTHDAY COUNT
    // =========================================================

    private void loadTodayBirthdayCount() {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();

        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH) + 1;

        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    int count = 0;

                    for (DocumentSnapshot student : snapshot.getDocuments()) {

                        String dob =
                                getString(student, "dateOfBirth");

                        if (dob.isEmpty()) {
                            continue;
                        }

                        Calendar dobCalendar = parseDob(dob);

                        if (dobCalendar == null) {
                            continue;
                        }

                        int dobDay =
                                dobCalendar.get(Calendar.DAY_OF_MONTH);

                        int dobMonth =
                                dobCalendar.get(Calendar.MONTH) + 1;

                        // IMPORTANT: year is intentionally ignored.
                        if (dobDay == todayDay &&
                                dobMonth == todayMonth) {
                            count++;
                        }
                    }

                    tvBirthdayCount.setText(
                            String.valueOf(count)
                    );
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    tvBirthdayCount.setText("0");
                });
    }


    // =========================================================
    // TODAY'S BIRTHDAY DIALOG
    // =========================================================

    private void showTodayBirthdaysDialog() {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }

        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();

        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH) + 1;

        LinearLayout listLayout =
                createDialogLayout();

        TextView loading =
                new TextView(requireContext());

        loading.setText("Loading birthdays...");
        loading.setTextSize(13);
        loading.setTextColor(0xFF777777);
        loading.setGravity(Gravity.CENTER);

        listLayout.addView(
                loading,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(60)
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("🎂 Today's Birthdays")
                        .setView(listLayout)
                        .setNegativeButton("CLOSE", null)
                        .create();

        dialog.show();

        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    listLayout.removeAllViews();

                    int count = 0;

                    for (DocumentSnapshot student : snapshot.getDocuments()) {

                        String dob = getString(student, "dateOfBirth");
                        Calendar dobCalendar = parseDob(dob);

                        if (dobCalendar == null) {
                            continue;
                        }

                        if (dobCalendar.get(Calendar.DAY_OF_MONTH) != todayDay ||
                                dobCalendar.get(Calendar.MONTH) + 1 != todayMonth) {
                            continue;
                        }

                        count++;

                        String firstName = getString(student, "firstName");
                        String middleName = getString(student, "middleName");
                        String lastName = getString(student, "lastName");

                        StringBuilder name = new StringBuilder();

                        if (!firstName.isEmpty()) name.append(firstName);
                        if (!middleName.isEmpty()) {
                            if (name.length() > 0) name.append(" ");
                            name.append(middleName);
                        }
                        if (!lastName.isEmpty()) {
                            if (name.length() > 0) name.append(" ");
                            name.append(lastName);
                        }

                        if (name.length() == 0) {
                            name.append("Student");
                        }

                        String className = getString(student, "className");
                        String division = getString(student, "division");

                        String info = "🎂 " + name;

                        if (!className.isEmpty()) {
                            info += "\n   Class: " + className;

                            if (!division.isEmpty()) {
                                info += " - " + division;
                            }
                        }

                        TextView row =
                                new TextView(requireContext());

                        row.setText(info);
                        row.setTextSize(13);
                        row.setTextColor(0xFF333333);
                        row.setPadding(
                                dpToPx(10),
                                dpToPx(10),
                                dpToPx(10),
                                dpToPx(10)
                        );

                        listLayout.addView(
                                row,
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                        );

                        View divider = new View(requireContext());
                        divider.setBackgroundColor(0xFFE5E5E5);

                        listLayout.addView(
                                divider,
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        dpToPx(1)
                                )
                        );
                    }

                    if (count == 0) {

                        TextView empty =
                                new TextView(requireContext());

                        empty.setText("No birthdays today");
                        empty.setTextSize(14);
                        empty.setTextColor(0xFF777777);
                        empty.setGravity(Gravity.CENTER);

                        listLayout.addView(
                                empty,
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        dpToPx(70)
                                )
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    listLayout.removeAllViews();

                    TextView error =
                            new TextView(requireContext());

                    error.setText("Unable to load birthdays");
                    error.setTextSize(14);
                    error.setTextColor(0xFF777777);
                    error.setGravity(Gravity.CENTER);

                    listLayout.addView(
                            error,
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    dpToPx(70)
                            )
                    );
                });
    }


    // =========================================================
    // PARSE DOB
    // =========================================================

    private Calendar parseDob(String dob) {

        if (dob == null || dob.trim().isEmpty()) {
            return null;
        }

        String value = dob.trim();
        String[] patterns = {
                "dd/MM/yyyy",
                "d/M/yyyy",
                "dd-MM-yyyy",
                "d-M-yyyy"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf =
                        new SimpleDateFormat(pattern, Locale.getDefault());
                sdf.setLenient(false);

                Date parsed = sdf.parse(value);

                if (parsed != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(parsed);
                    return calendar;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }


    // =========================================================
    // START OF TODAY
    // =========================================================

    private Date startOfToday() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }


    // =========================================================
    // PARSE DD/MM/YYYY
    // =========================================================

    private Date parseDdMmYyyy(String dateText) {

        if (dateText == null || dateText.trim().isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    );

            sdf.setLenient(false);
            Date parsed = sdf.parse(dateText.trim());

            if (parsed == null) {
                return null;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTime();

        } catch (Exception e) {
            return null;
        }
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void showDatePicker(
            EditText target
    ) {

        Calendar calendar =
                Calendar.getInstance();


        DatePickerDialog picker =
                new DatePickerDialog(
                        requireContext(),
                        (view, year, month, dayOfMonth) -> {

                            String selectedDate =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );


                            target.setText(
                                    selectedDate
                            );
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


        picker.show();
    }


    // =========================================================
    // TIME PICKER
    // =========================================================

    private void showTimePicker(
            EditText target
    ) {

        Calendar calendar =
                Calendar.getInstance();


        TimePickerDialog picker =
                new TimePickerDialog(
                        requireContext(),
                        (view, hourOfDay, minute) -> {

                            String selectedTime =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d",
                                            hourOfDay,
                                            minute
                                    );


                            target.setText(
                                    selectedTime
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


        picker.show();
    }


    // =========================================================
    // CREATE DIALOG LAYOUT
    // =========================================================

    private LinearLayout createDialogLayout() {

        LinearLayout layout =
                new LinearLayout(
                        requireContext()
                );


        layout.setOrientation(
                LinearLayout.VERTICAL
        );


        int padding =
                dpToPx(18);


        layout.setPadding(
                padding,
                0,
                padding,
                0
        );


        return layout;
    }


    // =========================================================
    // SINGLE LINE EDIT TEXT
    // =========================================================

    private EditText createSingleLineEditText(
            String hint,
            int heightDp
    ) {

        EditText editText =
                new EditText(
                        requireContext()
                );


        editText.setHint(
                hint
        );


        editText.setSingleLine(
                true
        );


        editText.setTextSize(
                12
        );


        editText.setGravity(
                Gravity.CENTER_VERTICAL
        );


        editText.setPadding(
                0,
                0,
                0,
                0
        );


        editText.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );


        editText.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(heightDp)
                )
        );


        return editText;
    }


    // =========================================================
    // MULTI LINE EDIT TEXT
    // =========================================================

    private EditText createMultiLineEditText(
            String hint,
            int heightDp
    ) {

        EditText editText =
                new EditText(
                        requireContext()
                );


        editText.setHint(
                hint
        );


        editText.setTextSize(
                12
        );


        editText.setGravity(
                Gravity.TOP
        );


        editText.setPadding(
                0,
                dpToPx(6),
                0,
                dpToPx(4)
        );


        editText.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );


        editText.setMinLines(
                2
        );


        editText.setMaxLines(
                3
        );


        editText.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(heightDp)
                )
        );


        return editText;
    }


    // =========================================================
    // ADD FIELD — FIXED
    // =========================================================

    private void addField(
            LinearLayout layout,
            View field,
            int topMarginDp,
            int bottomMarginDp
    ) {

        int height =
                LinearLayout.LayoutParams.WRAP_CONTENT;


        if (field.getLayoutParams() != null &&
                field.getLayoutParams().height > 0) {

            height =
                    field.getLayoutParams().height;
        }


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        height
                );


        params.setMargins(
                0,
                dpToPx(topMarginDp),
                0,
                dpToPx(bottomMarginDp)
        );


        layout.addView(
                field,
                params
        );
    }


    // =========================================================
    // LABEL
    // =========================================================

    private TextView createLabel(
            String text
    ) {

        TextView label =
                new TextView(
                        requireContext()
                );


        label.setText(
                text
        );


        label.setTextSize(
                12
        );


        label.setTextColor(
                0xFF555555
        );


        label.setPadding(
                0,
                dpToPx(2),
                0,
                dpToPx(3)
        );


        return label;
    }


    // =========================================================
    // AUDIENCE SPINNER
    // =========================================================

    private Spinner createAudienceSpinner() {

        Spinner spinner =
                new Spinner(
                        requireContext()
                );


        String[] audienceOptions = {
                "All",
                "Only Student",
                "Only Staff"
        };


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        audienceOptions
                );


        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );


        spinner.setAdapter(
                adapter
        );


        return spinner;
    }


    // =========================================================
    // LOG ACTIVITY
    // =========================================================

    private void logActivity(
            String activityText,
            String audience
    ) {

        if (mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        String userEmail =
                mAuth.getCurrentUser()
                        .getEmail();


        if (TextUtils.isEmpty(userEmail)) {
            userEmail = "Admin";
        }


        Map<String, Object> activity =
                new HashMap<>();


        activity.put(
                "schoolId",
                schoolId
        );


        activity.put(
                "activity",
                activityText
        );


        activity.put(
                "type",
                "Admin"
        );


        activity.put(
                "audience",
                audience
        );


        activity.put(
                "createdBy",
                userEmail
        );


        activity.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );


        db.collection("activities")
                .add(
                        activity
                );
    }


    // =========================================================
    // LOAD RECENT ACTIVITIES
    // =========================================================

    private void loadRecentActivities() {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("activities")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .limit(50)
                .get()
                .addOnSuccessListener(
                        querySnapshot -> {

                            if (!isAdded()) {
                                return;
                            }


                            recentActivities.clear();


                            recentActivities.addAll(
                                    querySnapshot.getDocuments()
                            );


                            Collections.sort(
                                    recentActivities,
                                    (a, b) -> {

                                        Timestamp ta =
                                                a.getTimestamp(
                                                        "createdAt"
                                                );


                                        Timestamp tb =
                                                b.getTimestamp(
                                                        "createdAt"
                                                );


                                        if (ta == null &&
                                                tb == null) {
                                            return 0;
                                        }


                                        if (ta == null) {
                                            return 1;
                                        }


                                        if (tb == null) {
                                            return -1;
                                        }


                                        return tb.compareTo(
                                                ta
                                        );
                                    }
                            );


                            displayRecentActivities();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            clearRecentActivities();
                        }
                );
    }


    // =========================================================
    // DISPLAY RECENT ACTIVITIES
    // =========================================================

    private void displayRecentActivities() {

        TextView[] activityViews = {
                tvActivity1,
                tvActivity2,
                tvActivity3,
                tvActivity4
        };


        clearRecentActivities();


        int limit =
                Math.min(
                        4,
                        recentActivities.size()
                );


        for (int i = 0;
             i < limit;
             i++) {

            DocumentSnapshot activity =
                    recentActivities.get(i);


            String activityText =
                    getString(
                            activity,
                            "activity"
                    );


            String audience =
                    getString(
                            activity,
                            "audience"
                    );


            Timestamp timestamp =
                    activity.getTimestamp(
                            "createdAt"
                    );


            String timeText =
                    "";


            if (timestamp != null) {

                timeText =
                        new java.text.SimpleDateFormat(
                                "dd MMM, hh:mm a",
                                Locale.getDefault()
                        ).format(
                                timestamp.toDate()
                        );
            }


            String finalText =
                    "• " +
                            activityText;


            if (!audience.isEmpty() &&
                    !audience.equalsIgnoreCase(
                            "All"
                    )) {

                finalText +=
                        " [" +
                                audience +
                                "]";
            }


            if (!timeText.isEmpty()) {

                finalText +=
                        "\n  " +
                                timeText;
            }


            activityViews[i].setText(
                    finalText
            );


            activityViews[i].setVisibility(
                    View.VISIBLE
            );
        }


        if (limit == 0) {

            tvActivity1.setText(
                    "No recent activities"
            );


            tvActivity1.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // CLEAR ACTIVITY DISPLAY
    // =========================================================

    private void clearRecentActivities() {

        if (tvActivity1 != null) {

            tvActivity1.setText("");

            tvActivity1.setVisibility(
                    View.VISIBLE
            );
        }


        if (tvActivity2 != null) {

            tvActivity2.setText("");

            tvActivity2.setVisibility(
                    View.VISIBLE
            );
        }


        if (tvActivity3 != null) {

            tvActivity3.setText("");

            tvActivity3.setVisibility(
                    View.VISIBLE
            );
        }


        if (tvActivity4 != null) {

            tvActivity4.setText("");

            tvActivity4.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // CLEAR ALL ACTIVITIES CONFIRMATION
    // =========================================================

    private void showClearActivitiesDialog() {

        if (!isAdded()) {
            return;
        }


        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "🗑 Clear All"
                )
                .setMessage(
                        "Clear all recent activities?"
                )
                .setNegativeButton(
                        "CANCEL",
                        null
                )
                .setPositiveButton(
                        "CLEAR ALL",
                        (dialog, which) ->
                                clearAllActivities()
                )
                .show();
    }


    // =========================================================
    // CLEAR ONLY ACTIVITIES
    // =========================================================

    private void clearAllActivities() {

        if (!isAdded() ||
                mAuth == null ||
                mAuth.getCurrentUser() == null) {

            return;
        }


        String schoolId =
                mAuth.getCurrentUser()
                        .getUid();


        db.collection("activities")
                .whereEqualTo(
                        "schoolId",
                        schoolId
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            if (!isAdded()) {
                                return;
                            }


                            if (snapshot.isEmpty()) {

                                recentActivities.clear();

                                clearRecentActivities();


                                tvActivity1.setText(
                                        "No recent activities"
                                );


                                return;
                            }


                            WriteBatch batch =
                                    db.batch();


                            for (DocumentSnapshot document :
                                    snapshot.getDocuments()) {

                                batch.delete(
                                        document.getReference()
                                );
                            }


                            batch.commit()
                                    .addOnSuccessListener(
                                            unused -> {

                                                if (!isAdded()) {
                                                    return;
                                                }


                                                // ONLY ACTIVITY DATA
                                                recentActivities.clear();


                                                clearRecentActivities();


                                                tvActivity1.setText(
                                                        "No recent activities"
                                                );


                                                Toast.makeText(
                                                        requireContext(),
                                                        "✅ Recent activities cleared!",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                    )
                                    .addOnFailureListener(
                                            e -> {

                                                if (!isAdded()) {
                                                    return;
                                                }


                                                Toast.makeText(
                                                        requireContext(),
                                                        "Failed to clear activities:\n" +
                                                                e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }


                            Toast.makeText(
                                    requireContext(),
                                    "Unable to clear activities.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // GET STRING
    // =========================================================

    private String getString(
            DocumentSnapshot document,
            String field
    ) {

        String value =
                document.getString(
                        field
                );


        if (value == null) {
            return "";
        }


        return value.trim();
    }


    // =========================================================
    // DP TO PX
    // =========================================================

    private int dpToPx(
            int dp
    ) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        return Math.round(
                dp * density
        );
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    private void logoutUser() {

        if (mAuth != null) {

            mAuth.signOut();
        }


        SharedPreferences preferences =
                requireActivity()
                        .getSharedPreferences(
                                "MySchoolPrefs",
                                MODE_PRIVATE
                        );


        preferences.edit()
                .clear()
                .apply();


        Intent intent =
                new Intent(
                        requireActivity(),
                        LoginActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(
                intent
        );


        requireActivity().finish();
    }
    @Override
    public void onDestroyView() {

        if (todayStudentAttendanceListener != null) {
            todayStudentAttendanceListener.remove();
            todayStudentAttendanceListener = null;
        }

        if (todayStaffAttendanceListener != null) {
            todayStaffAttendanceListener.remove();
            todayStaffAttendanceListener = null;
        }

        super.onDestroyView();
    }


    private void listenTodayPresent() {

        if (db == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String schoolId =
                FirebaseAuth.getInstance().getCurrentUser().getUid();

        String studentToday =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd",
                        java.util.Locale.getDefault()
                ).format(new java.util.Date());

        String staffToday =
                new java.text.SimpleDateFormat(
                        "dd/MM/yyyy",
                        java.util.Locale.getDefault()
                ).format(new java.util.Date());


        // =========================
        // STUDENT ATTENDANCE
        // =========================

        if (todayStudentAttendanceListener != null) {
            todayStudentAttendanceListener.remove();
        }

        todayStudentAttendanceListener =
                db.collection("studentAttendance")
                        .whereEqualTo("schoolId", schoolId)
                        .whereEqualTo("date", studentToday)
                        .addSnapshotListener((snapshot, error) -> {

                            if (error != null || snapshot == null) {
                                return;
                            }

                            todayStudentPresent = 0;
                            todayGirlsPresent = 0;
                            todayBoysPresent = 0;

                            for (DocumentSnapshot doc : snapshot.getDocuments()) {

                                String status = doc.getString("status");

                                if (!"Present".equalsIgnoreCase(status)) {
                                    continue;
                                }

                                todayStudentPresent++;

                                String gender = doc.getString("gender");

                                if ("Female".equalsIgnoreCase(gender)
                                        || "Girl".equalsIgnoreCase(gender)
                                        || "F".equalsIgnoreCase(gender)) {

                                    todayGirlsPresent++;

                                } else if ("Male".equalsIgnoreCase(gender)
                                        || "Boy".equalsIgnoreCase(gender)
                                        || "M".equalsIgnoreCase(gender)) {

                                    todayBoysPresent++;
                                }
                            }

                            updateTodayPresentCard();
                        });


        // =========================
        // STAFF ATTENDANCE
        // =========================

        if (todayStaffAttendanceListener != null) {
            todayStaffAttendanceListener.remove();
        }

        todayStaffAttendanceListener =
                db.collection("attendance")
                        .whereEqualTo("schoolId", schoolId)
                        .whereEqualTo("date", staffToday)
                        .addSnapshotListener((snapshot, error) -> {

                            if (error != null || snapshot == null) {
                                return;
                            }

                            todayStaffPresent = 0;

                            for (DocumentSnapshot doc : snapshot.getDocuments()) {

                                String status = doc.getString("status");

                                if ("P".equalsIgnoreCase(status)
                                        || "Present".equalsIgnoreCase(status)) {

                                    todayStaffPresent++;
                                }
                            }

                            updateTodayPresentCard();
                        });
    }


    private void updateTodayPresentCard() {
        int totalPresent = todayStudentPresent + todayStaffPresent;

        if (tvPresentCount != null) {
            tvPresentCount.setText(String.valueOf(totalPresent));
        }
    }


    private void showTodayPresentDialog() {

        if (!isAdded()) {
            return;
        }

        LinearLayout layout =
                new LinearLayout(requireContext());

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                50,
                20,
                50,
                20
        );


        addTodayPresentRow(
                layout,
                "Total Present",
                todayStaffPresent + todayStudentPresent
        );

        addTodayPresentRow(
                layout,
                "Staff",
                todayStaffPresent
        );

        addTodayPresentRow(
                layout,
                "Students",
                todayStudentPresent
        );

        addTodayPresentRow(
                layout,
                "Girls",
                todayGirlsPresent
        );

        addTodayPresentRow(
                layout,
                "Boys",
                todayBoysPresent
        );


        new AlertDialog.Builder(requireContext())
                .setTitle("Today's Present")
                .setView(layout)
                .setPositiveButton("OK", null)
                .show();
    }


    private void addTodayPresentRow(
            LinearLayout parent,
            String title,
            int count
    ) {

        TextView textView =
                new TextView(requireContext());

        textView.setText(
                title + " = " + count
        );

        textView.setTextSize(17);

        textView.setTextColor(
                Color.DKGRAY
        );

        textView.setPadding(
                0,
                12,
                0,
                12
        );

        parent.addView(textView);
    }
    private void showLogoutConfirmation() {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from MySCHOOL?")
                .setIcon(android.R.drawable.ic_dialog_alert)

                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })

                .setPositiveButton("Logout", (dialog, which) -> {

                    // Firebase logout
                    FirebaseAuth.getInstance().signOut();

                    // Clear saved login preferences
                    SharedPreferences prefs =
                            requireActivity().getSharedPreferences(
                                    "MySchoolPrefs",
                                    Context.MODE_PRIVATE
                            );

                    prefs.edit().clear().apply();

                    // Open LoginActivity
                    Intent intent = new Intent(
                            requireActivity(),
                            LoginActivity.class
                    );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    requireActivity().finish();
                })

                .show();
    }
}
