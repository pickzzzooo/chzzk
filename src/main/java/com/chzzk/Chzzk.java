package com.chzzk;

import com.chzzk.Command.pchzzk_command;
import com.chzzk.LoadData.LoadLiveStatus;
import com.chzzk.LoadData.ChatWebSocket;
import org.bukkit.plugin.java.JavaPlugin;

public final class Chzzk extends JavaPlugin {
    private ChatWebSocket chatWebSocket;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String channelId = getConfig().getString("channelId");
        boolean chatMode = getConfig().getBoolean("chatMode");

        if (channelId == null || channelId.isEmpty()) {
            getLogger().severe("config.yml에 channelId가 설정되지 않았습니다!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("chzzk 플러그인이 활성화되었습니다.");

        // LoadLiveStatus를 사용하여 생방송 상태 출력
        LoadLiveStatus loadLiveStatus = new LoadLiveStatus();
        loadLiveStatus.PrintLiveStatus(channelId, getLogger());

        // WebSocket 연결
        if (chatMode) {
            chatWebSocket = new ChatWebSocket(this);
            chatWebSocket.connect(channelId);
        }

        // 명령어 등록
        if (getCommand("pchzzk") != null) {
            getCommand("pchzzk").setExecutor(new pchzzk_command(this, chatWebSocket));
        }
    }

    @Override
    public void onDisable() {
        if (chatWebSocket != null) {
            chatWebSocket.disconnect();
        }
        getLogger().info("플러그인이 비활성화되었습니다.");
    }
}
