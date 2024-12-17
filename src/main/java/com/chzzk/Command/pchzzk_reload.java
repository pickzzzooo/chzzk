package com.chzzk.Command;

import com.chzzk.Chzzk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class pchzzk_reload implements CommandExecutor {
    private final Plugin plugin;

    public pchzzk_reload(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("chzzk.reload")) { // 권한 확인 (선택 사항)
            plugin.reloadConfig(); // 플러그인 설정 리로드
            sender.sendMessage("§a[chzzk] 플러그인의 설정이 리로드되었습니다.");
            return true;
        } else {
            sender.sendMessage("§c[chzzk] 이 명령어를 사용할 권한이 없습니다.");
            return false;
        }
    }
}
