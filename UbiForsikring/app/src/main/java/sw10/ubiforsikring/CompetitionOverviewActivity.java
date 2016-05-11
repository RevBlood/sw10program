package sw10.ubiforsikring;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.CompetitionObjects.Competition;
import sw10.ubiforsikring.Objects.CompetitionObjects.LeaderBoardEntry;

public class CompetitionOverviewActivity extends AppCompatActivity {
    Context mContext;
    int mCompetitionId;
    Competition mCompetition;
    List<LeaderBoardEntry> mRankings;
    ArrayAdapter<LeaderBoardEntry> mCompetitionAdapter;
    SimpleDateFormat mSdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_overview);
        mContext = this;
        mSdf = new SimpleDateFormat(mContext.getString(R.string.CompetitionTimeLeftFormat));

        //Get trip id for which data to display
        Intent intent = getIntent();
        mCompetitionId = intent.getIntExtra(getString(R.string.CompetitionIdIntentName), -1);

        ListView leaderBoard = (ListView) findViewById(R.id.LeaderBoard);
        mRankings = new ArrayList<>();
        mCompetitionAdapter = new LeaderBoardAdapter(this, mRankings);
        leaderBoard.setAdapter(mCompetitionAdapter);
    }

    @Override
    public void onResume() {
        // Clear leaderboard
        mRankings.clear();

        //Get data for GUI
        CompetitionGetTask competitionGetTask = new CompetitionGetTask(this);
        competitionGetTask.execute(mCompetitionId);

        super.onResume();
    }

    private class CompetitionGetTask extends AsyncTask<Integer, Void, Boolean> {
        final WeakReference<Context> mContextReference;

        public CompetitionGetTask(Context context) {
            mContextReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Integer... competitionId) {
            try {
                SharedPreferences preferences = getSharedPreferences(getString(R.string.SW10Preferences), Context.MODE_PRIVATE);
                int userId = preferences.getInt(getString(R.string.StoredCarId), -1);
                mCompetition = ServiceHelper.GetCompetitionForOverview(competitionId[0], userId);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mContextReference.get() != null) {
                if(!success) {
                    CompetitionLoadErrorDialog().show();
                    return;
                }

                // Find all views to update
                TextView competitionTitleView = (TextView) findViewById(R.id.CompetitionTitleView);
                TextView competitionTimeLeftView = (TextView) findViewById(R.id.CompetitionTimeLeftView);
                TextView rankXYView = (TextView) findViewById(R.id.RankXYView);
                TextView personalScoreView = (TextView) findViewById(R.id.PersonalScoreView);
                TextView competitionDescriptionView = (TextView) findViewById(R.id.CompetitionDescriptionView);

                competitionTitleView.setText(mCompetition.Name);

                String endDate = mSdf.format(mCompetition.EndDate);
                competitionTimeLeftView.setText(String.format(getString(R.string.CompetitionTimeLeftText), endDate));
                rankXYView.setText(String.format(getString(R.string.CompetitionOverviewRankText), mCompetition.Rank, mCompetition.ParticipantCount));
                personalScoreView.setText(String.format(getString(R.string.CompetitionOverviewAvgScore), mCompetition.PersonalScore));
                competitionDescriptionView.setText(mCompetition.Description);

                mRankings.addAll(mCompetition.LeaderBoardEntries);
                mCompetitionAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private AlertDialog CompetitionLoadErrorDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.TripOverviewLoadErrorText))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.TripListRetryLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CompetitionGetTask competitionGetTask = new CompetitionGetTask(mContext);
                        competitionGetTask.execute(mCompetitionId);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.DialogBack), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
    }
}
