package sw10.ubiforsikring;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class TripStatisticsAdapter extends ArrayAdapter<TripStatisticsEntry> {

    static final int VIEWTYPE_SECTION = 0;
    static final int VIEWTYPE_ITEM = 1;

    ArrayList<TripStatisticsEntry> mEntries = new ArrayList<>();
    LayoutInflater mInflater;
    Context mContext;

    public TripStatisticsAdapter(Context context, ArrayList<TripStatisticsEntry> entries) {
        super(context, -1, entries);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mEntries = entries;
    }

    static class HeaderViewHolder {
        public TextView DelinquencySectionView;
    }

    static class ItemViewHolder {
        public TextView DelinquencyTitleView;
        public TextView DelinquencyDescriptionView;
        public TextView DelinquencyOptimalityView;
        public TextView DelinquenciesPerHundredView;
        public TextView DelinquencyCostView;
    }



    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        //Fetch the relevant entry
        TripStatisticsEntry entry = mEntries.get(position);

        //Layout depends on whether entry is a section or an item
        switch (getItemViewType(position)) {
            case VIEWTYPE_SECTION:
                //If a ViewHolder does not exist for this view, create it
                if (rowView == null) {
                    rowView = mInflater.inflate(R.layout.listitem_delinquency_section, parent, false);

                    HeaderViewHolder headerViewHolder = new HeaderViewHolder();
                    headerViewHolder.DelinquencySectionView = (TextView) rowView.findViewById(R.id.DelinquencySectionText);

                    rowView.setTag(headerViewHolder);
                }

                //Populate ViewHolder with data
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) rowView.getTag();
                headerViewHolder.DelinquencySectionView.setText(entry.Title);

                break;

            case VIEWTYPE_ITEM:
                //If a ViewHolder does not exist for this view, create it
                if (rowView == null) {
                    rowView = mInflater.inflate(R.layout.listitem_delinquency, parent, false);

                    ItemViewHolder itemViewHolder = new ItemViewHolder();
                    itemViewHolder.DelinquencyTitleView = (TextView) rowView.findViewById(R.id.DelinquencyTitle);
                    itemViewHolder.DelinquencyDescriptionView = (TextView) rowView.findViewById(R.id.DelinquencyDescription);
                    itemViewHolder.DelinquencyOptimalityView = (TextView) rowView.findViewById(R.id.DelinquencyOptimality);
                    itemViewHolder.DelinquenciesPerHundredView = (TextView) rowView.findViewById(R.id.DelinquenciesPerHundred);
                    //itemViewHolder.DelinquencyCostView = (TextView) rowView.findViewById(R.id.DelinquencyCost);
                    rowView.setTag(itemViewHolder);
                }

                //Populate ViewHolder with data
                ItemViewHolder itemViewHolder = (ItemViewHolder) rowView.getTag();
                itemViewHolder.DelinquencyTitleView.setText(entry.Title);
                itemViewHolder.DelinquencyDescriptionView.setText(entry.DescriptionText);
                itemViewHolder.DelinquencyOptimalityView.setText(String.format(mContext.getString(R.string.DelinquencyOptimality), entry.Optimality));
                itemViewHolder.DelinquenciesPerHundredView.setText(entry.PerHundredText);
                //itemViewHolder.DelinquencyCostView.setText(String.format(mContext.getString(R.string.DelinquencyCost), entry.Price));

                break;
        }

        return rowView;
    }

    @Override
    public int getItemViewType(int position) {
        if (mEntries.get(position).IsSection) {
            return VIEWTYPE_SECTION;
        } else {
            return VIEWTYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

}