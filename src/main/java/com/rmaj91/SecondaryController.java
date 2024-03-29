package com.rmaj91;

import java.net.URL;
import java.util.ResourceBundle;

import com.rmaj91.model.Ball;
import com.rmaj91.model.Simulation;
import com.rmaj91.model.SimulationEditor;
import com.rmaj91.repository.BallRepo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class SecondaryController implements Initializable {

    public static double ballRadius = 20;
    public static final double MIN_BALL_RADIUS = 5;
    public static final double MAX_BALL_RADIUS = 100;
    public static final int velocityMultiplier = 8;

    //=============================================================================================
    // JavaFX elements
    //=============================================================================================
    @FXML
    private Canvas simulatorCanvas;
    @FXML
    private Canvas simulatorEditorCanvas;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button simulationBtn;
    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane background;
    @FXML
    private Label ballCoordinatesLbl;
    @FXML
    private Label cursorCoordinatesLbl;
    @FXML
    private Label radiusLbl;
    @FXML
    private Label speedVectorLbl;
    @FXML
    private Spinner<Integer> earthAcceleration;

    //=============================================================================================
    // Properties
    //=============================================================================================
    private BallRepo ballRepo;
    private Ball currentBall;
    private Simulation simulation;
    private SimulationEditor simulationEditor;

    private double xFromClick;
    private double yFromClick;


    //=============================================================================================
    // EVENTS METHODS
    //=============================================================================================
    @FXML
    private void createBall(MouseEvent event) {
        xFromClick = event.getSceneX();
        yFromClick = -(event.getSceneY() - simulatorCanvas.getHeight());
        if (event.getButton().name().equals("PRIMARY") && !isBallHere(xFromClick, yFromClick)) {
            xFromClickCorrect();
            radiusLbl.setText(String.format("%.1f px", ballRadius));
            speedVectorLbl.setText("0.0, 0.0");
            ballCoordinatesLbl.setText(String.format("%.1f, %.1f", xFromClick, yFromClick));
            Color color = colorPicker.getValue();
            currentBall = new Ball(xFromClick, yFromClick, ballRadius, color);
            simulationEditor.add(currentBall);
        } else if (event.getButton().name().equals("SECONDARY")) {
            simulationEditor.remove(xFromClick, yFromClick);
        }
        simulationEditor.clearView();
        simulationEditor.drawAll();
    }

    @FXML
    private void displayCoordinates(MouseEvent event) {
        double yCoordinate = -(event.getSceneY() - simulatorCanvas.getHeight());
        cursorCoordinatesLbl.setText(String.format("%.1f, %.1f", event.getSceneX(), yCoordinate));
    }

    @FXML
    private void setBallRadius(KeyEvent event) {
        String kChar = event.getText();
        if (kChar.equals(" "))
            simulationBtnClicked();
        else if (kChar.equalsIgnoreCase("W") && ballRadius < MAX_BALL_RADIUS)
            ballRadius++;
        else if (kChar.equalsIgnoreCase("S") && ballRadius > MIN_BALL_RADIUS)
            ballRadius--;

        if (currentBall != null) {
            currentBall.setRadius(ballRadius);
            simulationEditor.clearView();
            simulationEditor.drawAll();
        }
        radiusLbl.setText(String.format("%.1f px", ballRadius));
    }

    @FXML
    private void setBallVelocity(MouseEvent event) {
        if (currentBall != null) {

            double velocityX = xFromClick - event.getSceneX();
            double velocityY = yFromClick + event.getSceneY() - simulatorCanvas.getHeight();

            currentBall.setVx(velocityX * velocityMultiplier);
            currentBall.setVy(velocityY * velocityMultiplier);
            speedVectorLbl.setText(String.format("%.1f, %.1f", velocityX*velocityMultiplier, velocityY*velocityMultiplier));
            simulationEditor.clearView();
            simulationEditor.drawAll();
            simulationEditor.drawVector(currentBall);
        }
    }

    @FXML
    private void clearBallRef() {
        if (simulation.isSimulationFlag() && currentBall != null) {
            simulation.add(currentBall);
            simulationEditor.clearView();
            simulationEditor.clearRepo();
        } else if (currentBall != null) {
        }
        currentBall = null;
    }

    @FXML
    private void resetBtnClicked() {
        simulationEditor.clearRepo();
        simulationEditor.clearView();
        simulation.clearRepo();
        simulation.clearView();
    }

    @FXML
    private void simulationBtnClicked() {
        if (simulation.isSimulationFlag()) {
            simulation.stop();
            simulationBtn.setText("Start Simulation");
        } else {
            simulation.addAll(simulationEditor.getBallRepo());
            simulationEditor.clearView();
            simulationEditor.clearRepo();
            simulation.start();
            simulationBtn.setText("Stop Simulation");
        }
    }

    //=============================================================================================
    // Initialization
    //=============================================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        App.stage.setWidth(1280);
        App.stage.setHeight(600);
        App.stage.setResizable(true);
        simulationEditor = new SimulationEditor(simulatorEditorCanvas);
        ballRepo = new BallRepo();
        simulation = new Simulation(ballRepo, simulatorCanvas);
        setBackgroundSizeListeners();
        setEarthAccValueFactory();
        colorPicker.setValue(Color.DARKGREY);
        eartAccSpinnerListenerInit();
    }

    //=============================================================================================
    // Private Methods
    //=============================================================================================
    private boolean isBallHere(double xFromClick, double yFromClick) {
        BallRepo ballRepo = simulationEditor.getBallRepo();
        boolean isBallFound = false;
        for (Ball ball : ballRepo.getBalls()) {
            double x1 = ball.getPx();
            double y1 = ball.getPy();
            double distance = Math.sqrt((x1 - xFromClick) * (x1 - xFromClick) + (y1 - yFromClick) * (y1 - yFromClick));
            if (ball.getRadius() + ballRadius > distance) {
                isBallFound = true;
                break;
            }
        }
        return isBallFound;
    }

    private void eartAccSpinnerListenerInit() {
        earthAcceleration.valueProperty().addListener(e -> {
            Ball.EARTH_ACC = earthAcceleration.getValue();
        });
    }
    private void xFromClickCorrect() {
        if (xFromClick - ballRadius < 0)
            xFromClick = ballRadius;
        if (xFromClick + ballRadius > simulatorCanvas.getWidth())
            xFromClick = simulatorCanvas.getWidth() - ballRadius;
        if (yFromClick < ballRadius)
            yFromClick = ballRadius;
    }
    private void setBackgroundSizeListeners() {
        background.heightProperty().addListener(e -> {
            double height = background.getHeight();
            simulatorCanvas.setHeight(height);
            simulatorEditorCanvas.setHeight(height);
            ballRepo.clear();
            simulation.clearView();
        });

        background.widthProperty().addListener(e -> {
            double width = background.getWidth();
            simulatorCanvas.setWidth(width);
            simulatorEditorCanvas.setWidth(width);
            ballRepo.clear();
            simulation.clearView();
        });
    }

    private void setEarthAccValueFactory() {
        SpinnerValueFactory.IntegerSpinnerValueFactory svf
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(-10000, 10000, Ball.EARTH_ACC, 25);
        earthAcceleration.setValueFactory(svf);
    }

}