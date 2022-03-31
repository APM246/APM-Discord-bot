package me.name.bot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import org.json.*;

/**
 * 
 */
public class StatsRecorder {

    private String json_text;
    private HashMap<String,Integer> commandMap;

    public StatsRecorder() throws Exception {
        commandMap = new HashMap<>();
        commandMap.put("raven", 0);
        commandMap.put("game", 1);
        commandMap.put("guess", 2);
        commandMap.put("photo", 3);
        commandMap.put("bdsm", 4);
        commandMap.put("help", 5);
        commandMap.put("update", 6);
        commandMap.put("comment",7);
        commandMap.put("gif",8);
        commandMap.put("stat",9);
        commandMap.put("search",10);
        commandMap.put("joke",10);
        commandMap.put("news",11);
        commandMap.put("about",12);
        commandMap.put("play",13);
        commandMap.put("skip",14);
        commandMap.put("stop",15);
        commandMap.put("post", 16);
    }

    private void readJSON() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("stats.json"));
        String line = reader.readLine();
        json_text = "";
        while (line != null) {
            json_text += line;
            line = reader.readLine();
        }
        reader.close();
    }

    public void incrementCount(String command) throws Exception {
        readJSON();
        JSONObject jsonObject = new JSONObject(json_text);
        JSONArray jsonArray = jsonObject.getJSONArray("numCalls");
        int index = commandMap.get(command);
        JSONObject stat = jsonArray.getJSONObject(index);
        stat.increment("count");
        jsonArray.put(index, stat);
        jsonObject.put("numCalls", jsonArray);
        String newJSON = jsonObject.toString();
        write(newJSON);
    }

    private void write(String JSON) throws Exception {
        PrintWriter writer = new PrintWriter("stats.json");
        writer.write(JSON);
        writer.close();
    }

    public String getCount(String command) throws Exception {
        readJSON();
        JSONObject jsonObject = new JSONObject(json_text);
        JSONArray jsonArray = jsonObject.getJSONArray("numCalls");
        int index = commandMap.get(command);
        JSONObject stat = jsonArray.getJSONObject(index);
        return String.valueOf(stat.getInt("count"));
    }

    public static void main(String[] args) throws Exception
    {
        StatsRecorder statRecorder = new StatsRecorder();
        statRecorder.incrementCount("raven");
        System.out.println(statRecorder.getCount("raven"));
    }

}