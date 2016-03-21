package at.dongri.android.ibeaconfinder;

/**
 * Created by dongri on 2/23/14.
 */

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;


/**
 *
 * Created by Dongri on 2/23/14.
 *
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener, BeaconConsumer {

    private BeaconManager iBeaconManager;

    private ListView list = null;
    private BeaconAdapter adapter = null;
    private ArrayList<Beacon> arrayL = new ArrayList<Beacon>();
    private LayoutInflater inflater;

    private Intent service = null;

    public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        verifyBluetooth();

        list = (ListView) findViewById(R.id.list);
        adapter = new BeaconAdapter();
        list.setAdapter(adapter);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        iBeaconManager = BeaconManager.getInstanceForApplication(this);

        iBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        service = new Intent(MainActivity.this, BeaconService.class);
        //startService(service);
        //Toast.makeText(this, "Start Background Service", Toast.LENGTH_SHORT).show();
    }

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
                    iBeaconManager.unbind(this);
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
            case R.id.action_background:
                startService(service);
                Toast.makeText(this, "Start Background Service", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private void verifyBluetooth() {
        try {
            if (!iBeaconManager.getInstanceForApplication(this).checkAvailability()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unbind(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(true);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(false);
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
            }
        });
    }
    @Override
    public void onBeaconServiceConnect() {

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> iBeacons, Region region) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        arrayL.clear();
                        arrayL.addAll((ArrayList<Beacon>) iBeacons);
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
        public Beacon getItem(int arg0) {
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
                holder.beacon_uuid.setText("UUID: " + arrayL.get(position).getId1().toString().toUpperCase());

                holder.beacon_major.setText("Major: " + arrayL.get(position).getId2());

                holder.beacon_minor.setText(" Minor: " + arrayL.get(position).getId3());

                double proximity = arrayL.get(position).getDistance();
                holder.beacon_proximity.setText("Proximity: " + (new BigDecimal(proximity).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue()));

                holder.beacon_rssi.setText(" Rssi: " + arrayL.get(position).getRssi());

                holder.beacon_txpower.setText(" TxPower: " + arrayL.get(position).getTxPower());

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

            public ViewHolder(View view) {
                beacon_uuid = (TextView) view.findViewById(R.id.BEACON_uuid);
                beacon_major = (TextView) view.findViewById(R.id.BEACON_major);
                beacon_minor = (TextView) view.findViewById(R.id.BEACON_minor);
                beacon_proximity = (TextView) view.findViewById(R.id.BEACON_proximity);
                beacon_rssi = (TextView) view.findViewById(R.id.BEACON_rssi);
                beacon_txpower = (TextView) view.findViewById(R.id.BEACON_txpower);

                view.setTag(this);
            }
        }

    }
}
