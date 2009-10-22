package net.everythingandroid.timer;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;

public class ManageKeyguard {
  private static KeyguardManager myKM;
  private static KeyguardLock myKL;

  static void disableKeyguard(Context context) {
    myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    if (myKM.inKeyguardRestrictedInputMode()) {
      myKL = myKM.newKeyguardLock(Log.LOGTAG);
      myKL.disableKeyguard();
      Log.v("Keyguard disabled");
    }
  }

  static void reenableKeyguard() {
    if (myKL != null) {
      myKL.reenableKeyguard();
      myKL = null;
      Log.v("Keyguard reenabled");
    }
  }
}
