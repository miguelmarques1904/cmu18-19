package pt.ist.cmu.p2photo;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import pt.ist.cmu.helpers.ImageHelper;

public class ViewPhotoActivity extends AppCompatActivity {

    Bitmap b;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewphoto);

        uri = Uri.parse(getIntent().getStringExtra("photoURI"));

        Bitmap b = ImageHelper.getBitmapFromURI(uri, this.getContentResolver());


        if(b != null) {
            ImageView iv = (ImageView) findViewById(R.id.viewphoto_image);
            iv.setImageBitmap(b);
            iv.setAdjustViewBounds(true);
        }
    }


    public void viewPhotoBackOnClick(View v) {
        finish();
    }
}
