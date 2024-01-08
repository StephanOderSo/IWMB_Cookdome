package Viewmodel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.recyclerview.widget.RecyclerView;

import com.bienhuels.iwmb_cookdome.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import Model.Recipe;

public class Tools {
    public void getListViewSize(ArrayAdapter adapter, ListView view,  Handler handler){
        adapter.notifyDataSetChanged();
        int totalHeight=0;
        for ( int i=0;i<adapter.getCount();i++) {
            View mView=adapter.getView(i,null,view);
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += mView.getMeasuredHeight();
        }
        int newTotal=totalHeight;
        handler.post(() -> {
            ViewGroup.LayoutParams params=view.getLayoutParams();
            params.height=newTotal+view.getDividerHeight()*adapter.getCount();
            view.setLayoutParams(params);
        });

    }

    public StringBuilder setDietString(Recipe selectedRecipe,ArrayList<String> stringArray) {
        StringBuilder dietaryTxt = new StringBuilder();
        for (String diet : selectedRecipe.getDietaryRec()) {
            String dietShort = "";
            if (diet.equals(stringArray.get(0))) {
                dietShort = "VT";
            }
            if (diet.equals(stringArray.get(1))) {
                dietShort = "V";
            }
            if (diet.equals(stringArray.get(2))) {
                dietShort = "GF";
            }
            if (diet.equals(stringArray.get(3))) {
                dietShort = "LF";
            }
            if (diet.equals(stringArray.get(4))) {
                dietShort = "P";
            }
            if (diet.equals(stringArray.get(5))) {
                dietShort = "LoF";
            }
            dietaryTxt.append(dietShort);
            int i;
            i = selectedRecipe.getDietaryRec().indexOf(diet);
            if (i != selectedRecipe.getDietaryRec().size() - 1) {
                dietaryTxt.append(" | ");
            }
        }return dietaryTxt;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "recipeShot", null);
        return Uri.parse(path);
    }
    public void onCheck(CheckBox checkbox, String selectedFilter, RecyclerView.Adapter recyclerAdapter, ArrayList<String> selectedList){
        if(checkbox.isChecked()){
            selectedList.add(selectedFilter);
            recyclerAdapter.notifyItemInserted(selectedList.indexOf(selectedFilter));
        }
        else{
            selectedList.remove(selectedFilter);
            recyclerAdapter.notifyDataSetChanged();
        }
    }


}
