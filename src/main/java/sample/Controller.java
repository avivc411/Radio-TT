package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * TODO: eco99 stream not working
 * TODO: string properties are gibberish
 * TODO: string properties - add properties to change (menu)
 * TODO: add more stations
 * TODO: set volume option
 * TODO: add preferences+file
 */
public class Controller implements Initializable {
    private enum Language {ENGLISH, HEBREW}
    //a list of all supported stations
    private static final List<Station> stations = new LinkedList<>();
    private static final List<Station> favoriteStations = new LinkedList<>();
    private static final Dictionary<String, String> stringProperties = new Hashtable<>();

    //user's Documents\\Radio Touch-Touch\\Favorites.csv
    private static String pathToFavorites;
    private boolean playing = false;
    private Station currentStation = null;
    private IPlayer radioPlayer = new RadioPlayer();
    private Language currentLanguage = Language.ENGLISH;

    @FXML
    private Shape playIcon, pauseIcon1, pauseIcon2;
    @FXML
    public Rectangle pauseRectangle;
    @FXML
    private ImageView coverImg;
    @FXML
    private Slider volumeSlider;
    @FXML
    public GridPane stationsGrid;
    @FXML
    private Button stationsBtn, favoritesBtn;
    @FXML
    public MenuItem englishBtn, hebrewBtn;

    /**
     * Building the presets and favorites lists and the string properties map.
     */
    public Controller() {
        File favoritesFile;
        try {
            loadFromLines(getCSVFileLines("/ENGLISH String Properties.csv"), stringProperties);
            loadFromLines(getCSVFileLines("/Stations Info.csv"), stations);
            pathToFavorites = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() +
                    "/Radio Touch-Touch/Favorites.csv";
            favoritesFile = new File(pathToFavorites);
            if (favoritesFile.exists())
                loadFromLines(getCSVFileLines(favoritesFile.getAbsolutePath()), favoriteStations);
            else
                favoritesFile.getParentFile().mkdir();
        } catch (Exception e1) {
            try {
                loadFromLines(getCSVFileLines("\\ENGLISH String Properties.csv"), stringProperties);
                loadFromLines(getCSVFileLines("\\Stations Info.csv"), stations);
                pathToFavorites = FileSystemView.getFileSystemView().getDefaultDirectory() +
                        "\\Radio Touch-Touch\\Favorites.csv";
                favoritesFile = new File(pathToFavorites + "\\Radio Touch-Touch\\Favorites.csv");
                if (favoritesFile.exists())
                    loadFromLines(getCSVFileLines(favoritesFile.getAbsolutePath()), favoriteStations);
                else
                    favoritesFile.getParentFile().mkdir();
            } catch (Exception e2) {
                e1.printStackTrace();
                e2.printStackTrace();
                System.exit(-1);
            }
        }
    }

    /**
     * Set the relevant strings to visual elements.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setTextProperties();
        setLanguagesEventHandlers();
        showStations();
    }

    /**
     * Sets the text of all elements according to chosen language.
     */
    private void setTextProperties() {
        stationsBtn.textProperty().setValue(stringProperties.get("presets"));
        favoritesBtn.textProperty().setValue(stringProperties.get("favorites"));
    }

    /**
     * create event handler for languages button in the menu.
     */
    private void setLanguagesEventHandlers() {
        englishBtn.setOnAction(event -> changeLanguage(Language.ENGLISH));
        hebrewBtn.setOnAction(event -> changeLanguage(Language.HEBREW));
    }

    /**
     * Extract all CSV file's lines.
     *
     * @param location The location of the file to read.
     * @return A list with all the file's lines.
     */
    private List<String> getCSVFileLines(String location) throws IOException {
        InputStream in = getClass().getResourceAsStream(location);
        if (in == null)
            in = new BufferedInputStream(new FileInputStream(location));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

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
        return lines;
    }

    /**
     * Read stations details from csv lines and populate a stations list.
     *
     * @param addToList A list to add stations to.
     * @param lines     A line - name, url, icon.
     */
    private void loadFromLines(List<String> lines, List<Station> addToList) {
        if (lines == null || lines.size() == 0)
            return;
        for (String line : lines) {
            String[] separatedLine = line.split(",");
            String name, url, img = "";
            name = separatedLine[0];
            url = separatedLine[1];
            if (separatedLine.length > 2)
                img = separatedLine[2];
            addToList.add(new Station(name, url, img));
        }
    }

    /**
     * Read all the string properties from a properties file.
     *
     * @param stringProperties A dictionary - key in english, value in a preferred language.
     * @param lines            A line - key in english, value in a preferred language.
     */
    private void loadFromLines(List<String> lines, Dictionary<String, String> stringProperties) {
        if (lines == null || lines.size() == 0)
            return;
        for (String line : lines) {
            String[] separatedLine = line.split(",");
            String englishKey, value;
            englishKey = separatedLine[0];
            if (separatedLine.length < 2)
                value = englishKey;
            else value = separatedLine[1];
            stringProperties.put(englishKey, value);
        }
    }

    /**
     * Build the presets list in a grid pane - image, name, star for not\favorite.
     */
    public void showStations() {
        int numOfColumns = 3, numOfRows = stations.size();
        stationsGrid.setMinSize(350, 60 * numOfRows);
        for (int i = 0; i < numOfColumns; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.ALWAYS);
            stationsGrid.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i < numOfRows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            stationsGrid.getRowConstraints().add(rowConstraints);
        }

        for (int j = 0; j < numOfRows; j++) {
            stationsGrid.add(createPane(stations.get(j).getImg(), j), 0, j);
            stationsGrid.add(createPane(stations.get(j).getName(), j), 1, j);
            stationsGrid.add(createPane(j), 2, j);

        }
    }

    /**
     * Creating a pane with relevant station's detail.
     * Add on-click event handler for each cell.
     *
     * @param image An image\icon of the station.
     * @param index The station's index (also the row in the grid).
     * @return A pane with the relevant detail.
     */
    private Pane createPane(Image image, int index) {
        StackPane pane = new StackPane();
        pane.setMinSize(170, 80);
        ImageView imageView = new ImageView(image);
        pane.getChildren().add(imageView);
        StackPane.setAlignment(imageView, Pos.CENTER_LEFT);
        pane.setOnMouseClicked(e -> play(stations.get(index)));
        return pane;
    }

    /**
     * Creating a pane with relevant station's detail.
     * Add on-click event handler for each cell.
     *
     * @param name  The name of the station.
     * @param index The station's index (also the row in the grid).
     * @return A pane with the relevant detail.
     */
    private Pane createPane(String name, int index) {
        StackPane pane = new StackPane();
        pane.setMinSize(200, 80);
        Text text = new Text(name);
        pane.getChildren().add(text);
        StackPane.setAlignment(text, Pos.CENTER);
        pane.setOnMouseClicked(e -> play(stations.get(index)));
        return pane;
    }

    /**
     * Creating pane with relevant station's details.
     * Add on-click event handler for each cell.
     * Painting a star for a not\favorite station.
     *
     * @param index The station's index (also the row in the grid).
     * @return A pane with the relevant detail.
     */
    private Pane createPane(int index) {
        Station s = stations.get(index);

        StackPane pane = new StackPane();
        pane.setMinSize(80, 80);

        double xSize = 0.22, ySize = 0.22, xAlign = 0, yAlign = 80;
        double[] initXPoints = {10, 85, 110, 135, 210, 160,
                170, 110, 50, 60};
        double[] initYPoints = {85, 75, 10, 75, 85, 125,
                190, 150, 190, 125};

        double[] xPoints = setResolutions(initXPoints, xSize, xAlign, yAlign);
        double[] yPoints = setResolutions(initYPoints, ySize, xAlign, yAlign);

        Canvas canvas = new Canvas(pane.getMinWidth() * 1, pane.getMinHeight() * 1);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);
        if (favoriteStations.contains(s))
            gc.setFill(Color.GOLD);
        else
            gc.setFill(Color.BLACK);

        gc.fillPolygon(xPoints, yPoints, xPoints.length);
        gc.strokePolygon(xPoints, yPoints, xPoints.length);

        pane.getChildren().add(canvas);

        StackPane.setAlignment(canvas, Pos.CENTER_RIGHT);

        pane.setOnMouseClicked(e -> {
            if (favoriteStations.contains(s)) {
                gc.setFill(Color.BLACK);
                gc.fillPolygon(xPoints, yPoints, xPoints.length);
                gc.strokePolygon(xPoints, yPoints, xPoints.length);
                favoriteStations.remove(s);
            } else {
                gc.setFill(Color.GOLD);
                gc.fillPolygon(xPoints, yPoints, xPoints.length);
                gc.strokePolygon(xPoints, yPoints, xPoints.length);
                favoriteStations.add(s);
            }
        });
        return pane;
    }

    /**
     * Change the star icon size and position.
     *
     * @param points X or y points array.
     * @param size   Multiple the icon's size by size parameter.
     * @param xAlign Moves the icon on the horizontal axis.
     * @param yAlign Moves the icon on the vertical axis.
     * @return A new array of the points after change.
     */
    private double[] setResolutions(double[] points, double size, double xAlign, double yAlign) {
        double[] fixedPoints = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            fixedPoints[i] = size * (points[i] + xAlign + yAlign);
        }
        return fixedPoints;
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
     * Pause the streaming, make the "pause" icon and the pausing rectangle invisible
     * and the "play" icon visible.
     */
    private void pause() {
        playing = false;
        playIcon.setVisible(true);
        pauseIcon1.setVisible(false);
        pauseIcon2.setVisible(false);
        pauseRectangle.setVisible(false);
        radioPlayer.pause();
    }

    /**
     * Start a streaming, make the "pause" icon and the pausing rectangle visible
     * and the "play" icon invisible.
     */
    private void play(Station s) {
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

    /**
     * Save the current favorites list to a file.
     * The file is csv file with the structure of "name, stream url, image url".
     */
    public static void saveFavorites() {
        try {
            File favoritesFile = new File(pathToFavorites);

            FileWriter fileWriter = new FileWriter(favoritesFile);

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

    /**
     * Change the element's text according to chosen language.
     * @param l The chosen language.
     */
    private void changeLanguage(Language l) {
        switch (l) {
            case ENGLISH:
                if(currentLanguage == Language.ENGLISH)
                    return;
                currentLanguage = Language.ENGLISH;
                break;
            case HEBREW:
                if(currentLanguage == Language.HEBREW)
                    return;
                currentLanguage = Language.HEBREW;
        }
        try {
            loadFromLines(getCSVFileLines("/"+currentLanguage+" String Properties.csv"), stringProperties);
        } catch (IOException e) {
            try{
                loadFromLines(getCSVFileLines("\\"+currentLanguage+"String Properties.csv"), stringProperties);
            } catch (IOException e1) {
                e.printStackTrace();
                e1.printStackTrace();
            }
        }
        setTextProperties();
    }
}
