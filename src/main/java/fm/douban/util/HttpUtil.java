package fm.douban.util;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class HttpUtil {
    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    // okHttpClient 实例
    // 连接 2 分钟超时，读取 4 分钟超时
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.MINUTES)
        .readTimeout(4, TimeUnit.MINUTES).build();

    @PostConstruct
    public void init() {
        logger.info("okHttpClient init successful");
    }

    /**
     * 默认的 http header。
     *
     * @return
     */
    public Map<String, String> buildHeaderData(String referer, String host, String cookie) {
        Map<String, String> headers = new HashMap<>();

        // 比较通用的
        headers.put("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");

        // 不同的爬取目标，有不同的值
        if (referer != null) {
            headers.put("Referer", referer);
        }
        if (host != null) {
            headers.put("Host", host);
        }
        if (cookie != null) {
            headers.put("Cookie", cookie);
        }

        return headers;
    }

    /**
     * 根据输入的url，读取页面内容并返回
     */
    public String getContent(String url, Map<String, String> headers) {
        // 定义一个request
        Builder reqBuilder = new Request.Builder().url(url);

        // 如果传入 http header ，则放入 Request 中
        if (headers != null && !headers.isEmpty()) {
            for (String key : headers.keySet()) {
                reqBuilder.addHeader(key, headers.get(key));
            }
        }

        Request request = reqBuilder.build();
        // 使用client去请求
        Call call = okHttpClient.newCall(request);
        // 返回结果字符串
        String result = null;
        try {
            // 获得返回结果
            logger.error("request " + url + " begin . ");
            result = call.execute().body().string();
        } catch (IOException e) {
            logger.error("request " + url + " exception . ", e);
        }
        return result;
    }
}
