package com.example.societyfy.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.societyfy.Activities.models.User;
import com.example.societyfy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;



public class LoginFragment extends Fragment {

    private EditText userMail,userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private CircleImageView loginPhoto;
    public SharedPreferences  preferences;
    public SharedPreferences.Editor  editor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        userMail=v.findViewById(R.id.login_mail);
        userPassword=v.findViewById(R.id.login_pwd);
        btnLogin=v.findViewById(R.id.login_btn);
        loginProgress=v.findViewById(R.id.login_progress);
        mAuth=FirebaseAuth.getInstance();
        loginPhoto = v.findViewById(R.id.login_photo);





        loginPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentTransaction fragmentTransaction1 = getFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.fragment, new RegisterFragment());
                fragmentTransaction1.addToBackStack(null);
                fragmentTransaction1.commit();

            }
        });

        loginProgress.setVisibility(View.INVISIBLE);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnLogin.setVisibility(View.INVISIBLE);
                loginProgress.setVisibility(View.VISIBLE);
                String mail = userMail.getText().toString();
                String password = userPassword.getText().toString();
                preferences = Objects.requireNonNull(getContext()).getSharedPreferences("User_pref",Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.putString("password", userPassword.getText().toString());
                editor.commit();
                editor.apply();


                if (mail.isEmpty() || password.isEmpty()) {
                    showMessage("Please Verify all fields");
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);

                } else {
                    signIn(mail, password);

                }

            }

            private void showMessage(String message) {

                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

            }

            private void signIn(String mail, String password) {

                mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            loginProgress.setVisibility(View.INVISIBLE);
                            btnLogin.setVisibility(View.VISIBLE);
                            updateUI();
                        }
                        else{
                            showMessage("Wrong Credentials!!!");
                            btnLogin.setVisibility(View.VISIBLE);
                            loginProgress.setVisibility(View.INVISIBLE);

                        }
                    }
                });



            }
        });

        return v;


    }

    private void updateUI() {

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        fragmentTransaction.replace(R.id.fragment, new PermissionFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){

            updateUI();
        }
    }






}
