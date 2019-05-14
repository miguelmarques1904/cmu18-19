package pt.ist.cmu.p2photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import pt.ist.cmu.helpers.ImageHelper;

public class ViewPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewphoto);

        // extract bytes and convert to bitmap
        byte[] image = getIntent().getByteArrayExtra("bitmap");
        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);

        // create image view and load picture
        ImageView iv = findViewById(R.id.viewphoto_image);
        iv.setImageBitmap(bmp);
        iv.setAdjustViewBounds(true);
    }

    public void viewPhotoBackOnClick(View v) {
        finish();
    }
}
