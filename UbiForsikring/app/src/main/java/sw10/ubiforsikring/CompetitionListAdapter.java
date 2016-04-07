package sw10.ubiforsikring;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import sw10.ubiforsikring.Objects.CompetitionObjects.CompetitionListItem;

public class CompetitionListAdapter extends ArrayAdapter<CompetitionListItem> {
    List<CompetitionListItem> mCompetitions;
    LayoutInflater mInflater;
    Context mContext;

    public CompetitionListAdapter(Context context, List<CompetitionListItem> competitions) {
        super(context, -1, competitions);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCompetitions = competitions;
    }

    static class ViewHolder {
        public TextView CompetitionTitleView;
        public TextView CompetitionDescriptionView;
        public TextView CompetitionRankView;
        public TextView CompetitionTimeLeftView;
        public TextView CompetitionAttemptCountView;
        public Button EnterCompetitionButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        //Fetch the relevant item
        CompetitionListItem item = mCompetitions.get(position);

        //If a ViewHolder does not exist for this view, create it
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.listitem_competition, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.CompetitionTitleView = (TextView) rowView.findViewById(R.id.CompetitionTitleView);
            viewHolder.CompetitionDescriptionView = (TextView) rowView.findViewById(R.id.CompetitionDescriptionView);
            viewHolder.CompetitionRankView = (TextView) rowView.findViewById(R.id.CompetitionRankView);
            viewHolder.CompetitionTimeLeftView = (TextView) rowView.findViewById(R.id.CompetitionTimeLeftView);
            viewHolder.CompetitionAttemptCountView = (TextView) rowView.findViewById(R.id.CompetitionAttemptCountView);
            viewHolder.EnterCompetitionButton = (Button) rowView.findViewById((R.id.EnterCompetitionButton));

            rowView.setTag(viewHolder);
        }

        //Populate ViewHolder with data
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.CompetitionTitleView.setText(item.CompetitionName);
        viewHolder.CompetitionTimeLeftView.setText(mContext.getString(R.string.DefaultText));

        //Alter layout depending on participation status
        if (!item.IsParticipating) {
            viewHolder.CompetitionDescriptionView.setText(String.format(mContext.getString(R.string.CompetitionParticipantsDescription), item.ParticipantCount));
            viewHolder.EnterCompetitionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Ask server to participate
                }
            });
        } else {
            viewHolder.EnterCompetitionButton.setVisibility(View.GONE);
            viewHolder.CompetitionRankView.setVisibility(View.VISIBLE);
            viewHolder.CompetitionAttemptCountView.setVisibility(View.VISIBLE);
            viewHolder.CompetitionAttemptCountView.setText(String.format(mContext.getString(R.string.CompetitionAttemptCountText), item.AttemptCount));

            viewHolder.CompetitionDescriptionView.setText(String.format(mContext.getString(R.string.CompetitionRankDescription), item.Rank, item.ParticipantCount));
            int rankPercentage = ((item.Rank / item.ParticipantCount) * 100);
            viewHolder.CompetitionRankView.setText(String.format(mContext.getString(R.string.CompetitionRankText), rankPercentage));

            SetTextColor(viewHolder.CompetitionRankView, -1);
            SetTextColor(viewHolder.CompetitionDescriptionView, -1);

        }

        return rowView;
    }

    private void SetTextColor(TextView rankView, double rank) {
        //Define the sections of each color
        double sectionSize = 20;

        //Red
        if (rank >= sectionSize * 4) {
            rankView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorRed));
            return;
        }

        //Orange
        if (rank >= sectionSize * 3) {
            rankView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorOrange));
            return;
        }

        //Yellow
        if (rank >= sectionSize * 2) {
            rankView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorYellow));
            return;
        }

        //Lime
        if (rank >= sectionSize * 1) {
            rankView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorLime));
        }

        //Green
        else {
            rankView.setTextColor(ContextCompat.getColor(mContext, R.color.graphColorGreen));
        }
    }
}

