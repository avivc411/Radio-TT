package sample;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    //a list of all supported stations
    private List<Station> stations = new LinkedList<>();
    private static List<Station> favoriteStations = new LinkedList<>();
    private Dictionary<String, String> stringProperties = new Hashtable<>();
    private boolean playing = false;
    private Station currentStation;
    private IPlayer radioPlayer = new RadioPlayer();

    @FXML
    private Shape playIcon, pauseIcon1, pauseIcon2;
    @FXML
    public Rectangle pauseRectangle;
    @FXML
    private ImageView coverImg;
    @FXML
    private Slider volumeSlider;
    @FXML
    private TableView presetsView = new TableView();
    @FXML
    private Button presetsBtn;
    @FXML
    private Button favoritesBtn;

    /**
     * building the presets and favorites lists and the string properties map.
     */
    public Controller() {
        String basePath = new File("").getAbsolutePath(), pathToProperties, pathToStationsCSV;
        pathToStationsCSV = basePath.concat("\\src\\main\\resources\\stations info.csv");
        pathToProperties = basePath.concat("\\src\\main\\resources\\String Properties.csv");
        buildStationsList(getCSVFileLines(pathToStationsCSV));
        loadProperties(getCSVFileLines(pathToProperties));
        loadFavorites();
    }

    /**
     * set the relevant strings to visual elements.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        presetsBtn.textProperty().setValue(stringProperties.get("presets"));
        favoritesBtn.textProperty().setValue(stringProperties.get("favorites"));
        showPresets();
    }

    /**
     * extract all CSV file's lines.
     *
     * @param location The location of the file to read.
     * @return a list with all the file's lines.
     */
    private List<String> getCSVFileLines(String location) {
        try {
            File stationsFile = new File(location);
            FileReader fileReader = new FileReader(stationsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new LinkedList<>();
            bufferedReader.readLine();
            String line = bufferedReader.readLine();
            while (line != null) {
                if (line.contains("\n"))
                    line = line.substring(0, line.indexOf('\n'));
                lines.add(line);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            fileReader.close();
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * read stations details from csv lines and populate the stations list
     *
     * @param lines line - name, url, logo
     */
    private void buildStationsList(List<String> lines) {
        if (lines == null || lines.size() == 0)
            return;
        for (String line : lines) {
            String[] separatedLine = line.split(",");
            String name, url, img = "";
            name = separatedLine[0];
            url = separatedLine[1];
            if (separatedLine.length > 2)
                img = separatedLine[2];
            stations.add(new Station(name, url, img));
        }
    }

    /**
     * read all the string properties from a properties file.
     *
     * @param lines line - key in english, value in preferred language.
     */
    private void loadProperties(List<String> lines) {
        if (lines == null || lines.size() == 0)
            return;
        for (String line : lines) {
            String[] separatedLine = line.split(",");
            String englishKey, value;
            englishKey = separatedLine[0];
            value = separatedLine[1];
            stringProperties.put(englishKey, value);
        }
    }

    private void loadFavorites() {
        //TODO
    }

    /**
     * build the presets list in a table - image, name, star of is\isn't favorite
     * sets click listeners to start playing on image and name
     * sets click listeners to add\remove from favorites on the star
     */
    //TODO: Station s is not saved in the click events
    public void showPresets() {
        TableColumn<Station, Image> column1 = new TableColumn<>();
        column1.setPrefWidth(150);
        TableColumn<Station, String> column2 = new TableColumn<>(stringProperties.get("preset"));
        column2.setPrefWidth(140);
        TableColumn<Station, Polygon> column3 = new TableColumn<>(stringProperties.get("favorite"));
        column3.setPrefWidth(90);
        for (Station s1 : stations) {
            final Station starStation = new Station(s1.getName(), s1.getStreamUrl(), s1.getImg().impl_getUrl());
            Polygon star = new Polygon();

            if (favoriteStations.contains(s1))
                star.setFill(Color.YELLOW);
            else
                star.setFill(Color.WHITE);
            column3.setCellValueFactory(p -> new SimpleObjectProperty<>(star));

            star.setOnMouseClicked(event -> {
                if (favoriteStations.contains(starStation)) {
                    favoriteStations.remove(starStation);
                    star.setFill(Color.WHITE);
                } else {
                    favoriteStations.add(starStation);
                    star.setFill(Color.YELLOW);
                }
            });

            double shs = 5.0;
            //TODO: replace the star painting
            star.getPoints().addAll(new Double[]{0.0, shs * 3,
                    shs * 2, shs * 2,
                    shs * 3, 0.0,
                    shs * 4, shs * 2,
                    shs * 6, shs * 3,
                    shs * 4, shs * 4,
                    shs * 3, shs * 6,
                    shs * 2, shs * 4});

            column1.setCellValueFactory(p -> new SimpleObjectProperty(p.getValue().getImg()));
            column1.setCellFactory(tc -> {
                TableCell<Station, Image> cell = new TableCell<Station, Image>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : new ImageView(item));
                    }
                };
                cell.setOnMouseClicked(e -> {
                    if (!cell.isEmpty()) {
                        play(starStation);
                    }
                });
                return cell;
            });

            column2.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getName()));
            column2.setCellFactory(tc -> {
                TableCell<Station, String> cell = new TableCell<Station, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                    }
                };
                cell.setOnMouseClicked(e -> {
                    if (!cell.isEmpty()) {
                        play(starStation);
                    }
                });
                return cell;
            });
        }

        ObservableList<Station> observableStationsList = FXCollections.observableArrayList(stations);

        if (observableStationsList.size() >= 0) {
            presetsView.setItems(observableStationsList);
            presetsView.getColumns().setAll(column1, column2, column3);
        }
    }

    public void showFavorites() {
        //TODO
    }

    public void playPause() {
        if (currentStation == null)
            return;
        if (playing)
            pause();
        else play(currentStation);
    }

    /**
     * pause the streaming, make the "pause" icon and the pausing rectangle invisible
     * and the "play" icon visible.
     */
    public void pause() {
        playing = false;
        playIcon.setVisible(true);
        pauseIcon1.setVisible(false);
        pauseIcon2.setVisible(false);
        pauseRectangle.setVisible(false);
        radioPlayer.pause();
    }

    /**
     * start a streaming, make the "pause" icon and the pausing rectangle visible
     * and the "play" icon invisible.
     */
    public void play(Station s) {
        if (s == null)
            return;
        playing = true;
        pauseIcon1.setVisible(true);
        pauseIcon2.setVisible(true);
        pauseRectangle.setVisible(true);
        playIcon.setVisible(false);
        coverImg.setImage(s.getImg());
        currentStation = s;
        radioPlayer.play(s.getStreamUrl());
    }

    public void changeVolume() {
        //TODO
        double volume = volumeSlider.getValue();
    }

    public static void saveFavorites() {
        try {
            System.out.println(Controller.class.getCanonicalName());
            File file = new File(Controller.class.getCanonicalName());
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("station name,stream url,image url");
            bufferedWriter.newLine();
            for (Station s : favoriteStations) {
                bufferedWriter.write(s.getName() + "," + s.getStreamUrl() + "," + s.getImg().impl_getUrl());
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
