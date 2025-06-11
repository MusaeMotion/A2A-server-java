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

package com.musaemotion.a2a.agent.client.notification;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.musaemotion.a2a.common.notification.PushNotificationAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.server.notification
 * @project：A2A
 * @date：2025/4/22 17:03
 * @description：请完善描述
 */
@Slf4j
public class PushNotificationReceiverAuth extends PushNotificationAuth {


    /**
     * 已经注册的智能体终结点
     */
    private Map<String, String> agentJwkEndpoint = Maps.newHashMap();


    /**
     *  添加终结点
     * @param agentName
     * @param jwkEndpoint
     */
    public void registerAgentJwkEndpoint(String agentName, String jwkEndpoint) {
        this.agentJwkEndpoint.put(agentName, jwkEndpoint);
    }

    /**
     * @param signedJWT
     * @return
     * @throws ParseException
     * @throws MalformedURLException
     * @throws KeySourceException
     */
    private List<JWK> getCurJwk(SignedJWT signedJWT) throws ParseException, MalformedURLException, KeySourceException {
        JWKSelector selector = new JWKSelector(JWKMatcher.forJWSHeader(signedJWT.getHeader()));
        Object claimAgentName = signedJWT.getJWTClaimsSet().getClaim(CLAIM_AGENT_NAME);
        if (claimAgentName == null) {
            for (Map.Entry<String, String> entry : this.agentJwkEndpoint.entrySet()) {
                String value = entry.getValue();
                JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(value));
                List<JWK> matchingJwk = jwkSource.get(selector, null);
                if (matchingJwk.size() > 0) {
                    return matchingJwk;
                }
            }
            return Lists.newArrayList();
        } else {
            String agentName = (String) claimAgentName;
            if (!this.agentJwkEndpoint.containsKey(agentName)) {
                throw new RuntimeException("该智能体还未注册到通知服务");
            }
            // 加载jwkSource, 后续调整成缓存模式
            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(this.agentJwkEndpoint.get(agentName)));
            // 获取到对应的 jwk 信息
            List<JWK> matchingJwk = jwkSource.get(selector, null);
            return matchingJwk;
        }
    }

    /**
     * 验证通知请求
     * @param rawAuthHeader 原始请求头
     * @param rawRequestBody 原始请求body
     * @return
     * @throws Exception
     */
    public boolean verifyPushNotification(String rawAuthHeader, String rawRequestBody) throws Exception {

        if(!StringUtils.hasText(rawAuthHeader) || !rawAuthHeader.startsWith(AUTH_HEADER_PREFIX)) {
            return Boolean.FALSE;
        }

        // 获取 token
        String jwtToken = rawAuthHeader.substring(AUTH_HEADER_PREFIX.length()).trim();

        // 把 token 转换成 签名jwt 对象
        SignedJWT signedJWT = SignedJWT.parse(jwtToken);

        // 获取到对应的 jwk 信息
        List<JWK> matchingJwk = this.getCurJwk(signedJWT);

        // 为找到，抛出异常
        if (matchingJwk.isEmpty()) {
            throw new RuntimeException("No matching JWK found");
        }

        // 找到，判断是不是 RSK Key
        JWK selectedJwk = matchingJwk.get(0);
        if (!(selectedJwk instanceof RSAKey)) {
            throw new RuntimeException("Selected JWK is not an RSA key");
        }

        // 把找到的Jwk 信息强制转换成 RSAKey
        RSAKey rsaKey = (RSAKey) selectedJwk;

        // 验证 从 jwkSource 获取到的 jwk 信息 与 jwtToken 构建的signedJWT, 进行签名验证。
        Boolean verify = signedJWT.verify(new RSASSAVerifier(rsaKey));

        if(!verify) {
            // 签名验证不通过
            log.warn("Push notification not verified");
            return Boolean.FALSE;
        }
        // 判断token是否过期
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        // 验证请求体的 SHA256 哈希值
        String requestBodySha256 = calculateRequestBodySha256(rawRequestBody);
        String tokenRequestBodySha256 = claimsSet.getStringClaim(CLAIM_BODY);
        // 验证 body 是否通过
        if (!requestBodySha256.equals(tokenRequestBodySha256)) {
            throw new IllegalArgumentException("Invalid request body");
        }

        Date iat = claimsSet.getDateClaim(CLAIM_TIME);
        Date currentTime =  new Date();
        long differenceInMillis = Math.abs(currentTime.getTime() - iat.getTime());
        long fiveMinutesInMillis = 5 * 60 * 1000; // 5 minutes in milliseconds

        if (differenceInMillis > fiveMinutesInMillis) {
            throw new IllegalArgumentException("Token is expired");
        }

        return Boolean.TRUE;
    }
}
