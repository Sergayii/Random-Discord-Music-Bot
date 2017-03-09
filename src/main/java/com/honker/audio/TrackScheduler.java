package com.honker.audio;

import static com.honker.main.Main.VOICE_CHANNEL_ID;
import static com.honker.main.Main.bot;
import static com.honker.main.Main.mainChannel;
import static com.honker.main.Main.music;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.ArrayList;
import java.util.Random;
import static com.honker.main.Main.musicPaused;
import java.io.File;
import java.util.Collections;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;

public class TrackScheduler extends AudioEventAdapter {
    
    public final AudioPlayer player;
    public ArrayList<AudioTrack> queue;
    
    private AudioTrack currentTrack;
    
    public boolean looping = false, random = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        queue = new ArrayList<AudioTrack>();
    }

    public void resume(){
        stop();
        musicPaused = false;
        player.setPaused(false);
    }
    
    public void stop(){
        musicPaused = true;
        player.stopTrack();
    }
    
    public boolean playNoMessage(AudioTrack track){
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
        
        sendCurrentPlayingTrack();
        
        return bool;
    }
    
    public void setVolume(int volume){
        player.setVolume(volume);
    }
    
    public void setTrackTime(int time){
        currentTrack.setPosition(time * 1000);
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
        return "```\nTrack name: " + trackName + System.lineSeparator() + "Duration: " + (track.getDuration() / 1000) + " seconds" + System.lineSeparator() + "ID: " + getTrackID(track.makeClone()) + "\n```";
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
                chan.sendMessage("Current track:\n" + getTrackInfo(currentTrack));
            } else
                chan.sendMessage("No track is playing right now");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void sortPlaylist(){
        Collections.sort(music);
        Collections.sort(queue, (AudioTrack a, AudioTrack b) -> a.getIdentifier().compareTo(b.getIdentifier()));
    }
    
    public void shufflePlaylist(){
        long seed = new Random().nextLong();
        Collections.shuffle(music, new Random(seed));
        Collections.shuffle(queue, new Random(seed));
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
    
    public void queue(AudioTrack track) {
        queue.add(track);
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
