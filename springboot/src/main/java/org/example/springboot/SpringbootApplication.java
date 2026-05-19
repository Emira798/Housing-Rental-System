package org.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringbootApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(SpringbootApplication.class, args);
        } catch (Exception e) {
            System.err.println("===== 应用启动失败 =====");
            e.printStackTrace(System.err);
            System.err.println("===== 错误消息: " + e.getMessage() + " =====");
            // 保持容器存活15秒以便查看日志
            try { Thread.sleep(15000); } catch (InterruptedException ignored) {}
            System.exit(1);
        }
    }

}
