package com.example.android.studentportal;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userProfName, userStatus, userCountry,userGender, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendReqButton, DeclineFriendRequestButton;

    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;

    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        InitializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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

                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.student1).into(userProfileImage);

                    userProfName.setText(myProfileName);
                    userDOB.setText("Dob: " +myDOB);
                    userCountry.setText("Country: " +myCountry);
                    userGender.setText("Gender: " +myGender);
                    userStatus.setText(myStatus);


                    MaintainanceofButtons();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);


        if (!senderUserId.equals(receiverUserId))
        {
            SendFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    SendFriendReqButton.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToAPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends"))
                    {
                        UnFriendAnExistingFriend();
                    }
                }
            });
        }
        else
        {
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendReqButton.setVisibility(View.INVISIBLE);
        }
    }

    private void UnFriendAnExistingFriend()
    {
        FriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendReqButton.setText("Add Friend");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void AcceptFriendRequest()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                FriendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        SendFriendReqButton.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        SendFriendReqButton.setText("Unfriend");

                                                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendRequestButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }




    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendReqButton.setText("Add Friend");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }






    private void MaintainanceofButtons()
    {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiverUserId)){
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if (request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_sent";
                                SendFriendReqButton.setText("Cancel Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);
                            }
                            else if (request_type.equals("received"))
                            {
                                CURRENT_STATE = "request_received";
                                SendFriendReqButton.setText("Accept Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestButton.setEnabled(true);

                                DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserId))
                                            {
                                                CURRENT_STATE = "friends";
                                                SendFriendReqButton.setText("Unfriend");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequestToAPerson()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful())
                       {
                          FriendRequestRef.child(receiverUserId).child(senderUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        SendFriendReqButton.setEnabled(true);
                                        CURRENT_STATE = "request_sent";
                                        SendFriendReqButton.setText("Cancel Friend Request");

                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        DeclineFriendRequestButton.setEnabled(false);
                                    }
                                }
                            });
                       }
                    }
                });
    }

    private void InitializeFields() {
        userProfName = (TextView)findViewById(R.id.person_full_name);
        userStatus = (TextView)findViewById(R.id.person_profile_status);
        userCountry = (TextView)findViewById(R.id.person_country);
        userGender = (TextView)findViewById(R.id.person_gender);
        userDOB = (TextView)findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);

        SendFriendReqButton = (Button)findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestButton = (Button)findViewById(R.id.person_decline_friend_request_btn);

        CURRENT_STATE = "not_friends";
    }
}
