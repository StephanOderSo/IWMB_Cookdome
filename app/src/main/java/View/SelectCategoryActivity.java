package View;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bienhuels.iwmb_cookdome.R;

public class SelectCategoryActivity extends AppCompatActivity {
    CardView main,brekki,dessert,soup,snack,salad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);
        main=findViewById(R.id.main);
        brekki=findViewById(R.id.brekki);
        dessert=findViewById(R.id.dessert);
        soup=findViewById(R.id.soup);
        snack=findViewById(R.id.snack);
        salad=findViewById(R.id.salad);

        main.setOnClickListener(view -> {
            Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
            intent.putExtra("filter",getResources().getString(R.string.mainMeal));
            startActivity(intent);
            finish();
        });

        brekki.setOnClickListener(view -> {
            Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
            intent.putExtra("filter",getResources().getString(R.string.breakki));
            startActivity(intent);
            finish();
        });

        dessert.setOnClickListener(view -> {
            Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
            intent.putExtra("filter",getResources().getString(R.string.dessert));
            startActivity(intent);
            finish();
        });

        soup.setOnClickListener(view -> {
            Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
            intent.putExtra("filter",getResources().getString(R.string.soup));
            startActivity(intent);
            finish();
        });

        snack.setOnClickListener(view -> {
            Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
            intent.putExtra("filter",getResources().getString(R.string.snack));
            startActivity(intent);
            finish();
        });

        salad.setOnClickListener(view -> {
            Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
            intent.putExtra("filter",getResources().getString(R.string.salad));
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        Intent toMainIntent=new Intent(SelectCategoryActivity.this,MainActivity.class);
        startActivity(toMainIntent);}
}