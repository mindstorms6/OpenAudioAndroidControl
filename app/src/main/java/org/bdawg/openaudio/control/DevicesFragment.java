package org.bdawg.openaudio.control;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
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

public class DevicesFragment extends BaseFragment {

    public static final int ADD_DEVICE_REQUEST_CODE = 1;

	private static final String TAG = DevicesFragment.class.getName();
	private static List<Client> mFetchedDevices;
	private String mAccountId;
    private ListView elv;
    private ProgressBar progress;
    private ImageButton playButton;
    private EditText mrlText;
    private ClientListAdapter clientAdapter;
    private ImageButton volumeButton;
    private int currentVolume;
    private String title;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View v = inflater.inflate(R.layout.activity_devices, null);

        Bundle profileBundle = this.getActivity().getIntent().getBundleExtra(MainActivity.AUTH_BUNDLE_KEY);
        String name = profileBundle.getString(AuthzConstants.PROFILE_KEY.NAME.val);
        this.setTitle(String.format(getString(R.string.title_activity_devices), name));
        this.mAccountId = profileBundle.getString(AuthzConstants.PROFILE_KEY.USER_ID.val);
        this.elv = (ListView)v.findViewById(R.id.expandableListView);
        this.mrlText = (EditText)v.findViewById(R.id.editText);
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
                    final EditText  numberTextView = new EditText(DevicesFragment.this.getActivity());
                    numberTextView.setInputType(InputType.TYPE_CLASS_NUMBER);
                    AlertDialog.Builder b = new AlertDialog.Builder(DevicesFragment.this.getActivity());
                    b.setView(numberTextView);
                    b.setCancelable(true);
                    b.setTitle(DevicesFragment.this.getString(R.string.manual_offset_title));
                    b.setPositiveButton(DevicesFragment.this.getString(R.string.manual_offset_ok_button_title), new DialogInterface.OnClickListener() {
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
        this.progress = (ProgressBar) v.findViewById(R.id.progressBar);
        this.volumeButton = (ImageButton) v.findViewById(R.id.volume_button);
        this.playButton = (ImageButton) v.findViewById(R.id.play_button);
        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread postThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PlaybackObject toPost = new PlaybackObject();
                        toPost.setUserId(DevicesFragment.this.mAccountId);
                        toPost.setPlayableType(OAConstants.DL_TYPE_SIMPLE_URL);
                        Map<String,String> meta = new HashMap<String, String>();
                        meta.put(OAConstants.META_SIMPLE_URL_LOCATION_KEY, DevicesFragment.this.mrlText.getText().toString());
                        meta.put(OAConstants.META_SIMPLE_URL_CAN_PLAY_DIRECT_KEY, OAConstants.FALSE_STRING);
                        toPost.setMeta(meta);
                        toPost.setClientIds(new HashSet<String>(){{addAll(DevicesFragment.this.clientAdapter.getEnabledClients());}});
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
                VolumeView vv = new VolumeView(DevicesFragment.this.getActivity());
                vv.setVolume(currentVolume);
                vv.setOnVolumeChangedListener(new VolumeView.OnVolumeChangedListener() {
                    @Override
                    public void volumeChanged(final int newVolume) {
                        Thread postThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                VolumeObject vo = new VolumeObject();
                                vo.setNewVolume(newVolume);
                                DevicesFragment.this.currentVolume=newVolume;
                                vo.setUserId(DevicesFragment.this.mAccountId);
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
                BetterPopupWindow pop = new BetterPopupWindow(DevicesFragment.this.volumeButton);
                pop.setContentView(vv);
                pop.showLikeQuickAction();

            }
        });

        Thread fetchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mFetchedDevices = fetchDevices(mAccountId);
                    DevicesFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clientAdapter = new ClientListAdapter(DevicesFragment.this.getActivity(), mFetchedDevices);
                            DevicesFragment.this.elv.setAdapter(clientAdapter);
                            DevicesFragment.this.progress.setVisibility(View.INVISIBLE);
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
                toPost.setUserId(DevicesFragment.this.mAccountId);
                toPost.setPushRegId(BaseActivity.getRegistrationId(DevicesFragment.this.getActivity()));
                toPost.setType("Android");
                toPost.setDeviceId(Settings.Secure.getString(DevicesFragment.this.getActivity().getContentResolver(),
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
        return v;

    }

    @Override
	public void onAttach(Activity activity) {
        super.onAttach(activity);
		// Show the Up button in the action bar.

	}


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_DEVICE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Thread update = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DevicesFragment.this.clientAdapter.setData(fetchDevices(DevicesFragment.this.mAccountId));
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.devices, menu);
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
                Intent addDeviceIntent = new Intent(this.getActivity(),ClaimActivity.class);
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

    private void setTitle(String title){
        this.title = title;
    }

    @Override
    public String getTitle() {
       return this.title;
    }

    @Override
    public String getDrawerTitle(Context c) {
        return c.getString(R.string.devices_drawer_title);
    }
}
