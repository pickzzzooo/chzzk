package com.chzzk.Command;

import com.chzzk.LoadData.ChatWebSocket;
import com.chzzk.LoadData.LoadLiveStatus;
import com.google.gson.JsonObject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class pchzzk_command implements CommandExecutor {

    private final Plugin plugin;
    private final ChatWebSocket chatWebSocket;

    public pchzzk_command(Plugin plugin, ChatWebSocket chatWebSocket) {
        this.plugin = plugin;
        this.chatWebSocket = chatWebSocket;
    }

    private void sendStreamerInfo(CommandSender sender, String channelId) {
        // LoadLiveStatus를 사용하여 스트리머 정보 가져오기
        LoadLiveStatus loadLiveStatus = new LoadLiveStatus();
        JsonObject channelInfo = loadLiveStatus.getChannelInfo(channelId);

        if (channelInfo != null) {
            String channelName = channelInfo.get("channelName").getAsString();
            boolean isLive = channelInfo.get("openLive").getAsBoolean();

            // 메시지 전송
            sender.sendMessage("§e[스트리머 정보]");
            sender.sendMessage("§a채널 이름: §f" + channelName);
            sender.sendMessage("§a생방송 상태: §f" + (isLive ? "생방송 중" : "생방송 아님"));
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

            // 설정에서 channelId 가져오기
            String channelId = plugin.getConfig().getString("channelId");
            boolean chatMode = plugin.getConfig().getBoolean("chatMode");

            if (channelId == null || channelId.isEmpty()) {
                sender.sendMessage("§c[chzzk] 설정 파일에서 채널 ID를 찾을 수 없습니다.");
                return true;
            }

            // WebSocket 재연결
            if (chatMode && chatWebSocket != null) {
                chatWebSocket.disconnect();
                chatWebSocket.connect(channelId);
                sender.sendMessage("§aWebSocket이 다시 연결되었습니다.");
            }

            // 스트리머 정보 전송
            sendStreamerInfo(sender, channelId);

        } else {
            // pchzzk :: 현재 config 상태
            if (!sender.hasPermission("chzzk.use")) {
                sender.sendMessage("§c[chzzk] 이 명령어를 실행할 권한이 없습니다.");
                return true;
            }

            String channelId = plugin.getConfig().getString("channelId");
            if (channelId == null || channelId.isEmpty()) {
                sender.sendMessage("§c[chzzk] 설정 파일에서 채널 ID를 찾을 수 없습니다.");
                return true;
            }
            boolean donationMode = plugin.getConfig().getBoolean("donationMode");
            boolean chatMode = plugin.getConfig().getBoolean("chatMode");

            // 스트리머 정보 전송
            sendStreamerInfo(sender, channelId);
            sender.sendMessage("§a채팅 모드: §f" + (chatMode ? "활성화" : "비활성화"));
            sender.sendMessage("§a도네이션 모드: §f" + (donationMode ? "활성화" : "비활성화"));

        }
        return true;
    }
}
