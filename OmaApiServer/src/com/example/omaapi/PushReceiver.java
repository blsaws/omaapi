package com.example.omaapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

 public class PushReceiver extends BroadcastReceiver {
	 @Override 
	 public void onReceive(Context context, Intent intent) { 
		 Bundle bundle = intent.getExtras();
		 String str = intent.getAction()+":";
		 String data = "";
		 if (bundle != null) { 
			if (intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
				 String mimeType = intent.getType();
				 str = "mimeType:"+mimeType;
				 String headers = intent.getStringExtra("header");
				 data = intent.getStringExtra("data");
				 str += "/n" + data;
				 String contentTypeParameters = intent.getStringExtra("contentTypeParameters");
				 // TODO: Filter based upon
				 // pushSource.acceptSource
				 // pushSource.acceptContentType
				 // pushSource.acceptApplicationId
//				pushSource.onmessage(headers+"\n"+str); 
			}
			else {
				int i = 0;
				SmsMessage[ ] msgs = null; 
				Object[ ] pdus = (Object[ ]) bundle.get("pdus"); 
				msgs = new SmsMessage[pdus.length];
				String source;
				for (i=0; i<msgs.length; i++) { 
					msgs[i] = SmsMessage.createFromPdu((byte[ ])pdus[i]);
					source = msgs[i].getOriginatingAddress();
					str += "Message from sms:" + source; 
					str += " :";
					data = msgs[i].getMessageBody().toString();
					str += data;
					// TODO: Need to deliver to all PushSource objects created (there could be more than one)
				}
//	            Toast toast = Toast.makeText(context, 
//	            		"PushReceiver event:\n"+str, Toast.LENGTH_LONG);
//	            toast.show();			
		    	Intent pushevent = new Intent(context, OmaApiService.class);
		    	pushevent.setAction("pushevent");
		    	pushevent.putExtra("data",str);
		      	context.startService(pushevent); 	
		    }
		 }
	 }
}