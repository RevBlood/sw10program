package sw10.ubiforsikring;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.TripObjects.TripListItem;

public class TripListActivity extends AppCompatActivity {
    Context mContext;
    View mFooterView;
    int mOffset;

    //Trip list
    ArrayAdapter mTripListAdapter;
    List<TripListItem> mTripList;
    ListView mTripListView;

    //Active Trip
    TextView mCurrentTripDescriptionView;
    LatLng mPreviousPosition;
    double mMetersDriven;

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
        mTripList = new ArrayList<>();
        mStatusReceiver = new StatusReceiver();
        mCurrentTripDescriptionView = (TextView) findViewById(R.id.CurrentTripDescriptionView);
        mFooterView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listitem_footer, null, false);

        //Setup ListView
        mTripListView = (ListView) findViewById(R.id.TripListView);
        mTripListAdapter = new TripListAdapter(this, mTripList);
        mTripListView.setAdapter(mTripListAdapter);
        mTripListView.setOnItemClickListener(TripListViewListener);
        mTripListView.setOnScrollListener(new ListViewScrollListener(this, getResources().getInteger(R.integer.LoadMoreThreshold), getResources().getInteger(R.integer.ChunkSize)) {
            @Override public void GetMoreEntries(int index) {
                TripGetTask tripGetTask = new TripGetTask(mContext);
                tripGetTask.execute(index);
            }
        });
    }

    @Override
    public void onResume() {
        //Listen for TripService status
        registerReceiver(mStatusReceiver, new IntentFilter(getString(R.string.BroadcastStatusIntent)));

        //Connect to the TripService
        InitializeTripServiceConnection();
        BindTripService();

        super.onResume();
    }

    @Override
    public void onPause() {
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
            intent.putExtra(getString(R.string.TripOverviewIntent), mTripList.get(position).TripId);
            startActivity(intent);
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
            //Only need status once - Unregister the receiver
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
            //Unregister the receiver - We only need the route once
            unregisterReceiver(mRouteReceiver);

            // Reset any variables that could have been used earlier
            mMetersDriven = 0;
            mPreviousPosition = null;

            // Read the route from SharedPreferences
            List<LatLng> route = new ArrayList<>();
            SharedPreferences preferences = getSharedPreferences(getString(R.string.SW10Preferences), Context.MODE_MULTI_PROCESS);
            Set<String> values = preferences.getStringSet(getString(R.string.StoredRoute), new HashSet<String>());

            for (String value : values) {
                String[] latLng = value.split(";");
                route.add(new LatLng((Double.parseDouble(latLng[0])), Double.parseDouble(latLng[1])));
            }

            if(!route.isEmpty()) {
                //Add all distance to the active trip
                for (int i = 1; i < route.size() - 1; i++) {
                    mMetersDriven += DistanceBetweenLatLng(route.get(i), route.get(i - 1));
                }

                mCurrentTripDescriptionView.setText(String.format(getString(R.string.CurrentTripDistanceText), mMetersDriven / 1000));

                //Save last position as the previous for calculating further distances
                mPreviousPosition = route.get(route.size() - 1);
            }

            //Register receiver for getting new positions
            mLocationReceiver = new LocationReceiver();
            registerReceiver(mLocationReceiver, new IntentFilter(getString(R.string.BroadcastLiveGpsIntent)));
        }
    }

    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(getString(R.string.BroadcastLiveGpsLocation));
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

            //If a previous position exists, start adding up distance
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
            //Show the relevant layouts
            View currentTripLayout = findViewById(R.id.CurrentTripLayout);
            TextView currentTripTitleView = (TextView) findViewById(R.id.CurrentTripTitleView);
            currentTripLayout.setVisibility(View.VISIBLE);
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

    private class TripGetTask extends AsyncTask<Integer, Void, Boolean> {
        final WeakReference<Context> mContextReference;

        public TripGetTask(Context context) {
            mContextReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Integer... offset) {
            try {
                // Get car id from SharedPreferences - Then request trips for that car
                SharedPreferences preferences = getSharedPreferences(getString(R.string.SW10Preferences), Context.MODE_PRIVATE);
                int userId = preferences.getInt(getString(R.string.StoredCarId), -1);
                mTripList.addAll(ServiceHelper.GetTripsForListView(userId, offset[0]));
                return true;
            } catch (Exception e) {
                // If retrieval fails, save the offset in case user wants to retry
                mOffset = offset[0];
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mContextReference.get() != null) {
                // Resetting the adapter is necessary pre KitKat.
                mTripListView.setAdapter(mTripListAdapter);
                mTripListView.removeFooterView(mFooterView);
                findViewById(R.id.TripListLoadingView).setVisibility(View.GONE);
                mTripListView.setEmptyView(findViewById(R.id.TripListEmptyView));
                if (success) {
                    mTripListAdapter.notifyDataSetChanged();
                } else {
                    BuildAlertDialog().show();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.TripListEmptyView).setVisibility(View.GONE);
            mTripListView.setEmptyView(findViewById(R.id.TripListLoadingView));
            mTripListView.addFooterView(mFooterView, null, false);
            mTripListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private AlertDialog BuildAlertDialog(){
        return new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.TripListLoadErrorText))
                .setPositiveButton(getString(R.string.TripListRetryLoad), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TripGetTask tripGetTask = new TripGetTask(mContext);
                        tripGetTask.execute(mOffset);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.DialogIgnore), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
    }

    //endregion
}