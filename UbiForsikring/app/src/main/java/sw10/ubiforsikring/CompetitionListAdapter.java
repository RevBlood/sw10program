package sw10.ubiforsikring;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import sw10.ubiforsikring.Helpers.ServiceHelper;
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
        final CompetitionListItem item = mCompetitions.get(position);

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
        final ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.CompetitionTitleView.setText(item.CompetitionName);
        //viewHolder.CompetitionTimeLeftView.setText(mContext.getString(R.string.DefaultText));

        //Alter layout depending on participation status
        if (!item.IsParticipating) {
            viewHolder.CompetitionDescriptionView.setText(String.format(mContext.getString(R.string.CompetitionParticipantsDescription), item.ParticipantCount));
            final int competitionId = item.CompetitionId;
            viewHolder.EnterCompetitionButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        SharedPreferences preferences = mContext.getSharedPreferences(mContext.getString(R.string.UserPreferences), Context.MODE_PRIVATE);
                        int userId = preferences.getInt(mContext.getString(R.string.StoredCarId), -1);
                        ServiceHelper.CompetitionSignUp(userId, competitionId);
                        CreateParticipantLayout(viewHolder, item);

                    } catch (Exception e) {
                        ParticipationErrorDialog(viewHolder, item).show();
                    }
                }
            });
        } else {
            CreateParticipantLayout(viewHolder, item);
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

    public AlertDialog ParticipationErrorDialog(final ViewHolder viewHolder, final CompetitionListItem item) {
        return new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.CompetitionParticipateErrorText))
                .setPositiveButton(mContext.getString(R.string.TripListRetryLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            SharedPreferences preferences = mContext.getSharedPreferences(mContext.getString(R.string.UserPreferences), Context.MODE_PRIVATE);
                            int userId = preferences.getInt(mContext.getString(R.string.StoredCarId), -1);
                            ServiceHelper.CompetitionSignUp(userId, item.CompetitionId);
                            CreateParticipantLayout(viewHolder, item);
                        } catch (Exception e) {
                            ParticipationErrorDialog(viewHolder, item).show();
                            dialog.cancel();
                        }
                    }
                })
                .setNegativeButton(mContext.getString(R.string.TripListCancelLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
    }

    private void CreateParticipantLayout(ViewHolder viewHolder, CompetitionListItem item) {
        viewHolder.EnterCompetitionButton.setVisibility(View.GONE);
        viewHolder.CompetitionRankView.setVisibility(View.VISIBLE);
        viewHolder.CompetitionAttemptCountView.setVisibility(View.VISIBLE);
        viewHolder.CompetitionAttemptCountView.setText(String.format(mContext.getString(R.string.CompetitionAttemptCountText), item.AttemptCount));
        //viewHolder.CompetitionDescriptionView.setText(String.format(mContext.getString(R.string.CompetitionRankDescription), item.Rank, item.ParticipantCount));
        //int rankPercentage = ((item.Rank / item.ParticipantCount) * 100);
        //viewHolder.CompetitionRankView.setText(String.format(mContext.getString(R.string.CompetitionRankText), rankPercentage));

        SetTextColor(viewHolder.CompetitionRankView, -1);
        SetTextColor(viewHolder.CompetitionDescriptionView, -1);
    }
}

