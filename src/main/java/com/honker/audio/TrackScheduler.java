package com.honker.audio;

import static com.honker.main.Main.VOICE_CHANNEL_ID;
import static com.honker.main.Main.bot;
import static com.honker.main.Main.load;
import static com.honker.main.Main.mainChannel;
import static com.honker.main.Main.music;
import static com.honker.main.Main.musicPaused;
import static com.honker.main.Main.progress;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.ArrayList;
import java.util.Random;
import static com.honker.main.Main.ready;
import static com.honker.main.Operations.sendProgress;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class TrackScheduler extends AudioEventAdapter {
    
    public final AudioPlayer player;
    public ArrayList<AudioTrack> queue = new ArrayList<AudioTrack>();;
    
    private AudioTrack currentTrack;
    
    public boolean looping = false, random = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }
    
    public void resume(){
        stop();
        musicPaused = false;
        player.setPaused(false);
    }
    
    public void stop(){
        musicPaused = true;
        bot.client.changeStatus(Status.empty());
        player.stopTrack();
    }
    
    public boolean playNoMessage(AudioTrack track){
        if(progress != null) {
            try {
                progress.delete();
            } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
                ex.printStackTrace();
            }
        }
        progress = null;
        
        resume();
        
        if(new File(track.getIdentifier()).exists()){
            currentTrack = track.makeClone();

            try{
                player.startTrack(currentTrack, false);
            } catch(Exception e){
                stop();
                return false;
            }
            
            String trackName = getTrackName(currentTrack);
            if(trackName == null) {
                trackName = "None";
            }
            bot.client.changeStatus(Status.game(trackName));
            
            return true;
        } else{
            try{
                music.remove(music.indexOf(new File(track.getIdentifier())));
                queue.remove(queue.indexOf(track));
            } catch(Exception e){}
            
            return false;
        }
    }
    
    public boolean play(AudioTrack track){
        boolean bool = playNoMessage(track);
        
        sendProgress(mainChannel);
        
        return bool;
    }
    
    public void setVolume(int volume){
        player.setVolume(volume);
    }
    
    public void setTrackTime(int time){
        currentTrack.setPosition(time * 1000);
        updateTrack();
    }
    
    public String getTrackName(AudioTrack track){
        if(track != null){
            String fileName = track.getIdentifier().split(File.separator.replace("\\", "\\\\"))
                              [track.getIdentifier().split(File.separator.replace("\\", "\\\\")).length - 1];
            String songName = fileName.substring(0, fileName.length() - 4);
            return songName;
        }
        return null;
    }
    
    public String getShortenedTrackInfo(AudioTrack track){
        String trackName = getTrackName(track);
        return "Track name: " + trackName + "; ID: " + getTrackID(track.makeClone());
    }
    
    public String getTrackInfo(AudioTrack track){
        String trackName = getTrackName(track);
        return "Track name: " + trackName + System.lineSeparator() + "Duration: " + (track.getDuration() / 1000) + " seconds" + System.lineSeparator() + "ID: " + getTrackID(track);
    }
    
    public int getTrackID(AudioTrack track){
        ArrayList<File> loadedFiles = new ArrayList<File>();
        
        for(AudioTrack addedTrack : queue)
            loadedFiles.add(new File(addedTrack.getIdentifier()));
        
        return loadedFiles.indexOf(new File(track.getIdentifier()));
    }
    
    public void sendCurrentPlayingTrack(){
        sendCurrentPlayingTrack(mainChannel);
    }
    
    public void sendCurrentPlayingTrack(IChannel chan){
        try {
            if(getCurrentTrack() != null){
                sendProgress(chan);
            } else
                chan.sendMessage("No track is playing right now");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void shuffleMusic() {
        sortMusic();
        Collections.shuffle(music);
    }
    
    public void sortMusic() {
        HashSet<File> newMusic = new HashSet<File>(music);
        music = new ArrayList<File>(newMusic);
        Collections.sort(music);
    }
    
    public void shufflePlaylist(){
        sortPlaylist();
        Collections.shuffle(queue);
    }
    
    public void sortPlaylist(){
        HashSet<AudioTrack> newQueue = new HashSet<AudioTrack>(queue);
        queue = new ArrayList<AudioTrack>(newQueue);
        Collections.sort(queue, (AudioTrack a, AudioTrack b) -> a.getIdentifier().compareTo(b.getIdentifier()));
    }
    
    public AudioTrack getRandomTrack(){
        if(queue.size() > 0){
            AudioTrack track;
            while(true){
                track = queue.get(new Random().nextInt(queue.size()));
                if(!track.equals(currentTrack))
                    return track;
            }
        } else
            return null;
    }
    
    public void clearQueue() {
        stop();
        queue.clear();
        try {
            progress.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void queue(AudioTrack track) {
        queue.add(track);
    }

    public void queue(String url) {
        load(url);
    }
    
    public void queue(File file) {
        load(file.getAbsolutePath());
    }
    
    public void setCurrentTrack(AudioTrack track){
        currentTrack = track;
    }
    
    public AudioTrack getCurrentTrack(){
        if(currentTrack != null)
            return currentTrack.makeClone();
        return null;
    }
    
    public AudioTrack playRandomTrack(){
        AudioTrack track = getRandomTrack();
        if(track != null){
            try {
                play(track);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return track;
        } else
            return null;
    }
    
    public void nextTrack() {
        int index = getTrackID(currentTrack);
        if(index < queue.size() - 1)
            play(queue.get(index + 1));
        else{
            shufflePlaylist();
            play(queue.get(0));
        }
    }
    
    public void previousTrack() {
        int index = getTrackID(currentTrack);
        if(index > 0)
            play(queue.get(index - 1));
        else{
            shufflePlaylist();
            play(queue.get(queue.size() - 1));
        }
    }
    
    public void updateTrack() {
        if(!ready || musicPaused || progress == null || currentTrack == null) {
            return;
        }
        
        int scale = 5;
        int max = 20;
        int dur = (int)currentTrack.getDuration() / 1000;
        int pos = (int)currentTrack.getPosition() / 1000;
        int per = 0;
        for(int a = 1; a < max; a++) {
            if(pos >= dur - scale * 2) {
                per = max;
                break;
            }
            
            int dur1 = (int)(dur / max * a);
            int dur2 = (int)(dur / max * (a + 1));
            if(pos >= dur1 && pos <= dur2) {
                per = a;
                break;
            }
        }
        
        String msg = getTrackInfo(getCurrentTrack()) + "\n\nTrack progress:\n";
        for(int a = 0; a < per; a++) {
            msg += "#";
        }
        
        if(per * scale == 0) {
            msg += "1%";
        } else {
            msg += " " + (per * scale) + "%";
        }
        
        try {
            if(progress != null)
                progress.edit(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void rejoinMusicChannel(){
        try {
            mainChannel.sendMessage("Attempting to rejoin voice channel, wait!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        IVoiceChannel musicChannel = bot.client.getVoiceChannelByID(VOICE_CHANNEL_ID);
        try {
            musicChannel.leave();
            Thread.sleep(3000);
            musicChannel.join();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        try {
            Thread.sleep(1000);
            if(endReason.mayStartNext){
                if(looping){
                    mainChannel.sendMessage("Track ended, replaying it");
                    playNoMessage(currentTrack);
                } else if(random)
                    playRandomTrack();
                else
                    nextTrack();
            } else if(endReason == AudioTrackEndReason.FINISHED)
                stop();
            else if(endReason == AudioTrackEndReason.LOAD_FAILED){
                mainChannel.sendMessage("Hmmm, something went wrong with playing music, i'll rejoin!");
                rejoinMusicChannel();
                mainChannel.sendMessage("Ok, i'll play another track now.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs){
        try{
            mainChannel.sendMessage("Track got stuck, let's start another one!");
            nextTrack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
