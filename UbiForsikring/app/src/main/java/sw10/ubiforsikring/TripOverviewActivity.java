package sw10.ubiforsikring;

import android.content.Context;
import android.content.Intent;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void onResume() {
        SetupPieChart();
        super.onResume();
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

    private void SetupPieChart() {
        //TODO: Get proper data
        //Define data
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(70, 0));
        entries.add(new Entry(20, 0));
        entries.add(new Entry(15, 0));
        entries.add(new Entry(10, 0));
        entries.add(new Entry(10, 0));
        entries.add(new Entry(5, 0));

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
        data.setValueFormatter(new PieValueFormatter());

        //Style the PieChart itself
        PieChart pieChartView = (PieChart) findViewById(R.id.PieChartView);
        pieChartView.animateY(2000, Easing.EasingOption.EaseInQuad);
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
}
