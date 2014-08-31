package org.bdawg.openaudio.control;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.*;

import android.widget.*;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import org.apache.http.HttpResponse;

import org.apache.http.util.EntityUtils;
import org.bdawg.open_audio.Utils.OAConstants;
import org.bdawg.openaudio.Utils.Constants;
import org.bdawg.openaudio.adapters.ClientListAdapter;
import org.bdawg.openaudio.http_utils.HttpUtils;
import org.bdawg.openaudio.views.BetterPopupWindow;
import org.bdawg.openaudio.views.VolumeView;
import org.bdawg.openaudio.webObjects.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.*;

public class DevicesActivity extends BaseActivity {

    public static final int ADD_DEVICE_REQUEST_CODE = 1;

	private static final String TAG = DevicesActivity.class.getName();
	private static List<Client> mFetchedDevices;
	private String mAccountId;
    private ListView elv;
    private ProgressBar progress;
    private ImageButton playButton;
    private EditText mrlText;
    private ClientListAdapter clientAdapter;
    private ImageButton volumeButton;
    private int currentVolume;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Bundle profileBundle = this.getIntent().getBundleExtra(MainActivity.AUTH_BUNDLE_KEY);
	    String name = profileBundle.getString(AuthzConstants.PROFILE_KEY.NAME.val);
	    this.setTitle(String.format(getString(R.string.title_activity_devices), name));
        this.mAccountId = profileBundle.getString(AuthzConstants.PROFILE_KEY.USER_ID.val);
        this.elv = (ListView)findViewById(R.id.expandableListView);
        this.mrlText = (EditText)findViewById(R.id.editText);
        this.elv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (clientAdapter != null){
                    clientAdapter.onChildSelected(view, i);
                }
            }
        });
        this.elv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (clientAdapter != null){
                    final Client toUpdate = (Client) clientAdapter.getItem(i);
                    final EditText  numberTextView = new EditText(DevicesActivity.this);
                    numberTextView.setInputType(InputType.TYPE_CLASS_NUMBER);
                    AlertDialog.Builder b = new AlertDialog.Builder(DevicesActivity.this);
                    b.setView(numberTextView);
                    b.setCancelable(true);
                    b.setTitle("Set Manual Offset");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Thread postThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    OffsetObject toPost = new OffsetObject();
                                    toPost.setUserId(toUpdate.getUserId());
                                    toPost.setClientId(toUpdate.getClientId());
                                    toPost.setNewOffset(Integer.parseInt(numberTextView.getText().toString()));
                                    toPost.setNewOffset(5000);
                                    try {
                                        HttpUtils.executePost(OAConstants.WS_HOST + "/users/devices/" + toPost.getClientId(), toPost);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            postThread.start();
                        }
                    });
                    b.show();
                    return true;
                }
                return false;
            }
        });
        this.progress = (ProgressBar) findViewById(R.id.progressBar);
        this.volumeButton = (ImageButton) findViewById(R.id.volume_button);
        this.playButton = (ImageButton) findViewById(R.id.play_button);
        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Thread postThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PlaybackObject toPost = new PlaybackObject();
                            toPost.setUserId(DevicesActivity.this.mAccountId);
                            toPost.setPlayableType(OAConstants.DL_TYPE_SIMPLE_URL);
                            Map<String,String> meta = new HashMap<String, String>();
                            meta.put(OAConstants.META_SIMPLE_URL_LOCATION_KEY, DevicesActivity.this.mrlText.getText().toString());
                            meta.put(OAConstants.META_SIMPLE_URL_CAN_PLAY_DIRECT_KEY, OAConstants.FALSE_STRING);
                            toPost.setMeta(meta);
                            toPost.setClientIds(new HashSet<String>(){{addAll(DevicesActivity.this.clientAdapter.getEnabledClients());}});
                            try {
                                HttpResponse r = HttpUtils.executePost(OAConstants.WS_HOST + "control/play",toPost);
                                Log.d(TAG,"Post Status code was " + r.getStatusLine().getStatusCode());
                                Log.d(TAG,EntityUtils.toString(r.getEntity()));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    postThread.start();
                }
        });
        this.volumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VolumeView vv = new VolumeView(DevicesActivity.this);
                vv.setVolume(currentVolume);
                vv.setOnVolumeChangedListener(new VolumeView.OnVolumeChangedListener() {
                    @Override
                    public void volumeChanged(final int newVolume) {
                        Thread postThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                VolumeObject vo = new VolumeObject();
                                vo.setNewVolume(newVolume);
                                DevicesActivity.this.currentVolume=newVolume;
                                vo.setUserId(DevicesActivity.this.mAccountId);
                                List<String> toChange = new ArrayList<String>();
                                for (Client c : mFetchedDevices){
                                    toChange.add(c.getClientId());
                                }
                                vo.setClientIds(toChange);
                                try {
                                    HttpUtils.executePost(OAConstants.WS_HOST + "control/volume",vo);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        postThread.start();
                    }
                });
                BetterPopupWindow pop = new BetterPopupWindow(DevicesActivity.this.volumeButton);
                pop.setContentView(vv);
                pop.showLikeQuickAction();

            }
        });

        Thread fetchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mFetchedDevices = fetchDevices(mAccountId);
                    DevicesActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clientAdapter = new ClientListAdapter(DevicesActivity.this, mFetchedDevices);
                            DevicesActivity.this.elv.setAdapter(clientAdapter);
                            DevicesActivity.this.progress.setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        fetchThread.start();
        //TODO:Don't start this if we've already registered
        Thread regiserCallback = new Thread(new Runnable() {
            @Override
            public void run() {

                PushRegister toPost = new PushRegister();
                toPost.setUserId(DevicesActivity.this.mAccountId);
                toPost.setPushRegId(DevicesActivity.this.getRegistrationId(DevicesActivity.this));
                toPost.setType("Android");
                toPost.setDeviceId(Settings.Secure.getString(DevicesActivity.this.getContentResolver(),
                        Settings.Secure.ANDROID_ID));
                try {
                    HttpResponse r = HttpUtils.executePost(OAConstants.WS_HOST + "/users/push_register", toPost);
                    if (r.getStatusLine().getStatusCode() == 200){
                        Log.i(TAG, "Pushed reg code succcessfully");
                    } else {
                        //TODO: Mark this code for retry
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        regiserCallback.start();
	}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_DEVICE_REQUEST_CODE && resultCode == RESULT_OK){
            Thread update = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DevicesActivity.this.clientAdapter.setData(fetchDevices(DevicesActivity.this.mAccountId));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            update.start();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(false);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.devices, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			return true;
        case R.id.action_claim:
                Intent addDeviceIntent = new Intent(this,ClaimActivity.class);
                addDeviceIntent.putExtra(Constants.ACCOUNT_ID_KEY, this.mAccountId);
                this.startActivityForResult(addDeviceIntent, ADD_DEVICE_REQUEST_CODE);
                return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private List<Client> fetchDevices(String userId) throws IOException{
        List<Client> tr = new ArrayList<Client>();
        try {
            HttpResponse resp = HttpUtils.executeGet(OAConstants.WS_HOST + "/users/" + userId);
            if (resp.getStatusLine().getStatusCode() >= 200 && resp.getStatusLine().getStatusCode() <= 299){
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                tr = mapper.readValue(resp.getEntity().getContent(),new TypeReference<List<Client>>(){});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tr;
    }

}
