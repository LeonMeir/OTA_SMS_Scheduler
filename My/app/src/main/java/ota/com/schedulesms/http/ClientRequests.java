package ota.com.schedulesms.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClientRequests {
    final OkHttpClient client = new OkHttpClient.Builder().
//            connectTimeout(1, TimeUnit.MINUTES).
//            readTimeout(1, TimeUnit.MINUTES).
//            callTimeout(1, TimeUnit.MINUTES).
            build();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public String getScheduledMessages(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    public String updateScheduledMessages() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create("token=123&payload={\"sms\": [ {\"id\": 24,\"status\": \"Sent\",\"update\": 1634705635000 }, {\"id\": 25,\"status\": \"received\",\"update\": 1634705635000 }, {\"id\": 26,\"status\": \"Failed\",\"update\": 1634705635000 }] }",mediaType);
        Request request = new Request.Builder()
            .url("http://fxstudio.no-ip.org/agenda/smsNew/update.php")
            .method("POST", body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    public String post(String url,String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
