package at.dongri.android.ibeaconfinder;


import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;

/**
 * Created by dongri on 10/4/15.
 */

public class BeaconService extends Service implements BootstrapNotifier {

    public static final String TAG = org.altbeacon.beacon.service.BeaconService.class.getSimpleName();

    public static final int NOTIFICATION_ID = 9999;

    public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private RegionBootstrap regionBootstrap;

    private BeaconManager beaconManager;

    private Region region;

    private String beaconName;

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
        beaconManager.setBackgroundBetweenScanPeriod(1000);

        beaconName = "myRangingUniqueId";
        region = new Region(beaconName, null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for(Beacon beacon : beacons) {
                    Log.d(TAG, "UUID:" + beacon.getId1() + ", major:" + beacon.getId2() + ", minor:" + beacon.getId3() + ", Distance:" + beacon.getDistance() + ",RSSI" + beacon.getRssi() + ", TxPower" + beacon.getTxPower());
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void didEnterRegion(Region region) {
        showNotification("EnterRegion", "Hello~");
    }

    @Override
    public void didExitRegion(Region region) {
        showNotification("ExitRegion", "Bye~");
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        Log.d(TAG, "Determine State: " + i);
    }

    private void showNotification(String contentText, String subText){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_launcher);

        builder.setTicker("Ticker");
        builder.setContentTitle("iBeacon Finder");
        builder.setContentText(contentText);
        builder.setSubText(subText);
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE
                | Notification.DEFAULT_LIGHTS);
        builder.setAutoCancel(true);
        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}
