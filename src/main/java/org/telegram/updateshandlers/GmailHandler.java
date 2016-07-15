package org.telegram.updateshandlers;

import com.sun.mail.imap.IMAPFolder;
import org.telegram.BotConfig;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.io.*;
import java.util.Properties;

/**
 * Created by jazzt on 06.07.16.
 */
public class GmailHandler extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        SendMessage sendMessageRequest = new SendMessage();


        Properties props = System.getProperties();
        props.setProperty("mail.imap.ssl.enable", "true");

        // Get a Session object
        Session session = Session.getInstance(props, null);
        // session.setDebug(true);

        // Get a Store object
        Store store = null;
        try {
            store = session.getStore("imap");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        // Connect
        try {
            assert store != null;
            store.connect(BotConfig.HOST, BotConfig.PORT, BotConfig.MAIL, BotConfig.PASSWORD);

            // Open a Folder
            Folder folder = store.getFolder("Inbox");
            if (folder == null || !folder.exists()) {
                System.out.println("Invalid folder");
                System.exit(1);
            }

            folder.open(Folder.READ_WRITE);
            //who should get the message? the sender from which we got the message...
            //sendMessageRequest.setText("you said: " + message.getText());

            // Add messageCountListener to listen for new messages
            folder.addMessageCountListener(new MessageCountAdapter() {
                public void messagesAdded(MessageCountEvent ev) {
                    javax.mail.Message[] msgs = ev.getMessages();
                    System.out.println("Got " + msgs.length + " new messages");

                    // Just dump out the new messages
                    for (int i = 0; i < msgs.length; i++) {
                        try {
                            System.out.println("-----");
                            System.out.println("Message " +
                                    msgs[i].getMessageNumber() + ":");

                            sendMessageRequest.setChatId(message.getChatId().toString());
                            Object ob = msgs[i].getContent();
                            Multipart mp = (Multipart) ob;
                            String sMess = "";

                            for (int j = 0; j < ((Multipart) ob).getCount(); j++) {
                                BodyPart bodyPart = ((Multipart) ob).getBodyPart(j);
                                if (bodyPart.isMimeType("text/*")) {
                                    sMess = (String) bodyPart.getContent();
                                }
                            }

                            sendMessageRequest.setText(
                                    msgs[i].getSubject()
                                            + "\n"
                                            + parseMessageFromMail(sMess)
                            );


                            sendMessage(sendMessageRequest);
                        } catch (MessagingException | IOException | TelegramApiException mex) {
                            mex.printStackTrace();
                        }
                    }
                }
            });

            // Check mail once in "freq" MILLIseconds
            int freq = Integer.parseInt(BotConfig.UPDATE_TIME);
            boolean supportsIdle = false;
            try {
                if (folder instanceof IMAPFolder) {
                    IMAPFolder f = (IMAPFolder) folder;
                    f.idle();
                    supportsIdle = true;
                }
            } catch (FolderClosedException fex) {
                throw fex;
            } catch (MessagingException mex) {
                supportsIdle = false;
            }
            for (; ;) {
                if (supportsIdle && folder instanceof IMAPFolder) {
                    IMAPFolder f = (IMAPFolder) folder;
                    f.idle();
                    System.out.println("IDLE done");
                } else {
                    Thread.sleep(freq); // sleep for freq milliseconds

                    // This is to force the IMAP server to send us
                    // EXISTS notifications.
                    folder.getMessageCount();
                }
            }
        } catch (MessagingException | InterruptedException e) {
            e.printStackTrace();
        }
    }//end onUpdateReceived()


    @Override
    public String getBotUsername() {
        return BotConfig.USERNAMEMYPROJECT;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOTTOKEN;
    }


    private static String parseMessageFromMail(String message) throws IOException {
        return message.replaceAll("<|>|/|div|br|span|style|&gt;|font-size:12.8px|&#39;|&quot;|&lt;|=\"\"","");
    }

}
