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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventsActivity extends AppCompatActivity {


    private RecyclerView eventlist;
    private Toolbar mToolbar;


    private ImageButton AddNewEventButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, EventsRef;


    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        EventsRef = FirebaseDatabase.getInstance().getReference().child("Events");



        mToolbar = (Toolbar) findViewById(R.id.event_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Events");

        AddNewEventButton = (ImageButton) findViewById(R.id.add_new_event_button);

        eventlist = (RecyclerView) findViewById(R.id.All_User_Event_List);
        eventlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        eventlist.setLayoutManager(linearLayoutManager);

        AddNewEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToEventActivity();
            }
        });
        DisplayAllUsersEvents();
    }

    private void SendUserToEventActivity() {
        Intent addNewEventIntent = new Intent(EventsActivity.this, EventActivity.class);
        startActivity(addNewEventIntent);
    }




    private void DisplayAllUsersEvents()
    {
        Query SortPostsInDecendingOrder = EventsRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Events, EventsActivity.EventsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Events, EventsActivity.EventsViewHolder>
                (
                        Events.class,
                        R.layout.all_events_list,
                        EventsActivity.EventsViewHolder.class,
                        SortPostsInDecendingOrder
                )
        {

            @Override
            protected void populateViewHolder(EventsActivity.EventsViewHolder viewHolder, Events model, int position) {

                final String EventKey = getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setStartdate(model.getStartdate());
                viewHolder.setEnddate(model.getEnddate());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                viewHolder.setEventimage(getApplicationContext(),model.getEventimage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent ClickEventIntent = new Intent(EventsActivity.this,ClickEventActivity.class);
                        ClickEventIntent.putExtra("EventKey",EventKey);
                        startActivity(ClickEventIntent);
                    }
                });

            }
        };

        eventlist.setAdapter(firebaseRecyclerAdapter);
    }

    public static class EventsViewHolder extends  RecyclerView.ViewHolder {

        View mView;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname){
            TextView username = (TextView) mView.findViewById(R.id.event_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.event_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time){
            TextView postTime = (TextView) mView.findViewById(R.id.event_update_time);
            postTime.setText("   " +time);
        }

        public void setDate(String date){
            TextView postDate = (TextView) mView.findViewById(R.id.event_update_date);
            postDate.setText("   "+date);
        }

        public void setDescription(String description){
            TextView postDescription = (TextView) mView.findViewById(R.id.event_description);
            postDescription.setText(description);
        }

        public void setEventimage(Context ctx1,String eventimage){
            ImageView EventImage = (ImageView) mView.findViewById(R.id.event_image);
            Picasso.with(ctx1).load(eventimage).into(EventImage);
        }

        public void setEnddate(String enddate){
            TextView endDate = (TextView) mView.findViewById(R.id.event_endOn);
            endDate.setText("Event Ends On "+enddate);
        }
        public void setStartdate(String startdate){
            TextView startDate = (TextView) mView.findViewById(R.id.event_startOn);
            startDate.setText("Event Starts On "+startdate);
        }


    }

}
