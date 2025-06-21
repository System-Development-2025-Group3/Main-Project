package application.studyspace.services.calendar;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents a scheduled exam with relevant data for generating personalized study plans.
 * The title now fully serves as the subject.
 */
public class ExamEvent extends CalendarEvent {
    private double gradeWeight;
    private int difficulty;
    private int priority;
    private int numberOfTopics;
    private int minutesPerTopic;
    private Duration totalEstimatedStudyTime;

    /** Constructor for new exams. calendarId must be set before saving. */
    public ExamEvent(UUID userId,
                     String title,
                     String description,
                     String location,
                     ZonedDateTime start,
                     ZonedDateTime end,
                     double gradeWeight,
                     int difficulty,
                     int numberOfTopics,
                     int minutesPerTopic) {
        super(userId, title, description, location, start, end);
        this.gradeWeight = gradeWeight;
        this.difficulty = difficulty;
        this.priority = computePriority(gradeWeight, difficulty);
        this.numberOfTopics = numberOfTopics;
        this.minutesPerTopic = minutesPerTopic;
        this.totalEstimatedStudyTime = computeStudyTime(numberOfTopics, minutesPerTopic);
    }

    /** Full constructor for loading from DB, including calendarId. */
    public ExamEvent(UUID id,
                     UUID userId,
                     UUID calendarId,
                     String title,
                     String description,
                     String location,
                     ZonedDateTime start,
                     ZonedDateTime end,
                     double gradeWeight,
                     int difficulty,
                     int numberOfTopics,
                     int minutesPerTopic) {
        super(id, userId, title, description, location, start, end, false, false, null, null, null, null, null);
        this.setCalendarId(calendarId);
        this.gradeWeight = gradeWeight;
        this.difficulty = difficulty;
        this.priority = computePriority(gradeWeight, difficulty);
        this.numberOfTopics = numberOfTopics;
        this.minutesPerTopic = minutesPerTopic;
        this.totalEstimatedStudyTime = computeStudyTime(numberOfTopics, minutesPerTopic);
    }

    // --- computation helpers ---
    private int computePriority(double weight, int difficulty) {
        return (int) Math.min(100, weight * 2 + difficulty * 5);
    }
    private Duration computeStudyTime(int topics, int minsPerTopic) {
        return Duration.ofMinutes((long) topics * minsPerTopic);
    }

    // --- getters & setters ---
    public double getGradeWeight() { return gradeWeight; }
    public void setGradeWeight(double gradeWeight) {
        this.gradeWeight = gradeWeight;
        this.priority    = computePriority(gradeWeight, this.difficulty);
    }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) {
        if (difficulty < 0 || difficulty > 10) {
            throw new IllegalArgumentException("Difficulty must be between 0 and 10.");
        }
        this.difficulty = difficulty;
        this.priority   = computePriority(this.gradeWeight, difficulty);
    }

    public int getPriority() { return priority; }

    public int getNumberOfTopics() { return numberOfTopics; }
    public void setNumberOfTopics(int numberOfTopics) {
        this.numberOfTopics          = numberOfTopics;
        this.totalEstimatedStudyTime = computeStudyTime(this.numberOfTopics, this.minutesPerTopic);
    }

    public int getMinutesPerTopic() { return minutesPerTopic; }
    public void setMinutesPerTopic(int minutesPerTopic) {
        this.minutesPerTopic         = minutesPerTopic;
        this.totalEstimatedStudyTime = computeStudyTime(this.numberOfTopics, this.minutesPerTopic);
    }

    public Duration getTotalEstimatedStudyTime() { return totalEstimatedStudyTime; }
}
