package sample;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.net.URL;
import java.net.URLConnection;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;

/**
 * Using Javazoom (JLayer) media player to play stream.
 * Currently supporting only simple media files streaming (mp3, etc.).
 */
public class RadioPlayer implements IPlayer {
    private Player player;

    /**
     * Play a stream. The player runs in a different thread.
     * @param url Stream to play.
     */
    public void play(String url) {
        pause();
        try {
            Thread runningThread = new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            });

            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.connect();

            player = new Player(urlConnection.getInputStream());
            runningThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Pause current playing.
     */
    public void pause() {
        if (player != null)
            try {
                player.close();
            } catch (Exception ignored) {
            }
    }

    /**
     * Sets the volume to a value between 0 to 100. Sets the volume for both speaker and headphones.
     * @param volume The desirable volume level.
     */
    public void setVolume(double volume) {
        if(volume<0 || volume>100)
            return;
        setPortVolume(Port.Info.SPEAKER, volume);
        setPortVolume(Port.Info.HEADPHONE, volume);
    }

    /**
     * Sets the volume of a device @info to @volume level.
     * @param info Device to set it's volume.
     * @param volume Desirable level of volume.
     */
    private void setPortVolume(Info info, double volume) {
        if (AudioSystem.isLineSupported(info)) {
            try {
                Port outline = (Port) AudioSystem.getLine(info);
                outline.open();
                FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue((float) volume / 100);
            } catch (LineUnavailableException ex) {
                System.err.println("source not supported");
                ex.printStackTrace();
            }
        }
    }
}