package Model;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Step {

    String step;
    String media;
    StorageReference storageRef= FirebaseStorage.getInstance().getReference().child("Images/");

    public Step(String step){
        this.step=step;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }
    public void setMediaUri(Uri media,Thread thread) {
        storageRef.child(media.getLastPathSegment()).putFile(media).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
            while(!uriTask.isComplete());
            this.media = uriTask.getResult().toString();
            thread.start();
        });
    }

}
