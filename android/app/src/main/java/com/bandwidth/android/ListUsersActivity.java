package com.bandwidth.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.AuthSession;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Person;
import com.bandwidth.android.ui.login.LoginActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class ListUsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Adapter adapter;

    ArrayList<String> users = new ArrayList<String>();
    ArrayList<String> deviceIds = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            System.out.println("ListUsersActivity");

//            Bundle extras = getIntent().getExtras();
//            if(extras != null && extras.getString("redirectFromLogin").equals("1")) {
//                showUsers();
//
//            } else {
//
//                System.out.println("Attempting to fetch session");
//                Amplify.Auth.fetchAuthSession(
//                        result -> handleAuthResp(getApplicationContext(), result),
//                        error -> System.out.println("Error fetching Auth Session: " + error.toString())
//                );
//            }

            showUsers();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error querying datastore with Amplify" + e.getMessage());
        }

    }

    private void showUsers() {
        try {
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
                        updateView();

                    },
                    failure -> System.out.println("Could not query DataStore" + failure)
            );
        } catch (Exception e) {
            System.out.println("EXCEPTION HERE: " + e.getMessage());
        }
    }

    private void handleAuthResp(Context context, AuthSession result) {
        System.out.println("handleAuthResp(): " + result.toString());

        if(result.isSignedIn()) {
            System.out.println("isSignedIn!!!");
//            showUsers();

        } else {
            LoginActivity.isSignedIn(context, result);
        }
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