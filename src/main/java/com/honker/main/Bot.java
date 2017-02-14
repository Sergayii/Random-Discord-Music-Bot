package com.honker.main;

import com.honker.listeners.MessageListener;
import com.honker.listeners.UserListener;
import static com.honker.main.Main.BOT_TOKEN;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

public class Bot {
    
    public IDiscordClient client;
    public IUser user;
    
    public Bot() throws DiscordException{
        ClientBuilder builder = new ClientBuilder();
        builder.withToken(BOT_TOKEN);
        client = builder.login();
        user = client.getOurUser();
        
        client.getDispatcher().registerListener(new Main());
        client.getDispatcher().registerListener(new MessageListener());
        client.getDispatcher().registerListener(new UserListener());
    }
}
