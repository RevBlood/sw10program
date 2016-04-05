package sw10.ubiforsikring;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import sw10.ubiforsikring.Objects.TripObjects.TripListItem;

public class TripListAdapter extends ArrayAdapter<TripListItem> {
    List<TripListItem> mTrips;
    LayoutInflater mInflater;
    Context mContext;
    SimpleDateFormat mSdf;

    public TripListAdapter(Context context, List<TripListItem> trips) {
        super(context, -1, trips);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTrips = trips;

        //Date format for printing out trip timestamps
        mSdf = new SimpleDateFormat(mContext.getString(R.string.TripTimeTextFormat));
    }

    static class ViewHolder {
        public TextView TripTitleView;
        public TextView TripDescriptionView;
        public TextView TripOptimalityView;
        public TextView TripTimeView;
        public TextView TripDistanceView;
        public TextView TripCostView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        //Fetch the relevant item
        TripListItem item = mTrips.get(position);

        //If a ViewHolder does not exist for this view, create it
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.listitem_trip_historical, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.TripTitleView = (TextView) rowView.findViewById(R.id.TripTitleView);
            viewHolder.TripDescriptionView = (TextView) rowView.findViewById(R.id.TripDescriptionView);
            viewHolder.TripOptimalityView = (TextView) rowView.findViewById(R.id.TripOptimalityView);
            viewHolder.TripTimeView = (TextView) rowView.findViewById(R.id.TripTimeView);
            viewHolder.TripDistanceView = (TextView) rowView.findViewById(R.id.TripDistanceView);
            viewHolder.TripCostView = (TextView) rowView.findViewById(R.id.TripCostView);

            rowView.setTag(viewHolder);
        }

        //Populate ViewHolder with data
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.TripTitleView.setText(String.format(mContext.getString(R.string.TripTitle), item.TripId));
        viewHolder.TripDescriptionView.setText("Beskrivelse");
        viewHolder.TripOptimalityView.setText(String.format(mContext.getString(R.string.TripOptimalityText), item.OptimalScore));
        viewHolder.TripTimeView.setText(String.format(mContext.getString(R.string.TripTimeText), mSdf.format(item.TripStart), mSdf.format(item.TripEnd)));
        viewHolder.TripDistanceView.setText(String.format(mContext.getString(R.string.TripDistanceText), item.MetersDriven / 1000));
        SetTextColor(viewHolder.TripOptimalityView, item.OptimalScore);

        return rowView;
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
}

