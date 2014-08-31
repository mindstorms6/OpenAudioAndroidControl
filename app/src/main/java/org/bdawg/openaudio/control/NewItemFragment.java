package org.bdawg.openaudio.control;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

/**
 * Created by breland on 2/25/14.
 */
public class NewItemFragment extends BaseFragment {

    ViewSwitcher switcher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        switcher = (ViewSwitcher)inflater.inflate(R.layout.frag_switcher,null);
        switcher.addView(inflater.inflate(R.layout.frag_pick_source,null));
        switcher.addView(inflater.inflate(R.layout.frag_pick_device, null));
        return switcher;
    }

    @Override
    public String getTitle() {
        return "New Playback";
    }

    @Override
    public String getDrawerTitle(Context c) {
        return "New playback";
    }


}
