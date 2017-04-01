package com.honker.main;

import com.honker.listeners.MessageListener;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

import static com.honker.main.Main.main;

public class Bot {

    private IDiscordClient client;
    private IUser user;

    public void setUser(IUser user) {
        this.user = user;
    }
    
    public IUser getUser() {
        return user;
    }
    
    public void setClient(IDiscordClient client) {
        this.client = client;
    }
    
    public IDiscordClient getClient() {
        return client;
    }
    
    public void sendFile(String message, File file) {
        try {
            main.getMainChannel().sendFile(message, file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File file) {
        try {
            main.getMainChannel().sendFile(file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String message, String path) {
        try {
            main.getMainChannel().sendFile(message, false, getClass().getResourceAsStream(path), "Image.png");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String path) {
        try {
            main.getMainChannel().sendFile("", false, getClass().getResourceAsStream(path), "Image.png");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            main.getMainChannel().sendMessage(msg);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommandDone() {
        sendMessage("Command done!");
    }

    public void sendCommandInProgress() {
        sendMessage("Command in progress");
    }

    public void sendCommandFailed(String cause) {
        sendMessage("Command failed: " + cause);
    }

    public void sendTracksList() {
        try {
            if(main.getMusicManager().getScheduler().queue.isEmpty()) {
                sendMessage("The playlist is empty");
                return;
            }

            String list = "";
            for(AudioTrack track : main.getMusicManager().getScheduler().queue) {
                list += main.getMusicManager().getScheduler().getShortenedTrackInfo(track) + System.lineSeparator();
            }
            
            if(list.length() <= 1000) {
                sendMessage("Tracks list:\n" + list);
                return;
            }
            
            File file = File.createTempFile("musicList", ".txt");
            PrintWriter writer = new PrintWriter(file);

            writer.print(list);
            writer.flush();

            sendFile("There's just too much tracks, i'll send the playlist in this file", file);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendHelp() {
        sendMessage(
                      "Main commands:\n```\n"
                    + "!help - shows help\n"
                    + "!track - sends current track name and ID\n"
                    + "!playlist - sends the playlist\n"
                    + "!search <sentence> - search in tracks list\n"
                    + "!ping - sends \"pong\" if the bot is still alive\n"
                    + "```\n\n"
                    + "Admin commands:\n```\n"
                    + "!bot <command> [value] - used for controlling bot remotely\n"
                    + "\trestart - restarts bot\n"
                    + "\tshutdown - shuts bot down\n"
                    + "\tavatar - changes bot's avatar\n"
                    + "\t\tNote: for this comand to work, you need to attach an image\n"
                    + "\tnick <nick> - changes bot's nickname\n"
                    + "!music <command> [value] - used for controlling music\n"
                    + "\tpause - pauses music\n"
                    + "\tresume - resumes music\n"
                    + "\tstop - stops the music\n"
                    + "\tplay - plays a random track from the playlist\n"
                    + "\tnext - plays next track from the playlist\n"
                    + "\tprevious - plays previous track from the playlist\n"
                    + "\tleave - leaves the music voice channel\n"
                    + "\tjoin - joins the music voice channel\n"
                    + "\trejoin - rejoins to music voice channel\n"
                    + "\treplay - replays current track\n"
                    + "\tshuffle - shuffles playlist\n"
                    + "\tsort - sorts playlist\n"
                    + "\tclear - clears playlist\n"
                    + "\tplay <id> - plays a track from playlist\n"
                    + "\twind <time> - winds/rewinds current track\n"
                    + "\tvolume <vol> - changes music volume\n"
                    + "\tloop <true/false> - changes looping a single track\n"
                    + "\tqueue <track> - add a track to the playlist\n"
                    + "!files <command> [value] - used for controlling files\n"
                    + "\tunload - unloads all the files\n"
                    + "\treload - reloads all the files\n"
                    + "```"
        );
    }

    public void sendProgress() {
        if(main.getProgress() != null) {
            try {
                main.getProgress().delete();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        IMessage msg;
        try {
            msg = main.getMainChannel().sendMessage(main.getMusicManager().getScheduler().getTrackInfo(
                    main.getMusicManager().getScheduler().getCurrentTrack()) + "\n\nTrack progress:");
            main.setProgress(msg);
            main.getMusicManager().getScheduler().updateStatus();
        } catch(Exception e) {
            e.printStackTrace();
            main.setProgress(null);
            return;
        }

        main.getMusicManager().getScheduler().updateTrack();
    }
    
    public Bot() throws FileNotFoundException {
        ClientBuilder builder = new ClientBuilder();
        builder.withToken(main.getBotToken());
        try {
            setClient(builder.login());
        } catch(DiscordException ex) {
            return;
        }
        setUser(client.getOurUser());

        client.getDispatcher().registerListener(new Main());
        client.getDispatcher().registerListener(new MessageListener());
    }
}
