import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public static void seedTestData(CompanyManagementService service) {

        Company company = new Company("TechCorp");

        Department it = new Department(1, "IT","Bucharest");
        Department hr = new Department(2, "HR","Cluj");

        service.addDepartment(it);
        service.addDepartment(hr);

        Client client1 = new Client(1, "Google", "contact@google.com","IT");
        Client client2 = new Client(2, "Amazon", "contact@amazon.com","Retail");

        service.addClient(client1);
        service.addClient(client2);

        Developer dev1 = new Developer(1, "Alice", "alice@corp.com", 5000,it);
        dev1.learnSkill("Java");
        dev1.learnSkill("Spring");

        Developer dev2 = new Developer(2, "Bob", "bob@corp.com", 4500,hr);
        dev2.learnSkill("Python");

        Manager manager1 = new Manager(3, "Charlie", "charlie@corp.com", 7000, it, 5, 1000);

        service.hireEmployee(dev1, 1);
        service.hireEmployee(dev2, 1);
        service.hireEmployee(manager1, 1);

        service.appointManager(1, 3);

        Task t1 = new Task(1, "Backend API", "API Testing",LocalDate.of(2026, 12, 31),15);
        Task t2 = new Task(2, "Database design", "Integration",LocalDate.of(2026, 12, 31),20);
        Task t3 = new Task(3, "ML Model", "New model",LocalDate.of(2026, 1, 31),4);

        Project project1 = new Project(1, "AI Platform","Powerful AI Agent" ,it,client1,t1,1000,LocalDate.of(2026, 12, 31));
        project1.addRequiredSkill("Java");
        project1.addRequiredSkill("Spring");

        Project project2 = new Project(2, "DB integration","Postgre integration" ,it,client2,t2,1500,LocalDate.of(2026, 12, 31));
        project2.addRequiredSkill("Python");

        service.addProject(project1);
        service.addProject(project2);

        service.assignEmployeeToProject(1, 1);
        service.assignEmployeeToProject(2, 1);
        service.assignEmployeeToProject(3, 1);

        service.assignEmployeeToProject(2, 2);


        service.addTaskToProject(1, t1);
        service.addTaskToProject(1, t2);
        service.addTaskToProject(2, t3);

        service.assignTaskToEmployee(1, 1, 1);
        service.assignTaskToEmployee(1, 2, 2);
        service.assignTaskToEmployee(2, 3, 2);
        departments.put(it.getId(), it);
        departments.put(hr.getId(), hr);

        clients.put(client1.getId(), client1);
        clients.put(client2.getId(), client2);

        employees.put(dev1.getId(), dev1);
        employees.put(dev2.getId(), dev2);
        employees.put(manager1.getId(), manager1);

        projects.put(project1.getId(), project1);
        projects.put(project2.getId(), project2);
    }
    private static final Scanner SCANNER = new Scanner(System.in);

    private static Company company;
    private static CompanyManagementService service;

    private static final Map<Integer, Department> departments = new LinkedHashMap<>();
    private static final Map<Integer, Employee> employees = new LinkedHashMap<>();
    private static final Map<Integer, Project> projects = new LinkedHashMap<>();
    private static final Map<Integer, Client> clients = new LinkedHashMap<>();
    public static void main(String[] args) {
        titleMenu();
//        company = new Company("TechCorp");
//        service = new CompanyManagementService(company);
//        seedTestData(service);

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Alege optiunea: ");

            switch (choice) {
                case 1 -> companyMenu();
                case 2 -> departmentMenu();
                case 3 -> employeeMenu();
                case 4 -> clientMenu();
                case 5 -> projectMenu();
                case 6 -> taskMenu();
                case 7 -> analyticsMenu();
                case 0 -> running = false;
                default -> println("Optiune invalida.");
            }
        }

        println("Iesire...");
    }
    private static void titleMenu() {
        println("=== Company Management System ===");
        String companyName = readNonEmpty("Numele companiei: ");
        company = new Company(companyName);
        service = new CompanyManagementService(company);
        println("Compania a fost creata: " + companyName);
    }

    private static void printMainMenu() {
        println("");
        println("========== MENIU PRINCIPAL ==========");
        println("1. Company");
        println("2. Departments");
        println("3. Employees");
        println("4. Clients");
        println("5. Projects");
        println("6. Tasks");
        println("7. Analytics / Business logic");
        println("0. Exit");
        println("=====================================");
    }

    private static void companyMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- COMPANY MENU ---------");
            println("1. Afiseaza compania");
            println("2. Afiseaza summary");
            println("3. Optimizeaza alocarea resurselor");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> println(company.toString());
                case 2 -> service.printSummary();
                case 3 -> {
                    company.optimizeResourceAllocation();
                    println("Optimizare executata.");
                }
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void departmentMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- DEPARTMENTS ---------");
            println("1. Adauga departament");
            println("2. Afiseaza departamente");
            println("3. Redenumeste departament");
            println("4. Schimba locatia departamentului");
            println("5. Atribuie manager");
            println("6. Detalii departament");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> addDepartmentFlow();
                case 2 -> listDepartments();
                case 3 -> renameDepartmentFlow();
                case 4 -> relocateDepartmentFlow();
                case 5 -> appointManagerFlow();
                case 6 -> showDepartmentDetailsFlow();
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void employeeMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- EMPLOYEES ---------");
            println("1. Adauga Manager");
            println("2. Adauga Developer");
            println("3. Afiseaza angajati");
            println("4. Redenumeste angajat");
            println("5. Schimba email");
            println("6. Aplica marire salariala");
            println("7. Transfera angajat Intre departamente");
            println("8. Evalueaza performanta angajatului");
            println("9. Verifica eligibilitatea pentru promovare");
            println("10. Promoveaza Developer -> Manager");
            println("11. Sorteaza angajatii dupa salariu");
            println("12. Optiuni Developer");
            println("13. Optiuni Manager");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> addManagerFlow();
                case 2 -> addDeveloperFlow();
                case 3 -> listEmployees();
                case 4 -> renameEmployeeFlow();
                case 5 -> changeEmployeeEmailFlow();
                case 6 -> applyRaiseFlow();
                case 7 -> transferEmployeeFlow();
                case 8 -> evaluateEmployeePerformanceFlow();
                case 9 -> promotionEligibilityFlow();
                case 10 -> promoteDeveloperFlow();
                case 11 -> sortEmployeesBySalaryFlow();
                case 12 -> developerOpsFlow();
                case 13 -> managerOpsFlow();
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void clientMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- CLIENTS ---------");
            println("1. Adauga client");
            println("2. Afiseaza clienti");
            println("3. Actualizeaza contact");
            println("4. Schimba compania");
            println("5. Atribuie client la proiect");
            println("6. Afiseaza health score");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> addClientFlow();
                case 2 -> listClients();
                case 3 -> updateClientContactFlow();
                case 4 -> changeClientCompanyFlow();
                case 5 -> assignClientToProjectFlow();
                case 6 -> clientHealthFlow();
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void projectMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- PROJECTS ---------");
            println("1. Adauga proiect");
            println("2. Afiseaza proiecte");
            println("3. Redenumeste proiect");
            println("4. Actualizeaza bugetul");
            println("5. Schimba deadline");
            println("6. Schimba status");
            println("7. Adauga skill necesar");
            println("8. Schimba departamentul proiectului");
            println("9. Ataseaza angajat la proiect");
            println("10. Afiseaza raport proiect");
            println("11. Recomanda echipa optima");
            println("12. Marcheaza proiect ca anulat");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> addProjectFlow();
                case 2 -> listProjects();
                case 3 -> renameProjectFlow();
                case 4 -> updateProjectBudgetFlow();
                case 5 -> changeProjectDeadlineFlow();
                case 6 -> changeProjectStatusFlow();
                case 7 -> addRequiredSkillFlow();
                case 8 -> reassignProjectDepartmentFlow();
                case 9 -> assignEmployeeToProjectFlow();
                case 10 -> projectReportFlow();
                case 11 -> recommendOptimalTeamFlow();
                case 12 -> cancelProjectFlow();
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void taskMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- TASKS ---------");
            println("1. Adauga task la proiect");
            println("2. Afiseaza task-uri din proiect");
            println("3. Atribuie task la angajat");
            println("4. Marcheaza task complet");
            println("5. Blocheaza task");
            println("6. Redeschide task");
            println("7. Task-uri cu prioritate mare");
            println("8. Sorteaza task-urile unui proiect dupa prioritate");
            println("9. Redenumeste task");
            println("10. Schimba descrierea task-ului");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> addTaskFlow();
                case 2 -> listTasksFlow();
                case 3 -> assignTaskToEmployeeFlow();
                case 4 -> completeTaskFlow();
                case 5 -> blockTaskFlow();
                case 6 -> reopenTaskFlow();
                case 7 -> showHighPriorityTasksFlow();
                case 8 -> showSortedTasksFlow();
                case 9 -> renameTaskFlow();
                case 10 -> rewriteTaskDescriptionFlow();
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void analyticsMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- ANALYTICS / BUSINESS ---------");
            println("1. Total payroll");
            println("2. Top performers");
            println("3. Best developer for task");
            println("4. Evalueaza performanta angajatului");
            println("5. Verifica risc proiect");
            println("6. Cost / profit proiect");
            println("7. Estimeaza data finalizarii");
            println("8. Afiseaza summary companie");
            println("9. Optimizare resurse companie");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> println("Total payroll: " + service.totalPayroll());
                case 2 -> topPerformersFlow();
                case 3 -> bestDeveloperForTaskFlow();
                case 4 -> evaluateEmployeePerformanceFlow();
                case 5 -> projectRiskFlow();
                case 6 -> projectCostProfitFlow();
                case 7 -> estimateCompletionFlow();
                case 8 -> service.printSummary();
                case 9 -> {
                    company.optimizeResourceAllocation();
                    println("Optimizare executata.");
                }
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }

    // =========================
    // FLOW-URI: DEPARTMENTS
    // =========================

    private static void addDepartmentFlow() {
        int id = readInt("Department ID: ");
        String name = readNonEmpty("Nume departament: ");
        String location = readNonEmpty("Locatie: ");

        Department department = new Department(id, name, location);
        departments.put(id, department);
        service.addDepartment(department);

        println("Departament adaugat.");
    }

    private static void listDepartments() {
        if (departments.isEmpty()) {
            println("Nu exista departamente.");
            return;
        }

        println("=== Departamente ===");
        departments.values().forEach(d -> {
            String managerName = d.getManager() != null ? d.getManager().getFullName() : "none";
            println(
                    "ID=" + d.getId() +
                            ", name=" + d.getName() +
                            ", location=" + d.getLocation() +
                            ", manager=" + managerName +
                            ", headcount=" + d.getEmployees().size()
            );
        });
    }

    private static void renameDepartmentFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        String newName = readNonEmpty("Nume nou: ");
        department.setName(newName);
        println("Departamentul a fost redenumit.");
    }

    private static void relocateDepartmentFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        String newLocation = readNonEmpty("Noua locatie: ");
        department.setLocation(newLocation);
        println("Locatia a fost schimbata.");
    }

    private static void appointManagerFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        Employee employee = chooseEmployee();
        if (employee == null) return;

        if (!(employee instanceof Manager manager)) {
            println("Angajatul ales nu este Manager.");
            return;
        }

        department.appointManager(manager);
        println("Manager atribuit departamentului.");
    }

    private static void showDepartmentDetailsFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        println(department.toString());
        println("Employees:");
        department.getEmployees().forEach(e -> println(" - " + e));
        println("Average base salary: " + department.getAverageMonthlySalary());
    }
    private static void addManagerFlow() {
        int id = readInt("Employee ID: ");
        String fullName = readNonEmpty("Nume complet: ");
        String email = readNonEmpty("Email: ");
        double salary = readDouble("Salariu: ");
        Department department = chooseDepartment();
        if (department == null) return;

        int teamSize = readInt("Marime echipa: ");
        double bonus = readDouble("Bonus management: ");

        Manager manager = new Manager(id, fullName, email, salary, department, teamSize, bonus);
        employees.put(id, manager);
        company.addEmployee(manager);
        service.hireEmployee(manager, department.getId());

        println("Manager adaugat.");
    }

    private static void addDeveloperFlow() {
        int id = readInt("Employee ID: ");
        String fullName = readNonEmpty("Nume complet: ");
        String email = readNonEmpty("Email: ");
        double salary = readDouble("Salariu: ");
        Department department = chooseDepartment();
        if (department == null) return;

        String mainLanguage = readNonEmpty("Limbaj principal: ");
        double allowance = readDouble("Tech allowance: ");

        Developer developer = new Developer(id, fullName, email, salary, department);
        employees.put(id, developer);
        company.addEmployee(developer);
        service.hireEmployee(developer, department.getId());

        println("Developer adaugat.");
    }

    private static void listEmployees() {
        if (employees.isEmpty()) {
            println("Nu exista angajati.");
            return;
        }

        println("=== Angajati ===");
        employees.values().forEach(e -> println(formatEmployee(e)));
    }

    private static void renameEmployeeFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        String newName = readNonEmpty("Nume nou: ");
        employee.setFullName(newName);
        println("Numele a fost actualizat.");
    }

    private static void changeEmployeeEmailFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        String newEmail = readNonEmpty("Email nou: ");
        employee.setEmail(newEmail);
        println("Email-ul a fost actualizat.");
    }

    private static void applyRaiseFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        double percent = readDouble("Procent marire: ");
        employee.applyRaise(percent);
        println("Marire aplicata. Salariu nou: " + employee.getSalary());
    }

    private static void transferEmployeeFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        Department source = employee.getDepartment();
        Department target = chooseDepartment();
        if (target == null) return;

        if (source != null) {
            source.removeEmployee(employee);
        }
        target.addEmployee(employee);

        println("Angajat transferat In departamentul: " + target.getName());
    }

    private static void evaluateEmployeePerformanceFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        double score = service.evaluateEmployeePerformance(employee.getId());
        double altScore = service.calculatePerformanceScore(employee.getId());

        println("=== Performance ===");
        println("Employee: " + employee.getFullName());
        println("Score evaluare: " + String.format("%.2f", score));
        println("Score business: " + String.format("%.2f", altScore));
    }

    private static void promotionEligibilityFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        boolean eligible = service.isEligibleForPromotion(employee.getId());
        println("Eligibil pentru promovare: " + eligible);
    }

    private static void promoteDeveloperFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        if (!(employee instanceof Developer)) {
            println("Angajatul nu este Developer.");
            return;
        }

        int teamSize = readInt("Nou team size: ");
        double bonus = readDouble("Bonus manager: ");

        Manager promoted = service.promoteDeveloperToManager(employee.getId(), teamSize, bonus);
        employees.put(promoted.getId(), promoted);
        company.addEmployee(promoted);

        println("Promovare reusita. Noul obiect: " + promoted);
    }

    private static void sortEmployeesBySalaryFlow() {
        List<Employee> sorted = service.employeesSortedBySalaryDesc();
        if (sorted.isEmpty()) {
            println("Nu exista angajati.");
            return;
        }

        println("=== Angajati sortati dupa salariu descrescator ===");
        sorted.forEach(e -> println(formatEmployee(e)));
    }

    private static void developerOpsFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        if (!(employee instanceof Developer developer)) {
            println("Angajatul nu este Developer.");
            return;
        }

        boolean back = false;
        while (!back) {
            println("");
            println("----- Developer Ops: " + developer.getFullName() + " -----");
            println("1. Adauga skill");
            println("2. Elimina skill");
            println("3. Listeaza skill-uri");
            println("4. Verifica skill");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> {
                    String skill = readNonEmpty("Skill: ");
                    developer.learnSkill(skill);
                    println("Skill adaugat.");
                }
                case 2 -> {
                    String skill = readNonEmpty("Skill de eliminat: ");
                    developer.forgetSkill(skill);
                    println("Skill eliminat.");
                }
                case 3 -> println("Skills: " + developer.getSkills());
                case 4 -> {
                    String skill = readNonEmpty("Skill: ");
                    println("Are skill? " + developer.hasSkill(skill));
                }
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void managerOpsFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        if (!(employee instanceof Manager manager)) {
            println("Angajatul nu este Manager.");
            return;
        }

        boolean back = false;
        while (!back) {
            println("");
            println("----- Manager Ops: " + manager.getFullName() + " -----");
            println("1. Creste team size");
            println("2. Scade team size");
            println("3. Schimba bonus management");
            println("4. Afiseaza team size");
            println("5. Verifica aprobarea de buget");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            switch (choice) {
                case 1 -> {
                    manager.increaseTeamSize();
                    println("Team size incrementat.");
                }
                case 2 -> {
                    manager.decreaseTeamSize();
                    println("Team size decrementat.");
                }
                case 3 -> {
                    double bonus = readDouble("Bonus nou: ");
                    manager.setManagementBonus(bonus);
                    println("Bonus actualizat.");
                }
                case 4 -> println("Team size: " + manager.getTeamSize());
                case 5 -> {
                    double amount = readDouble("Suma buget: ");
                    println("Poate aproba? " + manager.canApproveBudget(amount));
                }
                case 0 -> back = true;
                default -> println("Optiune invalida.");
            }
        }
    }
    private static void addClientFlow() {
        int id = readInt("Client ID: ");
        String name = readNonEmpty("Nume: ");
        String email = readNonEmpty("Email: ");
        String industry = readNonEmpty("Industrie: ");

        Client client = new Client(id, name, email, industry);
        clients.put(id, client);
        company.addClient(client);
        service.addClient(client);

        println("Client adaugat.");
    }

    private static void listClients() {
        if (clients.isEmpty()) {
            println("Nu exista clienti.");
            return;
        }

        println("=== Clienti ===");
        clients.values().forEach(c -> {
            println(
                    "ID=" + c.getId() +
                            ", name=" + c.getName() +
                            ", industry=" + c.getIndustry() +
                            ", projects=" + c.getProjects().size() +
                            ", health=" + String.format("%.2f", c.calculateAccountHealthScore()) +
                            ", tier=" + c.getAccountTier()
            );
        });
    }

    private static void updateClientContactFlow() {
        Client client = chooseClient();
        if (client == null) return;

        String newName = readNonEmpty("Nume nou: ");
        String newEmail = readNonEmpty("Email nou: ");
        client.updateContact(newName, newEmail);
        println("Contact actualizat.");
    }

    private static void changeClientCompanyFlow() {
        Client client = chooseClient();
        if (client == null) return;

        String newCompany = readNonEmpty("Companie noua: ");
        client.changeCompany(newCompany);
        println("Compania a fost actualizata.");
    }

    private static void assignClientToProjectFlow() {
        Client client = chooseClient();
        if (client == null) return;

        Project project = chooseProject();
        if (project == null) return;

        if (project.getClient() != null) {
            project.getClient().removeProject(project);
        }

        project.reassignClient(client);
        client.registerProject(project);

        println("Client atribuit proiectului.");
    }

    private static void clientHealthFlow() {
        Client client = chooseClient();
        if (client == null) return;

        println("Client: " + client.getName());
        println("Health score: " + String.format("%.2f", client.calculateAccountHealthScore()));
        println("Tier: " + client.getAccountTier());
        println("Active projects: " + client.getActiveProjectsCount());
        println("Completed projects: " + client.getCompletedProjectsCount());
        println("Risky projects: " + client.getRiskyProjectsCount());
    }

    private static void addProjectFlow() {
        int id = readInt("Project ID: ");
        String name = readNonEmpty("Nume proiect: ");
        String description = readNonEmpty("Descriere: ");
        double budget = readDouble("Buget: ");
        LocalDate startDate = readDate("Start date (YYYY-MM-DD): ");
        LocalDate deadline = readDate("Deadline (YYYY-MM-DD): ");
        Department department = chooseDepartment();
        if (department == null) return;

        Client client = null;
        String attachClient = readNonEmpty("Atasezi client acum? (y/n): ");
        if (attachClient.equalsIgnoreCase("y")) {
            client = chooseClient();
            if (client == null) return;
        }

            Project project = new Project(id, name, description, department,client,null,budget, deadline);
        projects.put(id, project);
        company.addProject(project);
        service.addProject(project);

        if (client != null) {
            client.registerProject(project);
        }

        println("Proiect adaugat.");
    }

    private static void listProjects() {
        if (projects.isEmpty()) {
            println("Nu exista proiecte.");
            return;
        }

        println("=== Proiecte ===");
        projects.values().forEach(p -> println(formatProject(p)));
    }

    private static void renameProjectFlow() {
        Project project = chooseProject();
        if (project == null) return;

        String newName = readNonEmpty("Nume nou: ");
        project.setName(newName);
        println("Proiect redenumit.");
    }

    private static void updateProjectBudgetFlow() {
        Project project = chooseProject();
        if (project == null) return;

        double budget = readDouble("Buget nou: ");
        project.updateBudget(budget);
        println("Buget actualizat.");
    }

    private static void changeProjectDeadlineFlow() {
        Project project = chooseProject();
        if (project == null) return;

        LocalDate deadline = readDate("Deadline nou (YYYY-MM-DD): ");
        project.changeDeadline(deadline);
        println("Deadline actualizat.");
    }

    private static void changeProjectStatusFlow() {
        Project project = chooseProject();
        if (project == null) return;

        ProjectStatus status = readProjectStatus("Status nou: ");
        project.changeStatus(status);
        println("Status actualizat.");
    }
    private static ProjectStatus readProjectStatus(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim().toUpperCase();
            try {
                return ProjectStatus.valueOf(value);
            } catch (IllegalArgumentException ex) {
                println("Status invalid. Valori acceptate: PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED");
            }
        }
    }

    private static void addRequiredSkillFlow() {
        Project project = chooseProject();
        if (project == null) return;

        String skill = readNonEmpty("Skill necesar: ");
        project.addRequiredSkill(skill);
        println("Skill adaugat proiectului.");
    }

    private static void reassignProjectDepartmentFlow() {
        Project project = chooseProject();
        if (project == null) return;

        Department department = chooseDepartment();
        if (department == null) return;

        project.reassignDepartment(department);
        println("Departamentul proiectului a fost schimbat.");
    }

    private static void assignEmployeeToProjectFlow() {
        Project project = chooseProject();
        if (project == null) return;

        Employee employee = chooseEmployee();
        if (employee == null) return;

        service.assignEmployeeToProject(employee.getId(), project.getId());
        println("Angajat asignat la proiect.");
    }

    private static void projectReportFlow() {
        Project project = chooseProject();
        if (project == null) return;

        println("=== Project report ===");
        println(formatProject(project));
        println("Employees on project:");
        project.getEmployees().forEach(e -> println(" - " + e.getFullName()));
        println("Required skills: " + project.getRequiredSkills());
        println("High priority? cost/profit/risk:");
        println("Cost: " + service.calculateProjectCost(project.getId()));
        println("Profit: " + service.calculateProjectProfit(project.getId()));
        println("At risk: " + service.isProjectAtRisk(project.getId()));
        println("Estimated completion: " + service.estimateCompletionDate(project.getId()));
    }

    private static void recommendOptimalTeamFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int maxMembers = readInt("Cati membri recomanzi maxim? ");

        List<Employee> recommended = service.recommendOptimalTeam(project.getId(), maxMembers);
        println("=== Echipa optima recomandata ===");
        recommended.forEach(e -> println(formatEmployee(e)));
    }

    private static void cancelProjectFlow() {
        Project project = chooseProject();
        if (project == null) return;

        project.changeStatus(ProjectStatus.CANCELLED);
        if (project.getClient() != null) {
            project.getClient().removeProject(project);
        }
        println("Proiect marcat ca CANCELLED.");
    }

    private static void addTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int id = readInt("Task ID: ");
        String title = readNonEmpty("Titlu: ");
        String description = readNonEmpty("Descriere: ");
        LocalDate dueDate = readDate("Due date (YYYY-MM-DD): ");
        int difficulty = readInt("Difficulty (1-10): ");

        Task task = new Task(id, title, description, dueDate, difficulty);
        service.addTaskToProject(project.getId(), task);

        println("Task adaugat in proiect.");
    }

    private static void listTasksFlow() {
        Project project = chooseProject();
        if (project == null) return;

        Map<Integer, Task> taskMap = project.getTasks();
        if (taskMap.isEmpty()) {
            println("Nu exista task-uri.");
            return;
        }

        println("=== Task-uri din proiect ===");
        taskMap.values().forEach(t -> println(formatTask(t)));
    }

    private static void assignTaskToEmployeeFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("Task ID: ");
        Employee employee = chooseEmployee();
        if (employee == null) return;

        service.assignTaskToEmployee(project.getId(), taskId, employee.getId());
        println("Task asignat angajatului.");
    }

    private static void completeTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("Task ID: ");
        service.completeTask(project.getId(), taskId);
        println("Task marcat ca finalizat.");
    }

    private static void blockTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("Task ID: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        task.block();
        println("Task blocat.");
    }

    private static void reopenTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("Task ID: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        task.reopen();
        println("Task redeschis.");
    }

    private static void showHighPriorityTasksFlow() {
        Project project = chooseProject();
        if (project == null) return;

        List<Task> highPriority = service.getHighPriorityTasks(project.getId());
        if (highPriority.isEmpty()) {
            println("Nu exista task-uri.");
            return;
        }

        println("=== Task-uri cu prioritate mare ===");
        highPriority.forEach(t -> println(formatTask(t)));
    }

    private static void showSortedTasksFlow() {
        Project project = chooseProject();
        if (project == null) return;

        List<Task> sorted = project.getTasks().values().stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .collect(Collectors.toList());

        println("=== Task-uri sortate dupa prioritate ===");
        sorted.forEach(t -> println(formatTask(t)));
    }

    private static void renameTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("Task ID: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        String newTitle = readNonEmpty("Titlu nou: ");
        task.setTitle(newTitle);
        println("Task redenumit.");
    }

    private static void rewriteTaskDescriptionFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("Task ID: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        String newDescription = readNonEmpty("Descriere noua: ");
        task.setDescription(newDescription);
        println("Descriere actualizata.");
    }

    // =========================
    // FLOW-URI: ANALYTICS
    // =========================

    private static void topPerformersFlow() {
        if (employees.isEmpty()) {
            println("Nu exista angajati.");
            return;
        }

        List<Employee> ranked = employees.values().stream()
                .sorted((a, b) -> Double.compare(
                        service.evaluateEmployeePerformance(b.getId()),
                        service.evaluateEmployeePerformance(a.getId())
                ))
                .collect(Collectors.toList());

        println("=== Top performers ===");
        ranked.forEach(e -> println(
                e.getFullName() +
                        " | score=" + String.format("%.2f", service.evaluateEmployeePerformance(e.getId())) +
                        " | role=" + e.getRole()
        ));
    }

    private static void bestDeveloperForTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("Task ID: ");

        Employee best = service.assignBestDeveloperToTask(project.getId(), taskId);
        println("Cel mai bun developer pentru task: " + best);
    }

    private static void projectRiskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        println("Project: " + project.getName());
        println("At risk: " + service.isProjectAtRisk(project.getId()));
        println("Progress: " + project.progress());
        println("Deadline: " + project.getDeadline());
    }

    private static void projectCostProfitFlow() {
        Project project = chooseProject();
        if (project == null) return;

        println("Project: " + project.getName());
        println("Cost: " + service.calculateProjectCost(project.getId()));
        println("Profit: " + service.calculateProjectProfit(project.getId()));
        println("At risk: " + service.isProjectAtRisk(project.getId()));
    }

    private static void estimateCompletionFlow() {
        Project project = chooseProject();
        if (project == null) return;

        LocalDate date = service.estimateCompletionDate(project.getId());
        println("Data estimata de finalizare: " + date);
    }
    private static Department chooseDepartment() {
        if (departments.isEmpty()) {
            println("Nu exista departamente.");
            return null;
        }

        listDepartments();
        int id = readInt("Department ID ales: ");
        Department department = departments.get(id);

        if (department == null) {
            println("Departament inexistent.");
        }
        return department;
    }

    private static Employee chooseEmployee() {
        if (employees.isEmpty()) {
            println("Nu exista angajati.");
            return null;
        }

        listEmployees();
        int id = readInt("Employee ID ales: ");
        Employee employee = employees.get(id);

        if (employee == null) {
            println("Angajat inexistent.");
        }
        return employee;
    }

    private static Client chooseClient() {
        if (clients.isEmpty()) {
            println("Nu exista clienti.");
            return null;
        }

        listClients();
        int id = readInt("Client ID ales: ");
        Client client = clients.get(id);

        if (client == null) {
            println("Client inexistent.");
        }
        return client;
    }

    private static Project chooseProject() {
        if (projects.isEmpty()) {
            println("Nu exista proiecte.");
            return null;
        }

        listProjects();
        int id = readInt("Project ID ales: ");
        Project project = projects.get(id);

        if (project == null) {
            println("Proiect inexistent.");
        }
        return project;
    }

    private static Task findTask(Project project, int taskId) {
        Task task = project.getTasks().get(taskId);
        if (task == null) {
            println("Task inexistent.");
        }
        return task;
    }

    private static String formatEmployee(Employee e) {
        return "ID=" + e.getId()
                + ", name=" + e.getFullName()
                + ", role=" + e.getRole()
                + ", salary=" + e.getSalary()
                + ", dept=" + (e.getDepartment() != null ? e.getDepartment().getName() : "none")
                + ", hireDate=" + e.getHireDate();
    }

    private static String formatProject(Project p) {
        return "ID=" + p.getId()
                + ", name=" + p.getName()
                + ", status=" + p.getStatus()
                + ", budget=" + p.getBudget()
                + ", deadline=" + p.getDeadline()
                + ", progress=" + p.progress()
                + ", client=" + (p.getClient() != null ? p.getClient().getName() : "none")
                + ", dept=" + (p.getDepartment() != null ? p.getDepartment().getName() : "none");
    }

    private static String formatTask(Task t) {
        return "ID=" + t.getId()
                + ", title=" + t.getTitle()
                + ", status=" + t.getStatus()
                + ", priority=" + t.getPriority()
                + ", difficulty=" + t.getDifficulty()
                + ", dueDate=" + t.getDueDate()
                + ", assignee=" + (t.getEmployee() != null ? t.getEmployee().getFullName() : "none");
    }

    private static void println(String message) {
        System.out.println(message);
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            println("Valoare invalida. Incearca din nou.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                println("Introdu un numar intreg valid.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                println("Introdu un numar real valid.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ex) {
                println("Format invalid. Foloseste YYYY-MM-DD.");
            }
        }
    }
}