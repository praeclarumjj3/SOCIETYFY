package com.example.societyfy.Activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.societyfy.Activities.Adapters.UserAdapter;
import com.example.societyfy.Activities.models.ClusterMarker;
import com.example.societyfy.Activities.models.PolyLineData;
import com.example.societyfy.Activities.models.User;
import com.example.societyfy.Activities.models.UserLocation;
import com.example.societyfy.Activities.models.UserRepo;
import com.example.societyfy.Activities.util.MyClusterManagerRenderer;
import com.example.societyfy.BuildConfig;
import com.example.societyfy.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.GeoApiContext;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.societyfy.Activities.Constants.MAPVIEW_BUNDLE_KEY;


public class Hangout_list extends Fragment implements OnMapReadyCallback, View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener, UserAdapter.UserListRecyclerClickListener{
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 0;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private static final String TAG = "UserListFragment";


    private RecyclerView userlist;
    private UserAdapter adapter;
    private UserRepo userRepo;
    private View v;

    private MapView mMapView;
    RecyclerView mUserListRecyclerView;
    RelativeLayout mMapContainer;
    FirebaseFirestore mDb;
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundary;
    private UserLocation mUserPosition;
    List<User> userList = new ArrayList<>();
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();

    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private GeoApiContext mGeoApiContext = null;

    private ArrayList<PolyLineData> mPolyLinesData = new ArrayList<>();

    private Marker mSelectedMarker = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    FragmentTransaction fragmentTransaction;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mUserLocations.size() == 0) { // make sure the list doesn't duplicate by navigating back
            if (getArguments() != null) {

                final ArrayList<UserLocation> locations = getArguments().getParcelableArrayList(getString(R.string.intent_huser_locations));
                mUserLocations.addAll(locations);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_hangout_list, container, false);
        setHasOptionsMenu(true);
        mUserListRecyclerView = v.findViewById(R.id.hangout_list);
        userRepo = new UserRepo(FirebaseFirestore.getInstance());

        mMapView = (MapView) v.findViewById(R.id.map);
        mMapContainer = v.findViewById(R.id.map_container);
        mDb = FirebaseFirestore.getInstance();
        v.findViewById(R.id.btn_full_screen_map).setOnClickListener(this);
        v.findViewById(R.id.btn_reset_map).setOnClickListener(this);


        ((MainActivity)getActivity()).toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.baseline_emoji_people_white_18dp));
        ((MainActivity)getActivity()).toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        getUsers();
        initUI();
        initGoogleMap(savedInstanceState);

        setUserPosition();

        for (UserLocation userLocation : mUserLocations) {
            Log.d("Location", "onCreateView: user location: " + userLocation.getUser().getName());
            Log.d("Location", "onCreateView: geopoint: " + userLocation.getGeoPoint().getLatitude()
                    + " , " + userLocation.getGeoPoint().getLongitude());
        }
        return v;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.setting_menu).setVisible(false).setEnabled(false);
        menu.findItem(R.id.study_users).setVisible(true).setEnabled(false);
        menu.findItem(R.id.chat_study).setVisible(true).setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch(id) {

            case R.id.chat_study:
                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment, new HangoutFragment());
                fragmentTransaction.commit();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void initUI() {

        userlist = v.findViewById(R.id.hangout_list);
        adapter = new UserAdapter(userList, getActivity(),this);
        userlist.setAdapter(adapter);
        userlist.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private void getUsers() {

        userRepo.getHangoutUsers(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("UserList", "Listen failed.", e);
                    return;
                }


                for (QueryDocumentSnapshot doc : snapshots) {
                    userList.add(new User(doc.getString("email"), doc.getString("image"), doc.getString("name"), doc.getString("user_id")));
                }

            }
        });
    }

    //Map Methods

    private void startUserLocationsRunnable() {
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations() {
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users in the chatroom.");

        try {
            for (final ClusterMarker clusterMarker : mClusterMarkers) {

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection("Users' Locations")
                        .document(clusterMarker.getUser().getUser_id());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                            // update the location
                            for (int i = 0; i < mClusterMarkers.size(); i++) {
                                try {
                                    if (mClusterMarkers.get(i).getUser().getUser_id().equals(updatedUserLocation.getUser().getUser_id())) {

                                        LatLng updatedLatLng = new LatLng(
                                                updatedUserLocation.getGeoPoint().getLatitude(),
                                                updatedUserLocation.getGeoPoint().getLongitude()
                                        );

                                        mClusterMarkers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));

                                    }


                                } catch (NullPointerException e) {
                                    Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage());
        }

    }

    private void resetMap(){
        if(mGoogleMap != null) {
            mGoogleMap.clear();

            if(mClusterManager != null){
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }

            if(mPolyLinesData.size() > 0){
                mPolyLinesData.clear();
                mPolyLinesData = new ArrayList<>();
            }
        }
    }

    private void addMapMarkers() {

        if (mGoogleMap != null) {
            resetMap();

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), mGoogleMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getActivity(),
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for (UserLocation userLocation : mUserLocations) {

                Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeoPoint().toString());
                try {
                    String snippet = "";
                    if (userLocation.getUser().getUser_id().equals(FirebaseAuth.getInstance().getUid())) {
                        snippet = "This is you";
                    } else {
                        snippet = "Determine route to " + userLocation.getUser().getName() + "?";
                    }

                    String avatar = userLocation.getUser().getImage();
                    ;

                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeoPoint().getLatitude(), userLocation.getGeoPoint().getLongitude()),
                            userLocation.getUser().getName(),
                            snippet,
                            avatar,
                            userLocation.getUser(),
                            getContext()
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.cluster();

            setCameraView();
        }
    }



    private void setCameraView() {

        if (mUserPosition != null) {
            double bottomBoundary = mUserPosition.getGeoPoint().getLatitude() - .01;
            double leftBoundary = mUserPosition.getGeoPoint().getLongitude() - .01;
            double topBoundary = mUserPosition.getGeoPoint().getLatitude() + .01;
            double rightBoundary = mUserPosition.getGeoPoint().getLongitude() + .01;

            mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        }
    }

    private void setUserPosition() {

        for (UserLocation userLocation : mUserLocations) {
            if ((userLocation.getUser().getUser_id()).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                mUserPosition = userLocation;
            }
        }
    }

    private void initGoogleMap(Bundle savedInstanceState) {

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(Integer.parseInt(BuildConfig.google_maps_api_key)))
                    .build();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        startUserLocationsRunnable();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        map.setMyLocationEnabled(true);
        mGoogleMap = map;
        addMapMarkers();
        mGoogleMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    private void expandMapAnimation() {
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                49,
                79);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                50,
                20);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    private void contractMapAnimation() {
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                79,
                49);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                20,
                50);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_full_screen_map: {

                if (mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                    Toast.makeText(getContext(), "Expanding Map", Toast.LENGTH_SHORT).show();
                } else if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    contractMapAnimation();
                    Toast.makeText(getContext(), "Contracting Map", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.btn_reset_map:{
                addMapMarkers();
                break;
            }

        }
    }


    @Override
    public void onInfoWindowClick(final Marker marker) {

        if (marker.getSnippet().equals("This is you")) {
            marker.hideInfoWindow();
        } else {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(marker.getSnippet())
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            mSelectedMarker = marker;
                            dialog.dismiss();

                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("Open Google Maps?")
                                    .setCancelable(true)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                            String latitude = String.valueOf(marker.getPosition().latitude);
                                            String longitude = String.valueOf(marker.getPosition().longitude);
                                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                            mapIntent.setPackage("com.google.android.apps.maps");

                                            try{
                                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                                    startActivity(mapIntent);
                                                }
                                            }catch (NullPointerException e){
                                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                                Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                            dialog.cancel();
                                        }
                                    });
                            final AlertDialog alert = builder.create();
                            alert.show();

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }


    }

    @Override
    public void onUserClicked(int position) {

        Log.d("Click","OnUser Clicked: user=" + userList.get(position).getName());
        String selectedUserId = userList.get(position).getUser_id();

        for(ClusterMarker clusterMarker: mClusterMarkers){
            if(selectedUserId.equals(clusterMarker.getUser().getUser_id())){
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(clusterMarker.getPosition().latitude,clusterMarker.getPosition().longitude)),
                        600,
                        null
                );
                break;
            }
        }

    }
}
