package pt.ist.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ModeSelectionActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modeselection);
        intent = new Intent(ModeSelectionActivity.this, MainActivity.class);
    }

    public void cloudOnClick(View v) {
        intent.putExtra("mode", MainActivity.MODE_CLOUD);
        startActivity(intent);
    }

    public void wifiOnClick(View v) {
        intent.putExtra("mode", MainActivity.MODE_WIFI_DIRECT);
        startActivity(intent);
    }
}
