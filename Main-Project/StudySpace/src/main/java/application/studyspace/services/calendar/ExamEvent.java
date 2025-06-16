package application.studyspace.services.calendar;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents a scheduled exam with relevant data for generating personalized study plans.
 * <p>
 * This class includes information such as subject, difficulty, grade weight,
 * and estimated study time, and can be used by the Smart Study Planner to
 * calculate study effort and manage prioritization.
 */
public class ExamEvent {

    private final UUID id;
    private final UUID userId;
    private String title;
    private String subject;
    private String description;
    private String location;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private double gradeWeight;
    private int difficulty;
    private int priority;
    private boolean hidden;
    private int numberOfTopics;
    private int minutesPerTopic;
    private Duration totalEstimatedStudyTime;
    /**
     * Constructs a new {@code ExamEvent} with all relevant study planning information.
     * <p>
     * This constructor calculates the study priority and total estimated study time
     * based on the provided exam attributes such as difficulty, grade weight,
     * number of topics, and minutes per topic.
     *
     * @param userId             The unique identifier of the user associated with this exam.
     * @param title              The title or name of the exam (e.g., "Biology Final").
     * @param subject            The subject related to the exam (e.g., "Biology", "History").
     * @param description        Optional text description providing additional details about the exam.
     * @param location           The location where the exam will take place (e.g., classroom or online link).
     *
     * @param gradeWeight        The weight of the exam as a percentage of the final grade (e.g., 25.0).
     * @param difficulty         The estimated difficulty of the exam (0 = easy, 10 = hard). Must be in the range [0–10].
     * @param numberOfTopics     The number of distinct topics the student needs to study for this exam.
     * @param minutesPerTopic    The estimated amount of time in minutes required to study each topic.
     */
    // Constructor for new exams
    public ExamEvent(UUID userId,
                     String title,
                     String subject,
                     String description,
                     String location,
                     ZonedDateTime start,
                     ZonedDateTime end,
                     double gradeWeight,
                     int difficulty,
                     int numberOfTopics,
                     int minutesPerTopic) {
        this.id             = UUID.randomUUID();
        this.userId         = userId;
        this.title          = title;
        this.subject        = subject;
        this.description    = description;
        this.location       = location;
        this.start            = start;
        this.end              = end;
        this.gradeWeight    = gradeWeight;
        this.difficulty     = difficulty;
        this.priority       = computePriority(gradeWeight, difficulty);
        this.numberOfTopics = numberOfTopics;
        this.minutesPerTopic = minutesPerTopic;
        this.totalEstimatedStudyTime = computeStudyTime(numberOfTopics, minutesPerTopic);

    }

    //returns the priority score based on weight and difficulty
    private int computePriority(double weight, int difficulty) {
        return (int) Math.min(100, weight * 2 + difficulty * 5);
    }
    //returns the total estimated study time in minutes
    private Duration computeStudyTime(int numberOfTopics, int minutesPerTopic) {
        return Duration.ofMinutes((long) numberOfTopics * minutesPerTopic);
    }

    // Getters and Setters
    public UUID getId() { return id; }

    public UUID getUserId() { return userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public ZonedDateTime getStart() { return start; }
    public void setStart(ZonedDateTime start) { this.start = start; }
    public ZonedDateTime getEnd() { return end; }
    public void setEnd(ZonedDateTime end) { this.end = end; }

    public double getGradeWeight() { return gradeWeight; }
    public void setGradeWeight(double gradeWeight) {
        this.gradeWeight = gradeWeight;
        this.priority = computePriority(gradeWeight, this.difficulty);
    }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) {
        if (difficulty < 0 || difficulty > 10) {
            throw new IllegalArgumentException("Difficulty must be between 0 and 10.");
        }
        this.difficulty = difficulty;
        this.priority = computePriority(this.gradeWeight, difficulty);
    }

    public int getPriority() { return priority; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    public int getNumberOfTopics() { return numberOfTopics; }
    public void setNumberOfTopics(int numberOfTopics) {
        this.numberOfTopics = numberOfTopics;
        this.totalEstimatedStudyTime = computeStudyTime(this.numberOfTopics, this.minutesPerTopic);
    }

    public int getMinutesPerTopic() { return minutesPerTopic; }
    public void setMinutesPerTopic(int minutesPerTopic) {
        this.minutesPerTopic = minutesPerTopic;
        this.totalEstimatedStudyTime = computeStudyTime(this.numberOfTopics, this.minutesPerTopic);
    }

    public Duration getTotalEstimatedStudyTime() { return totalEstimatedStudyTime; }
}
