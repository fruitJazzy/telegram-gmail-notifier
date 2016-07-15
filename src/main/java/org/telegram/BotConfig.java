package org.telegram;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * @brief Bots configurations
 * @date 20 of June of 2015
 */
public class BotConfig {

    public static final String WEBHOOK_TOKEN = "<token>";
    public static final String WEBHOOK_USER = "webhooksamplebot";

    // usernameproject is name bot in telegram
    public static  String USERNAMEMYPROJECT;
    public static  String BOTTOKEN;

    public static  String HOST = "imap.gmail.com";
    public static  int PORT = 993;
    public static  String MAIL;
    public static  String PASSWORD;
    public static  String UPDATE_TIME;

    static {
        try {

            Properties prop = new Properties();
            InputStream in = BotConfig.class.getClassLoader().getResourceAsStream("botconfig.properties");

            if (in != null) {
                prop.load(in);

                USERNAMEMYPROJECT = prop.getProperty("usernameproject");
                BOTTOKEN = prop.getProperty("bottoken");
                UPDATE_TIME = prop.getProperty("update_time");
                MAIL = prop.getProperty("mail");
                PASSWORD = prop.getProperty("password");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
