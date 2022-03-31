package me.name.music;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;


import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class Music
{
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final String client_secret = System.getenv("youtube_client_secret");

    public Music()
    {
        musicManagers = new HashMap<>();
        playerManager = new DefaultAudioPlayerManager();
        playerManager.setPlayerCleanupThreshold(100000);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadAndPlay(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Cannot find:  " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }

    public void kickBot() 
    {
        playerManager.shutdown();
    }

    private String searchTermtoID(String searchTerm) throws IOException
    {
        searchTerm = searchTerm.replaceAll(" ", "%20");
        String command =
                "curl \"https://www.googleapis.com/youtube/v3/search?part=id&order=relevance&q=" + searchTerm +
                        "&type=video&key=" + client_secret + "\"";    

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        byte[] bytes = new byte[50000];
        int letter;
        int i = 0;

        while ((letter = inputStream.read()) != -1)
        {
            bytes[i] = (byte) letter;
            i++;
        }

        String json_text = new String(bytes, 0, i);
        //System.out.println(json_text); return "";

        JSONObject original_jsonObject = new JSONObject(json_text);
        JSONArray jsonArray = original_jsonObject.getJSONArray("items");
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        JSONObject video = jsonObject.getJSONObject("id");
        String videoID = video.getString("videoId");

        return videoID;
    }

    public String searchTermtoURL(String searchTerm) throws IOException
    {
        return "https://www.youtube.com/watch?v=" + searchTermtoID(searchTerm);
    }

    public static void main(String[] args)
    {
        try
        {
            System.out.println(new Music().searchTermtoURL("Yamborghini High"));
            System.out.println(new Music().searchTermtoURL("Forget you"));
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

