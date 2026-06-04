import java.time.LocalDate;
import java.util.*;

enum TaskStatus{TODO, IN_PROGRESS, REVIEW, DONE,BLOCKED}
enum ProjectStatus{PLANNED,ACTIVE,ON_HOLD,COMPLETED,CANCELLED}


abstract class Employee {
    protected LocalDate hireDate;
    protected final int id;
    private String fullName;
    private String email;
    private double salary;
    private Department department;


    protected Employee(int id,String fullName,String email,double salary,Department department) {
        if(id <=0)
        {
            throw new IllegalArgumentException("ID invalid");
        }
        if (salary<=0)
        {
            throw new IllegalArgumentException("Salariu invalid");
        }

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.salary = salary;
        this.hireDate =LocalDate.now();
        this.department = department;

    }

    public abstract String getRole();
    public int getId() {return id;}
    public String getFullName() {return fullName;}
    public void setFullName(String fullName) {this.fullName = fullName;}
    public LocalDate getHireDate() {return hireDate;}
    public void setHireDate(LocalDate hireDate) {this.hireDate = hireDate;}

    @Override
    public String toString() {
        String deptName=department != null ? department.getName() : "fara departament" ;
        return getRole() + "{id=" + id
                + ", nume='" + fullName + '\''
                + ", email='" + email + '\''
                + ", salariu=" + getSalary()
                + ", departament='" + deptName + '\''
                + ", dataAngajare=" + hireDate
                + '}';

    }

    @Override
    public boolean equals(Object o)
    {
        if(this==o) return true;
        if(!(o instanceof Employee employee)) return false;
        return this.id == employee.id;
    }

    public int hashCode()
    {
        return Objects.hash(id);
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Department getDepartment() {
        return this.department;
    }

    public double getSalary() {
        return this.salary;
    }

    public double getBaseSalary() {
        return this.salary;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String newEmail) {
        email = newEmail;
    }

    public void applyRaise(double percent) {
        if (percent < 0) {
            throw new IllegalArgumentException("Procentul de marire nu poate fi negativ");
        }
        this.salary = this.salary + (percent / 100.0) * salary;
    }
}
