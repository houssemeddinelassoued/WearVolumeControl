package com.fiskur.wearvolume;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        Intent startVolumeServiceIntent = new Intent(this, WearVolumeService.class);
        startVolumeServiceIntent.setAction(WearVolumeService.ACTION_VOLUME_INIT);
        startService(startVolumeServiceIntent);
        
        finish(); 
    }
}
