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
    
    public final String[] marks = new String[] {"!", "#", "$", "%", "^", "&", "*", "(", ")", "+", "=", "-", "<", ">", ",", ".", ":", ";", "@", "?", "\n"};
    private String COMMAND_SYMBOL = "!";

    private String BOT_TOKEN;
    private String MAIN_CHANNEL_ID;
    private String VOICE_CHANNEL_ID;
    private String GUILD_ID;
    private String MUSIC_PATH;

    private boolean ready = false;
    private boolean musicPaused = true;
    
    private IMessage progress;
    private IChannel mainChannel;
    
    private MusicManager musicManager;
    private AudioPlayerManager playerManager;
    
    private Bot bot;
    private ArrayList<File> music = new ArrayList<File>();

    public String getCommandSymbol() {
        return COMMAND_SYMBOL;
    }

    public void setCommandSymbol(String commandSymbol) {
        this.COMMAND_SYMBOL = commandSymbol;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }

    public void setBotToken(String botToken) {
        this.BOT_TOKEN = botToken;
    }

    public String getMainChannelID() {
        return MAIN_CHANNEL_ID;
    }

    public void setMainChannelID(String MainChannelID) {
        this.MAIN_CHANNEL_ID = MainChannelID;
    }

    public String getVoiceChannelID() {
        return VOICE_CHANNEL_ID;
    }

    public void setVoiceChannelID(String VoiceChannelID) {
        this.VOICE_CHANNEL_ID = VoiceChannelID;
    }

    public String getGuildID() {
        return GUILD_ID;
    }

    public void setGuildID(String GuildID) {
        this.GUILD_ID = GuildID;
    }

    public String getMusicPath() {
        return MUSIC_PATH;
    }

    public void setMusicPath(String MusicPath) {
        this.MUSIC_PATH = MusicPath;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isMusicPaused() {
        return musicPaused;
    }

    public void setMusicPaused(boolean musicPaused) {
        this.musicPaused = musicPaused;
    }

    public IMessage getProgress() {
        return progress;
    }

    public void setProgress(IMessage progress) {
        this.progress = progress;
    }

    public IChannel getMainChannel() {
        return mainChannel;
    }

    public void setMainChannel(IChannel mainChannel) {
        this.mainChannel = mainChannel;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public void setMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public void setPlayerManager(AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public ArrayList<File> getMusic() {
        return music;
    }

    public void setMusic(ArrayList<File> music) {
        this.music = music;
    }
    
    @EventSubscriber
    public void onReadyEvent(ReadyEvent e) {
        main.setMainChannel(main.getBot().getClient().getChannelByID(main.getMainChannelID()));

        main.setPlayerManager(new DefaultAudioPlayerManager());
        AudioSourceManagers.registerLocalSource(main.getPlayerManager());
        main.setMusicManager(new MusicManager(main.getPlayerManager()));

        IGuild guild = main.getBot().getClient().getGuildByID(main.getGuildID());
        try {
            guild.getAudioManager().setAudioProvider(main.getMusicManager().getAudioProvider());
        } catch(NullPointerException ex) {
            ex.printStackTrace();
        }

        main.playMusic();

        main.setReady(true);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                while(main.isReady()) {
                    main.getMusicManager().getScheduler().updateTrack();
                    main.getMusicManager().getScheduler().updateStatus();
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
        getMusicManager().getScheduler().stop();
        getMusic().clear();
        getMusicManager().getScheduler().queue.clear();
        getMusicManager().getScheduler().setCurrentTrack(null);
    }

    public void loadMusic() {
        List<String> filesToLoad;
        try {
            filesToLoad = Files.walk(new File(getMusicPath()).toPath()).filter(path -> Files.isRegularFile(path) && !music.contains(path.toFile())).map(path -> path.toFile().toString()).collect(Collectors.toList());
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

            for(AudioTrack track : getMusicManager().getScheduler().queue) {
                loadedFiles.add(new File(filesToLoad.get(getMusicManager().getScheduler().queue.indexOf(track))));
            }

            setMusic(loadedFiles);

            getMusicManager().getScheduler().sortMusic();
            getMusicManager().getScheduler().shufflePlaylist();
        }
    }

    public void reloadMusic() {
        unloadMusic();
        loadMusic();

        getMusicManager().getScheduler().resume();
    }

    public void playMusic() {
        IVoiceChannel musicChannel = getBot().getClient().getVoiceChannelByID(getVoiceChannelID());
        try {
            musicChannel.leave();
            Thread.sleep(3000);
            musicChannel.join();
            getMusicManager().getPlayer().setPaused(true);

            reloadMusic();

            if(!musicManager.getScheduler().queue.isEmpty()) {
                getMusicManager().getScheduler().play(getMusicManager().getScheduler().queue.get(0));
            } else {
                musicChannel.leave();
            }

            getMusicManager().getPlayer().setVolume(100);
            getMusicManager().getPlayer().setPaused(false);

            setMusicPaused(false);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void load(String trackUrl) {
        getPlayerManager().loadItem(trackUrl, new AudioLoadResultHandler() {

           @Override
           public void trackLoaded(AudioTrack track) {
                getMusicManager().getScheduler().queue(track);
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
        if(getProgress() != null) {
            try {
                getProgress().delete();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        
        exit("I'm leaving to apply an update, goodbye!");
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
        getBot().getClient().changeStatus(Status.empty());

        List<IVoiceChannel> voiceChannels = getBot().getClient().getConnectedVoiceChannels();

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
                getMainChannel().sendMessage(exitMessage);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        try {
            getBot().getClient().logout();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        setMusicPaused(true);
        setReady(false);
    }

    public void join() {
        setBot(new Bot());
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
                setBotToken(setting.replaceFirst("BOT_TOKEN = ", ""));
            } else if(setting.startsWith("MAIN_CHANNEL_ID = ")) {
                setMainChannelID(setting.replaceFirst("MAIN_CHANNEL_ID = ", ""));
            } else if(setting.startsWith("VOICE_CHANNEL_ID = ")) {
                setVoiceChannelID(setting.replaceFirst("VOICE_CHANNEL_ID = ", ""));
            } else if(setting.startsWith("GUILD_ID = ")) {
                setGuildID(setting.replaceFirst("GUILD_ID = ", ""));
            } else if(setting.startsWith("MUSIC_PATH = ")) {
                setMusicPath(setting.replaceFirst("MUSIC_PATH = ", ""));
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
