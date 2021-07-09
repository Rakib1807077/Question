package com.example.question;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.question.Adapters.PostAdapter;
import com.example.question.Model.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private ProgressBar progress_circular;

    //coming to the home activity and retrieve the questions post,so later variable declaring,finding and other stuff
    private PostAdapter postAdapter;
    private List<Post> postList;

    private CircleImageView navHeaderImage;
    private TextView navHeaderEmail,navHeaderName;

    private DatabaseReference userRef;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth=FirebaseAuth.getInstance();

        fab=findViewById(R.id.fab);

        drawerLayout=findViewById(R.id.drawer_layout);
        toolbar=findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Question App");

        recyclerView=findViewById(R.id.recyclerView);
        progress_circular=findViewById(R.id.progress_circular);

        //we need to set a linear layout manager for recycler view,so initializing
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);  //firebase realtime database theke data retrieve korar jonno ekta adapter lagbe..ja package e toiry koresi

        NavigationView navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this,AskAQuestionActivity.class);
                startActivity(intent);
            }
        });

        navHeaderEmail=navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);
        navHeaderName=navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
        navHeaderImage=navigationView.getHeaderView(0).findViewById(R.id.nav_header_profile_image);
        //Now populating it
        userRef=FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Glide.with(HomeActivity.this).load(snapshot.child("profileimageurl").getValue(String.class)).into(navHeaderImage);
                navHeaderName.setText(snapshot.child("username").getValue().toString());
                navHeaderEmail.setText(snapshot.child("email").getValue().toString());



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

        postList=new ArrayList<>();
        postAdapter=new PostAdapter(HomeActivity.this,postList);//postAdapter.java te class e je context r list r kotha bola ase..otay ekhane pass kora hoyese
        recyclerView.setAdapter(postAdapter);

        readQuestionsPosts();
    }

    private void readQuestionsPosts()
    {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("questions posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    Post post=dataSnapshot.getValue(Post.class);
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.nav_science:
                {Intent intent=new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intent.putExtra("title","Science");
                startActivity(intent);
                break;}
            case R.id.nav_foods:
            {Intent intent=new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intent.putExtra("title","food");
                startActivity(intent);
                break;}
            case R.id.nav_sports:
            {Intent intent=new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intent.putExtra("title","sports");
                startActivity(intent);
                break;}
            case R.id.logout:
            {
                 FirebaseAuth.getInstance().signOut();
                    finish();
                    Toast.makeText(getApplicationContext(),"You are log out",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                break;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
        {
            super.onBackPressed();
        }

    }


}