package com.example.myschool;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DemoDashboardActivity extends AppCompatActivity {

    private String role = "Admin";
    private DemoDataStore store;
    private LinearLayout content;
    private String currentCategory = "";
    private String currentTitle = "";

    private static class Feature {
        final String label;
        final String category;

        Feature(String label, String category) {
            this.label = label;
            this.category = category;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        role = getIntent().getStringExtra("demoRole");

        if (role == null ||
                (!role.equals("Admin") && !role.equals("Teacher"))) {
            role = "Admin";
        }

        store = new DemoDataStore(this);
        buildHome();
    }

    private void buildHome() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF7F5FC);

        TextView header = new TextView(this);
        header.setText("‹  Exit Demo                    " + role + " Demo");
        header.setTextColor(0xFF4527A0);
        header.setTextSize(18);
        header.setPadding(dp(18), dp(18), dp(12), dp(18));
        header.setBackgroundColor(0xFFFFFFFF);

        header.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Exit Demo?")
                        .setMessage("Return to the login page?")
                        .setNegativeButton("Stay", (dialog, which) ->
                                dialog.dismiss())
                        .setPositiveButton("Exit", (dialog, which) ->
                                finish())
                        .show()
        );

        root.addView(header);

        TextView banner = new TextView(this);
        banner.setText(
                "DEMO MODE • Sample data only • Not connected to real school records"
        );
        banner.setTextColor(0xFF6D28D9);
        banner.setTextSize(12);
        banner.setPadding(dp(16), dp(12), dp(16), dp(12));
        root.addView(banner);

        ScrollView scroll = new ScrollView(this);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(4), dp(14), dp(24));

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        setContentView(root);

        showFeatureMenu();
    }

    private void showFeatureMenu() {
        currentCategory = "";
        content.removeAllViews();

        TextView title = text(
                "Welcome, Demo " + role,
                23,
                0xFF222222,
                true
        );
        title.setPadding(dp(4), dp(8), dp(4), dp(12));
        content.addView(title);

        TextView desc = text(
                "Explore the features below. Changes made here are saved only in this app's local demo database.",
                14,
                0xFF666666,
                false
        );
        desc.setPadding(dp(4), 0, dp(4), dp(14));
        content.addView(desc);

        Feature[] features;

        if (role.equals("Admin")) {
            features = new Feature[]{
                    new Feature("Students", "Students"),
                    new Feature("Teachers / Staff", "Teachers"),
                    new Feature("Classes", "Classes"),
                    new Feature("Attendance", "Attendance"),
                    new Feature("Homework", "Homework"),
                    new Feature("Exams", "Exams"),
                    new Feature("Marks", "Marks"),
                    new Feature("Fees", "Fees"),
                    new Feature("Timetable", "Timetable"),
                    new Feature("Notices", "Notices")
            };
        } else {
            features = new Feature[]{
                    new Feature("My Class / Students", "Students"),
                    new Feature("Attendance", "Attendance"),
                    new Feature("Homework", "Homework"),
                    new Feature("Conduct Test", "Exams"),
                    new Feature("Fill Marks", "Marks"),
                    new Feature("Timetable", "Timetable"),
                    new Feature("Notices", "Notices")
            };
        }

        for (Feature feature : features) {
            Button featureButton = new Button(this);

            featureButton.setText(feature.label + "  ›");
            featureButton.setAllCaps(false);
            featureButton.setTextSize(15);
            featureButton.setTextColor(0xFF3F2A72);
            featureButton.setBackgroundTintList(
                    ColorStateList.valueOf(0xFFFFFFFF)
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(54)
                    );

            params.setMargins(0, 0, 0, dp(9));
            content.addView(featureButton, params);

            featureButton.setOnClickListener(v ->
                    showRecords(feature)
            );
        }
    }

    private void showRecords(Feature feature) {
        currentCategory = feature.category;
        currentTitle = feature.label;

        content.removeAllViews();

        Button back = new Button(this);
        back.setText("‹  Back to " + role + " Demo");
        back.setAllCaps(false);

        content.addView(back);

        back.setOnClickListener(v -> showFeatureMenu());

        TextView title = text(
                feature.label,
                22,
                0xFF222222,
                true
        );
        title.setPadding(0, dp(12), 0, dp(10));
        content.addView(title);

        TextView info = text(
                "Shared demo data • Local device only",
                12,
                0xFF777777,
                false
        );
        info.setPadding(0, 0, 0, dp(12));
        content.addView(info);

        ArrayList<DemoDataStore.DemoRecord> records =
                store.getRecords(feature.category);

        if (records.isEmpty()) {
            TextView empty = text(
                    "No demo records yet.",
                    15,
                    0xFF777777,
                    false
            );

            empty.setPadding(0, dp(12), 0, dp(12));
            content.addView(empty);

        } else {
            for (DemoDataStore.DemoRecord record : records) {
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(
                        dp(14),
                        dp(13),
                        dp(14),
                        dp(13)
                );
                card.setBackgroundColor(0xFFFFFFFF);

                LinearLayout.LayoutParams cardParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                cardParams.setMargins(0, 0, 0, dp(10));
                content.addView(card, cardParams);

                card.addView(text(
                        record.title,
                        16,
                        0xFF222222,
                        true
                ));

                card.addView(text(
                        record.details,
                        14,
                        0xFF666666,
                        false
                ));

                TextView stamp = text(
                        record.createdAt,
                        11,
                        0xFF999999,
                        false
                );

                stamp.setPadding(0, dp(7), 0, 0);
                card.addView(stamp);
            }
        }

        if (canAdd(feature.category)) {
            Button add = new Button(this);

            add.setText(addLabel(feature.category));
            add.setAllCaps(false);

            content.addView(add);

            add.setOnClickListener(v ->
                    showAddDialog(feature.category)
            );
        }
    }

    private boolean canAdd(String category) {
        if (role.equals("Admin")) {
            return true;
        }

        return category.equals("Attendance")
                || category.equals("Homework")
                || category.equals("Exams")
                || category.equals("Marks")
                || category.equals("Notices");
    }

    private String addLabel(String category) {
        switch (category) {
            case "Students":
                return "＋ Add Demo Student";

            case "Teachers":
                return "＋ Add Demo Teacher";

            case "Classes":
                return "＋ Add Demo Class";

            case "Attendance":
                return "＋ Record Demo Attendance";

            case "Homework":
                return "＋ Add Demo Homework";

            case "Exams":
                return "＋ Add Demo Test";

            case "Marks":
                return "＋ Add Demo Marks";

            case "Fees":
                return "＋ Add Demo Fee Record";

            case "Timetable":
                return "＋ Add Demo Timetable Entry";

            default:
                return "＋ Add Demo Notice";
        }
    }

    private void showAddDialog(String category) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);

        EditText titleInput = new EditText(this);
        titleInput.setHint(titleHint(category));
        titleInput.setSingleLine(true);
        form.addView(titleInput);

        EditText detailsInput = new EditText(this);
        detailsInput.setHint(
                "Details (class, subject, date, marks, etc.)"
        );

        detailsInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        detailsInput.setMinLines(2);
        form.addView(detailsInput);

        new AlertDialog.Builder(this)
                .setTitle(
                        addLabel(category).replace("＋ ", "")
                )
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {

                    String name = titleInput.getText()
                            .toString()
                            .trim();

                    String detail = detailsInput.getText()
                            .toString()
                            .trim();

                    if (name.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Please enter a title/name.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    long result = store.addRecord(
                            category,
                            name,
                            detail
                    );

                    if (result == -1) {
                        Toast.makeText(
                                this,
                                "Could not save demo record.",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {
                        Toast.makeText(
                                this,
                                "Saved in demo mode only.",
                                Toast.LENGTH_SHORT
                        ).show();

                        showRecords(
                                new Feature(
                                        currentTitle,
                                        currentCategory
                                )
                        );
                    }
                })
                .show();
    }

    private String titleHint(String category) {
        switch (category) {
            case "Students":
                return "Student name";

            case "Teachers":
                return "Teacher name";

            case "Classes":
                return "Class and division";

            case "Attendance":
                return "Student name — Present/Absent";

            case "Homework":
                return "Homework title";

            case "Exams":
                return "Test / exam name";

            case "Marks":
                return "Student name — Subject";

            case "Fees":
                return "Student fee record";

            case "Timetable":
                return "Day — Period";

            default:
                return "Notice title";
        }
    }

    private TextView text(
            String value,
            int size,
            int color,
            boolean bold
    ) {
        TextView tv = new TextView(this);

        tv.setText(value == null ? "" : value);
        tv.setTextSize(size);
        tv.setTextColor(color);

        if (bold) {
            tv.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        return tv;
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    @Override
    protected void onDestroy() {
        if (store != null) {
            store.close();
        }

        super.onDestroy();
    }
}
