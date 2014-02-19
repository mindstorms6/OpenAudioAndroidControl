package org.bdawg.openaudio.webObjects;

public class OffsetObject {
    private String userId;
    private String clientId;
    private int newOffset;

    public OffsetObject(){

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
    public int getNewOffset() {
        return newOffset;
    }
    public void setNewOffset(int newOffset) {
        this.newOffset = newOffset;
    }
}
