package com.honker.listeners;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;

import static com.honker.main.Main.main;

public class MessageListener implements IListener<MessageReceivedEvent> {

    public void command(IChannel chan, IUser user, String msg, String userName, List<Attachment> attachments) throws FileNotFoundException {
        if(msg.length() > main.COMMAND_SYMBOL.length()) {
            chan.setTypingStatus(true);
            msg = msg.substring(main.COMMAND_SYMBOL.length());
            while(msg.contains("  ")) {
                msg = msg.replace("  ", " ");
            }
            msg = msg.trim();
            String msgL = msg.toLowerCase();
            String[] cmd = msgL.split(" ");
            
            if(cmd[0].equals("help")) {
                if(cmd.length == 1) {
                    sendHelp();
                }
            } else if(cmd[0].equals("track")) {
                if(cmd.length == 1) {
                    if(!main.ready) {
                        sendCommandFailed("The music isn't even started yet!!!");
                    } else {
                        main.musicManager.scheduler.sendCurrentPlayingTrack();
                    }
                }
            } else if(cmd[0].equals("playlist")) {
                if(cmd.length == 1) {
                    if(!main.ready) {
                        sendCommandFailed("The music isn't even started yet!!!");
                    } else {
                        sendTracksList();
                    }
                }
            } else if(cmd[0].equals("search")) {
                if(cmd.length > 1) {
                    if(main.ready) {
                        cmd = msgL.split(" ", 2);

                        ArrayList<File> results = new ArrayList<File>();
                        String search = cmd[1];
                        for(File file : main.music) {
                            AudioTrack track = main.musicManager.scheduler.queue.get(main.music.indexOf(file));
                            String trackName = main.musicManager.scheduler.getTrackName(track).toLowerCase();
                            if(trackName.contains(search)) {
                                results.add(file);
                            }
                        }

                        ArrayList<AudioTrack> tracks = new ArrayList<AudioTrack>();
                        for(File file : results) {
                            tracks.add(main.musicManager.scheduler.queue.get(main.music.indexOf(file)));
                        }

                        String answer = new String();
                        for(AudioTrack track : tracks) {
                            answer += main.musicManager.scheduler.getTrackInfo(track) + 
                                      System.lineSeparator() + System.lineSeparator();
                        }

                        if(!answer.equals("")) {
                            if(answer.length() <= 1900) {
                                sendMessage("This is what i've found:\n" + answer);
                            } else {
                                File file;
                                try {
                                    file = File.createTempFile("searchResults", ".txt");
                                } catch(IOException ex) {
                                    ex.printStackTrace();
                                    file = null;
                                }

                                if(file != null) {
                                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

                                    try {
                                        writer.write(answer.replace("```", ""));
                                        writer.newLine();
                                        writer.flush();
                                    } catch(IOException e) {
                                        file = null;
                                    }

                                    if(file != null) {
                                        sendFile("There's just too much tracks, i'll send them in a file", file);
                                    } else {
                                        sendMessage("Something broke, and i can't send the result");
                                    }
                                }
                            }
                        } else {
                            sendMessage("I found nothing!");
                        }
                    } else {
                        sendCommandFailed("The music isn't even started yet!!!");
                    }
                }
            } else if(cmd[0].equals("ping")) {
                if(cmd.length == 1) {
                    sendMessage("Pong!");
                }
            } else if(cmd[0].equals("bot")) {
                if(cmd[1].equals("restart")) {
                    if(cmd.length == 2) {
                        sendCommandInProgress();
                        main.restart();
                        sendCommandDone();
                    }
                } else if(cmd[1].equals("shutdown")) {
                    if(cmd.length == 2) {
                        if(!main.ready) {
                            sendCommandFailed("I'm not done launching, wait!");
                        } else {
                            main.shutdown();
                        }
                    }
                } else if(cmd[1].equals("avatar")) {
                    if(cmd.length == 2 && attachments.size() == 1) {
                        cmd = msg.split(" ", 3);
                        try {
                            main.bot.client.changeAvatar(Image.forUrl("png", attachments.get(0).getUrl()));
                            sendCommandDone();
                        } catch(Exception e) {
                            e.printStackTrace();
                            sendCommandFailed("Something went wrong, sorry!");
                        }
                    }
                } else if(cmd[1].equals("nick")) {
                    if(cmd.length > 1) {
                        cmd = msg.split(" ", 3);
                        try {
                            main.bot.client.changeUsername(cmd[2]);
                            sendCommandDone();
                        } catch(Exception e) {
                            e.printStackTrace();
                            sendCommandFailed("Something went wrong, sorry!");
                        }
                    }
                }
            } else if(cmd[0].equals("music")) {
                if(cmd[1].equals("resume")) {
                    if(cmd.length == 2) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else if(main.ready && !main.musicPaused) {
                            sendCommandFailed("The music isn't paused!");
                        } else if(main.ready && main.musicPaused) {
                            main.musicManager.scheduler.resume();
                            sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("pause")) {
                    if(cmd.length == 2) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else if(main.ready && main.musicPaused) {
                            sendCommandFailed("The music is already paused!");
                        } else if(main.musicManager.scheduler.getCurrentTrack() != null) {
                            main.musicManager.scheduler.pause();
                            sendCommandDone();
                        } else {
                            sendCommandFailed("No track is playing right now...");
                        }
                    }
                } else if(cmd[1].equals("stop")) {
                    if(cmd.length == 2) {
                        main.musicManager.scheduler.stop();
                        sendCommandDone();
                    }
                } else if(cmd[1].equals("play")) {
                    if(cmd.length == 2) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.musicManager.scheduler.playRandomTrack();
                            sendCommandDone();
                        }
                    } else if(cmd.length == 3) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else {
                            try {
                                int index = Integer.parseInt(cmd[2]);
                                if(index >= main.musicManager.scheduler.queue.size()) {
                                    sendCommandFailed("ID is too big");
                                } else if(index < 0) {
                                    sendCommandFailed("ID is too small");
                                } else {
                                    boolean trackPlayed = main.musicManager.scheduler.play
                                                          (main.musicManager.scheduler.queue.get(index));
                                    if(!trackPlayed) {
                                        sendCommandFailed("Track with this ID doesn't exist");
                                    } else {
                                        sendCommandDone();
                                    }
                                }
                            } catch(NumberFormatException ex) {
                                sendCommandFailed("ID isn't an integer");
                            } catch(Exception ex) {
                                sendCommandFailed("Unexpected error");
                                ex.printStackTrace();
                            }
                        }
                    }
                } else if(cmd[1].equals("leave")) {
                    if(cmd.length == 2) {
                        sendMessage("Leaving the voice channel, wait!");
                        main.musicManager.scheduler.leaveMusicChannel();
                        sendCommandDone();
                    }
                } else if(cmd[1].equals("join")) {
                    if(cmd.length == 2) {
                        sendMessage("Attempting to join the voice channel, wait!");
                        try {
                            main.musicManager.scheduler.joinMusicChannel();
                        } catch(MissingPermissionsException ex) {
                            sendCommandFailed("Something went wrong!");
                            ex.printStackTrace();
                            return;
                        }
                        sendCommandDone();
                    }
                } else if(cmd[1].equals("rejoin")) {
                    if(cmd.length == 2) {
                        sendMessage("Attempting to rejoin to voice channel, wait!");
                        try {
                            main.musicManager.scheduler.rejoinMusicChannel();
                        } catch(Exception ex) {
                            sendCommandFailed("Something went wrong!");
                            ex.printStackTrace();
                            return;
                        }
                        sendCommandDone();
                    }
                } else if(cmd[1].equals("replay")) {
                    if(cmd.length == 2) {
                        if(main.musicManager.scheduler.getCurrentTrack() != null) {
                            main.musicManager.scheduler.setTrackTime(0);
                            sendCommandDone();
                        } else {
                            sendCommandFailed("No track is playing right now...");
                        }
                    }
                } else if(cmd[1].equals("next")) {
                    if(cmd.length == 2) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.musicManager.scheduler.nextTrack();
                            sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("previous")) {
                    if(cmd.length == 2) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.musicManager.scheduler.previousTrack();
                            sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("shuffle")) {
                    if(cmd.length == 2) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.musicManager.scheduler.shufflePlaylist();
                            sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("sort")) {
                    if(cmd.length == 2) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.musicManager.scheduler.sortPlaylist();
                            sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("loop")) {
                    if(cmd.length == 3) {
                        if(cmd[2].equals("true")) {
                            main.musicManager.scheduler.looping = true;
                            sendCommandDone();
                        } else if(cmd[2].equals("false")) {
                            main.musicManager.scheduler.looping = false;
                            sendCommandDone();
                        } else {
                            sendCommandFailed("Argument isn't \"true\" nor \"false\"");
                        }
                    }
                } else if(cmd[1].equals("wind")) {
                    if(cmd.length == 3) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else if(main.ready && main.musicPaused) {
                            sendCommandFailed("The music is paused!");
                        } else {
                            try {
                                int time = Integer.parseInt(cmd[2]);
                                int duration = (int)(main.musicManager.scheduler.getCurrentTrack().getDuration() / 1000);
                                if(time > duration) {
                                    sendCommandFailed("\"time\" is too big");
                                } else if(time < 0) {
                                    sendCommandFailed("\"time\" is too small");
                                } else {
                                    main.musicManager.scheduler.setTrackTime(time);
                                    sendCommandDone();
                                }
                            } catch(NumberFormatException ex) {
                                sendCommandFailed("\"time\" isn't an integer");
                            } catch(Exception ex) {
                                sendCommandFailed("Unexpected error");
                            }
                        }
                    }
                } else if(cmd[1].equals("volume")) {
                    if(cmd.length == 3) {
                        if(!main.ready) {
                            sendCommandFailed("The music isn't even started yet!");
                        } else {
                            try {
                                int volume = Integer.parseInt(cmd[2]);
                                if(volume > 100) {
                                    sendCommandFailed("\"vol\" is too big");
                                } else if(volume < 0) {
                                    sendCommandFailed("\"vol\" is too small");
                                } else {
                                    main.musicManager.scheduler.setVolume(volume);
                                    sendCommandDone();
                                }
                            } catch(NumberFormatException ex) {
                                sendCommandFailed("\"vol\" isn't an integer");
                            } catch(Exception ex) {
                                sendCommandFailed("Unexpected error");
                            }
                        }
                    }
                } else if(cmd[1].equals("queue")) {
                    if(cmd.length > 2) {
                        boolean queueWasEmpty = main.musicManager.scheduler.queue.isEmpty();
                        int tracksLoaded = 0;
                        
                        cmd = msgL.split(" ", 3);
                        
                        for(File file : main.music) {
                            if(file.getAbsolutePath().toLowerCase().contains(cmd[2])) {
                                main.musicManager.scheduler.queue(file);
                                tracksLoaded++;
                            }
                        }

                        if(tracksLoaded == 0) {
                            sendMessage("I found no tracks with this name!");
                        } else {
                            sendMessage("Queued " + tracksLoaded + " tracks");
                        }
                        
                        if(queueWasEmpty && !main.musicManager.scheduler.queue.isEmpty()) {
                            main.musicManager.scheduler.play(main.musicManager.scheduler.queue.get(0));
                        }
                    }
                } else if(cmd[1].equals("clear")) {
                    if(cmd.length == 2) {
                        main.musicManager.scheduler.clearQueue();
                        sendCommandDone();
                    }
                }
            } else if(cmd[0].equals("files")) {
                if(cmd[1].equals("reload")) {
                    if(cmd.length == 2) {
                        main.reloadMusic();
                    }
                } else if(cmd[1].equals("unload")) {
                    if(cmd.length == 2) {
                        main.unloadMusic();
                    }
                }
            }
            chan.setTypingStatus(false);
        }
    }

    @Override
    public void handle(MessageReceivedEvent e) {
        IChannel chan = e.getMessage().getChannel();
        IUser user = e.getMessage().getAuthor();
        String msg = e.getMessage().getContent();
        String userName = user.getName();

        if(chan.getID().equals(main.MAIN_CHANNEL_ID)) {
            if(msg.startsWith(main.COMMAND_SYMBOL) && !user.isBot()) {
                try {
                    command(chan, user, msg, userName, e.getMessage().getAttachments());
                } catch(FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
