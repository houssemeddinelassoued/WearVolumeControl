package com.fiskur.wearvolume;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;

public class WearVolumeService extends Service {
	
	private static final int NOTIFICATION_ID = 1;
	public static final String ACTION_VOLUME_INIT = "com.fiskur.wearvolume.action.ACTION_VOLUME_INIT";
	public static final String ACTION_VOLUME_UP = "com.fiskur.wearvolume.action.ACTION_VOLUME_UP";
	public static final String ACTION_VOLUME_DOWN = "com.fiskur.wearvolume.action.ACTION_VOLUME_DOWN";
	public static final String ACTION_DISMISS = "com.fiskur.wearvolume.action.ACTION_DISMISS";
	public static final String ACTION_MUTE_TOGGLE = "com.fiskur.wearvolume.action.ACTION_MUTE_TOGGLE";
	
	private AudioManager mAudioManager = null;
	private NotificationManagerCompat mNotificationManager = null;
	
	private static int mPreMuteVolumeIndex = 0;
	private static boolean mMuted = false;
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(mAudioManager == null){
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}
		
		if(mNotificationManager == null){
            mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        }
		
		if(intent != null){
			
			final String action = intent.getAction();
			
			int maxVolumeIndex = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			int volumeIndex = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			
			if(ACTION_VOLUME_INIT.equals(action)){
				updateNotification(volumeIndex);
			}else if(ACTION_VOLUME_UP.equals(action)){
				
				if(mMuted){
					volumeIndex = mPreMuteVolumeIndex;
					mMuted = false;
				}
				
				if(volumeIndex < maxVolumeIndex)volumeIndex++;
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeIndex, 0);
				updateNotification(volumeIndex);
				
			}else if(ACTION_VOLUME_DOWN.equals(action)){
				
				if(volumeIndex > 0)volumeIndex--;
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeIndex, 0);
				updateNotification(volumeIndex);
				
			}else if(ACTION_MUTE_TOGGLE.equals(action)){
				
				if(mMuted){
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mPreMuteVolumeIndex, 0);
					mMuted = !mMuted;
					updateNotification(volumeIndex);
				}else{
					mPreMuteVolumeIndex = volumeIndex;
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
					mMuted = !mMuted;
					updateNotification(0);
				}
				
			}else if(ACTION_DISMISS.equals(action)){
				stopSelf();
			}
			
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void updateNotification(int volumeIndex){
		NotificationCompat.WearableExtender wearableExtender = new WearableExtender();
		
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
		notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        notificationBuilder.setContentTitle("Wear Volume Control");
        notificationBuilder.setContentText("Volume: " + volumeIndex);
        wearableExtender.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.wear_vol_background));
        wearableExtender.setHintHideIcon(true);
        
        //Stop service
        Intent stopIntent = new Intent(getApplicationContext(), WearVolumeService.class);
        stopIntent.setAction(ACTION_DISMISS);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setDeleteIntent(stopPendingIntent);

        //Volume up
        Intent volUpIntent = new Intent(getApplicationContext(), WearVolumeService.class);
        volUpIntent.setAction(ACTION_VOLUME_UP);
        PendingIntent volUpPendingIntent = PendingIntent.getService(this, 1, volUpIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action volUpAction = new NotificationCompat.Action.Builder(R.drawable.ic_vol_up, "Volume Up", volUpPendingIntent).build();
        wearableExtender.addAction(volUpAction);
        
        //Volume down
        Intent volDownIntent = new Intent(getApplicationContext(), WearVolumeService.class);
        volDownIntent.setAction(ACTION_VOLUME_DOWN);
        PendingIntent volDownPendingIntent = PendingIntent.getService(this, 1, volDownIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action volDownAction = new NotificationCompat.Action.Builder(R.drawable.ic_vol_down, "Volume Down", volDownPendingIntent).build();
        wearableExtender.addAction(volDownAction);
        
        //Mute
        Intent muteIntent = new Intent(getApplicationContext(), WearVolumeService.class);
        muteIntent.setAction(ACTION_MUTE_TOGGLE);
        PendingIntent mutePendingIntent = PendingIntent.getService(this, 1, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        int muteDrawable;
        
        if(mMuted){
        	muteDrawable = R.drawable.ic_unmute;
        }else{
        	muteDrawable = R.drawable.ic_mute;
        }
        
        NotificationCompat.Action muteAction = new NotificationCompat.Action.Builder(muteDrawable, "Toggle Mute", mutePendingIntent).build();
        wearableExtender.addAction(muteAction);
        
        notificationBuilder.extend(wearableExtender);

        mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
