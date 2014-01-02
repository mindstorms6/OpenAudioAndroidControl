package org.bdawg.openaudio.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.bdawg.openaudio.control.R;
import org.bdawg.openaudio.webObjects.Client;

import java.util.*;

/**
 * Created by breland on 12/29/13.
 */
public class ClientListAdapter extends BaseAdapter{

    private List<Client> data;
    private Map<Client, Boolean> states;
    private Activity c;

    public ClientListAdapter(Activity cxt,List<Client> incoming){
        this.c = cxt;
        setData(incoming);
    }

    public void onChildSelected(View v, int childPosition){
        ImageView tb = (ImageView)v.findViewById(R.id.toggleButton);
        Client c = (Client)getItem(childPosition);
        boolean newState = !states.get(c);
        tb.setImageDrawable(getDrawableForState(newState));
        states.put(c, newState);
        notifyDataSetChanged();
    }

    private Drawable getDrawableForState(boolean state){
        return this.c.getResources().getDrawable(state ? android.R.drawable.presence_online : android.R.drawable.presence_busy);
    }

    public List<String> getEnabledClients(){
        List<String> tr = new ArrayList<String>();
        for (Map.Entry<Client, Boolean> entry : states.entrySet()){
            if (entry.getValue()){
                tr.add(entry.getKey().getClientId());
            }
        }
        return tr;
    }

    @Override
    public int getCount() {
        return this.data.size();
    }

    @Override
    public Object getItem(int i) {
        return this.data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Client client = (Client) getItem(i);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.elv_client, null);
        }

        TextView listItem = (TextView) view.findViewById(R.id.lblListItem);
        listItem.setText("[" + client.getClientId() + "] " + client.getName());
        ImageView tb = (ImageView)view.findViewById(R.id.toggleButton);
        tb.setImageDrawable(getDrawableForState(states.get(client)));
        return  view;
    }

    public void setData(List<Client> newClients){
        this.data = newClients;
        this.states = new HashMap<Client, Boolean>();
        for (Client c : this.data){
            states.put(c,false);
        }
        this.c.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
