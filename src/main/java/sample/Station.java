package sample;

import javafx.scene.image.Image;

import java.util.Objects;

public class Station {
    private String name, streamUrl;
    private Image img;

    public Station(String name, String streamUrl, String imgUrl) {
        this.name = name;
        this.streamUrl = streamUrl;
        try{
            this.img = new Image(imgUrl, 140, 120, true, true);
        }catch (Exception e){
            this.img = null;
        }
    }

    public String getName() {
        return name;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public Image getImg() {
        return img;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Objects.equals(name, station.name) &&
                Objects.equals(streamUrl, station.streamUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, streamUrl);
    }
}
