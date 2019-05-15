package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.orhanobut.hawk.Hawk;

import pt.ist.cmu.helpers.Constants;

public class ModeSelectionActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modeselection);
        intent = new Intent(ModeSelectionActivity.this, MainActivity.class);

        // initialize hawk
        Hawk.init(getApplicationContext()).build();
    }

    public void cloudOnClick(View v) {
        //Hawk.deleteAll();
        Hawk.put(Constants.APP_MODE, Constants.APP_MODE_CLOUD);
        startActivity(intent);
    }

    public void wifiOnClick(View v) {
        Hawk.deleteAll();
        Hawk.put(Constants.APP_MODE, Constants.APP_MODE_WIFI_DIRECT);
        startActivity(intent);
    }
}
