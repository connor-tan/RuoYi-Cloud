package com.ruoyi.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 认证授权中心
 * 
 * @author ruoyi
 */
@EnableRyFeignClients
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class RuoYiAuthApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiAuthApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  认证授权中心启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "   _____ _                                                             _ \n" +
                " |  ___(_)_ __   __ _  ___ _ __ ___    ___ _ __ ___  ___ ___  ___  __| |\n" +
                " | |_  | | '_ \\ / _` |/ _ | '__/ __|  / __| '__/ _ \\/ __/ __|/ _ \\/ _` |\n" +
                " |  _| | | | | | (_| |  __| |  \\__ \\ | (__| | | (_) \\__ \\__ |  __| (_| |\n" +
                " |_|   |_|_| |_|\\__, |\\___|_|  |___/  \\___|_|  \\___/|___|___/\\___|\\__,_|\n" +
                "                |___/                 _                                 \n" +
                "                        _ __   ___   | |__  _   _  __ _ ___             \n" +
                "                       | '_ \\ / _ \\  | '_ \\| | | |/ _` / __|            \n" +
                "                       | | | | (_) | | |_) | |_| | (_| \\__ \\            \n" +
                "                       |_| |_|\\___/  |_.__/ \\__,_|\\__, |___/            \n" +
                "                                                  |___/                 ");
    }
}
