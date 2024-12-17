package com.chzzk;

import com.chzzk.Command.pchzzk_reload;
import com.chzzk.LoadData.LoadLiveStatus;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

public final class Chzzk extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        String channelId = getConfig().getString("channelId");
        if (channelId == null || channelId.isEmpty()) {
            getLogger().severe("config.yml에 channelId가 설정되지 않았습니다!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("플러그인이 활성화되었습니다.");

        // LoadLiveStatus를 사용하여 생방송 상태 출력
        LoadLiveStatus loadLiveStatus = new LoadLiveStatus();
        loadLiveStatus.PrintLiveStatus(channelId, getLogger());

        getCommand("pchzzk").setExecutor(new pchzzk_reload(this));
    }



    @Override
    public void onDisable() {
        getLogger().info("플러그인이 비활성화되었습니다.");
    }
}
