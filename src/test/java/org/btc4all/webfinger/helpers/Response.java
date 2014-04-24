package org.btc4all.webfinger.helpers;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.ByteArrayInputStream;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class Response {
    public static final ProtocolVersion HTTP = new ProtocolVersion("HTTP", 1, 1);

    public static final StatusLine FOUND = new BasicStatusLine(HTTP, 302, "Found");

    public static final StatusLine NOT_FOUND = new BasicStatusLine(HTTP, 404, "Not Found");
    public static final StatusLine FORBIDDEN = new BasicStatusLine(HTTP, 403, "Forbidden");
    public static final StatusLine BAD_REQUEST = new BasicStatusLine(HTTP, 400, "Bad Request");

    public static final StatusLine SERVER_ERROR = new BasicStatusLine(HTTP, 500, "Internal Server Error");
    public static final StatusLine SERVICE_UNAVAILABLE = new BasicStatusLine(HTTP, 502, "Service Unavailable");

    public static final StatusLine OK = new BasicStatusLine(HTTP, 200, "OK");

    public static final MockHelper testHelper = new MockHelper();

    public static HttpResponse OKResponseWithDataFromFile(String filename) {
        HttpResponse response = new BasicHttpResponse(Response.OK);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        MockData data = testHelper.getData(filename);
        httpEntity.setContent(new ByteArrayInputStream(data.getResponse().getBytes()));
        if (data.getContentType() != null) {
            httpEntity.setContentType(data.getContentType());
        }
        response.setEntity(httpEntity);
        return response;
    }

}
