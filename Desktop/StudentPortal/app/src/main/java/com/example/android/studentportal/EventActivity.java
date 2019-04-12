package com.example.android.studentportal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class EventActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private ImageButton SelectEventImage;
    private Button UpdateEventButton;
    private EditText EventDescription;


    private static final int GalleryPick=1;
    private Uri ImageUri;
    private String Description;

    private StorageReference EventReference;
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, eventRandomName, downloadUrl,current_user_id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        EventReference = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Events");

        SelectEventImage = (ImageButton) findViewById(R.id.EventImage);
        UpdateEventButton = (Button) findViewById(R.id.User_Event_button);
        EventDescription = (EditText) findViewById(R.id.EventText);
        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar)findViewById(R.id.update_Event_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Event");

        SelectEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        UpdateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateEventInfo();
            }
        });
    }

    private void ValidateEventInfo()
    {
        Description = EventDescription.getText().toString();

        if(TextUtils.isEmpty(Description)){
            Toast.makeText(EventActivity.this,"We're Sorry Your Event Description is Empty",Toast.LENGTH_LONG).show();
        }
        else if(ImageUri == null){
            Toast.makeText(EventActivity.this,"We're Sorry You havn't Selected The Event Image",Toast.LENGTH_LONG).show();
        }
        else{
            loadingBar.setTitle("Creating Event");
            loadingBar.setMessage("Please wait, Your Event is being created");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringEventToFirebaseStorage();
        }

    }

    private void StoringEventToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        eventRandomName = saveCurrentDate + saveCurrentTime;


        StorageReference filePath = EventReference.child("Event Images").child(ImageUri.getLastPathSegment() + eventRandomName + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {

                    downloadUrl = task.getResult().getDownloadUrl().toString();

                    Toast.makeText(EventActivity.this,"Event Created Successfully",Toast.LENGTH_SHORT).show();

                    SavingEventInformationToDatabase();
                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(EventActivity.this,"Error Occured:"+message,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingEventInformationToDatabase() {
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid",current_user_id);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("description",Description);
                    postsMap.put("eventimage",downloadUrl);
                    postsMap.put("profileimage",userProfileImage);
                    postsMap.put("fullname", userFullName);
                    PostsRef.child(current_user_id + eventRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        SendUserToEventsActivity();
                                        Toast.makeText(EventActivity.this,"Event Created Successfully..",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else {
                                        Toast.makeText(EventActivity.this,"Error Occurred While Updating your Event",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            ImageUri = data.getData();
            SelectEventImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id= item.getItemId();

        if(id==android.R.id.home)
        {
            SendUserToEventsActivity();
        }
        return super.onOptionsItemSelected(item);
    }


    private void SendUserToEventsActivity()
    {
        Intent mainIntent = new Intent(EventActivity.this,EventsActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
