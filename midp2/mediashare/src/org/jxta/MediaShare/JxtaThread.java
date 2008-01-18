// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Gauge;

public class JxtaThread extends Jxta implements Runnable {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private boolean stopPolling = false;
    private static final int DEFAULT_ALERT_TIMEOUT = 5000;

    //
    // Constructors
    //
    public JxtaThread(String ident, String host, int port, int pollInt, JxtaGui gui) {
        super(ident, host, port, pollInt, gui);
    }

    //
    // Runnable methods
    //
    public void run() {
        while (!stopPolling) {
            if (!connected && connectInitiated) {
                StatusUpdate updater = new StatusUpdate(gui);

                new Timer().scheduleAtFixedRate(updater, 1000, 1000);
                try {
                    connect();
                    updater.cancel();
                    gui.connected();
                }
                catch (Exception ex) {
                    updater.cancel();
                    ex.printStackTrace();
                    gui.showMessage("Connect",
                            "Error connecting to relay: " + ex.getMessage(),
                            AlertType.ERROR,
                            Alert.FOREVER);
                }
            }

            try {
                // keep polling until we drain all queued messages
                while (poll()) {
                }
            } catch (Throwable t) {
                t.printStackTrace();
                gui.showMessage("Poll",
                        "Error processing message: " + t.getMessage(),
                        AlertType.ERROR,
                        DEFAULT_ALERT_TIMEOUT);
            }

            try {
                // poll interval is specified in seconds
                Thread.currentThread().sleep(pollInterval * 1000);
            } catch (InterruptedException ignore) {
            }
        }
    }

    //
    // A timer task to update a gauge until canceled.
    //
    static class StatusUpdate extends TimerTask {
        private int tick = 0;
        private JxtaGui jgui;

        StatusUpdate(JxtaGui in_jgui) {
            jgui = in_jgui;
        }

        public void run() {
            jgui.setTick(++tick);
        }
    }
}
