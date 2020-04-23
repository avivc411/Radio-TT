package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * TODO: string properties are gibberish
 * TODO: add preferences+file
 * TODO: current song playing
 * TODO: change menu
 */
public class Controller implements Initializable {
    private enum Language {ENGLISH, HEBREW}

    //a list of all supported stations
    private static final List<Station> stations = new LinkedList<>();
    private static final List<Station> favoriteStations = new LinkedList<>();
    private static Dictionary<String, String> stringProperties = new Hashtable<>();

    //user's Documents\\Radio Touch-Touch\\Favorites.csv
    private static String pathToFavorites;
    private boolean playing = false;
    private Station currentStation = null;
    private IPlayer radioPlayer = new RadioPlayer();
    private Language currentLanguage = Language.ENGLISH;
    private int currentIndex = 0;

    @FXML
    private Pane rootPane, playerPane;
    @FXML
    private Shape playIcon, pauseIcon1, pauseIcon2;
    @FXML
    private Rectangle pauseRectangle;
    @FXML
    private ImageView coverImg;
    @FXML
    private Slider volumeSlider;
    @FXML
    private GridPane stationsGrid, favoritesGrid;
    @FXML
    private Button stationsBtn, favoritesBtn;
    @FXML
    private MenuItem englishBtn, hebrewBtn;
    @FXML
    private Menu fileMenu, preferencesMenu, helpMenu;
    @FXML
    SplitMenuButton chooseLanguage;

    /**
     * Building the stations and favorites lists and the string properties map.
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
        if (currentLanguage != Language.ENGLISH)
            setTextProperties();
        setLanguagesEventHandlers();
        showStations();
        volumeSlider.setValue(volumeSlider.getMax());
        changeVolume();
    }

    /**
     * Response to key press.
     * Space - play\pause.
     * Add\Subtract (key pad) - next\previous station.
     * Volume up\down - change volume by 10%.
     */
    private void keyPressed(KeyEvent event) {
        System.out.println(event.getCode());
        switch (event.getCode()) {
            case SPACE:
                playPause();
                break;
            case VOLUME_UP:
                volumeSlider.setValue(volumeSlider.getValue() + volumeSlider.getMax() * 0.1);
                changeVolume();
                break;
            case VOLUME_DOWN:
                volumeSlider.setValue(volumeSlider.getValue() - volumeSlider.getMax() * 0.1);
                changeVolume();
                break;
            case ADD:
                currentIndex++;
                if (favoritesGrid.isVisible())
                    play(favoriteStations.get(Math.abs(currentIndex % favoriteStations.size())));
                else
                    play(stations.get(Math.abs(currentIndex % stations.size())));
                break;
            case SUBTRACT:
                currentIndex--;
                if (favoritesGrid.isVisible())
                    play(favoriteStations.get(Math.abs(currentIndex % favoriteStations.size())));
                else
                    play(stations.get(Math.abs(currentIndex % stations.size())));
                break;
        }
        event.consume();
    }

    /**
     * Sets responses to user clicks events.
     */
    public void setKeysHandlers() {
        rootPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);

        playerPane.setOnScroll(event -> {
            volumeSlider.setValue(volumeSlider.getValue() + event.getDeltaY() / 4);
            changeVolume();
        });
    }

    /**
     * Sets the text of all elements according to chosen language.
     */
    private void setTextProperties() {
        stationsBtn.textProperty().setValue(stringProperties.get("Station"));
        favoritesBtn.textProperty().setValue(stringProperties.get("Favorites"));
        fileMenu.textProperty().setValue(stringProperties.get("File"));
        preferencesMenu.textProperty().setValue(stringProperties.get("Preferences"));
        helpMenu.textProperty().setValue(stringProperties.get("Help"));
        chooseLanguage.textProperty().setValue(stringProperties.get("Choose Language"));
        englishBtn.textProperty().setValue(stringProperties.get("English"));
        hebrewBtn.textProperty().setValue(stringProperties.get("Hebrew"));
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
    private List<String> getCSVFileLines(String location, Charset charset) throws IOException {
        InputStream in = getClass().getResourceAsStream(location);
        if (in == null)
            in = new BufferedInputStream(new FileInputStream(location));
        BufferedReader bufferedReader;

        if (charset == null) {
            bufferedReader = new BufferedReader(new InputStreamReader(in));
        } else
            bufferedReader = new BufferedReader(new InputStreamReader(in, charset));

        return getCSVFileLines(bufferedReader);
    }

    /**
     * Read file without specify charset.
     */
    private List<String> getCSVFileLines(String location) throws IOException {
        return getCSVFileLines(location, null);
    }

    /**
     * @param bufferedReader Reader to use.
     * @return A list with all the file's lines.
     */
    private List<String> getCSVFileLines(BufferedReader bufferedReader) throws IOException {
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
     * Build the stations list in a grid pane - image, name, star for not\favorite.
     */
    private void createStationsList(List<Station> stations, GridPane stationsGrid, boolean buildFavorites) {
        int numOfColumns = 3, numOfRows = stations.size();
        stationsGrid.setMinSize(350, 60 * numOfRows);
        setConstraints(numOfColumns, numOfRows, stationsGrid);

        addPanes(numOfRows, stations, stationsGrid, buildFavorites);
    }

    /**
     * Sets the constraints for the stations grids (favorites and regular).
     *
     * @param numOfColumns Num of columns in the grid (currently fixed on 3).
     * @param numOfRows    Num of rows in the grid.
     * @param stationsGrid Which grid to refer.
     */
    private void setConstraints(int numOfColumns, int numOfRows, GridPane stationsGrid) {
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
    }

    /**
     * Add panes to @stationsGrid and set event listener for click on pane's content.
     *
     * @param numOfRows      Num of rows in the grid.
     * @param stations       A list of stations to read details from.
     * @param stationsGrid   Which grid to refer.
     * @param buildFavorites A flag for the event listeners creation.
     */
    private void addPanes(int numOfRows, List<Station> stations, GridPane stationsGrid, boolean buildFavorites) {
        for (int j = 0, index; j < numOfRows; j++) {
            if (buildFavorites)
                index = Controller.stations.indexOf(stations.get(j));
            else
                index = j;
            stationsGrid.add(createPane(stations.get(j).getImg(), index), 0, j);
            stationsGrid.add(createPane(stations.get(j).getName(), index), 1, j);
            stationsGrid.add(createPane(index), 2, j);
        }
    }

    /**
     * Show the stations list.
     */
    public void showStations() {
        //on initialization
        if (stationsGrid.getChildren().size() == 0) {
            createStationsList(stations, stationsGrid, false);
        }
        favoritesGrid.setVisible(false);
        stationsGrid.setVisible(true);
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
        StackPane.setAlignment(imageView, Pos.CENTER);
        pane.setOnMouseClicked(e -> play(stations.get(currentIndex = index)));
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
        pane.setOnMouseClicked(e -> play(stations.get(currentIndex = index)));
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
            favoritesGrid.getChildren().clear();
            createStationsList(favoriteStations, favoritesGrid, true);
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

    /**
     * Show the favorites stations.
     */
    public void showFavorites() {
        if (favoritesGrid.getChildren().size() == 0)
            createStationsList(favoriteStations, favoritesGrid, true);
        favoritesGrid.setVisible(true);
        stationsGrid.setVisible(false);
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

    /**
     * Sets the volume to the volume slider value.
     */
    public void changeVolume() {
        double volume = volumeSlider.getValue();
        radioPlayer.setVolume(volume);
    }

    /**
     * Save the current favorites list to a file.
     * The file is csv file with the structure of "name, stream url, image url".
     */
    public static void saveFavorites() {
        try {
            FileWriter fileWriter = new FileWriter(pathToFavorites);
            fileWriter.close();
            fileWriter = new FileWriter(pathToFavorites);

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
     *
     * @param l The chosen language.
     */
    private void changeLanguage(Language l) {
        Charset charset = null;
        switch (l) {
            case ENGLISH:
                if (currentLanguage == Language.ENGLISH)
                    return;
                currentLanguage = Language.ENGLISH;
                break;
            case HEBREW:
                if (currentLanguage == Language.HEBREW)
                    return;
                currentLanguage = Language.HEBREW;
                charset = StandardCharsets.UTF_16;
        }
        Dictionary<String, String> properties = new Hashtable<>(stringProperties.size());
        try {
            loadFromLines(getCSVFileLines("/" + currentLanguage + " String Properties.csv", charset), properties);
            stringProperties = properties;
        } catch (IOException e) {
            try {
                loadFromLines(getCSVFileLines("\\" + currentLanguage + "String Properties.csv", charset), properties);
                stringProperties = properties;
            } catch (IOException e1) {
                e.printStackTrace();
                e1.printStackTrace();
            }
        }
        setTextProperties();
    }
}
