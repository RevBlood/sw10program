package sw10.ubiforsikring;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import sw10.ubiforsikring.Objects.TripObjects.Trip;

public class TripStatisticsActivity extends AppCompatActivity {
    Trip mTrip;
    ArrayAdapter mTripStatisticsAdapter;
    ArrayList<TripStatisticsEntry> mEntryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_statistics);
        mEntryList = new ArrayList<>();

        //Get trip id for which data to display
        Intent intent = getIntent();
        mTrip = (Trip) intent.getSerializableExtra(getString(R.string.TripStatisticsIntent));

        //Setup ListView
        ListView tripStatisticsListView = (ListView) findViewById(R.id.TripStatisticsListView);
        mTripStatisticsAdapter = new TripStatisticsAdapter(this, mEntryList);
        tripStatisticsListView.setAdapter(mTripStatisticsAdapter);

        //Setup text
        TextView tripTitleView = (TextView) findViewById(R.id.TripTitleView);
        TextView tripDescriptionView = (TextView) findViewById(R.id.TripDescriptionView);
        //TextView totalCostView = (TextView) findViewById(R.id.TotalCostView);

        tripTitleView.setText(String.format(getString(R.string.TripTitle), mTrip.LocalTripId));
        tripDescriptionView.setText(TimeStringGenerator.Generate(mTrip.TripEnd.getTime(), this));
        //totalCostView.setText("43,00 Dkr");
        //totalCostView.setTextColor(ContextCompat.getColor(this, R.color.graphColorRed));
    }

    @Override
    public void onResume() {
        double speedPercentage = (mTrip.SpeedingScore / mTrip.MetersDriven ) * 100;
        double accelerationPercentage = (mTrip.AccelerationScore / mTrip.MetersDriven ) * 100;
        double brakePercentage = (mTrip.Brakescore / mTrip.MetersDriven ) * 100;
        double jerkPercentage = (mTrip.Jerkscore / mTrip.MetersDriven ) * 100;
        double roadTypePercentage = (mTrip.RoadtypeScore / mTrip.MetersDriven ) * 100;
        double criticalTimePercentage = (mTrip.CriticalTimeScore / mTrip.MetersDriven ) * 100;

        double distancePerHundred = ((mTrip.MetersSped / mTrip.MetersDriven) * 100 ) / 1000;
        double accelerationsPerHundred = (mTrip.AccelerationCount / (mTrip.MetersDriven / 1000 )) * 100;
        double brakesPerHundred = (mTrip.BrakeCount / (mTrip.MetersDriven / 1000 )) * 100;
        double jerksPerHundred = (mTrip.JerkCount / (mTrip.MetersDriven / 1000 )) * 100;

        mEntryList.add(new TripStatisticsEntry(getString(R.string.DrivingStyleText)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.SpeedMetricName),
                speedPercentage,
                String.format(getString(R.string.SpeedText), mTrip.MetersSped),
                String.format(getString(R.string.DistancePerHundredText), distancePerHundred)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.AccelerationMetricName),
                accelerationPercentage,
                String.format(getString(R.string.AccelerationCountText), mTrip.AccelerationCount),
                String.format(getString(R.string.CountPerHundredText), accelerationsPerHundred)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.BrakeMetricName),
                brakePercentage,
                String.format(getString(R.string.AccelerationCountText), mTrip.BrakeCount),
                String.format(getString(R.string.CountPerHundredText), brakesPerHundred)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.JerkMetricName),
                jerkPercentage,
                String.format(getString(R.string.AccelerationCountText), mTrip.JerkCount),
                String.format(getString(R.string.CountPerHundredText), jerksPerHundred)));
        mEntryList.add(new TripStatisticsEntry(getString(R.string.EnvironmentText)));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.RoadTypeMetricName),
                roadTypePercentage,
                String.format(getString(R.string.RoadTypeText), mTrip.RoadtypeMajority),
                ""));
        mEntryList.add(new TripStatisticsEntry(
                getString(R.string.CriticalTimeMetricName),
                criticalTimePercentage,
                String.format(getString(R.string.CriticalTimeText), mTrip.TimePeriodMajority),
                ""));

        mTripStatisticsAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onPause() {
        mEntryList.clear();
        mTripStatisticsAdapter.notifyDataSetChanged();
        super.onPause();
    }
}