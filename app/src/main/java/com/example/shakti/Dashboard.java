package com.example.shakti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shakti.R;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Dashboard extends AppCompatActivity {

    CardView helplines;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    ImageView menuIcon;
    FirebaseAuth auth;
    Button logout;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        helplines = findViewById(R.id.helpline);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menu_icon);


        helplines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Helplines.class);
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

        // Handle Navigation Item Clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_helpline) {
                    Intent intent = new Intent(Dashboard.this, Helplines.class);
                    startActivity(intent);
                } else if (id == R.id.nav_add_friends) {
                    Toast.makeText(Dashboard.this, "Add Friends Selected", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_send_sms) {
                    Toast.makeText(Dashboard.this, "Send SMS Selected", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_track_me) {
                    Toast.makeText(Dashboard.this, "Track Me Selected", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {
                    auth = FirebaseAuth.getInstance();
                    logout = findViewById(R.id.nav_logout);
                    user = auth.getCurrentUser();
                    if (user == null){
                        Intent intent = new Intent(Dashboard.this, LoginPage.class);
                        startActivity(intent);
                        finish();
                    }

                    logout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(Dashboard.this, LoginPage.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
                drawerLayout.closeDrawer(GravityCompat.START);
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
}