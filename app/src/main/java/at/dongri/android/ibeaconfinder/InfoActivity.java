package at.dongri.android.ibeaconfinder;

import android.app.Activity;
import android.os.Bundle;

import at.dongri.android.ibeaconfinder.R;

/**
 * Created by dongri on 3/9/14.
 */
public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}

