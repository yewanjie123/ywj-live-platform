package imclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Netty客户端
 */
@SpringBootApplication
public class ImClientApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ImClientApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
}