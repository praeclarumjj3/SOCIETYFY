package com.example.societyfy.Activities.Fragments;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.societyfy.Activities.FoodFragment;
import com.example.societyfy.Activities.HangoutFragment;
import com.example.societyfy.Activities.MainActivity;
import com.example.societyfy.Activities.OtherFragment;
import com.example.societyfy.Activities.PlayFragment;
import com.example.societyfy.Activities.Services.LocationService;
import com.example.societyfy.Activities.StudyFragment;
import com.example.societyfy.Activities.UserClient;
import com.example.societyfy.Activities.models.FoodUser;
import com.example.societyfy.Activities.models.HangoutUser;
import com.example.societyfy.Activities.models.OtherUser;
import com.example.societyfy.Activities.models.PlayUser;
import com.example.societyfy.Activities.models.StudyUser;
import com.example.societyfy.Activities.models.User;
import com.example.societyfy.Activities.models.UserLocation;
import com.example.societyfy.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

import static com.example.societyfy.Activities.Constants.ERROR_DIALOG_REQUEST;
import static com.example.societyfy.Activities.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.societyfy.Activities.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "MainActivity";
    private CardView study;
    private CardView play;
    private CardView food;
    private CardView hangout;
    private CardView other;
    private FragmentTransaction fragmentTransaction;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private FirebaseFirestore mDb;
    ProgressBar service_pro;
    TextView note;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home2, container, false);

        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        study = v.findViewById(R.id.study);
        play = v.findViewById(R.id.play);
        food = v.findViewById(R.id.food);
        hangout = v.findViewById(R.id.hangout);
        other = v.findViewById(R.id.other);
        service_pro = v.findViewById(R.id.servicepro);
        note = v.findViewById(R.id.note);
        service_pro.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);

        study.setOnClickListener(this);
        play.setOnClickListener(this);
        food.setOnClickListener(this);
        hangout.setOnClickListener(this);
        other.setOnClickListener(this);


        return v;


    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.study:

                DocumentReference userRef1 = mDb.collection("Users").document(FirebaseAuth.getInstance().getUid());
                userRef1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User userS = Objects.requireNonNull(task.getResult()).toObject(User.class);

                            DocumentReference joinStudy = mDb
                                    .collection("Study")
                                    .document(FirebaseAuth.getInstance().getUid());

                            StudyUser studyUser = new StudyUser(userS.getEmail(), userS.getImage(), userS.getName(), userS.getUser_id());

                            joinStudy.set(studyUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "added");
                                    }
                                }
                            });
                        }
                    }
                });

                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.fragment, new StudyFragment());
                fragmentTransaction.commit();
                break;


            case R.id.play:

                DocumentReference userRef2 = mDb.collection("Users").document(FirebaseAuth.getInstance().getUid());
                userRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User userP = Objects.requireNonNull(task.getResult()).toObject(User.class);

                            DocumentReference joinPlay = mDb
                                    .collection("Play")
                                    .document(FirebaseAuth.getInstance().getUid());

                            PlayUser playUser = new PlayUser(userP.getEmail(), userP.getImage(), userP.getName(), userP.getUser_id());

                            joinPlay.set(playUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "added");
                                    }
                                }
                            });
                        }
                    }
                });


                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.fragment, new PlayFragment());
                fragmentTransaction.commit();
                break;

            case R.id.food:

                DocumentReference userRef3 = mDb.collection("Users").document(FirebaseAuth.getInstance().getUid());
                userRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User userF = Objects.requireNonNull(task.getResult()).toObject(User.class);

                            DocumentReference joinFood = mDb
                                    .collection("Food")
                                    .document(FirebaseAuth.getInstance().getUid());

                            FoodUser foodUser = new FoodUser(userF.getEmail(), userF.getImage(), userF.getName(), userF.getUser_id());

                            joinFood.set(foodUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "added");
                                    }
                                }
                            });
                        }
                    }
                });


                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.fragment, new FoodFragment());
                fragmentTransaction.commit();
                break;

            case R.id.hangout:

                DocumentReference userRef4 = mDb.collection("Users").document(FirebaseAuth.getInstance().getUid());
                userRef4.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User userH = Objects.requireNonNull(task.getResult()).toObject(User.class);

                            DocumentReference joinHangout = mDb
                                    .collection("Hangout")
                                    .document(FirebaseAuth.getInstance().getUid());

                            HangoutUser hangoutUser = new HangoutUser(userH.getEmail(), userH.getImage(), userH.getName(), userH.getUser_id());

                            joinHangout.set(hangoutUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "added");
                                    }
                                }
                            });
                        }
                    }
                });


                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.fragment, new HangoutFragment());
                fragmentTransaction.commit();
                break;

            case R.id.other:

                DocumentReference userRef5 = mDb.collection("Users").document(FirebaseAuth.getInstance().getUid());
                userRef5.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User userO = Objects.requireNonNull(task.getResult()).toObject(User.class);

                            DocumentReference joinOther = mDb
                                    .collection("Other")
                                    .document(FirebaseAuth.getInstance().getUid());

                            OtherUser otherUser = new OtherUser(userO.getEmail(), userO.getImage(), userO.getName(), userO.getUser_id());

                            joinOther.set(otherUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "added");
                                    }
                                }
                            });
                        }
                    }
                });


                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.fragment, new OtherFragment());
                fragmentTransaction.commit();
                break;

            default:
                Toast.makeText(getContext(), "Invalid Choice", Toast.LENGTH_SHORT).show();

        }
    }


    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(getContext(), LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                Objects.requireNonNull(getActivity()).startForegroundService(serviceIntent);
                service_pro.setVisibility(View.GONE);
                note.setVisibility(View.GONE);
            }else{
                Objects.requireNonNull(getActivity()).startService(serviceIntent);
                service_pro.setVisibility(View.GONE);
                note.setVisibility(View.GONE);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.societyfy.Activities.Services".equals(service.service.getClassName())) {
                Log.d("Service", "isLocationServiceRunning: location service is already running.");
                service_pro.setVisibility(View.GONE);
                note.setVisibility(View.GONE);
                return true;
            }
        }
        Log.d("Service", "isLocationServiceRunning: location service is not running.");
        service_pro.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);
        return false;
    }

    private void getUserDetails() {

        if (mUserLocation == null) {
            mUserLocation = new UserLocation();

            DocumentReference userRef = mDb.collection("Users").document(FirebaseAuth.getInstance().getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                       mUserLocation.setUser(user);
                        ((UserClient)(Objects.requireNonNull(getActivity()).getApplicationContext())).setUser(user);
                        getLastKnownLocation();

                    }
                }
            });
        } else {
            getLastKnownLocation();
        }
    }

    private void saveUserLocation() {

        if (mUserLocation != null) {
            DocumentReference locationRef = mDb.collection("Users' Locations").document(FirebaseAuth.getInstance().getUid());
            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "latitude: " + mUserLocation.getGeoPoint().getLatitude());
                        Log.d(TAG, "longitude: " + mUserLocation.getGeoPoint().getLongitude());
                    }
                }
            });
        }

    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation : called");
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null) {
                        Log.d(TAG, "OnComplete latitude: " + location.getLatitude());
                        Log.d(TAG, "OnComplete latitude: " + location.getLongitude());
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        mUserLocation.setGeoPoint(geoPoint);
                        mUserLocation.setTimestamp(null);
                        saveUserLocation();
                        startLocationService();

                    }
                }

            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (!mLocationPermissionGranted)
                getLocationPermission();
        } else {
            getUserDetails();
        }

    }


    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: checking google services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getContext(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getUserDetails();
                }
            }
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;

    }


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getUserDetails();

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getUserDetails();
                } else {
                    getLocationPermission();
                }
            }
        }


    }

}