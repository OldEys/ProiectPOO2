public class Manager extends Employee {
    private int teamSize;
    private double bonus;

    public Manager(int id, String fullName, String email, double salary, Department department, int teamSize,double bonus) {
        super(id, fullName, email, salary,department);
        if(teamSize < 0) {
            throw new IllegalArgumentException("Dimensiunea echipei este invalida");
        }
        if (bonus < 0) {
            throw new IllegalArgumentException("Bonusul nu poate fi negativ");
        }
        this.teamSize = teamSize;
        this.bonus = bonus;
    }

    @Override
    public double getSalary() {
        return super.getSalary() +  bonus;
    }


    @Override
    public String getRole() {
        return "Manager";
    }


    public int getTeamSize() {
        return teamSize;
    }

    public double getBonus() {
        return bonus;
    }

    public boolean canApproveBudget(double amount) {
        return amount <= 50000;
    }

    public void increaseTeamSize() {
        this.teamSize += 1;
    }

    public void decreaseTeamSize() {
        if (this.teamSize == 0) {
            return;
        }
        this.teamSize -= 1;
    }

    public void setManagementBonus(double bonus) {
        if (bonus < 0) {
            throw new IllegalArgumentException("Bonusul nu poate fi negativ");
        }
        this.bonus=bonus;
    }
}
