package com.example.shakti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import com.example.shakti.R;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class Dashboard extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        helplines = findViewById(R.id.helpline);
        add_friends = findViewById(R.id.add_friends);
        send_sms = findViewById(R.id.send_sms);
        track_me = findViewById(R.id.track_me);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menu_icon);
        profile_icon = findViewById(R.id.profile_icon);


        helplines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Helplines.class);
                startActivity(intent);
            }
        });

        add_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, AddFriends.class);
                startActivity(intent);
            }
        });

        send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSendSmsDialog();
            }
        });

        track_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, TrackMe.class);
                startActivity(intent);
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
                    Intent intent = new Intent(Dashboard.this, TrackMe.class);
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

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
}