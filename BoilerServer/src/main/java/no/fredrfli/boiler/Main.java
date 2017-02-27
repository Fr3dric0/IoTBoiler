package no.fredrfli.boiler;

import static spark.Spark.*;
import com.fazecast.jSerialComm.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * @author: Fredrik F. Lindhagen <fred.lindh96@gmail.com>
 * @created: 15.02.2017
 */

public class Main {
    private static SerialPort btPort;
    private static String btName = "PLab_fredrfli-DevB";
    private static boolean connected = false;

    private static boolean boiling = false;
    private static boolean startboil = false;
    private static Date lastBoil;

    private static Date boilStart;
    private static Date boilEnd;
    private static double liter;



    //private static DataListener onBoil = new DataListener();

    public static void main(String[] args) {
        try {
            init(btName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        before((req, res) -> {
            // Blocks all connection, if bluetooth connection is broken.
            if (!connected) {
                halt(500, "{\"error\": \"Server connection problem with arduino. Please check bluetooth chip\"}");
                init(btName); // Try to initialize again
            }
        });

        get("/boil", (req, res) -> {
/*
            if (boiling && boilStart == null) {
                boiling = false;
                sendText("B"); // Update boiling
                return "{\"boiling\": false}";
            }*/

            // If Service is already boiling, send stats
            if (boiling || startboil) {
                res.status(400);
                return String.format("{\"boiling\": true, \"started\": \"%s\"}", boilStart);
            }

            // Reset dates
            boilStart = null;
            boilEnd = null;

            try {
                sendText("R"); // Send the boil command
            } catch (IOException ioe) {
                halt(500, String.format("{\"error\": \"%s\"}", ioe.getMessage()));
                return null;
            }

            startboil = true;
            Thread.sleep(1000); // Wait 1 sec

            try {
                sendText("B"); // Check if boiling
            } catch (IOException ioe) {
                halt(500, "{\"boiling\": false, \"error\": \"" + ioe.getMessage() + "\"}");
                return null;
            }

            if (boiling) {
                return boilStart != null ?
                        "{\"boiling\": true}" :
                        String.format("{\"boiling\": true, \"started\": \"%s\"}", boilStart);
            } else {
                return "{\"boiling\": false}";
            }
        });

        get("/isboiling", (req, res) -> {
            try {
                sendText("B");
            } catch (IOException ioe) {
                halt(500, "{\"error\": \""+ioe.getMessage()+"\"}");
                return null;
            }

            Thread.sleep(500); // Wait 500ms.

            if (boiling) {
                return String.format("{\"boiling\": true, \"started\": \"%s\", \"ended\": \"%s\", \"liter\": %s}", boilStart, boilEnd, liter);
            } else {
                if (lastBoil != null) {
                    return "{\"boiling\": false, \"lastBoil\": \""+lastBoil+"\", \"liter\": "+liter+"}";
                }

                return "{\"boiling\": false}";
            }
        });

        get("/stats", (req, res) -> {
            res.type("application/json");
            return String.format(
                    "{\"boiling\":%s, \"started\": \"%s\", \"ended\": \"%s\", \"lastBoil\": \"%s\", \"liter\": %s}",
                    boiling,
                    boilStart,
                    boilEnd,
                    lastBoil,
                    liter
            );
        });

        after((req, res) -> {
            res.type("application/json");
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

        System.out.println("Bluetooth connection successful (" + btPort.getDescriptivePortName() + ")");

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
                System.out.println("Boil started " + boilStart);
                break;
            // Is boiling, or could not start
            case "1":
                System.out.println("Still boiling");
                break;

            // Is boiling
            case "2":
                boiling = true;

                break;
            // Is not boiling
            case "3":
                if (boiling) { // Save state and date
                    boilEnd = new Date();
                    lastBoil = boilEnd;
                    liter = getLiter(boilStart, boilEnd);
                    boilStart = null;
                    System.out.println("Boil ended " + boilEnd);
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

    private static double getLiter(Date start, Date end) {
        // f(x:duration) = -2.48 + 0.7 ln(x)
        double b = -2.48;
        double a = 0.7;
        int duration = (int) (end.getTime() - start.getTime());

        double liter = b + a * Math.log(duration/1000);
        return liter > 0 ? liter : 0;
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
