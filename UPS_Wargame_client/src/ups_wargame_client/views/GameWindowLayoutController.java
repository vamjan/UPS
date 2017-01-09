/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.views;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ups_wargame_client.control.ClientController;
import ups_wargame_client.control.Command;
import ups_wargame_client.control.IController;
import ups_wargame_client.control.Lobby;
import ups_wargame_client.data_model.IGameData;
import ups_wargame_client.data_model.Playfield;
import ups_wargame_client.data_model.Unit;
import ups_wargame_client.net_interface.MsgType;

/**
 * FXML Controller class of game window. Serves as viewer of game data and server information.
 * Also serves for user to control the application and send messages to server using
 * GUI elements.
 * @author Jan Vampol
 */
public class GameWindowLayoutController implements Initializable, IViewable {

    private IController controller = null;
    private Lobby selectedLobby = null;

    @FXML
    private AnchorPane anchorPane;
    private Stage mainStage;
    @FXML
    private TabPane chatPane;
    @FXML
    private TitledPane lobbyInfo;
    @FXML
    private Pane gamePane;
    @FXML
    private Pane lobbyPane;

    @FXML
    private ListView lobbyList;
    //observable list of lobbies, works with ListView lobbyList
    private ObservableList<Lobby> obsLobby = FXCollections.observableArrayList();
    @FXML
    private ListView gameInfo;
    //observable list of units, works with ListView gameInfo
    private ObservableList<Unit> obsUnits = FXCollections.observableArrayList();

    @FXML
    private Button disconnectButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addLobbyButton;
    @FXML
    private Button connectLobbyButton;
    @FXML
    private Button leaveLobbyButton;
    @FXML
    private Button readyButton;
    @FXML
    private Button skipButton;
    @FXML
    private Button concedeButton;

    @FXML
    private TextArea serverChatTextArea;
    @FXML
    private TextArea lobbyChatTextArea;
    @FXML
    private TextField playerName;

    @FXML
    private Label playerOneLabel;
    @FXML
    private Label playerTwoLabel;
    @FXML
    private Label readyOneLabel;
    @FXML
    private Label readyTwoLabel;
    @FXML
    private Label winLabel;

    @FXML
    private Canvas canvasMap; //map canvas
    @FXML
    private Canvas canvasField; //unit canvas

    private IGameData gd = null;
    //cached resource images
    private final Image[] cachedImages = new Image[9];
    private int lastI = 0, lastJ = 0;
    private double offsetX;
    private double offsetY;
    private double startX;
    private double endX;
    private double startY;
    private double endY;
    private double horizonstalDistance;
    private double hexSize;
    private double verticalDistance;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        controller = ClientController.getInstance();
        //reference view to controller
        controller.setupView(this);
        //setup ListView observables
        gameInfo.setItems(obsUnits);
        lobbyList.setItems(obsLobby);
        lobbyInfo.setExpanded(false);
        //load resource images and keep them in memory
        cachedImages[0] = new Image(getClass().getResourceAsStream("/Flag_white.png"));
        cachedImages[1] = new Image(getClass().getResourceAsStream("/Flag_blue.png"));
        cachedImages[2] = new Image(getClass().getResourceAsStream("/Flag_red.png"));
        cachedImages[3] = new Image(getClass().getResourceAsStream("/Infantry_blue.png"));
        cachedImages[4] = new Image(getClass().getResourceAsStream("/Infantry_red.png"));
        cachedImages[5] = new Image(getClass().getResourceAsStream("/SPG_blue.png"));
        cachedImages[6] = new Image(getClass().getResourceAsStream("/SPG_red.png"));
        cachedImages[7] = new Image(getClass().getResourceAsStream("/Tank_blue.png"));
        cachedImages[8] = new Image(getClass().getResourceAsStream("/Tank_red.png"));
        
        playerName.setText(String.format("%X", controller.getClientID()));

        offsetX = 75;
        offsetY = 200;

        startX = offsetX;
        endX = canvasMap.getWidth() - offsetX;
        startY = offsetY;
        endY = canvasMap.getHeight() - offsetY;
        horizonstalDistance = (endX - startX) / 15; //HACK: musi se zjistit pri startu
        hexSize = horizonstalDistance / 0.75;
        verticalDistance = 0.86603 * hexSize; //sqrt(3)/2*hexsize

        disconnectButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                backToStart();
            }
        });
        //send get_server to refresh server data
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.GET_SERVER, (short) 0, null));
            }
        });
        //get name form user and send create_lobby with name to server
        addLobbyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                TextInputDialog dialog = new TextInputDialog("Lobby name");
                dialog.setTitle("Create lobby");
                dialog.setHeaderText("Lobby creation window");
                dialog.setContentText("Please enter lobby name:");

                // Traditional way to get the response value.
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent() && result.get().length() <= 30) {
                    Object o[] = {result.get()};
                    controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.CREATE_LOBBY, (short) 1, o));
                }
            }
        });
        //sending join_lobby on click
        connectLobbyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Lobby tmp;
                if ((tmp = (Lobby) lobbyList.getSelectionModel().getSelectedItem()) != null) {
                    Object o[] = {tmp.getIndex()};
                    controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.JOIN_LOBBY, (short) 1, o));
                    selectedLobby = tmp;
                }
            }
        });
        //sending leave_lobby on click
        leaveLobbyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (selectedLobby != null) {
                    Object o[] = {selectedLobby.getIndex()};
                    controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.LEAVE_LOBBY, (short) 1, o));
                    selectedLobby = null; //TODO: might be a problem
                }
            }
        });
        //sending toggle_ready on click
        readyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.TOGGLE_READY, (short) 0, null));
            }
        });
        //sending end on click
        concedeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.END, (short) 0, null));
            }
        });
        //sending skip on click when client is on turn
        skipButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (gd.playerOnTurn()) {
                    if (gd.getAttacking()) {
                        System.out.println("Skipping attack");
                        controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.SKIP, (short) 0, null));
                    } else {
                        System.out.println("Skipping move");
                        gd.setAttacking(true);
                        gd.setUpdated(true);
                    }
                } else {
                    showLobbyMessage("[Client]: ", "Not your turn!");
                }
            }
        });

        canvasField.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX() - startX, y = event.getY() - startY;

                int q = (int) Math.floor(x * 2 / 3 / hexSize * 2);
                int r = (int) Math.floor((-x / 3 + 0.57735 * y) / hexSize * 2);

                GraphicsContext g2d = canvasMap.getGraphicsContext2D();

                int i = Playfield.convertRow(r, q);
                int j = q;

                if (lastI == i && lastJ == j) {
                } else {
                    drawPoint(lastI, lastJ, startX, startY, horizonstalDistance, verticalDistance,
                            hexSize - 4, gd.getPlayField().getMap()[lastI][lastJ], g2d);

                    if (gd.getPlayField().contains(i, j)) {
                        drawPoint(i, j, startX, startY, horizonstalDistance, verticalDistance,
                                hexSize - 5, 'Y', g2d);

                        lastI = i;
                        lastJ = j;
                    }
                }
            }
        });
        //play on click on map
        canvasField.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int r = Playfield.getRow(lastI, lastJ);
                int q = lastJ;

                if (gd.play(q, r)) {//something weird here
                    System.out.println("I PLAY!");
                } else {
                    System.out.println("I CAN'T PLAY!");
                }
            }
        });

        gamePane.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    draw();
                } else {
                    canvasField.getGraphicsContext2D().clearRect(0, 0, canvasField.getHeight(), canvasField.getWidth());
                    canvasMap.getGraphicsContext2D().clearRect(0, 0, canvasMap.getHeight(), canvasMap.getWidth());
                }
            }

        });
    }

    @Override
    public void showServerMessage(String data, String msg) {
        serverChatTextArea.appendText(data + msg + '\n');
    }

    @Override
    public void showLobbyMessage(String data, String msg) {
        lobbyChatTextArea.appendText(data + msg + '\n');
    }

    @Override
    public void backToStart() {
        ClientController.getInstance().stopConnection();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MainWindowLayout.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("WARGAME");
            stage.setScene(scene);
            stage.show();
            mainStage.hide();
        } catch (IOException ioe) {
            System.err.println("Error!" + ioe.getMessage());
            System.exit(0);
        }
    }

    @Override
    public void setLobbyList(List list) {
        obsLobby.clear();
        obsLobby.addAll(list);
    }

    @Override
    public void showLobby() {
        lobbyInfo.setExpanded(true);
        playerOneLabel.setText(selectedLobby.getPlayerOne());
        readyOneLabel.setText(selectedLobby.getReadyOne() ? "READY" : "NOT READY");
        playerTwoLabel.setText(selectedLobby.getPlayerTwo());
        readyTwoLabel.setText(selectedLobby.getReadyTwo() ? "READY" : "NOT READY");
    }

    @Override
    public void updateLobby(Lobby lobby) {
        this.selectedLobby.setPlayerOne(lobby.getPlayerOne());
        this.selectedLobby.setReadyOne(lobby.getReadyOne());
        this.selectedLobby.setPlayerTwo(lobby.getPlayerTwo());
        this.selectedLobby.setReadyTwo(lobby.getReadyTwo());
        this.showLobby();
    }

    @Override
    public void hideLobby() {
        lobbyInfo.setExpanded(false);
    }

    @Override
    public void toggleConnected() {
        boolean isConnected = !leaveLobbyButton.isVisible();
        connectLobbyButton.setVisible(!isConnected);
        leaveLobbyButton.setVisible(isConnected);
        readyButton.setVisible(isConnected);
        addLobbyButton.setVisible(!isConnected);
        connectLobbyButton.setManaged(!isConnected);
        leaveLobbyButton.setManaged(isConnected);
        readyButton.setManaged(isConnected);
        addLobbyButton.setManaged(!isConnected);
    }

    @Override
    public void refuse() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Refused");
        alert.setHeaderText("Unable to comply");
        alert.setContentText("Action was refused by the server.");

        alert.showAndWait();
    }

    @Override
    public void acknowledge() {
        System.out.println("Action acknowledged.");
    }

    @Override
    public void showReconnect(int index) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Reconnect options");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to reconnect to active game?");

        ButtonType buttonTypeOne = new ButtonType("Yes");
        ButtonType buttonTypeTwo = new ButtonType("No");

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            Object o[] = {index};
            controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.RECONNECT, (short) 1, o));
            selectedLobby = obsLobby.get(index);
            toggleConnected();
            showLobby();
        }
    }

    @Override
    public void showWait() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game is empty");
        alert.setHeaderText(null);
        alert.setContentText("Your opponent has left the game.\nYou can leave any time with Concede button, or wait for him/her/it to reconnect.");

        alert.showAndWait();
    }

    public void setPrimaryStage(Stage stage) {
        this.mainStage = stage;
    }

    @Override
    public void startGame(IGameData data) {
        this.gd = data;
        gamePane.setDisable(false);
        lobbyPane.setDisable(true);
        gamePane.setManaged(true);
        lobbyPane.setManaged(false);
        gamePane.setVisible(true);
        lobbyPane.setVisible(false);
        winLabel.setText("");
    }

    @Override
    public void endGame(String winner) {
        gamePane.setDisable(true);
        lobbyPane.setDisable(false);
        gamePane.setManaged(false);
        lobbyPane.setManaged(true);
        gamePane.setVisible(false);
        lobbyPane.setVisible(true);
        if (winner.equals("DRAW")) {
            winLabel.setText("Game was a draw!");
        } else {
            winLabel.setText(winner + " has won the game!");
        }
        this.gd = null;
    }

    public void draw() {
        drawMap(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize);
        drawUnits(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize / 1.5);
    }

    @Override
    public void redraw(IGameData data) {
        this.gd = data;
        drawUnits(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize / 1.5);
        drawScore(gd);
        updateUnits(gd);
    }

    public void drawMap(IGameData gd, double startX, double startY, double horizonstalDistance, double verticalDistance, double hexSize) {

        GraphicsContext g2d = canvasMap.getGraphicsContext2D();

        g2d.setStroke(Color.BLACK);
        g2d.setFont(new Font(8));
        g2d.setLineWidth(1);

        for (int i = 0; i < gd.getPlayField().getRows(); i++) {
            for (int j = 0; j < gd.getPlayField().getColumns(); j++) {
                drawPoint(i, j, startX, startY, horizonstalDistance, verticalDistance,
                        hexSize, gd.getPlayField().getMap()[i][j], g2d);
            }
        }

        g2d.setLineWidth(2);

        for (int i = 0; i < gd.getPlayField().getRows(); i++) {
            for (int j = 0; j < gd.getPlayField().getColumns(); j++) {
                drawPoint(i, j, startX, startY, horizonstalDistance, verticalDistance,
                        hexSize, '0', g2d);
            }
        }

        g2d.setStroke(Color.TRANSPARENT);

        canvasField.toFront();
    }

    private void drawPoint(int row, int col, double startX, double startY, double horizonstalDistance,
            double verticalDistance, double hexSize, char terrain, GraphicsContext g2d) {
        if (col % 2 == 0) {
            drawHex((startX + horizonstalDistance * col) + (horizonstalDistance / 2),
                    (startY + verticalDistance * row) + (verticalDistance / 2),
                    (hexSize / 2), terrain, g2d);
        } else {
            drawHex((startX + horizonstalDistance * col) + (horizonstalDistance / 2),
                    (startY + verticalDistance * row),
                    (hexSize / 2), terrain, g2d);
        }
    }

    private void drawHex(double x, double y, double size, char terrain, GraphicsContext g2d) {
        double[] coordsX = new double[6];
        double[] coordsY = new double[6];

        if (terrain == '0') {
            g2d.setFill(Color.TRANSPARENT);
        } else if (terrain == 'G') {
            g2d.setFill(Color.GREEN);
        } else if (terrain == 'Y') {
            g2d.setFill(Color.YELLOW);
        } else if (terrain == 'D') {
            g2d.setFill(Color.BROWN);
        }

        for (int i = 0; i < 6; i++) {
            double angleRad = (Math.PI / 180) * (60 * i);
            double coordX = x + size * Math.cos(angleRad);
            double coordY = y + size * Math.sin(angleRad);
            coordsX[i] = coordX;
            coordsY[i] = coordY;
        }

        g2d.fillPolygon(coordsX, coordsY, 6);
        g2d.strokePolygon(coordsX, coordsY, 6);
    }

    public void drawUnits(IGameData gd, double startX, double startY, double horizonstalDistance, double verticalDistance, double hexSize) {
        GraphicsContext g2d = canvasField.getGraphicsContext2D();

        g2d.clearRect(0, 0, canvasField.getWidth(), canvasField.getHeight());

        for (Unit val : gd.getUnits()) {
            if (!val.isDead()) {
                int row = Playfield.convertRow(val.getCoordX(), val.getCoordZ());
                int col = val.getCoordZ();
                if (col % 2 == 0) {
                    this.drawUnit((startX + horizonstalDistance * col) + (horizonstalDistance / 2),
                            (startY + verticalDistance * row) + (verticalDistance / 2),
                            hexSize, val, g2d);
                } else {
                    this.drawUnit((startX + horizonstalDistance * col) + (horizonstalDistance / 2),
                            (startY + verticalDistance * row),
                            hexSize, val, g2d);
                }
            }
        }
    }

    private void drawUnit(double x, double y, double size, Unit unit, GraphicsContext g2d) {
        int index = 1;
        double width = size, height = size;

        if (unit.getType().getName() == 'I') {
            index += 2;
        } else if (unit.getType().getName() == 'S') {
            index += 4;
            height = height / 1.5;
        } else if (unit.getType().getName() == 'T') {
            index += 6;
            height = height / 1.5;
        }

        if (unit.getAllegiance().getName() == 'B') {
            g2d.drawImage(cachedImages[index],
                    x - size / 2 + width,
                    y - size / 2,
                    -width, height);
        } else if (unit.getAllegiance().getName() == 'R') {
            index++;
            g2d.drawImage(cachedImages[index],
                    x - size / 2,
                    y - size / 2,
                    width, height);
        } else {
            g2d.drawImage(cachedImages[0],
                    x - size / 2,
                    y - size / 2,
                    width, height);
        }
        if (unit.equals(gd.getUnitOnTurn())) {
            drawConvenientArrow(x, y, Color.CORAL, size / 2);
        }
    }

    public void drawScore(IGameData gd) {
        GraphicsContext g2d = canvasField.getGraphicsContext2D();

        g2d.clearRect(0, 0, canvasField.getWidth(), 200);

        if (gd.getUnitOnTurn().getAllegiance().getName() == 'B') {
            g2d.setStroke(Color.BLACK);
        }
        g2d.setFill(Color.BLUE);
        g2d.setFont(new Font(25));

        g2d.fillText(gd.getPlayerBlue(), 50, 50);
        g2d.strokeText(gd.getPlayerBlue(), 50, 50);
        g2d.fillText(String.valueOf(gd.getBlueScore()), 250, 50);

        g2d.setStroke(Color.TRANSPARENT);
        g2d.setFill(Color.BLACK);
        g2d.fillText("VS", 325, 50);
        if (gd.playerOnTurn()) {
            g2d.fillText(gd.getAttacking() ? "ATTACKING" : "MOVING", 285, 80);
        } else {
            g2d.fillText("WAITING", 285, 80);
        }

        if (gd.getUnitOnTurn().getAllegiance().getName() == 'R') {
            g2d.setStroke(Color.BLACK);
        }
        g2d.setFill(Color.RED);
        g2d.fillText(String.valueOf(gd.getRedScore()), 400, 50);
        g2d.fillText(gd.getPlayerRed(), 450, 50);
        g2d.strokeText(gd.getPlayerRed(), 450, 50);

        g2d.setStroke(Color.TRANSPARENT);
        g2d.setFill(Color.BLACK);
    }

    public void drawConvenientArrow(double x, double y, Color color, double hexSize) {
        y -= hexSize * 1.5;

        GraphicsContext g2d = canvasField.getGraphicsContext2D();

        double coordsX[] = {x, x + 10, x + 5};
        double coordsY[] = {y, y, y + 7};

        g2d.setFill(color);
        g2d.fillPolygon(coordsX, coordsY, 3);
    }

    public void updateUnits(IGameData gd) {
        obsUnits.clear();
        for (Unit val : gd.getUnits()) {
            if (val.getType() != Unit.UnitType.FLAG && !val.isDead()) {
                obsUnits.add(val);
            }
        }
    }
}
