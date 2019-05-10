package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class ViewAlbumActivity extends AppCompatActivity {

    LinearLayout albumLl;
    Button backBtn;

    List<String> albumList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewalbum);

        backBtn = (Button) findViewById(R.id.viewalbum_back);
        albumLl = (LinearLayout) findViewById(R.id.viewablum_ll);


        //TODO request user albums to server
        for(int i=1; i<=20; i++) {
            albumList.add("album" + i);
        }

        for(int i = 0; i < albumList.size(); i++) {
            final String albumName = albumList.get(i);
            Button btn = new Button(getApplicationContext());
            btn.setAllCaps(false);
            btn.setText(albumName);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent manageAlbumIntent = new Intent(ViewAlbumActivity.this, ManageAlbumActivity.class);
                    manageAlbumIntent.putExtra("albumName", albumName);
                    startActivity(manageAlbumIntent);
                }
            });

            albumLl.addView(btn);
        }




    }

    public void backOnClick(View v) {
        finish();
    }
}
