package com.example.societyfy.Activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.societyfy.Activities.Adapters.UserAdapter;
import com.example.societyfy.Activities.Fragments.HelpFragment;
import com.example.societyfy.Activities.Fragments.HomeFragment;
import com.example.societyfy.Activities.Fragments.ProfileFragment;
import com.example.societyfy.Activities.Fragments.SettingsFragment;
import com.example.societyfy.Activities.Fragments.UserListFragment;
import com.example.societyfy.Activities.Services.LocationService;
import com.example.societyfy.Activities.models.User;
import com.example.societyfy.Activities.models.UserLocation;
import com.example.societyfy.Activities.models.UserRepo;
import com.example.societyfy.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.societyfy.Activities.Constants.ERROR_DIALOG_REQUEST;
import static com.example.societyfy.Activities.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.societyfy.Activities.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String CURRENT_USER_KEY = "CURRENT_USER_KEY";
    private static final String CURRENT_USER_NAME = "CURRENT_USER_NAME";
    private static final String CURRENT_USER_IMAGE = "CURRENT_USER_IMAGE";

    public FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction;
    Fragment fragment;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    public DrawerLayout drawer;
    FirebaseFirestore mDb;
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private UserRepo userRepo;
    public Toolbar toolbar;
    public NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        drawer = findViewById(R.id.drawer_layout);

        userRepo = new UserRepo(FirebaseFirestore.getInstance());
        mDb = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getUsers();

        if (firstStart) {

            login();
            getSupportActionBar().hide();
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        } else if (currentUser == null) {
            login();
            getSupportActionBar().hide();
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CURRENT_USER_KEY, currentUser.getUid());
            editor.putString(CURRENT_USER_NAME, currentUser.getDisplayName());
            editor.putString(CURRENT_USER_IMAGE, String.valueOf(currentUser.getPhotoUrl()));
            editor.apply();


            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle
                    (this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);


            updateNavHeader();

            getSupportActionBar().setTitle("Let's Societyfy");
            fragment = new HomeFragment();
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT > 11) {
            invalidateOptionsMenu();
            menu.findItem(R.id.study_users).setVisible(false);
            menu.findItem(R.id.chat_study).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.setting_menu) {

            getSupportActionBar().setTitle("Settings");
            fragment = new SettingsFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    private void login() {
        fragment = new LoginFragment();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("QUIT");
            builder.setMessage("Are you sure you want to quit the app?");

            builder.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    finishAffinity();
                }
            });

            builder.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });

            builder.create().show();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportActionBar().setTitle("Let's Societyfy");
            fragment = new HomeFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_profile) {
            getSupportActionBar().setTitle("Profile");
            fragment = new ProfileFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_settings) {
            getSupportActionBar().setTitle("Settings");
            fragment = new SettingsFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();


        } else if (id == R.id.nav_user_list) {
            getSupportActionBar().setTitle("Users' List");


            fragment = new UserListFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.intent_user_locations), mUserLocations);
            fragment.setArguments(bundle);

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();


        }  else if (id == R.id.nav_help) {
            getSupportActionBar().setTitle("Help");


            fragment = new HelpFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();


        } else if (id == R.id.nav_sign_out) {

            FirebaseAuth.getInstance().signOut();
            currentUser = null;
            fragment = new LoginFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().hide();
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    private void getUsers() {

        userRepo.getUsers(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("UserList", "Listen failed.", e);
                    return;
                }
                List<User> userList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    userList.add(new User(doc.getString("email"), doc.getString("image"), doc.getString("name"), doc.getString("user_id")));
                    User user = new User(doc.getString("email"), doc.getString("image"), doc.getString("name"), doc.getString("user_id"));
                    getUserLocation(user);
                }


            }
        });
    }

    private void getUserLocation(User user) {

        DocumentReference locationRef = mDb.collection("Users' Locations").document(user.getUser_id());

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (Objects.requireNonNull(task.getResult()).toObject(UserLocation.class) != null) {
                        mUserLocations.add(task.getResult().toObject(UserLocation.class));
                    }

                }

            }
        });

    }


    public void updateNavHeader() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.nav_username);
        TextView navmail = headerView.findViewById(R.id.nav_user_mail);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        navUserName.setText(currentUser.getDisplayName());
        navmail.setText(currentUser.getEmail());
        Glide.with(getApplicationContext()).load(currentUser.getPhotoUrl()).into(navUserPhoto);

    }

}
