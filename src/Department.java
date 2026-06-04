import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
public class Department {
    private int id;
    private String name;
    private String location;
    private Manager manager;
    private final Set<Employee> employees = new LinkedHashSet<>();

    public Department(int id,String name,String location)
    {
        if(id<=0)
        {
            throw new IllegalArgumentException("ID invalid");
        }

        this.id = id;
        this.name = name;
        this.location = location;
    }

    public int getId() {return id;}

    public Set<Employee> getEmployees() {
        return Collections.unmodifiableSet(employees);
    }

    public void addEmployee(Employee employee)
    {
        if(employee==null) return ;
        employees.add(employee);
        employee.setDepartment(this);
    }
    public void removeEmployee(Employee employee)
    {
        if(employee==null) return ;
        employees.remove(employee);
        if(employee.equals(manager))
        {
            this.manager = null;
        }
        if(employee.getDepartment()==this)
        {
            employee.setDepartment(null);
        }
    }
    public double getAverageMonthlySalary()
    {
        return this.employees.stream().mapToDouble(Employee::getSalary).average()
                .orElse(0.0);
    }
    public double getTotalMonthlySalary()
    {
        return this.employees.stream().mapToDouble(Employee::getSalary).sum();
    }
    public int getEmployeeCount()
    {
        return this.employees.size();
    }

    @Override
    public String toString()
    {
        String managerName = manager == null ? "fara manager" : manager.getFullName();
        return "Departament{id=" + id
                + ", nume='" + name + '\''
                + ", locatie='" + location + '\''
                + ", manager='" + managerName + '\''
                + ", numarAngajati=" + employees.size()
                + '}';

    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof Department that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    public String getName() {
        return name;
    }

    public void appointManager(Manager newManager) {

        if (newManager == null) {
            throw new IllegalArgumentException("Managerul nu poate fi null");
        }

        if (!employees.contains(newManager)) {
            employees.add(newManager);
        }

        if (this.manager != null) {
            System.out.println("Manager inlocuit: "
                    + this.manager.getFullName()
                    + " -> " + newManager.getFullName());
        }

        this.manager = newManager;

        newManager.setDepartment(this);
    }
    public Employee getManager() {
        return manager;
    }

    public String getLocation() {
        return location;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setLocation(String location) {
        this.location = location;
    }

}
