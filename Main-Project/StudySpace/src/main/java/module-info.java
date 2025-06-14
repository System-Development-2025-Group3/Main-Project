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

    opens application.studyspace to javafx.fxml;
    exports application.studyspace;
    exports application.studyspace.controllers.auth;
    opens application.studyspace.controllers.auth to javafx.fxml;
    exports application.studyspace.controllers.landingpage;
    opens application.studyspace.controllers.landingpage to javafx.fxml;
    exports application.studyspace.services.API;
    opens application.studyspace.services.API to javafx.fxml;
    exports application.studyspace.services.auth;
    opens application.studyspace.controllers.onboarding to javafx.fxml;
    opens application.studyspace.services.auth to javafx.fxml;
    exports application.studyspace.services.DataBase;
    opens application.studyspace.services.DataBase to javafx.fxml;
    exports application.studyspace.services.Styling;
    opens application.studyspace.services.Styling to javafx.fxml;
    exports application.studyspace.services.Scenes;
    opens application.studyspace.services.Scenes to javafx.fxml;
    exports application.studyspace.services.calendar;


}
