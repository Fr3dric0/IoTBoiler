package no.fredrfli.boiler;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import no.fredrfli.boiler.models.Boiler;

import javax.xml.soap.Text;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * @author: Fredrik F. Lindhagen <fred.lindh96@gmail.com>
 * @created: 21.02.2017
 */
public class BoilerController {
    private String host = "http://localhost:4567";
    private HashMap<String, String> paths = new HashMap<>();
    private Gson gson = new Gson();

    @FXML public Circle crclReady;
    @FXML public Circle crclBoil;
    @FXML public Circle crclProblem;

    @FXML public TextField lastBoil;
    @FXML public TextField txtError;
    @FXML public TextField txtStarted;

    @FXML
    public void initialize() {
        paths.put("boil", host + "/boil");
        paths.put("isBoiling", host + "/isboiling");
        paths.put("stats", host + "/stats");

        if (connectedToServer()) {
            crclReady.setStyle("-fx-fill: #55FF55; -fx-stroke: #333 2px;");
        }

    }

    @FXML
    public void boilHandler(ActionEvent evt) {

        Boiler boiler = boil();

        if (boiler == null) {
            txtError.setText("Problem connecting to boiler-server");
            crclProblem.setStyle("-fx-fill: #FF5555; -fx-stroke: #333 2px;");
            return;
        }

        if (boiler.error != null) {
            txtError.setText(boiler.error);
            return;
        }

        System.out.println("Started boiling");

        if (boiler.lastBoil != null) {
            lastBoil.setText(boiler.lastBoil);
        }

        if (boiler.boiling) {
            txtStarted.setText(boiler.started);
            crclReady.setStyle("-fx-fill: #DDD; -fx-stroke: #333 2px;");
            crclBoil.setStyle("-fx-fill: #55FF55; -fx-stroke: #333 2px;");
        }

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boiler = isBoiling();
        while(boiler != null && boiler.boiling) {
            boiler = isBoiling();

            if (boiler == null){
                txtError.setText("Problem with connection to server");
                crclProblem.setStyle("-fx-fill: #FF5555; -fx-stroke: #333 2px;");
            }

            try {
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (boiler == null) {
            txtError.setText("Problem with connection to server");
            crclProblem.setStyle("-fx-fill: #FF5555; -fx-stroke: #333 2px;");
        }

        if (!boiler.boiling && boiler.lastBoil != null) {
            txtStarted.setText("");
            lastBoil.setText(boiler.lastBoil);
            crclReady.setStyle("-fx-fill: #55FF55; -fx-stroke: #333 2px;");
            crclBoil.setStyle("-fx-fill: #DDD; -fx-stroke: #333 2px;");
        }

        System.out.println("Boiling complete");

    }


    private Boiler isBoiling() {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(paths.get("isBoiling"));

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setUseCaches(false);

            //int status = conn.getResponseCode();

            BufferedReader res = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = res.readLine()) != null) {
                sb.append(line);
            }

            res.close();

            conn.disconnect();
            return gson.fromJson(sb.toString(), Boiler.class);
        } catch (Exception e) {
            e.printStackTrace();

            conn.disconnect();
        }

        return null;
    }

    private Boiler boil() {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(paths.get("boil"));

            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setUseCaches(false);

            //int status = conn.getResponseCode();

            BufferedReader res = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = res.readLine()) != null) {
                sb.append(line);
            }

            res.close();

            conn.disconnect();
            return gson.fromJson(sb.toString(), Boiler.class);
        } catch (Exception e) {
            e.printStackTrace();

            conn.disconnect();
        }

        return null;
    }

    private boolean connectedToServer() {
        HttpURLConnection conn = null;
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        //conn.disconnect();
        return true;
    }

}
