package sw10.ubiforsikring;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import sw10.ubiforsikring.Objects.TripObjects.Trip;

/**
 * Created by treel on 19-02-2016.
 */
public class TripOverviewAdapter extends ArrayAdapter<Trip> {
    final static int VIEWTYPE_HISTORICAL = 0;
    final static int VIEWTYPE_ACTIVE = 1;
    Context mContext;
    List<Trip> mTrips;

    public TripOverviewAdapter(Context context, List<Trip> trips) {
        super(context, -1, trips);
        mContext = context;
        mTrips = trips;
    }

    static class HistoricalViewHolder {
        public TextView TripTitleView;
        public TextView TripDescriptionView;
        public TextView TripOptimalityView;
        public TextView TripTimeView;
        public TextView TripDistanceView;
        public TextView TripCostView;
    }

    static class ActiveViewHolder {
        public TextView NewTripTitleView;
        public TextView NewTripDistanceView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        //Layout depends on whether the trip is active or historical
        switch (this.getItemViewType(position)) {
            case VIEWTYPE_HISTORICAL:
                //If a ViewHolder does not exist for this view, create it
                if (rowView == null) {
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    rowView = inflater.inflate(R.layout.listitem_trip_overview, parent, false);

                    HistoricalViewHolder historicalViewHolder = new HistoricalViewHolder();
                    historicalViewHolder.TripTitleView = (TextView) rowView.findViewById(R.id.TripTitleView);
                    historicalViewHolder.TripDescriptionView = (TextView) rowView.findViewById(R.id.TripDescriptionView);
                    historicalViewHolder.TripOptimalityView = (TextView) rowView.findViewById(R.id.TripOptimalityView);
                    historicalViewHolder.TripTimeView = (TextView) rowView.findViewById(R.id.TripTimeView);
                    historicalViewHolder.TripDistanceView = (TextView) rowView.findViewById(R.id.TripDistanceView);
                    historicalViewHolder.TripCostView = (TextView) rowView.findViewById(R.id.TripCostView);

                    rowView.setTag(historicalViewHolder);
                }

                //Populate ViewHolder with data
                HistoricalViewHolder historicalViewHolder = (HistoricalViewHolder) rowView.getTag();
                historicalViewHolder.TripTitleView.setText(String.format(mContext.getString(R.string.TripTitle), mTrips.get(position).TripId));
                historicalViewHolder.TripDescriptionView.setText(mContext.getString(R.string.DefaultText));
                historicalViewHolder.TripOptimalityView.setText(String.format(mContext.getString(R.string.TripOptimalityText), 8));
                historicalViewHolder.TripTimeView.setText(String.format(mContext.getString(R.string.TripTimeText), "A", "B"));
                historicalViewHolder.TripDistanceView.setText(String.format(mContext.getString(R.string.TripDistanceText), 9.7));
                historicalViewHolder.TripCostView.setText(String.format(mContext.getString(R.string.TripCostText), 14.1));
                SetTextColor(historicalViewHolder.TripOptimalityView, 8);
                break;

            case VIEWTYPE_ACTIVE:
                //If a ViewHolder does not exist for this view, create it
                if (rowView == null) {
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    rowView = inflater.inflate(R.layout.listitem_new_trip_overview, parent, false);

                    ActiveViewHolder activeViewHolder = new ActiveViewHolder();
                    activeViewHolder.NewTripTitleView = (TextView) rowView.findViewById(R.id.NewTripTitleView);
                    activeViewHolder.NewTripDistanceView = (TextView) rowView.findViewById(R.id.NewTripDistanceView);
                    rowView.setTag(activeViewHolder);
                }

                //Populate ViewHolder with data
                ActiveViewHolder activeViewHolder = (ActiveViewHolder) rowView.getTag();
                activeViewHolder.NewTripTitleView.setText(mContext.getString(R.string.NewTripTitle));
                activeViewHolder.NewTripDistanceView.setText(String.format(mContext.getString(R.string.NewTripDistanceText), 10.7));
                break;
        }

        return rowView;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTrips.get(position).IsActive) {
            return VIEWTYPE_ACTIVE;
        } else {
            return VIEWTYPE_HISTORICAL;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private void SetTextColor(TextView optimalityView, int optimality) {
        //Get the limit where everything is just red
        int maxColor = mContext.getResources().getInteger(R.integer.OptimalityMaxColor);

        //Define the sections of each color
        double sectionSize = maxColor / 5;

        //Red
        if (optimality >= sectionSize * 4) {
            optimalityView.setTextColor(Color.rgb(255, 0, 0));
            return;
        }

        //Orange
        if (optimality >= sectionSize * 3) {
            optimalityView.setTextColor(Color.rgb(255, 165, 0));
            return;
        }

        //Yellow
        if (optimality >= sectionSize * 2) {
            optimalityView.setTextColor(Color.rgb(255, 255, 0));
            return;
        }

        //Chartreuse
        if (optimality >= sectionSize * 1) {
            optimalityView.setTextColor(Color.rgb(165, 255, 0));
        }

        //Green
        else {
            optimalityView.setTextColor(Color.rgb(0, 255, 0));
        }
    }
}

