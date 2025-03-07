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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.ItemTouchHelper;

public class AddFriends extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelplineAdapter adapter;
    private List<Item> helplineList;
    private List<Map<String, Object>> friendsList;  // Cached Firestore list
    private ImageView backButton;
    private ImageButton addFriend;
    private TextView noEntriesText;
    private FirebaseFirestore db;
    private String userId;

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
        friendsList = new ArrayList<>();
        adapter = new HelplineAdapter(this, helplineList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadFriendsFromFirestore();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        enableSwipeToDelete();
        backButton.setOnClickListener(view -> onBackPressed());
        addFriend.setOnClickListener(view -> showAddFriendDialog());
    }

    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                deleteFriend(viewHolder.getAdapterPosition());
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void deleteFriend(int position) {
        if (position < 0 || position >= friendsList.size()) return;

        friendsList.remove(position);
        db.collection("users").document(userId).update("friends", friendsList).addOnSuccessListener(aVoid -> {
            if (!isDestroyed()) {
                helplineList.remove(position);
                adapter.notifyItemRemoved(position);
                updateNoEntriesText();
                Toast.makeText(this, "Friend deleted", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (!isDestroyed()) {
                Toast.makeText(this, "Error deleting friend: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadFriendsFromFirestore() {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (!isDestroyed()) {
                helplineList.clear();
                friendsList.clear();

                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> retrievedFriends = (List<Map<String, Object>>) documentSnapshot.get("friends");
                    if (retrievedFriends != null) {
                        friendsList.addAll(retrievedFriends);
                        for (Map<String, Object> friend : friendsList) {
                            helplineList.add(new Item((String) friend.get("name"), (String) friend.get("phone")));
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                updateNoEntriesText();
            }
        }).addOnFailureListener(e -> {
            if (!isDestroyed()) {
                Toast.makeText(this, "Error loading friends: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_friends_form, null);
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
                return;
            }

            if (!phone.matches("\\d{10}")) {
                Toast.makeText(this, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prevent duplicate entries
            for (Map<String, Object> existingFriend : friendsList) {
                if (existingFriend.get("phone").equals(phone)) {
                    Toast.makeText(this, "This phone number is already in the list", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Map<String, Object> newFriend = new HashMap<>();
            newFriend.put("name", name);
            newFriend.put("phone", phone);

            friendsList.add(newFriend);
            db.collection("users").document(userId).update("friends", friendsList).addOnSuccessListener(aVoid -> {
                if (!isDestroyed()) {
                    helplineList.add(new Item(name, phone));
                    adapter.notifyItemInserted(helplineList.size() - 1);
                    recyclerView.smoothScrollToPosition(helplineList.size() - 1);
                    updateNoEntriesText();
                    Toast.makeText(this, "Friend Added", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }).addOnFailureListener(e -> {
                if (!isDestroyed()) {
                    Toast.makeText(this, "Error adding friend: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void updateNoEntriesText() {
        noEntriesText.setVisibility(helplineList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
