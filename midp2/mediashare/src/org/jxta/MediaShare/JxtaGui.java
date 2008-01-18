// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Gauge;

public interface JxtaGui {
    public void alert(AlertType alert);

    public void setImage(byte[] data, String caption, String sender);

    public void setTick(int tick);

    public void showMessage(String err, String msg, AlertType alert, int duration);

    public void connected();

    public String getMessage();
}