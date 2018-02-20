package uk.ac.ebi.subs.ena.http;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
public class UnirestWrapper {

    @Value("${ena.login_name:Webin-46220}")
    String username;

    @Value("${ena.password:Nq9F5Ig}")
    String password;

    @Value("${ena.submission.url:https://www-test.ebi.ac.uk/ena/submit/drop-box/submit}")
    String submissionUrl;

    public HttpResponse<String> postJson(Map<String, Field> parameters ){
        HttpResponse<String> response = null;
        try {
            final HttpRequestWithBody httpRequestWithBody = Unirest.post(submissionUrl).basicAuth(username, password);

            MultipartBody multipartBody = null;

            for (String key :parameters.keySet()) {
                if (multipartBody != null) {
                    multipartBody = multipartBody.field(key, parameters.get(key).inputStream, parameters.get(key).filename);
                } else {
                    multipartBody = httpRequestWithBody.field(key, parameters.get(key).inputStream, parameters.get(key).filename);
                }

            }

            response = httpRequestWithBody.asString();

        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public static final class Field {
        String filename;
        InputStream inputStream;

        public Field(String filename, InputStream inputStream) {
            this.filename = filename;
            this.inputStream = inputStream;
        }


    }
}