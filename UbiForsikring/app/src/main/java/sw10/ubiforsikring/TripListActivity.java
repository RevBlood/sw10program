package sw10.ubiforsikring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.TripObjects.TripListItem;

public class TripListActivity extends AppCompatActivity {
    Context mContext;
    View mFooterView;

    //Trip list
    ArrayAdapter mTripListAdapter;
    List<TripListItem> mTripList;
    ListView mTripListView;

    //Active Trip
    TextView mCurrentTripDescriptionView;
    LatLng mPreviousPosition;
    double mMetersDriven = 0;

    //TripService communication
    ServiceConnection mTripServiceConnection;
    Messenger mMessenger;
    BroadcastReceiver mRouteReceiver;
    BroadcastReceiver mLocationReceiver;
    BroadcastReceiver mStatusReceiver;

    //TripService status
    boolean mIsTripActive = false;
    boolean mIsProcessing = false;

    //region ACTIVITY EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);
        mContext = this;
        mStatusReceiver = new StatusReceiver();
        mTripList = new ArrayList<>();
        mCurrentTripDescriptionView = (TextView) findViewById(R.id.CurrentTripDescriptionView);
        mFooterView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listitem_footer, null, false);

        //Setup ListView
        mTripListView = (ListView) findViewById(R.id.TripOverviewListView);
        mTripListAdapter = new TripListAdapter(this, mTripList);
        mTripListView.setAdapter(mTripListAdapter);
        mTripListView.setOnItemClickListener(TripListViewListener);
    }

    @Override
    public void onResume() {
        //Reset trip list
        mTripList.clear();
        mTripListAdapter.notifyDataSetChanged();

        //Setup the list view again
        mTripListView.setOnScrollListener(TripListViewScrollListener);
        findViewById(R.id.TripListEmptyView).setVisibility(View.GONE);
        mTripListView.setEmptyView(findViewById(R.id.TripListLoadingView));

        //Get entries for trip list view
        TripGetTask tripGetTask = new TripGetTask();
        tripGetTask.execute(0);

        //Listen for TripService status
        registerReceiver(mStatusReceiver, new IntentFilter(getString(R.string.BroadcastStatusIntent)));

        //Connect to the TripService
        InitializeTripServiceConnection();
        BindTripService();

        super.onResume();
    }

    @Override
    public void onPause() {
        //Disable trip list from updating
        mTripListView.setOnScrollListener(null);

        //Stop listening for new locations
        if (mIsTripActive) {
            unregisterReceiver(mLocationReceiver);
        }

        //Disconnect from TripService
        unbindService(mTripServiceConnection);

        super.onPause();
    }

    //endregion

    //region LISTENERS

    ListView.OnItemClickListener TripListViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent = new Intent(mContext, TripOverviewActivity.class);
            intent.putExtra(getString(R.string.TripIdIntentName), mTripList.get(position).TripId);
            startActivity(intent);
        }
    };

    ListView.OnScrollListener TripListViewScrollListener = new ListViewScrollListener(this, 5) {
        @Override public void GetMoreTrips(int index) {
            TripGetTask tripGetTask = new TripGetTask();
            tripGetTask.execute(index);
        }
    };

    View.OnClickListener CurrentTripClickListener = new View.OnClickListener(){
        @Override public void onClick(View view) {
            startActivity(new Intent(mContext, LiveMapActivity.class));
        }
    };

    //endregion

    //region INCOMING EVENTS

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Only need status once - Unregister the receiver afterwards
            unregisterReceiver(mStatusReceiver);

            mIsTripActive = intent.getBooleanExtra(getString(R.string.BroadcastIsTripActive), false);
            mIsProcessing = intent.getBooleanExtra(getString(R.string.BroadcastIsProcessing), false);

            HandleTripStatus();
            HandleProcessingStatus();
        }
    }

    private class RouteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<Location> route = intent.getParcelableArrayListExtra(getString(R.string.BroadcastRouteLocationList));

            if(!route.isEmpty()) {
                //Add all distance to the active trip
                for (int i = 1; i < route.size() - 1; i++) {
                    mMetersDriven += route.get(i).distanceTo(route.get(i - 1));
                }

                mCurrentTripDescriptionView.setText(String.format(getString(R.string.CurrentTripDistanceText), mMetersDriven / 1000));

                //Save last position as the previous for calculating further distances
                mPreviousPosition = new LatLng(route.get(route.size() - 1).getLatitude(), route.get(route.size() - 1).getLongitude());
            }

            //Unregister the receiver - We only need the route once
            unregisterReceiver(mRouteReceiver);

            //Register receiver for getting new positions
            mLocationReceiver = new PositionReceiver();
            registerReceiver(mLocationReceiver, new IntentFilter(getString(R.string.BroadcastLiveGpsIntent)));
        }
    }

    private class PositionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(getString(R.string.BroadcastLiveGpsLocation));
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

            //If there is a previous position, start adding the distance to new positions
            if (mPreviousPosition != null) {
                mMetersDriven += DistanceBetweenLatLng(position, mPreviousPosition);

                mCurrentTripDescriptionView.setText(String.format(getString(R.string.CurrentTripDistanceText), mMetersDriven / 1000));
            }

            //Save position as the previous for calculating further distances
            mPreviousPosition = position;
        }
    }

    private void HandleTripStatus() {
        if (mIsTripActive) {
            //Listen for, and request the route so far, from the TripService
            mRouteReceiver = new RouteReceiver();
            registerReceiver(mRouteReceiver, new IntentFilter(getString(R.string.BroadcastRouteIntent)));
            MessageTripService(TripService.UPDATE_ROUTE_BROADCAST);

            //Show the relevant layout
            View currentTripLayout = findViewById(R.id.CurrentTripLayout);
            TextView currentTripTitleView = (TextView) findViewById(R.id.CurrentTripTitleView);
            currentTripLayout.setVisibility(View.VISIBLE);
            currentTripLayout.setOnClickListener(CurrentTripClickListener);
            currentTripTitleView.setText(getString(R.string.CurrentTripTitle));
            mCurrentTripDescriptionView.setText(getString(R.string.CurrentTripDistanceDefaultText));
        }
    }

    private void HandleProcessingStatus() {
        if (mIsProcessing) {
            //Show the relevant layout
            View activeTripLayout = findViewById(R.id.CurrentTripLayout);
            activeTripLayout.setVisibility(View.VISIBLE);

            //Show the relevant layout
            View currentTripLayout = findViewById(R.id.CurrentTripLayout);
            TextView currentTripTitleView = (TextView) findViewById(R.id.CurrentTripTitleView);
            currentTripLayout.setVisibility(View.VISIBLE);
            currentTripLayout.setOnClickListener(CurrentTripClickListener);
            currentTripTitleView.setText(getString(R.string.CurrentTripTitle));
            mCurrentTripDescriptionView.setText(getString(R.string.CurrentTripProcessingText));
        }
    }

    //endregion

    //region TRIP SERVICE

    private void InitializeTripServiceConnection() {
        //Create a connection and a messenger for communication with the service
        //Enable/disable interaction with the service depending on connection status
        mTripServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMessenger = new Messenger(service);

                //As soon as the service is available, request current status
                MessageTripService(TripService.UPDATE_STATUS_BROADCAST);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMessenger = null;
            }
        };
    }

    private void BindTripService() {
        Intent intent = new Intent(this, TripService.class);
        bindService(intent, mTripServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean MessageTripService(int messageId) {
        //Create message to TripService
        Message message = Message.obtain(null, messageId, 0, 0);

        //Send the Message to the Service
        try {
            mMessenger.send(message);
            return true;
        } catch (RemoteException e) {
            Log.e("Debug", "Failed to contact TripService");
            return false;
        }
    }

    //endregion

    //region OTHER

    private double DistanceBetweenLatLng(LatLng first, LatLng second) {
        float[] result = new float[1];
        Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, result);
        return result[0];
    }

    private class TripGetTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... index) {
            mTripList.addAll(ServiceHelper.GetTripsForListview(1, index[0]));
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mTripListView.removeFooterView(mFooterView);
            mTripListView.setEmptyView(findViewById(R.id.TripListEmptyView));
            mTripListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            mTripListView.addFooterView(mFooterView);
            mTripListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    //endregion
}