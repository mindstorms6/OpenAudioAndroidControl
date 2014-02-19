package org.bdawg.openaudio.webObjects;

/**
 * Created by breland on 1/1/14.
 */
public class ClaimObject{

private String clientId;
private String userId;
private String name;

public ClaimObject(){

        }

public String getClientId() {
        return clientId;
        }
public void setClientId(String clientId) {
        this.clientId = clientId;
        }
public String getUserId() {
        return userId;
        }
public void setUserId(String userId) {
        this.userId = userId;
        }

public String getName() {
        return name;
        }

public void setName(String name) {
        this.name = name;
        }
}