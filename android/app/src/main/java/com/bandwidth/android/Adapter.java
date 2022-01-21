package com.bandwidth.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

// Extends the Adapter class to RecyclerView.Adapter
// and implement the unimplemented methods
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    ArrayList users;
    ArrayList deviceIds;
    Context context;

    // Constructor for initialization
    public Adapter(Context context, ArrayList users, ArrayList deviceIds) {
        this.context = context;
        this.users = users;
        this.deviceIds = deviceIds;
    }

//    @NonNull
    @Override
//    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflating the Layout(Instantiates list_item.xml
        // layout file into View object)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        // Passing view to ViewHolder
//        Adapter.ViewHolder viewHolder = new Adapter.ViewHolder(view);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Binding data to the into specified position
    @Override
//    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // deviceId of user
                String deviceId = (String)holder.itemView.getTag();
                System.out.println("CLICKED " + view.toString() + ":::" + deviceId);
//                holder.text.setText((String) "CLICKED");
                ((ListUsersActivity)context).callUser(deviceId);
            }
        });

        // TypeCast Object
        holder.text.setText((String) users.get(position));
        holder.itemView.setTag(deviceIds.get((position)));
    }

    @Override
    public int getItemCount() {
        // Returns number of items
        // currently available in Adapter
        return users.size();
    }

    // Initializing the Views
    public class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView images;
        TextView text;

        public ViewHolder(View view) {
            super(view);
            text = (TextView) view.findViewById(R.id.users);
        }
    }
}
