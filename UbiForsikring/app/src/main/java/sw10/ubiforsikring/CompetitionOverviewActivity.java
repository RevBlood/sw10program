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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_overview);
        mContext = this;

        //Get trip id for which data to display
        Intent intent = getIntent();
        mCompetitionId = intent.getIntExtra(getString(R.string.CompetitionIdIntentName), -1);

        ListView leaderBoard = (ListView) findViewById(R.id.LeaderBoard);
        leaderBoard.setAdapter(mCompetitionAdapter);
    }

    @Override
    public void onResume() {
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
                SharedPreferences preferences = getSharedPreferences(getString(R.string.UserPreferences), Context.MODE_PRIVATE);
                int userId = preferences.getInt(getString(R.string.StoredCarId), -1);

                //mCompetition = ServiceHelper.GetCompetition(userId, competitionId[0]);
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
                }

                // Find all views to update
                TextView competitionTitleView = (TextView) findViewById(R.id.CompetitionTitleView);
                TextView competitionTimeLeftView = (TextView) findViewById(R.id.CompetitionTimeLeftView);
                TextView rankXYView = (TextView) findViewById(R.id.RankXYView);
                TextView personalScoreView = (TextView) findViewById(R.id.PersonalScoreView);
                TextView competitionDescriptionView = (TextView) findViewById(R.id.CompetitionDescriptionView);
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
                .setNegativeButton(getString(R.string.TripOverviewErrorGoBack), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
    }
}
