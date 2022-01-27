package com.bandwidth.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

// Extends the Adapter class to RecyclerView.Adapter
// and implement the unimplemented methods
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    ArrayList users;
    Context context;

    // Constructor for initialization
    public Adapter(Context context, ArrayList users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflating the Layout(Instantiates list_item.xml
        // layout file into View object)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        // Passing view to ViewHolder
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Binding data to the into specified position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // deviceId of user
                String deviceId = (String)holder.itemView.getTag();
                System.out.println("Clicked " + view.toString() + ":::" + deviceId);
                ((ListUsersActivity)context).callUser(deviceId);
            }
        });

        // Split the name and deviceId from the concatenated string
        // set the deviceId as a tag on each user displayed
        // we will need the deviceId when a user is clicked to pass to our backend
        String namePlusId = (String)users.get(position);
        String[] nameParts = namePlusId.split("\\|\\|");

        String name = nameParts[0];
        String deviceId = nameParts[1];

        holder.text.setText(name);
        holder.itemView.setTag(deviceId);
    }

    @Override
    public int getItemCount() {
        // Returns number of items
        // currently available in Adapter
        return users.size();
    }

    // Initializing the Views
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public ViewHolder(View view) {
            super(view);
            text = (TextView) view.findViewById(R.id.users);
        }
    }
}
