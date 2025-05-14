package com.sean.seanBI.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/3/11
 **/
@Configuration
@ConfigurationProperties(prefix = "zhi-pu.client")
@Data
public class ZhiPiuConfig {
    private String apiKey;

    @Bean
    public ClientV4 ClientV4() {
        ClientV4 client = new ClientV4.Builder(apiKey)
                .enableTokenCache()//是否开启token缓存，开启后会缓存token，减少token请求次数
                .networkConfig(3000, 1000, 1000, 1000, TimeUnit.SECONDS)// 设置连接超时、读取超时、写入超时、ping间隔、ping超时时间
                .build();
        return client;
    }

}
