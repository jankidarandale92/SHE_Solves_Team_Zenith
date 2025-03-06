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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shakti.R;

import com.google.android.material.navigation.NavigationView;

public class Dashboard extends AppCompatActivity {

    CardView helplines;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    ImageView menuIcon;

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
                    Toast.makeText(Dashboard.this, "Logging Out...", Toast.LENGTH_SHORT).show();
                    finish(); // Close the app
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