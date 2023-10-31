package Viewmodel.CreateRecipeAdapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bienhuels.iwmb_cookdome.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Model.Firebase;
import Model.Recipe;
import Model.User;

public class ShareListAdapter extends ArrayAdapter <User> {
    Recipe recipe;
    ArrayList<User>list;
    Handler handler;
    Firebase firebase=new Firebase();


    public ShareListAdapter(@NonNull Context context, int resource, ArrayList<User> list,Recipe selectedRecipe,Handler handler) {
        super(context, resource,list);
        this.recipe=selectedRecipe;
        this.list=list;
        this.handler=handler;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user=getItem(position);
        if(convertView==null) {
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.cell_user,parent,false);
        }
        if(user!=null){
            ImageView userImage=convertView.findViewById(R.id.userImage);
            TextView userName=convertView.findViewById(R.id.userName);
            ImageButton remove=convertView.findViewById(R.id.share);
            Picasso.get()
                    .load(user.getPhoto())
                    .placeholder(R.drawable.image)
                    .fit()
                    .centerCrop()
                    .into(userImage);
           userName.setText(user.getName());
           remove.setImageResource(R.drawable.remove);
           remove.setOnClickListener(view -> {
               Runnable updateRun= () -> {

                   handler.post(() -> {
                       remove(user);
                       notifyDataSetChanged();

                   });
               };
               Thread updateThread=new Thread(updateRun);

               String uid=user.getId();
               Context context=getContext();

               Runnable run= () -> {
                   firebase.removeUserFromShareList(uid, recipe.getKey(), context, recipe.getOwner(), recipe.getPriv(), handler, updateThread);
                   firebase.unshareRecipe(recipe.getKey(), uid,recipe.getPriv(),context,handler);
               };
               Thread thread=new Thread(run);
               thread.start();

           });
        }
        return convertView;
    }
}
