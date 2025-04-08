package ru.zzz3230.tblib.client;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zzz3230.tblib.TbConnection;
import ru.zzz3230.tblib.dto.LeaderboardData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NetworkClient {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger log = LoggerFactory.getLogger(NetworkClient.class);

    private final Gson gson;

    record DiscordRegisterData(String authCode, String userId, String userName, String avatarId) {}
    record LoginData(String integrationTag, String password) {}
    record AccountData(String username, int aid) {}
    record GameFinishedData(int score){}

    private final TbConnection tbConnection;
    private final LocalClient localClient;
    private String token;
    private AccountData cachedAccountData;

    public NetworkClient(TbConnection tbConnection, LocalClient localClient) {
        this.tbConnection = tbConnection;
        this.localClient = localClient;

        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS") // твой формат без зоны
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    private final SimpleDateFormat sdf;

                    {
                        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // важное место!
                    }

                    @Override
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                            throws JsonParseException {
                        try {
                            return sdf.parse(json.getAsString());
                        } catch (ParseException e) {
                            throw new JsonParseException(e);
                        }
                    }
                })
                .create();
    }

    public int getAid() {
        return cachedAccountData.aid;
    }

    public boolean alreadyLoggedIn() {
        return token != null;
    }

    public boolean validateCachedCredentials(String currentUserId){
        var authData = localClient.getLocalAuthData();
        return authData.integrationTag() != null && authData.integrationTag().equals(currentUserId);
    }

    public boolean tryLoginByCachedCredentials() {
        var authData = localClient.getLocalAuthData();
        if (authData.jwt() != null) {
            log.info("Using cached credentials for {}", authData.username());
            token = authData.jwt();
            return validateToken();
        }
        return false;
    }

    private boolean validateToken(){
        return getUsername() != null;
    }

    public boolean gameFinished(int score) {
        GameFinishedData data = new GameFinishedData(score);
        log.info("Starting game finished with {}", score);

        try {
            HttpURLConnection connection = executePostRequest(
                    "/api/app/tetris/game_finished",
                    data,
                    Collections.singletonMap("Authorization", "Bearer " + token)
            );

            return handleResponse(connection, "???", "Game finished");
        } catch (IOException e) {
            log.error("Game Finish error: ", e);
            return false;
        }
    }

    public LeaderboardData getLeaderboard(LeaderboardData.Scope scope){
        if (token == null) throw new RuntimeException("Authorization token is missing");

        log.info("Fetching leaderboard for: {}", token);

        try {
            HttpURLConnection connection = executeGetRequest(
                    "/api/app/tetris/get_leaderboard?scope=" + scope.getScope(),
                    Collections.singletonMap("Authorization", "Bearer " + token)
            );

            var resp = handleStringResponse(connection);
            return resp.map(s -> gson.fromJson(s, LeaderboardData.class)).orElse(null);

        } catch (IOException e) {
            log.error("Fetching leaderboard error: ", e);
            return null;
        }
    }

    public boolean discordRegister(String authCode, String username, String avatar, String userId) {
        DiscordRegisterData data = new DiscordRegisterData(authCode, userId, username, avatar);
        log.info("Starting registration for {}", username);

        try {
            HttpURLConnection connection = executePostRequest(
                    "/api/account/register_via_discord",
                    data,
                    Collections.emptyMap()
            );

            return handleResponse(connection, username, "Registration");
        } catch (IOException e) {
            log.error("Registration error: ", e);
            return false;
        }
    }

    public Optional<String> discordLogin(String userId) {
        LoginData data = new LoginData(userId, userId);
        log.info("Begin login of {}", userId);

        try {
            HttpURLConnection connection = executePostRequest(
                    "/api/account/generate_token",
                    data,
                    Collections.emptyMap()
            );

            return handleTokenResponse(connection);
        } catch (IOException e) {
            log.error("Login error: ", e);
            return Optional.empty();
        }
    }

    public String getUsername() {
        if (cachedAccountData != null) return cachedAccountData.username;
        if (token == null) throw new RuntimeException("Authorization token is missing");

        log.info("Fetching account info for token: {}", token);

        try {
            HttpURLConnection connection = executeGetRequest(
                    "/api/account/info",
                    Collections.singletonMap("Authorization", "Bearer " + token)
            );

            return handleAccountInfoResponse(connection);
        } catch (IOException e) {
            log.error("Account info error: ", e);
            return null;
        }
    }

    public Image getAvatar(int aid) {
        String fileName = "avatar_" + aid + ".png";
        if(!localClient.isFileExist(fileName)){
            try {
                localClient.downloadFile(tbConnection.makeRelativeUri("/api/account/avatar?aid=" + aid).toString(), fileName);
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        }
        try {
            return ImageIO.read(localClient.getFile(fileName).getRawReader());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private HttpURLConnection executePostRequest(String path, Object data, Map<String, String> headers) throws IOException {
        URL url = buildUrl(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        configureConnection(connection, "POST", headers);
        addJsonRequestBody(connection, data);

        return connection;
    }

    private HttpURLConnection executeGetRequest(String path, Map<String, String> headers) throws IOException {
        URL url = buildUrl(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        configureConnection(connection, "GET", headers);
        return connection;
    }

    private URL buildUrl(String path) throws MalformedURLException {
        return tbConnection.makeRelativeUri(path).toURL();
    }

    private void configureConnection(HttpURLConnection connection, String method, Map<String, String> headers)
            throws ProtocolException {
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        headers.forEach(connection::setRequestProperty);

        if ("POST".equals(method)) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
        }
    }

    private void addJsonRequestBody(HttpURLConnection connection, Object data) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] jsonData = gson.toJson(data).getBytes(StandardCharsets.UTF_8);
            os.write(jsonData);
        }
    }

    private boolean handleResponse(HttpURLConnection connection, String username, String operation) {
        try {
            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                log.info("{} successful for {}", operation, username);
                return true;
            }

            log.warn("{} failed. Status code: {}", operation, statusCode);
            logErrorResponse(connection);
            return false;
        } catch (IOException e) {
            log.error("Error handling response: ", e);
            return false;
        } finally {
            connection.disconnect();
        }
    }

    private Optional<String> handleTokenResponse(HttpURLConnection connection) {
        try {
            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                String responseBody = readResponseBody(connection.getInputStream());
                token = responseBody;
                return Optional.of(responseBody);
            }

            log.warn("Login failed. Status code: {}", statusCode);
            logErrorResponse(connection);
            return Optional.empty();
        } catch (IOException e) {
            log.error("Error handling token response: ", e);
            return Optional.empty();
        } finally {
            connection.disconnect();
        }
    }

    private Optional<String> handleStringResponse(HttpURLConnection connection) {
        try {
            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                String responseBody = readResponseBody(connection.getInputStream());
                return Optional.of(responseBody);
            }

            log.warn("Request failed. Status code: {}", statusCode);
            logErrorResponse(connection);
            return Optional.empty();
        } catch (IOException e) {
            log.error("Error handling token response: ", e);
            return Optional.empty();
        } finally {
            connection.disconnect();
        }
    }


    private String handleAccountInfoResponse(HttpURLConnection connection) {
        try {
            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                String jsonResponse = readResponseBody(connection.getInputStream());
                AccountData data = gson.fromJson(jsonResponse, AccountData.class);
                cachedAccountData = data;
                return data.username;
            }

            log.error("Account info request failed. Status code: {}", statusCode);
            logErrorResponse(connection);
            return null;
        } catch (IOException e) {
            log.error("Error handling account info: ", e);
            return null;
        } finally {
            connection.disconnect();
        }
    }

    private String readResponseBody(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    private void logErrorResponse(HttpURLConnection conn) {
        try {
            String errorResponse = readResponseBody(conn.getErrorStream());
            log.debug("Error response: {}", errorResponse);
        } catch (IOException ex) {
            log.trace("Failed to read error stream", ex);
        }
    }

}
