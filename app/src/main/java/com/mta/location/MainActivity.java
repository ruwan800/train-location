package com.mta.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.crashlytics.android.Crashlytics;
import com.mta.location.util.TrainUpdater;
import com.mta.location.util.TrainUpdaterSimulator;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private MapFragment mapFragment;
    private int selectedLineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer_layout);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        Fabric.with(this, new Crashlytics());

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.live_map: {
                                if(mapFragment != null) {
                                    mapFragment.setTrainUpdater(new TrainUpdater());
                                    if (0 < selectedLineId) {
                                        mapFragment.setLineId(selectedLineId);
                                    }
                                }
                                break;
                            }
                            case R.id.simulate: {
                                if(mapFragment != null) {
                                    selectedLineId = mapFragment.getLineId();
                                    mapFragment.setLineId(3);
                                    mapFragment.setTrainUpdater(new TrainUpdaterSimulator());
                                }
                                break;
                            }
                            case R.id.settings: {
                                //TODO goto settings activity
                            }
                            default: break;
                        }

                        return true;
                    }
                });

        startLocationUpdates();

        View headerView = navigationView.getHeaderView(0);
        Switch trackSwitch = headerView.findViewById(R.id.trackSwitch);
        trackSwitch.setChecked(true);
        trackSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    startLocationUpdates();
                } else {
                    Location.getInstance(MainActivity.this).stopLocationUpdates(MainActivity.this);
                }
            }
        });

        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            mapFragment = new MapFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            mapFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mapFragment).commit();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LocationUpdateJob.scheduleJob(this);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (0 < grantResults.length && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LocationUpdateJob.scheduleJob(this);
            }
        }
    }
}
