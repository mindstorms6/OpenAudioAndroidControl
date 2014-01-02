package org.bdawg.openaudio.webObjects;

/**
 * Created by breland on 1/1/14.
 */
public class Client {

    private String userId;
    private String clientId;
    private long lastHB;
    private String name;

    public Client(){
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public long getLastHB() {
        return lastHB;
    }
    public void setLastHB(long lastHB) {
        this.lastHB = lastHB;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
