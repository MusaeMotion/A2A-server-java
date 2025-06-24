package com.musaemotion.a2a.agent.host.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/7  10:00
 * @description sse管理器
 */
@Slf4j
public class SseEmitterManager {

	private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private static final String KEY = "%s_%s";

	/**
	 * 构建key
	 * @param conversationId 交谈id
	 * @param inputMessageId 输入消息id
	 * @return
	 */
	private static String buildKey(String conversationId,String inputMessageId){
		return String.format(KEY, conversationId, inputMessageId);
	}

	/**
	 * 订阅
	 * @param conversationId
	 * @param inputMessageId
	 */
	public static  SseEmitter subscribe(String conversationId, String inputMessageId) {
		String key = buildKey(conversationId, inputMessageId);
		if (emitters.containsKey(key)) {
			return emitters.get(key);
		}
		log.info("subscribe emitter for key: {}", key);
		// TODO 暂不设置超时，后续完善客户端心跳
		SseEmitter sseEmitter = new SseEmitter(0L);
		subscribe(key, sseEmitter);
		return sseEmitter;
	}

	/**
	 * 订阅 SSE 连接。
	 * @param key key
	 * @param emitter   SSE 连接
	 */
	private static void subscribe(String key, SseEmitter emitter) {
		emitters.put(key, emitter);

		scheduler.scheduleAtFixedRate(() -> {
			SseEmitter sseEmitter = emitters.get(key);
			if (sseEmitter != null) {
				try {
					sseEmitter.send(SseEmitter.event().id(String.valueOf(System.currentTimeMillis())).data("ping", MediaType.TEXT_PLAIN).reconnectTime(30000));
				} catch (Exception e) {
					log.error("Failed to send ping to key: {} with {}", key, e.getMessage());
					emitters.remove(key);
				}
			}
		}, 0, 10, TimeUnit.SECONDS);

		emitter.onCompletion(() -> {
			log.info("onCompletion for key: {}", key);
			emitters.remove(key);
		});
		emitter.onTimeout(() -> {
			log.info("onTimeout for key: {}", key);
			emitters.remove(key);
		});
		emitter.onError((e) -> {
			log.error("onError for key: {} with {}", key, e.getMessage());
			emitters.remove(key);
		});
	}

	/**
	 * 推送给前端
	 * @param conversationId
	 * @param inputMessageId
	 * @param data
	 */
	public static void pushData(String conversationId, String inputMessageId, String eventName, String agentName, String data) {
		String key = buildKey(conversationId, inputMessageId);
		SseEmitter emitter = emitters.get(key);
		if (emitter != null) {
			try {
				log.info("pushData emitter for key: {}", key);
				emitter.send(SseEmitter.event()
						.id(String.valueOf(System.currentTimeMillis()))
						.name(eventName+"_"+agentName)
						.data(data, MediaType.TEXT_PLAIN)
				);
			} catch (Exception e) {
				log.error("Failed to send data to sessionId: {} with {}", key, e.getMessage());
				emitters.remove(key);
			}
		} else {
			log.info("No emitter found for sessionId: {}", key);
		}
	}

	/**
	 * 删除会话id
	 * @param conversationId
	 * @param inputMessageId
	 */
	public static void removeEmitter(String conversationId, String inputMessageId) {
		String key = buildKey(conversationId, inputMessageId);
		log.info("Removing emitter for key: {}", key);
		var emitter = emitters.get(key);
		if (emitter != null) {
			synchronized (emitter) {
				emitter.complete();
				emitters.remove(key);
			}
		}
	}
}
