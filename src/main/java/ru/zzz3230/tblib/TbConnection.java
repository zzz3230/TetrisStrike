package ru.zzz3230.tblib;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zzz3230.tblib.dto.CentralData;
import ru.zzz3230.tblib.dto.SentStatus;
import ru.zzz3230.tblib.exceptions.ConnectionException;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TbConnection {

    private static final Logger log = LoggerFactory.getLogger(TbConnection.class);
    private final String appName;
    private final String appSecret;

    private final String[] CENTRAL_SERVERS = {
            "https://zzz3230.github.io",
    };

    private String endpointUrl;

    public TbConnection(String appName, String appSecret) {
        this.appName = appName;
        this.appSecret = appSecret;
    }

    public URI makeRelativeUri(String path) {
        try {
            log.info("Made url {}", endpointUrl + path);
            return new URI(endpointUrl + path);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public void initialize() throws ConnectionException {
        for (String server : CENTRAL_SERVERS) {
            try {
                String response = TbHttpClient.get(server + "/central.json").get().body();
                Gson g = new Gson();
                try{
                    CentralData data = g.fromJson(response, CentralData.class);
                    Arrays.stream(data.entries()).filter(x -> x.name().equals(appName)).findFirst().flatMap(x -> Arrays.stream(data.endpoints()).filter(y -> y.name().equals(x.endpoints()[0])).findFirst()).ifPresent(z -> {
                        String[] netName = new String[1];
                        Arrays.stream(data.entries()).filter(x -> x.name().equals(appName)).findFirst().ifPresent(x ->
                        {
                            netName[0] = x.netName();
                        });

                        endpointUrl = z.primaryHost();
                        if(netName[0] != null && !netName[0].isEmpty()){
                            endpointUrl += "/" + netName[0];
                        }
                    });
                }
                catch (JsonSyntaxException e){
                    throw new ConnectionException(e.getMessage());
                }

                if(endpointUrl == null){
                    throw new ConnectionException("Entry/Endpoint URL not found");
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Initialized with endpoint: {}", endpointUrl);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    private String argsToString(Map<String, String> args) {
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : args.entrySet()) {
            String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            joiner.add(key + "=" + value);
        }
        if(appSecret != null){
            joiner.add("_sec=" + appSecret);
        }
        return joiner.toString();
    }

    public CompletableFuture<SentStatus> sendData(String action, Map<String, String> args) throws ConnectionException {
        if(endpointUrl == null){
            throw new ConnectionException("Not initialized");
        }
        return TbHttpClient.get(endpointUrl + "/" + action + "?" + argsToString(args)).thenApply(x -> {
            return new SentStatus(x.statusCode(), x.body());
        });
    }

    public <T> CompletableFuture<T> receiveData(String action, Map<String, String> args, Class<T> clazz) throws ConnectionException {
        if(endpointUrl == null){
            throw new ConnectionException("Not initialized");
        }
        Gson g = new Gson();
        return TbHttpClient.get(endpointUrl + "/" + action + "?" + argsToString(args)).thenApply(x -> g.fromJson(x.body(), clazz));
    }
}
