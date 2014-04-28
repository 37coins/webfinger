package org.btc4all.webfinger.helpers;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class Response {
    public static final ProtocolVersion HTTP = new ProtocolVersion("HTTP", 1, 1);

    public static HttpResponse found () { return create(302, "Found"); }
    public static HttpResponse notFound() { return create(404, "Not Found"); }

    public static HttpResponse create(int statusCode, String reason) {
        return new BasicHttpResponse(new BasicStatusLine(HTTP, statusCode, reason));
    }

    public static HttpResponse create(int statusCode) {
        return new BasicHttpResponse(new BasicStatusLine(HTTP, statusCode, ""));
    }

    public static HttpResponse OKResponseWithDataFromFile(String filename) {
        try {
            HttpResponse response = create(200, "OK");
            BasicHttpEntity httpEntity = new BasicHttpEntity();
            httpEntity.setContent(new FileInputStream("src/test/fixtures/" + filename));
            response.setEntity(httpEntity);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
