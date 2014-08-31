package org.bdawg.openaudio.control;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;

public class MainActivity extends BaseActivity {

	private static final String TAG = MainActivity.class.getName();
	private AmazonAuthorizationManager mAuthManager;
	private ImageButton mLoginButton;
	private TextView mLoginStatus;

	public static final String AUTH_BUNDLE_KEY = "auth_bundle";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
		setContentView(R.layout.activity_main);
		this.mLoginButton = (ImageButton) findViewById(R.id.loginAMZNButton);
		this.mLoginStatus = (TextView) findViewById(R.id.loginStatusText);
		this.mLoginButton.setVisibility(View.INVISIBLE);
		this.mLoginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAuthManager.authorize(new String[] { "profile" },
						Bundle.EMPTY, new AuthorizationListener() {

							@Override
							public void onSuccess(Bundle arg0) {
								authGood(arg0);

							}

							@Override
							public void onError(AuthError arg0) {
								authBad();

							}

							@Override
							public void onCancel(Bundle arg0) {
								authBad();
							}
						});
			}
		});
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// check if still authorized!
		mAuthManager.getToken(new String[] { "profile" }, new APIListener() {
			@Override
			public void onSuccess(Bundle arg0) {
				authGood(arg0);
			}

			@Override
			public void onError(AuthError arg0) {
				authBad();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void authGood(final Bundle returnedBundle) {
		Thread doProfileGetThread = new Thread(new Runnable() {
			@Override
			public void run() {
				MainActivity.this.mAuthManager.getProfile(new APIListener() {

					@Override
					public void onSuccess(Bundle arg0) {
						// go on to the next activity!
						Bundle profileBundle = arg0
								.getBundle(AuthzConstants.BUNDLE_KEY.PROFILE.val);
						if (profileBundle == null) {
							MainActivity.this.mAuthManager
									.clearAuthorizationState(new APIListener() {

										@Override
										public void onSuccess(Bundle arg0) {
											authBad();
										}

										@Override
										public void onError(AuthError arg0) {
											Log.e(TAG, "Error logging out!");
											authBad();
										}
									});
						} else {
							MainActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									MainActivity.this.mLoginButton
											.setVisibility(View.INVISIBLE);
									MainActivity.this.mLoginStatus
											.setText(R.string.main_authorized);
								}
							});
							Intent deviceIntent = new Intent(MainActivity.this,
									NavActivity.class);
							deviceIntent
									.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
							deviceIntent
									.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							deviceIntent.putExtra(AUTH_BUNDLE_KEY, profileBundle);
							MainActivity.this.startActivity(deviceIntent);
						}
					}

					@Override
					public void onError(AuthError arg0) {
						MainActivity.this.authBad();

					}
				});

			}
		});
		doProfileGetThread.start();
	}

	private void authBad() {
		MainActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mLoginButton.setVisibility(View.VISIBLE);
				mLoginStatus.setText(R.string.main_not_authorized);
			}
		});

	}

}
