package com.honker.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Random;

import java.io.File;

import java.util.Collections;
import java.util.HashSet;

import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import static com.honker.main.Main.main;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public ArrayList<AudioTrack> queue = new ArrayList<AudioTrack>();
    
    private AudioTrack currentTrack;

    public boolean looping = false, random = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public void resume() {
        player.setPaused(false);
        updateStatus();
    }
    
    public void pause() {
        player.setPaused(true);
        updateStatus();
    }

    public void stop() {
        player.stopTrack();
        currentTrack = null;
        updateStatus();
    }

    public void updateStatus() {
        String trackName = "";
        if(currentTrack == null || queue.isEmpty()) {
            trackName = "nothing";
        } else {
            if(player.isPaused()) {
                trackName = "\u25ae\u25ae ";
            }
            trackName += getTrackName(currentTrack);
        }

        main.getBot().getClient().changeStatus(Status.game(trackName));
    }
    
    public boolean playNoMessage(AudioTrack track) {
        if(main.getProgress() != null) {
            try {
                main.getProgress().delete();
            } catch(MissingPermissionsException | RateLimitException | DiscordException ex) {
                ex.printStackTrace();
            }
        }
        main.setProgress(null);

        resume();

        if(new File(track.getIdentifier()).exists()) {
            currentTrack = track.makeClone();

            try {
                player.startTrack(currentTrack, false);
            } catch(Exception e) {
                stop();
                return false;
            }

            updateStatus();

            return true;
        } else {
            try {
                main.getMusic().remove(main.getMusic().indexOf(new File(track.getIdentifier())));
                queue.remove(queue.indexOf(track));
            } catch(Exception e) {}

            return false;
        }
    }

    public boolean play(AudioTrack track) {
        boolean bool = playNoMessage(track);

        main.getBot().sendProgress();

        return bool;
    }

    public int getVolume() {
        return player.getVolume();
    }
    
    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public void setTrackTime(int time) {
        currentTrack.setPosition(time * 1000);
        updateTrack();
    }

    public String getTrackName(AudioTrack track) {
        if(track != null) {
            String fileName = track.getIdentifier().split(File.separator.replace("\\", "\\\\"))[track.getIdentifier().split(File.separator.replace("\\", "\\\\")).length - 1];
            String songName = fileName.substring(0, fileName.length() - 4);
            return songName;
        }
        return null;
    }

    public float getTrackPosition(AudioTrack track) {
        return track.getPosition() / 1000;
    }
    
    public String getTrackDurationString(AudioTrack track) {
        float duration = getTrackDuration(track), seconds = duration, minutes, hours;
        minutes = duration / 60;
        hours = duration / 3600;
        if(hours < 1) {
            minutes = (float)Math.floor(minutes);
            for(int a = 0; a < minutes; a++) {
                seconds -= 60;
            }
            return (int)minutes + " minutes " + (int)seconds + " seconds";
        } else if(minutes < 1) {
            return (int)seconds + " seconds";
        }
        
        hours = (float)Math.floor(hours);
        minutes = (float)Math.floor(minutes);
        for(int a = 0; a < hours; a++) {
            minutes -= 60;
        }
        for(int a = 0; a < minutes; a++) {
            seconds -= 60;
        }
        return (int)hours + " hours " + (int)minutes + " minutes " + (int)seconds + " seconds";
    }
    
    public float getTrackDuration(AudioTrack track) {
        return track.getDuration() / 1000;
    }
    
    public String getShortenedTrackInfo(AudioTrack track) {
        String trackName = getTrackName(track);
        return "Track name: " + trackName + "; ID: " + getTrackID(track.makeClone());
    }

    public String getTrackInfo(AudioTrack track) {
        String trackName = getTrackName(track);
        return "Track name: " + trackName + System.lineSeparator() +
               "File path: `" + track.getIdentifier().replaceFirst("/", "").replace("\\", "/") + "`" + System.lineSeparator() +
               "Duration: " + getTrackDurationString(track) + " (" + (int)getTrackDuration(track) + " seconds)" +System.lineSeparator() +
               "Volume: " + getVolume() + "%" + System.lineSeparator() +
               "ID: " + getTrackID(track);
    }

    public int getTrackID(AudioTrack track) {
        ArrayList<File> loadedFiles = new ArrayList<File>();

        for(AudioTrack addedTrack : queue) {
            loadedFiles.add(new File(addedTrack.getIdentifier()));
        }

        return loadedFiles.indexOf(new File(track.getIdentifier()));
    }

    public void sendCurrentPlayingTrack() {
        try {
            if(getCurrentTrack() != null) {
                main.getBot().sendProgress();
            } else {
                main.getBot().sendMessage("No track is playing right now");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void shuffleMusic() {
        sortMusic();
        Collections.shuffle(main.getMusic());
    }

    public void sortMusic() {
        HashSet<File> newMusic = new HashSet<File>(main.getMusic());
        main.setMusic(new ArrayList<File>(newMusic));
        Collections.sort(main.getMusic());
    }

    public void shufflePlaylist() {
        Collections.shuffle(queue);
    }

    public void sortPlaylist() {
        Collections.sort(queue, (AudioTrack a, AudioTrack b) -> a.getIdentifier().compareTo(b.getIdentifier()));
    }

    public AudioTrack getRandomTrack() {
        if(queue.size() > 0) {
            AudioTrack track;
            while(true) {
                track = queue.get(new Random().nextInt(queue.size()));
                if(!track.equals(currentTrack)) {
                    return track;
                }
            }
        } else {
            return null;
        }
    }

    public void clearQueue() {
        stop();
        queue.clear();
        try {
            main.getProgress().delete();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void queue(AudioTrack track) {
        queue.add(track);
    }

    public void queue(String url) {
        main.load(url);
    }

    public void queue(File file) {
        main.load(file.getAbsolutePath());
    }

    public void setCurrentTrack(AudioTrack track) {
        currentTrack = track;
    }

    public AudioTrack getCurrentTrack() {
        if(currentTrack != null) {
            return currentTrack.makeClone();
        }
        return null;
    }

    public AudioTrack playRandomTrack() {
        AudioTrack track = getRandomTrack();
        if(track != null) {
            try {
                play(track);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            return track;
        } else {
            return null;
        }
    }

    public void nextTrack() {
        int index = getTrackID(currentTrack);
        if(index < queue.size() - 1) {
            play(queue.get(index + 1));
        } else {
            shufflePlaylist();
            play(queue.get(0));
        }
    }

    public void previousTrack() {
        int index = getTrackID(currentTrack);
        if(index > 0) {
            play(queue.get(index - 1));
        } else {
            shufflePlaylist();
            play(queue.get(queue.size() - 1));
        }
    }

    public void updateTrack() {
        if(!main.getBot().getClient().isReady() || main.getProgress() == null || currentTrack == null) {
            return;
        }

        // scale * max must always be equal to 100
        float scale = 5;
        float max = 20;
        
        float dur = getTrackDuration(currentTrack);
        float pos = getTrackPosition(currentTrack);
        float per = 0;
        for(int a = 1; a < max + 1; a++) {
            if(pos >= dur - scale) {
                per = max;
                break;
            }

            float dur1 = dur / max * a;
            float dur2 = dur / max * (a + 1);
            if(pos >= dur1 && pos <= dur2) {
                per = a;
                break;
            }
        }

        String msg = getTrackInfo(getCurrentTrack()) + "\n\nTrack progress:\n";
        
        for(int a = 0; a < per; a++) {
            msg += "\u25a0";
        }
        
        if(per * scale == 0) {
            msg += "1%";
        } else {
            msg += " " + (per * scale) + "%";
        }

        msg += " (" + pos + " seconds)";
        
        if(main.getProgress() != null) {
            try {
                main.getProgress().edit(msg);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void joinMusicChannel() throws MissingPermissionsException {
        IVoiceChannel musicChannel = main.getBot().getClient().getVoiceChannelByID(main.getVoiceChannelID());
        musicChannel.join();
    }
    
    public void leaveMusicChannel() {
        IVoiceChannel musicChannel = main.getBot().getClient().getVoiceChannelByID(main.getVoiceChannelID());
        musicChannel.leave();
    }
    
    public void rejoinMusicChannel() throws MissingPermissionsException {
        leaveMusicChannel();
        try {
            Thread.sleep(1000);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        joinMusicChannel();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        try {
            Thread.sleep(1000);
            if(endReason.mayStartNext) {
                if(looping) {
                    main.getBot().sendMessage("Track ended, replaying it");
                    playNoMessage(currentTrack);
                } else if(random) {
                    playRandomTrack();
                } else {
                    nextTrack();
                }
            } else if(endReason == AudioTrackEndReason.FINISHED) {
                stop();
            } else if(endReason == AudioTrackEndReason.LOAD_FAILED) {
                main.getBot().sendMessage("Hmmm, something went wrong with playing music, i'll rejoin!");
                rejoinMusicChannel();
                main.getBot().sendMessage("Ok, i'll play another track now.");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        try {
            main.getBot().sendMessage("Track got stuck, let's start another one!");
            nextTrack();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
