package me.name;

import java.io.InputStream;
import java.util.regex.Pattern;

import org.json.*;

public class NewsUpdates
{
    private final String client_secret = System.getenv("nytimes_client_secret");

    private String getJSON() throws Exception
    {
        //String command1 = "curl https://api.nytimes.com/svc/mostpopular/v2/viewed/1.json?api-key=";
        String command = "curl https://api.nytimes.com/svc/topstories/v2/world.json?api-key=" + client_secret;
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        byte[] bytes = new byte[500000];
        int letter;
        int i = 0;

        while ((letter = inputStream.read()) != -1)
        {
            bytes[i] = (byte) letter;
            i++;
        }

        String joke = new String(bytes, 0, i);

        return joke;
    }

    private String remove_backslahes(String url)
    {
        String[] parts = url.split(Pattern.quote("\\"));
        int length = parts.length;
        url = "";

        for (int i = 0; i < length; i++)
        {
            url += parts[i];
        }

        return url;
    }


    public String retrieveURL(boolean isRandom) throws Exception
    {
        String json_string = getJSON();
        JSONObject jsonObject = new JSONObject(json_string);
        JSONArray jsonArray = jsonObject.getJSONArray("results");

        JSONObject jsonObject1;

        if (isRandom)
        {
            int random = (int) (jsonArray.length()*Math.random());
            jsonObject1 = jsonArray.getJSONObject(random);

        }
        else
        {
            jsonObject1 = jsonArray.getJSONObject(0); // gets top article
        }

        String url = jsonObject1.getString("url");

        return remove_backslahes(url);
    }
    
    public static void main(String[] args)
    {
        try
        {
            System.out.println(new NewsUpdates().retrieveURL(true));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
