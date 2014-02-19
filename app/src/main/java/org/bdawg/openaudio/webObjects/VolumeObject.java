package org.bdawg.openaudio.webObjects;

import java.util.Collection;
import java.util.Set;

/**
 * Created by breland on 1/4/14.
 */
public class VolumeObject {

    private int newVolume;
    private Collection<? extends String> clientIds;
    private String userId;

    public VolumeObject(){

    }

    public int getNewVolume() {
        return newVolume;
    }

    public void setNewVolume(int newVolume) {
        this.newVolume = newVolume;
    }

    public Collection< ? extends String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(Collection<? extends String> clientIds) {
        this.clientIds = clientIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}