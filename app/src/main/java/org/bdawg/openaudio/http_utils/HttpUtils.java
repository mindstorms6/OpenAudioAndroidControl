package org.bdawg.openaudio.http_utils;

import android.webkit.MimeTypeMap;
import com.google.common.net.MediaType;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by breland on 12/29/13.
 */
public abstract class HttpUtils {
    public static HttpResponse executeGet(String URI)
            throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URI);
        return httpclient.execute(httpGet);
    }

    public static HttpResponse executePost(String URI, Object toPost) throws IOException{
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(URI);
        ObjectMapper mapper = new ObjectMapper();
        StringEntity entity = new StringEntity(mapper.writeValueAsString(toPost));
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        return httpClient.execute(httpPost);
    }
}
