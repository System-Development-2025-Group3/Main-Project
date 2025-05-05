module application.studyspace {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires org.json;

    opens application.studyspace to javafx.fxml;
    exports application.studyspace;
    exports application.studyspace.controllers.auth;
    opens application.studyspace.controllers.auth to javafx.fxml;
    exports application.studyspace.services;
    opens application.studyspace.services to javafx.fxml;
    exports application.studyspace.controllers.landingpage;
    opens application.studyspace.controllers.landingpage to javafx.fxml;
}
