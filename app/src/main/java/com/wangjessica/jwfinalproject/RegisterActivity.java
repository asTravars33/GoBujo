package com.wangjessica.jwfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // Layout components
    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView loginLink;

    // Firebase info
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(email==null || email.equals("") || password==null || password.equals("")) return;

        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Creating Account...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Add the user to Firebase
                String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap<String, Object> info = new HashMap<String, Object>();
                    info.put("Name", "User");
                rootRef.child("Users").child(uId).updateChildren(info);
                // Direct user to main page
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeFields() {
        createAccountButton = findViewById(R.id.register_button);
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        loginLink = findViewById(R.id.login_link);
        loadingBar = new ProgressDialog(this);
    }
}