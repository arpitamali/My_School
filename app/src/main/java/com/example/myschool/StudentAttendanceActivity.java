package com.example.myschool;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentAttendanceActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String studentId = "";
    private String schoolId = "";

    private TextView tvStudent;
    private TextView tvSummary;
    private LinearLayout attendanceList;

    private final List<AttendanceItem> items = new ArrayList<>();

    private ListenerRegistration attendanceListener;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_student_attendance);

        db = FirebaseFirestore.getInstance();


        tvStudent =
                findViewById(
                        R.id.tvStudentAttendanceStudent
                );

        tvSummary =
                findViewById(
                        R.id.tvStudentAttendanceSummary
                );

        attendanceList =
                findViewById(
                        R.id.studentAttendanceHistoryContainer
                );


        findViewById(
                R.id.btnStudentAttendanceHistoryBack
        ).setOnClickListener(
                v -> finish()
        );


        findViewById(
                R.id.btnRefreshStudentAttendance
        ).setOnClickListener(
                v -> load()
        );


        studentId =
                safe(
                        getIntent().getStringExtra(
                                "studentId"
                        )
                );


        schoolId =
                safe(
                        getIntent().getStringExtra(
                                "schoolId"
                        )
                );


        load();
    }


    // =====================================================
    // LOAD STUDENT
    // =====================================================

    private void load() {

        if (studentId.isEmpty()) {

            toast(
                    "Student ID missing."
            );

            return;
        }


        /*
         * If schoolId was passed from previous screen,
         * use it directly.
         */
        if (!schoolId.isEmpty()) {

            db.collection("students")
                    .document(studentId)
                    .get()
                    .addOnSuccessListener(
                            student -> {

                                if (student.exists()) {

                                    tvStudent.setText(
                                            buildStudentTitle(
                                                    student
                                            )
                                    );

                                } else {

                                    tvStudent.setText(
                                            "Student"
                                    );
                                }

                                loadAttendance();
                            }
                    )
                    .addOnFailureListener(
                            e -> {

                                tvStudent.setText(
                                        "Student"
                                );

                                loadAttendance();
                            }
                    );

            return;
        }


        /*
         * If schoolId was not passed,
         * fetch it from students collection.
         */
        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(
                        student -> {

                            if (student.exists()) {

                                schoolId =
                                        safe(
                                                student.getString(
                                                        "schoolId"
                                                )
                                        );


                                tvStudent.setText(
                                        buildStudentTitle(
                                                student
                                        )
                                );

                            } else {

                                tvStudent.setText(
                                        "Student"
                                );
                            }


                            loadAttendance();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            toast(
                                    "Unable to load student."
                            );
                        }
                );
    }


    // =====================================================
    // LOAD ATTENDANCE
    // =====================================================

    private void loadAttendance() {

        attendanceList.removeAllViews();

        items.clear();


        if (schoolId.isEmpty()) {

            tvSummary.setText(
                    "Attendance --"
            );

            return;
        }


        if (attendanceListener != null) {

            attendanceListener.remove();
        }


        attendanceListener =
                db.collection(
                                "studentAttendance"
                        )
                        .whereEqualTo(
                                "schoolId",
                                schoolId
                        )
                        .whereEqualTo(
                                "studentId",
                                studentId
                        )
                        .addSnapshotListener(
                                (
                                        snapshot,
                                        error
                                ) -> {

                                    if (error != null) {

                                        toast(
                                                "Unable to load attendance."
                                        );

                                        return;
                                    }


                                    if (snapshot == null) {

                                        return;
                                    }


                                    items.clear();


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


                                        /*
                                         * Half Day is treated
                                         * as Absent.
                                         */
                                        if (
                                                status.equalsIgnoreCase(
                                                        "Half Day"
                                                )
                                        ) {

                                            status =
                                                    "Absent";
                                        }


                                        if (
                                                !status.equalsIgnoreCase(
                                                        "Present"
                                                )
                                                        &&
                                                        !status.equalsIgnoreCase(
                                                                "Absent"
                                                        )
                                                        &&
                                                        !status.equalsIgnoreCase(
                                                                "Holiday"
                                                        )
                                        ) {

                                            continue;
                                        }


                                        String date =
                                                safe(
                                                        doc.getString(
                                                                "date"
                                                        )
                                                );


                                        items.add(
                                                new AttendanceItem(
                                                        date,
                                                        status
                                                )
                                        );
                                    }


                                    /*
                                     * Latest date first.
                                     */
                                    items.sort(
                                            Comparator.comparing(
                                                    AttendanceItem::date,
                                                    (
                                                            a,
                                                            b
                                                    ) ->
                                                            compareDates(
                                                                    b,
                                                                    a
                                                            )
                                            )
                                    );


                                    render();
                                }
                        );
    }


    // =====================================================
    // RENDER
    // =====================================================

    private void render() {

        attendanceList.removeAllViews();


        int present = 0;
        int absent = 0;
        int holiday = 0;


        for (
                AttendanceItem item :
                items
        ) {

            if (
                    item.status.equalsIgnoreCase(
                            "Present"
                    )
            ) {

                present++;

            } else if (
                    item.status.equalsIgnoreCase(
                            "Absent"
                    )
            ) {

                absent++;

            } else if (
                    item.status.equalsIgnoreCase(
                            "Holiday"
                    )
            ) {

                holiday++;
            }
        }


        int applicable =
                present + absent;


        String attendanceText;


        if (
                applicable == 0
                        &&
                        holiday > 0
        ) {

            attendanceText =
                    "Holiday";

        } else if (
                applicable == 0
        ) {

            attendanceText =
                    "--";

        } else {

            int percentage =
                    Math.round(
                            (
                                    present * 100f
                            ) / applicable
                    );


            attendanceText =
                    percentage + "%";
        }


        tvSummary.setText(
                "Attendance " +
                        attendanceText +
                        "  •  Present " +
                        present +
                        "  •  Absent " +
                        absent +
                        "  •  Holiday " +
                        holiday
        );


        if (items.isEmpty()) {

            TextView empty =
                    new TextView(this);


            empty.setText(
                    "No attendance records found."
            );


            empty.setTextColor(
                    0xFF777B86
            );


            empty.setTextSize(
                    15
            );


            empty.setGravity(
                    Gravity.CENTER
            );


            empty.setPadding(
                    20,
                    40,
                    20,
                    40
            );


            attendanceList.addView(
                    empty
            );


            return;
        }


        for (
                AttendanceItem item :
                items
        ) {

            addAttendanceCard(
                    item
            );
        }
    }


    // =====================================================
    // ATTENDANCE CARD
    // =====================================================

    private void addAttendanceCard(
            AttendanceItem item
    ) {

        MaterialCardView card =
                new MaterialCardView(this);


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );


        params.setMargins(
                0,
                0,
                0,
                8
        );


        card.setLayoutParams(
                params
        );


        card.setRadius(
                16
        );


        card.setCardElevation(
                1
        );


        LinearLayout root =
                new LinearLayout(this);


        root.setOrientation(
                LinearLayout.HORIZONTAL
        );


        root.setGravity(
                Gravity.CENTER_VERTICAL
        );


        root.setPadding(
                14,
                13,
                14,
                13
        );


        // DATE
        TextView date =
                new TextView(this);


        date.setText(
                displayDate(
                        item.date
                )
        );


        date.setTextColor(
                0xFF25252B
        );


        date.setTextSize(
                14
        );


        root.addView(
                date,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1f
                )
        );


        // STATUS
        TextView status =
                new TextView(this);


        status.setText(
                item.status
        );


        status.setTextColor(
                statusColor(
                        item.status
                )
        );


        status.setTextSize(
                13
        );


        status.setTypeface(
                null,
                1
        );


        root.addView(
                status
        );


        card.addView(
                root
        );


        attendanceList.addView(
                card
        );
    }


    // =====================================================
    // STATUS COLOR
    // =====================================================

    private int statusColor(
            String status
    ) {

        if (
                status.equalsIgnoreCase(
                        "Present"
                )
        ) {

            return 0xFF1F7A4D;
        }


        if (
                status.equalsIgnoreCase(
                        "Absent"
                )
        ) {

            return 0xFFB23B3B;
        }


        return 0xFFB17A00;
    }


    // =====================================================
    // DATE COMPARISON
    // =====================================================

    private int compareDates(
            String a,
            String b
    ) {

        Date da =
                parseDate(a);

        Date dbDate =
                parseDate(b);


        if (
                da == null
                        &&
                        dbDate == null
        ) {

            return b.compareTo(a);
        }


        if (da == null) {

            return 1;
        }


        if (dbDate == null) {

            return -1;
        }


        return da.compareTo(
                dbDate
        );
    }


    // =====================================================
    // PARSE DATE
    // =====================================================

    private Date parseDate(
            String value
    ) {

        try {

            return new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            ).parse(value);

        } catch (
                ParseException e
        ) {

            return null;
        }
    }


    // =====================================================
    // DISPLAY DATE
    // =====================================================

    private String displayDate(
            String value
    ) {

        Date date =
                parseDate(value);


        if (date == null) {

            return value;
        }


        return new SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
        ).format(date);
    }


    // =====================================================
    // STUDENT TITLE
    // =====================================================

    private String buildStudentTitle(
            DocumentSnapshot doc
    ) {

        if (
                doc == null
                        ||
                        !doc.exists()
        ) {

            return "Student";
        }


        String name =
                (
                        safe(
                                doc.getString(
                                        "firstName"
                                )
                        )
                                + " " +
                                safe(
                                        doc.getString(
                                                "middleName"
                                        )
                                )
                                + " " +
                                safe(
                                        doc.getString(
                                                "lastName"
                                        )
                                )
                )
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        /*
         * Fallback for studentName
         */
        if (name.isEmpty()) {

            name =
                    safe(
                            doc.getString(
                                    "studentName"
                            )
                    );
        }


        if (name.isEmpty()) {

            name = "Student";
        }


        String className =
                safe(
                        doc.getString(
                                "className"
                        )
                );


        String division =
                safe(
                        doc.getString(
                                "division"
                        )
                );


        if (
                !className.isEmpty()
        ) {

            if (
                    !division.isEmpty()
            ) {

                return name +
                        " • " +
                        className +
                        " " +
                        division;

            } else {

                return name +
                        " • " +
                        className;
            }
        }


        return name;
    }


    // =====================================================
    // SAFE STRING
    // =====================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    // =====================================================
    // TOAST
    // =====================================================

    private void toast(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }


    // =====================================================
    // DESTROY
    // =====================================================

    @Override
    protected void onDestroy() {

        if (
                attendanceListener != null
        ) {

            attendanceListener.remove();
        }


        attendanceListener = null;


        super.onDestroy();
    }


    // =====================================================
    // ATTENDANCE MODEL
    // =====================================================

    private static class AttendanceItem {

        private final String date;
        private final String status;


        AttendanceItem(
                String date,
                String status
        ) {

            this.date =
                    date;

            this.status =
                    status;
        }


        String date() {

            return date;
        }
    }
}