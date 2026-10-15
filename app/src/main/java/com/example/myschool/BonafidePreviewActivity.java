
package com.example.myschool;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BonafidePreviewActivity extends AppCompatActivity {

    private TextView tvSchoolName;
    private TextView tvSchoolAddress;
    private TextView tvCertificateNumber;
    private TextView tvCertificateBody;
    private TextView tvPurpose;
    private TextView tvCertificateDate;
    private TextView tvPrincipalName;

    private Button btnPrint;
    private Button btnShare;
    private ImageButton btnBack;

    private LinearLayout certificateView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonafide_preview);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupBack();
        setupButtons();

        loadCertificateData();
        loadSchoolData();
    }

    // ---------------------------------------------------------
    // INITIALIZE VIEWS
    // ---------------------------------------------------------

    private void initViews() {

        tvSchoolName = findViewById(R.id.tvSchoolName);
        tvSchoolAddress = findViewById(R.id.tvSchoolAddress);

        tvCertificateNumber =
                findViewById(R.id.tvCertificateNumber);

        tvCertificateBody =
                findViewById(R.id.tvCertificateBody);

        tvPurpose = findViewById(R.id.tvPurpose);

        tvCertificateDate =
                findViewById(R.id.tvCertificateDate);

        tvPrincipalName =
                findViewById(R.id.tvPrincipalName);

        btnPrint = findViewById(R.id.btnPrint);
        btnShare = findViewById(R.id.btnShare);
        btnBack = findViewById(R.id.btnBack);

        certificateView = findViewById(R.id.certificateView);
    }

    // ---------------------------------------------------------
    // BUTTONS
    // ---------------------------------------------------------

    private void setupButtons() {

        btnPrint.setOnClickListener(v -> printCertificate());

        btnShare.setOnClickListener(v -> shareCertificate());
    }

    private void setupBack() {
        btnBack.setOnClickListener(v -> finish());
    }

    // ---------------------------------------------------------
    // LOAD CERTIFICATE DATA
    // ---------------------------------------------------------

    private void loadCertificateData() {

        Intent intent = getIntent();

        studentId = intent.getStringExtra("studentId");

        String certificateNumber =
                safeString(intent.getStringExtra("certificateNumber"));

        String purpose =
                safeString(intent.getStringExtra("purpose"));

        String date =
                safeString(intent.getStringExtra("date"));

        String principalName =
                safeString(intent.getStringExtra("principalName"));

        String template =
                safeString(intent.getStringExtra("template"));

        if (certificateNumber.isEmpty()) {
            certificateNumber = "N/A";
        }

        if (purpose.isEmpty()) {
            purpose = "General Purpose";
        }

        if (date.isEmpty()) {
            date = java.text.DateFormat
                    .getDateInstance()
                    .format(new java.util.Date());
        }

        if (principalName.isEmpty()) {
            principalName = "Principal";
        }

        if (template.isEmpty()) {
            template = "Classic School";
        }

        tvCertificateNumber.setText(
                "Certificate No: " + certificateNumber
        );

        tvPurpose.setText("Purpose: " + purpose);

        tvCertificateDate.setText("Date: " + date);

        tvPrincipalName.setText(principalName);

        applyTemplate(template);

        if (studentId == null || studentId.trim().isEmpty()) {

            tvCertificateBody.setText(
                    "Student information is not available."
            );

            Toast.makeText(
                    this,
                    "Student information missing",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        loadStudent(studentId);
    }

    // ---------------------------------------------------------
    // LOAD SCHOOL DATA FROM FIRESTORE
    // ---------------------------------------------------------

    private void loadSchoolData() {

        if (mAuth.getCurrentUser() == null) {

            tvSchoolName.setText("MY SCHOOL");
            tvSchoolAddress.setText("School Address");

            return;
        }

        String schoolId = mAuth.getCurrentUser().getUid();

        db.collection("schools")
                .document(schoolId)
                .get()
                .addOnSuccessListener(document -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (!document.exists()) {

                        tvSchoolName.setText("MY SCHOOL");
                        tvSchoolAddress.setText("School Address");

                        return;
                    }

                    String schoolName =
                            getFirestoreString(document, "schoolName");

                    String schoolAddress =
                            getFirestoreString(document, "schoolAddress");

                    tvSchoolName.setText(
                            schoolName.isEmpty()
                                    ? "MY SCHOOL"
                                    : schoolName
                    );

                    tvSchoolAddress.setText(
                            schoolAddress.isEmpty()
                                    ? "School Address"
                                    : schoolAddress
                    );
                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    tvSchoolName.setText("MY SCHOOL");
                    tvSchoolAddress.setText("School Address");

                    Toast.makeText(
                            this,
                            "Failed to load school information",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // LOAD STUDENT DATA
    // ---------------------------------------------------------

    private void loadStudent(String id) {

        db.collection("students")
                .document(id)
                .get()
                .addOnSuccessListener(document -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (!document.exists()) {

                        tvCertificateBody.setText(
                                "Student record was not found."
                        );

                        Toast.makeText(
                                this,
                                "Student not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    showStudent(document);
                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            "Failed to load student information",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // DISPLAY STUDENT DETAILS
    // ---------------------------------------------------------

    private void showStudent(DocumentSnapshot document) {

        String firstName =
                getFirestoreString(document, "firstName");

        String middleName =
                getFirestoreString(document, "middleName");

        String lastName =
                getFirestoreString(document, "lastName");

        String studentName =
                (firstName + " " + middleName + " " + lastName)
                        .replaceAll("\\s+", " ")
                        .trim();

        String fatherName =
                getFirestoreString(document, "fatherName");

        String className =
                getFirestoreString(document, "className");

        String division =
                getFirestoreString(document, "division");

        String admissionNumber =
                getFirestoreString(document, "admissionNumber");

        String academicYear =
                getFirestoreString(document, "academicYear");

        if (studentName.isEmpty()) {
            studentName = "Student";
        }

        if (fatherName.isEmpty()) {
            fatherName = "Parent / Guardian";
        }

        if (className.isEmpty()) {
            className = "-";
        }

        if (division.isEmpty()) {
            division = "-";
        }

        if (admissionNumber.isEmpty()) {
            admissionNumber = "-";
        }

        if (academicYear.isEmpty()) {
            academicYear = "-";
        }

        String body =
                "This is to certify that " + studentName +
                        ", son/daughter of " + fatherName +
                        ", is a bonafide student of this school. " +
                        "He/She is studying in Class " + className +
                        " - " + division +
                        " during the academic year " + academicYear +
                        ". Admission Number: " + admissionNumber + ".";

        tvCertificateBody.setText(body);
    }

    // ---------------------------------------------------------
    // APPLY CERTIFICATE TEMPLATE
    // ---------------------------------------------------------

    private void applyTemplate(String template) {

        switch (template) {

            case "Modern":

                tvSchoolName.setTextColor(
                        Color.parseColor("#4B1FA8")
                );

                tvCertificateBody.setTextColor(
                        Color.parseColor("#333333")
                );

                tvCertificateBody.setTextSize(17);
                tvCertificateBody.setLineSpacing(8, 1);
                break;

            case "Elegant":

                tvSchoolName.setTextColor(
                        Color.parseColor("#6D4C41")
                );

                tvCertificateBody.setTextColor(
                        Color.parseColor("#4E342E")
                );

                tvCertificateBody.setTextSize(16);
                tvCertificateBody.setLineSpacing(10, 1);
                break;

            case "Formal":

                tvSchoolName.setTextColor(Color.BLACK);

                tvCertificateBody.setTextColor(
                        Color.parseColor("#222222")
                );

                tvCertificateBody.setTextSize(15);
                tvCertificateBody.setLineSpacing(7, 1);
                break;

            case "Premium":

                tvSchoolName.setTextColor(
                        Color.parseColor("#5D4037")
                );

                tvCertificateBody.setTextColor(
                        Color.parseColor("#3E2723")
                );

                tvCertificateBody.setTextSize(17);
                tvCertificateBody.setLineSpacing(10, 1);
                break;

            case "Classic School":
            default:

                tvSchoolName.setTextColor(
                        Color.parseColor("#17102D")
                );

                tvCertificateBody.setTextColor(
                        Color.parseColor("#333333")
                );

                tvCertificateBody.setTextSize(16);
                tvCertificateBody.setLineSpacing(7, 1);
                break;
        }
    }

    // ---------------------------------------------------------
    // CREATE CERTIFICATE PDF
    // ---------------------------------------------------------


    private File createCertificatePdf() throws IOException {

        File directory = new File(getCacheDir(), "certificates");

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create PDF folder");
        }

        File pdfFile = new File(directory, "BonafideCertificate.pdf");

        // A4 page size in PDF points
        final int pageWidth = 595;
        final int pageHeight = 842;
        final int margin = 25;

        float density = getResources().getDisplayMetrics().density;

        // IMPORTANT: Convert PDF width to view pixels
        int contentWidth = Math.round(
                (pageWidth - (margin * 2)) * density
        );

        certificateView.measure(
                View.MeasureSpec.makeMeasureSpec(
                        contentWidth,
                        View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                )
        );

        int contentHeight = certificateView.getMeasuredHeight();

        if (contentHeight <= 0) {
            throw new IOException("Certificate has no content");
        }

        certificateView.layout(
                0,
                0,
                contentWidth,
                contentHeight
        );

        PdfDocument document = new PdfDocument();

        try {

            PdfDocument.PageInfo pageInfo =
                    new PdfDocument.PageInfo.Builder(
                            pageWidth,
                            pageHeight,
                            1
                    ).create();

            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            canvas.drawColor(Color.WHITE);

            float availableWidth = pageWidth - (margin * 2);
            float availableHeight = pageHeight - (margin * 2);

            float scale = Math.min(
                    availableWidth / contentWidth,
                    availableHeight / contentHeight
            );

            float drawnWidth = contentWidth * scale;
            float left = (pageWidth - drawnWidth) / 2f;

            canvas.save();
            canvas.translate(left, margin);
            canvas.scale(scale, scale);

            certificateView.draw(canvas);

            canvas.restore();

            document.finishPage(page);

            try (FileOutputStream output =
                         new FileOutputStream(pdfFile)) {

                document.writeTo(output);
                output.flush();
            }

        } finally {
            document.close();
        }

        return pdfFile;
    }

    // ---------------------------------------------------------
    // PRINT / SAVE PDF
    // ---------------------------------------------------------

    private void printCertificate() {

        try {

            File pdfFile = createCertificatePdf();

            PrintManager printManager =
                    (PrintManager) getSystemService(PRINT_SERVICE);

            if (printManager == null) {

                Toast.makeText(
                        this,
                        "Printing is not available",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            PrintDocumentAdapter adapter =
                    new PrintDocumentAdapter() {

                        @Override
                        public void onLayout(
                                PrintAttributes oldAttributes,
                                PrintAttributes newAttributes,
                                CancellationSignal cancellationSignal,
                                LayoutResultCallback callback,
                                Bundle extras) {

                            if (cancellationSignal.isCanceled()) {
                                callback.onLayoutCancelled();
                                return;
                            }

                            PrintDocumentInfo info =
                                    new PrintDocumentInfo.Builder(
                                            "BonafideCertificate.pdf"
                                    )
                                            .setContentType(
                                                    PrintDocumentInfo.CONTENT_TYPE_DOCUMENT
                                            )
                                            .setPageCount(1)
                                            .build();

                            callback.onLayoutFinished(info, true);
                        }

                        @Override
                        public void onWrite(
                                PageRange[] pages,
                                ParcelFileDescriptor destination,
                                CancellationSignal cancellationSignal,
                                WriteResultCallback callback) {

                            try (
                                    FileInputStream input =
                                            new FileInputStream(pdfFile);

                                    FileOutputStream output =
                                            new FileOutputStream(
                                                    destination.getFileDescriptor()
                                            )
                            ) {

                                byte[] buffer = new byte[8192];
                                int length;

                                while ((length = input.read(buffer)) != -1) {

                                    if (cancellationSignal.isCanceled()) {
                                        callback.onWriteCancelled();
                                        return;
                                    }

                                    output.write(buffer, 0, length);
                                }

                                output.flush();

                                callback.onWriteFinished(
                                        new PageRange[]{
                                                new PageRange(0, 0)
                                        }
                                );

                            } catch (IOException e) {

                                callback.onWriteFailed(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Unable to print PDF"
                                );
                            }
                        }
                    };

            PrintAttributes attributes =
                    new PrintAttributes.Builder()
                            .setMediaSize(
                                    PrintAttributes.MediaSize.ISO_A4
                            )
                            .setResolution(
                                    new PrintAttributes.Resolution(
                                            "certificate",
                                            "Certificate",
                                            300,
                                            300
                                    )
                            )
                            .setMinMargins(
                                    PrintAttributes.Margins.NO_MARGINS
                            )
                            .build();

            printManager.print(
                    "Bonafide Certificate",
                    adapter,
                    attributes
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "PDF creation failed: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ---------------------------------------------------------
    // SHARE CERTIFICATE PDF
    // ---------------------------------------------------------

    private void shareCertificate() {

        try {

            File pdfFile = createCertificatePdf();

            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            shareIntent.setType("application/pdf");

            shareIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    pdfUri
            );

            shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Bonafide Certificate"
            );

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Please find the attached bonafide certificate."
            );

            shareIntent.setClipData(
                    ClipData.newUri(
                            getContentResolver(),
                            "Bonafide Certificate",
                            pdfUri
                    )
            );

            shareIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(
                    Intent.createChooser(
                            shareIntent,
                            "Share Bonafide Certificate"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Sharing failed: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ---------------------------------------------------------
    // FIRESTORE STRING HELPER
    // ---------------------------------------------------------

    private String getFirestoreString(
            DocumentSnapshot document,
            String key) {

        Object value = document.get(key);

        return value == null
                ? ""
                : String.valueOf(value).trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }
}
