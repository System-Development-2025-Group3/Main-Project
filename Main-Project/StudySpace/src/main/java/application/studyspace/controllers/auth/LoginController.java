package application.studyspace.controllers.auth;

import application.studyspace.services.DataBase.DatabaseHelper;
import application.studyspace.services.DataBase.SkipSplashScreenManager;
import application.studyspace.services.Scenes.ViewManager;
import application.studyspace.services.auth.ValidationUtils;
import application.studyspace.services.auth.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.util.UUID;

import static application.studyspace.services.auth.AutoLoginHandler.activateAutoLogin;

public class LoginController {

    @FXML private TextField InputEmailTextfield;
    @FXML private PasswordField InputPassword;
    @FXML private CheckBox stayLoggedInCheckbox;

    @FXML
    private void handleRegisterTextClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/auth/Register.fxml");
    }

    @FXML
    private void handleSubmitLoginButtonClick(MouseEvent event) throws SQLException {
        String email = InputEmailTextfield.getText();
        String pw    = InputPassword.getText();
        ValidationUtils.ValidationResult result = ValidationUtils.validateLogin(email, pw);

        switch (result) {
            case EMPTY_EMAIL -> {
                // Optionally, display error feedback here
                InputEmailTextfield.setStyle("-fx-border-color: red;");
            }
            case INVALID_EMAIL -> {
                InputEmailTextfield.setStyle("-fx-border-color: red;");
            }
            case UNKNOWN_EMAIL -> {
                InputEmailTextfield.setStyle("-fx-border-color: red;");
            }
            case EMPTY_PASSWORD -> {
                InputPassword.setStyle("-fx-border-color: red;");
            }
            case INVALID_CREDENTIALS -> {
                InputPassword.setStyle("-fx-border-color: red;");
            }
            case OK -> {
                DatabaseHelper dbHelper = new DatabaseHelper();
                UUID userUUID = dbHelper.getUserUUIDByEmail(email);
                SessionManager.getInstance().login(userUUID);
                if (stayLoggedInCheckbox.isSelected()) {
                    activateAutoLogin(userUUID.toString());
                }
                ViewManager.show("/application/studyspace/landingpage/Landing-Page.fxml");
                ViewManager.showOverlay("/application/studyspace/onboarding/OnboardingPage1.fxml", null);

                boolean skipSplashDatabase = new SkipSplashScreenManager()
                        .loadSkipSplashScreenFromDatabase(userUUID);
                SessionManager.getInstance().setSkipSplashScreen(skipSplashDatabase);
            }
        }
    }

    @FXML
    public void handleAboutUsClick(MouseEvent event) {
        ViewManager.show("/application/studyspace/CustomerInteraction/AboutUs.fxml");
    }
}
