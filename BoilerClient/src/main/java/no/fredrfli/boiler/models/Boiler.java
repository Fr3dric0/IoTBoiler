package no.fredrfli.boiler.models;

import java.time.format.DateTimeFormatter;
import java.util.Date;
/**
 * @author: Fredrik F. Lindhagen <fred.lindh96@gmail.com>
 * @created: 27.02.2017
 */
public class Boiler {
    public boolean boiling = false;
    public String started;
    public String ended;
    public String lastBoil;
    public String error;
    public float liters;

    public Boiler(boolean boiling, String started, String ended, String lastBoil, float liters, String error) {
        this.boiling = boiling;
        this.started = started;
        this.ended = ended;
        this.lastBoil = lastBoil;
        this.error = error;
        this.liters = liters;
    }
}
