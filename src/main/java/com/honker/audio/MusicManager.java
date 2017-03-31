package com.honker.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class MusicManager {

    private AudioPlayer player;
    private TrackScheduler scheduler;

    public AudioProvider getAudioProvider() {
        return new AudioProvider(getPlayer());
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public void setPlayer(AudioPlayer player) {
        this.player = player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(TrackScheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    public MusicManager(AudioPlayerManager manager) {
        setPlayer(manager.createPlayer());
        setScheduler(new TrackScheduler(getPlayer()));
        player.addListener(scheduler);
    }
}
