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
        helplineList.add(new Item("Police, Fire, Rescue", "112"));
        helplineList.add(new Item("Women Helpline", "1091"));
        helplineList.add(new Item("Child Helpline", "1098"));
        helplineList.add(new Item("Nari Sammata Manch helpline", "020-24473116"));
        helplineList.add(new Item("National Commission for Women Helpline", "7827170170"));

        adapter = new HelplineAdapter(this, helplineList);
        recyclerView.setAdapter(adapter);
    }
}