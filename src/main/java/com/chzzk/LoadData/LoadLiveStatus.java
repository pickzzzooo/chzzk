package com.chzzk.LoadData;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.logging.Logger;

/*
https://api.chzzk.naver.com/service/v1/channels/(채널아이디)
에서 가져오는 정보들 추출

 */

public class LoadLiveStatus {

    private final OkHttpClient httpClient = new OkHttpClient();
    // 채널 정보를 가져오기
    public JsonObject getChannelInfo(String channelId) {
        String apiUrl = "https://api.chzzk.naver.com/service/v1/channels/" + channelId;

        try {
            // API 요청 생성
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .build();

            // API 요청 실행
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                System.err.println("API 요청 실패: " + response.code());
                return null;
            }

            // JSON 파싱
            String responseBody = response.body().string();
            JsonElement jsonElement = JsonParser.parseString(responseBody);
            return jsonElement.getAsJsonObject().getAsJsonObject("content");

        } catch (Exception e) {
            System.err.println("API 요청 또는 JSON 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // 채널명과 라이브상태 콘솔 출력
    public void PrintLiveStatus(String channelId, Logger logger) {
        JsonObject channelInfo = getChannelInfo(channelId);

        if (channelInfo != null) {
            // 채널 이름 가져오기
            String channelName = channelInfo.get("channelName").getAsString();

            // 생방송 상태 확인
            boolean isLive = channelInfo.get("openLive").getAsBoolean();

            logger.info("채널 이름: " + channelName);
            if (isLive) {
                logger.info("채널이 현재 생방송 중입니다.");
            } else {
                logger.warning("채널이 현재 생방송 중이 아닙니다.");
            }
        } else {
            logger.severe("채널 정보를 가져오지 못했습니다.");
        }
    }
}


