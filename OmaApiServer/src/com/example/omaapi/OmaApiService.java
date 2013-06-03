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

import java.io.IOException;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import com.example.omaapi.SampleHttpd.Response.Status;
import com.example.omaapi.R;

public class OmaApiService extends Service {
	public static HttpListener httpd = null;
	private Context context;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//      Toast t = Toast.makeText(getBaseContext(), "OmaApiService (re)started\nIntent Category: "+intent.getAction(), Toast.LENGTH_LONG);
//		t.show();
		context = getApplicationContext();
		
		if (httpd == null) {
			httpd = new HttpListener(this, Integer.parseInt(this.getString(R.string.port)));
			try {
				httpd.start();
			}
			catch (IOException ioe) {
	      		// error handling
	    	}
		}
		
		if (intent.getAction() == "pushevent") {
			SampleHttpd.pushEvent = intent.getStringExtra("data");
            Toast toast = Toast.makeText(getBaseContext(), 
            		"OmaApiService event:\n"+SampleHttpd.pushEvent, Toast.LENGTH_LONG);
            toast.show();
		}
		
    return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if (httpd != null) httpd.stop();
        super.onDestroy();
	}

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        OmaApiService getService() {
            return OmaApiService.this;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }   
    
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    public class HttpListener extends SampleHttpd {
    	private int http_port;

        public HttpListener(Service service, int port) {
            super(port);
        	http_port = port;
        }

        @Override
        public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
            // System.out.println(method + " '" + uri + "' ");

        	int mc = uri.indexOf("/mc");
            int cmapi = uri.indexOf("/cmapi");
            int push = uri.indexOf("/push");

            String choice = parms.get("choice");
            if (choice != null) {
            	if (choice.equalsIgnoreCase("mc")) mc = 0;
            	if (choice.equalsIgnoreCase("cmapi")) cmapi = 0;
            }
            Response resp;
            if (push==0) resp = handlePush(uri, parms);
            else if (mc==0) resp = handleMC();
            else if (cmapi == 0) resp = handleCMAPI();
            else {
                String msg = "<html><body><h1>Request to "+method + " '" + uri + "'\n"+
                		"Hello from AT&T HTTP Listener at port " + http_port + "</h1>\n";

                msg += "<form action='?' method='get'>\n" +
                       "<h1>Please select:</h1>\n" +
                       "<input type='radio' name='choice' value='mc' checked='yes'/>Mobile Code<br/>\n" +
                       "<input type='radio' name='choice' value='cmapi'/>OpenCMAPI<br/>\n" +
                       "<input type='submit' value='SUBMIT'/>" +
                       "</form>\n";	
        /*
                    if (parms.get("username") == null) {
                        msg += "<form action='?' method='get'>\n" +
                               "<p>Your name: <input type='text' name='username'></p>\n" +
                               "</form>\n";
                    }
                    else {
                    	msg += "<p>How are you, " + parms.get("username") + "?</p>";
                    }
        */
                msg += "</body></html>\n";
                    
                resp = new Response(msg);        	            	
            }
            return(resp);
        }
        
    	public boolean mc_async_response = false;
    	private String mc_result = "This is supposed to be mc scanning result";
        private Response handleMC() {
            mc_async_response = false;
            Intent intent = new Intent(context, ScanCodeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            while (!mc_async_response) {}            
            SampleHttpd.Response resp = new SampleHttpd.Response(Status.OK,"text/plain", mc_result);
            resp.addHeader("Access-Control-Allow-Origin","*");
            return resp;        	
        }

        public void scanResult(String result) {
        	mc_result = result;
        	mc_async_response = true;
        }
        
        private Response handlePush(String uri, Map<String, String> parms) {
            // WRAPI Push API
        	// Set the accepted sources filter to be used by the Push API server. See where this
        	// is used in SampleHttpd.
        	if (parms.get("push-accept-source")!= null) {
        		pushAcceptSource = parms.get("push-accept-source");
        		if (pushAcceptSource.indexOf(' ')==0) pushAcceptSource = pushAcceptSource.substring(1);
        	}
        	else {
        		pushAcceptSource = "any";
        	}
        	// Log the event
        	System.out.println("OMA API server received Push API request for source: "+pushAcceptSource);
        	// 
            Response resp = new Response(Status.OK,"text/event-stream","");
            resp.addHeader("Access-Control-Allow-Origin","*");
            resp.eventsource = true; // This is a flag to to the server to not close the connection...
            return resp;       	
        }
        
        private Response handleCMAPI() {
            Response resp = new Response(Status.OK,"text/html","<html><body><h1>You have chosen CMAPI</h1></body></html>");
            resp.addHeader("Access-Control-Allow-Origin","*");
            return resp;
        }
    }	
}
