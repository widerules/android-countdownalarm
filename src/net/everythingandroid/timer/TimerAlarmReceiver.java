package net.everythingandroid.timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;

public class TimerAlarmReceiver extends BroadcastReceiver {
	// 200ms off, 200ms on
	private static final long[] vibrate_pattern = { 200, 200 };
	private static final String alarm_timeout = "5";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("TimerAlarmReceiver: onReceive() start");
		//Acquire wakelock
		ManageWakeLock.acquire(context);
		
		ManageNotification.clearAll(context);
		
		// Show the notification
		NotificationManager myNM = 
			(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);


		//Timer myTimer = new Timer(context);		
		//myTimer.stop();
		
//		boolean repeat = myPrefs.getBoolean(context.getString(R.string.pref_repeat), false);
//		if (repeat) {
//			int hour = Integer.parseInt(myPrefs.getString("prevHourSelected", "0"));
//			int min  = Integer.parseInt(myPrefs.getString("prevMinSelected",  "0"));
//			int sec  = Integer.parseInt(myPrefs.getString("prevSecSelected",  "0"));
//			if (hour > 0 || min > 0 || sec > 0) {
//				myTimer.start(hour, min, sec);
//				myTimer.halt();
//			}
//		}
		
		boolean vibrate = 
			myPrefs.getBoolean(context.getString(R.string.pref_vibrate), false);
		boolean flashLed = 
			myPrefs.getBoolean(context.getString(R.string.pref_flashled), false);
		String flashLedCol = 
			myPrefs.getString(context.getString(R.string.pref_flashled_color), "yellow");
		int timeout =
			Integer.valueOf(myPrefs.getString(context.getString(R.string.pref_timeout), alarm_timeout));
		
		String defaultRingtone = 
			Settings.System.DEFAULT_RINGTONE_URI.toString();
		Uri alarmSoundURI = Uri.parse(
				myPrefs.getString(context.getString(R.string.pref_alarmsound),defaultRingtone));
		
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(
				R.drawable.alarm_icon, 
				context.getText(R.string.timer_complete),
				System.currentTimeMillis());

		notification.flags = Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_INSISTENT;		
		
		notification.audioStreamType = AudioManager.STREAM_ALARM;
		//notification.audioStreamType = AudioManager.STREAM_RING;
		
		if (flashLed) {
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.ledOnMS = 250;
			notification.ledOffMS = 250;
			int col = Color.YELLOW;
			try {
				col = Color.parseColor(flashLedCol);	
			} catch (IllegalArgumentException e) {
				//int col = Color.YELLOW;
			}
			//Blue, Green, Red, Yellow, Magenta
			notification.ledARGB = col;
		}
				
		if (vibrate) {
			notification.vibrate = vibrate_pattern;
		}		
		
		notification.sound = alarmSoundURI;
		
		//The pendingintent to launch if the status message is clicked
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, TimerActivity.class), 0);

		//Set the messages that show when the status bar is pulled down
		notification.setLatestEventInfo(context,
				context.getText(R.string.timer_complete),
				context.getText(R.string.notification_tip_complete), contentIntent);

		Log.v("*** Notify running ***");
		
		//Send notification with unique ID
		myNM.notify(ManageNotification.NOTIFICATION_ALERT, notification);		
		
		//ManageKeyguard.disableKeyguard(context);
//			Intent mainIntent = new Intent(context, TimerActivity.class);
//			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			mainIntent.putExtra("net.everythingandroid.ALARM_RINGING", true);
//			context.startActivity(mainIntent);
		
		ClearAllReceiver.setCancel(context, timeout);
				
		Intent alarmDialog = new Intent(context, TimerAlarmActivity.class);
		alarmDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP); //| Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(alarmDialog);
		Log.v("TimerAlarmReceiver: onReceive() end");
	}
}