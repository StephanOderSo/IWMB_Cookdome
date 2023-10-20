package View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import Model.Database;
import Model.Recipe;
import Model.User;

public class UsersActivity extends AppCompatActivity implements  Database{
    RecyclerView recycler;
    SearchView search;
    Recipe recipe=new Recipe();
    String key;
    Handler handler=new Handler();
    Context context;
    FirebaseUser fbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent previousIntent=getIntent();
        key=previousIntent.getStringExtra("key");
        context=getApplicationContext();
        fbUser= FirebaseAuth.getInstance().getCurrentUser();
        getRecipe();
        setContentView(R.layout.activity_users);
        search=findViewById(R.id.searchUsers);
        recycler=findViewById(R.id.userRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                FirebaseRecyclerAdapter<User,RecyclerViewHolder>adapter=searchUsers(s,recipe,context, fbUser.getUid());
                adapter.startListening();
                recycler.setAdapter(adapter);
                return false;
            }
        });
    }
    private void getRecipe(){
        Runnable setDataRun= () -> recipe=recipe.getRecipe();
        Thread setDataThread=new Thread(setDataRun);

        Runnable runnable= () -> recipe.download(key,context,handler,setDataThread,fbUser);
        Thread getRecipeThread=new Thread(runnable);
        getRecipeThread.start();
    }
}