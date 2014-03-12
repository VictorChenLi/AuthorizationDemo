package com.example.authorizationdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Button btn_greeting;
	private TextView tv_hello;
	private MainActivity demo;
	private String mEmail;
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    private static final String TAG = "TokenInfoTask";

    public static final String EXTRA_ACCOUNTNAME = "extra_accountname";

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        demo=this;
        btn_greeting = (Button) this.findViewById(R.id.btn_greeting);
        tv_hello = (TextView) this.findViewById(R.id.tv_hello);
        addListener();
        Bundle extras = getIntent().getExtras();
        if (extras!=null&&extras.containsKey(EXTRA_ACCOUNTNAME)) {
            mEmail = extras.getString(EXTRA_ACCOUNTNAME);
            new GetTokenTask(demo).execute();
        }
    }
    
    /** Starts an activity in Google Play Services so the user can pick an account */
    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }
    
    /** Attempt to get the user name. If the email address isn't known yet,
     * then call pickUserAccount() method so the user can pick an account.
     */
    private void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        }
        else
        {
        	new GetTokenTask(demo).execute();
        }
    }
    
    /** Checks whether the device currently has a network connection */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
        	handleAuthorizeResult(resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void handleAuthorizeResult(int resultCode, Intent data) {
        if (data == null) {
            show("Unknown error, click the button again");
            return;
        }
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "Retrying");
            new GetTokenTask(demo).execute();
            return;
        }
        if (resultCode == RESULT_CANCELED) {
            show("User rejected authorization.");
            return;
        }
        show("Unknown error, click the button again");
    }
    
    public void show(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_hello.setText(message);
            }
        });
    }
    
    private void addListener()
    {
    	btn_greeting.setOnClickListener(new OnClickListener(){


    	    private final String NAME_KEY = "given_name";
    	    
    	    /**
    	     * Reads the response from the input stream and returns it as a string.
    	     */
    	    private String readResponse(InputStream is) throws IOException {
    	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	        byte[] data = new byte[2048];
    	        int len = 0;
    	        while ((len = is.read(data, 0, data.length)) >= 0) {
    	            bos.write(data, 0, len);
    	        }
    	        return new String(bos.toByteArray(), "UTF-8");
    	    }

    	    /**
    	     * Parses the response and returns the first name of the user.
    	     * @throws JSONException if the response is not JSON or if first name does not exist in response
    	     */
    	    private String getFirstName(String jsonResponse) throws JSONException {
    	      JSONObject profile = new JSONObject(jsonResponse);
    	      return profile.getString(NAME_KEY);
    	    }
    	    
			@Override
			public void onClick(View arg0) {
				if(mEmail==null)
				{
					demo.pickUserAccount();
				}
				else
					new GetTokenTask(demo).execute();
				
			}
    		
    	});
    }
    
    public class GetTokenTask extends AsyncTask<Void, Void, Void>{

    	private final String NAME_KEY = "given_name";
	    private final MainActivity context;
	    
	    public GetTokenTask(MainActivity mActivity)
	    {
	    	context = mActivity;
	    }
    	
	    /**
	     * Reads the response from the input stream and returns it as a string.
	     */
	    private String readResponse(InputStream is) throws IOException {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        byte[] data = new byte[2048];
	        int len = 0;
	        while ((len = is.read(data, 0, data.length)) >= 0) {
	            bos.write(data, 0, len);
	        }
	        return new String(bos.toByteArray(), "UTF-8");
	    }

	    /**
	     * Parses the response and returns the first name of the user.
	     * @throws JSONException if the response is not JSON or if first name does not exist in response
	     */
	    private String getFirstName(String jsonResponse) throws JSONException {
	      JSONObject profile = new JSONObject(jsonResponse);
	      return profile.getString(NAME_KEY);
	    }
	    
		@Override
		protected Void doInBackground(Void... arg0) {
			try{
			// got the token from google auth service with email account
			String token = GoogleAuthUtil.getToken(getApplicationContext(),mEmail,SCOPE);
			// verify the token from google auth server
			URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();
	        int sc = con.getResponseCode();
	        if (sc == 200) {
	          InputStream is = con.getInputStream();
	          String name = getFirstName(readResponse(is));
	          context.show("Hello " + name + "!");
	          is.close();
	        } else if (sc == 401) {
	            GoogleAuthUtil.invalidateToken(getApplicationContext(), token);
	            Log.i(TAG, "Server auth error: " + readResponse(con.getErrorStream()));
	        } else {
	        }
		
		} catch (UserRecoverableAuthException userRecoverableException) {
			// TODO Auto-generated catch block
			userRecoverableException.printStackTrace();
			demo.handleException(userRecoverableException);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
			return null;
		}
    	
    }
    
    /**
     * This method is a hook for background threads and async tasks that need to provide the
     * user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
