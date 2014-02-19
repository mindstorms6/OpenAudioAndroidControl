package org.bdawg.openaudio.webObjects;


/**
 * Created by breland on 2/18/14.
 */
public class PushRegister {
    private String userId;
    private String pushRegId;
    private String type;
    private String deviceId;



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPushRegId() {
        return pushRegId;
    }

    public void setPushRegId(String pushRegId) {
        this.pushRegId = pushRegId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


}
