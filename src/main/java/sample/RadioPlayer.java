package sample;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.net.URL;
import java.net.URLConnection;

public class RadioPlayer implements IPlayer{
    private Player player;

    public void play(String url){
        pause();
        try {
            Thread runningThread = new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            });

            // Connection
            URLConnection urlConnection = new URL(url).openConnection();

            // Connecting
            urlConnection.connect();

            // Playing
            player = new Player(urlConnection.getInputStream());
            runningThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (player != null)
            try {
                player.close();
            }catch (Exception ignored){}
    }
}