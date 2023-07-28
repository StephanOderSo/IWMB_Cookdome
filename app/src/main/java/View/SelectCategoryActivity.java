package View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bienhuels.iwmb_cookdome.R;

public class SelectCategoryActivity extends AppCompatActivity {
    CardView main,brekki,dessert,soup,snack,salad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);
        main=findViewById(R.id.main);
        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
                intent.putExtra("filter",getResources().getString(R.string.mainMeal));
                startActivity(intent);
                finish();
            }
        });
        brekki=findViewById(R.id.brekki);
        brekki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
                intent.putExtra("filter",getResources().getString(R.string.breakki));
                startActivity(intent);
                finish();
            }
        });
        dessert=findViewById(R.id.dessert);
        dessert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
                intent.putExtra("filter",getResources().getString(R.string.dessert));
                startActivity(intent);
                finish();
            }
        });
        soup=findViewById(R.id.soup);
        soup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
                intent.putExtra("filter",getResources().getString(R.string.soup));
                startActivity(intent);
                finish();
            }
        });
        snack=findViewById(R.id.snack);
        snack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
                intent.putExtra("filter",getResources().getString(R.string.snack));
                startActivity(intent);
                finish();
            }
        });
        salad=findViewById(R.id.salad);
        salad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectCategoryActivity.this, SearchActivity.class);
                intent.putExtra("filter",getResources().getString(R.string.salad));
                startActivity(intent);
                finish();
            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent toMainIntent=new Intent(SelectCategoryActivity.this,MainActivity.class);
        startActivity(toMainIntent);}
}