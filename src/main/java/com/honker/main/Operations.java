package com.honker.main;

import static com.honker.main.Main.main;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import sx.blah.discord.handle.obj.IMessage;

public abstract class Operations {

    public static String[] marks = new String[] {"!", "#", "$", "%", "^", "&", "*", "(", ")", "+", "=", "-", "<", ">", ",", ".", ":", ";", "@", "?", "\n"};

    public static void sendFile(String message, File file) {
        try {
            main.mainChannel.sendFile(message, file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendFile(File file) {
        try {
            main.mainChannel.sendFile(file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendFile(String message, String path) {
        try {
            main.mainChannel.sendFile(message, false, Operations.class.getResourceAsStream(path), "Image.png");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendFile(String path) {
        try {
            main.mainChannel.sendFile("", false, Operations.class.getResourceAsStream(path), "Image.png");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String msg) {
        try {
            main.mainChannel.sendMessage(msg);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendCommandDone() {
        sendMessage("Command done!");
    }

    public static void sendCommandInProgress() {
        sendMessage("Command in progress");
    }

    public static void sendCommandFailed(String cause) {
        sendMessage("Command failed: " + cause);
    }

    public static void sendTracksList() {
        try {
            if(main.musicManager.scheduler.queue.isEmpty()) {
                sendMessage("The playlist is empty");
                return;
            }

            String list = "";
            for(AudioTrack track : main.musicManager.scheduler.queue) {
                list += main.musicManager.scheduler.getShortenedTrackInfo(track) + System.lineSeparator();
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

    public static void sendHelp() {
        sendMessage(
                      "Main commands:\n```\n"
                    + "!help - shows help\n"
                    + "!track - sends current track name and ID\n"
                    + "!playlist - sends the playlist\n"
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
                    + "\tqueue <track> - add a track to the playlist"
                    + "!files <command> [value] - used for controlling files\n"
                    + "\tunload - unloads all the files\n"
                    + "\treload - reloads all the files\n"
                    + "```"
        );
    }

    public static void sendProgress() {
        if(main.progress != null) {
            try {
                main.progress.delete();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        IMessage msg;
        try {
            msg = main.mainChannel.sendMessage(main.musicManager.scheduler.getTrackInfo(main.musicManager.scheduler.getCurrentTrack())
                                   + "\n\nTrack progress:");
            main.progress = msg;
        } catch(Exception e) {
            e.printStackTrace();
            main.progress = null;
            return;
        }

        main.musicManager.scheduler.updateTrack();
    }
}
