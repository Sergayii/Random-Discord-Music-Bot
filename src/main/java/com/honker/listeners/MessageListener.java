package com.honker.listeners;

import static com.honker.main.Main.COMMAND_SYMBOL;
import static com.honker.main.Main.MAIN_CHANNEL_ID;
import static com.honker.main.Main.loadMusic;
import static com.honker.main.Main.music;
import static com.honker.main.Main.musicManager;
import static com.honker.main.Main.musicPaused;
import static com.honker.main.Main.play;
import static com.honker.main.Main.ready;
import static com.honker.main.Main.reloadMusic;
import static com.honker.main.Main.restart;
import static com.honker.main.Main.root;
import static com.honker.main.Main.shutdown;
import static com.honker.main.Main.unloadMusic;
import com.honker.main.Operations;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class MessageListener extends Operations implements IListener<MessageReceivedEvent>{
    
    public void command(IChannel chan, IUser user, String msg, String userName) throws FileNotFoundException{
        if(msg.length() > 1){
            msg = msg.substring(1).toLowerCase();
            for(String str = " "; str.length() < 1000; str += " ")
                msg = msg.replace(str, " ");
            msg = msg.replace("  ", " ");
            msg = msg.trim();
            String[] cmd = msg.split(" ");
            
            if(cmd[0].equals("help")){
                if(cmd.length == 1)
                    sendHelp(chan);
                else if(cmd.length == 2 && cmd[1].equals("me"))
                    sendCommand(chan, "No");
                else
                    sendCommandFailedLength(chan);
//            } else if(cmd[0].equals("roll")){
//                if(cmd.length == 2){
//                    try{
//                        int d = Integer.parseInt(cmd[1]);
//                        if(d > 100)
//                            sendCommandFailed(chan, "This number is too big");
//                        else if(d < 2)
//                            sendCommandFailed(chan, "This number is too small");
//                        else
//                            rollDice(chan, d);
//                    } catch(Exception ex){
//                        sendCommandFailed(chan, "Is this even a number?");
//                        ex.printStackTrace();
//                    }
//                } else if(cmd.length == 1)
//                    rollDice(chan, 6);
//                else
//                    sendCommandFailedLength(chan);
//            } else if(cmd[0].equals("bugurt")){
//                if(cmd.length == 1){
//                    String bugurt = generateBugurt();
//                    if(bugurt.equals(""))
//                        sendCommandFailed(chan, "I don't know any words to write a bugurt with, send some messages so i can learn some!");
//                    else
//                        sendCommand(chan, "YOUR BUGURT:\n\n" + bugurt);
//                } else
//                    sendCommandFailedLength(chan);
            } else if(cmd[0].equals("track")){
                if(cmd.length == 1){
                    if(!ready)
                        sendCommandFailed(chan, "The music isn't even started yet!!!");
                    else
                        musicManager.scheduler.sendCurrentPlayingTrack(chan);
                } else
                    sendCommandFailedLength(chan);
            } else if(cmd[0].equals("list")){
                if(cmd.length == 1){
                    if(!ready)
                        sendCommandFailed(chan, "The music isn't even started yet!!!");
                    else
                        sendTracksList(chan);
                } else
                    sendCommandFailedLength(chan);
            } else if(cmd[0].equals("search")){
                if(cmd.length > 1){
                    if(ready){
                        cmd = msg.split(" ", 2);

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
                            if(answer.length() <= 2000)
                                sendMessage(chan, "This is what i've found:\n" + answer);
                            else{
                                File file = new File("./searchResults.txt");
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
                        else
                            sendMessage(chan, "I found nothing!");
                    } else
                        sendCommandFailed(chan, "The music isn't even started yet!!!");
                } else sendCommandFailedLength(chan);
            } else{
                //if(user.getPermissionsForGuild(bot.client.getGuildByID(GUILD_ID))){
                    if(cmd[0].equals("bot") && cmd.length == 2){
                        if(cmd[1].equals("restart")){
                            sendCommandInProgress(chan);
                            try {
                                restart();
                            } catch (InterruptedException ex) {
                                shutdown();
                            }
                            sendCommandDone(chan);
                        } else if(cmd[1].equals("shutdown")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else
                                root.dispose();
                        }
                    } else if(cmd[0].equals("music") && cmd.length == 2){
                        if(cmd[1].equals("resume")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else if(ready && !musicPaused)
                                sendCommandFailed(chan, "Main.musicPaused == false");
                            else if(ready && musicPaused){
                                musicManager.scheduler.player.setPaused(false);
                                musicPaused = false;
                                sendCommandDone(chan);
                            }
                        } else if(cmd[1].equals("stop")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else if(ready && musicPaused)
                                sendCommandFailed(chan, "Main.musicPaused == true");
                            else{
                                musicManager.scheduler.player.setPaused(true);
                                musicPaused = true;
                                sendCommandDone(chan);
                            }
                        } else if(cmd[1].equals("play")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                musicManager.scheduler.playRandomTrack();
                                sendCommandDone(chan);
                            }
                        } else if(cmd[1].equals("rejoin")){
                            musicManager.scheduler.rejoinMusicChannel();
                            sendCommandDone(chan);
                        } else if(cmd[1].equals("replay")){
                            if(musicManager.scheduler.getCurrentTrack() != null){
                                play(musicManager.scheduler.getCurrentTrack());
                                sendCommandDone(chan);
                            } else
                                sendCommandFailed(chan, "Main.musicManager.scheduler.currentTrack == null");
                        } else if(cmd[1].equals("next")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                musicManager.scheduler.nextTrack();
                                sendCommandDone(chan);
                            }
                        } else if(cmd[1].equals("previous")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                musicManager.scheduler.previousTrack();
                                sendCommandDone(chan);
                            }
                        } else if(cmd[1].equals("shuffle")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                musicManager.scheduler.shufflePlaylist();
                                sendCommandDone(chan);
                            }
                        } else if(cmd[1].equals("sort")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                musicManager.scheduler.sortPlaylist();
                                sendCommandDone(chan);
                            }
                        } else
                            sendCommandFailed(chan, "Unknown argument");
                    } else if(cmd[0].equals("music") && cmd.length == 3){
                        if(cmd[1].equals("play")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                try{
                                    int index = Integer.parseInt(cmd[2]);
                                    if(index >= musicManager.scheduler.queue.size())
                                        sendCommandFailed(chan, "cmd[2] value is too big");
                                    else if(index < 0)
                                        sendCommandFailed(chan, "cmd[2] value is too small");
                                    else{
                                        boolean trackPlayed = play(musicManager.scheduler.queue.get(index));
                                        if(!trackPlayed)
                                            sendCommandFailed(chan, "Track with this ID doesn't exist");
                                        else
                                            sendCommandDone(chan);
                                    }
                                } catch(NumberFormatException ex){
                                    sendCommandFailed(chan, "cmd[2] isn't an integer");
                                } catch(Exception ex){
                                    sendCommandFailed(chan, "Unexpected error");
                                    ex.printStackTrace();
                                }
                            }
                        } else if(cmd[1].equals("loop")){
                            if(cmd[2].equals("true")){
                                musicManager.scheduler.looping = true;
                                sendCommandDone(chan);
                            } else if(cmd[2].equals("false")){
                                musicManager.scheduler.looping = false;
                                sendCommandDone(chan);
                            } else
                                sendCommandFailed(chan, "cmd[2] isn't \"true\" nor \"false\"");
                        } else if(cmd[1].equals("wind")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else if(ready && musicPaused)
                                sendCommandFailed(chan, "Main.musicPaused == true");
                            else{
                                try{
                                    int time = Integer.parseInt(cmd[2]);
                                    int duration = (int)(musicManager.scheduler.getCurrentTrack().getDuration() / 1000);
                                    if(time > duration)
                                        sendCommandFailed(chan, "cmd[2] value is too big");
                                    else if(time < 0)
                                        sendCommandFailed(chan, "cmd[2] value is too small");
                                    else{
                                        musicManager.scheduler.setTrackTime(time);
                                        sendCommandDone(chan);
                                    }
                                } catch(NumberFormatException ex){
                                    sendCommandFailed(chan, "cmd[2] isn't an integer");
                                } catch(Exception ex){
                                    sendCommandFailed(chan, "Unexpected error");
                                }
                            }
                        } else if(cmd[1].equals("volume")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                try{
                                    int volume = Integer.parseInt(cmd[2]);
                                    if(volume > 100)
                                        sendCommandFailed(chan, "cmd[2] value is too big");
                                    else if(volume < 0)
                                        sendCommandFailed(chan, "cmd[2] value is too small");
                                    else{
                                        musicManager.scheduler.setVolume(volume);
                                        sendCommandDone(chan);
                                    }
                                } catch(NumberFormatException ex){
                                    sendCommandFailed(chan, "cmd[2] isn't an integer");
                                } catch(Exception ex){
                                    sendCommandFailed(chan, "Unexpected error");
                                }
                            }
                        } else
                            sendCommandFailed(chan, "Unknown argument");
                    } else if(cmd[0].equals("files") && cmd.length == 2){
                        if(cmd[1].equals("unload")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                sendCommandInProgress(chan);
                                unloadMusic();
                                sendCommandDone(chan);
                            }
                        } else if(cmd[1].equals("reload")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                sendCommandInProgress(chan);
                                reloadMusic();
                                sendCommandDone(chan);
                                musicManager.scheduler.playRandomTrack();
                            }
                        } else if(cmd[1].equals("load")){
                            if(!ready)
                                sendCommandFailed(chan, "Main.ready == false");
                            else{
                                sendCommandInProgress(chan);
                                loadMusic();
                                sendCommandDone(chan);
                            }
                        }
                    } else
                        sendCommandFailed(chan, "Unknown command: " + cmd[0]);
//                } else
//                    sendCommandFailed(chan, "You don't have permissions to do this command, sorry!");
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
                    command(chan, user, msg, userName);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}
