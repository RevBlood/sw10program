package sw10.ubiforsikring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import sw10.ubiforsikring.Objects.CompetitionObjects.LeaderBoardEntry;

public class LeaderBoardAdapter extends ArrayAdapter<LeaderBoardEntry> {
    List<LeaderBoardEntry> mRankings;
    LayoutInflater mInflater;
    Context mContext;

    public LeaderBoardAdapter(Context context, List<LeaderBoardEntry> rankings) {
        super(context, -1, rankings);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRankings = rankings;
    }

    static class ViewHolder {
        public TextView RankView;
        public TextView UsernameView;
        public TextView ScoreView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        //Fetch the relevant item
        final LeaderBoardEntry item = mRankings.get(position);

        //If a ViewHolder does not exist for this view, create it
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.listitem_leaderboard, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.RankView = (TextView) rowView.findViewById(R.id.RankView);
            viewHolder.UsernameView = (TextView) rowView.findViewById(R.id.UsernameView);
            viewHolder.ScoreView = (TextView) rowView.findViewById(R.id.ScoreView);

            rowView.setTag(viewHolder);
        }

        //Populate ViewHolder with data
        final ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.RankView.setText(item.Rank);
        viewHolder.UsernameView.setText(item.Username);
        viewHolder.ScoreView.setText(Double.toString(item.Score));

        return rowView;
    }
}


