package at.dongri.android.ibeaconfinder;

/**
 * Created by dongri on 2/23/14.
 */
import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import at.dongri.android.ibeaconfinder.R;

/**
 *
 * Created by Dongri on 2/23/14.
 *
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener, IBeaconConsumer  {
    protected static final String TAG = "MonitoringActivity";
    private ListView list = null;
    private BeaconAdapter adapter = null;
    private ArrayList<IBeacon> arrayL = new ArrayList<IBeacon>();
    private LayoutInflater inflater;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_scan:
                if (iBeaconManager.isBound(this)) {
                    iBeaconManager.unBind(this);
                    item.setTitle("Scan");
                    arrayL.clear();
                    adapter.notifyDataSetChanged();
                }else{
                    iBeaconManager.bind(this);
                    item.setTitle("Stop");
                }
                return true;
            case R.id.action_info:
                startActivity(new Intent(getBaseContext(), InfoActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        //mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        verifyBluetooth();

        list = (ListView) findViewById(R.id.list);
        adapter = new BeaconAdapter();
        list.setAdapter(adapter);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void verifyBluetooth() {
        try {
            if (!IBeaconManager.getInstanceForApplication(this).checkAvailability()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);

//                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Bluetooth not enabled");
//                builder.setMessage("Please enable bluetooth in settings and restart this application.");
//                builder.setPositiveButton(android.R.string.ok, null);
//                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        finish();
//                        System.exit(0);
//                    }
//                });
//                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();
        }

    }

    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, true);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, false);
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
            }
        });
    }
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<IBeacon> iBeacons, Region region) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        arrayL.clear();
                        arrayL.addAll((ArrayList<IBeacon>) iBeacons);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

        });
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                logToDisplay("I just saw an iBeacon for the first time!"+region);
            }

            @Override
            public void didExitRegion(Region region) {
                logToDisplay("I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                logToDisplay("I have just switched from seeing/not seeing iBeacons: ");
            }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class BeaconAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (arrayL != null && arrayL.size() > 0)
                return arrayL.size();
            else
                return 0;
        }

        @Override
        public IBeacon getItem(int arg0) {
            return arrayL.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                ViewHolder holder;

                if (convertView != null) {
                    holder = (ViewHolder) convertView.getTag();
                } else {
                    holder = new ViewHolder(convertView = inflater.inflate(R.layout.tupple_monitoring, null));
                }
                if (arrayL.get(position).getProximityUuid() != null)
                    holder.beacon_uuid.setText("UUID: " + arrayL.get(position).getProximityUuid().toUpperCase());

                holder.beacon_major.setText("Major: " + arrayL.get(position).getMajor());

                holder.beacon_minor.setText(" Minor: " + arrayL.get(position).getMinor());

                holder.beacon_proximity.setText("Proximity: " + arrayL.get(position).getProximity());

                holder.beacon_rssi.setText(" Rssi: " + arrayL.get(position).getRssi());

                holder.beacon_txpower.setText(" TxPower: " + arrayL.get(position).getTxPower());

                holder.beacon_range.setText("Accuracy: " + arrayL.get(position).getAccuracy());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private class ViewHolder {
            private TextView beacon_uuid;
            private TextView beacon_major;
            private TextView beacon_minor;
            private TextView beacon_proximity;
            private TextView beacon_rssi;
            private TextView beacon_txpower;
            private TextView beacon_range;

            public ViewHolder(View view) {
                beacon_uuid = (TextView) view.findViewById(R.id.BEACON_uuid);
                beacon_major = (TextView) view.findViewById(R.id.BEACON_major);
                beacon_minor = (TextView) view.findViewById(R.id.BEACON_minor);
                beacon_proximity = (TextView) view.findViewById(R.id.BEACON_proximity);
                beacon_rssi = (TextView) view.findViewById(R.id.BEACON_rssi);
                beacon_txpower = (TextView) view.findViewById(R.id.BEACON_txpower);
                beacon_range = (TextView) view.findViewById(R.id.BEACON_range);

                view.setTag(this);
            }
        }

    }
}
