package com.chzzk;

import com.chzzk.Command.pchzzk_command;
import com.chzzk.Event.ChzzkChatHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        //config 불러오기: channelId(연결할 생방송 채널 키 값)
        saveDefaultConfig();

        String channelId = getConfig().getString("channelId");

        if (channelId == null || channelId.isEmpty()) {
            getLogger().severe("config.yml에 channelId가 설정되지 않았습니다!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("chzzk 플러그인이 활성화되었습니다.");

        // 명령어 등록
        if (getCommand("pchzzk") != null) {
            getCommand("pchzzk").setExecutor(new pchzzk_command(this));
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("chzzk 플러그인이 비활성화됩니다.");
    }
}