package com.example.myschool;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentNoticesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout list;
    private String schoolId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_notices);

        schoolId = getIntent().getStringExtra("schoolId");

        if (schoolId == null || schoolId.trim().isEmpty()) {
            schoolId = getSharedPreferences(
                    "MySchoolPrefs", MODE_PRIVATE
            ).getString("schoolId", "");
        }

        db = FirebaseFirestore.getInstance();

        list = findViewById(R.id.layoutNoticesList);

        TextView header = findViewById(R.id.tvNoticesHeader);
        header.setOnClickListener(v -> finish());

        if (schoolId == null || schoolId.trim().isEmpty()) {
            empty("School information not found");
            return;
        }

        loadNotices();
    }

    private void loadNotices() {
        db.collection("notices")
                .whereEqualTo("schoolId", schoolId)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null) {
                        empty("Unable to load notices");
                        return;
                    }

                    list.removeAllViews();

                    if (snapshot == null || snapshot.isEmpty()) {
                        empty("No notices available");
                        return;
                    }

                    List<DocumentSnapshot> items =
                            new ArrayList<>(snapshot.getDocuments());

                    Collections.sort(items,
                            (a, b) -> Long.compare(timeOf(b), timeOf(a)));

                    for (DocumentSnapshot document : items) {
                        card(document);
                    }
                });
    }

    private long timeOf(DocumentSnapshot document) {
        Object value = document.get("createdAt");

        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate().getTime();
        }

        Date date = parseDate(str(document, "date"));
        return date == null ? 0 : date.getTime();
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] formats = {
                "dd/MM/yyyy",
                "dd-MM-yyyy",
                "yyyy-MM-dd"
        };

        for (String format : formats) {
            try {
                return new SimpleDateFormat(
                        format, Locale.getDefault()
                ).parse(value);
            } catch (Exception ignored) {
                // Try the next format.
            }
        }

        return null;
    }

    private String str(DocumentSnapshot document, String key) {
        Object value = document.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void card(DocumentSnapshot document) {
        String title = str(document, "title");
        String description = str(document, "description");
        String date = str(document, "date");

        if (title.isEmpty()) {
            title = "School Notice";
        }

        StringBuilder content = new StringBuilder();
        content.append("📢  ").append(title);

        if (!date.isEmpty()) {
            content.append("\n📅 ").append(date);
        }

        if (!description.isEmpty()) {
            content.append("\n\n").append(description);
        }

        String author = str(document, "createdByRole");

        if (author.isEmpty()) {
            author = str(document, "role");
        }

        if (!author.isEmpty()) {
            content.append("\n\nPosted by: ").append(author);
        }

        TextView text = new TextView(this);
        text.setText(content.toString());
        text.setTextSize(14);
        text.setTextColor(Color.rgb(65, 65, 65));
        text.setPadding(dp(16), dp(15), dp(16), dp(15));
        text.setBackground(cardBackground());
        text.setElevation(dp(2));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(10);
        list.addView(text, params);
    }

    private void empty(String message) {
        list.removeAllViews();

        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(Color.GRAY);
        text.setTextSize(14);
        text.setGravity(Gravity.CENTER);
        text.setPadding(dp(10), dp(30), dp(10), dp(30));

        list.addView(text, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }

    private GradientDrawable cardBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(16));
        background.setStroke(dp(1), Color.rgb(238, 231, 218));
        return background;
    }

    private int dp(float value) {
        return (int) (value
                * getResources().getDisplayMetrics().density + 0.5f);
    }
}
