// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import javax.microedition.lcdui.*;

public class ConnectingForm extends Form {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private Gauge status;
    private StringItem server;
    private StringItem elapsed;
    int max;

    //
    // Constructors
    //
    public ConnectingForm() {
        super("Connecting ...");
        status = new Gauge(null, false, 10, 0);

        server = new StringItem("Connecting to server ...", null);
        elapsed = new StringItem("#", null);

        append(server);
        append(elapsed);
        append(status);

        max = status.getMaxValue();
    }

    public void setRelayHost(String str) {
        server.setText("Connecting to server [" + str + "]");
    }

    public void setTick(int tick) {
        elapsed.setText(Integer.toString(++tick) + "s");
        status.setValue(tick % max);
    }
}
