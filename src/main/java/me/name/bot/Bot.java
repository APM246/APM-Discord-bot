package me.name.bot;

import me.name.Reader;
import me.name.NewsUpdates;
import me.name.exceptions.InvalidPeriodException;
import me.name.exceptions.InvalidSorterException;
import me.name.exceptions.SubredditDoesNotExistException;
import me.name.music.Music;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nonnull;
import me.name.Reddit;

public class Bot extends ListenerAdapter
{
    private CommandsManager commandsManager;
    private MessageChannel error_channel;
    private boolean isJDASet;

    public Bot() throws Exception
    {
        commandsManager = new CommandsManager(new Reddit(), new NewsUpdates(), new Music());
        isJDASet = false;
    }

    public static void main(String[] args) throws Exception
    {
        JDABuilder.createLight(Reader.retrieveBotToken(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS).
        addEventListeners(new Bot()).setActivity(Activity.playing("type >help")).build();      
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event)
    {
        MessageChannel channel = event.getChannel();

        try 
        {
            if (!isJDASet) {
                error_channel = event.getJDA().getTextChannelById("788047889770217504");
                isJDASet = true;
            }
            if (event.getAuthor().isBot()) return; // don't respond to bots (including self)
            Message message = event.getMessage();
            if (message.getAuthor().isBot() || message.getType() != MessageType.DEFAULT) return;
            String content = message.getContentRaw();
            if (content.length() == 0) return;
            String[] args = content.split(" ", 2);
 
            if (args[0].charAt(0) == '>') commandsManager.executeCommand(channel, args, event);
            else if (commandsManager.shouldMeme(content)) commandsManager.memeify(content, channel);
        }

        catch (SubredditDoesNotExistException e)
        {      
            String[] error_messages = {"Horrible spelling.", "Awful spelling.", "Unsatisfactory spelling."};
            int random_number = (int) (Math.random()*error_messages.length);
            channel.sendMessage(event.getAuthor().getAsMention() + " " + error_messages[random_number]).queue(); 
        }

        catch (InvalidSorterException e) 
        {
            channel.sendMessage("You did not provide a valid sorter. The available options are top, best, new," +
            " controversial, rising and hot (default).").queue();
        }

        catch (InvalidPeriodException e)
        {
            channel.sendMessage("You did not provide a valid time period. The available options are all, day, hour," +
             " month, year and week").queue();
        }

        catch (Exception e)
        {
            if (e.getMessage() != null) error_channel.sendMessage(e.getMessage()).queue();
            else error_channel.sendMessage("Unknown error").queue();
        }
    }
}