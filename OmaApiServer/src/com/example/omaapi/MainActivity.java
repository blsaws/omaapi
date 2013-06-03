/*
 * Copyright 2013 Bryan Sullivan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.omaapi;

import com.example.omaapi.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static Intent service = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context context = getBaseContext();

    	if (service == null) {
    		service = new Intent(context, OmaApiService.class);
      		context.startService(service); 
            Toast toast = Toast.makeText(getBaseContext(), 
            		"OMA API server is started,\n listening on http://localhost:4035", Toast.LENGTH_LONG);
            toast.show();
    	}

    	setContentView(R.layout.activity_main);
	}
	
	private void stopServer() {
		if (service != null) {
			stopService(service);
			service = null;
			Toast toast = Toast.makeText(getBaseContext(), 
	        		"OMA API server is stopped", Toast.LENGTH_LONG);
	        toast.show();
		}		
	}
	
    public void quitServer(View view) {
    	stopServer();
    	finish();
    }

	@Override
	protected void onResume() {
         super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
