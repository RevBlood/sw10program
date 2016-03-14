package sw10.ubiforsikring;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class TripListAdapter extends ArrayAdapter<TripListEntry> {
    final static int VIEWTYPE_HISTORICAL = 0;
    final static int VIEWTYPE_CURRENT = 1;

    List<TripListEntry> mTrips;
    LayoutInflater mInflater;
    Context mContext;
    SimpleDateFormat mSdf;

    public TripListAdapter(Context context, List<TripListEntry> trips) {
        super(context, -1, trips);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTrips = trips;

        //Date format for printing out trip timestamps
        mSdf = new SimpleDateFormat(mContext.getString(R.string.TripTimeTextFormat));
    }

    static class HistoricalViewHolder {
        public TextView TripTitleView;
        public TextView TripDescriptionView;
        public TextView TripOptimalityView;
        public TextView TripTimeView;
        public TextView TripDistanceView;
        public TextView TripCostView;
    }

    static class CurrentViewHolder {
        public TextView CurrentTripTitleView;
        public TextView CurrentTripDescriptionView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        //Fetch the relevant entry
        TripListEntry entry = mTrips.get(position);

        //Layout depends on whether the trip is active or historical
        switch (this.getItemViewType(position)) {
            case VIEWTYPE_HISTORICAL:
                //If a ViewHolder does not exist for this view, create it
                if (rowView == null) {
                    rowView = mInflater.inflate(R.layout.listitem_trip_historical, parent, false);

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
                historicalViewHolder.TripTitleView.setText(String.format(mContext.getString(R.string.TripTitle), entry.TripId));
                historicalViewHolder.TripDescriptionView.setText(mContext.getString(R.string.DefaultText));
                historicalViewHolder.TripOptimalityView.setText(String.format(mContext.getString(R.string.TripOptimalityText), entry.Optimality));
                historicalViewHolder.TripTimeView.setText(String.format(mContext.getString(R.string.TripTimeText), mSdf.format(entry.TimeStarted), mSdf.format(entry.TimeEnded)));
                historicalViewHolder.TripDistanceView.setText(String.format(mContext.getString(R.string.TripDistanceText), entry.Distance / 1000));
                historicalViewHolder.TripCostView.setText(String.format(mContext.getString(R.string.TripCostText), entry.Cost));
                SetTextColor(historicalViewHolder.TripOptimalityView, entry.Optimality);
                break;

            case VIEWTYPE_CURRENT:
                //If a ViewHolder does not exist for this view, create it
                if (rowView == null) {
                    rowView = mInflater.inflate(R.layout.listitem_trip_current, parent, false);

                    CurrentViewHolder currentViewHolder = new CurrentViewHolder();
                    currentViewHolder.CurrentTripTitleView = (TextView) rowView.findViewById(R.id.CurrentTripTitleView);
                    currentViewHolder.CurrentTripDescriptionView = (TextView) rowView.findViewById(R.id.CurrentTripDescriptionView);
                    rowView.setTag(currentViewHolder);
                }

                //Populate ViewHolder with data
                CurrentViewHolder currentViewHolder = (CurrentViewHolder) rowView.getTag();
                currentViewHolder.CurrentTripTitleView.setText(mContext.getString(R.string.CurrentTripTitle));
                if (mTrips.get(position).IsProcessing) {
                    currentViewHolder.CurrentTripDescriptionView.setText(mContext.getString(R.string.CurrentTripProcessingText));
                } else {
                    currentViewHolder.CurrentTripDescriptionView.setText(String.format(mContext.getString(R.string.CurrentTripDistanceText), entry.Distance / 1000));
                }
                    break;
        }

        return rowView;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTrips.get(position).IsActive || mTrips.get(position).IsProcessing) {
            return VIEWTYPE_CURRENT;
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

