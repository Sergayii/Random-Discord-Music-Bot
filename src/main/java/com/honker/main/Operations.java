package com.honker.main;

import static com.honker.main.Main.bot;
import static com.honker.main.Main.musicManager;
import static com.honker.main.Main.users;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
//import java.util.ArrayList;
import java.util.List;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public abstract class Operations {
    
    public String[] marks = new String[]{"!", "#", "$", "%", "^", "&", "*", "(", ")", "+", "=", "-", "<", ">", ",", ".", ":", ";", "@", "?", "\n"};

//    private ArrayList<String> nouns = new ArrayList<String>();
//    private ArrayList<String> verbs = new ArrayList<String>();
//    private ArrayList<String> adjectives = new ArrayList<String>();
    
    public void sendFile(IChannel chan, String message, File file){
        try{
            chan.sendFile(message, file);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendFile(IChannel chan,  File file){
        try{
            chan.sendFile(file);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendFile(IChannel chan, String message, String path){
        try{
            chan.sendFile(message, false, Operations.class.getResourceAsStream(path), "Image.png");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendFile(IChannel chan, String path){
        try{
            chan.sendFile("", false, Operations.class.getResourceAsStream(path), "Image.png");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendMessage(IChannel chan, String msg){
        try{
            chan.sendMessage(msg);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendCommand(IChannel chan, String cmd){
        sendMessage(chan, "[COMMAND] " + cmd);
    }
    
    public void sendCommandDone(IChannel chan){
        sendCommand(chan, "Done!");
    }
    
    public void sendCommandInProgress(IChannel chan){
        sendCommand(chan, "Command in progress");
    }
    
    public void sendCommandFailed(IChannel chan, String cause){
        sendCommand(chan, "Command failed: " + cause);
    }
    
    public void sendCommandFailedLength(IChannel chan){
        sendCommandFailed(chan, "Don't play with arguments count!");
    }
    
//    public void addWords(String msg){
//        msg = msg.toLowerCase();
//        for(String str = " "; str.length() < 1000; str += " ")
//            msg = msg.replace(str, " ");
//        msg = msg.replace("  ", " ");
//        msg = msg.trim();
//        for(String mark : marks)
//            msg = msg.replace(mark, "");
//        String[] words = msg.split(" ");
//        
//        for(String word : words){
//            if(word.endsWith("ing") || word.endsWith("s")){
//                if(!verbs.contains(word))
//                    verbs.add(word);
//            } else if(word.endsWith("ful") || (word.endsWith("less") && !word.equals("less"))){
//                if(!adjectives.contains(word))
//                    adjectives.add(word);
//            } else if(!nouns.contains(word)){
//                if(!word.equals("are") && !word.equals("is"))
//                    nouns.add(word);
//            }
//        }
//    }
//    
//    public String generateBugurt(){
//        String bugurt = new String();
//        int bugurtLength = new Random().nextInt(10) + 2;
//        for(int a = 0; a < bugurtLength; a++){
//            int lineLength = new Random().nextInt(10) + 1;
//            int oldChoice;
//            int choice = 0;
//            String bugurtLine = new String();
//            for(int b = 0; b < lineLength; b++){
//                if(b > 0)
//                    oldChoice = choice;
//                else
//                    oldChoice = 0;
//                choice = new Random().nextInt(3) + 1;
//                if(choice == 1){
//                    if(nouns.size() >= 1){
//                        choice = new Random().nextInt(nouns.size());
//                        String noun = nouns.get(choice);
//                        if(noun != null)
//                            bugurtLine += noun.toUpperCase() + " ";
//                    }
//                } else if(choice == 2){
//                    if(verbs.size() >= 1){
//                        choice = new Random().nextInt(verbs.size());
//                        String verb = verbs.get(choice);
//                        if(verb != null){
//                            if(bugurtLine.equals(""))
//                                bugurtLine += verb.toUpperCase() + " ";
//                            else if(oldChoice == choice)
//                                bugurtLine += "AND " + verb.toUpperCase() + " ";
//                            else
//                                bugurtLine += "IS " + verb.toUpperCase() + " ";
//                        }
//                    }
//                } else if(choice == 3){
//                    if(adjectives.size() >= 1){
//                        choice = new Random().nextInt(adjectives.size());
//                        String adjective = adjectives.get(choice);
//                        if(adjective != null){
//                            if(b == 0)
//                                bugurtLine += adjective.toUpperCase() + " ";
//                            else if(oldChoice == choice)
//                                bugurtLine += "AND " + adjective.toUpperCase() + " ";
//                            else
//                                bugurtLine += "IS " + adjective.toUpperCase() + " ";
//                        }
//                    }
//                }
//            }
//            if(!bugurtLine.equals("")){
//                bugurt += bugurtLine;
//                if(a < bugurtLength - 1)
//                    bugurt += "\n@\n";
//            }
//        }
//        if(bugurt.endsWith("\n@\n"))
//            bugurt = bugurt.substring(0, bugurt.length() - 4);
//        return bugurt;
//    }
//    
//    public void rollDice(IChannel chan, int d){
//        int playerNumber = new Random().nextInt(d) + 1;
//        int botNumber = new Random().nextInt(d) + 1;
//        sendCommand(chan, "You rolled **" + playerNumber + "**, I rolled **" + botNumber + "**");
//        
//        if(playerNumber > botNumber)
//            sendCommand(chan, "You won!");
//        else if(playerNumber < botNumber)
//            sendCommand(chan, "I won!");
//        else if(playerNumber == botNumber)
//            sendCommand(chan, "Draw!");
//        else
//            sendCommand(chan, "I don't know who even won...");
//    }
    
    public void sendTracksList(IChannel chan){
        try {
            File file = File.createTempFile("musicList", ".txt");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            
            for(AudioTrack track : musicManager.scheduler.queue){
                writer.write(musicManager.scheduler.getShortenedTrackInfo(track));
                writer.newLine();
                writer.flush();
            }
            
            sendFile(chan, "There's just too much tracks, i'll send the list in this file", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendHelp(IChannel chan){
        sendMessage(chan,
                    "WHAT MEANS WHAT:\n```\n"
                    + "<arg> - what you must enter\n"
                    + "[arg] - what is optional to enter\n\n"
                    + "```\n\n"
                    + "MAIN COMMANDS:\n```\n"
                    + "!help - shows help\n"
                    + "!track - sends current track name and ID\n"
                    + "!list - sends tracks list\n"
                    + "!search <sentence> - search for <sentence> in tracks list\n"
                    + "```\n\n"
                    + "ADMIN COMMANDS:\n```\n"
                    + "!bot <command> [value] - used for controlling bot remotely\n"
                    + "\trestart - restarts bot (but no changes to code get applied)\n"
                    + "\tshutdown - shuts bot down\n"
                    + "!music <command> [value] - used for controlling music\n"
                    + "\tstop - stops music if it's not stopped already\n"
                    + "\tresume - resumes music if it got stopped for some reason\n"
                    + "\tplay - plays a random track from the playlist\n"
                    + "\tnext - plays next track from the list\n"
                    + "\tprevious - plays previous track from the list\n"
                    + "\trejoin - rejoins to music voice channel\n"
                    + "\treplay - replays current track\n"
                    + "\tshuffle - shuffles playlist\n"
                    + "\tsort - sorts playlist\n"
                    + "\tplay <id> - plays a track from playlist with ID of <id>\n"
                    + "\twind <time> - winds/rewinds current track to <time> seconds\n"
                    + "\tvolume <vol> - sets music volume to <vol>\n"
                    + "\tloop <true/false> - sets looping to <true/false>\n"
                    + "!files <command> [value] - used for controlling files\n"
                    + "\tload - loads any new files\n"
                    + "\tunload - unloads all the files\n"
                    + "\treload - reloads all the files\n"
                    + "```");
    }
    
    public UserVar findUser(IUser user){
        for(UserVar userVar : users){
            if(userVar.user.equals(user))
                return userVar;
        }
        return null;
    }
    
    public void updateUsers(){
        List<IUser> usersList = bot.client.getUsers();
        
        int a = 0;
        for(IUser user : usersList){
            if(!user.equals(bot.user))
                users[a] = new UserVar(user);
            a += 1;
        }
    }
}
