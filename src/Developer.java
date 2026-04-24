import java.util.HashSet;
import java.util.Set;

public class Developer extends Employee {
    private Set<String> skills=new HashSet<String>();
    public Developer(int id,String fullName, String email, double salary,Department department) {
        super(id,fullName,email,salary,department);
    }



    @Override
    public String getRole() {
        return "Developer";
    }

    public void addSkill(String skill) {
        if (skill != null && !skill.isBlank()) {
            skills.add(skill);
        }
    }

    public void removeSkill(String skill) {
        skills.remove(skill);
    }

    public boolean hasSkill(String skill) {
        return skills.contains(skill);
    }

    public int getSkillCount() {
        return skills.size();
    }

    public void learnSkill(String skill) {
        this.skills.add(skill);
    }

    public void forgetSkill(String skill) {
        this.skills.remove(skill);
    }

    public String getSkills() {
        return skills.toString();
    }
}
