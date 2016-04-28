package sw10.ubiforsikring;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.CompetitionObjects.CompetitionListItem;

public class CompetitionListActivity extends AppCompatActivity {
    Context mContext;
    View mFooterView;
    int mIndex;

    //Competition list
    ArrayAdapter mCompetitionListAdapter;
    List<CompetitionListItem> mCompetitionList;
    ListView mCompetitionListView;

    //region ACTIVITY EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_list);
        mContext = this;
        mCompetitionList = new ArrayList<>();
        mFooterView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listitem_footer, null, false);

        //Setup ListView
        mCompetitionListView = (ListView) findViewById(R.id.CompetitionListView);
        mCompetitionListAdapter = new CompetitionListAdapter(this, mCompetitionList);
        mCompetitionListView.setAdapter(mCompetitionListAdapter);
        mCompetitionListView.setOnItemClickListener(CompetitionClickListener);
        mCompetitionListView.setOnScrollListener(new ListViewScrollListener(this, getResources().getInteger(R.integer.LoadMoreThreshold), getResources().getInteger(R.integer.ChunkSize)) {
            @Override public void GetMoreEntries(int index) {
                CompetitionGetTask competitionGetTask = new CompetitionGetTask(mContext);
                competitionGetTask.execute(index);
            }
        });

        //TODO: Remove test data
        //CompetitionListItem testItemOne = new CompetitionListItem(-1, "DM i Roadkill", 357, false, -1, -1);
        //CompetitionListItem testItemTwo = new CompetitionListItem(-1, "Kør Casper Ned 2016", 2300001, true, 9027, 403);
        //mCompetitionList.add(testItemOne);
        //mCompetitionList.add(testItemTwo);
        //mCompetitionListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        //Check if user has a username - Display dialog if not.
        HandleUsername();

        super.onResume();
    }

    //endregion

    //region LISTENERS

    ListView.OnItemClickListener CompetitionClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent = new Intent(mContext, CompetitionOverviewActivity.class);
            intent.putExtra(getString(R.string.CompetitionIdIntentName), mCompetitionList.get(position).CompetitionId);
            startActivity(intent);
        }
    };

    //endregion

    //region OTHER

    private class CompetitionGetTask extends AsyncTask<Integer, Void, Boolean> {
        final WeakReference<Context> mContextReference;

        public CompetitionGetTask(Context context) {
            mContextReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Integer... index) {
            try {
                mCompetitionList.addAll(ServiceHelper.GetCompetitionsForListView(1, index[0]));
                return true;
            } catch (Exception e) {
                mIndex = index[0];
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mContextReference.get() != null) {
                mCompetitionListView.removeFooterView(mFooterView);
                findViewById(R.id.CompetitionListLoadingView).setVisibility(View.GONE);
                mCompetitionListView.setEmptyView(findViewById(R.id.CompetitionListEmptyView));
                if (success) {
                    mCompetitionListAdapter.notifyDataSetChanged();
                } else {
                    BuildAlertDialog().show();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            mCompetitionListView.addFooterView(mFooterView, null, false);
            mCompetitionListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private AlertDialog BuildAlertDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.CompetitionListLoadErrorText))
                .setPositiveButton(getString(R.string.TripListRetryLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CompetitionGetTask competitionGetTask = new CompetitionGetTask(mContext);
                        competitionGetTask.execute(mIndex);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.TripListCancelLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
    }

    private AlertDialog BuildUsernameDialog(){
        final EditText inputView = new EditText(this);

        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.UsernameNotSetTitle))
                .setMessage(getString(R.string.UsernameNotSetDescription))
                .setPositiveButton(getString(R.string.UsernameOk), null)
                .setNegativeButton(getString(R.string.UsernameGoBack), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (inputView.getText().toString().length() >= mContext.getResources().getInteger(R.integer.userNameMinLength)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                            editor.putString(getString(R.string.UsernameTitle), inputView.getText().toString()).apply();
                            alertDialog.cancel();
                        } else {
                            inputView.setError(mContext.getString(R.string.UsernameTooShortError));
                        }
                    }
                });
            }
        });

        alertDialog.setView(inputView, 50, 0, 70, 0);
        return alertDialog;
    }

    private void HandleUsername() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString(getString(R.string.UsernameTitle), "").isEmpty()) {
            BuildUsernameDialog().show();
        }
    }
    //endregion
}