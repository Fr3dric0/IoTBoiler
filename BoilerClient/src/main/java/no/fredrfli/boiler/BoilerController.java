package no.fredrfli.boiler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;

import java.net.HttpURLConnection;

/**
 * @author: Fredrik F. Lindhagen <fred.lindh96@gmail.com>
 * @created: 21.02.2017
 */
public class BoilerController {

    @FXML public Circle crclReady;
    @FXML public Circle crclBoil;
    @FXML public Circle crclProblem;

    @FXML public TextField lastBoil;

    @FXML
    public void initialize() {

        if (connectedToServer()) {
            crclReady.setStyle("-fx-fill: #55FF55; -fx-stroke: #333 2px;");
        }

    }

    @FXML
    public void boilHandler(ActionEvent evt) {
        System.out.println("Boiling");
    }

    private boolean connectedToServer() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
