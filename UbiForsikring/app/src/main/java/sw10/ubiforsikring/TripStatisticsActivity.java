package sw10.ubiforsikring;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class TripStatisticsActivity extends AppCompatActivity {
    long mTripId;
    ArrayAdapter mTripStatisticsAdapter;
    ArrayList<TripStatisticsEntry> mEntryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_statistics);

        //Get trip id for which data to display
        Intent intent = getIntent();
        mTripId = intent.getLongExtra(getString(R.string.TripIdIntentName), -1);

        //TODO: Remove test data
        mEntryList = new ArrayList<>();
        mEntryList.add(new TripStatisticsEntry(getString(R.string.DrivingStyleText)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.SpeedMetricName),
                14,
                1.6,
                String.format(getString(R.string.SpeedText), 1.2),
                String.format(getString(R.string.DistancePerHundredText), 15.1)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.AccelerationMetricName),
                5,
                0.4,
                String.format(getString(R.string.AccelerationCountText), 3),
                String.format(getString(R.string.CountPerHundredText), 15.5)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.BrakeMetricName),
                11,
                1.2,
                String.format(getString(R.string.AccelerationCountText), 5),
                String.format(getString(R.string.CountPerHundredText), 23.3)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.JerkMetricName),
                7,
                0.5,
                String.format(getString(R.string.AccelerationCountText), 4),
                String.format(getString(R.string.CountPerHundredText), 19.4)));
        mEntryList.add(new TripStatisticsEntry(getString(R.string.EnvironmentText)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.RoadTypeMetricName),
                30,
                2.7,
                String.format(getString(R.string.RoadTypeText), "Villavej"),
                ""));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.CriticalTimeMetricName),
                0,
                0,
                String.format(getString(R.string.CriticalTimeText), "om natten"),
                ""));

        //Setup ListView
        ListView tripStatisticsListView = (ListView) findViewById(R.id.TripStatisticsListView);
        mTripStatisticsAdapter = new TripStatisticsAdapter(this, mEntryList);
        tripStatisticsListView.setAdapter(mTripStatisticsAdapter);

        //Setup text
        TextView tripTitleView = (TextView) findViewById(R.id.TripTitleView);
        TextView tripDescriptionView = (TextView) findViewById(R.id.TripDescriptionView);
        TextView totalCostView = (TextView) findViewById(R.id.TotalCostView);

        tripTitleView.setText(String.format(getString(R.string.TripTitle), mTripId));
    }
}
