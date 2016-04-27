package sw10.ubiforsikring;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.TripObjects.Trip;

public class TripOverviewActivity extends AppCompatActivity {
    Context mContext;
    long mTripId;
    Trip mTrip;
    SimpleDateFormat mSdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_overview);
        mContext = this;

        //Date format for printing out trip timestamps
        mSdf = new SimpleDateFormat(mContext.getString(R.string.TripPeriodTextFormat));

        //Get trip id for which data to display
        Intent intent = getIntent();
        mTripId = intent.getLongExtra(getString(R.string.TripOverviewIntent), -1);

        //Setup buttons
        Button showTripOnMapButton = (Button) findViewById(R.id.ShowTripOnMapButton);
        showTripOnMapButton.setOnClickListener(MapDisplayListener);

        Button tripStatisticsButton = (Button) findViewById(R.id.TripStatisticsButton);
        tripStatisticsButton.setOnClickListener(TripStatisticsListener);
    }

    @Override
    public void onResume() {
        //Get data for GUI
        OverviewGetTask overviewGetTask = new OverviewGetTask(this);
        overviewGetTask.execute(mTripId);

        super.onResume();
    }

    Button.OnClickListener MapDisplayListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(mContext, MapDisplayActivity.class);
            intent.putExtra(getString(R.string.MapDisplayIntent), mTripId);
            startActivity(intent);
        }
    };

    Button.OnClickListener TripStatisticsListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(mContext, TripStatisticsActivity.class);
            intent.putExtra(getString(R.string.TripStatisticsIntent), mTrip);
            startActivity(intent);
        }
    };

    private void SetupPieChart() {
        //Define data
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry((float)mTrip.SpeedingScore, 0));
        entries.add(new Entry((float)mTrip.AccelerationScore, 0));
        entries.add(new Entry((float)mTrip.Brakescore, 0));
        entries.add(new Entry((float)mTrip.Jerkscore, 0));
        entries.add(new Entry((float)mTrip.RoadtypeScore, 0));
        entries.add(new Entry((float)mTrip.CriticalTimeScore, 0));

        //Define labels for legend
        ArrayList<String> labels = new ArrayList<>();
        labels.add(getString(R.string.SpeedMetricName));
        labels.add(getString(R.string.AccelerationMetricName));
        labels.add(getString(R.string.BrakeMetricName));
        labels.add(getString(R.string.JerkMetricName));
        labels.add(getString(R.string.RoadTypeMetricName));
        labels.add(getString(R.string.CriticalTimeMetricName));

        //Create dataset from entries and style it
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setValueTextSize(18);
        dataSet.setColors(GetGraphColorArray());

        //Combine the dataSet with labels and style it
        PieData data = new PieData(labels, dataSet);

        final double totalTripAddition = mTrip.AccelerationScore + mTrip.Brakescore + mTrip.Jerkscore + mTrip.SpeedingScore + mTrip.CriticalTimeScore + mTrip.RoadtypeScore;
        data.setValueFormatter(new PieValueFormatter(){
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                // Handle case where entry is less than a percent. Everything else is beautiful
                if ((value / totalTripAddition) < 0.5) {
                    return "";
                } else {
                    return super.getFormattedValue(value, entry, dataSetIndex, viewPortHandler);
                }
            }
        });

        //Style the PieChart itself
        PieChart pieChartView = (PieChart) findViewById(R.id.PieChartView);
        pieChartView.animateY(2000, Easing.EasingOption.EaseInQuad);
        pieChartView.setTouchEnabled(false);
        pieChartView.setDrawHoleEnabled(false);
        pieChartView.setDrawSliceText(false);
        pieChartView.setUsePercentValues(true);
        pieChartView.setDescription("");
        pieChartView.setDescriptionTextSize(18);

        //Style the legend of the PieChart
        Legend legend = pieChartView.getLegend();
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
        legend.setTextSize(18);
        legend.setFormSize(18);

        //Create the PieChart
        pieChartView.setData(data);
    }

    public class PieValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        public PieValueFormatter() {
            mFormat = new DecimalFormat("##0");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value) + "%";
        }
    }

    private int[] GetGraphColorArray() {
        int[] graphColors = new int[6];
        graphColors[0] = ContextCompat.getColor(this, R.color.graphColorRed);
        graphColors[1] = ContextCompat.getColor(this, R.color.graphColorPurple);
        graphColors[2] = ContextCompat.getColor(this, R.color.graphColorYellow);
        graphColors[3] = ContextCompat.getColor(this, R.color.graphColorGreen);
        graphColors[4] = ContextCompat.getColor(this, R.color.graphColorBrown);
        graphColors[5] = ContextCompat.getColor(this, R.color.graphColorBlue);
        return graphColors;
    }

    private class OverviewGetTask extends AsyncTask<Long, Void, Boolean> {
        final WeakReference<Context> mContextReference;

        public OverviewGetTask(Context context) {
            mContextReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Long... tripId) {
            try {
                SharedPreferences preferences = getSharedPreferences(getString(R.string.UserPreferences), Context.MODE_PRIVATE);
                int userId = preferences.getInt(getString(R.string.StoredCarId), -1);

                mTrip = ServiceHelper.GetTrip(userId, tripId[0]);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (mContextReference.get() != null) {
                if(!success) {
                    BuildAlertDialog().show();
                }

                // Find all views
                TextView tripTitleView = (TextView) findViewById(R.id.TripTitleView);
                TextView tripDescriptionView = (TextView) findViewById(R.id.TripDescriptionView);
                //TextView totalCostView = (TextView) findViewById(R.id.TotalCostView);
                TextView baseCostValueView = (TextView) findViewById(R.id.BaseCostValueView);
                TextView environmentCostPercentageView = (TextView) findViewById(R.id.EnvironmentCostPercentageView);
                TextView environmentCostValueView = (TextView) findViewById(R.id.EnvironmentCostValueView);
                TextView drivingStyleCostPercentageView = (TextView) findViewById(R.id.DrivingStyleCostPercentageView);
                TextView drivingStyleCostValueView = (TextView) findViewById(R.id.DrivingStyleCostValueView);
                TextView totalCostPercentageView = (TextView) findViewById(R.id.TotalCostPercentageView);
                TextView totalCostValueView = (TextView) findViewById(R.id.TotalCostValueView);
                TextView tripStartValueView = (TextView) findViewById(R.id.TripStartValueView);
                TextView tripEndValueView = (TextView) findViewById(R.id.TripEndValueView);

                // Trip Title
                tripTitleView.setText(String.format(getString(R.string.TripTitle), mTrip.LocalTripId));
                tripDescriptionView.setText(TimeStringGenerator.Generate(mTrip.TripEnd.getTime(), mContext));
                // totalCostView.setText("43,00 Dkr");
                // totalCostView.setTextColor(ContextCompat.getColor(mContextReference.get(), R.color.graphColorRed));

                // Base Cost
                baseCostValueView.setText(String.format(getString(R.string.BaseCostValue), mTrip.MetersDriven / 1000));

                // Environment
                double environmentCost = (mTrip.CriticalTimeScore + mTrip.RoadtypeScore) / 1000;
                double environmentPercentage;
                if (mTrip.MetersDriven == 0) {
                    environmentPercentage = 0.0;
                } else {
                    environmentPercentage = (environmentCost / (mTrip.MetersDriven / 1000)) * 100;
                }

                if (environmentCost >= 0) {
                    environmentCostValueView.setText(String.format(getString(R.string.CostPlusValue), environmentCost));
                    environmentCostPercentageView.setText(String.format(getString(R.string.CostPlusPercentage), environmentPercentage));
                } else {
                    environmentCostValueView.setText(String.format(getString(R.string.CostMinusValue), environmentCost));
                    environmentCostPercentageView.setText(String.format(getString(R.string.CostMinusPercentage), environmentPercentage));
                }

                // Driving Style
                double drivingStyleCost = (mTrip.AccelerationScore +
                                          mTrip.Brakescore +
                                          mTrip.Jerkscore +
                                          mTrip.SpeedingScore) / 1000;
                double drivingStylePercentage;
                if (mTrip.MetersDriven == 0) {
                    drivingStylePercentage = 0.0;
                } else {
                    drivingStylePercentage = (drivingStyleCost / (mTrip.MetersDriven / 1000)) * 100;
                }

                SetTextColor(drivingStyleCostPercentageView, drivingStylePercentage);
                if (drivingStyleCost >= 0) {
                    drivingStyleCostValueView.setText(String.format(getString(R.string.CostPlusValue), drivingStyleCost));
                    drivingStyleCostPercentageView.setText(String.format(getString(R.string.CostPlusPercentage), drivingStylePercentage));
                } else {
                    drivingStyleCostValueView.setText(String.format(getString(R.string.CostMinusValue), drivingStyleCost));
                    drivingStyleCostPercentageView.setText(String.format(getString(R.string.CostMinusPercentage), drivingStylePercentage));
                }

                // Total Cost
                double totalCost = (mTrip.MetersDriven / 1000) + (environmentCost + drivingStyleCost);

                double totalCostPercentage;
                if (mTrip.MetersDriven == 0) {
                    totalCostPercentage = 0.0;
                } else {
                    totalCostPercentage = environmentPercentage + drivingStylePercentage;
                }

                if (totalCostPercentage >= 0) {
                    totalCostValueView.setText(String.format(getString(R.string.BaseCostValue), totalCost));
                    totalCostPercentageView.setText(String.format(getString(R.string.CostPlusPercentage), totalCostPercentage));
                } else {
                    totalCostValueView.setText(String.format(getString(R.string.CostMinusValue), totalCost));
                    totalCostPercentageView.setText(String.format(getString(R.string.CostMinusPercentage), totalCostPercentage));
                }

                // Timestamps
                tripStartValueView.setText(mSdf.format(mTrip.TripStart));
                tripEndValueView.setText(mSdf.format(mTrip.TripEnd));

                // PieChart
                SetupPieChart();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private void SetTextColor(TextView optimalityView, double optimality) {
        //Get the limit where everything is just red
        int maxColor = mContext.getResources().getInteger(R.integer.OptimalityMaxColor);

        //Define the sections of each color
        double sectionSize = maxColor / 5;

        //Red
        if (optimality >= sectionSize * 4) {
            optimalityView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorRed));
            return;
        }

        //Orange
        if (optimality >= sectionSize * 3) {
            optimalityView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorOrange));
            return;
        }

        //Yellow
        if (optimality >= sectionSize * 2) {
            optimalityView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorYellow));
            return;
        }

        //Lime
        if (optimality >= sectionSize * 1) {
            optimalityView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorLime));
        }

        //Green
        else {
            optimalityView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorGreen));
        }
    }

    private AlertDialog BuildAlertDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.TripOverviewLoadErrorText))
                .setPositiveButton(getString(R.string.TripListRetryLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        OverviewGetTask overviewGetTask = new OverviewGetTask(mContext);
                        overviewGetTask.execute(mTripId);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.TripOverviewErrorGoBack), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
    }
}