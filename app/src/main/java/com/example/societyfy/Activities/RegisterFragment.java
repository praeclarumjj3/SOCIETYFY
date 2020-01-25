package com.example.societyfy.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.societyfy.Activities.models.User;
import com.example.societyfy.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterFragment extends Fragment {



    CircleImageView ImgUserPhoto;
    static int PReqCode=1;
    static int RequesCode=1;
    public Uri pickedImgUri;
    public Uri downloadURL;

    private EditText userMail,userPassword,userName;
    private ProgressBar loadingProgress;
    private Button regBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;
    StorageReference storageReference;

    public SharedPreferences preferences;
    public SharedPreferences.Editor  editor;
    //public static String Url;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_register, container, false);

        userMail=v.findViewById(R.id.regUid);
        userPassword=v.findViewById(R.id.regPwd);
        userName=v.findViewById(R.id.regName);
        loadingProgress=v.findViewById(R.id.login_progress);
        regBtn=v.findViewById(R.id.login_bton);

        loadingProgress.setVisibility(View.INVISIBLE);
        mAuth=  FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReference();



        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String mail = userMail.getText().toString();
                final String password = userPassword.getText().toString();
                final String name = userName.getText().toString();


                preferences = Objects.requireNonNull(getContext()).getSharedPreferences("User_pref",Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.putString("password", userPassword.getText().toString());
                editor.apply();



                if (mail.isEmpty() || name.isEmpty() || password.isEmpty()) {
                    showMessage("Please Verify all fields");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
                else {

                    if(pickedImgUri!=null){

                    CreateUserAccount(mail,password,name);}
                    else{
                        showMessage("Please choose an image") ;
                        loadingProgress.setVisibility(View.INVISIBLE);
                        regBtn.setVisibility(View.VISIBLE);
                    }

                }
            }
        });

        ImgUserPhoto=v.findViewById(R.id.regUserPic);
        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT>=28){
                    checkAndRequestForPermission();
                }
                else{
                    openGallery();
                }
            }
        });



        return v;
    }

    private void CreateUserAccount(final String uid, String pwd, final String name) {

        mAuth.createUserWithEmailAndPassword(uid,pwd).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){



                    showMessage("Account creation may take a few seconds. " +
                            "Please DON'T close the app");
                    update(name,pickedImgUri,mAuth.getCurrentUser());


                }
                else{
                    showMessage("Account creation failed. Change your e-mail id please.");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void update(final String name, final Uri pickedImgUri, final FirebaseUser currentUser) {

        if (pickedImgUri != null) {

            final StorageReference fileReference = storageReference.child("images/" + currentUser.getUid());
            fileReference.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadURL = uri;
                            Log.i("URL", uri.toString());


                            //insert some default data
                            User user = new User();
                            user.setEmail(userMail.getText().toString());
                            user.setName(userName.getText().toString());
                            user.setUser_id(FirebaseAuth.getInstance().getUid());
                            user.setImage(downloadURL.toString());


                            DocumentReference newUserRef = mFirestore
                                    .collection("Users")
                                    .document(currentUser.getUid());

                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

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

                                                                if (task.isSuccessful()) {
                                                                    showMessage("Registration Complete");
                                                                    updateUI();
                                                                }


                                                            }
                                                        });
                                                    }
                                                });


                                            }
                                        });

                                    } else {
                                        Toast.makeText(getContext(), "Database failed", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showMessage("FAILUREinURIobtain");
                }

                });



        }else {  showMessage("Please choose an image") ;
        loadingProgress.setVisibility(View.INVISIBLE);
        regBtn.setVisibility(View.VISIBLE);
        }
    }

    private void updateUI() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        fragmentTransaction.replace(R.id.fragment, new PermissionFragment());
        fragmentTransaction.commit();
    }

    private void showMessage(String message) {

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

    }

    private void openGallery() {

        Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,RequesCode);
    }


    private void checkAndRequestForPermission() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){

                Toast.makeText(getContext(), "Please grant permission", Toast.LENGTH_SHORT).show();

            }
            else{
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PReqCode);
                checkAndRequestForPermission();}
        }
        else
            openGallery();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1 && requestCode==RequesCode && data!=null){
            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);

        }
    }







}
