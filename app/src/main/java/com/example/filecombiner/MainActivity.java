package com.example.filecombiner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FILES_REQUEST = 1;
    private TextView txtOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPickFiles = findViewById(R.id.btnPickFiles);
        txtOutput = findViewById(R.id.txtOutput);

        btnPickFiles.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_FILES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILES_REQUEST && resultCode == RESULT_OK && data != null) {
            List<Uri> uris = new ArrayList<>();

            if (data.getClipData() != null) {
                // Multiple files selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    uris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                // Single file selected
                uris.add(data.getData());
            }

            String combinedText = readMultipleFiles(this, uris);
            txtOutput.setText(combinedText);

            File savedFile = saveAsTextFile(this, combinedText);
            Toast.makeText(this, "Saved: " + savedFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    public String readMultipleFiles(Context context, List<Uri> fileUris) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < fileUris.size(); i++) {
            Uri uri = fileUris.get(i);

            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                result.append("----- FILE ").append(i + 1).append(" START -----\n");

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                result.append("----- FILE ").append(i + 1).append(" END -----\n\n");

            } catch (Exception e) {
                e.printStackTrace();
                result.append("Error reading file ").append(i + 1).append("\n\n");
            }
        }

        return result.toString();
    }

    public File saveAsTextFile(Context context, String content) {
        File file = new File(context.getExternalFilesDir(null), "combined_files.txt");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }
}
