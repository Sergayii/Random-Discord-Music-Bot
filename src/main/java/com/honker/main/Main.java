package com.honker.main;

import com.honker.audio.MusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;

public class Main {
    
    public static Main main;
    
    public final String COMMAND_SYMBOL = "!";
    public String BOT_TOKEN, MAIN_CHANNEL_ID, VOICE_CHANNEL_ID, GUILD_ID, MUSIC_PATH;

    public boolean ready = false, musicPaused = true;

    public IMessage progress;
    public IChannel mainChannel;

    public MusicManager musicManager;
    public AudioPlayerManager playerManager;
    
    public Bot bot;

    public ArrayList<File> music = new ArrayList<File>();

    @EventSubscriber
    public void onReadyEvent(ReadyEvent e) {
        main.mainChannel = main.bot.client.getChannelByID(main.MAIN_CHANNEL_ID);

        main.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(main.playerManager);
        main.musicManager = new MusicManager(main.playerManager);

        IGuild guild = main.bot.client.getGuildByID(main.GUILD_ID);
        try {
            guild.getAudioManager().setAudioProvider(main.musicManager.getAudioProvider());
        } catch(NullPointerException ex) {
            ex.printStackTrace();
        }

        main.playMusic();

        main.ready = true;

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                while(main.ready) {
                    main.musicManager.scheduler.updateTrack();
                    main.musicManager.scheduler.updateStatus();
                    try {
                        Thread.sleep(3000);
                    } catch(InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    public void unloadMusic() {
        musicManager.scheduler.stop();
        music.clear();
        musicManager.scheduler.queue.clear();
        musicManager.scheduler.setCurrentTrack(null);
    }

    public void loadMusic() {
        List<String> filesToLoad;
        try {
            filesToLoad = Files.walk(new File(MUSIC_PATH).toPath()).filter(path -> Files.isRegularFile(path) && !music.contains(path.toFile())).map(path -> path.toFile().toString()).collect(Collectors.toList());
        } catch(IOException ex) {
            ex.printStackTrace();
            filesToLoad = null;
        }

        if(filesToLoad != null) {
            for(String fileName : filesToLoad) {
                load(fileName);
            }
            try {
                Thread.sleep(10000);
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }

            ArrayList<File> loadedFiles = new ArrayList<File>();

            for(AudioTrack track : musicManager.scheduler.queue) {
                loadedFiles.add(new File(filesToLoad.get(musicManager.scheduler.queue.indexOf(track))));
            }

            music = loadedFiles;

            musicManager.scheduler.sortMusic();
            musicManager.scheduler.shufflePlaylist();
        }
    }

    public void reloadMusic() {
        unloadMusic();
        loadMusic();

        musicManager.scheduler.resume();
    }

    public void playMusic() {
        IVoiceChannel musicChannel = bot.client.getVoiceChannelByID(VOICE_CHANNEL_ID);
        try {
            musicChannel.leave();
            Thread.sleep(3000);
            musicChannel.join();
            musicManager.player.setPaused(true);

            reloadMusic();

            if(!musicManager.scheduler.queue.isEmpty()) {
                musicManager.scheduler.play(musicManager.scheduler.queue.get(0));
            } else {
                musicChannel.leave();
            }

            musicManager.player.setVolume(100);
            musicManager.player.setPaused(false);

            musicPaused = false;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void load(String trackUrl) {
        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {

           @Override
           public void trackLoaded(AudioTrack track) {
               musicManager.scheduler.queue(track);
           }

           @Override
           public void playlistLoaded(AudioPlaylist playlist) {}

           @Override
           public void noMatches() {}

           @Override
           public void loadFailed(FriendlyException ex) {
               ex.printStackTrace();
           }
       });
    }

    public void shutdown() {
        exit("I'm leaving to apply an update, goodbye!");
        if(progress != null) {
            try {
                progress.delete();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        System.exit(0);
    }

    public void restart() {
        exit("Restarting");
        join();
    }

    public void exit() {
        exit("");
    }

    public void exit(String exitMessage) {
        bot.client.changeStatus(Status.empty());

        List<IVoiceChannel> voiceChannels = bot.client.getConnectedVoiceChannels();

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                for(IVoiceChannel channel : voiceChannels) {
                    if(channel.isConnected()) {
                        channel.leave();
                    }
                }
            }
        });
        t.start();

        try {
            if(exitMessage != null && !exitMessage.isEmpty()) {
                mainChannel.sendMessage(exitMessage);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        try {
            bot.client.logout();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        musicPaused = true;
        ready = false;
    }

    public void join() {
        bot = new Bot();
    }

    public void init() throws DiscordException, InterruptedException, FileNotFoundException {
        Scanner settingsReader = new Scanner(new File("./settings.txt"));
        StringBuilder string = new StringBuilder();
        while(settingsReader.hasNext()) {
            String line = settingsReader.nextLine();
            string.append(line);
            string.append(System.lineSeparator());
        }

        String[] settingsList = string.toString().split(System.lineSeparator());
        ArrayList<String> settings = new ArrayList<String>();
        settings.addAll(Arrays.asList(settingsList));

        for(String setting : settings) {
            if(setting.startsWith("BOT_TOKEN = ")) {
                BOT_TOKEN = setting.replaceFirst("BOT_TOKEN = ", "");
            } else if(setting.startsWith("MAIN_CHANNEL_ID = ")) {
                MAIN_CHANNEL_ID = setting.replaceFirst("MAIN_CHANNEL_ID = ", "");
            } else if(setting.startsWith("VOICE_CHANNEL_ID = ")) {
                VOICE_CHANNEL_ID = setting.replaceFirst("VOICE_CHANNEL_ID = ", "");
            } else if(setting.startsWith("GUILD_ID = ")) {
                GUILD_ID = setting.replaceFirst("GUILD_ID = ", "");
            } else if(setting.startsWith("MUSIC_PATH = ")) {
                MUSIC_PATH = setting.replaceFirst("MUSIC_PATH = ", "");
            } else {
                throw new IllegalArgumentException("No such setting");
            }
        }
        
        join();
    }
    
    public static void main(String[] args) throws DiscordException, InterruptedException, FileNotFoundException {
        main = new Main();
        main.init();
    }
}
