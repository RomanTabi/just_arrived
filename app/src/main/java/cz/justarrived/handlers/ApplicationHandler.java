package cz.justarrived.handlers;

import android.content.Context;

import cz.justarrived.Constants;

public class ApplicationHandler {

  public enum AppState {
    IDLE,
    TRACKING
  }

  private static AppState sAppState;

  private static final ApplicationHandler INSTANCE = new ApplicationHandler();

  private ApplicationHandler() {}

  public static ApplicationHandler getInstance() {
    return INSTANCE;
  }

  public AppState getApplicationState(Context context) {
    if (sAppState == null) {
      boolean isTracking = SharedPreferencesHandler.getSharedPreference(context, Constants.SERVICE_RUNNING_KEY, false);

      if (isTracking) {
        sAppState = AppState.TRACKING;
      } else {
        sAppState = AppState.IDLE;
      }
    }
    return sAppState;
  }

  public void setApplicationState(Context context, AppState state) {
    sAppState = state;

    if (state.equals(AppState.IDLE)) {
      SharedPreferencesHandler.setSharedPreference(context, Constants.SERVICE_RUNNING_KEY, false);
    } else if (state.equals(AppState.TRACKING)) {
      SharedPreferencesHandler.setSharedPreference(context, Constants.SERVICE_RUNNING_KEY, true);
    }
  }
}
