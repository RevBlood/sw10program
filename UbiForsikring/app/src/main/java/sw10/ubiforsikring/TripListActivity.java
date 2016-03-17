package sw10.ubiforsikring;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TripListActivity extends AppCompatActivity {
    Context mContext;
    ArrayAdapter mTripListAdapter;
    List<TripListEntry> mTripList;

    //Active Trip
    LatLng mPreviousPosition;
    TripListEntry mActiveTrip;

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

        //TODO: Remove test data
        mTripList = new ArrayList<>();
        mTripList.add(0, new TripListEntry(1, 1010101010, 1011111111, 10700, 47, 16.01));
        mTripList.add(0, new TripListEntry(2, 1000101111, 1011110101, 10700, 35, 14.48));
        mTripList.add(0, new TripListEntry(3, 1001101010, 1111111111, 7200, 25, 11.11));
        mTripList.add(0, new TripListEntry(4, 1010101110, 1110000001, 4300, 13, 5.84));
        mTripList.add(0, new TripListEntry(5, 1000100010, 1001110101, 5600, 4, 6.13));
        mTripList.add(0, new TripListEntry(6, 100000000, 110000000, 20100, 10, 22.7));
        mTripList.add(0, new TripListEntry(7, 10000000, 11000000, 20100, 39, 28.34));

        //Setup ListView
        ListView tripListView = (ListView) findViewById(R.id.TripOverviewListView);
        mTripListAdapter = new TripListAdapter(this, mTripList);
        tripListView.setAdapter(mTripListAdapter);
        tripListView.setOnItemClickListener(MainListViewListener);
        tripListView.setEmptyView(findViewById(R.id.MainListViewEmpty));
    }

    @Override
    public void onResume() {
        //Remove previously active trip from list if relevant
        if (mActiveTrip != null) {
            mTripList.remove(mActiveTrip);
            mTripListAdapter.notifyDataSetChanged();
        }

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
        unregisterReceiver(mLocationReceiver);

        //Disconnect from TripService
        unbindService(mTripServiceConnection);

        super.onPause();
    }

    //endregion

    //region LISTENERS

    ListView.OnItemClickListener MainListViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            //Figure which type of layout was clicked. Then determine action based on that
            int itemType = mTripListAdapter.getItemViewType(position);

            if(itemType == TripListAdapter.VIEWTYPE_CURRENT) {
                startActivity(new Intent(mContext, LiveMapActivity.class));
            } else {
                Intent intent = new Intent(mContext, TripOverviewActivity.class);
                intent.putExtra(getString(R.string.TripIdIntentName), mTripList.get(position).TripId);
                startActivity(intent);
            }
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
                    mActiveTrip.Distance += route.get(i).distanceTo(route.get(i - 1));
                }

                //Update distance in the ListView
                mTripListAdapter.notifyDataSetChanged();

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
                mActiveTrip.Distance += DistanceBetweenLatLng(position, mPreviousPosition);

                Log.d("Debug", "Position: " + DistanceBetweenLatLng(position, mPreviousPosition));

                //Update distance in the ListView
                mTripListAdapter.notifyDataSetChanged();

                //Save position as the previous for calculating further distances
                mPreviousPosition = position;
            }
        }
    }

    private void HandleTripStatus() {
        if (mIsTripActive) {
            //Update the ListView
            mActiveTrip = new TripListEntry(true, false);
            mTripList.add(0, mActiveTrip);

            //Notify ListView of the change
            mTripListAdapter.notifyDataSetChanged();

            //Listen for, and request the route so far, from the TripService
            mRouteReceiver = new RouteReceiver();
            registerReceiver(mRouteReceiver, new IntentFilter(getString(R.string.BroadcastRouteIntent)));
            MessageTripService(TripService.UPDATE_ROUTE_BROADCAST);
        }
    }

    private void HandleProcessingStatus() {
        if (mIsProcessing) {
            mActiveTrip = new TripListEntry(false, true);
            mTripList.add(0, mActiveTrip);

            //Notify ListView of the change
            mTripListAdapter.notifyDataSetChanged();
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

    //endregion
}