import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Duration;

import static javafx.scene.layout.BorderStrokeStyle.SOLID;

public class GUI extends Application {
    private Game game;
    private Game.Slot[][] slots;
    private BorderPane root;
    private BoardPane playground;
    private ItemBox currentItemBox;
    private ItemBox savedItemBox;
    private Label scoreLabel;
    private Label timeLabel;
    private MediaPlayer musicPlayer;
    private AudioClip scoreSoundPlayer;
    private AudioClip clickSoundPlayer;
    private AudioClip newGameSoundPlayer;
    private Label playAgainLabel;
    static private Font fontBig;
    static private Font fontSmall;
    Map<Game.Values, String> images = new HashMap<>();
    private final int width = 4;
    private final int height = 4;

    /**
     * Box that contains graphic representation of an element containing <code>Values</code>.
     */
    private class ElementBox extends VBox {
        protected final Game.ElementWithValue element;
        private ElementBox(Game.ElementWithValue element) {
            this.element = element;
            setBorder(new Border(new BorderStroke(Color.WHITE, SOLID, null, null)));
        }

        /**
         * Shows the image of the element on the board, visualizes <code>Values</code>.
         */
        protected void paint() {
            getChildren().clear();
            String s = images.get(getValue());
            ImageView r = new ImageView(s);
            getChildren().add(r);
        }

        protected Game.Values getValue() {
            return element.getValue();
        }

    }

    /**
     * Box that contains graphic representation of a <code>Slot</code>.
     */
    private class SlotBox extends ElementBox {

        private SlotBox(Game.Slot slot) {
            super(slot);
            setOnMouseClicked(e -> {
                game.chooseSlot((Game.Slot) this.element);
                game.makeStep();
                playground.paint();
                clickSoundPlayer.play();

                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), event1 -> {
                    if (game.checkAdjacency()) {
                        scoreSoundPlayer.play();
                    }
                    playground.paint();
                    if (game.isOver()) {
                        root.setCenter(playAgainLabel);
                    }
                }));
                timeline.playFromStart();
            });
        }

    }


    /**
     * Box that contains graphic representation of an Item, it has a label and can be highlighted.
     */
    private class ItemBox extends ElementBox {
        private final Label label = new Label();
        private ItemBox(Game.Item item, String text) {
            super(item);
            label.setText(text);
            setPrefWidth(80);
            setAlignment(Pos.CENTER);
        }

        /**
         * Uses super() to visualize and adds a label.
         */
        @Override
        protected void paint() {
            super.paint();
            getChildren().add(label);
            label.setFont(fontSmall);
            label.setTextFill(Color.WHITE);
        }

        /**
         * Shows a thick border around the box.
         */
        private void highlight() {
            setBorder(new Border(new BorderStroke(Color.WHITE, SOLID, null, new BorderWidths(5))));
        }

        /**
         * Hides the thick border around the box.
         */
        private void dehighlight() {
            setBorder(Border.EMPTY);
        }
    }

    /**
     * Pane that contains graphic representation of a play board.
     */
    private class BoardPane extends GridPane {
        private BoardPane() {
           setMaxWidth(0);
            root.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    SlotBox card = new SlotBox(slots[j][i]);
                    add(card, i, j);
                    card.paint();
                }
            }
        }

        private void paint() {
            for (Node node: getChildren()) {
                if (node instanceof SlotBox) {
                    ((SlotBox) node).paint();
                }
            }
            currentItemBox.paint();
            savedItemBox.paint();
            if (game.isSavedPicked()) {
                savedItemBox.highlight();
                currentItemBox.dehighlight();
            }
            else {
                currentItemBox.highlight();
                savedItemBox.dehighlight();
            }
            scoreLabel.setText("Score: " + game.getScore());
        }
    }

    /**
     * Starts the application, loads all the image paths, fonts and sounds, starts the timer, initializes the game and
     * creates the graphic window.
     */
    @Override
    public void start(Stage stage){
        loadFonts();
        loadImages();

        root = new BorderPane();
        prepareGame();

        stage.setTitle("3x");
        stage.setScene(new Scene(root));
        stage.show();
        stage.setResizable(false);
        createAudio();

        setTimer();
    }

    private void loadFonts() {
        try {
            fontSmall = Font.loadFont(new FileInputStream(new File("fonts/prstartk.TTF")), 9);
            fontBig = Font.loadFont(new FileInputStream(new File("fonts/prstartk.TTF")), 12);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //credit: Pixel flowers stages of blooming by Brysiaa, Deviantart
    //credit: pixel bee by jxy25, Pixilart
    //modified and edited by me
    private void loadImages() {
        images.put(Game.Values.LVL1, "file:images\\lvl1.png");
        images.put(Game.Values.LVL2, "file:images\\lvl2.png");
        images.put(Game.Values.LVL3, "file:images\\lvl3.png");
        images.put(Game.Values.LVL4, "file:images\\lvl4.png");
        images.put(Game.Values.LVL5, "file:images\\lvl5.png");
        images.put(Game.Values.EMPTY, "file:images\\empty.png");
        images.put(Game.Values.DANGER, "file:images\\danger.png");
    }

    private void prepareGame() {
        initializeGame();
        createGraphics();
    }

    private void initializeGame() {
        game = new Game(width, height);
        slots = game.getSlots();
    }

    private void createGraphics() {
        createPlayAgainLabel();

        createTopPanel();
        createPlayground();
        playground.paint();
    }

    //credit: Kevin MacLeod - Pixelland ♫ NO COPYRIGHT 8-bit Music
    //credit: https://mixkit.co/free-sound-effects/arcade/
    private void createAudio() {
        musicPlayer = new MediaPlayer(loadMusic("music\\music.mp3"));
        musicPlayer.setOnEndOfMedia(() -> musicPlayer.seek(Duration.ZERO));
        musicPlayer.play();

        scoreSoundPlayer = new AudioClip(Paths.get("music/score.wav").toUri().toString());
        clickSoundPlayer = new AudioClip(Paths.get("music/click.wav").toUri().toString());
        newGameSoundPlayer = new AudioClip(Paths.get("music/newgame.wav").toUri().toString());

        newGameSoundPlayer.play();
    }

    private Media loadMusic(String path) {
        return new Media(new File(path).toURI().toString());
    }

    private void setTimer() {
        Timeline tl = new Timeline(new KeyFrame(new Duration(1000), e -> {
            game.incrementElapsedTime();
            timeLabel.setText("Time: " + game.getElapsedTime());
        }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    private void createPlayAgainLabel() {
        playAgainLabel = new Label("Play again");
        playAgainLabel.setOnMouseClicked(e -> {
            prepareGame();
            newGameSoundPlayer.play();
        });
        playAgainLabel.setOnMousePressed(e -> playAgainLabel.setFont(fontSmall));
        playAgainLabel.setOnMouseReleased(e -> playAgainLabel.setFont(fontBig));
        playAgainLabel.setFont(fontBig);
        playAgainLabel.setTextFill(Color.WHITE);
    }

    private void createPlayground() {
        playground = new BoardPane();
        root.setCenter(this.playground);
        BorderPane.setAlignment(playground, Pos.CENTER);
    }

    private void createTopPanel() {
        createSpecialCards();
        createLabels();

        HBox topPanel = new HBox(timeLabel, scoreLabel, currentItemBox, savedItemBox);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setSpacing(30);
        topPanel.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        topPanel.setBorder(new Border(new BorderStroke(Color.WHITE, SOLID, null, null)));
        topPanel.setMinSize(500, 0);
        root.setTop(topPanel);
    }

    private void createSpecialCards() {
        currentItemBox = new ItemBox(game.getCurrentItem(), "new");
        currentItemBox.setOnMouseClicked(e -> {
            game.pickCurrentItem();
            playground.paint();
            clickSoundPlayer.play();
        });

        savedItemBox = new ItemBox(game.getSavedItem(), "saved");
        savedItemBox.setOnMouseClicked(e -> {
            if (game.hasSaved()) {
                game.pickSavedItem();
            }
            else {
                game.saveItem();
            }
            playground.paint();
            clickSoundPlayer.play();
        });
    }

    private void createLabels(){
        scoreLabel = new Label("Score: 0");
        timeLabel = new Label("Time: 0");

        scoreLabel.setFont(fontBig);
        scoreLabel.setTextFill(Color.WHITE);
        timeLabel.setFont(fontBig);
        timeLabel.setTextFill(Color.WHITE);
    }

    /**
     * Starting point.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
