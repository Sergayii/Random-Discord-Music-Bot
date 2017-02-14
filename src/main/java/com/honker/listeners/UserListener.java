package com.honker.listeners;

import com.honker.main.Operations;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.NickNameChangeEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;

public class UserListener extends Operations{
    
    @EventSubscriber
    public void onUserJoinEvent(UserJoinEvent e){
        updateUsers();
    }
    
    @EventSubscriber
    public void onUserLeaveEvent(UserLeaveEvent e){
        updateUsers();
    }
    
    @EventSubscriber
    public void onNickNameChangeEvent(NickNameChangeEvent e){
        updateUsers();
    }
}
