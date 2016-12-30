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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
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
import ups_wargame_client.data_model.GameData;
import ups_wargame_client.data_model.IGameData;
import ups_wargame_client.data_model.Playfield;
import ups_wargame_client.data_model.Unit;
import ups_wargame_client.net_interface.MsgType;

/**
 * FXML Controller class
 *
 * @author sini
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
    private ObservableList<Lobby> obsList = FXCollections.observableArrayList();

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
    private Label playerOneLabel;
    @FXML
    private Label playerTwoLabel;
    @FXML
    private Label readyOneLabel;
    @FXML
    private Label readyTwoLabel;
    
    @FXML
    private Canvas canvasMap;
    @FXML
    private Canvas canvasField;

    private GameData gd = null;
    private Image[] cachedImages = new Image[9];
    private int lastI = 0, lastJ = 0;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        controller = ClientController.getInstance();
        controller.setupView(this);

        lobbyList.setItems(obsList);
        lobbyInfo.setExpanded(false);
        
        cachedImages[0] = new Image(getClass().getResourceAsStream("/Flag_white.png"));
        cachedImages[1] = new Image(getClass().getResourceAsStream("/Flag_blue.png"));
        cachedImages[2] = new Image(getClass().getResourceAsStream("/Flag_red.png"));
        cachedImages[3] = new Image(getClass().getResourceAsStream("/Infantry_blue.png"));
        cachedImages[4] = new Image(getClass().getResourceAsStream("/Infantry_red.png"));
        cachedImages[5] = new Image(getClass().getResourceAsStream("/SPG_blue.png"));
        cachedImages[6] = new Image(getClass().getResourceAsStream("/SPG_red.png"));
        cachedImages[7] = new Image(getClass().getResourceAsStream("/Tank_blue.png"));
        cachedImages[8] = new Image(getClass().getResourceAsStream("/Tank_red.png"));

        double offsetX = 75;
        double offsetY = 200;

        gd = new GameData();

        double startX = offsetX;
        double endX = canvasMap.getWidth() - offsetX;
        double startY = offsetY;
        double endY = canvasMap.getHeight() - offsetY;
        double horizonstalDistance = (endX - startX) / gd.getPlayField().getColumns();
        double hexSize = horizonstalDistance / 0.75;
        double verticalDistance = 0.86603 * hexSize; //sqrt(3)/2*hexsize

        //this.drawMap(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize);
        //this.drawUnits(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize / 1.5);

        disconnectButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                backToStart();
            }
        });

        refreshButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.GET_SERVER, (short) 0, null));
            }
        });

        addLobbyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                TextInputDialog dialog = new TextInputDialog("Lobby name");
                dialog.setTitle("Create lobby");
                dialog.setHeaderText("Lobby creation window");
                dialog.setContentText("Please enter lobby name:");

                // Traditional way to get the response value.
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    Object o[] = {result.get()};
                    controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.CREATE_LOBBY, (short) 1, o));
                }
            }
        });

        connectLobbyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Lobby tmp = null;
                if ((tmp = (Lobby) lobbyList.getSelectionModel().getSelectedItem()) != null) {
                    Object o[] = {tmp.getIndex()};
                    controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.JOIN_LOBBY, (short) 1, o));
                    selectedLobby = tmp;
                }
            }
        });

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

        readyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.TOGGLE_READY, (short) 0, null));
            }
        });
        
        concedeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                endGame();
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
                            hexSize-4, gd.getPlayField().getMap()[lastI][lastJ], g2d);

                    if (gd.getPlayField().contains(i, j)) {
                        drawPoint(i, j, startX, startY, horizonstalDistance, verticalDistance,
                                hexSize-5, 'Y', g2d);

                        lastI = i;
                        lastJ = j;
                    }
                }
            }
        });
        
        canvasField.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int r = Playfield.getRow(lastI, lastJ);
                int q = lastJ;
                
                
            }
        });
        
        gamePane.visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                System.out.println(gd.getPlayField().getHex(0, 0));
                if(newValue) {
                    drawMap(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize);
                    drawUnits(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize/1.5);
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
        obsList.clear();
        obsList.addAll(list);
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
        connectLobbyButton.setManaged(!isConnected);
        leaveLobbyButton.setManaged(isConnected);
        readyButton.setManaged(isConnected);
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

    public void setPrimaryStage(Stage stage) {
        this.mainStage = stage;
    }
    
    @Override
    public void setupGameData(int rows, String[] map) {
        int cols = map[0].length();
        char[][] tmp = new char[rows][cols];
        for(int i = 0; i < rows; i++) {
            tmp[i] = map[i].toCharArray();
        }
        this.gd = new GameData(rows, cols, tmp);
    }
    
    @Override
    public void setUnits(List list) {
        this.gd.setUnits(list);
        this.startGame();
    }
    
    @Override
    public void updatePlayers(String playerBlu, String playerRed, int scoreBlu, int scoreRed, char player) {
        this.gd.updateScore(playerBlu, playerRed, scoreBlu, scoreRed, player);
        this.drawScore();
    }
    
    @Override
    public void startGame() {
        gamePane.setDisable(false);
        lobbyPane.setDisable(true);
        gamePane.setManaged(true);
        lobbyPane.setManaged(false);
        gamePane.setVisible(true);
        lobbyPane.setVisible(false);
    }
    
    @Override
    public void endGame() {
        gamePane.setDisable(true);
        lobbyPane.setDisable(false);
        gamePane.setManaged(false);
        lobbyPane.setManaged(true);
        gamePane.setVisible(false);
        lobbyPane.setVisible(true);
    }
    
    @Override
    public IGameData getGameData() {
        return this.gd;
    }
    
    public void drawMap(GameData gd, double startX, double startY, double horizonstalDistance, double verticalDistance, double hexSize) {

        GraphicsContext g2d = canvasMap.getGraphicsContext2D();

        g2d.setStroke(Color.BLACK);
        g2d.setFont(new Font(8));
        g2d.setLineWidth(1);

        for (int i = 0; i < gd.getPlayField().getRows(); i++) {
            for (int j = 0; j < gd.getPlayField().getColumns(); j++) {
                //int row = Playfield.getRow(i, j);
                drawPoint(i, j, startX, startY, horizonstalDistance, verticalDistance,
                       hexSize, gd.getPlayField().getMap()[i][j], g2d);
                /*if (j % 2 == 0) {
                    this.drawHex((startX + horizonstalDistance * j) + (horizonstalDistance / 2),
                            (startY + verticalDistance * i) + (verticalDistance / 2),
                            (hexSize / 2), gd.getPlayField().getMap()[i][j], g2d);
                    g2d.strokeText(j + " " + row, (startX + horizonstalDistance * j),
                            (startY + verticalDistance * i) + (verticalDistance / 2));
                } else {
                    this.drawHex((startX + horizonstalDistance * j) + (horizonstalDistance / 2),
                            (startY + verticalDistance * i),
                            (hexSize / 2), gd.getPlayField().getMap()[i][j], g2d);
                    g2d.strokeText(j + " " + row, (startX + horizonstalDistance * j),
                            (startY + verticalDistance * i));
                }*/
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

    public void drawUnits(GameData gd, double startX, double startY, double horizonstalDistance, double verticalDistance, double hexSize) {
        GraphicsContext g2d = canvasField.getGraphicsContext2D();
        
        g2d.clearRect(0, 0, canvasField.getWidth(), canvasField.getHeight());
        
        for (Unit val : gd.getUnits()) {
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

    private void drawUnit(double x, double y, double size, Unit unit, GraphicsContext g2d) {
        int index = 1;
        double width = size, height = size;

        if (unit.getType() == 'I') {
            index += 2;
        } else if (unit.getType() == 'S') {
            index += 4;
            height = height / 1.5;
        } else if (unit.getType() == 'T') {
            index += 6;
            height = height / 1.5;
        }

        if (unit.getAllegiance() == 'B') {
            g2d.drawImage(cachedImages[index],
                    x - size / 2 + width,
                    y - size / 2,
                    -width, height);
        } else if (unit.getAllegiance() == 'R') {
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
    }
    
    private void drawScore() {
        GraphicsContext g2d = canvasField.getGraphicsContext2D();
        
        g2d.clearRect(0, 0, canvasField.getWidth(), 200);
        
        g2d.setFill(Color.BLUE);
        g2d.setFont(new Font(25));
        
        g2d.fillText(gd.getPlayerBlue(), 50, 50);
        g2d.fillText(String.valueOf(gd.getBlueScore()), 150, 50);
        
        g2d.setFill(Color.BLACK);
        g2d.fillText("VS", 325, 50);
        
        g2d.setFill(Color.RED);
        g2d.fillText(String.valueOf(gd.getBlueScore()), 500, 50);
        g2d.fillText(gd.getPlayerBlue(), 550, 50);
        
    }
}
