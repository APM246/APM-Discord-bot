package me.name;

import java.io.*;
import java.util.Properties;

public class Reader
{
    public static String retrieveBotToken() throws IOException
    {
        Properties properties = new Properties();
        InputStream inputStream = Reader.class.getClassLoader().getResourceAsStream("bot_token.config");
        properties.load(inputStream);
        return properties.getProperty("bot_token");
    }

    public static String[] retrieveBannedSubReddits() throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader("banned.txt"));
        String line = reader.readLine();
        reader.close();
        return line.split(",");
    }

    public static void main(String[] args) throws Exception
    {
        Reader.retrieveBotToken();
    }
}
