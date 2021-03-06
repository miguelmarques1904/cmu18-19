package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import pt.ist.cmu.api.ApiService;
import pt.ist.cmu.api.RetrofitInstance;
import pt.ist.cmu.helpers.Constants;
import pt.ist.cmu.models.Album;
import pt.ist.cmu.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAlbumActivity extends AppCompatActivity {

    LinearLayout albumLayout;
    Button backBtn;

    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewalbum);

        backBtn = findViewById(R.id.viewalbum_back);
        albumLayout = findViewById(R.id.viewablum_ll);

        title = findViewById(R.id.viewalbum_title);

        // initialize hawk
        Hawk.init(getApplicationContext()).build();

        // get user from preferences
        User user = Hawk.get(Constants.CURRENT_USER_KEY);

        String token = "Token " + user.getToken();

        // delete current set album, if exists
        if (Hawk.contains(Constants.CURRENT_ALBUM_KEY)) {
            Hawk.delete(Constants.CURRENT_ALBUM_KEY);
        }

        // Call server
        // Add albums to list
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<List<Album>> call = service.getUserAlbums(token);

        call.enqueue(new Callback<List<Album>>() {
            @Override
            public void onResponse(Call<List<Album>> call, Response<List<Album>> response) {
                switch (response.code()) {
                    case 200:
                        // add buttons for each album
                        for (Album a : response.body()) {
                            final String albumName = a.getName();

                            Button btn = new Button(getApplicationContext());
                            btn.setAllCaps(false);
                            btn.setText("Album " + albumName);

                            // send album name on intent
                            btn.setOnClickListener(v -> {
                                Intent manageAlbumIntent = new Intent(ViewAlbumActivity.this, ManageAlbumActivity.class);
                                manageAlbumIntent.putExtra(Constants.CURRENT_ALBUM_KEY, albumName);
                                startActivity(manageAlbumIntent);
                            });

                            albumLayout.addView(btn);
                        }
                        break;
                    case 204:
                        title.setText("You have no albums");
                        break;
                    case 401:
                        Toast.makeText(getApplicationContext(), "Credentials are invalid.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(Call<List<Album>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Something went wrong... Network may be down...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void backOnClick(View v) {
        finish();
    }
}
