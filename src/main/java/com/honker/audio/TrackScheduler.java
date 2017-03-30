package com.honker.audio;

import static com.honker.main.Main.main;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.ArrayList;
import java.util.Random;

import static com.honker.main.Operations.sendProgress;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import static com.honker.main.Operations.sendMessage;

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
        main.musicPaused = false;
        updateStatus();
    }
    
    public void pause() {
        player.setPaused(true);
        main.musicPaused = true;
        updateStatus();
    }

    public void stop() {
        player.stopTrack();
        currentTrack = null;
        main.musicPaused = true;
        updateStatus();
    }

    public void updateStatus() {
        String trackName = "";
        if(currentTrack == null || queue.isEmpty()) {
            trackName = "nothing";
        } else {
            if(main.musicPaused || player.isPaused()) {
                trackName = "(PAUSED) ";
            }
            trackName += getTrackName(currentTrack);
        }

        main.bot.client.changeStatus(Status.game(trackName));
    }
    
    public boolean playNoMessage(AudioTrack track) {
        if(main.progress != null) {
            try {
                main.progress.delete();
            } catch(MissingPermissionsException | RateLimitException | DiscordException ex) {
                ex.printStackTrace();
            }
        }
        main.progress = null;

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
                main.music.remove(main.music.indexOf(new File(track.getIdentifier())));
                queue.remove(queue.indexOf(track));
            } catch(Exception e) {}

            return false;
        }
    }

    public boolean play(AudioTrack track) {
        boolean bool = playNoMessage(track);

        sendProgress();

        return bool;
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

    public String getShortenedTrackInfo(AudioTrack track) {
        String trackName = getTrackName(track);
        return "Track name: " + trackName + "; ID: " + getTrackID(track.makeClone());
    }

    public String getTrackInfo(AudioTrack track) {
        String trackName = getTrackName(track);
        return "Track name: " + trackName + System.lineSeparator() +
               "File path: `" + new File(track.getIdentifier()).getAbsolutePath().replace("\\", "/") + "`" + System.lineSeparator() +
               "Duration: " + (track.getDuration() / 1000) + " seconds" + System.lineSeparator() +
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
                sendProgress();
            } else {
                sendMessage("No track is playing right now");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void shuffleMusic() {
        sortMusic();
        Collections.shuffle(main.music);
    }

    public void sortMusic() {
        HashSet<File> newMusic = new HashSet<File>(main.music);
        main.music = new ArrayList<File>(newMusic);
        Collections.sort(main.music);
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
            main.progress.delete();
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
        if(!main.ready || main.musicPaused || main.progress == null || currentTrack == null) {
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
            if(main.progress != null) {
                main.progress.edit(msg);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void joinMusicChannel() throws MissingPermissionsException {
        IVoiceChannel musicChannel = main.bot.client.getVoiceChannelByID(main.VOICE_CHANNEL_ID);
        musicChannel.join();
    }
    
    public void leaveMusicChannel() {
        IVoiceChannel musicChannel = main.bot.client.getVoiceChannelByID(main.VOICE_CHANNEL_ID);
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
                    sendMessage("Track ended, replaying it");
                    playNoMessage(currentTrack);
                } else if(random) {
                    playRandomTrack();
                } else {
                    nextTrack();
                }
            } else if(endReason == AudioTrackEndReason.FINISHED) {
                stop();
            } else if(endReason == AudioTrackEndReason.LOAD_FAILED) {
                sendMessage("Hmmm, something went wrong with playing music, i'll rejoin!");
                rejoinMusicChannel();
                sendMessage("Ok, i'll play another track now.");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        try {
            sendMessage("Track got stuck, let's start another one!");
            nextTrack();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
