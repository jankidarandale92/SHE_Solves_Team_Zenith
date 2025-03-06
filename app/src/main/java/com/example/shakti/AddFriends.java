package com.example.shakti;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AddFriends extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelplineAdapter adapter;
    private List<Item> helplineList;
    ImageView backButton;
    private ImageButton addFriend;
    TextView noEntriesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backButton = findViewById(R.id.back_button);
        addFriend = findViewById(R.id.add_friend);
        noEntriesText = findViewById(R.id.no_entries_text);

        helplineList = new ArrayList<>();
//        helplineList.add(new Item("Police", "100"));
//        helplineList.add(new Item("Hospital", "102"));
//        helplineList.add(new Item("Police, Fire, Rescue", "112"));
//        helplineList.add(new Item("Women Helpline", "1091"));
//        helplineList.add(new Item("Child Helpline", "1098"));
//        helplineList.add(new Item("Nari Sammata Manch helpline", "020-24473116"));
//        helplineList.add(new Item("National Commission for Women Helpline", "7827170170"));

        adapter = new HelplineAdapter(this, helplineList);
        recyclerView.setAdapter(adapter);

        updateNoEntriesText();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        addFriend.setOnClickListener(view -> showAddFriendDialog());
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_friends_form, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextPhone = dialogView.findViewById(R.id.editTextPhone);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter both name and phone number", Toast.LENGTH_SHORT).show();
            } else {
                helplineList.add(new Item(name, phone));
                adapter.notifyItemInserted(helplineList.size() - 1);
                recyclerView.smoothScrollToPosition(helplineList.size() - 1);

                // Update visibility
                updateNoEntriesText();

                Toast.makeText(this, "Friend Added: " + name + " (" + phone + ")", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    // Function to update visibility
    private void updateNoEntriesText() {
        if (helplineList.isEmpty()) {
            noEntriesText.setVisibility(View.VISIBLE);
        } else {
            noEntriesText.setVisibility(View.GONE);
        }
    }
}
