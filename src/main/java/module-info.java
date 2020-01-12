module bsaf {
    requires java.logging;

    requires transitive java.datatransfer;
    requires transitive java.desktop;

    exports org.jdesktop.application;
    exports org.jdesktop.application.session;
    exports org.jdesktop.application.utils;

    // must be opened if resources are loaded from classloader as in ResourceManager
    opens org.jdesktop.application.resources.icons;

}
