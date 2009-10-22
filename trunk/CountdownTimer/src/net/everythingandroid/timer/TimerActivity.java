package net.everythingandroid.timer;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TimerActivity extends Activity {
  private static final int PREFERENCES_ID = Menu.FIRST;
  private static final int ABOUT_ID = Menu.FIRST + 1;
  private static final int DIALOG_ABOUT = 0;
  private static final int DIALOG_TIMER_COMPLETE = 1;
  // private static final int MSG_UPDATE_TIMER = 1;

  private Timer myTimer;
  private SharedPreferences myPrefs;
  private EditText hourEditText, minEditText, secEditText;
  private TextView hourTextView, minTextView, secTextView;
  private Spinner hourSpinner, minSpinner, secSpinner;
  private ArrayAdapter<String> secSpinnerAA, minSpinnerAA, hourSpinnerAA;
  private ArrayList<String> secSpinnerList, minSpinnerList, hourSpinnerList;
  private int keyboard_hidden;
  private TimerViewHandler tvHandler;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    myTimer = new Timer(this) {
      @Override
      public int start(long time) {
        int result = super.start(time);

        switch (result) {
          case Timer.TIMER_TOO_BIG:
            Toast.makeText(myContext, R.string.timer_too_long, Toast.LENGTH_SHORT).show();
            break;
          case Timer.TIMER_ZERO:
            Toast.makeText(myContext, R.string.select_a_time, Toast.LENGTH_SHORT).show();
            break;
          case Timer.TIMER_STARTED_OK:
            // startHandler();
            tvHandler.start();
            break;
          case Timer.TIMER_STARTED_OK_FROM_PAUSE:
            break;
          case Timer.TIMER_STALE:
            break;
        }
        return result;
      }

      @Override
      public void stop() {
        super.stop();
        // stopHandler();
        tvHandler.stop();

      }

      @Override
      public void pause() {
        super.pause();
        tvHandler.stop();
      }

      @Override
      public void resume() {
        super.resume();
        tvHandler.start();
      }

      @Override
      public void pauseResume() {
        super.pauseResume();
        updateButtons();
      }

      @Override
      public void startStop(long time) {
        super.startStop(time);
        updateButtons();
      }
    };

    tvHandler = new TimerViewHandler(myTimer) {
      @Override
      public void updateView() {
        updateCountdown();
      }
    };

    setContentView(R.layout.main);

    Button button = (Button) findViewById(R.id.StartStopButton);
    button.setOnClickListener(StartStopTimer);
    button = (Button) findViewById(R.id.PauseButton);
    button.setOnClickListener(PauseResumeTimer);

    hourTextView = (TextView) findViewById(R.id.HourTextView);
    minTextView = (TextView) findViewById(R.id.MinTextView);
    secTextView = (TextView) findViewById(R.id.SecTextView);

    hourSpinner = (Spinner) findViewById(R.id.HourSpinner);
    minSpinner = (Spinner) findViewById(R.id.MinuteSpinner);
    secSpinner = (Spinner) findViewById(R.id.SecondSpinner);

    hourEditText = (EditText) findViewById(R.id.HourEditText);
    minEditText = (EditText) findViewById(R.id.MinuteEditText);
    secEditText = (EditText) findViewById(R.id.SecondEditText);

    // hourEditText.setOnKeyListener(new OnKeyListener() {
    // public boolean onKey(View v, int keyCode, KeyEvent event) {
    // if (keyCode == KeyEvent.KEYCODE_ENTER) {
    // minEditText.requestFocus();
    // }
    // return false;
    // }
    // }
    // );

    hourSpinnerList =
      new ArrayList<String>(Arrays
          .asList(getResources().getStringArray(R.array.hours_array_full)));
    minSpinnerList =
      new ArrayList<String>(Arrays.asList(getResources().getStringArray(
          R.array.mins_secs_array_full)));
    secSpinnerList =
      new ArrayList<String>(Arrays.asList(getResources().getStringArray(
          R.array.mins_secs_array_full)));

    hourSpinnerAA =
      new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hourSpinnerList);
    minSpinnerAA =
      new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, minSpinnerList);
    secSpinnerAA =
      new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, secSpinnerList);

    hourSpinnerAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    minSpinnerAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    secSpinnerAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    hourSpinner.setAdapter(hourSpinnerAA);
    minSpinner.setAdapter(minSpinnerAA);
    secSpinner.setAdapter(secSpinnerAA);

    myPrefs = PreferenceManager.getDefaultSharedPreferences(TimerActivity.this);

    if (savedInstanceState == null) { // Only when it's created for the first
      // time
      // Check if the user has set a custom sound for the timer, if not show a
      // message
      if (myPrefs.getString(getString(R.string.pref_alarmsound), null) == null) {
        Toast.makeText(this, R.string.please_select_sound, Toast.LENGTH_LONG).show();
      }
    }

    keyboard_hidden = getResources().getConfiguration().keyboardHidden;

    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      LinearLayout LL = (LinearLayout) findViewById(R.id.ButtonLayout);
      LL.setOrientation(LinearLayout.HORIZONTAL);
    }

    if (keyboard_hidden == Configuration.KEYBOARDHIDDEN_NO) {
      hourEditText.setVisibility(EditText.VISIBLE);
      minEditText.setVisibility(EditText.VISIBLE);
      secEditText.setVisibility(EditText.VISIBLE);

      hourSpinner.setVisibility(View.GONE);
      minSpinner.setVisibility(View.GONE);
      secSpinner.setVisibility(View.GONE);
    }
  }

  @Override
  protected void onPause() {
    Log.v("TimerActivity: onPause()");
    super.onPause();

    myTimer.save();
    // stopHandler();
    tvHandler.stop();

    SharedPreferences.Editor settings = myPrefs.edit();

    if (keyboard_hidden == Configuration.KEYBOARDHIDDEN_YES) {
      settings.putString("prevHourSelected", (String) hourSpinner.getSelectedItem());
      settings.putString("prevMinSelected", (String) minSpinner.getSelectedItem());
      settings.putString("prevSecSelected", (String) secSpinner.getSelectedItem());
    } else {
      settings.putString("prevSecSelected", String.valueOf(getEditTextValue(secEditText)));
      settings.putString("prevMinSelected", String.valueOf(getEditTextValue(minEditText)));
      settings.putString("prevHourSelected", String.valueOf(getEditTextValue(hourEditText)));
    }
    settings.commit();

    // Reset the timer and buttons in case the alarm triggers and the activity
    // ends up in the background
    Timer emptyTimer = new Timer(this);
    updateButtons(emptyTimer);
    updateCountdown(emptyTimer);
  }

  @Override
  protected void onResume() {
    Log.v("TimerActivity: onResume()");
    super.onResume();

    String hour = myPrefs.getString("prevHourSelected", "0");
    String min = myPrefs.getString("prevMinSelected", "0");
    String sec = myPrefs.getString("prevSecSelected", "0");

    if (hourSpinnerList.size() == getResources().getStringArray(R.array.hours_array_full).length + 1) {
      hourSpinnerList.remove(0);
    }
    if (minSpinnerList.size() == getResources().getStringArray(R.array.mins_secs_array_full).length + 1) {
      minSpinnerList.remove(0);
    }
    if (secSpinnerList.size() == getResources().getStringArray(R.array.mins_secs_array_full).length + 1) {
      secSpinnerList.remove(0);
    }

    int hourPos = hourSpinnerAA.getPosition(String.valueOf(hour));
    int minPos = minSpinnerAA.getPosition(String.valueOf(min));
    int secPos = secSpinnerAA.getPosition(String.valueOf(sec));

    if (hourPos == -1) { // Not found in list
      hourSpinnerList.add(0, hour);
      hourSpinner.setSelection(0);
    } else {
      hourSpinner.setSelection(hourPos);
    }
    if (minPos == -1) { // Not found in list
      minSpinnerList.add(0, min);
      minSpinner.setSelection(0);
    } else {
      minSpinner.setSelection(minPos);
    }
    if (secPos == -1) { // Not found in list
      secSpinnerList.add(0, sec);
      secSpinner.setSelection(0);
    } else {
      secSpinner.setSelection(secPos);
    }

    hourEditText.setText(hour);
    minEditText.setText(min);
    secEditText.setText(sec);

    myTimer.restore();

    updateButtons();
    updateCountdown();

    // if (getIntent().getBooleanExtra("net.everythingandroid.ALARM_RINGING",
    // false)) {
    // // if (getIntent().getExtras() != null) {
    // // Log.v("found extras");
    // // if
    // (getIntent().getExtras().getBoolean("net.everythingandroid.ALARM_RINGING"))
    // {
    // Log.v("show dialog after timer complete");
    // showDialog(DIALOG_TIMER_COMPLETE);
    // // }
    // }
  }

  private OnClickListener StartStopTimer = new OnClickListener() {
    public void onClick(View v) {
      int hourSelected = 0, minSelected = 0, secSelected = 0;
      // myNM.cancelAll();

      // Keyboard is available, use edittext boxes
      if (keyboard_hidden == Configuration.KEYBOARDHIDDEN_NO) {
        hourSelected = getEditTextValue(hourEditText);
        minSelected = getEditTextValue(minEditText);
        secSelected = getEditTextValue(secEditText);
      } else { // Keyboard not available, use spinners
        hourSelected = getSpinnerValue(hourSpinner);
        minSelected = getSpinnerValue(minSpinner);
        secSelected = getSpinnerValue(secSpinner);
      }

      myTimer.startStop(hourSelected, minSelected, secSelected);
    }
  };

  private OnClickListener PauseResumeTimer = new OnClickListener() {
    public void onClick(View v) {
      myTimer.pauseResume();
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // Preferences menu item
    MenuItem PrefMenuItem = menu.add(0, PREFERENCES_ID, 0, R.string.menu_preference);
    PrefMenuItem.setIcon(android.R.drawable.ic_menu_preferences);

    // About menu item
    MenuItem AboutMenuItem = menu.add(0, ABOUT_ID, 0, R.string.menu_about);
    AboutMenuItem.setIcon(android.R.drawable.ic_menu_info_details);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case PREFERENCES_ID:
        Intent prefIntent = new Intent(this, Preferences.class);
        // prefIntent.setFlags();
        startActivity(prefIntent);
        return true;
      case ABOUT_ID:
        showDialog(DIALOG_ABOUT);
        // showDialog(DIALOG_TIMER_COMPLETE);
        return true;
    }
    return super.onMenuItemSelected(featureId, item);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      if (!myTimer.isRunning()) {
        Log.v("TimerActivity: onWindowFocusChanged(true)");
        ManageNotification.clear(this);
        ManageWakeLock.release();
        // myTimer.clearNotification();
      }
    } else {
      // Reset the UI in case it shows when the alert rings
      Log.v("TimerActivity: onWindowFocusChanged(false)");
      // Reset the timer and buttons in case the alarm triggers and the activity
      // ends up in the background
      // Timer emptyTimer = new Timer(this);
      // updateButtons(emptyTimer);
      // updateCountdown(emptyTimer);
    }
  }

  // @Override
  // public void onNewIntent(Intent intent) {
  // if (intent.getBooleanExtra("net.everythingandroid.ALARM_RINGING", false)) {
  // Log.v("onNewIntent(): show dialog after timer complete");
  // showDialog(DIALOG_TIMER_COMPLETE);
  // }
  // }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DIALOG_ABOUT:
        // Create a view to display in the about dialog
        TextView myTV = new TextView(TimerActivity.this);
        myTV.setText("Created by: Adam K\n" + "Email: adam@everythingandroid.net\n"
            + "Blog: http://www.everythingandroid.net\n\n"
            + "A simple countdown timer alarm application.  "
            + "Feel free to email me any comments, suggestions or bugs.\n\n"
            + "Credits to Min Tran for the app icon.\n" + "(http://min.frexy.com)");
        myTV.setPadding(15, 15, 15, 15);
        myTV.setTextColor(Color.WHITE);
        myTV.setTextSize(14);
        // Linkify!
        Linkify.addLinks(myTV, Linkify.ALL);

        // Try and find app version number
        String version = new String("");
        PackageManager pm = this.getPackageManager();
        try {
          // Get version number, not sure if there is a better way to do this
          version =
            " v" + pm.getPackageInfo(TimerActivity.class.getPackage().getName(), 0).versionName;
        } catch (NameNotFoundException e) {
          // No need to do anything here if it fails
          // e.printStackTrace();
        }

        return new AlertDialog.Builder(TimerActivity.this).setIcon(R.drawable.alarm_icon).setTitle(
            getString(R.string.app_name) + version).setView(myTV).setPositiveButton(
                android.R.string.ok, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked OK so do some stuff */
                  }
                })
                /*
                 * .setNeutralButton("Middle Button", new
                 * DialogInterface.OnClickListener() { public void
                 * onClick(DialogInterface dialog, int whichButton) { //User clicked
                 * Something so do some stuff } }) .setNegativeButton("Cancel", new
                 * DialogInterface.OnClickListener() { public void
                 * onClick(DialogInterface dialog, int whichButton) { //User clicked
                 * Cancel so do some stuff } })
                 */
                .create();
      case DIALOG_TIMER_COMPLETE:
        // Dialog to show when the timer is complete
        return new AlertDialog.Builder(TimerActivity.this).setTitle(R.string.timer_complete)
        .setIcon(android.R.drawable.ic_dialog_info)
        // .setMessage(R.string.timer_complete)
        // .setCancelable(false)
        .setOnCancelListener(new DialogInterface.OnCancelListener() {
          public void onCancel(DialogInterface dialog) {
            // myNM.cancelAll();
            // myTimer.stop();
            ManageNotification.clear(TimerActivity.this);
            ManageKeyguard.reenableKeyguard();
          }
        }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // myNM.cancelAll();
            // myTimer.stop();
            // myTimer.clearNotification();
            ManageNotification.clear(TimerActivity.this);
            ManageKeyguard.reenableKeyguard();
          }
        })
        // .setNeutralButton("Snooze", new DialogInterface.OnClickListener()
        // {
        // public void onClick(DialogInterface dialog, int whichButton) {
        // myNM.cancelAll();
        // }
        // })
        // .setNegativeButton("Restart", new
        // DialogInterface.OnClickListener() {
        // public void onClick(DialogInterface dialog, int whichButton) {
        // myNM.cancelAll();
        // }
        // })
        .create();
    }
    return null;
  }

  private void updateButtons() {
    updateButtons(myTimer);
  }

  private void updateButtons(Timer myTimer) {
    Log.v("TimerActivity: updateButtons()");

    Button startStopButton = (Button) findViewById(R.id.StartStopButton);
    Button pauseButton = (Button) findViewById(R.id.PauseButton);

    if (myTimer.isRunning()) {
      startStopButton.setText(R.string.stop_button_text);
      pauseButton.setEnabled(true);
      pauseButton.setFocusable(true);
    } else {
      startStopButton.setText(R.string.start_button_text);
      pauseButton.setEnabled(false);
      pauseButton.setFocusable(false);
    }
    if (myTimer.isPaused()) {
      pauseButton.setText(R.string.resume_button_text);
    } else {
      pauseButton.setText(R.string.pause_button_text);
    }
  }

  private void updateCountdown() {
    updateCountdown(myTimer);
  }

  private void updateCountdown(Timer myTimer) {
    Log.v("TimerActivity: updateCountdown()");
    myTimer.refreshTimerVals();
    int hours = myTimer.getHoursLeft();
    int mins = myTimer.getMinsLeft();
    int secs = myTimer.getSecsLeft();
    hourTextView.setText("" + (hours < 10 ? "0" : "") + hours);
    minTextView.setText("" + (mins < 10 ? "0" : "") + mins);
    secTextView.setText("" + (secs < 10 ? "0" : "") + secs);
  }

  private int getEditTextValue(EditText et) {
    try {
      return Integer.parseInt(et.getText().toString());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private int getSpinnerValue(Spinner spinner) {
    try {
      return Integer.parseInt((String) spinner.getSelectedItem());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

}
