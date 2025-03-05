package com.example.shakti;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class Helplines extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HelplineAdapter adapter;
    private List<Item> helplineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helplines);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        helplineList = new ArrayList<>();
        helplineList.add(new Item("Police", "100"));
        helplineList.add(new Item("Hospital", "102"));
        helplineList.add(new Item("Women Helpline", "1091"));

        adapter = new HelplineAdapter(this, helplineList);
        recyclerView.setAdapter(adapter);
    }
}