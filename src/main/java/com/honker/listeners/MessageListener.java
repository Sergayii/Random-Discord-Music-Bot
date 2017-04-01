package com.honker.listeners;

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
                    main.getBot().sendHelp();
                }
            } else if(cmd[0].equals("track")) {
                if(cmd.length == 1) {
                    if(!main.getBot().getClient().isReady()) {
                        main.getBot().sendCommandFailed("The music isn't even started yet!!!");
                    } else {
                        main.getMusicManager().getScheduler().sendCurrentPlayingTrack();
                    }
                }
            } else if(cmd[0].equals("playlist")) {
                if(cmd.length == 1) {
                    if(!main.getBot().getClient().isReady()) {
                        main.getBot().sendCommandFailed("The music isn't even started yet!!!");
                    } else {
                        main.getBot().sendTracksList();
                    }
                }
            } else if(cmd[0].equals("search")) {
                if(cmd.length > 1) {
                    if(main.getBot().getClient().isReady()) {
                        cmd = msgL.split(" ", 2);

                        ArrayList<File> results = new ArrayList<File>();
                        String search = cmd[1];
                        for(File file : main.getMusic()) {
                            AudioTrack track = main.getMusicManager().getScheduler().queue.get(main.getMusic().indexOf(file));
                            String trackName = main.getMusicManager().getScheduler().getTrackName(track).toLowerCase();
                            if(trackName.contains(search)) {
                                results.add(file);
                            }
                        }

                        ArrayList<AudioTrack> tracks = new ArrayList<AudioTrack>();
                        for(File file : results) {
                            tracks.add(main.getMusicManager().getScheduler().queue.get(main.getMusic().indexOf(file)));
                        }

                        String answer = new String();
                        for(AudioTrack track : tracks) {
                            answer += main.getMusicManager().getScheduler().getTrackInfo(track) + 
                                      System.lineSeparator() + System.lineSeparator();
                        }

                        if(!answer.equals("")) {
                            if(answer.length() <= 1900) {
                                main.getBot().sendMessage("This is what i've found:\n" + answer);
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
                                        main.getBot().sendFile("There's just too much tracks, i'll send them in a file", file);
                                    } else {
                                        main.getBot().sendMessage("Something broke, and i can't send the result");
                                    }
                                }
                            }
                        } else {
                            main.getBot().sendMessage("I found nothing!");
                        }
                    } else {
                        main.getBot().sendCommandFailed("The music isn't even started yet!!!");
                    }
                }
            } else if(cmd[0].equals("ping")) {
                if(cmd.length == 1) {
                    main.getBot().sendMessage("Pong!");
                }
            } else if(cmd[0].equals("bot")) {
                if(cmd[1].equals("restart")) {
                    if(cmd.length == 2) {
                        main.getBot().sendCommandInProgress();
                        main.restart();
                        main.getBot().sendCommandDone();
                    }
                } else if(cmd[1].equals("shutdown")) {
                    if(cmd.length == 2) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("I'm not done launching, wait!");
                        } else {
                            main.shutdown();
                        }
                    }
                } else if(cmd[1].equals("avatar")) {
                    if(cmd.length == 2 && attachments.size() == 1) {
                        cmd = msg.split(" ", 3);
                        try {
                            main.getBot().getClient().changeAvatar(Image.forUrl("png", attachments.get(0).getUrl()));
                            main.getBot().sendCommandDone();
                        } catch(Exception e) {
                            e.printStackTrace();
                            main.getBot().sendCommandFailed("Something went wrong, sorry!");
                        }
                    }
                } else if(cmd[1].equals("nick")) {
                    if(cmd.length > 1) {
                        cmd = msg.split(" ", 3);
                        try {
                            main.getBot().getClient().changeUsername(cmd[2]);
                            main.getBot().sendCommandDone();
                        } catch(Exception e) {
                            e.printStackTrace();
                            main.getBot().sendCommandFailed("Something went wrong, sorry!");
                        }
                    }
                }
            } else if(cmd[0].equals("music")) {
                if(cmd[1].equals("resume")) {
                    if(cmd.length == 2) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else if(main.getBot().getClient().isReady() && !main.getMusicManager().getPlayer().isPaused()) {
                            main.getBot().sendCommandFailed("The music isn't paused!");
                        } else if(main.getBot().getClient().isReady() && main.getMusicManager().getPlayer().isPaused()) {
                            main.getMusicManager().getScheduler().resume();
                            main.getBot().sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("pause")) {
                    if(cmd.length == 2) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else if(main.getBot().getClient().isReady() && main.getMusicManager().getPlayer().isPaused()) {
                            main.getBot().sendCommandFailed("The music is already paused!");
                        } else if(main.getMusicManager().getScheduler().getCurrentTrack() != null) {
                            main.getMusicManager().getScheduler().pause();
                            main.getBot().sendCommandDone();
                        } else {
                            main.getBot().sendCommandFailed("No track is playing right now...");
                        }
                    }
                } else if(cmd[1].equals("stop")) {
                    if(cmd.length == 2) {
                        main.getMusicManager().getScheduler().stop();
                        main.getBot().sendCommandDone();
                    }
                } else if(cmd[1].equals("play")) {
                    if(cmd.length == 2) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.getMusicManager().getScheduler().playRandomTrack();
                            main.getBot().sendCommandDone();
                        }
                    } else if(cmd.length == 3) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else {
                            try {
                                int index = Integer.parseInt(cmd[2]);
                                if(index >= main.getMusicManager().getScheduler().queue.size()) {
                                    main.getBot().sendCommandFailed("ID is too big");
                                } else if(index < 0) {
                                    main.getBot().sendCommandFailed("ID is too small");
                                } else {
                                    boolean trackPlayed = main.getMusicManager().getScheduler().play
                                                          (main.getMusicManager().getScheduler().queue.get(index));
                                    if(!trackPlayed) {
                                        main.getBot().sendCommandFailed("Track with this ID doesn't exist");
                                    } else {
                                        main.getBot().sendCommandDone();
                                    }
                                }
                            } catch(NumberFormatException ex) {
                                main.getBot().sendCommandFailed("ID isn't an integer");
                            } catch(Exception ex) {
                                main.getBot().sendCommandFailed("Unexpected error");
                                ex.printStackTrace();
                            }
                        }
                    }
                } else if(cmd[1].equals("leave")) {
                    if(cmd.length == 2) {
                        main.getBot().sendMessage("Leaving the voice channel, wait!");
                        main.getMusicManager().getScheduler().leaveMusicChannel();
                        main.getBot().sendCommandDone();
                    }
                } else if(cmd[1].equals("join")) {
                    if(cmd.length == 2) {
                        main.getBot().sendMessage("Attempting to join the voice channel, wait!");
                        try {
                            main.getMusicManager().getScheduler().joinMusicChannel();
                        } catch(MissingPermissionsException ex) {
                            main.getBot().sendCommandFailed("Something went wrong!");
                            ex.printStackTrace();
                            return;
                        }
                        main.getBot().sendCommandDone();
                    }
                } else if(cmd[1].equals("rejoin")) {
                    if(cmd.length == 2) {
                        main.getBot().sendMessage("Attempting to rejoin to voice channel, wait!");
                        try {
                            main.getMusicManager().getScheduler().rejoinMusicChannel();
                        } catch(Exception ex) {
                            main.getBot().sendCommandFailed("Something went wrong!");
                            ex.printStackTrace();
                            return;
                        }
                        main.getBot().sendCommandDone();
                    }
                } else if(cmd[1].equals("replay")) {
                    if(cmd.length == 2) {
                        if(main.getMusicManager().getScheduler().getCurrentTrack() != null) {
                            main.getMusicManager().getScheduler().setTrackTime(0);
                            main.getBot().sendCommandDone();
                        } else {
                            main.getBot().sendCommandFailed("No track is playing right now...");
                        }
                    }
                } else if(cmd[1].equals("next")) {
                    if(cmd.length == 2) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.getMusicManager().getScheduler().nextTrack();
                            main.getBot().sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("previous")) {
                    if(cmd.length == 2) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.getMusicManager().getScheduler().previousTrack();
                            main.getBot().sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("shuffle")) {
                    if(cmd.length == 2) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.getMusicManager().getScheduler().shufflePlaylist();
                            main.getBot().sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("sort")) {
                    if(cmd.length == 2) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else {
                            main.getMusicManager().getScheduler().sortPlaylist();
                            main.getBot().sendCommandDone();
                        }
                    }
                } else if(cmd[1].equals("loop")) {
                    if(cmd.length == 3) {
                        if(cmd[2].equals("true")) {
                            main.getMusicManager().getScheduler().looping = true;
                            main.getBot().sendCommandDone();
                        } else if(cmd[2].equals("false")) {
                            main.getMusicManager().getScheduler().looping = false;
                            main.getBot().sendCommandDone();
                        } else {
                            main.getBot().sendCommandFailed("Argument isn't \"true\" nor \"false\"");
                        }
                    }
                } else if(cmd[1].equals("wind")) {
                    if(cmd.length == 3) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else if(main.getBot().getClient().isReady() && main.getMusicManager().getPlayer().isPaused()) {
                            main.getBot().sendCommandFailed("The music is paused!");
                        } else {
                            try {
                                int time = Integer.parseInt(cmd[2]);
                                int duration = (int)(main.getMusicManager().getScheduler().getCurrentTrack().getDuration() / 1000);
                                if(time > duration) {
                                    main.getBot().sendCommandFailed("\"time\" is too big");
                                } else if(time < 0) {
                                    main.getBot().sendCommandFailed("\"time\" is too small");
                                } else {
                                    main.getMusicManager().getScheduler().setTrackTime(time);
                                    main.getBot().sendCommandDone();
                                }
                            } catch(NumberFormatException ex) {
                                main.getBot().sendCommandFailed("\"time\" isn't an integer");
                            } catch(Exception ex) {
                                main.getBot().sendCommandFailed("Unexpected error");
                            }
                        }
                    }
                } else if(cmd[1].equals("volume")) {
                    if(cmd.length == 3) {
                        if(!main.getBot().getClient().isReady()) {
                            main.getBot().sendCommandFailed("The music isn't even started yet!");
                        } else {
                            try {
                                int volume = Integer.parseInt(cmd[2]);
                                if(volume > 100) {
                                    main.getBot().sendCommandFailed("\"vol\" is too big");
                                } else if(volume < 0) {
                                    main.getBot().sendCommandFailed("\"vol\" is too small");
                                } else {
                                    main.getMusicManager().getScheduler().setVolume(volume);
                                    main.getBot().sendCommandDone();
                                }
                            } catch(NumberFormatException ex) {
                                main.getBot().sendCommandFailed("\"vol\" isn't an integer");
                            } catch(Exception ex) {
                                main.getBot().sendCommandFailed("Unexpected error");
                            }
                        }
                    }
                } else if(cmd[1].equals("queue")) {
                    if(cmd.length > 2) {
                        boolean queueWasEmpty = main.getMusicManager().getScheduler().queue.isEmpty();
                        int tracksLoaded = 0;
                        
                        cmd = msgL.split(" ", 3);
                        
                        for(File file : main.getMusic()) {
                            if(file.getAbsolutePath().toLowerCase().contains(cmd[2])) {
                                main.getMusicManager().getScheduler().queue(file);
                                tracksLoaded++;
                            }
                        }

                        if(tracksLoaded == 0) {
                            main.getBot().sendMessage("I found no tracks with this name!");
                        } else {
                            main.getBot().sendMessage("Queued " + tracksLoaded + " tracks");
                        }
                        
                        if(queueWasEmpty && !main.getMusicManager().getScheduler().queue.isEmpty()) {
                            main.getMusicManager().getScheduler().play(main.getMusicManager().getScheduler().queue.get(0));
                        }
                    }
                } else if(cmd[1].equals("clear")) {
                    if(cmd.length == 2) {
                        main.getMusicManager().getScheduler().clearQueue();
                        main.getBot().sendCommandDone();
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

        if(chan.getID().equals(main.getMainChannelID())) {
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
