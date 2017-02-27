package no.fredrfli.boiler;

import javax.swing.event.EventListenerList;

/**
 * @author: Fredrik F. Lindhagen <fred.lindh96@gmail.com>
 * @created: 24.02.2017
 */
public class DataListener {
    protected EventListenerList listenerList = new EventListenerList();

    public void addEventListener(IDataListener listener) {
        listenerList.add(IDataListener.class, listener);
    }
    public void removeEventListener(IDataListener listener) {
        listenerList.remove(IDataListener.class, listener);
    }

    void fireEvent(DataEvent evt) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = 0; i < listeners.length; i+=2) {
            if (listeners[i] == IDataListener.class) {
                ((IDataListener) listeners[i+1]).onData(evt);
            }
        }
    }
}
