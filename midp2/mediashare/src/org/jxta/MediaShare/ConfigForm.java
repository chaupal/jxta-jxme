// Copyright 2006 E. Mike Durbin / Sun Microsystems, Inc.
//
package org.jxta.MediaShare;

import org.jxta.MidletCommon.*;

import javax.microedition.lcdui.*;

public class ConfigForm extends Form {
    //
    // Public Stuff
    //

    //
    // Private Stuff
    //
    private TextField relayHostField;
    private TextField relayPortField;
    private TextField identityField;
    private Command saveCommand;

    //
    // Constructors
    //
    public ConfigForm(CommandListener list, String title, BaseMidlet midlet) {
        super("Configuration Form");
        setCommandListener(list);

        relayHostField = new TextField("Relay Host", null, 20, 0);
        relayPortField = new TextField("Relay Port", null, 6, 0);
        identityField = new TextField("Identity", null, 20, 0);

        append(relayHostField);
        append(relayPortField);
        append(identityField);

        saveCommand = new Command("Save", Command.OK, 1);
        addCommand(saveCommand);
    }

    public void setData(String rh, int rp, String id) {
        relayHostField.setString(rh);
        relayPortField.setString("" + rp);
        identityField.setString(id);
        return;
    }

    public String getRelayHost() {
        return (relayHostField.getString());
    }

    public int getRelayPort() {
        return (Integer.parseInt(relayPortField.getString()));
    }

    public String getIdentity() {
        return (identityField.getString());
    }
}
