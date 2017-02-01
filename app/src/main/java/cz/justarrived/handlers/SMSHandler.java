package cz.justarrived.handlers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import cz.justarrived.R;

public class SMSHandler {

	private static final String SENT_INTENT_ACTION = "sent";

	private final Context mContext;

	public SMSHandler(Context context) {
		mContext = context;
	}

	public void sendSMS(Context context, String number, String content) {
		try {
			Intent intent = new Intent(SENT_INTENT_ACTION);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String result;

					switch (getResultCode()) {
						case Activity.RESULT_OK:
							result = mContext.getResources().getString(R.string.sms_send_result_ok);
							break;
						default:
							result = mContext.getResources().getString(R.string.sms_send_result_error);
							break;
					}
					Log.i("SMS", "" + result);
//					Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
				}
			}, new IntentFilter(SENT_INTENT_ACTION));

			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(number, null, content, pendingIntent, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}