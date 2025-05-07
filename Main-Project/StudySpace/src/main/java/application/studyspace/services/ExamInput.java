package application.studyspace.services;

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
    private String module; // e.g. "Informatik", "Mathe", etc.
    private String examType;
    private LocalDate examDate;
    private int creditPoints;
    private int userID; // (user mapping)
    private int availableStudyDays; // how many days are left to study

    // constructor
    public ExamInput(String title, String module, String examType, LocalDate examDate, int creditPoints, int userID) {
        this.title = title;
        this.module = module;
        this.examType = examType;
        this.examDate = examDate;
        this.creditPoints = creditPoints;
        this.userID = userID;

        // calculate how many days are left
        this.availableStudyDays = calculateAvailableStudyDays();
    }

    // counts ALL calendar days between today and the exam date
    private int calculateAvailableStudyDays() {
        LocalDate today = LocalDate.now();
        int count = 0;

        while (!today.isAfter(examDate)) {
            count++; // count every
            today = today.plusDays(1);
        }

        return count;
    }

    // saves the exam to the database
    public boolean saveToDatabase() {

        String sql = "INSERT INTO exams (title, module, exam_type, exam_date, credit_points, userID, available_study_days) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, module);
            stmt.setString(3, examType);
            stmt.setDate(4, Date.valueOf(examDate));
            stmt.setInt(5, creditPoints);
            stmt.setInt(6, userID);
            stmt.setInt(7, availableStudyDays);

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
                ", module='" + module + '\'' +
                ", examType='" + examType + '\'' +
                ", examDate=" + examDate +
                ", creditPoints=" + creditPoints +
                ", userID=" + userID +
                ", availableStudyDays=" + availableStudyDays +
                '}';
    }
}
