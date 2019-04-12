package com.example.android.studentportal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText Username,Fullname,Country;
    private Button ConfirmInformation;
    private CircleImageView setupProfile;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;

    String currentUserID;
    final static int GalleryPick = 1;


    Context context = SetupActivity.this;
    CharSequence text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profileimages");

        Username = (EditText) findViewById(R.id.setup_username);
        Fullname = (EditText) findViewById(R.id.setup_fullname);
        Country = (EditText) findViewById(R.id.setup_country);
        ConfirmInformation = (Button) findViewById(R.id.setup_save);
        setupProfile = (CircleImageView) findViewById(R.id.setup_profile);

        ConfirmInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfirmInformation();
            }
        });


        setupProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });


        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.student1).into(setupProfile);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this,"Please Select Profile Image First",Toast.LENGTH_SHORT).show();
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
    }

    if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Uploading Image");
                loadingBar.setMessage("Please wait..");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, text = "Profile Picture Uploaded Successfully", Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();


                                    UsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                /*Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);*/
                                                Toast.makeText(context, text = "Profile Picture Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                            else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(context, text = "Error Occured:" + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
            else {
                Toast.makeText(context,text="Error Occured:Image Can't Be Cropped. Try Again",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void saveConfirmInformation() {
        String username = Username.getText().toString();
        String fullname = Fullname.getText().toString();
        String country = Country.getText().toString();
        if(TextUtils.isEmpty(username)){
            Toast.makeText(context,text = "Username is Empty",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(context,text = "Fullname is Empty",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(context,text = "Country is Empty",Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Confirming");
            loadingBar.setMessage("Please wait, Your Details are getting updated");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("status","");
            userMap.put("gender","");
            userMap.put("dob","");

            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(context,text="Your Data Stored Successfully",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(context,text = "Error Occured:" +message,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });



        }

    }





    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
