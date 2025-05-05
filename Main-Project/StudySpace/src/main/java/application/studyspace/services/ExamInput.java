package application.studyspace.services;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
public class ExamInput {


        public enum ExamType {
            KLAUSUR,
            REFERAT,
            ESSAY,
            PROJEKTARBEIT
        }

        private String title;
        private int creditPoints;
        private int userID;// optional: Skala 1–5
        private String examType;
        private LocalDate examDate;

        public ExamInput(String title, String examType, LocalDate examDate, int creditPoints, int userID) {
            this.title = title;
            this.examType = examType;
            this.examDate = examDate;
            this.creditPoints = creditPoints;
            this.userID = userID;
        }


        @Override
        public String toString() {
            return "ExamInput{" +
                    "title='" + title + '\'' +
                    ", examType=" + examType +
                    ", examDate=" + examDate +
                    ", creditPoints=" + creditPoints +
                    ", userID=" + userID +
                    '}';
        }

    public boolean saveToDatabase() {
        String sql = "INSERT INTO exams (title, exam_type, exam_date, credit_points, userID) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, examType);
            stmt.setDate(3, Date.valueOf(examDate));
            stmt.setInt(4, creditPoints);
            stmt.setInt(5, userID);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern des ExamInputs: " + e.getMessage());
            return false;
        }
    }








}

