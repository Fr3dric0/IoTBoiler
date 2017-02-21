package no.fredrfli.boiler;

import static spark.Spark.*;
import com.fazecast.jSerialComm.*;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * @author: Fredrik F. Lindhagen <fred.lindh96@gmail.com>
 * @created: 15.02.2017
 */

public class Main {
    private static SerialPort btPort;

    private static boolean connected = false;

    private static boolean boiling = false;
    private static boolean startboil = false;
    private static Date lastBoil;

    private static Date boilStart;
    private static Date boilEnd;


    public static void main(String[] args) {
        try {
            init("PLab_fredrfli-DevB");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        before((req, res) -> {
            // Blocks all connection, if bluetooth connection is broken.
            if (!connected) {
                halt(500, "Server connection problem with arduino. Please check");
                init("PLab_fredrfli-DevB"); // Try to initialize again
            }
        });

        get("/boil", (req, res) -> {
            // If Service is already boiling, send NOT OK.
            if (boiling || startboil) {
                res.status(400);
                return "NOT OK";
            }

            // Reset dates
            boilStart = null;
            boilEnd = null;

            try {
                sendText("R"); // Send the boil command
            } catch (IOException ioe) {
                halt(500, "NOT OK");
                return null;
            }

            startboil = true;
            Thread.sleep(1000); // Wait 1 sec

            try {
                sendText("B"); // Check if boiling
            } catch (IOException ioe) {
                halt(500, "NOT OK");
                return null;
            }
            return "OK";
        });

        get("/isboiling", (req, res) -> {
            try {
                sendText("B");
            } catch (IOException ioe) {
                halt(500, "arduino connection problem");
                return null;
            }

            Thread.sleep(500); // Wait 500ms.
            res.type("application/json");

            if (boiling) {
                return String.format("{\"boiling\": true, \"started\": \"%s\", \"ended\": \"%s\"}", boilStart, boilEnd);
            } else {
                return "{\"boiling\": false}";
            }
        });

        get("/stats", (req, res) -> {
            res.type("application/json");
            return String.format(
                    "{\"boiling\":%s, \"boilStart\": \"%s\", \"boilEnd\": \"%s\", \"lastBoil\": \"%s\"}",
                    boiling,
                    boilStart,
                    boilEnd,
                    lastBoil);
        });

        after((req, res) -> {
            //res.type("text/plain");
        });
    }

    private static void init(String btPortName) throws IOException {
        SerialPort[] serialPorts = SerialPort.getCommPorts();

        for (SerialPort port: serialPorts) {
            if (port.getDescriptivePortName().equals(btPortName)) {
                btPort = port;
                break;
            }
        }

        if (btPort == null) {
            throw new IOException("Could not find Bluetooth-port: " + btPortName);
        }

        int count = 0;
        int tries = 5; // Max tries
        while (!openPort()) {
            System.out.println("Trying to connect to bluetooth port: " + btPort.getDescriptivePortName());

            if (count > tries) {
                System.err.println("Connection to Arduino failed, exiting program");
                System.exit(1);
            }
            count++;
        }

        connected = true;

        try {
            sendText("H");
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }


    private static boolean openPort() {
        btPort.openPort();

        if (!btPort.isOpen()) {
            return false;
        }

        System.out.println("BLUETOOTH PORT CONNECTED");
        System.out.println(btPort.getDescriptivePortName());

        btPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

            @Override
            public void serialEvent(SerialPortEvent evt) {
                if (evt.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) { return; }

                byte[] newData = new byte[btPort.bytesAvailable()];
                int numRead = btPort.readBytes(newData, newData.length);

                String s = "";
                for (int i = 0; i < numRead; i++) {
                    s += (char) newData[i];
                }
                System.out.println("Revieced data from Arduino: " + s);
                btResponseHandler(s);
            }
        });

        return true;
    }

    private static void btResponseHandler(String res) {
        if (res == null || res.length() < 1) {
            return;
        }

        switch(res.substring(0,1)) {
            // Has started boiling
            case "0":
                boiling = true;
                startboil = false; // Boiling has been confirmed by Arduino
                boilStart = new Date();
                break;
            // Is boiling, or could not start
            case "1":
                boiling = false;
                startboil = false;
                break;

            // Is boiling
            case "2":
                boiling = true;

                break;
            // Is not boiling
            case "3":
                if (boiling) {
                    boilEnd = new Date();
                    lastBoil = boilEnd;
                }

                boiling = false;
                break;
            // PING for ensuring connection with Arduino
            case "4":
                connected = true;
                break;
            case "5":
                connected = false;
                break;
        }
    }


    private static void sendText(String txt) throws IOException {
        if (!btPort.isOpen()) {
            connected = false;
            throw new IOException("Connection to bluetooth failed");
        }

        txt += "\r\n"; // Add newline ending
        byte[] buffer = new byte[txt.length()];

        for (int i = 0; i < txt.length(); i++) {
            buffer[i] = (byte) txt.charAt(i);
        }

        try(OutputStream out = btPort.getOutputStream()) {
            out.write(buffer, 0, txt.length());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
