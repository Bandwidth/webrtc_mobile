package com.bandwidth.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.amplifyframework.core.Action;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Consumer;
import com.amplifyframework.core.async.Cancelable;
import com.amplifyframework.core.model.query.ObserveQueryOptions;
import com.amplifyframework.core.model.query.QuerySortBy;
import com.amplifyframework.core.model.query.QuerySortOrder;
import com.amplifyframework.datastore.DataStoreException;
import com.amplifyframework.datastore.DataStoreQuerySnapshot;
import com.amplifyframework.datastore.generated.model.Person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class ListUsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Adapter adapter;

    ArrayList<String> users;
    HashSet<String> processedUserIds = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        users = new ArrayList<String>();

        setContentView(R.layout.activity_list_users);

        // Setting the layout as linear
        // layout for vertical orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new Adapter(ListUsersActivity.this, users);
        recyclerView.setAdapter(adapter);

        BWLibrary.configureAmplify(getApplicationContext());

        try {
            showUsersRealTime();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error attempting to show users" + e.getMessage());
        }

    }

    private void showUsersRealTime() {
        String myTag = "ObserveQuery";

        try {
            // This is basically a subscription; gets a list of users (Person records)
            // locally available and the subscription is for subsequent updates to the query
            // A new user add will also trigger this call
            Consumer<DataStoreQuerySnapshot<Person>> onQuerySnapshot = value -> {
                System.out.println(myTag + ": success on snapshot: " + value.getItems().size());
                System.out.println(myTag + ": sync status: " + value.getIsSynced());
                System.out.println(myTag + ": items: " + value.getItems().toString());

                for(Person p : value.getItems()) {
                    if(processedUserIds.contains(p.getId())) {
                        continue;
                    }

                    processedUserIds.add(p.getId());

                    // We create a concatenated string of name and deviceId of a user
                    // When displaying them (via Adapter.java using Recycler view), we split them
                    // out and use name for display and deviceId as a tag for each item
                    // We do this since we require the deviceId to be passed to the backend for
                    // calling purposes
                    // TODO There probably is a much better way to do this...
                    String fullName = p.getFirstName() + " " + p.getLastName() + "||" + p.getClientId();
                    users.add(fullName);
                }

                Collections.sort(users);
                this.runOnUiThread(() -> updateView());
            };

            // What to do when observation for query has started
            Consumer<Cancelable> observationStarted = value -> {
                System.out.println(myTag + ": success on cancelable");
            };

            // What to do when there is an error with observation for query
            Consumer<DataStoreException> onObservationError = value -> {
                System.out.println(myTag + ": error on snapshot$value");
            };

            // What to do when observation for query has completed
            Action onObservationComplete = () -> {
                System.out.println(myTag + ": complete");
            };

            // Queue observation of our query

            // Create a list of query options (predicates, sort by, etc)

            // Sort By
            QuerySortBy querySortBy = new QuerySortBy("person", "firstname", QuerySortOrder.ASCENDING);
            List<QuerySortBy> sortByList = new ArrayList<QuerySortBy>();
            sortByList.add(querySortBy);

            ObserveQueryOptions opts = new ObserveQueryOptions(null, sortByList);

            // Query to observe
            Amplify.DataStore.<Person>observeQuery(
                    Person.class,
                    opts,
                    observationStarted,
                    onQuerySnapshot,
                    onObservationError,
                    onObservationComplete
            );
        } catch (Exception e) {
            System.out.println(myTag + ": exception in observe query: " + e.getMessage() );
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