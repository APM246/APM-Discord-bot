package me.name.bot;

/**
 * could make this into a private inner class
 */
public class Player 
{
    private long channelID;
    private String name;


    public Player(long channelID, String name)
    {
        this.channelID = channelID;
        this.name = name;
    }

    public long getchannelID()
    {
        return channelID;
    }

    public String getChannelName() 
    {
        return name;
    }
}