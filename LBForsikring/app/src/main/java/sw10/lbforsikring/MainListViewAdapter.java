package sw10.lbforsikring;

import android.content.ClipData;
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
    final static int VIEWTYPE_HISTORICAL = 0;
    final static int VIEWTYPE_ACTIVE = 1;
    Context mContext;
    List<Trip> mTrips;

    public MainListViewAdapter(Context context, List<Trip> trips) {
        super(context, -1, trips);
        mContext = context;
        mTrips = trips;
    }

    static class HistoricalViewHolder {
        public TextView TitleView;
        public TextView DescriptionView;
        public TextView CostView;
        public TextView OptimalityView;
    }

    static class ActiveViewHolder {
        public TextView TitleView;
        public TextView DescriptionView;
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
                    rowView = inflater.inflate(R.layout.listview_main, parent, false);

                    HistoricalViewHolder historicalViewHolder = new HistoricalViewHolder();
                    historicalViewHolder.TitleView = (TextView) rowView.findViewById(R.id.MainListViewTitle);
                    historicalViewHolder.DescriptionView = (TextView) rowView.findViewById(R.id.MainListViewDescription);
                    historicalViewHolder.CostView = (TextView) rowView.findViewById(R.id.MainListViewCost);
                    historicalViewHolder.OptimalityView = (TextView) rowView.findViewById(R.id.MainListViewOptimality);
                    rowView.setTag(historicalViewHolder);
                }

                //Populate ViewHolder with data
                HistoricalViewHolder historicalViewHolder = (HistoricalViewHolder) rowView.getTag();
                historicalViewHolder.TitleView.setText(String.format(mContext.getString(R.string.MainListViewTitle), mTrips.get(position).TripId));
                historicalViewHolder.DescriptionView.setText(mContext.getString(R.string.MainListViewDescription));
                historicalViewHolder.CostView.setText(String.format(mContext.getString(R.string.MainListViewCost), 87.0));
                SetTextColor(historicalViewHolder.OptimalityView, 35);
                historicalViewHolder.OptimalityView.setText(String.format(mContext.getString(R.string.MainListViewOptimality), 35));
                break;

            case VIEWTYPE_ACTIVE:
                //If a ViewHolder does not exist for this view, create it
                if (rowView == null) {
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    rowView = inflater.inflate(R.layout.listview_newtrip_main, parent, false);

                    ActiveViewHolder activeViewHolder = new ActiveViewHolder();
                    activeViewHolder.TitleView = (TextView) rowView.findViewById(R.id.MainListViewTitle);
                    activeViewHolder.DescriptionView = (TextView) rowView.findViewById(R.id.MainListViewDescription);
                    rowView.setTag(activeViewHolder);
                }

                //Populate ViewHolder with data
                ActiveViewHolder activeViewHolder = (ActiveViewHolder) rowView.getTag();
                activeViewHolder.TitleView.setText(String.format(mContext.getString(R.string.MainListViewTitle), mTrips.get(position).TripId));
                activeViewHolder.DescriptionView.setText(mContext.getString(R.string.MainListViewDescription));
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

