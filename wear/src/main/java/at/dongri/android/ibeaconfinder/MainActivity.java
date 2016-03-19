package at.dongri.android.ibeaconfinder;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends WearableActivity implements BeaconConsumer {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    private static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private BeaconManager mBeaconManager;
    private Region mRegion;

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    private String mTextString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
        mBeaconManager.bind(this);
        mRegion = new Region("at.dongri.ibeaconfinder.regionid", null, null, null);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

        mTextString = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBeaconManager.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d("MainActirivty", "onBeaconServiceConnect");
        mBeaconManager.setForegroundScanPeriod(1000);
        mBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d("MainActirivty", "didEnterRegion " + region.toString());
                try {
                    mBeaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d("MainActirivty", "didExitRegion " + region.toString());
                try {
                    mBeaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.d("MainActirivty", "didDetermineStateForRegion " + region.toString());
            }

        });
        mBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.d("MainActirivty", "didRangeBeaconsInRegion collection size: " + collection.size());
                mTextString = getString(R.string.scaninng);
                if (collection.size() > 0) {
                    for (Iterator iterator = collection.iterator(); iterator.hasNext(); ) {
                        Beacon beacon = (Beacon) iterator.next();
                        String beaconStr = "uuid" + beacon.getId1().toString() +
                                           ", major:" + beacon.getId2().toString() +
                                           ", minor:" + beacon.getId3().toString() +
                                           ", Distance:" + beacon.getDistance() +
                                           ", RSSI:" + beacon.getRssi();
                        Log.d("MainActirivty", "ibeacon: " + beaconStr);
                        mTextString = mTextString + "\n" + beaconStr;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText(mTextString);
                    }
                });
            }
        });
        try {
            Log.d("MainActirivty", "startRangingBeaconsInRegion: findid");
            mBeaconManager.startMonitoringBeaconsInRegion(mRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }

}
