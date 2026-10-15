
package com.example.myschool;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AppDemoActivity extends AppCompatActivity {

    private final int purple = Color.rgb(109, 40, 217);
    private final int ink = Color.rgb(35, 38, 55);
    private final int muted = Color.rgb(100, 105, 120);
    private final int background = Color.rgb(248, 247, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(background);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);

        // Top, left, right and bottom spacing
        page.setPadding(dp(30), dp(30), dp(30), dp(28));

        scroll.addView(page);

        // Back button
        TextView back = text(
                "\u2190  Back to Login", 15, purple, true
        );

        back.setPadding(0, 0, 0, dp(20));
        back.setOnClickListener(v -> finish());
        page.addView(back);

        // Main title
        page.addView(text(
                "My School", 30, ink, true
        ));

        TextView subtitle = text(
                "APP INFORMATION", 12, purple, true
        );

        subtitle.setPadding(0, dp(5), 0, dp(18));
        page.addView(subtitle);

        // Hero section
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(20), dp(18), dp(18));
        hero.setBackground(bg(purple, 20));

        LinearLayout.LayoutParams heroParams =
                new LinearLayout.LayoutParams(-1, -2);

        heroParams.bottomMargin = dp(12);
        hero.setLayoutParams(heroParams);

        addText(hero,
                "One app. A connected school.",
                23, Color.WHITE, true, 0, 0, 0, 10);

        addText(hero,
                "MySCHOOL helps bring school activities, "
                        + "academic information and communication "
                        + "together in one place.",
                14, Color.WHITE, false, 0, 0, 0, 18);

        addPill(hero, "Admin",
                "Manage school operations and student records");

        addPill(hero, "Teacher",
                "Attendance, homework, tests and marks");

        addPill(hero, "Parent",
                "Student updates and academic information");

        page.addView(hero);

        // App design
        addSection(page,
                "01  APP DESIGN",
                "Explore the main features of MySCHOOL");

        addInfoCard(page,
                "\uD83C\uDFEB  Admin Dashboard",
                "Manage students, teachers, classes, timetables, "
                        + "notices and other school activities.");

        addInfoCard(page,
                "\uD83D\uDC68\u200D\uD83C\uDFEB  Teacher Workspace",
                "Access attendance, homework, conduct tests, "
                        + "marks entry, assignments, notices "
                        + "and parent messages.");

        addInfoCard(page,
                "\uD83D\uDC68\u200D\uD83D\uDC67  Parent Dashboard",
                "View student information, attendance, notices "
                        + "and academic progress, depending on "
                        + "the features enabled in the app.");

        // Data connection
        addSection(page,
                "02  DATA & CONNECTION",
                "How information travels through the app");

        LinearLayout flow = card();

        addFlowStep(flow,
                "1",
                "User Action",
                "A user opens a feature, such as Attendance "
                        + "or Homework.");

        addFlowStep(flow,
                "2",
                "Firebase Request",
                "The relevant screen requests data from "
                        + "the configured Firebase service.");

        addFlowStep(flow,
                "3",
                "Database Response",
                "Firebase returns the requested data. "
                        + "Configured real-time listeners can "
                        + "receive subsequent changes.");

        addFlowStep(flow,
                "4",
                "Display & Updates",
                "The app updates the relevant screen "
                        + "according to its implementation "
                        + "and the user's access permissions.");

        addText(flow,
                "Note: The actual database paths, listeners "
                        + "and permissions depend on the project's "
                        + "Firebase configuration and code.",
                12, muted, false, 0, 8, 0, 0);

        page.addView(flow);

        // Security information
        addSection(page,
                "03  IMPORTANT INFORMATION",
                "A quick note about access and security");

        addInfoCard(page,
                "\uD83D\uDD12  Secure Access",
                "Admin, Teacher and Parent access should be "
                        + "controlled by authentication and "
                        + "appropriate database security rules.");

        addInfoCard(page,
                "\u2139\uFE0F  App Information",
                "This page explains the app's design and "
                        + "general data flow. It does not provide "
                        + "a separate trial account or modify "
                        + "school records.");

        // Footer
        TextView footer = text(
                "My School  \u2022  App Information",
                12, muted, true
        );

        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(20), 0, 0);
        page.addView(footer);

        setContentView(scroll);
    }

    private void addSection(
            LinearLayout root,
            String title,
            String description) {

        TextView heading = text(title, 17, ink, true);
        heading.setPadding(0, dp(20), 0, dp(5));
        root.addView(heading);

        TextView sub = text(description, 13, muted, false);
        sub.setPadding(0, 0, 0, dp(12));
        root.addView(sub);
    }

    private void addInfoCard(
            LinearLayout root,
            String title,
            String description) {

        LinearLayout c = card();

        addText(c, title, 16, ink, true,
                0, 0, 0, 7);

        addText(c, description, 14, muted, false,
                0, 0, 0, 0);

        root.addView(c);
    }

    private void addPill(
            LinearLayout root,
            String title,
            String description) {

        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.VERTICAL);
        line.setPadding(dp(12), dp(10), dp(12), dp(10));
        line.setBackground(bg(0x33FFFFFF, 12));

        addText(line, title, 15, Color.WHITE, true,
                0, 0, 0, 3);

        addText(line, description, 12, Color.WHITE, false,
                0, 0, 0, 0);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, -2);

        lp.bottomMargin = dp(8);
        root.addView(line, lp);
    }

    private void addFlowStep(
            LinearLayout root,
            String number,
            String title,
            String description) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);

        TextView badge = text(number, 15, Color.WHITE, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(bg(purple, 30));

        row.addView(badge,
                new LinearLayout.LayoutParams(dp(34), dp(34)));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), 0, 0, dp(18));

        addText(content, title, 15, ink, true,
                0, 0, 0, 4);

        addText(content, description, 13, muted, false,
                0, 0, 0, 0);

        row.addView(content,
                new LinearLayout.LayoutParams(0, -2, 1));

        root.addView(row);
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        c.setBackground(bg(Color.WHITE, 18));
        c.setElevation(dp(2));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, -2);

        lp.bottomMargin = dp(10);
        c.setLayoutParams(lp);

        return c;
    }

    private void addText(
            LinearLayout root,
            String value,
            int size,
            int color,
            boolean bold,
            int left,
            int top,
            int right,
            int bottom) {

        TextView v = text(value, size, color, bold);

        v.setPadding(
                dp(left),
                dp(top),
                dp(right),
                dp(bottom)
        );

        root.addView(v);
    }

    private TextView text(
            String value,
            int size,
            int color,
            boolean bold) {

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);

        if (bold) {
            v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return v;
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }
}
