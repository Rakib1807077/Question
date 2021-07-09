package com.example.question;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.question.Adapters.PostAdapter;
import com.example.question.Model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectedActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private String title="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selected);

        toolbar=findViewById(R.id.home_toolbar1);
        setSupportActionBar(toolbar);

        recyclerView=findViewById(R.id.recyclerView1);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);  //firebase realtime database theke data retrieve korar jonno ekta adapter lagbe..ja package e toiry koresi


        postList=new ArrayList<>();
        postAdapter=new PostAdapter(CategorySelectedActivity.this,postList);//postAdapter.java te class e je context r list r kotha bola ase..otay ekhane pass kora hoyese
        recyclerView.setAdapter(postAdapter);

        if(getIntent().getExtras()!=null)
        {
            title=getIntent().getStringExtra("title");
            getSupportActionBar().setTitle(title);

            readPosts();
        }



    }

    private void readPosts() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("questions posts");
        //amra shudhu only particular catagory r question chay.tay e process
        Query query=reference.orderByChild("topic").equalTo(title);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear(); //Eta use korar karon jana lagbe
                postList.clear();
                {
                    for(DataSnapshot dataSnapshot: snapshot.getChildren())
                    {
                        Post post=dataSnapshot.getValue(Post.class);
                        postList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategorySelectedActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }
}