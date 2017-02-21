package no.fredrfli.boiler;

import static spark.Spark.*;
import com.fazecast.jSerialComm.*;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: Fredrik F. Lindhagen <fred.lindh96@gmail.com>
 * @created: 15.02.2017
 */

public class Main {
    private static SerialPort btPort;
    private static SerialPort[] serialPorts;
    private static SerialPort port;

    public static void main(String[] args) {
        serialPorts = SerialPort.getCommPorts();
        String[] portNames = new String[serialPorts.length];

        for (int i = 0; i < serialPorts.length; i++) {
            if (serialPorts[i].getDescriptivePortName().equals("PLab_fredrfli-DevB")) {
                btPort = serialPorts[i];
                break;
            }
        }

        int count = 0;
        int tries = 5;
        while (!openPort()) {
            System.out.println("Trying to connect to bluetooth port: " + btPort.getDescriptivePortName());
            if (count > tries) {
                System.exit(1);
            }
            count++;
        }

        try {
            sendText("R");
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        get("/", (req, res) -> "Hello World");
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
                System.out.println("READING");
                System.out.println(s);
            }
        });

        return true;
    }

    private static void sendText(String txt) throws IOException {
        if (!btPort.isOpen()) {
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
