package com.ruoyi.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 网关启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class RuoYiGatewayApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiGatewayApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  网关模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
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
