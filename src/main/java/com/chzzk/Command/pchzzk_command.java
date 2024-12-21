package com.chzzk.Command;

import com.chzzk.Event.ChzzkChatHandler;
import com.chzzk.LoadData.LoadLiveStatus;
import com.google.gson.JsonObject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.io.IOException;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

/*
 pchzzk reload = 플러그인 리로드 & 현재 config 플레이어게 메시지 출력
 pchzzk =  현재 config 플레이어게 메시지 출력
 */

public class pchzzk_command implements CommandExecutor {
    private ChzzkChatHandler chatHandler;
    private final Plugin plugin;

    public pchzzk_command(Plugin plugin) {
        this.plugin = plugin;
    }

    private void sendStreamerInfo(CommandSender sender) {
        String channelId = plugin.getConfig().getString("channelId");
        if (channelId == null || channelId.isEmpty()) {
            sender.sendMessage("§c[chzzk] 설정 파일에서 채널 ID를 찾을 수 없습니다.");
        }
        boolean donationMode = plugin.getConfig().getBoolean("donationMode");
        boolean chatMode = plugin.getConfig().getBoolean("chatMode");

        // LoadLiveStatus를 사용하여 스트리머 정보 불러오기
        LoadLiveStatus loadLiveStatus = new LoadLiveStatus();
        JsonObject channelInfo = loadLiveStatus.getChannelInfo(channelId);

        if (channelInfo != null) {
            String channelName = channelInfo.get("channelName").getAsString();
            boolean isLive = channelInfo.get("openLive").getAsBoolean();

            // 명령어 입력자에게 인게임 메시지 전달
            sender.sendMessage("§e[스트리머 정보]");
            sender.sendMessage("§a채널 이름: §f" + channelName);
            sender.sendMessage("§a생방송 상태: §f" + (isLive ? "ON" : "OFF"));
            sender.sendMessage("§a채팅 모드: §f" + (chatMode ? "ON" : "OFF"));
            sender.sendMessage("§a도네이션 모드: §f" + (donationMode ? "ON" : "OFF"));
        } else {
            sender.sendMessage("§c[chzzk] 스트리머 정보를 가져오지 못했습니다.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // pchzzk reload
            if (!sender.hasPermission("chzzk.reload")) {
                sender.sendMessage("§c[chzzk] 이 명령어를 실행할 권한이 없습니다.");
                return true;
            }

            plugin.reloadConfig(); // 설정 리로드
            sender.sendMessage("§a[chzzk] 플러그인의 설정이 리로드되었습니다.");

            // 설정에서 channelId 값 제대로 설정 되어있는지 확인
            String channelId = plugin.getConfig().getString("channelId");

            if (channelId == null || channelId.isEmpty()) {
                sender.sendMessage("§c[chzzk] 설정 파일에서 채널 ID를 찾을 수 없습니다.");
                return true;
            }

            if (chatHandler != null) {
                chatHandler.close();
                chatHandler = null; // 객체 참조 제거
                sender.sendMessage("§a[chzzk] ChzzkChatHandler close");
            }

            sendStreamerInfo(sender);

        // pchzzk start
        } else if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
            if (chatHandler != null) {
                sender.sendMessage("§c[chzzk] ChzzkChatHandler가 이미 실행 중입니다.");
                return true;
            }

            try {
                String channelId = plugin.getConfig().getString("channelId");
                chatHandler = new ChzzkChatHandler(plugin, channelId);
                chatHandler.start();
                sender.sendMessage("§a[chzzk] ChzzkChatHandler start.");
            } catch (IOException e) {
                sender.sendMessage("§c[chzzk] ChzzkChatHandler 시작 중 오류가 발생했습니다.");
                e.printStackTrace();
            }

        // pchzzk stop
        } else if (args.length > 0 && args[0].equalsIgnoreCase("stop")) {
            if (chatHandler == null) {
                sender.sendMessage("§c[chzzk] ChzzkChatHandler가 실행 중이 아닙니다.");
                return true;
            }

            chatHandler.close();
            chatHandler = null; // 객체 참조 제거
            sender.sendMessage("§a[chzzk] ChzzkChatHandler close");

        // pchzzk
        } else {
            // pchzzk :: 현재 config 상태
            if (!sender.hasPermission("chzzk.use")) {
                sender.sendMessage("§c[chzzk] 이 명령어를 실행할 권한이 없습니다.");
                return true;
            }

            sendStreamerInfo(sender);
        }
        return true;
    }

}
