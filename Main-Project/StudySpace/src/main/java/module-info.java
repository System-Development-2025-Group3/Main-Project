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
    requires org.mariadb.jdbc;
    requires com.calendarfx.view;
    requires ical4j.core;
    requires org.apache.commons.text;
    requires java.prefs;

    // === Opens for FXML Reflection ===
    opens application.studyspace to javafx.fxml;
    opens application.studyspace.controllers.auth to javafx.fxml;
    opens application.studyspace.controllers.landingpage to javafx.fxml;
    opens application.studyspace.controllers.onboarding to javafx.fxml;
    opens application.studyspace.controllers.scenes to javafx.fxml;
    opens application.studyspace.services.API to javafx.fxml;
    opens application.studyspace.services.auth to javafx.fxml;
    opens application.studyspace.services.DataBase to javafx.fxml;
    opens application.studyspace.services.Styling to javafx.fxml;
    opens application.studyspace.services.Scenes to javafx.fxml;

    // === Exports for normal usage ===
    exports application.studyspace;
    exports application.studyspace.controllers.auth;
    exports application.studyspace.controllers.landingpage;
    exports application.studyspace.services.API;
    exports application.studyspace.services.auth;
    exports application.studyspace.services.DataBase;
    exports application.studyspace.services.Styling;
    exports application.studyspace.services.Scenes;
    exports application.studyspace.services.calendar;
}
