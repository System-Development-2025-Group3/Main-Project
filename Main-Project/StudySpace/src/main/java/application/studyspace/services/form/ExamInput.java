package application.studyspace.services.form;

import application.studyspace.services.DataBase.DatabaseConnection;
import application.studyspace.services.DataBase.DatabaseHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;



public class ExamInput {


    public enum ExamType {
        KLAUSUR,
        REFERAT,
        ESSAY,
        PROJEKTARBEIT
    }

    private String title;
    private String examType;
    private LocalDate examDate;
    private int creditPoints;
    private UUID userUUID;

    // constructor
    public ExamInput(String title, String examType, LocalDate examDate, int creditPoints, UUID userUUID) {
        this.title = title;
        this.examType = examType;
        this.examDate = examDate;
        this.creditPoints = creditPoints;
        this.userUUID = userUUID;
    }



    // saves the exam to the database
    public boolean saveToDatabase() {
        DatabaseHelper hp = new DatabaseHelper();
        byte[] uuidBytes = hp.uuidToBytes(userUUID);

        String sql = "INSERT INTO exams (title, exam_type, exam_date, credit_points, userUUID) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, examType);
            stmt.setDate(3, Date.valueOf(examDate));
            stmt.setInt(4, creditPoints);
            stmt.setBytes(5, uuidBytes);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // print error
            System.err.println("Error saving exam: " + e.getMessage());
            return false;
        }
    }


    @Override
    public String toString() {
        return "ExamInput{" +
                "title='" + title + '\'' +
                ", examType='" + examType + '\'' +
                ", examDate=" + examDate +
                ", creditPoints=" + creditPoints +
                ", userID=" + userUUID +
                '}';
    }
}
