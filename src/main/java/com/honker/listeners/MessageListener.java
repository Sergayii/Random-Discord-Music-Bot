package com.honker.listeners;

import static com.honker.main.Main.COMMAND_SYMBOL;
import static com.honker.main.Main.MAIN_CHANNEL_ID;
import static com.honker.main.Main.bot;
import static com.honker.main.Main.loadMusic;
import static com.honker.main.Main.music;
import static com.honker.main.Main.musicManager;
import static com.honker.main.Main.musicPaused;
import static com.honker.main.Main.play;
import static com.honker.main.Main.ready;
import static com.honker.main.Main.reloadMusic;
import static com.honker.main.Main.restart;
import static com.honker.main.Main.shutdown;
import static com.honker.main.Main.unloadMusic;
import static com.honker.main.Operations.sendCommandDone;
import static com.honker.main.Operations.sendCommandFailed;
import static com.honker.main.Operations.sendCommandInProgress;
import static com.honker.main.Operations.sendFile;
import static com.honker.main.Operations.sendHelp;
import static com.honker.main.Operations.sendMessage;
import static com.honker.main.Operations.sendTracksList;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.Image;

public class MessageListener implements IListener<MessageReceivedEvent> {
    
    public void command(IChannel chan, IUser user, String msg, String userName, List<Attachment> attachments) throws FileNotFoundException{
        if(msg.length() > COMMAND_SYMBOL.length()){
            msg = msg.substring(COMMAND_SYMBOL.length());
            while(msg.contains("  "))
                msg = msg.replace("  ", " ");
            msg = msg.trim();
            String msgL = msg.toLowerCase();
            String[] cmd = msgL.split(" ");
            
            if(cmd[0].equals("help")){
                if(cmd.length == 1)
                    sendHelp(chan);
            } else if(cmd[0].equals("track")){
                if(cmd.length == 1){
                    if(!ready)
                        sendCommandFailed(chan, "The music isn't even started yet!!!");
                    else
                        musicManager.scheduler.sendCurrentPlayingTrack(chan);
                }
            } else if(cmd[0].equals("list")){
                if(cmd.length == 1){
                    if(!ready)
                        sendCommandFailed(chan, "The music isn't even started yet!!!");
                    else
                        sendTracksList(chan);
                }
            } else if(cmd[0].equals("search")){
                if(cmd.length > 1){
                    if(ready){
                        cmd = msgL.split(" ", 2);

                        ArrayList<File> results = new ArrayList<File>();
                        String search = cmd[1];
                        for(File file : music){
                            AudioTrack track = musicManager.scheduler.queue.get(music.indexOf(file));
                            String trackName = musicManager.scheduler.getTrackName(track).toLowerCase();
                            if(trackName.contains(search))
                                results.add(file);
                        }

                        ArrayList<AudioTrack> tracks = new ArrayList<AudioTrack>();
                        for(File file : results)
                            tracks.add(musicManager.scheduler.queue.get(music.indexOf(file)));

                        String answer = new String();
                        for(AudioTrack track : tracks){
                            answer += musicManager.scheduler.getTrackInfo(track) + System.lineSeparator() + System.lineSeparator();
                        }

                        if(!answer.equals(""))
                            if(answer.length() <= 1900)
                                sendMessage(chan, "This is what i've found:\n" + answer);
                            else{
                                File file;
                                try {
                                    file = File.createTempFile("searchResults", ".txt");
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                    file = null;
                                }
                                
                                if(file != null){
                                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

                                    try{
                                        writer.write(answer.replace("```", ""));
                                        writer.newLine();
                                        writer.flush();
                                    } catch(IOException e){
                                        file = null;
                                    }

                                    if(file != null)
                                        sendFile(chan, "There's just too much tracks, i'll send them in a file", file);
                                    else
                                        sendMessage(chan, "Something broke, and i can't send the result");
                                }
                            }
                        else
                            sendMessage(chan, "I found nothing!");
                    } else
                        sendCommandFailed(chan, "The music isn't even started yet!!!");
                }
            } else{
                if(cmd[0].equals("bot")){
                    if(cmd[1].equals("restart")){
                        if(cmd.length == 2) {
                            sendCommandInProgress(chan);
                            restart();
                            sendCommandDone(chan);
                        }
                    } else if(cmd[1].equals("shutdown")){
                        if(cmd.length == 2) {
                            if(!ready)
                                sendCommandFailed(chan, "I'm not done launching, wait!");
                            else
                                shutdown();
                        }
                    } else if(cmd[1].equals("avatar")) {
                        if(cmd.length == 2 && attachments.size() == 1) {
                            cmd = msg.split(" ", 3);
                            try {
                                bot.client.changeAvatar(Image.forUrl("png", attachments.get(0).getUrl()));
                                sendCommandDone(chan);
                            } catch(Exception e) {
                                e.printStackTrace();
                                sendCommandFailed(chan, "Something went wrong, sorry!");
                            }
                        }
                    } else if(cmd[1].equals("nick")) {
                        if(cmd.length > 1) {
                            cmd = msg.split(" ", 3);
                            try {
                                bot.client.changeUsername(cmd[2]);
                                sendCommandDone(chan);
                            } catch(Exception e) {
                                e.printStackTrace();
                                sendCommandFailed(chan, "Something went wrong, sorry!");
                            }
                        }
                    }
                } else if(cmd[0].equals("music")){
                    if(cmd[1].equals("resume")){
                        if(cmd.length == 2) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else if(ready && !musicPaused)
                                sendCommandFailed(chan, "The music isn't paused!");
                            else if(ready && musicPaused){
                                musicManager.scheduler.player.setPaused(false);
                                musicPaused = false;
                                sendCommandDone(chan);
                            }
                        }
                    } else if(cmd[1].equals("pause")){
                        if(cmd.length == 2) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else if(ready && musicPaused)
                                sendCommandFailed(chan, "The music is already paused!");
                            else{
                                musicManager.scheduler.player.setPaused(true);
                                musicPaused = true;
                                sendCommandDone(chan);
                            }
                        }
                    } else if(cmd[1].equals("play")){
                        if(cmd.length == 2) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else{
                                musicManager.scheduler.playRandomTrack();
                                sendCommandDone(chan);
                            }
                        } else if(cmd.length == 3) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else{
                                try{
                                    int index = Integer.parseInt(cmd[2]);
                                    if(index >= musicManager.scheduler.queue.size())
                                        sendCommandFailed(chan, "ID is too big");
                                    else if(index < 0)
                                        sendCommandFailed(chan, "ID is too small");
                                    else{
                                        boolean trackPlayed = play(musicManager.scheduler.queue.get(index));
                                        if(!trackPlayed)
                                            sendCommandFailed(chan, "Track with this ID doesn't exist");
                                        else
                                            sendCommandDone(chan);
                                    }
                                } catch(NumberFormatException ex){
                                    sendCommandFailed(chan, "ID isn't an integer");
                                } catch(Exception ex){
                                    sendCommandFailed(chan, "Unexpected error");
                                    ex.printStackTrace();
                                }
                            }
                        }
                    } else if(cmd[1].equals("rejoin")){
                        if(cmd.length == 2) {
                            musicManager.scheduler.rejoinMusicChannel();
                            sendCommandDone(chan);
                        }
                    } else if(cmd[1].equals("replay")){
                        if(cmd.length == 2) {
                            if(musicManager.scheduler.getCurrentTrack() != null){
                                play(musicManager.scheduler.getCurrentTrack());
                                sendCommandDone(chan);
                            } else
                                sendCommandFailed(chan, "No track is playing right now...");
                        }
                    } else if(cmd[1].equals("next")){
                        if(cmd.length == 2) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else{
                                musicManager.scheduler.nextTrack();
                                sendCommandDone(chan);
                            }
                        }
                    } else if(cmd[1].equals("previous")){
                        if(cmd.length == 2) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else{
                                musicManager.scheduler.previousTrack();
                                sendCommandDone(chan);
                            }
                        }
                    } else if(cmd[1].equals("shuffle")){
                        if(cmd.length == 2) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else{
                                musicManager.scheduler.shufflePlaylist();
                                sendCommandDone(chan);
                            }
                        }
                    } else if(cmd[1].equals("sort")){
                        if(cmd.length == 2) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else{
                                musicManager.scheduler.sortPlaylist();
                                sendCommandDone(chan);
                            }
                        }
                    } else if(cmd[1].equals("loop")){
                        if(cmd.length == 3) {
                            if(cmd[2].equals("true")){
                                musicManager.scheduler.looping = true;
                                sendCommandDone(chan);
                            } else if(cmd[2].equals("false")){
                                musicManager.scheduler.looping = false;
                                sendCommandDone(chan);
                            } else
                                sendCommandFailed(chan, "Argument isn't \"true\" nor \"false\"");
                        }
                    } else if(cmd[1].equals("wind")){
                        if(cmd.length == 3) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else if(ready && musicPaused)
                                sendCommandFailed(chan, "The music is paused!");
                            else{
                                try{
                                    int time = Integer.parseInt(cmd[2]);
                                    int duration = (int)(musicManager.scheduler.getCurrentTrack().getDuration() / 1000);
                                    if(time > duration)
                                        sendCommandFailed(chan, "\"time\" is too big");
                                    else if(time < 0)
                                        sendCommandFailed(chan, "\"time\" is too small");
                                    else{
                                        musicManager.scheduler.setTrackTime(time);
                                        sendCommandDone(chan);
                                    }
                                } catch(NumberFormatException ex){
                                    sendCommandFailed(chan, "\"time\" isn't an integer");
                                } catch(Exception ex){
                                    sendCommandFailed(chan, "Unexpected error");
                                }
                            }
                        }
                    } else if(cmd[1].equals("volume")){
                        if(cmd.length == 3) {
                            if(!ready)
                                sendCommandFailed(chan, "The music isn't even started yet!");
                            else{
                                try{
                                    int volume = Integer.parseInt(cmd[2]);
                                    if(volume > 100)
                                        sendCommandFailed(chan, "\"vol\" is too big");
                                    else if(volume < 0)
                                        sendCommandFailed(chan, "\"vol\" is too small");
                                    else{
                                        musicManager.scheduler.setVolume(volume);
                                        sendCommandDone(chan);
                                    }
                                } catch(NumberFormatException ex){
                                    sendCommandFailed(chan, "\"vol\" isn't an integer");
                                } catch(Exception ex){
                                    sendCommandFailed(chan, "Unexpected error");
                                }
                            }
                        }
                    }
                } else if(cmd[0].equals("files")) {
                    if(cmd[1].equals("reload")) {
                        if(cmd.length == 2)
                            reloadMusic();
                    } else if(cmd[1].equals("load")) {
                        if(cmd.length == 2)
                            loadMusic();
                    } else if(cmd[1].equals("unload")) {
                        if(cmd.length == 2)
                            unloadMusic();
                    }
                }
            }
        }
    }

    @Override
    public void handle(MessageReceivedEvent e){
        IChannel chan = e.getMessage().getChannel();
        IUser user = e.getMessage().getAuthor();
        String msg = e.getMessage().getContent();
        String userName = user.getName();
        
        if(msg.startsWith(COMMAND_SYMBOL) && !user.isBot()){
            try {
                if(chan.getID().equals(MAIN_CHANNEL_ID))
                    command(chan, user, msg, userName, e.getMessage().getAttachments());
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}
