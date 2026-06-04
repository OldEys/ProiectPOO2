import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Developer extends Employee {
    private final Set<String> skills = new LinkedHashSet<>();
    public Developer(int id,String fullName, String email, double salary,Department department) {
        super(id,fullName,email,salary,department);
    }



    @Override
    public String getRole() {
        return "Developer";
    }

    public void addSkill(String skill) {
        if (skill != null && !skill.isBlank()) {
            skills.add(normalizeazaSkill(skill));
        }
    }

    public void removeSkill(String skill) {
        skills.remove(normalizeazaSkill(skill));
    }

    public boolean hasSkill(String skill) {
        return skills.contains(normalizeazaSkill(skill));
    }

    public int getSkillCount() {
        return skills.size();
    }

    public void learnSkill(String skill) {
        if (skill != null && !skill.isBlank()) {
            this.skills.add(normalizeazaSkill(skill));
        }
    }

    public void forgetSkill(String skill) {
        this.skills.remove(normalizeazaSkill(skill));
    }

    public String getSkills() {
        return skills.toString();
    }

    public Set<String> getSkillSet() {
        return Collections.unmodifiableSet(skills);
    }

    private String normalizeazaSkill(String skill) {
        return skill == null ? "" : skill.trim().toLowerCase();
    }
}
