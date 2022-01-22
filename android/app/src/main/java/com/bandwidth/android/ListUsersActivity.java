package com.bandwidth.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Person;

import java.util.ArrayList;

public class ListUsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Adapter adapter;

    ArrayList<String> users;
    ArrayList<String> deviceIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        users = new ArrayList<String>();
        deviceIds = new ArrayList<String>();

        setContentView(R.layout.activity_list_users);

        // Setting the layout as linear
        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new Adapter(ListUsersActivity.this, users, deviceIds);
        recyclerView.setAdapter(adapter);

        BWLibrary.configureAmplify(getApplicationContext());

        try {
            showUsers();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error attempting to show users" + e.getMessage());
        }

    }

    private void showUsers() {
        // Query the Amplify Datastore for users
        Amplify.DataStore.query(Person.class,
                persons -> {
                    while (persons.hasNext()) {
                        Person p = persons.next();

                        System.out.println("==== Persons ====");
                        System.out.println("Name: " + p.getFirstName());

                        // TODO ignore current user
                        String fullName = p.getFirstName() + " " + p.getLastName();
                        users.add(fullName);
                        deviceIds.add(p.getClientId());
                    }
                    this.runOnUiThread(() -> updateView());
                },
                failure -> System.out.println("Could not query DataStore" + failure)
        );
    }

    private void updateView() {
        System.out.println("in updateView");
        adapter.notifyDataSetChanged();
    }

    public void callUser(String deviceId) {
        System.out.println("callUser called for device:" + deviceId);
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("calleeId", deviceId);
        startActivity(i);
    }
}