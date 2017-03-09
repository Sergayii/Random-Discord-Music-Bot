package com.honker.main;

import com.honker.listeners.MessageListener;
import static com.honker.main.Main.BOT_TOKEN;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

public class Bot {
    
    public IDiscordClient client;
    public IUser user;
    
    public Bot() {
        ClientBuilder builder = new ClientBuilder();
        builder.withToken(BOT_TOKEN);
        try {
            client = builder.login();
        } catch (DiscordException ex) {
            return;
        }
        user = client.getOurUser();
        
        client.getDispatcher().registerListener(new Main());
        client.getDispatcher().registerListener(new MessageListener());
    }
}
