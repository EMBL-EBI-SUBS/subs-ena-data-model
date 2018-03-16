package uk.ac.ebi.subs.ena.http;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

@Service
public class UniRestWrapper {

    @Value("${ena.login_name}")
    String username;

    @Value("${ena.password}")
    String password;

    @Value("${ena.submission.url:https://www-test.ebi.ac.uk/ena/submit/drop-box/submit}")
    String submissionUrl;

    public String postJson(Map<String, Field> parameters ){
        String response;
        try {
            final HttpRequestWithBody httpRequestWithBody = Unirest.post(submissionUrl).basicAuth(username, password);
            final Iterator<String> keyIterator = parameters.keySet().iterator();

            if (keyIterator.hasNext()) {
                String key = keyIterator.next();
                MultipartBody multipartBody = httpRequestWithBody.field(key.toUpperCase(), parameters.get(key).inputStream, parameters.get(key).filename);
                while (keyIterator.hasNext()) {
                    key = keyIterator.next();
                    multipartBody = multipartBody.field(key.toUpperCase(), parameters.get(key).inputStream, parameters.get(key).filename);
                }
            }

            response = httpRequestWithBody.asString().getBody();

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