package sw10.ubiforsikring;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TripOverviewActivity extends AppCompatActivity {
    Context mContext;
    long mTripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_overview);
        mContext = this;

        //Get trip id for which data to display
        Intent intent = getIntent();
        mTripId = intent.getLongExtra(getString(R.string.TripIdIntentName), -1);

        //Setup buttons
        Button showTripOnMapButton = (Button) findViewById(R.id.ShowTripOnMapButton);
        showTripOnMapButton.setOnClickListener(MapDisplayListener);

        Button tripStatisticsButton = (Button) findViewById(R.id.TripStatisticsButton);
        tripStatisticsButton.setOnClickListener(TripStatisticsListener);

        //Setup text
        TextView tripTitleView = (TextView) findViewById(R.id.TripTitleView);
        TextView tripDescriptionView = (TextView) findViewById(R.id.TripDescriptionView);
        TextView totalCostView = (TextView) findViewById(R.id.TotalCostView);
        TextView baseCostValueView = (TextView) findViewById(R.id.BaseCostValueView);
        TextView environmentCostPercentageView = (TextView) findViewById(R.id.EnvironmentCostPercentageView);
        TextView environmentCostValueView = (TextView) findViewById(R.id.EnvironmentCostValueView);
        TextView drivingStyleCostPercentageView = (TextView) findViewById(R.id.DrivingStyleCostPercentageView);
        TextView drivingStyleCostValueView = (TextView) findViewById(R.id.DrivingStyleCostValueView);
        TextView totalCostPercentageView = (TextView) findViewById(R.id.TotalCostPercentageView);
        TextView totalCostValueView = (TextView) findViewById(R.id.TotalCostValueView);
        TextView tripStartValueView = (TextView) findViewById(R.id.TripStartValueView);
        TextView tripEndValueView = (TextView) findViewById(R.id.TripEndValueView);

        tripTitleView.setText(String.format(getString(R.string.TripTitle), mTripId));
    }

    Button.OnClickListener MapDisplayListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(mContext, MapDisplayActivity.class);
            intent.putExtra(getString(R.string.TripIdIntentName), mTripId);
            startActivity(intent);
        }
    };

    Button.OnClickListener TripStatisticsListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(mContext, TripStatisticsActivity.class);
            intent.putExtra(getString(R.string.TripIdIntentName), mTripId);
            startActivity(intent);
        }
    };
}
