package sw10.lbforsikring;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

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

        if(isServiceRunning(LocationService.class)) {
            Log.i("Debug", "Service already running");
            mDriving = true;
            toggleDrivingButton.setText(R.string.ToggleDrivingStop);
        }
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, LocationService.class));
        Log.i("Debug", "Service stopped on application exit");

        super.onDestroy();
    }

    Button.OnClickListener OnToggleDrivingListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Button toggleDrivingButton = (Button)findViewById(R.id.toggleDrivingButton);

            //If user was not driving, reset Logcat and start the location service - otherwise stop it
            if (!mDriving) {
                ClearLogCat();
                startService(new Intent(mContext, LocationService.class));
                Log.i("Debug", "Service Started");
                toggleDrivingButton.setText(R.string.ToggleDrivingStop);
                mDriving = true;
            } else {
                stopService(new Intent(mContext, LocationService.class));
                Log.i("Debug", "Service Stopped");
                toggleDrivingButton.setText(R.string.ToggleDrivingStart);
                mDriving = false;

                File file = GetFilePath();
                ArrayList<String> log = ReadLogCat();
                WriteLogCat(file, log);

                Log.i("Debug", "Wrote logfile to" + file.getAbsolutePath());
            }
        }
    };

    private ArrayList<String> ReadLogCat() {
        ArrayList<String> lines = new ArrayList<>();

        try {
            Process process = Runtime.getRuntime().exec("logcat -d -v time Debug:v *:S");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            Log.e("Debug", "Failed to read Logcat");
        }
        return lines;
    }

    private void WriteLogCat(File file, ArrayList<String> log) {
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            for(String line : log) {
                osw.write(line + "\n");
            }

            osw.flush();
            osw.close();
        } catch (IOException e) {
            Log.e("Debug", "Unable to write Logcat to file");
        }

        //Tell the system a new file exists - Otherwise a computer might not see it
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    private void ClearLogCat() {
        try {
            new ProcessBuilder()
                .command("logcat", "-c")
                .redirectErrorStream(true)
                .start();
        } catch (IOException e) {
            Log.e("Debug", "Failed to clear Logcat");
        }
    }

    private File GetFilePath(){
        //Save file in folder in downloads
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        dir = new File (dir.getAbsolutePath() + R.string.LogFolder);
        dir.mkdirs();

        //Get timestamp
        Long timestamp = System.currentTimeMillis()/1000;

        //Add filename to dir and return
        return new File(dir, R.string.LogFilename + timestamp.toString() + R.string.LogFiletype);
    }

    //Method found on http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}