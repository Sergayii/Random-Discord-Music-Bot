package com.honker.main;

import static com.honker.main.Main.bot;
import static com.honker.main.Main.musicManager;
import static com.honker.main.Main.progress;
import static com.honker.main.Main.users;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public abstract class Operations {
    
    public static String[] marks = new String[]{"!", "#", "$", "%", "^", "&", "*", "(", ")", "+", "=", "-", "<", ">", ",", ".", ":", ";", "@", "?", "\n"};
    
    public static void sendFile(IChannel chan, String message, File file){
        try{
            chan.sendFile(message, file);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void sendFile(IChannel chan,  File file){
        try{
            chan.sendFile(file);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void sendFile(IChannel chan, String message, String path){
        try{
            chan.sendFile(message, false, Operations.class.getResourceAsStream(path), "Image.png");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void sendFile(IChannel chan, String path){
        try{
            chan.sendFile("", false, Operations.class.getResourceAsStream(path), "Image.png");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void sendMessage(IChannel chan, String msg){
        try{
            chan.sendMessage(msg);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void sendCommandDone(IChannel chan){
        sendMessage(chan, "Command done!");
    }
    
    public static void sendCommandInProgress(IChannel chan){
        sendMessage(chan, "Command in progress");
    }
    
    public static void sendCommandFailed(IChannel chan, String cause){
        sendMessage(chan, "Command failed: " + cause);
    }
    
    public static void sendTracksList(IChannel chan){
        try {
            File file = File.createTempFile("musicList", ".txt");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            
            for(AudioTrack track : musicManager.scheduler.queue){
                writer.write(musicManager.scheduler.getShortenedTrackInfo(track));
                writer.newLine();
                writer.flush();
            }
            
            sendFile(chan, "There's just too much tracks, i'll send the list in this file", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void sendHelp(IChannel chan){
        sendMessage(chan,
                    "What means what:\n```\n"
                    + "<arg> - what you must enter\n"
                    + "[arg] - what is optional to enter\n\n"
                    + "```\n\n"
                    + "Main commands:\n```\n"
                    + "!help - shows help\n"
                    + "!track - sends current track name and ID\n"
                    + "!list - sends tracks list\n"
                    + "!search <sentence> - search for <sentence> in tracks list\n"
                    + "!ping - sends \"pong\" if the bot is still alive\n"
                    + "```\n\n"
                    + "Admin commands:\n```\n"
                    + "!bot <command> [value] - used for controlling bot remotely\n"
                    + "\trestart - restarts bot (but no changes to code get applied)\n"
                    + "\tshutdown - shuts bot down\n"
                    + "\tavatar - changes bot's avatar\n"
                    + "\t\tNote: for this comand to work, you need to attach an image\n"
                    + "\tnick <nick> - changes bot's nickname\n"
                    + "!music <command> [value] - used for controlling music\n"
                    + "\tpause - pauses music\n"
                    + "\tresume - resumes music\n"
                    + "\tplay - plays a random track from the playlist\n"
                    + "\tnext - plays next track from the playlist\n"
                    + "\tprevious - plays previous track from the playlist\n"
                    + "\trejoin - rejoins to music voice channel\n"
                    + "\treplay - replays current track\n"
                    + "\tshuffle - shuffles playlist\n"
                    + "\tsort - sorts playlist\n"
                    + "\tplay <id> - plays a track from playlist\n"
                    + "\twind <time> - winds/rewinds current track\n"
                    + "\tvolume <vol> - changes music volume\n"
                    + "\tloop <true/false> - changes looping a single track\n"
                    + "!files <command> [value] - used for controlling files\n"
                    + "\tload - loads any new files\n"
                    + "\tunload - unloads all the files\n"
                    + "\treload - reloads all the files\n"
                    + "```");
    }
    
    public static void sendProgress(IChannel chan) {
        IMessage msg;
        try{
            msg = chan.sendMessage("Track progress:");
            progress = msg;
        } catch(Exception e){
            e.printStackTrace();
            progress = null;
            return;
        }
        
        musicManager.scheduler.updateTrack();
    }
    
    public static UserVar findUser(IUser user){
        for(UserVar userVar : users){
            if(userVar.user.equals(user))
                return userVar;
        }
        return null;
    }
    
    public static void updateUsers(){
        List<IUser> usersList = bot.client.getUsers();
        
        int a = 0;
        for(IUser user : usersList){
            if(!user.equals(bot.user))
                users[a] = new UserVar(user);
            a += 1;
        }
    }
}
