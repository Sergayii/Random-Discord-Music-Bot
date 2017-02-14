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
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.ReconnectSuccessEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;

public class Main extends Operations{

    public static final String COMMAND_SYMBOL = "!";
    public static String BOT_TOKEN, MAIN_CHANNEL_ID, VOICE_CHANNEL_ID, GUILD_ID, MUSIC_PATH;

    public static boolean ready = false, musicPaused = true;

    public static IChannel mainChannel;

    public static MusicManager musicManager;
    public static AudioPlayerManager playerManager;

    public static UserVar[] users = new UserVar[1000];
    public static Bot bot;

    public static ArrayList<File> music = new ArrayList<File>();

    @EventSubscriber
    public void onReconnectSuccessEvent(ReconnectSuccessEvent e){
        updateUsers();
    }

    @EventSubscriber
    public void onReadyEvent(ReadyEvent e){
        updateUsers();

        mainChannel = bot.client.getChannelByID(MAIN_CHANNEL_ID);

        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        musicManager = new MusicManager(playerManager);

        IGuild guild = bot.client.getGuildByID(GUILD_ID);
        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        playMusic();
        
        ready = true;
    }

    public static void unloadMusic(){
        musicManager.scheduler.stop();
        music.clear();
        musicManager.scheduler.queue.clear();
        musicManager.scheduler.setCurrentTrack(null);
    }

    public static void loadMusic(){
        List<String> filesToLoad;
        try {
            filesToLoad = Files.walk(new File(MUSIC_PATH).toPath()).filter(path->Files.isRegularFile(path)&&!music.contains(path.toFile())).map(path->path.toFile().toString()).collect(Collectors.toList());
        } catch (IOException ex) {
            ex.printStackTrace();
            filesToLoad = null;
        }
        
        if(filesToLoad != null){
            for(String fileName : filesToLoad)
                load(fileName);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            ArrayList<File> loadedFiles = new ArrayList<File>();

            for(AudioTrack track : musicManager.scheduler.queue)
                loadedFiles.add(new File(filesToLoad.get(musicManager.scheduler.queue.indexOf(track))));

            music = loadedFiles;

            HashSet<File> newMusic = new HashSet<File>(music);
            music = new ArrayList<File>(newMusic);

            HashSet<AudioTrack> newQueue = new HashSet<AudioTrack>(musicManager.scheduler.queue);
            musicManager.scheduler.queue = new ArrayList<AudioTrack>(newQueue);

            musicManager.scheduler.shufflePlaylist();
        }
    }

    public static void reloadMusic(){
        unloadMusic();
        loadMusic();

        musicManager.scheduler.resume();
    }

    public static void playMusic(){
        IVoiceChannel musicChannel = bot.client.getVoiceChannelByID(VOICE_CHANNEL_ID);
        try {
            musicChannel.leave();
            Thread.sleep(3000);
            musicChannel.join();
            musicManager.player.setPaused(true);

            reloadMusic();

            if(!music.isEmpty()){
                musicManager.scheduler.play(musicManager.scheduler.queue.get(0));
            } else
                musicChannel.leave();

            musicManager.player.setVolume(100);
            musicManager.player.setPaused(false);
            
            musicPaused = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void load(String trackUrl){
        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {}

            @Override
            public void noMatches() {
                System.out.println("Can't find song: " + trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {}
        });
    }

    public static void queue(AudioTrack track){
        musicManager.scheduler.queue(track);
    }

    public static boolean play(AudioTrack track){
        return musicManager.scheduler.play(track);
    }

    public static void shutdown(){
        exit("I'm leaving to apply an update, goodbye!");
        System.exit(0);
    }

    public static void restart() throws InterruptedException{
        exit("Restarting");
        join();
    }

    public static void exit(){
        exit("");
    }

    public static void exit(String exitMessage){
        List<IVoiceChannel> voiceChannels = bot.client.getConnectedVoiceChannels();
        
        new Thread(new Runnable(){
            
            @Override
            public void run(){
                for(IVoiceChannel channel : voiceChannels){
                    if(channel.isConnected())
                        channel.leave();
                }
            }
        }).start();

        try {
            if(exitMessage != null && !exitMessage.equals(""))
                mainChannel.sendMessage(exitMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            bot.client.logout();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        musicPaused = true;
        ready = false;
    }

    public static void join() throws InterruptedException{
        try {
            bot = new Bot();
        } catch (DiscordException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws DiscordException, InterruptedException, FileNotFoundException, NoSuchFieldException{
        Scanner settingsReader = new Scanner(new File("./settings.txt"));
        StringBuilder string = new StringBuilder();
        while(settingsReader.hasNext()){
            String line = settingsReader.nextLine();
            string.append(line);
            string.append(System.lineSeparator());
        }

        String[] settingsList = string.toString().split(System.lineSeparator());
        ArrayList<String> settings = new ArrayList<String>();
        settings.addAll(Arrays.asList(settingsList));

        for(String setting : settings){
            if(setting.startsWith("BOT_TOKEN = "))
                BOT_TOKEN = setting.replaceFirst("BOT_TOKEN = ", "");
            else if(setting.startsWith("MAIN_CHANNEL_ID = "))
                MAIN_CHANNEL_ID = setting.replaceFirst("MAIN_CHANNEL_ID = ", "");
            else if(setting.startsWith("VOICE_CHANNEL_ID = "))
                VOICE_CHANNEL_ID = setting.replaceFirst("VOICE_CHANNEL_ID = ", "");
            else if(setting.startsWith("GUILD_ID = "))
                GUILD_ID = setting.replaceFirst("GUILD_ID = ", "");
            else if(setting.startsWith("MUSIC_PATH = "))
                MUSIC_PATH = setting.replaceFirst("MUSIC_PATH = ", "");
            else
                throw new IllegalArgumentException("No such setting");
        }

        join();
    }
}
