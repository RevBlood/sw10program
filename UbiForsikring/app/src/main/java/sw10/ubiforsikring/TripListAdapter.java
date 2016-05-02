package sw10.ubiforsikring;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
        public ImageView TripSmileyView;
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
            rowView = mInflater.inflate(R.layout.listitem_trip, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.TripTitleView = (TextView) rowView.findViewById(R.id.TripTitleView);
            viewHolder.TripDescriptionView = (TextView) rowView.findViewById(R.id.TripDescriptionView);
            viewHolder.TripSmileyView = (ImageView) rowView.findViewById(R.id.TripSmileyView);
            viewHolder.TripTimeView = (TextView) rowView.findViewById(R.id.TripTimeView);
            viewHolder.TripDistanceView = (TextView) rowView.findViewById(R.id.TripDistanceView);
            viewHolder.TripCostView = (TextView) rowView.findViewById(R.id.TripCostView);

            rowView.setTag(viewHolder);
        }

        //Populate ViewHolder with data
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.TripTitleView.setText(String.format(mContext.getString(R.string.TripTitle), item.LocalTripId));
        viewHolder.TripDescriptionView.setText(TimeStringGenerator.Generate(item.TripEnd.getTime(), mContext));
        viewHolder.TripTimeView.setText(String.format(mContext.getString(R.string.TripTimeText), mSdf.format(item.TripStart), mSdf.format(item.TripEnd)));
        if (item.MetersDriven == 0) {
            viewHolder.TripDistanceView.setText(String.format(mContext.getString(R.string.TripDistanceText), 0.0));
        } else {
            viewHolder.TripDistanceView.setText(String.format(mContext.getString(R.string.TripDistanceText), item.MetersDriven / 1000));
        }

        SetSmileyFace(viewHolder.TripSmileyView, item.ScorePercentage);

        return rowView;
    }

    private void SetSmileyFace(ImageView tripSmileyView, double optimality) {
        // Get threshold values
        int thresholdOne = mContext.getResources().getInteger(R.integer.SmileyOneThreshold);
        int thresholdTwo = mContext.getResources().getInteger(R.integer.SmileyTwoThreshold);
        int thresholdThree = mContext.getResources().getInteger(R.integer.SmileyThreeThreshold);
        int thresholdFour = mContext.getResources().getInteger(R.integer.SmileyFourThreshold);

        // Set smiley face dependant on optimality
        if (optimality <= thresholdOne) {
            tripSmileyView.setImageResource(R.drawable.smiley_one);
            return;
        }

        if (optimality <= thresholdTwo) {
            tripSmileyView.setImageResource(R.drawable.smiley_two);
            return;
        }

        if (optimality <= thresholdThree) {
            tripSmileyView.setImageResource(R.drawable.smiley_three);
            return;
        }

        if (optimality <= thresholdFour) {
            tripSmileyView.setImageResource(R.drawable.smiley_four);
            return;
        }

        // If optimality is not included in any of the above thresholds, just set it to the worst threshold
        tripSmileyView.setImageResource(R.drawable.smiley_five);
    }
}

