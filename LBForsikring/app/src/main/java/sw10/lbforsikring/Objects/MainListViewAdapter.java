package sw10.lbforsikring.Objects;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import sw10.lbforsikring.Objects.TripObjects.Trip;
import sw10.lbforsikring.R;

/**
 * Created by treel on 19-02-2016.
 */
public class MainListViewAdapter extends ArrayAdapter<Trip> {
    private Context mContext;
    private List<Trip> mTrips;

    public MainListViewAdapter(Context context, List<Trip> trips) {
        super(context, -1, trips);
        mContext = context;
        mTrips = trips;
    }

    @Override
    public View getView(int location, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listview_main, parent, false);

        TextView titleView = (TextView) rowView.findViewById(R.id.MainListViewTitle);
        TextView descriptionView = (TextView) rowView.findViewById(R.id.MainListViewDescription);
        TextView costView = (TextView) rowView.findViewById(R.id.MainListViewCost);
        TextView optimalityView = (TextView) rowView.findViewById(R.id.MainListViewOptimality);

        titleView.setText(mContext.getString(R.string.MainListViewTitle) + " " + mTrips.get(location).TripId);
        descriptionView.setText(mContext.getString(R.string.MainListViewDescription));
        costView.setText("5000" + mContext.getString(R.string.MainListViewCostCurrency));
        SetTextColor(optimalityView, 35);
        optimalityView.setText("35" + '%');
        return rowView;
    }

    private void SetTextColor(TextView optimalityView, int optimality) {
        //Get the limit where everything is just red
        int maxColor = mContext.getResources().getInteger(R.integer.OptimalityMaxColor);

        //Optimality is red
        if (optimality >= maxColor) {
            optimalityView.setTextColor(Color.rgb(255, 0, 0));
            return;
        }

        //Optimality is more red than green
        if (optimality > maxColor / 2) {
            double partAboveHalf = optimality - (maxColor / 2);
            double half = maxColor / 2;
            double percentage = partAboveHalf / half * 100;
            int green = (int) (255 - (255 * (percentage / 100)));
            optimalityView.setTextColor(Color.rgb(255, green, 0));
            return;
        }

        //Optimality is yellow
        if (optimality == maxColor / 2) {
            optimalityView.setTextColor(Color.rgb(255, 255, 0));
            return;
        }

        //Optimality more green than red
        if (optimality > 0) {
            double half = maxColor / 2;
            double percentage = optimality / half * 100;
            int red = (int) (255 * (percentage / 100));
            optimalityView.setTextColor(Color.rgb(red, 255, 0));
            return;
        }

        //Optimality is green (0)
        optimalityView.setTextColor(Color.rgb(0, 255, 0));
    }
}

