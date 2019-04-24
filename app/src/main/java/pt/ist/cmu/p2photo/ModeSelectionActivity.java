package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ModeSelectionActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modeselection);

        intent = getIntent();
    }


    public void cloudOnClick(View v) {
        intent.putExtra("mode", MainActivity.MODE_CLOUD);
        setResult(RESULT_OK, intent);
        finish();

    }

    public void wifiOnClick(View v) {
        intent.putExtra("mode", MainActivity.MODE_WIFI_DIRECT);
        setResult(RESULT_OK, intent);
        finish();

    }
}
