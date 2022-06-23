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

public class LoginActivity extends AppCompatActivity {

    // Firebase info
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    DatabaseReference rootRef;

    // Layout components
    private Button loginButton;
    private EditText userEmail, userPassword;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        initializeFields();
        rootRef = FirebaseDatabase.getInstance().getReference();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });
    }

    private void loginUser() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if(email==null || email.equals("") || password==null || password.equals("")) return;
        loadingBar.setTitle("Logging In");
        loadingBar.setMessage("Logging In...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Add the user to firebase
                String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap<String, Object> info = new HashMap<String, Object>();
                    info.put("Name", "User");
                rootRef.child("Users").child(uId).updateChildren(info);
                // Direct the user to main page
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void initializeFields() {
        loginButton = findViewById(R.id.login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        registerLink = findViewById(R.id.register_link);
        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}