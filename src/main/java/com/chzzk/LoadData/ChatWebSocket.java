package com.chzzk.LoadData;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ChatWebSocket extends WebSocketListener {
    private final Plugin plugin;
    private WebSocket webSocket;

    public ChatWebSocket(Plugin plugin) {
        this.plugin = plugin;
    }

    private String generateWebSocketUrl(String channelId) {
        int serverId = Math.abs(channelId.hashCode()) % 9 + 1;
        return "wss://kr-ss" + serverId + ".chat.naver.com/chat";
    }

    private String fetchAccessToken(String channelId) {
        String url = "https://comm-api.game.naver.com/v1/chats/access-token?channelId=" + channelId + "&chatType=STREAMING";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                return json.get("accessToken").getAsString();
            } else {
                plugin.getLogger().severe("Access Token 요청 실패: " + response.message());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Access Token 요청 중 오류 발생: " + e.getMessage());
        }
        return null;
    }

    public void connect(String channelId) {
        String url = generateWebSocketUrl(channelId);
        String accessToken = fetchAccessToken(channelId);

        if (accessToken == null) {
            plugin.getLogger().severe("Access Token을 가져오지 못했습니다. WebSocket 연결 불가.");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        webSocket = client.newWebSocket(request, this);
        plugin.getLogger().info("WebSocket 연결 성공: " + url);
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "플러그인 비활성화");
            webSocket = null;
            plugin.getLogger().info("WebSocket 연결 해제");
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                JsonObject json = JsonParser.parseString(text).getAsJsonObject();
                String username = json.get("username").getAsString();
                String message = json.get("message").getAsString();

                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage("§e[채팅] §f" + username + ": " + message);
                });
            } catch (Exception e) {
                plugin.getLogger().severe("WebSocket 메시지 처리 중 오류 발생: " + e.getMessage());
            }
        });
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        plugin.getLogger().severe("WebSocket 오류 발생: " + t.getMessage());
    }
}
