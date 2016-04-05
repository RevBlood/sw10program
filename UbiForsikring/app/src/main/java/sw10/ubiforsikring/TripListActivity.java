package sw10.ubiforsikring;

import android.app.ProgressDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import sw10.ubiforsikring.Helpers.ServiceHelper;
import sw10.ubiforsikring.Objects.TripObjects.TripListItem;

public class TripListActivity extends AppCompatActivity {
    Context mContext;
    ArrayAdapter mTripListAdapter;
    List<TripListItem> mTripList;

    //Active Trip
    LatLng mPreviousPosition;
    TripListItem mActiveTrip;

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

        //Setup ListView
        ListView tripListView = (ListView) findViewById(R.id.TripOverviewListView);
        mTripListAdapter = new TripListAdapter(this, mTripList);
        tripListView.setAdapter(mTripListAdapter);
        tripListView.setOnItemClickListener(TripListViewListener);
        tripListView.setEmptyView(findViewById(R.id.MainListViewEmpty));
    }

    @Override
    public void onResume() {
        //Reset trip list
        mTripList.clear();
        mTripListAdapter.notifyDataSetChanged();
        ListView tripListView = (ListView) findViewById(R.id.TripOverviewListView);
        tripListView.setOnScrollListener(TripListViewScrollListener);
        GetMoreTripsHelper(0);

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
        ListView tripListView = (ListView) findViewById(R.id.TripOverviewListView);
        tripListView.setOnScrollListener(null);

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

    ListView.OnScrollListener TripListViewScrollListener = new ListViewScrollListener(this, 5) {
        @Override public void GetMoreTrips(int index) {
            GetMoreTripsHelper(index);
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
                    mActiveTrip.MetersDriven += route.get(i).distanceTo(route.get(i - 1));
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
                mActiveTrip.MetersDriven += DistanceBetweenLatLng(position, mPreviousPosition);

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
            mActiveTrip = new TripListItem(true, false);
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
            mActiveTrip = new TripListItem(false, true);
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

    private void GetMoreTripsHelper(final int index) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mContext.getString(R.string.LoadingTrips));
        progressDialog.show();

        new android.os.Handler().post(
            new Runnable() {
                public void run() {
                    List<TripListItem> items = ServiceHelper.GetTripsForListview(1, index);
                    mTripList.addAll(ServiceHelper.GetTripsForListview(1, index));
                    mTripListAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }
            }
        );
    }

    //endregion
}