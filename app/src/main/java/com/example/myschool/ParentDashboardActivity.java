package com.example.myschool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ParentDashboardActivity extends AppCompatActivity {

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    // =========================================================
    // DRAWER
    // =========================================================

    private View drawer;
    private View drawerScrim;


    // =========================================================
    // TOP HEADER
    // =========================================================

    private TextView tvTopSchoolName;


    // =========================================================
    // CHILD DATA
    // =========================================================

    private TextView tvChildAvatar;
    private TextView tvChildName;
    private TextView tvChildClass;


    // =========================================================
    // DRAWER DATA
    // =========================================================

    private TextView tvDrawerSchool;
    private TextView tvDrawerAvatar;
    private TextView tvDrawerName;
    private TextView tvDrawerRole;


    // =========================================================
    // SWITCH STUDENT
    // =========================================================

    private TextView btnSwitchStudent;
    private TextView btnDrawerSwitchStudent;

    private final List<DocumentSnapshot> childList =
            new ArrayList<>();


    // =========================================================
    // EVENTS / NOTICES
    // =========================================================

    private LinearLayout eventsContainer;
    private LinearLayout noticesContainer;


    // =========================================================
    // CURRENT STUDENT
    // =========================================================

    private String currentStudentId = "";
    private String currentSchoolId = "";


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_parent_dashboard
        );


        // =====================================================
        // FIREBASE
        // =====================================================

        mAuth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();


        // =====================================================
        // GET STUDENT ID
        // =====================================================

        currentStudentId =
                getIntent().getStringExtra(
                        "studentId"
                );


        if (currentStudentId == null ||
                currentStudentId.trim().isEmpty()) {

            currentStudentId =
                    getSharedPreferences(
                            "MySchoolPrefs",
                            MODE_PRIVATE
                    )
                            .getString(
                                    "studentId",
                                    ""
                            );
        }


        // =====================================================
        // INITIALIZE
        // =====================================================

        initViews();


        // =====================================================
        // SETUP
        // =====================================================

        setupDrawer();

        setupLogout();

        setupStaticClicks();


        // =====================================================
        // LOAD DATA
        // =====================================================

        loadCurrentStudent();
    }


    // =========================================================
    // INIT VIEWS
    // =========================================================

    private void initViews() {

        drawer =
                findViewById(
                        R.id.parentDrawer
                );


        drawerScrim =
                findViewById(
                        R.id.drawerScrim
                );


        // =====================================================
        // TOP SCHOOL NAME
        // =====================================================

        tvTopSchoolName =
                findViewById(
                        R.id.tvTopSchoolName
                );


        // =====================================================
        // CHILD
        // =====================================================

        tvChildAvatar =
                findViewById(
                        R.id.tvChildAvatar
                );


        tvChildName =
                findViewById(
                        R.id.tvChildName
                );


        tvChildClass =
                findViewById(
                        R.id.tvChildClass
                );


        // =====================================================
        // DRAWER
        // =====================================================

        tvDrawerSchool =
                findViewById(
                        R.id.tvDrawerSchool
                );


        tvDrawerAvatar =
                findViewById(
                        R.id.tvDrawerAvatar
                );


        tvDrawerName =
                findViewById(
                        R.id.tvDrawerName
                );


        tvDrawerRole =
                findViewById(
                        R.id.tvDrawerRole
                );


        // =====================================================
        // SWITCH
        // =====================================================

        btnSwitchStudent =
                findViewById(
                        R.id.btnSwitchStudent
                );


        btnDrawerSwitchStudent =
                findViewById(
                        R.id.btnDrawerSwitchStudent
                );


        // =====================================================
        // EVENTS / NOTICES
        // =====================================================

        eventsContainer =
                findViewById(
                        R.id.eventsContainer
                );


        noticesContainer =
                findViewById(
                        R.id.noticesContainer
                );
    }


    // =========================================================
    // DRAWER SETUP
    // =========================================================

    private void setupDrawer() {

        View btnMenu =
                findViewById(
                        R.id.btnMenu
                );


        View btnNotifications =
                findViewById(
                        R.id.btnNotifications
                );


        TextView btnClose =
                findViewById(
                        R.id.btnDrawerClose
                );


        // =====================================================
        // MENU
        // =====================================================

        if (btnMenu != null) {

            btnMenu.setOnClickListener(
                    v -> openDrawer()
            );
        }


        // =====================================================
        // NOTIFICATIONS
        // =====================================================

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showParentNotifications());
        }


        // =====================================================
        // CLOSE DRAWER
        // =====================================================

        if (btnClose != null) {

            btnClose.setOnClickListener(
                    v -> closeDrawer()
            );
        }


        if (drawerScrim != null) {

            drawerScrim.setOnClickListener(
                    v -> closeDrawer()
            );
        }


        // =====================================================
        // SWITCH STUDENT
        // =====================================================

        if (btnSwitchStudent != null) {

            btnSwitchStudent.setOnClickListener(
                    v -> showStudentSwitcher()
            );
        }


        if (btnDrawerSwitchStudent != null) {

            btnDrawerSwitchStudent.setOnClickListener(
                    v -> showStudentSwitcher()
            );
        }


        // =====================================================
        // HIDE DRAWER INITIALLY
        // =====================================================

        if (drawer != null) {

            drawer.post(
                    () -> {

                        if (drawer.getWidth() > 0) {

                            drawer.setTranslationX(
                                    -drawer.getWidth()
                            );
                        }
                    }
            );
        }
    }


    // =========================================================
    // OPEN DRAWER
    // =========================================================

    private void openDrawer() {

        if (drawerScrim != null) {

            drawerScrim.setVisibility(
                    View.VISIBLE
            );
        }


        if (drawer != null) {

            drawer.animate()
                    .translationX(0)
                    .setDuration(220)
                    .start();
        }
    }


    // =========================================================
    // CLOSE DRAWER
    // =========================================================

    private void closeDrawer() {

        if (drawer == null) {
            return;
        }


        drawer.animate()
                .translationX(
                        -drawer.getWidth()
                )
                .setDuration(220)
                .withEndAction(
                        () -> {

                            if (drawerScrim != null) {

                                drawerScrim.setVisibility(
                                        View.GONE
                                );
                            }
                        }
                )
                .start();
    }


    // =========================================================
    // LOGOUT
    // =========================================================
    private void showParentNotifications() {
        if (currentStudentId == null || currentStudentId.trim().isEmpty()) {
            Toast.makeText(this, "Student information not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("students").document(currentStudentId).get().addOnSuccessListener(student -> {
            if (!student.exists()) return;
            String cls = value(student, "className");
            String div = value(student, "division");
            String school = value(student, "schoolId");
            if (school.isEmpty()) school = currentSchoolId;
            if (school.isEmpty()) {
                Toast.makeText(this, "School information not available.", Toast.LENGTH_SHORT).show();
                return;
            }
            final String schoolIdQuery = school;
            final java.util.ArrayList<String> items = new java.util.ArrayList<>();
            db.collection("homework").whereEqualTo("schoolId", schoolIdQuery).get().addOnSuccessListener(hw -> {
                for (DocumentSnapshot d : hw.getDocuments()) {
                    if (!value(d, "className").equalsIgnoreCase(cls)) continue;
                    String dDiv = value(d, "division");
                    if (!dDiv.isEmpty() && !dDiv.equalsIgnoreCase(div)) continue;
                    String title = value(d, "title");
                    String subject = value(d, "subject");
                    items.add("📘 Homework: " + (title.isEmpty() ? "New homework" : title)
                            + (subject.isEmpty() ? "" : " (" + subject + ")"));
                }
                db.collection("conductTests").whereEqualTo("schoolId", schoolIdQuery).get().addOnSuccessListener(tests -> {
                    for (DocumentSnapshot d : tests.getDocuments()) {
                        if (!value(d, "className").equalsIgnoreCase(cls)) continue;
                        String dDiv = value(d, "division");
                        if (!dDiv.isEmpty() && !dDiv.equalsIgnoreCase(div)) continue;
                        String topic = value(d, "topic");
                        String date = value(d, "testDate");
                        items.add("📝 Exam: " + (topic.isEmpty() ? "Exam scheduled" : topic)
                                + (date.isEmpty() ? "" : " — " + date));
                    }
                    new AlertDialog.Builder(ParentDashboardActivity.this)
                            .setTitle("Homework & Exam Notifications")
                            .setMessage(items.isEmpty() ? "No homework or exam notifications available."
                                    : android.text.TextUtils.join("\\n\\n", items))
                            .setPositiveButton("OK", null)
                            .show();
                }).addOnFailureListener(e -> Toast.makeText(this, "Unable to load exam notifications.", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(this, "Unable to load homework notifications.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Unable to load student details.", Toast.LENGTH_SHORT).show());
    }

    private void setupLogout() {

        TextView btnLogout = findViewById(R.id.btnLogout);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                closeDrawer();

                new AlertDialog.Builder(ParentDashboardActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setPositiveButton("Logout", (dialog, which) -> {
                            logoutUser();
                        })
                        .setCancelable(true)
                        .show();
            });
        }
    }


    // =============================================
// SETTINGS DIALOG
// =============================================
    private void showSettingsDialog() {

        String studentName = "Not available";
        String schoolName = "MySCHOOL";

        if (tvChildName != null
                && tvChildName.getText() != null
                && !tvChildName.getText().toString().trim().isEmpty()) {
            studentName = tvChildName.getText().toString().trim();
        }

        if (tvTopSchoolName != null
                && tvTopSchoolName.getText() != null
                && !tvTopSchoolName.getText().toString().trim().isEmpty()) {
            schoolName = tvTopSchoolName.getText().toString().trim();
        }

        String message =
                "Account Settings\n\n"
                        + "App: MySCHOOL\n"
                        + "School: " + schoolName + "\n"
                        + "Selected Student: " + studentName
                        + "\n\nYou can switch the selected student from here.";

        new AlertDialog.Builder(this)
                .setTitle("⚙ Settings")
                .setMessage(message)
                .setNeutralButton("Switch Student", (dialog, which) -> {
                    showStudentSwitcher();
                })
                .setPositiveButton("Close", null)
                .show();
    }


    // =============================================
// HELP & SUPPORT DIALOG
// =============================================
    private void showHelpDialog() {

        new AlertDialog.Builder(this)
                .setTitle("❓ Help & Support")
                .setMessage(
                        "Welcome to MySCHOOL Support.\n\n"
                                + "For help with any of the following:\n\n"
                                + "• Attendance or homework\n"
                                + "• Exam marks or fees\n"
                                + "• School notices and events\n"
                                + "• Student account problems\n\n"
                                + "Please contact your school administration "
                                + "or class teacher through School Enquiry."
                )
                .setNegativeButton("Close", null)
                .setPositiveButton("School Enquiry", (dialog, which) -> {
                    openSchoolEnquiry();
                })
                .show();
    }


    // =============================================
// ABOUT US DIALOG
// =============================================
    private void showAboutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("ℹ About MySCHOOL")
                .setMessage(
                        "MySCHOOL\n\n"
                                + "A school management application designed "
                                + "to help parents stay connected with their school.\n\n"
                                + "Features include:\n"
                                + "• Student information\n"
                                + "• Attendance and homework\n"
                                + "• Exam marks and timetable\n"
                                + "• School notices and events\n"
                                + "• School communication\n\n"
                                + "Stay informed. Stay connected."
                )
                .setPositiveButton("OK", null)
                .show();
    }


    private void logoutUser() {

        // =====================================================
        // FIREBASE LOGOUT
        // =====================================================

        if (mAuth != null) {

            mAuth.signOut();
        }


        // =====================================================
        // CLEAR LOCAL LOGIN
        // =====================================================

        SharedPreferences preferences =
                getSharedPreferences(
                        "MySchoolPrefs",
                        MODE_PRIVATE
                );


        preferences.edit()
                .clear()
                .apply();


        // =====================================================
        // LOGIN PAGE
        // =====================================================

        Intent intent =
                new Intent(
                        ParentDashboardActivity.this,
                        LoginActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }


    // =========================================================
    // LOAD CURRENT STUDENT
    // =========================================================

    private void loadCurrentStudent() {

        if (currentStudentId == null ||
                currentStudentId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Student information not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        db.collection("students")
                .document(currentStudentId)
                .addSnapshotListener(
                        (student, error) -> {

                            if (error != null) {

                                Toast.makeText(
                                        ParentDashboardActivity.this,
                                        "Unable to load student data.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            if (student == null ||
                                    !student.exists()) {

                                Toast.makeText(
                                        ParentDashboardActivity.this,
                                        "Student record not found.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }


                            applyStudentData(
                                    student
                            );
                        }
                );
    }


    // =========================================================
    // APPLY STUDENT DATA
    // =========================================================

    private void applyStudentData(
            DocumentSnapshot student) {

        String firstName =
                value(
                        student,
                        "firstName"
                );


        String middleName =
                value(
                        student,
                        "middleName"
                );


        String lastName =
                value(
                        student,
                        "lastName"
                );


        String fullName =
                (
                        firstName + " " +
                                middleName + " " +
                                lastName
                ).trim();


        if (fullName.isEmpty()) {

            fullName =
                    "Student";
        }


        String initial =
                getInitial(
                        firstName,
                        fullName
                );


        // =====================================================
        // AVATAR
        // =====================================================

        if (tvChildAvatar != null) {

            tvChildAvatar.setText(
                    initial
            );
        }


        if (tvDrawerAvatar != null) {

            tvDrawerAvatar.setText(
                    initial
            );
        }


        // =====================================================
        // NAME
        // =====================================================

        if (tvChildName != null) {

            tvChildName.setText(
                    fullName
            );
        }


        if (tvDrawerName != null) {

            tvDrawerName.setText(
                    fullName
            );
        }


        // =====================================================
        // SCHOOL
        // =====================================================

        String schoolId =
                value(
                        student,
                        "schoolId"
                );


        currentSchoolId =
                schoolId;


        // =====================================================
        // CLASS + ROLL ONLY
        // =====================================================

        String className =
                value(
                        student,
                        "className"
                );


        String division =
                value(
                        student,
                        "division"
                );


        String rollNumber =
                value(
                        student,
                        "rollNumber"
                );


        String classText =
                className;


        if (!division.isEmpty()) {

            if (!classText.isEmpty()) {

                classText +=
                        " - ";
            }


            classText +=
                    division;
        }


        if (!rollNumber.isEmpty()) {

            if (!classText.isEmpty()) {

                classText +=
                        " • ";
            }


            classText +=
                    "Roll No. " +
                            rollNumber;
        }


        if (classText.isEmpty()) {

            classText =
                    "Student";
        }


        if (tvChildClass != null) {

            tvChildClass.setText(
                    classText
            );
        }


        // =====================================================
        // SCHOOL DATA
        // =====================================================

        if (!schoolId.isEmpty()) {

            listenSchool(
                    schoolId
            );


            listenSiblingStudents(
                    student
            );


            listenEvents(
                    schoolId
            );


            listenNotices(
                    schoolId
            );

        } else {

            if (tvTopSchoolName != null) {

                tvTopSchoolName.setText(
                        "My School"
                );
            }


            if (tvDrawerSchool != null) {

                tvDrawerSchool.setText(
                        "My School"
                );
            }
        }
    }


    // =========================================================
    // SCHOOL REALTIME
    // =========================================================

    private void listenSchool(
            String schoolId) {

        db.collection("schools")
                .document(schoolId)
                .addSnapshotListener(
                        (school, error) -> {

                            String schoolName =
                                    "My School";


                            if (error == null &&
                                    school != null &&
                                    school.exists()) {

                                String schoolValue =
                                        value(
                                                school,
                                                "schoolName"
                                        );


                                if (!schoolValue.isEmpty()) {

                                    schoolName =
                                            schoolValue;
                                }
                            }


                            // =================================================
                            // TOP HEADER
                            // =================================================

                            if (tvTopSchoolName != null) {

                                tvTopSchoolName.setText(
                                        schoolName
                                );
                            }


                            // =================================================
                            // DRAWER SCHOOL
                            // =================================================

                            if (tvDrawerSchool != null) {

                                tvDrawerSchool.setText(
                                        schoolName
                                );
                            }
                        }
                );
    }


    // =========================================================
    // SAME SCHOOL CHILDREN
    // =========================================================

    private void listenSiblingStudents(
            DocumentSnapshot currentStudent) {

        String phone =
                value(
                        currentStudent,
                        "studentPhone"
                );


        if (phone.isEmpty() ||
                currentSchoolId.isEmpty()) {

            hideSwitchStudent();

            return;
        }


        db.collection("students")
                .whereEqualTo(
                        "schoolId",
                        currentSchoolId
                )
                .whereEqualTo(
                        "studentPhone",
                        phone
                )
                .whereEqualTo(
                        "active",
                        true
                )
                .addSnapshotListener(
                        (snapshot, error) -> {

                            if (error != null ||
                                    snapshot == null) {

                                hideSwitchStudent();

                                return;
                            }


                            childList.clear();


                            for (
                                    DocumentSnapshot document :
                                    snapshot.getDocuments()
                            ) {

                                childList.add(
                                        document
                                );
                            }


                            if (childList.size() > 1) {

                                showSwitchStudent();

                            } else {

                                hideSwitchStudent();
                            }
                        }
                );
    }


    // =========================================================
    // SHOW SWITCH
    // =========================================================

    private void showSwitchStudent() {

        if (btnSwitchStudent != null) {

            btnSwitchStudent.setVisibility(
                    View.VISIBLE
            );
        }


        if (btnDrawerSwitchStudent != null) {

            btnDrawerSwitchStudent.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // =========================================================
    // HIDE SWITCH
    // =========================================================

    private void hideSwitchStudent() {

        if (btnSwitchStudent != null) {

            btnSwitchStudent.setVisibility(
                    View.GONE
            );
        }


        if (btnDrawerSwitchStudent != null) {

            btnDrawerSwitchStudent.setVisibility(
                    View.GONE
            );
        }
    }


    // =========================================================
    // SWITCH STUDENT
    // =========================================================

    private void showStudentSwitcher() {

        if (childList.size() <= 1) {

            return;
        }


        String[] names =
                new String[
                        childList.size()
                        ];


        for (
                int i = 0;
                i < childList.size();
                i++
        ) {

            names[i] =
                    buildFullName(
                            childList.get(i)
                    );
        }


        new androidx.appcompat.app.AlertDialog.Builder(
                this
        )
                .setTitle(
                        "Switch Student"
                )
                .setItems(
                        names,
                        (dialog, which) -> {

                            DocumentSnapshot selected =
                                    childList.get(
                                            which
                                    );


                            currentStudentId =
                                    selected.getId();


                            getSharedPreferences(
                                    "MySchoolPrefs",
                                    MODE_PRIVATE
                            )
                                    .edit()
                                    .putString(
                                            "studentId",
                                            currentStudentId
                                    )
                                    .apply();


                            closeDrawer();


                            applyStudentData(
                                    selected
                            );
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }


    // =========================================================
    // UPCOMING EVENTS - SPECIAL OCCASIONS + MEETINGS
    // =========================================================

    private void listenEvents(String schoolId) {

        if (eventsContainer == null || schoolId == null || schoolId.trim().isEmpty()) {
            return;
        }

        eventsContainer.removeAllViews();

        db.collection("specialOccasions")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(occasionSnapshot -> {

                    List<DocumentSnapshot> occasions =
                            new ArrayList<>(occasionSnapshot.getDocuments());

                    db.collection("meetings")
                            .whereEqualTo("schoolId", schoolId)
                            .get()
                            .addOnSuccessListener(meetingSnapshot -> {

                                List<DocumentSnapshot> meetings =
                                        new ArrayList<>(meetingSnapshot.getDocuments());

                                List<EventItem> items = new ArrayList<>();

                                for (DocumentSnapshot doc : occasions) {
                                    addUpcomingEventItem(items, doc, "Special Occasion");
                                }

                                for (DocumentSnapshot doc : meetings) {
                                    addUpcomingEventItem(items, doc, "Meeting");
                                }

                                Collections.sort(items, Comparator.comparing(
                                        item -> item.date == null ? new Date(Long.MAX_VALUE) : item.date
                                ));

                                eventsContainer.removeAllViews();
                                TextView viewAll = findViewById(R.id.tvEventsViewAll);
                                if (viewAll != null) viewAll.setVisibility(items.size() > 0 ? View.VISIBLE : View.GONE);

                                int count = Math.min(items.size(), 3);

                                if (count == 0) {
                                    addEmptyEvent();
                                    return;
                                }

                                for (int i = 0; i < count; i++) {
                                    addEventCard(items.get(i));
                                }
                            })
                            .addOnFailureListener(e -> {
                                eventsContainer.removeAllViews();
                                addEmptyEvent();
                            });
                })
                .addOnFailureListener(e -> {
                    eventsContainer.removeAllViews();
                    addEmptyEvent();
                });
    }

    private void addUpcomingEventItem(
            List<EventItem> list,
            DocumentSnapshot doc,
            String type) {

        String dateText = value(doc, "date");
        Date date = parseDashboardDate(dateText);

        if (date == null) {
            return;
        }

        Date today = parseDashboardDate(
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date())
        );
        if (today != null && date.before(today)) {
            return;
        }

        String title = value(doc, "title");
        if (title.isEmpty()) {
            title = type;
        }

        String description = value(doc, "description");
        String time = value(doc, "time");
        String venue = value(doc, "venue");

        list.add(new EventItem(type, title, dateText, time, venue, description, date));
    }

    private Date parseDashboardDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] patterns = {"dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd"};

        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.getDefault())
                        .parse(value.trim());
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private static class EventItem {
        String type;
        String title;
        String dateText;
        String time;
        String venue;
        String description;
        Date date;

        EventItem(String type, String title, String dateText,
                  String time, String venue, String description, Date date) {
            this.type = type;
            this.title = title;
            this.dateText = dateText;
            this.time = time;
            this.venue = venue;
            this.description = description;
            this.date = date;
        }
    }

    // =========================================================
    // EVENT CARD
    // =========================================================

    private void addEventCard(EventItem event) {

        if (eventsContainer == null) {
            return;
        }

        TextView text = new TextView(this);

        StringBuilder content = new StringBuilder();
        content.append(event.type)
                .append("\n")
                .append(event.title);

        if (!event.dateText.isEmpty()) {
            content.append("\n").append(event.dateText);
        }

        if (!event.time.isEmpty()) {
            content.append(" • ").append(event.time);
        }

        if (!event.venue.isEmpty()) {
            content.append("\nVenue: ").append(event.venue);
        }

        if (!event.description.isEmpty()) {
            content.append("\n").append(event.description);
        }

        text.setText(content.toString());
        text.setTextSize(12);
        text.setTextColor(Color.rgb(65, 65, 65));
        text.setPadding(dp(14), dp(13), dp(14), dp(13));
        text.setBackground(roundedBackground(Color.WHITE, 16, Color.rgb(238, 231, 218)));
        text.setElevation(dp(2));
        text.setMaxLines(6);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, dp(9));
        text.setLayoutParams(params);
        eventsContainer.addView(text);
    }

    // =========================================================
    // EMPTY EVENT
    // =========================================================

    private void addEmptyEvent() {

        if (eventsContainer == null) {
            return;
        }


        TextView empty =
                new TextView(this);


        empty.setText(
                "No upcoming events"
        );


        empty.setGravity(
                Gravity.CENTER
        );


        empty.setTextSize(
                11
        );


        empty.setTextColor(
                Color.rgb(
                        150,
                        150,
                        150
                )
        );


        empty.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        65
                )
        );


        eventsContainer.addView(
                empty
        );
    }


    // =========================================================
    // NOTICES
    // =========================================================

    private void listenNotices(String schoolId) {
        if (schoolId == null || schoolId.trim().isEmpty() || noticesContainer == null) return;

        db.collection("notices").whereEqualTo("schoolId", schoolId)
                .addSnapshotListener((snapshot, error) -> {
                    if (noticesContainer == null) return;
                    noticesContainer.removeAllViews();
                    if (error != null || snapshot == null || snapshot.isEmpty()) {
                        addEmptyNotice();
                        return;
                    }
                    List<DocumentSnapshot> notices = new ArrayList<>(snapshot.getDocuments());
                    Collections.sort(notices, (a, b) -> Long.compare(noticeTime(b), noticeTime(a)));
                    int count = Math.min(notices.size(), 4);
                    for (int i = 0; i < count; i++) addNoticeCard(notices.get(i));
                    TextView viewAll = findViewById(R.id.tvNoticeViewAll);
                    if (viewAll != null) viewAll.setVisibility(notices.size() > 0 ? View.VISIBLE : View.GONE);
                    if (count == 0) addEmptyNotice();
                });
    }

    private long noticeTime(DocumentSnapshot notice) {
        Object createdAt = notice.get("createdAt");
        if (createdAt instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) createdAt).toDate().getTime();
        }
        Date parsed = parseDashboardDate(value(notice, "date"));
        if (parsed != null) return parsed.getTime();
        return 0L;
    }

    // =========================================================
    // NOTICE CARD
    // =========================================================

    private void addNoticeCard(
            DocumentSnapshot notice) {

        if (noticesContainer == null) {
            return;
        }


        TextView text =
                new TextView(this);


        String title =
                value(
                        notice,
                        "title"
                );


        String description =
                value(
                        notice,
                        "description"
                );


        String date =
                value(
                        notice,
                        "date"
                );


        if (title.isEmpty()) {

            title =
                    "School Notice";
        }


        String content =
                title;


        if (!description.isEmpty()) {

            content +=
                    "\n" +
                            description;
        }


        if (!date.isEmpty()) {

            content +=
                    "\n" +
                            date;
        }


        text.setText(
                content
        );


        text.setTextSize(12);
        text.setTextColor(Color.rgb(65, 65, 65));
        text.setPadding(dp(14), dp(13), dp(14), dp(13));
        text.setBackground(roundedBackground(Color.WHITE, 16, Color.rgb(238, 231, 218)));
        text.setElevation(dp(2));
        text.setMaxLines(5);


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        params.setMargins(0, 0, 0, dp(9));


        text.setLayoutParams(
                params
        );


        noticesContainer.addView(
                text
        );
    }


    // =========================================================
    // EMPTY NOTICE
    // =========================================================

    private void addEmptyNotice() {

        if (noticesContainer == null) {
            return;
        }


        TextView empty =
                new TextView(this);


        empty.setText(
                "No recent notices"
        );


        empty.setGravity(
                Gravity.CENTER
        );


        empty.setTextSize(
                11
        );


        empty.setTextColor(
                Color.rgb(
                        150,
                        150,
                        150
                )
        );


        empty.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        65
                )
        );


        noticesContainer.addView(
                empty
        );
    }


    // =========================================================
    // DASHBOARD / DRAWER NAVIGATION
    // =========================================================

    private void setupStaticClicks() {

        View navHome = findViewById(R.id.navHome);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                closeDrawer();

                // Parent Dashboard main screen वर परत जा
                // Dashboard याच Activity मध्ये असेल तर drawer बंद होईल.
            });
        }

        View navMyChild = findViewById(R.id.navMyChild);

        if (navMyChild != null) {
            navMyChild.setOnClickListener(v -> {
                closeDrawer();
                loadMyChildDetails();
            });
        }

        View cardGallery = findViewById(R.id.cardGallery);
        if (cardGallery != null) {
            cardGallery.setOnClickListener(v -> openSchoolFacilities());
        }

        View cardBusTracking = findViewById(R.id.cardBusTracking);
        if (cardBusTracking != null) {
            cardBusTracking.setOnClickListener(v -> openTeacherMessaging());
        }

        View navGallery = findViewById(R.id.navGallery);
        if (navGallery != null) {
            navGallery.setOnClickListener(v -> {
                closeDrawer();
                openSchoolFacilities();
            });
        }

        View navBusTracking = findViewById(R.id.navBusTracking);
        if (navBusTracking != null) {
            navBusTracking.setOnClickListener(v -> {
                closeDrawer();
                openTeacherMessaging();
            });
        }

        View navMessages = findViewById(R.id.navMessages);
        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                closeDrawer();
                openSchoolEnquiry();
            });
        }

        View cardEvents = findViewById(R.id.cardEvents);
        if (cardEvents != null) cardEvents.setOnClickListener(v -> openParentEvents());

        View cardNotices = findViewById(R.id.cardNotices);
        if (cardNotices != null) cardNotices.setOnClickListener(v -> openParentNotices());

        View navEvents = findViewById(R.id.navEvents);
        if (navEvents != null) navEvents.setOnClickListener(v -> { closeDrawer(); openParentEvents(); });

        View navNotices = findViewById(R.id.navNotices);
        if (navNotices != null) navNotices.setOnClickListener(v -> { closeDrawer(); openParentNotices(); });

        View tvEventsViewAll = findViewById(R.id.tvEventsViewAll);
        if (tvEventsViewAll != null) tvEventsViewAll.setOnClickListener(v -> openParentEvents());

        View tvNoticeViewAll = findViewById(R.id.tvNoticeViewAll);
        if (tvNoticeViewAll != null) tvNoticeViewAll.setOnClickListener(v -> openParentNotices());

// PARENT STUDENT FEATURES
        View cardAttendance = findViewById(R.id.cardAttendance);
        if (cardAttendance != null) cardAttendance.setOnClickListener(v -> openParentFeature("attendance"));

        View navAttendance = findViewById(R.id.navAttendance);
        if (navAttendance != null) navAttendance.setOnClickListener(v -> { closeDrawer(); openParentFeature("attendance"); });

        View cardHomework = findViewById(R.id.cardHomework);
        if (cardHomework != null) cardHomework.setOnClickListener(v -> openParentFeature("homework"));

        View navHomework = findViewById(R.id.navHomework);
        if (navHomework != null) navHomework.setOnClickListener(v -> { closeDrawer(); openParentFeature("homework"); });

        View cardExamMarks = findViewById(R.id.cardExamMarks);
        if (cardExamMarks != null) cardExamMarks.setOnClickListener(v -> openParentFeature("exam"));

        View navExamMarks = findViewById(R.id.navExamMarks);
        if (navExamMarks != null) navExamMarks.setOnClickListener(v -> { closeDrawer(); openParentFeature("exam"); });

        View cardFees = findViewById(R.id.cardFees);
        if (cardFees != null) cardFees.setOnClickListener(v -> openParentFeature("fees"));

        View navFees = findViewById(R.id.navFees);
        if (navFees != null) navFees.setOnClickListener(v -> { closeDrawer(); openParentFeature("fees"); });

        View cardTimeTable = findViewById(R.id.cardTimeTable);
        if (cardTimeTable != null) cardTimeTable.setOnClickListener(v -> openParentFeature("timetable"));

        View navTimeTable = findViewById(R.id.navTimeTable);
        if (navTimeTable != null) navTimeTable.setOnClickListener(v -> { closeDrawer(); openParentFeature("timetable"); });

        // SETTINGS
        View navSettings = findViewById(R.id.navSettings);

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                closeDrawer();
                showSettingsDialog();
            });
        }

// HELP & SUPPORT
        View navHelp = findViewById(R.id.navHelp);

        if (navHelp != null) {
            navHelp.setOnClickListener(v -> {
                closeDrawer();
                showHelpDialog();
            });
        }

// ABOUT US
        View navAbout = findViewById(R.id.navAbout);

        if (navAbout != null) {
            navAbout.setOnClickListener(v -> {
                closeDrawer();
                showAboutDialog();
            });
        }

    }

    private void openParentFeature(String feature) {
        if (currentStudentId == null || currentStudentId.trim().isEmpty()) {
            Toast.makeText(this, "Student information not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("attendance".equals(feature)) {
            Intent intent = new Intent(this, StudentAttendanceActivity.class);
            intent.putExtra("studentId", currentStudentId);
            intent.putExtra("schoolId", currentSchoolId);
            startActivity(intent);
            return;
        }
        Intent intent = new Intent(this, ParentStudentFeatureActivity.class);
        intent.putExtra("feature", feature);
        intent.putExtra("studentId", currentStudentId);
        intent.putExtra("schoolId", currentSchoolId);
        startActivity(intent);
    }

    private void openParentEvents() {
        if (currentSchoolId == null || currentSchoolId.trim().isEmpty()) {
            Toast.makeText(this, "School information not available yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ParentEventsActivity.class);
        intent.putExtra("schoolId", currentSchoolId);
        intent.putExtra("studentId", currentStudentId);
        startActivity(intent);
    }

    private void openParentNotices() {
        if (currentSchoolId == null || currentSchoolId.trim().isEmpty()) {
            Toast.makeText(this, "School information not available yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ParentNoticesActivity.class);
        intent.putExtra("schoolId", currentSchoolId);
        intent.putExtra("studentId", currentStudentId);
        startActivity(intent);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBackground(int fillColor, int radiusDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private void openSchoolFacilities() {
        Intent intent = new Intent(
                ParentDashboardActivity.this,
                ParentSchoolFacilitiesActivity.class
        );
        intent.putExtra("studentId", currentStudentId);
        startActivity(intent);
    }

    private void openTeacherMessaging() {
        Intent intent = new Intent(
                ParentDashboardActivity.this,
                ParentTeacherMessagingActivity.class
        );
        intent.putExtra("studentId", currentStudentId);
        startActivity(intent);
    }

    private void openSchoolEnquiry() {
        Intent intent = new Intent(
                ParentDashboardActivity.this,
                ParentSchoolEnquiryActivity.class
        );
        intent.putExtra("studentId", currentStudentId);
        startActivity(intent);
    }

    // =========================================================
    // FULL NAME
    // =========================================================

    private String buildFullName(
            DocumentSnapshot student) {

        String first =
                value(
                        student,
                        "firstName"
                );


        String middle =
                value(
                        student,
                        "middleName"
                );


        String last =
                value(
                        student,
                        "lastName"
                );


        String name =
                (
                        first + " " +
                                middle + " " +
                                last
                ).trim();


        if (name.isEmpty()) {

            return "Student";
        }


        return name;
    }


    // =========================================================
    // INITIAL
    // =========================================================

    private String getInitial(
            String firstName,
            String fullName) {

        String source =
                firstName;


        if (source == null ||
                source.trim().isEmpty()) {

            source =
                    fullName;
        }


        if (source == null ||
                source.trim().isEmpty()) {

            return "S";
        }


        return source
                .trim()
                .substring(
                        0,
                        1
                )
                .toUpperCase();
    }


    // =========================================================
    // VALUE
    // =========================================================

    private String value(
            DocumentSnapshot document,
            String key) {

        Object object =
                document.get(key);


        if (object == null) {

            return "";
        }


        return String.valueOf(
                object
        ).trim();
    }
    // =============================================
// MY CHILD - FETCH ADMIN STUDENT DETAILS
// =============================================
    private void loadMyChildDetails() {

        if (currentStudentId == null
                || currentStudentId.trim().isEmpty()) {

            new AlertDialog.Builder(this)
                    .setTitle("My Child")
                    .setMessage("Student information not found.")
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        db.collection("students")
                .document(currentStudentId)
                .get()
                .addOnSuccessListener(student -> {

                    if (!student.exists()) {
                        new AlertDialog.Builder(this)
                                .setTitle("My Child")
                                .setMessage("Student record not found.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    // Student name
                    String firstName = value(student, "firstName");
                    String middleName = value(student, "middleName");
                    String lastName = value(student, "lastName");

                    String fullName = (firstName + " "
                            + middleName + " "
                            + lastName).trim();

                    if (fullName.isEmpty()) {
                        fullName = "Not available";
                    }

                    // Student information
                    String className = value(student, "className");
                    String division = value(student, "division");
                    String rollNumber = value(student, "rollNumber");
                    String phone = value(student, "studentPhone");
                    String schoolId = value(student, "schoolId");

                    StringBuilder details = new StringBuilder();

                    details.append("Student Name: ")
                            .append(fullName)
                            .append("\n\n");

                    details.append("Class: ")
                            .append(className.isEmpty()
                                    ? "Not available" : className)
                            .append("\n\n");

                    details.append("Division: ")
                            .append(division.isEmpty()
                                    ? "Not available" : division)
                            .append("\n\n");

                    details.append("Roll Number: ")
                            .append(rollNumber.isEmpty()
                                    ? "Not available" : rollNumber)
                            .append("\n\n");

                    details.append("Student Phone: ")
                            .append(phone.isEmpty()
                                    ? "Not available" : phone);

                    new AlertDialog.Builder(this)
                            .setTitle("My Child")
                            .setMessage(details.toString())
                            .setPositiveButton("Close", null)
                            .show();

                })
                .addOnFailureListener(error -> {

                    new AlertDialog.Builder(this)
                            .setTitle("My Child")
                            .setMessage(
                                    "Unable to load student details.\n"
                                            + error.getLocalizedMessage()
                            )
                            .setPositiveButton("OK", null)
                            .show();
                });
    }


    // =========================================================
    // BACK
    // =========================================================

    @Override
    public void onBackPressed() {

        if (drawerScrim != null &&
                drawerScrim.getVisibility()
                        == View.VISIBLE) {

            closeDrawer();

            return;
        }


        super.onBackPressed();
    }
}
