package com.honker.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class MusicManager {
    
    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    
    public MusicManager(AudioPlayerManager manager){
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }
    
    public AudioProvider getAudioProvider() {
        return new AudioProvider(player);
    }
}
