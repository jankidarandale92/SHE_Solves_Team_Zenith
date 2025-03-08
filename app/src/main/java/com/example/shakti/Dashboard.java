package com.example.shakti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.media.MediaRecorder;
import android.net.Uri;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Dashboard extends AppCompatActivity {

    //  Declaration of all the Required Variables
    CardView helplines;
    CardView add_friends;
    CardView send_sms;
    CardView track_me;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    ImageView menuIcon;
    FirebaseAuth auth;
    Button logout;
    FirebaseUser user;
    ImageView profile_icon;
    ImageView sos;
    private DatabaseReference databaseReference;

    private List<String> contactList = new ArrayList<>();

    private static final int CALL_PERMISSION_REQUEST = 1;
    private static final int SMS_PERMISSION_REQUEST = 2;
    private static final int RECORD_PERMISSION_REQUEST = 3;

    private MediaRecorder mediaRecorder;
    private FirebaseFirestore db;
    private File audioFile;
    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        //  Initialization of all the Required Variables
        auth = FirebaseAuth.getInstance();

        helplines = findViewById(R.id.helpline);
        add_friends = findViewById(R.id.add_friends);
        send_sms = findViewById(R.id.send_sms);
        track_me = findViewById(R.id.track_me);
        username = findViewById(R.id.user_name);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menu_icon);
        profile_icon = findViewById(R.id.profile_icon);
        sos = findViewById(R.id.sos_button);
        databaseReference = FirebaseDatabase.getInstance().getReference("add_friends");

        //  Fetch Username from Firebase
        fetchUsername();

        // Fetch contacts from Firebase
        fetchContacts();

        // Click Listener for SOS Image
        sos.setOnClickListener(v -> handleSOS());

        //  Helplines
        helplines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Helplines.class);
                startActivity(intent);
            }
        });

        //  Add Friends
        add_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, AddFriends.class);
                startActivity(intent);
            }
        });

        //  Send SMS
        send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSendSmsDialog();
            }
        });

        track_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLocationViaSMS();
            }
        });

        // Open Drawer when Icon is Clicked
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        profile_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, ProfilePage.class);
                startActivity(intent);
            }
        });

        //  Handling Menus
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_helpline) {
                    Intent intent = new Intent(Dashboard.this, Helplines.class);
                    startActivity(intent);
                } else if (id == R.id.nav_add_friends) {
                    Intent intent = new Intent(Dashboard.this, AddFriends.class);
                    startActivity(intent);
                } else if (id == R.id.nav_send_sms) {
                    showSendSmsDialog();
                } else if (id == R.id.nav_track_me) {
                    sendLocationViaSMS();
                } else if (id == R.id.nav_nearby_safe_places) {
                    Intent intent = new Intent(Dashboard.this, NearbyPlaces.class);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {  // Check if the user is logged in before signing out
                        auth.signOut();
                        Intent intent = new Intent(Dashboard.this, LoginPage.class);
                        startActivity(intent);
                        finish();  // Close Dashboard activity
                    }
                }

                drawerLayout.closeDrawer(GravityCompat.START);  // Close drawer after selection
                return true;
            }
        });
    }

    //  Function to fetch Contacts
    private void fetchContacts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String phoneNumber = dataSnapshot.getValue(String.class);
                    if (phoneNumber != null) {
                        contactList.add(phoneNumber);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dashboard.this, "Failed to fetch contacts!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  Function to fetch Username
    private void fetchUsername() {
        FirebaseUser user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            // Add a real-time listener
            userRef.addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    Log.e("Firestore", "Error listening to username updates", error);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("username");
                    Log.d("DEBUG", "Real-time username update: " + name);

                    if (name != null && !name.isEmpty()) {
                        username.setText("Hi, " + name);
                    } else {
                        username.setText("Hi, User");
                    }
                } else {
                    Log.e("DEBUG", "No user document found");
                    username.setText("Hi, User");
                }
            });
        } else {
            Log.e("DEBUG", "User is null");
            username.setText("Hi, User");
        }
    }

    //  Function to navigate back
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // ------------------- Handling the Send SMS Functionality -------------------
    private void showSendSmsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_send_sms, null);
        builder.setView(dialogView);

        // Find views in dialog
        EditText message = dialogView.findViewById(R.id.et_message);
        Button sendButton = dialogView.findViewById(R.id.btn_send);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.create();
        dialog.show();

        sendButton.setOnClickListener(v -> {
            String msg = message.getText().toString().trim();

            if (msg.isEmpty()) {
                Toast.makeText(Dashboard.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            sendSMSToAllContacts(msg);  // Call function to send SMS to all numbers
            dialog.dismiss();  // Close the dialog
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Close dialog when cancel is clicked
    }

    private void sendSMSToAllContacts(String message) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(user.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> friendsList = (List<Map<String, Object>>) documentSnapshot.get("friends");

                if (friendsList != null && !friendsList.isEmpty()) {
                    boolean atLeastOneSMSsent = false;

                    for (Map<String, Object> friend : friendsList) {
                        String phoneNumber = (String) friend.get("phone");
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            sendSMS(phoneNumber, message);
                            atLeastOneSMSsent = true;
                        }
                    }

                    if (atLeastOneSMSsent) {
                        Toast.makeText(this, "SMS sent to all contacts", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No valid phone numbers found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No contacts exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch contacts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void sendSMS(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(Dashboard.this, "SMS Sent", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(Dashboard.this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }

    // ------------------- Handling the Track Me Functionality -------------------
    private void sendLocationViaSMS() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String locationUrl = "Track Me... My location: https://www.google.com/maps?q=" + latitude + "," + longitude;

                // Send location via SMS
                sendSMSToAllContacts(locationUrl);
            } else {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // ------------------- Handling the SOS Activation -------------------
    private void handleSOS() {
        startAudioRecording();
        sendSMSToAllContacts("Emergency!!! HELP ME....");
        sendLocationViaSMS();
        callEmergencyNumber("7447776999");
    }

    private void callEmergencyNumber(String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST);
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }


//    ------------------- Handling the Audio Recording -------------------

    private void startAudioRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, RECORD_PERMISSION_REQUEST);
            return;
        }

        // Start recording if permission is already granted
        startRecording();
    }

    // Handle user's response to permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RECORD_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permission granted! Starting recording...", Toast.LENGTH_SHORT).show();
                startRecording();
            } else {
                Toast.makeText(this, "❌ Permission denied! Cannot record audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Function to start recording audio and save in Internal Storage
    private void startRecording() {
        try {
            // Path to save the file in "Documents" folder of Internal Storage
            File documentsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!documentsFolder.exists()) {
                documentsFolder.mkdirs();  // Create Documents folder if it doesn't exist
            }

            // Create file name
            String fileName = "sos_audio_" + System.currentTimeMillis() + ".3gp";
            audioFile = new File(documentsFolder, fileName);

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.prepare();
            mediaRecorder.start();

            Toast.makeText(this, "🎤 Recording started... Saving in Documents!", Toast.LENGTH_SHORT).show();

            // Automatically stop recording after 10 seconds
            new Handler().postDelayed(this::stopAudioRecording, 10000);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Recording failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Function to stop recording
    private void stopAudioRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Toast.makeText(this, "✅ Recording saved in Documents!", Toast.LENGTH_SHORT).show();
        }
    }
}

