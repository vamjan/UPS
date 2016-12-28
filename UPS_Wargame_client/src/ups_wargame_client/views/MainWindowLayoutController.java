/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.views;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import ups_wargame_client.control.ClientController;
import ups_wargame_client.control.GameEngine;
import ups_wargame_client.data_model.GameData;
import ups_wargame_client.data_model.Playfield;
import ups_wargame_client.data_model.Unit;

/**
 *
 * @author sini
 */
public class MainWindowLayoutController implements Initializable {

    @FXML //  fx:id="myButton"
    private Button goButton; // Value injected by FXMLLoader

    @FXML
    private TextField playerTF;
    @FXML
    private TextField serverTF;
    @FXML
    private TextField portTF;

    @FXML
    private Canvas canvasMap;
    @FXML
    private Canvas canvasField;

    GameData gd = null;
    Image[] cachedImages = new Image[9];
    int lastI = 0, lastJ = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
        double offsetY = 50;

        gd = new GameData();

        double startX = offsetX;
        double endX = canvasMap.getWidth() - offsetX;
        double startY = offsetY;
        double endY = canvasMap.getHeight() - offsetY;
        double horizonstalDistance = (endX - startX) / gd.getPlayField().getColumns();
        double hexSize = horizonstalDistance / 0.75;
        double verticalDistance = 0.86603 * hexSize; //sqrt(3)/2*hexsize

        this.drawMap(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize);
        this.drawUnits(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize / 1.5);

        goButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String serverName = serverTF.getText(); //TODO: input control
                String port = portTF.getText();
                String name = playerTF.getText();
                int portNum = -1;

                try {
                    portNum = Integer.parseInt(port);

                    if (!ClientController.getInstance().setupConnection(serverName, portNum, name)) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Connection not possible");
                        alert.setHeaderText("I was unable to connect");
                        alert.setContentText("I am sorry... don't close me pls :(");

                        alert.showAndWait();
                    } else {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameWindowLayout.fxml"));
                            Parent root = loader.load();
                            Stage stage = new Stage();
                            stage.setTitle("Wargame");
                            stage.setScene(new Scene(root));

                            GameWindowLayoutController controller = (GameWindowLayoutController) loader.getController();
                            controller.setPrimaryStage(stage);

                            stage.show();

                            GameEngine e = new GameEngine();
                            ClientController.getInstance().setupEngine(e);
                            ClientController.getInstance().startConnection();
                            // Hide this current window
                            ((Node) (event.getSource())).getScene().getWindow().hide();
                        } catch (IOException ioe) {
                            System.err.println("JavaFX Error!");
                            System.exit(0);
                        }
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("Wrong port input!");
                } catch (IllegalArgumentException iae) {
                    System.err.println("Port number is out or range!");
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

        Timeline secondsWonder = new Timeline(new KeyFrame(Duration.seconds(0.5), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (Unit val : gd.getUnits()) {
                    int row = Playfield.convertRow(val.getCoordX(), val.getCoordZ());
                    int col = val.getCoordZ();
                    
                    col = (col+1)%gd.getPlayField().getColumns();
                    
                    val.setCoordX(Playfield.getRow(row, col));
                    val.setCoordZ(col);
                    
                    drawUnits(gd, startX, startY, horizonstalDistance, verticalDistance, hexSize / 1.5);
                }
            }
        }));
        secondsWonder.setCycleCount(Timeline.INDEFINITE);
        secondsWonder.play();
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
}
