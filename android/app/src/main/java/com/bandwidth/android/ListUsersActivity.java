package com.bandwidth.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Person;

import java.util.ArrayList;
import java.util.Arrays;

public class ListUsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    ArrayList<String> users = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        try {
            System.out.println("ListUsersActivity: Initialized Amplify with cloud sync");

            // Initialize Amplify plugins
            try {
                Amplify.addPlugin(new AWSCognitoAuthPlugin());
                Amplify.addPlugin(new AWSApiPlugin());
                Amplify.addPlugin(new AWSDataStorePlugin());

                Amplify.configure(getApplicationContext());
            } catch (Exception e) {
                // ignore exception if amplify already configured
                System.out.println("EXCEPTION:" + e.getMessage());
            }

            // Query the Amplify Datastore for users
            Amplify.DataStore.observe(Person.class,
                    started -> System.out.println("Observation began."),
                    change -> System.out.println(change.item().toString()),
                    failure -> System.out.println("Observation failed." + failure),
                    () -> System.out.println("Observation complete.")
            );

            Amplify.DataStore.query(Person.class,
                    persons -> {
                        while (persons.hasNext()) {
                            Person p = persons.next();

                            System.out.println("==== Persons ====");
                            System.out.println("Name: " + p.getFirstName());

                            String fullName = p.getFirstName() + " " + p.getLastName();
                            users.add(fullName);

                        }
                    },
                    failure -> System.out.println("Could not query DataStore" + failure)
            );

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error querying datastore with Amplify" + e.getMessage());
        }

        // Get list of users from Amplify Datastore

        // Getting reference of recyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // Setting the layout as linear
        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Sending reference and data to Adapter
        Adapter adapter = new Adapter(ListUsersActivity.this, users);

        // Setting Adapter to RecyclerView
        recyclerView.setAdapter(adapter);

    }

    public void callUser() {
        System.out.println("callUser called");
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}