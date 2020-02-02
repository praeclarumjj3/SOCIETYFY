package com.example.societyfy.Activities.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.societyfy.Activities.MainActivity;
import com.example.societyfy.Activities.PermissionFragment;
import com.example.societyfy.Activities.models.User;
import com.example.societyfy.Activities.models.UserLocation;
import com.example.societyfy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    CircleImageView profile_pic;
    private final String TAG = "DELETE";
    TextView profile_name;
    TextView profile_mail;
    TextView profile_address;
    Button update;
    ProgressBar profile_pro;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    static int PReqCode=1;
    static int RequesCode=1;
    View v;
    Uri pickedImgUri;
    private FirebaseFirestore db;
    public Uri downloadURL;
    private FirebaseStorage mStorage;
    StorageReference storageReference;


    private GeoPoint geoPointUser;



    private static final String CURRENT_USER_IMAGE = "CURRENT_USER_IMAGE";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_profile, container, false);

        ((MainActivity)getActivity()).drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ((MainActivity)getActivity()).toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        ((MainActivity)getActivity()).toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
            }
        });


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();


        profile_pic = v.findViewById(R.id.profile_photo);
        profile_name = v.findViewById(R.id.profile_name);
        profile_mail = v.findViewById(R.id.profile_user_mail);
        profile_address = v.findViewById(R.id.profile_user_address);
        update = v.findViewById(R.id.update);
        profile_pro = v.findViewById(R.id.profile_progress);
        profile_pro.setVisibility(View.INVISIBLE);
        update.setVisibility(View.INVISIBLE);
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReference();

        getUserPosition();


        profile_name.setText(currentUser.getDisplayName());
        profile_mail.setText(currentUser.getEmail());

        Glide.with(this).load(currentUser.getPhotoUrl()).into(profile_pic);

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openGallery();


            }
        });



        mAuth=  FirebaseAuth.getInstance();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                update.setVisibility(View.INVISIBLE);
                profile_pro.setVisibility(View.VISIBLE);

                final String name = currentUser.getDisplayName();
                update(name, pickedImgUri,mAuth.getCurrentUser());


                }
        });



        return v;

    }

    private void openGallery() {

        Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,RequesCode);
    }

    private void showMessage(String message) {

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

    }

    private void update( final String name, Uri pickedImgUri, final FirebaseUser currentUser) {


        final StorageReference fileReference = storageReference.child("images/" + currentUser.getUid());
        fileReference.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                     @Override
                                                                     public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                         showMessage("Upload Successful");
                                                                         fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                             @Override
                                                                             public void onSuccess(Uri uri) {
                                                                                 downloadURL = uri;
                                                                                 Log.i("URL", uri.toString());


                                                                                 DocumentReference Ref = db.collection("Users").document(currentUser.getUid());

                                                                                 Ref.update("image", downloadURL.toString())
                                                                                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                             @Override
                                                                                             public void onSuccess(Void aVoid) {
                                                                                                 Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                                                             }
                                                                                         })
                                                                                         .addOnFailureListener(new OnFailureListener() {
                                                                                             @Override
                                                                                             public void onFailure(@NonNull Exception e) {
                                                                                                 Log.w(TAG, "Error updating document", e);
                                                                                             }
                                                                                         });
                                                                             }
                                                                         });
                                                                     }
                                                                 });

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(name)
                                .setPhotoUri(uri).build();

                        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    showMessage("Profile Updated");
                                    updateImage();
                                    updateUI();
                                }


                            }
                        });
                    }
                });


            }
        });

    }
    private void updateImage() {
        CircleImageView profile_pic = v.findViewById(R.id.profile_photo);
        Glide.with(this).load(currentUser.getPhotoUrl()).into(profile_pic);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CURRENT_USER_IMAGE, String.valueOf(currentUser.getPhotoUrl()));
        editor.apply();



    }

    private void updateUI() {
        final Intent i = new Intent(getActivity(), MainActivity.class);
        startActivity(i);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==-1 && requestCode==RequesCode && data!=null){
            pickedImgUri = data.getData();
            profile_pic.setImageURI(pickedImgUri);
            update.setVisibility(View.VISIBLE);


        }
    }

    private String getAddressOfUser(GeoPoint geoPoint) {
        String mAddress = "";

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(),geoPoint.getLongitude(),1);

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            String addressOfUser = city + " , " + state + " , " + country + " , " + postalCode + "(" + knownName + ")";
            profile_address.setText(addressOfUser);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mAddress;
    }

    private void getUserPosition(){

        DocumentReference locationRef = db.collection("Users' Locations").document(currentUser.getUid());

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (Objects.requireNonNull(task.getResult()).toObject(UserLocation.class) != null) {
                        geoPointUser = Objects.requireNonNull(task.getResult().toObject(UserLocation.class)).getGeoPoint();
                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(geoPointUser.getLatitude(),geoPointUser.getLongitude(),1);

                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String country = addresses.get(0).getCountryName();
                            String postalCode = addresses.get(0).getPostalCode();
                            String knownName = addresses.get(0).getFeatureName();// Only if available else return NULL
                            String addressOfUser = city + " , " + state + " , " + country + " , " + postalCode + "(" + knownName + ")";
                            profile_address.setText(addressOfUser);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }

            }
        });


    }

}



