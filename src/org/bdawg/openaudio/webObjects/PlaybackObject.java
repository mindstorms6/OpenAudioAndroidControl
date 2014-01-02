package org.bdawg.openaudio.webObjects;

/**
 * Created by breland on 1/1/14.
 */

        import java.util.Map;
        import java.util.Set;

public class PlaybackObject {

    private Set<String> clientIds;
    private String userId;
    private String playableType;
    private Map<String, String> meta;

    public PlaybackObject(){

    }

    public Set<String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(Set<String> clientIds) {
        this.clientIds = clientIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlayableType() {
        return playableType;
    }

    public void setPlayableType(String playableType) {
        this.playableType = playableType;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }


}
