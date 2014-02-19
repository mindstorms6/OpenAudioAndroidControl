package org.bdawg.openaudio.control;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.bdawg.open_audio.Utils.OAConstants;
import org.bdawg.openaudio.Utils.Constants;
import org.bdawg.openaudio.http_utils.HttpUtils;
import org.bdawg.openaudio.webObjects.ClaimObject;

import java.io.IOException;

/**
 * Created by breland on 1/1/14.
 */
public class ClaimActivity extends BaseActivity {
    private Button claimButton;
    private String accountId;
    private TextView label;
    private EditText name;
    private EditText clientId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        this.accountId = this.getIntent().getStringExtra(Constants.ACCOUNT_ID_KEY);
        this.claimButton = (Button)this.findViewById(R.id.button_do_claim);
        this.label = (TextView) this.findViewById(R.id.label_result_claim);
        this.name = (EditText)findViewById(R.id.edit_text_name);
        this.clientId = (EditText)findViewById(R.id.edit_text_clientId);
        this.claimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClaimActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ClaimActivity.this.label.setText("Hold on... claiming.");
                    }
                });
                Thread postThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ClaimObject toPost = new ClaimObject();
                        toPost.setUserId(ClaimActivity.this.accountId);
                        toPost.setName(ClaimActivity.this.name.getText().toString());
                        toPost.setClientId(ClaimActivity.this.clientId.getText().toString());
                        try {
                            HttpResponse r = HttpUtils.executePost(OAConstants.WS_HOST + "/users", toPost);
                            if (r.getStatusLine().getStatusCode() == 200){
                                ClaimActivity.this.setResult(RESULT_OK, ClaimActivity.this.getIntent());
                                ClaimActivity.this.finish();
                            } else {
                                ClaimActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ClaimActivity.this.label.setText("Claim failed.");
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            ClaimActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ClaimActivity.this.label.setText("Claim failed.");
                                }
                            });
                        }
                    }
                });
                postThread.start();
            }
        });
    }
}