package com.jingdianjichi.tool;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;


import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: ChickenWing
 * @Description: httpUtils
 * @DateTime: 2022/12/10 22:35
 */
@Slf4j
public class HttpUtils {

    static CloseableHttpClient httpClient;

    static {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(500);
        connectionManager.setDefaultMaxPerRoute(500);
        connectionManager.setDefaultSocketConfig(
                SocketConfig.custom().setSoTimeout(15, TimeUnit.SECONDS)
                        .setTcpNoDelay(true).build()
        );
        connectionManager.setValidateAfterInactivity(TimeValue.ofSeconds(15));

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(1))
                .setConnectionRequestTimeout(Timeout.ofSeconds(1))
                .setResponseTimeout(Timeout.ofSeconds(1))
                .build();

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()
                .build();
    }

    public static String post(String url, List<Pair<String, String>> pairList, Map<String, String> headerMap) throws Exception {
        String urlParam = buildUrlParam(pairList);
        url = url + "?" + urlParam;
        final HttpPost httpPost = new HttpPost(url);
        if (Objects.nonNull(headerMap) && headerMap.size() > 0) {
            headerMap.forEach((key, value) -> httpPost.addHeader(key, value));
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } finally {
            if (null != response) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

    private static String buildUrlParam(List<Pair<String, String>> pairList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Pair<String, String> dataPair : pairList) {
            stringBuilder.append(dataPair.getKey()).append("=").append(dataPair.getValue()).append("&");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

}
