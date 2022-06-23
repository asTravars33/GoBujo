package com.wangjessica.jwfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    // Layout components
    ImageView pfpImg;
    EditText nameField;

    // Firebase info
    FirebaseAuth auth;
    String uId;
    DatabaseReference userRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Instantiate layout components
        pfpImg = findViewById(R.id.profile_img);
        nameField = findViewById(R.id.name_field);

        // Instantiate firebase info
        auth = FirebaseAuth.getInstance();
        uId = auth.getCurrentUser().getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users").child(uId);
        storageRef = FirebaseStorage.getInstance().getReference().child("Profiles");

        // Fill in the fields
        initializeInfo(true);
    }

    public void changePfp(View view) {
        Intent choosePicIntent = new Intent();
        choosePicIntent.setAction(Intent.ACTION_GET_CONTENT);
        choosePicIntent.setType("image/*");
        startActivityForResult(choosePicIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            // Store the profile image in Firebase
            StorageReference filePath = storageRef.child(uId + ".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    System.out.println("Image was uploaded!");
                    downloadAndDisplay(uId+".jpg");
                }
            });
        }
    }
    public void downloadAndDisplay(String path){
        // Download the byte array from the path
        StorageReference imgRef = storageRef.child(path);
        imgRef.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the byte array to an image view to display
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                pfpImg.setImageBitmap(bitmap);
            }
        });
    }

    public void initializeInfo(boolean changeName){
        // Put in all the user's information
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("Name") && changeName) {
                    String name = ((DataSnapshot) snapshot.child("Name")).getValue().toString();
                    nameField.setText(name);
                }
                // Download the image
                if(storageRef.child(uId+".jpg")!=null){
                    downloadAndDisplay(uId+".jpg");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void saveUpdates(View view) {
        String newName = nameField.getText().toString();
        System.out.println("Setting name: "+newName);
        HashMap<String, Object> updateInfo = new HashMap<String, Object>();
            updateInfo.put("Name", newName);
        System.out.println(userRef.child(uId));
        userRef.child("Name").setValue(newName); //updateChildren(updateInfo);
        initializeInfo(true);
    }
}