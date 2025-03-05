package com.example.shakti;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUp extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameEditText = findViewById(R.id.username_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edittext);
        signupButton = findViewById(R.id.btn_signup);

        Button signup = findViewById(R.id.btn_signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    // If all conditions are met, navigate to HomeActivity
                    Intent intent = new Intent(SignUp.this, Dashboard.class);
                    startActivity(intent);
                    finish(); // Close SignUp Activity
                }
            }
        });
    }

    private boolean validateInputs() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (username.isEmpty()) {
            showToast("Username cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            showToast("Password cannot be empty");
            return false;
        }
        if (password.length() < 6) {
            showToast("Password must be at least 6 characters long");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}