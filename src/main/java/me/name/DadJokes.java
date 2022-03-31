package me.name;

import java.io.IOException;
import java.io.InputStream;

public class DadJokes
{
    public static String generateDadJoke() throws IOException
    {
        String command = "curl https://icanhazdadjoke.com/";
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        byte[] bytes = new byte[250];
        int length = inputStream.read(bytes);
        String joke = new String(bytes, 0 , length);
        process.destroy();

        return joke;
    }

    public static void main(String[] args) throws IOException
    {
        System.out.println(generateDadJoke());
    }
}
