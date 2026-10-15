package com.example.myschool;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout list;
    private String schoolId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_events);

        schoolId = getIntent().getStringExtra("schoolId");

        if (schoolId == null || schoolId.trim().isEmpty()) {
            schoolId = getSharedPreferences(
                    "MySchoolPrefs", MODE_PRIVATE
            ).getString("schoolId", "");
        }

        db = FirebaseFirestore.getInstance();

        list = findViewById(R.id.layoutEventsList);

        TextView header = findViewById(R.id.tvEventsHeader);
        header.setOnClickListener(v -> finish());

        if (schoolId == null || schoolId.trim().isEmpty()) {
            empty("School information not found");
            return;
        }

        loadEvents();
    }

    private void loadEvents() {
        db.collection("specialOccasions")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnSuccessListener(occasions -> {

                    List<EventItem> items = new ArrayList<>();

                    for (DocumentSnapshot document :
                            occasions.getDocuments()) {
                        items.add(createItem(document, "Special Occasion"));
                    }

                    db.collection("meetings")
                            .whereEqualTo("schoolId", schoolId)
                            .get()
                            .addOnSuccessListener(meetings -> {

                                for (DocumentSnapshot document :
                                        meetings.getDocuments()) {
                                    items.add(createItem(document, "Meeting"));
                                }

                                Collections.sort(items,
                                        Comparator.comparingLong(item ->
                                                item.dateValue == null
                                                        ? Long.MAX_VALUE
                                                        : item.dateValue.getTime()));

                                list.removeAllViews();

                                if (items.isEmpty()) {
                                    empty("No events available");
                                    return;
                                }

                                for (EventItem item : items) {
                                    card(item);
                                }
                            })
                            .addOnFailureListener(error ->
                                    empty("Unable to load meetings"));
                })
                .addOnFailureListener(error ->
                        empty("Unable to load events"));
    }

    private EventItem createItem(
            DocumentSnapshot document, String type) {

        EventItem item = new EventItem();

        item.type = type;
        item.title = str(document, "title");

        if (item.title.isEmpty()) {
            item.title = type;
        }

        item.date = str(document, "date");
        item.time = str(document, "time");
        item.venue = str(document, "venue");
        item.description = str(document, "description");
        item.dateValue = parseDate(item.date);

        return item;
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

    private void card(EventItem item) {
        StringBuilder content = new StringBuilder();

        content.append("⭐  ")
                .append(item.type)
                .append("\n")
                .append(item.title);

        if (!item.date.isEmpty()) {
            content.append("\n📅 ").append(item.date);
        }

        if (!item.time.isEmpty()) {
            content.append("  •  ").append(item.time);
        }

        if (!item.venue.isEmpty()) {
            content.append("\n📍 ").append(item.venue);
        }

        if (!item.description.isEmpty()) {
            content.append("\n\n").append(item.description);
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

    private static class EventItem {
        String type;
        String title;
        String date;
        String time;
        String venue;
        String description;
        Date dateValue;
    }
}
