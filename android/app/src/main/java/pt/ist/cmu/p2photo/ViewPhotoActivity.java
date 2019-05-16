package pt.ist.cmu.p2photo;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import pt.ist.cmu.helpers.ImageHelper;

public class ViewPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewphoto);

        Uri uri = Uri.parse(getIntent().getStringExtra("photoURI"));
        Bitmap bitmap = ImageHelper.getBitmapFromURI(ViewPhotoActivity.this, uri);

        if (bitmap != null) {
            ImageView iv = findViewById(R.id.viewphoto_image);
            iv.setImageBitmap(bitmap);
            iv.setAdjustViewBounds(true);
        }
    }

    public void viewPhotoBackOnClick(View v) {
        finish();
    }
}
