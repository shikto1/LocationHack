package com.example.shishir.locationhack.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shishir.locationhack.Model_Class.LatLongClass;
import com.example.shishir.locationhack.Model_Class.MyLocation;
import com.example.shishir.locationhack.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SeeLocationActivity extends AppCompatActivity {

    private ListView dateListView;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> dateList = new ArrayList<>();
    private ArrayList<MyLocation> locationList = new ArrayList<>();
    private String uiId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_location);
        mAuth = FirebaseAuth.getInstance();

        findViewById();
    }

    private void findViewById() {
        dateListView = (ListView) findViewById(R.id.dateListView);

        setUpAdapter();
    }

    private void setUpAdapter() {
        uiId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(uiId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot d : dataSnapshot.getChildren()) {
                    dateList.add(d.getKey());
                    databaseReference.child(d.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dd : dataSnapshot.getChildren()) {
                                MyLocation myLocation = new MyLocation(d.getKey(), dd.getKey(), dd.child("Lat").getValue(String.class), dd.child("Long").getValue(String.class));
                                locationList.add(myLocation);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                adapter = new ArrayAdapter<String>(SeeLocationActivity.this, android.R.layout.simple_list_item_1, dateList);
                dateListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        dateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SeeLocationActivity.this, MapsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("date", dateList.get(position));
                intent.putExtra("locList", locationList);
                startActivity(intent);
            }
        });
        super.onStart();
    }
}
