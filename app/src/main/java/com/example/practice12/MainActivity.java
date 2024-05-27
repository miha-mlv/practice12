package com.example.practice12;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etSurname;
    private EditText etAge;
    private EditText etEmail;
    private TextView tvAllUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAllUsers = findViewById(R.id.tvAllUsers);
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        Button btnInsert = findViewById(R.id.btnInsert);
        Button btnQuery = findViewById(R.id.btnQuery);

        btnInsert.setOnClickListener(v -> insertPerson());

        btnQuery.setOnClickListener(v -> queryPeople());
    }

    private void insertPerson() {
        String name = etName.getText().toString();
        String surname = etSurname.getText().toString();
        int age = Integer.parseInt(etAge.getText().toString());
        String email = etEmail.getText().toString();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("surname", surname);
        values.put("age", age);
        values.put("email", email);

        Uri uri = getContentResolver().insert(Uri.parse("content://com.example.practice12.provider/users"), values);
        if (uri != null) {
            Toast.makeText(this, "Person inserted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to insert person", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryPeople() {
        Cursor cursor = getContentResolver().query(BookProvider.CONTENT_URI, null, null, null, null);
        StringBuilder sb = new StringBuilder();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("surname"));
                @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex("age"));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex("email"));

                sb.append("Name: ").append(name).append("\n");
                sb.append("Surname: ").append(address).append("\n");
                sb.append("Age: ").append(age).append("\n");
                sb.append("Email: ").append(email).append("\n\n");

            } while (cursor.moveToNext());
            cursor.close();
            tvAllUsers.setText(sb);
        } else {
            Toast.makeText(this, "user not found", Toast.LENGTH_SHORT).show();
        }
    }
}