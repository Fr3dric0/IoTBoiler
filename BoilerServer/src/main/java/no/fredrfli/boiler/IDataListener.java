package no.fredrfli.boiler;

import java.util.EventListener;

/**
 * @author: Fredrik F. Lindhagen <fred.lindh96@gmail.com>
 * @created: 24.02.2017
 */
public interface IDataListener extends EventListener {
    void onData(DataEvent evt);
}
