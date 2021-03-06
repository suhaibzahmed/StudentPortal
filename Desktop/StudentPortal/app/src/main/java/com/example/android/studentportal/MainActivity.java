package com.example.android.studentportal;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {


    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postlist;
    private Toolbar mToolbar;



    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;



    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;



    String currentUserID;
/*if error occurs comment this one*/
    Context context=MainActivity.this;
    CharSequence text;

    Boolean LikeChecker = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");



        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        postlist = (RecyclerView) findViewById(R.id.All_User_Post_List);
        postlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postlist.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
/*if Error*/
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_img);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);



        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }

                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.student1).into(NavProfileImage);
                    }

                    else
                    {
                        Toast.makeText(MainActivity.this,"Profile name doesn't exist",Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

/*till here*/


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });



        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

        NavProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToProfileActivity();
            }
        });



        DisplayAllUsersPosts();
    }



    public void updateUserStatus(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UsersRef.child(currentUserID).child("userState")
                .updateChildren(currentStateMap);
    }


    private void DisplayAllUsersPosts()
    {

        Query SortPostsInDecendingOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                (
                        Posts.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        SortPostsInDecendingOrder
                )
        {

            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {

                final String PostKey = getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(),model.getPostimage());

                viewHolder.setLikeButtonStatus(PostKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent ClickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                        ClickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(ClickPostIntent);
                    }
                });

                viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent CommentsIntent = new Intent(MainActivity.this,CommentsActivity.class);
                        CommentsIntent.putExtra("PostKey",PostKey);
                        startActivity(CommentsIntent);
                    }
                });

                viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                             if(LikeChecker.equals(true))
                             {
                                 if (dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                     LikesRef.child(PostKey).child(currentUserID).removeValue();
                                     LikeChecker = false;
                                 }
                                 else{
                                     LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                     LikeChecker = false;

                                 }
                             }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };

        postlist.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online");
    }


    public static class PostsViewHolder extends  RecyclerView.ViewHolder {

        View mView;

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) mView.findViewById(R.id.display_no_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).hasChild(currentUserId))
                    {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes)+(" Likes"));
                    }
                    else {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes)+(" Likes"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname){
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time){
            TextView postTime = (TextView) mView.findViewById(R.id.update_time);
            postTime.setText("   " +time);
        }

        public void setDate(String date){
            TextView postDate = (TextView) mView.findViewById(R.id.update_date);
            postDate.setText("   "+date);
        }

        public void setDescription(String description){
            TextView postDescription = (TextView) mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }

        public void setPostimage(Context ctx1,String postimage){
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }


    }




    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void SendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(profileIntent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null)
        {
            sendUserToLoginActivity();
        }
        else{
            checkUserExistence();
        }
    }

    private void checkUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

   }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch(item.getItemId()){

            case R.id.Nav_Feed:
                SendUserToMainActivity();
                break;

            case R.id.Nav_Friends:
                sendUserToFriendsActivity();
                Toast.makeText(context,text="Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Messages:
                sendUserToMessagesActivity();
                Toast.makeText(context,text="Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Notes:
                sendUserToNotesActivity();
                Toast.makeText(context,text="Notes", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Quiz:
                sendUserToQuizActivity();
                Toast.makeText(context,text="Quiz", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Todo:
                Toast.makeText(context,text="Todo List", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_FindFriends:
                sendUserToFindFriendsActivity();
                Toast.makeText(context,text="Find Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Events:
                SendUserToEventsActivity();
                Toast.makeText(context,text="Events", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Request:
                sendUserToFriendRequestsActivity();
                Toast.makeText(context,text="Added You", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Settings:
                sendUserToSettingsActivity();
                Toast.makeText(context,text="Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Feedback:
                sendUserToFeedBackActivity();
                Toast.makeText(context,text="Feedback", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Nav_Logout:
                updateUserStatus("offline");
                mAuth.signOut();
                sendUserToLoginActivity();
                break;


        }
    }



    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(MainActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToMessagesActivity() {
        Intent MessagesIntent = new Intent(MainActivity.this,MessagesActivity.class);
        startActivity(MessagesIntent);
    }

    private void sendUserToFriendsActivity() {
        Intent FriendsIntent = new Intent(MainActivity.this,FriendsActivity.class);
        startActivity(FriendsIntent);
    }

    private void sendUserToFriendRequestsActivity() {
        Intent FriendReqIntent = new Intent(MainActivity.this,FriendRequestActivity.class);
        startActivity(FriendReqIntent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent FindFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(FindFriendsIntent);
    }

    private void SendUserToEventsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,EventsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);

    }
    private void sendUserToNotesActivity() {
        Intent noteIntent = new Intent(MainActivity.this,AllNotesActivity.class);
        startActivity(noteIntent);

    }
    private void sendUserToQuizActivity() {
        Intent noteIntent = new Intent(MainActivity.this,QuizActivity.class);
        startActivity(noteIntent);

    }

    private void sendUserToFeedBackActivity(){
        Intent feedIntent = new Intent(MainActivity.this,FeedbackActivity.class);
        startActivity(feedIntent);
    }


}
