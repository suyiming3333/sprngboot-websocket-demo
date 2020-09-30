package com.corn.sprngboot.websocket.demo;

import com.corn.sprngboot.websocket.demo.netty.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class SprngbootWebsocketDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SprngbootWebsocketDemoApplication.class, args);

        try {
            new NettyServer(12345).run();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

}
