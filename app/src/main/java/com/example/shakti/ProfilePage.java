package com.example.shakti;

import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfilePage extends AppCompatActivity {

    ImageButton back, btnEdit;
    Button save;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private Uri imageUri;
    private ShapeableImageView imgProfile;

    EditText username, email, phone, dob, address;
    Calendar calendar;
    FirebaseFirestore fstore;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String userID;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        back = findViewById(R.id.btnBack);
        save = findViewById(R.id.btn_save);
        btnEdit = findViewById(R.id.btnEdit);
        imgProfile = findViewById(R.id.imgProfile);
        username = findViewById(R.id.etUsername);
        email = findViewById(R.id.etEmail);
        phone = findViewById(R.id.etPhone);
        dob = findViewById(R.id.etDob);
        address = findViewById(R.id.etAddress);

        calendar = Calendar.getInstance();

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUid();
            loadUserProfile();  // Fetch user profile data
        }

        imgProfile.setOnClickListener(view -> showFullImageDialog());
        dob.setOnClickListener(v -> showDatePickerDialog());
        save.setOnClickListener(view -> saveUserProfile());
        back.setOnClickListener(view -> onBackPressed());
        btnEdit.setOnClickListener(view -> showImagePickerDialog());
    }

    // Load user profile data from Firestore
    private void loadUserProfile() {
        DocumentReference docRef = fstore.collection("users").document(userID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    username.setText(document.getString("username"));
                    email.setText(document.getString("email"));
                    phone.setText(document.getString("phone"));
                    dob.setText(document.getString("dob"));
                    address.setText(document.getString("address"));
                    String imageUrl = document.getString("profileImageUrl");
                    if (imageUrl != null) {
                        imgProfile.setImageURI(Uri.parse(imageUrl)); // Load image if exists
                    }
                }
            }
        });
    }

    // Save user profile data to Firestore
    private void saveUserProfile() {
        String user_name = username.getText().toString().trim();
        String user_email = email.getText().toString().trim();
        String user_phone = phone.getText().toString().trim();
        String user_dob = dob.getText().toString().trim();
        String user_address = address.getText().toString().trim();

        Map<String, Object> user = new HashMap<>();
        user.put("username", user_name);
        user.put("email", user_email);
        user.put("phone", user_phone);
        user.put("dob", user_dob);
        user.put("address", user_address);

        DocumentReference documentReference = fstore.collection("users").document(userID);
        documentReference.set(user).addOnSuccessListener(aVoid ->
                Toast.makeText(ProfilePage.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(ProfilePage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );

        if (imageUri != null) {
            uploadImageToStorage();
        }
    }

    // Upload profile image to Firebase Storage
    private void uploadImageToStorage() {
        StorageReference fileRef = storageReference.child("profile_images/" + userID + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    fstore.collection("users").document(userID)
                            .update("profileImageUrl", downloadUrl);
                })
        ).addOnFailureListener(e ->
                Toast.makeText(ProfilePage.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show()
        );
    }

    private void showFullImageDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_image);

        ImageView fullImageView = dialog.findViewById(R.id.fullImageView);
        Drawable drawable = imgProfile.getDrawable();
        fullImageView.setImageDrawable(drawable);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) ->
                dob.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear), year, month, day);

        datePickerDialog.show();
    }

    private void showImagePickerDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"},
                (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                });
        builder.show();
    }

    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }
}
