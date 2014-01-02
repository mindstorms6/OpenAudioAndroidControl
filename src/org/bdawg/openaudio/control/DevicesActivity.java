package org.bdawg.openaudio.control;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.*;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import org.apache.http.HttpResponse;

import org.apache.http.util.EntityUtils;
import org.bdawg.openaudio.Utils.Constants;
import org.bdawg.openaudio.Utils.OAConstants;
import org.bdawg.openaudio.adapters.ClientListAdapter;
import org.bdawg.openaudio.http_utils.HttpUtils;
import org.bdawg.openaudio.webObjects.Client;
import org.bdawg.openaudio.webObjects.PlaybackObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import sun.misc.IOUtils;

import java.io.IOException;
import java.util.*;

public class DevicesActivity extends Activity {

    public static final int ADD_DEVICE_REQUEST_CODE = 1;

	private static final String TAG = DevicesActivity.class.getName();
	private static List<Client> mFetchedDevices;
	private String mAccountId;
    private ListView elv;
    private ProgressBar progress;
    private ImageButton playButton;
    private EditText mrlText;
    private ClientListAdapter clientAdapter;
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
        this.progress = (ProgressBar) findViewById(R.id.progressBar);
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
                ObjectMapper mapper = new ObjectMapper();
                tr = mapper.readValue(resp.getEntity().getContent(),new TypeReference<List<Client>>(){});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tr;
    }

}
