package sw10.ubiforsikring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Objects.TripObjects.Trip;

public class TripOverviewActivity extends AppCompatActivity {;
    ArrayAdapter mMainListViewAdapter;
    List<Trip> tripList;

    //region ACTIVITY EVENTS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_overview);
        tripList = new ArrayList<>();

        //TODO: Remove test data
        Trip trip1 = new Trip();
        trip1.TripId = 1;
        tripList.add(trip1);
        Trip trip2 = new Trip();
        trip2.TripId = 2;
        tripList.add(0, trip2);

        //Setup ListView
        ListView mainListView = (ListView) findViewById(R.id.TripOverviewListView);
        mMainListViewAdapter = new TripOverviewAdapter(this, tripList);
        mainListView.setAdapter(mMainListViewAdapter);
        mainListView.setOnItemClickListener(MainListViewListener);
        mainListView.setEmptyView(findViewById(R.id.MainListViewEmpty));
    }

    //endregion

    //region LISTENERS

    ListView.OnItemClickListener MainListViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> a, View v, int position, long id) {
            Toast.makeText(getBaseContext(), "Click", Toast.LENGTH_SHORT).show();
        }
    };

    //endregion

}