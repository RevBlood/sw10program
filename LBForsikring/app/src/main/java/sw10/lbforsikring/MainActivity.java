package sw10.lbforsikring;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    Context mContext;
    Boolean mDriving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        Button toggleDrivingButton = (Button) findViewById(R.id.toggleDrivingButton);
        toggleDrivingButton.setOnClickListener(OnToggleDrivingListener);
    }

    Button.OnClickListener OnToggleDrivingListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Button toggleDrivingButton = (Button)findViewById(R.id.toggleDrivingButton);

            if (!mDriving) {
                startService(new Intent(mContext, LocationService.class));
                Log.e("App", "Service Started");
                toggleDrivingButton.setText(R.string.Stop);
                mDriving = true;
            } else {
                stopService(new Intent(mContext, LocationService.class));
                Log.e("App", "Service Stopped");
                toggleDrivingButton.setText(R.string.Start);
                mDriving = false;
            }
        }
    };
}