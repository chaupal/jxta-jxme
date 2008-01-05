package net.jxta.impl.peergroup;

import com.sun.java.util.collections.HashMap;
import com.sun.java.util.collections.Iterator;
import net.jxta.document.*;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.id.ID;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.util.java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.Enumeration;

public class StdPeerGroupParamAdv {
    private static final Logger LOG = Logger.getInstance(StdPeerGroupParamAdv.class.getName());

    private static final String paramTag = "Parm";
    private static final String protoTag = "Proto";
    private static final String appTag = "App";
    private static final String svcTag = "Svc";
    private static final String mcidTag = "MCID";
    private static final String msidTag = "MSID";

    private static final String miaTag = ModuleImplAdvertisement.getAdvertisementType();

    // In the future we should be able to manipulate all modules regardless
    // of their kind, but right now it helps to keep them categorized
    // as follows.

    private HashMap servicesTable = null;
    private HashMap protosTable = null;
    private HashMap appsTable = null;

    public StdPeerGroupParamAdv() {
        // set defaults
        servicesTable = new HashMap();
        protosTable = new HashMap();
        appsTable = new HashMap();
    }

    public StdPeerGroupParamAdv(Element root) throws PeerGroupException {
        try {
            initialize(root);
        } catch (Throwable any) {
            throw new PeerGroupException(any.getMessage());
        }
    }

    public HashMap getServices() {
        return servicesTable;
    }

    public HashMap getProtos() {
        return protosTable;
    }

    public HashMap getApps() {
        return appsTable;
    }


    public void setServices(HashMap servicesTable) {
        if (servicesTable == null) this.servicesTable = new HashMap();
        else this.servicesTable = servicesTable;
    }

    public void setProtos(HashMap protosTable) {
        if (protosTable == null) this.protosTable = new HashMap();
        else this.protosTable = protosTable;
    }

    public void setApps(HashMap appsTable) {
        if (appsTable == null) this.appsTable = new HashMap();
        else this.appsTable = appsTable;
    }

    public void initialize(Element root) throws Exception {

        if (!TextElement.class.isInstance(root))
            throw new IllegalArgumentException(getClass().getName() + " only supports TextElement");

        TextElement doc = (TextElement) root;

        if (! doc.getName().equals(paramTag))

            throw new IllegalArgumentException("Could not construct : "
                    + getClass().getName()
                    + "from doc containing a "
                    + doc.getName());

        // set defaults

        servicesTable = new HashMap();
        protosTable = new HashMap();
        appsTable = new HashMap();

        int appCount = 0;
        Enumeration modules = doc.getChildren();
        while (modules.hasMoreElements()) {
            HashMap theTable;

            TextElement module = (TextElement) modules.nextElement();
            String tagName = module.getName();
            if (tagName.equals(svcTag)) {
                theTable = servicesTable;
            } else if (tagName.equals(appTag)) {
                theTable = appsTable;
            } else if (tagName.equals(protoTag)) {
                theTable = protosTable;
            } else continue;

            ModuleSpecID specID = null;
            ModuleClassID classID = null;
            ModuleImplAdvertisement inLineAdv = null;

            try {
                if (module.getTextValue() != null) {
                    specID = (ModuleSpecID)
                             ID.create(URI.create(module.getTextValue()));
                }

                // Check for children anyway.
                Enumeration fields = module.getChildren();
                while (fields.hasMoreElements()) {
                    TextElement field = (TextElement) fields.nextElement();
                    if (field.getName().equals(mcidTag)) {
                        classID = (ModuleClassID)
                                  ID.create(URI.create(field.getTextValue()));
                        continue;
                    }
                    if (field.getName().equals(msidTag)) {
                        specID = (ModuleSpecID)
                                 ID.create(URI.create(field.getTextValue()));
                        continue;
                    }
                    if (field.getName().equals(miaTag)) {
                        inLineAdv = (ModuleImplAdvertisement)
                                AdvertisementFactory.newAdvertisement(field);
                    }
                }
            } catch (Exception any) {
                if (LOG.isEnabledFor(Priority.WARN)) LOG.warn("Broken entry; skipping", any);
                continue;
            }

            if (inLineAdv == null && specID == null) {
                if (LOG.isEnabledFor(Priority.WARN)) LOG.warn("Insufficent entry; skipping");
                continue;
            }

            Object theValue;
            if (inLineAdv == null) {
                theValue = specID;
            } else {
                specID = inLineAdv.getModuleSpecID();
                theValue = inLineAdv;
            }
            if (classID == null) {
                classID = specID.getBaseClass();
            }

            // For applications, the role does not matter. We just create
            // a unique role ID on the fly.
            // When outputing the add we get rid of it to save space.

            if (theTable == appsTable) {
                // Only the first (or only) one may use the base class.
                if (appCount++ != 0) {
                    classID = IDFactory.newModuleClassID(classID);
                }
            }
            theTable.put(classID, theValue);
        }
    }

    public Document getDocument(MimeMediaType encodeAs) {
        StructuredTextDocument doc;

        doc = (StructuredTextDocument)
                StructuredDocumentFactory.newStructuredDocument(encodeAs,
                        paramTag);

        outputModules(doc, servicesTable, svcTag, encodeAs);
        outputModules(doc, protosTable, protoTag, encodeAs);
        outputModules(doc, appsTable, appTag, encodeAs);
        return doc;
    }

    private void outputModules(StructuredTextDocument doc, HashMap modulesTable, String mainTag, MimeMediaType encodeAs) {

        Iterator allClasses = modulesTable.keySet().iterator();
        while (allClasses.hasNext()) {
            ModuleClassID mcid = (ModuleClassID) allClasses.next();
            Object val = modulesTable.get(mcid);

            // For applications, we ignore the role ID. It is not meaningfull,
            // and a new one is assigned on the fly when loading this adv.

            if (val instanceof Advertisement) {
                TextElement m = doc.createElement(mainTag);
                doc.appendChild(m);

                if (!(modulesTable == appsTable
                        ||
                        mcid.equals(mcid.getBaseClass()))) {
                    // It is not an app and there is a role ID. Output it.

                    TextElement i = doc.createElement(mcidTag, mcid.toString());
                    m.appendChild(i);
                }

                StructuredTextDocument advdoc = (StructuredTextDocument)
                        ((Advertisement) val).getDocument(encodeAs);

                StructuredDocumentUtils.copyElements(doc, m, advdoc);

            } else if (val instanceof ModuleSpecID) {
                TextElement m;

                if (modulesTable == appsTable
                        || mcid.equals(mcid.getBaseClass())) {

                    // Either it is an app or there is no role ID.
                    // So the specId is good enough.
                    m = doc.createElement(mainTag,
                            val.toString());
                    doc.appendChild(m);
                } else {
                    // The role ID matters, so the classId must be separate.
                    m = doc.createElement(mainTag);
                    doc.appendChild(m);

                    TextElement i;
                    i = doc.createElement(mcidTag, mcid.toString());
                    m.appendChild(i);

                    i = doc.createElement(msidTag,
                            val.toString());
                    m.appendChild(i);
                }
            } else {
                if (LOG.isEnabledFor(Priority.WARN)) LOG.warn("unsupported class in modules table");
            }
        }
    }
}
