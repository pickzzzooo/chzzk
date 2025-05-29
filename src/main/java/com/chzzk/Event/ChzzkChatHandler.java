package com.chzzk.Event;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import xyz.r2turntrue.chzzk4j.ChzzkBuilder;
import xyz.r2turntrue.chzzk4j.chat.ChatEventListener;
import xyz.r2turntrue.chzzk4j.chat.ChatMessage;
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat;
import xyz.r2turntrue.chzzk4j.chat.DonationMessage;

import xyz.r2turntrue.chzzk4j.Chzzk;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
채팅 / 후원 금액, 메시지 감지 및 출력까지

 */

public class ChzzkChatHandler {
    private final ChzzkChat chat;
    private ExecutorService executor;
    private Chzzk chzzk = new ChzzkBuilder().build();

    public ChzzkChatHandler(Plugin plugin, String channelId) throws IOException {

        this.chat = chzzk.chat(channelId)
                .withChatListener(new ChatEventListener() {
                    @Override
                    public void onConnect(ChzzkChat chat, boolean isReconnecting) {
                        System.out.println("Connect received!");

                        if (!isReconnecting) {
                            chat.requestRecentChat(50);
                        }
                    }

                    @Override
                    public void onError(Exception ex) {
                        ex.printStackTrace();
                    }

                    @Override
                    public void onChat(ChatMessage msg) {
                        // config 확인
                        boolean chatMode = plugin.getConfig().getBoolean("chatMode");
                        if (chatMode == false) {return;}

                        String nickname;
                        if (msg.getProfile() == null) {
                            nickname = "익명";
                        } else {
                            nickname = msg.getProfile().getNickname();
                        }

                        //랜덤 색깔 코드 (클래스 여기 있음)
                        String rand_color_code = RandomColorCode.getRandomColorCode();

                        Bukkit.broadcastMessage("§2[채팅] " + rand_color_code + nickname + " : §f" + msg.getContent());
                    }


                    @Override
                    public void onDonationChat(DonationMessage msg) {
                        // config 확인
                        boolean donationMode = plugin.getConfig().getBoolean("donationMode");
                        if (donationMode == false) {return;}

                        String nickname;
                        if (msg.getProfile() == null) {
                            nickname = "익명";
                        } else {
                            nickname = msg.getProfile().getNickname();
                        }
                        handleDonation(msg.getPayAmount(), plugin);
                        Bukkit.broadcastMessage("§6[후원] §a" + nickname + " [" + msg.getPayAmount() + "원] " + " : §f" + msg.getContent());
                    }

                })
                .build();

        this.executor = Executors.newSingleThreadExecutor();
    }

    private void handleDonation(int amount, Plugin plugin) {
        FileConfiguration config = plugin.getConfig();

        List<String> commands = config.getStringList("후원 보상." + amount);
        if (commands.isEmpty()) {
            commands = config.getStringList("후원 보상.0");
        }

        for (String command : commands) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });
        }
    }



    public void start() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(); // 종료된 ExecutorService 재생성
        }

        executor.submit(() -> {
            try {
                chat.connectBlocking();
                Thread.sleep(700000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("ChatHandler interrupted!");
            } finally {
                close();
            }
        });
    }

    public void close() {
        try {
            if (chat != null) {
                chat.closeBlocking();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
        }
    }

    private class RandomColorCode {
        // Minecraft 색깔 코드 배열
        private static final String[] COLOR_CODES = {
                "§1", // 진한 파랑
                "§9", // 파랑
                "§b", // 밝은 청록색
                "§c", // 밝은 빨강
                "§d" // 밝은 보라색
        };

        public static String getRandomColorCode() {
            Random random = new Random();
            return COLOR_CODES[random.nextInt(COLOR_CODES.length)];
        }
    }
}

