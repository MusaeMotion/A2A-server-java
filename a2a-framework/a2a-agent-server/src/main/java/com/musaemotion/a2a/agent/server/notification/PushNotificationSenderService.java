/*
 * Copyright (c) 2025 MusaeMotion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musaemotion.a2a.agent.server.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.musaemotion.a2a.common.notification.PushNotificationAuth;
import com.musaemotion.a2a.common.utils.GuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.notification
 * @project：A2A
 * @date：2025/4/22 16:32
 * @description：推送服务
 */
@Slf4j
@Service
public class PushNotificationSenderService extends PushNotificationAuth {

    private String jwkJson ="";

    private PrivateKey privateKey;

    public PushNotificationSenderService() {
        generateJwk();
    }

    /**
     * 验证推送url
     * @param url
     * @return
     */
    public static CompletableFuture<Boolean> verifyPushNotificationUrl(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url + "?validationToken=" + GuidUtils.createShortRandomGuid());
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    boolean isVerified = responseBody.equals(request.getURI().getQuery().split("=")[1]);
                    log.info("Verified push-notification URL: {} => {}", url, isVerified);
                    return isVerified;
                }
            } catch (Exception e) {
                log.error("verifyPushNotificationUrl Error during sending push-notification for URL {}: {}", url, e.getMessage());
                return false;
            }
        });
    }


    /**
     * 生成jwk对象
     * @param rsaKey
     * @return
     */
    private String jwkJson(RSAKey rsaKey) throws JsonProcessingException {
        // 使用 Jackson 序列化 JWK Set
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode jwkSetObjectNode = mapper.createObjectNode();
        ArrayNode keys = mapper.createArrayNode();

        ObjectNode keyNode = mapper.createObjectNode();
        keyNode.put("kty", rsaKey.getKeyType().toString());
        keyNode.put("use", rsaKey.getKeyUse().toString());
        keyNode.put("kid", rsaKey.getKeyID());
        keyNode.put("n", rsaKey.getModulus().toString());
        keyNode.put("e", rsaKey.getPublicExponent().toString());

        keys.add(keyNode);
        jwkSetObjectNode.set("keys", keys);

        return mapper.writeValueAsString(jwkSetObjectNode);
    }

    /**
     * 生成jwt
     */
    private void generateJwk() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048, new SecureRandom());
            // 生成密钥对
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            // 创建公钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            //创建 RSAKey 对象
            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .keyID(GuidUtils.createShortRandomGuid()) // 设置 kid
                    .keyUse(KeyUse.SIGNATURE) // 设置 use 为签名用途
                    .build();

            // 创建 JWK Set
            // JWKSet jwkSet = new JWKSet(rsaKey);
            this.privateKey = privateKey;
            String json = this.jwkJson(rsaKey);
            this.jwkJson = json;
            log.info("Generated JWK: {}", json);

        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating JWK: {} ", e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 构建jws信息
     * @return
     */
    public String getJwk() {
        return this.jwkJson;
    }

    /**
     * 生成jwt 内容
     * @param data
     * @return
     */
    private String generateJwt(Map<String, Object> data, String agentName) throws JsonProcessingException {
        String requestBodySha256 = calculateRequestBodySha256(data);
        // 过期时间
        long iat = System.currentTimeMillis() / 1000L;
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim(CLAIM_BODY, requestBodySha256)
                .claim(CLAIM_TIME, iat)
                .claim(CLAIM_AGENT_NAME, agentName)
                .build();

        // 签名jwt 对象
        SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), claimsSet);
        try {
            // 私钥签名
            signedJWT.sign(new RSASSASigner(privateKey));
        } catch (JOSEException e) {
            log.error("Error signing JWT", e);
            return null;
        }
        return signedJWT.serialize();
    }

    /**
     * 推送消息
     * @param url
     * @param data
     * @return
     */
    public CompletableFuture<Void> sendPushNotification(String url, Map<String, Object> data, String agentName) {
        return CompletableFuture.runAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                String jwtToken = generateJwt(data, agentName);
                HttpPost request = new HttpPost(url);
                request.setHeader(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + jwtToken);
                StringEntity requestEntity = new StringEntity(new ObjectMapper().writeValueAsString(data), StandardCharsets.UTF_8);
                requestEntity.setContentType("application/json;charset=UTF-8");
                request.setEntity(requestEntity);
                // log.info("发送的内容: {}", data);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    // 消息发送状态
                    if (response.getStatusLine().getStatusCode() >= 400) {
                        log.info("Error sending push-notification for URL {}: {}, statusCode: {} ", url, response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
                    } else {
                        // 完成正常推送
                        log.info("Push-notification sent for URL: {}", url);
                    }
                }
            } catch (Exception e) {
                log.error("sendPushNotification Error during sending push-notification for URL {}: {}", url, e.getMessage());
            }
        });
    }
}
