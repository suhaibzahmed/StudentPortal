package com.example.android.studentportal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userProfName, userStatus, userCountry,userGender, userDOB;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        profileUserRef=FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        userProfName = (TextView)findViewById(R.id.my_profile_full_name);
        userStatus = (TextView)findViewById(R.id.my_profile_status);
        userCountry = (TextView)findViewById(R.id.my_profile_country);
        userGender = (TextView)findViewById(R.id.my_profile_gender);
        userDOB = (TextView)findViewById(R.id.my_profile_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
            if(dataSnapshot.exists()){
                String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                String myStatus = dataSnapshot.child("status").getValue().toString();
                String myDOB = dataSnapshot.child("dob").getValue().toString();
                String myCountry = dataSnapshot.child("country").getValue().toString();
                String myGender = dataSnapshot.child("gender").getValue().toString();

                Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.student1).into(userProfileImage);

                userProfName.setText(myProfileName);
                userDOB.setText("dob: " +myDOB);
                userCountry.setText("country: " +myCountry);
                userGender.setText("gender: " +myGender);
                userStatus.setText(myStatus);
            }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
