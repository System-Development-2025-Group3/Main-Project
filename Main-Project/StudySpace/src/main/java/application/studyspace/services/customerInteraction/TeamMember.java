package application.studyspace.services.customerInteraction;


public class TeamMember {
    private final String name;
    private final String age;
    private final String profession;
    private final String description;
    private final String imagePath;


    public TeamMember(String name, String age, String profession, String description, String imagePath) {
        this.name = name;
        this.age = age;
        this.profession = profession;
        this.description = description;
        this.imagePath = imagePath; // This should be a *classpath-relative* path
    }


    public String getName() {
        return name;
    }


    public String getAge() {
        return age;
    }


    public String getProfession() {
        return profession;
    }


    public String getDescription() {
        return description;
    }


    public String getImagePath() {
        return imagePath;
    }
}
