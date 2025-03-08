package com.example.shakti;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    //  Declaration of all the Required Variables
    private final List<Place> places;
    private final OnDirectionClickListener listener;

    public interface OnDirectionClickListener {
        void onDirectionClick(double lat, double lon);
    }

    public PlacesAdapter(List<Place> places, OnDirectionClickListener listener) {
        this.places = places;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = places.get(position);
        holder.name.setText(place.getName());
        holder.directionButton.setOnClickListener(v -> listener.onDirectionClick(place.getLatitude(), place.getLongitude()));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        Button directionButton;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.place_name);
            directionButton = itemView.findViewById(R.id.btn_direction);
        }
    }
}