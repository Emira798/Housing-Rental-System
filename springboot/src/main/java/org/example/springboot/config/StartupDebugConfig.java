package org.example.springboot.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StartupDebugConfig {
    private static final Logger log = LoggerFactory.getLogger(StartupDebugConfig.class);

    @Value("${spring.datasource.url:#{null}}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String datasourceUsername;

    @Value("${server.port:#{null}}")
    private String serverPort;

    @PostConstruct
    public void printConfig() {
        log.info("========================================");
        log.info(">>> 服务器端口: {}", serverPort);
        log.info(">>> 数据库URL: {}", datasourceUrl);
        log.info(">>> 数据库用户: {}", datasourceUsername);
        log.info("========================================");
    }
}
