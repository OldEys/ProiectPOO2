import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);

    private static Company company;
    private static CompanyManagementService service;

    private static final Map<Integer, Department> departments = new LinkedHashMap<>();
    private static final Map<Integer, Employee> employees = new LinkedHashMap<>();
    private static final Map<Integer, Project> projects = new LinkedHashMap<>();
    private static final Map<Integer, Client> clients = new LinkedHashMap<>();
    private static final DepartmentsService serviciuDepartamenteJdbc = DepartmentsService.getInstance();
    private static final ClientService serviciuClientiJdbc = ClientService.getInstance();
    private static final EmployeeService serviciuAngajatiJdbc = EmployeeService.getInstance();
    private static final ProjectService serviciuProiecteJdbc = ProjectService.getInstance();
    private static final TaskService serviciuTaskuriJdbc = TaskService.getInstance();
    private static final LogService serviciuAudit = LogService.getInstance();
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver gasit");
        } catch (Exception e) {
            e.printStackTrace();
        }
        company = new Company("TechCorp");
        service = new CompanyManagementService(company);

        incarcaDateDinBazaDeDate();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Alege optiunea: ");
            String menu = "meniu_principal";

            switch (choice) {
                case 1 -> {
                    companyMenu();
                    inregistreazaActiune(menu + "_companie");
                }
                case 2 -> {
                    departmentMenu();
                    inregistreazaActiune(menu + "_departamente");
                }
                case 3 -> {
                    employeeMenu();
                    inregistreazaActiune(menu + "_angajati");
                }
                case 4 -> {
                    clientMenu();
                    inregistreazaActiune(menu + "_clienti");
                }
                case 5 -> {
                    projectMenu();
                    inregistreazaActiune(menu + "_proiecte");
                }
                case 6 -> {
                    taskMenu();
                    inregistreazaActiune(menu + "_taskuri");
                }
                case 7 -> {
                    analyticsMenu();
                    inregistreazaActiune(menu + "_analize");
                }
                case 0 -> {
                    inregistreazaActiune(menu + "_iesire");
                    running = false;
                }
                default -> println("Optiune invalida.");
            }
        }

        println("Aplicatia se inchide...");
    }
    private static void titleMenu() {
        println("=== Sistem de management al companiei ===");
        String companyName = readNonEmpty("Numele companiei: ");
        company = new Company(companyName);
        service = new CompanyManagementService(company);
        println("Compania a fost creata: " + companyName);
    }

    private static boolean incarcaDateDinBazaDeDate() {
        try {
            List<Department> departamenteDinBaza = serviciuDepartamenteJdbc.citesteToate();
            List<Client> clientiDinBaza = serviciuClientiJdbc.citesteToate();
            List<Employee> angajatiDinBaza = serviciuAngajatiJdbc.citesteToate();
            List<Project> proiecteDinBaza = serviciuProiecteJdbc.citesteToate();

            if (departamenteDinBaza.isEmpty()
                    && clientiDinBaza.isEmpty()
                    && angajatiDinBaza.isEmpty()
                    && proiecteDinBaza.isEmpty()) {
                persistaDateCurenteInBazaDeDate();
                return true;
            }

            departments.clear();
            clients.clear();
            employees.clear();
            projects.clear();
            company = new Company(company.getName());
            service = new CompanyManagementService(company);

            for (Department department : departamenteDinBaza) {
                departments.put(department.getId(), department);
                service.addDepartment(department);
            }

            for (Client client : clientiDinBaza) {
                clients.put(client.getId(), client);
                service.addClient(client);
            }

            for (Employee employee : angajatiDinBaza) {
                if (employee.getDepartment() != null) {
                    Department department = departments.get(employee.getDepartment().getId());
                    employee.setDepartment(department);
                }
                employees.put(employee.getId(), employee);
                service.addEmployee(employee);
            }

            for (Department department : departments.values()) {
                serviciuDepartamenteJdbc.citesteManagerId(department.getId()).ifPresent(managerId -> {
                    Employee employee = employees.get(managerId);
                    if (employee instanceof Manager manager) {
                        department.appointManager(manager);
                    }
                });
            }

            for (Project project : proiecteDinBaza) {
                if (project.getDepartment() != null) {
                    project.reassignDepartment(departments.get(project.getDepartment().getId()));
                }
                if (project.getClient() != null) {
                    Client client = clients.get(project.getClient().getId());
                    project.reassignClient(client);
                    if (client != null) {
                        client.registerProject(project);
                    }
                }
                projects.put(project.getId(), project);
                service.addProject(project);
            }

            println("Datele au fost incarcate din baza de date.");
            return true;
        } catch (RuntimeException exception) {
            println("Baza de date nu este disponibila. Detalii: " + exception.getMessage());
            return false;
        }
    }

    private static void persistaDateCurenteInBazaDeDate() {
        for (Department department : departments.values()) {
            serviciuDepartamenteJdbc.creeaza(department);
        }
        for (Client client : clients.values()) {
            serviciuClientiJdbc.creeaza(client);
        }
        for (Employee employee : employees.values()) {
            serviciuAngajatiJdbc.creeaza(employee);
        }
        for (Department department : departments.values()) {
            serviciuDepartamenteJdbc.actualizeaza(department);
        }
        for (Project project : projects.values()) {
            serviciuProiecteJdbc.creeaza(project);
        }
        for (Project project : projects.values()) {
            for (Task task : project.getTasks().values()) {
                serviciuTaskuriJdbc.creeaza(task);
            }
        }
    }

    private static void ruleazaPersistenta(String descriere, Runnable operatie) {
        try {
            operatie.run();
        } catch (RuntimeException exception) {
            println("Persistenta in baza de date a esuat pentru " + descriere + ": " + exception.getMessage());
        }
    }

    private static void inregistreazaActiune(String numeActiune) {
        try {
            serviciuAudit.inregistreaza(numeActiune);
        } catch (RuntimeException exception) {
            println("Auditul nu a putut fi scris: " + exception.getMessage());
        }
    }


    private static void printMainMenu() {
        println("");
        println("========== MENIU PRINCIPAL ==========");
        println("1. Companie");
        println("2. Departamente");
        println("3. Angajati");
        println("4. Clienti");
        println("5. Proiecte");
        println("6. Task-uri");
        println("7. Analize si logica de business");
        println("0. Iesire");
        println("=====================================");
    }

    private static void companyMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- MENIU COMPANIE ---------");
            println("1. Afiseaza compania");
            println("2. Afiseaza rezumat");
            println("3. Optimizeaza alocarea resurselor");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu = "meniu_companie";

            switch (choice) {
                case 1 -> {
                    println(company.toString());
                    inregistreazaActiune(menu + "_afisare_companie");
                }
                case 2 -> {
                    service.printSummary();
                    inregistreazaActiune(menu + "_rezumat");
                }
                case 3 -> {
                    company.optimizeResourceAllocation();
                    println("Optimizare executata.");
                    inregistreazaActiune(menu + "_optimizare_resurse");
                }
                case 0 -> {
                    back = true;
                    inregistreazaActiune(menu + "_inapoi");
                }
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void departmentMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- DEPARTAMENTE ---------");
            println("1. Adauga departament");
            println("2. Afiseaza departamente");
            println("3. Redenumeste departament");
            println("4. Schimba locatia departamentului");
            println("5. Atribuie manager");
            println("6. Detalii departament");
            println("7. Sterge departament");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu = "meniu_departament";

            switch (choice) {
                case 1 -> {
                    addDepartmentFlow();
                    inregistreazaActiune(menu + "_adauga");
                }
                case 2 -> {
                    listDepartments();
                    inregistreazaActiune(menu + "_afisare");
                }
                case 3 -> {
                    renameDepartmentFlow();
                    inregistreazaActiune(menu + "_redenumire");
                }
                case 4 -> {
                    relocateDepartmentFlow();
                    inregistreazaActiune(menu + "_relocare");
                }
                case 5 -> {
                    appointManagerFlow();
                    inregistreazaActiune(menu + "_atribuire_manager");
                }
                case 6 -> {
                    showDepartmentDetailsFlow();
                    inregistreazaActiune(menu + "_detalii");
                }
                case 7 -> {
                    deleteDepartmentFlow();
                    inregistreazaActiune(menu + "_stergere");
                }
                case 0 -> {
                    back = true;
                    inregistreazaActiune(menu + "_inapoi");
                }
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void employeeMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- ANGAJATI ---------");
            println("1. Adauga manager");
            println("2. Adauga developer");
            println("3. Afiseaza angajati");
            println("4. Redenumeste angajat");
            println("5. Schimba email");
            println("6. Aplica marire salariala");
            println("7. Transfera angajat intre departamente");
            println("8. Evalueaza performanta angajatului");
            println("9. Verifica eligibilitatea pentru promovare");
            println("10. Promoveaza developer la manager");
            println("11. Sorteaza angajatii dupa salariu");
            println("12. Optiuni developer");
            println("13. Optiuni manager");
            println("14. Sterge angajat");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu = "meniu_angajati";

            switch (choice) {
                case 1 -> {
                    addManagerFlow();
                    inregistreazaActiune(menu + "_adauga_manager");
                }
                case 2 -> {
                    addDeveloperFlow();
                    inregistreazaActiune(menu + "_adauga_developer");
                }
                case 3 -> {
                    listEmployees();
                    inregistreazaActiune(menu + "_afisare");
                }
                case 4 -> {
                    renameEmployeeFlow();
                    inregistreazaActiune(menu + "_redenumire");
                }
                case 5 -> {
                    changeEmployeeEmailFlow();
                    inregistreazaActiune(menu + "_schimbare_email");
                }
                case 6 -> {
                    applyRaiseFlow();
                    inregistreazaActiune(menu + "_marire_salariu");
                }
                case 7 -> {
                    transferEmployeeFlow();
                    inregistreazaActiune(menu + "_transfer");
                }
                case 8 -> {
                    evaluateEmployeePerformanceFlow();
                    inregistreazaActiune(menu + "_evaluare_performanta");
                }
                case 9 -> {
                    promotionEligibilityFlow();
                    inregistreazaActiune(menu + "_eligibilitate_promovare");
                }
                case 10 -> {
                    promoteDeveloperFlow();
                    inregistreazaActiune(menu + "_promovare");
                }
                case 11 -> {
                    sortEmployeesBySalaryFlow();
                    inregistreazaActiune(menu + "_sortare_salariu");
                }
                case 12 -> {
                    developerOpsFlow();
                    inregistreazaActiune(menu + "_developer_ops");
                }
                case 13 -> {
                    managerOpsFlow();
                    inregistreazaActiune(menu + "_manager_ops");
                }
                case 14 -> {
                    deleteEmployeeFlow();
                    inregistreazaActiune(menu + "_stergere");
                }
                case 0 -> {
                    back = true;
                    inregistreazaActiune(menu + "_inapoi");
                }
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void clientMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- CLIENTI ---------");
            println("1. Adauga client");
            println("2. Afiseaza clienti");
            println("3. Actualizeaza contact");
            println("4. Schimba compania");
            println("5. Atribuie client la proiect");
            println("6. Afiseaza scorul de sanatate");
            println("7. Sterge client");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu="meniu_client";
            switch (choice) {
                case 1 -> {
                    inregistreazaActiune(menu+"_adauga_client");
                    addClientFlow();
                }
                case 2 -> {
                    inregistreazaActiune(menu+"_afisare_client");
                    listClients();
                }
                case 3 -> {
                    inregistreazaActiune(menu+"_redenumire_client");
                    updateClientContactFlow();
                }
                case 4 -> {
                    inregistreazaActiune(menu+"_schimbare_companie_client");
                    changeClientCompanyFlow();
                }
                case 5 -> {
                    inregistreazaActiune(menu+"_asigneaza_client_proiect");
                    assignClientToProjectFlow();
                }
                case 6 -> {
                    inregistreazaActiune(menu+"_performanta_client");
                    clientHealthFlow();
                }
                case 7 -> {
                    inregistreazaActiune(menu+"_sterge_client");
                    deleteClientFlow();
                }
                case 0 -> {
                    inregistreazaActiune(menu+"_inapoi");
                    back = true;
                }
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void projectMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- PROIECTE ---------");
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
            println("13. Sterge proiect");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu = "meniu_proiect";

            switch (choice) {
                case 1 -> {
                    addProjectFlow();
                    inregistreazaActiune(menu + "_adauga");
                }
                case 2 -> {
                    listProjects();
                    inregistreazaActiune(menu + "_afisare");
                }
                case 3 -> {
                    renameProjectFlow();
                    inregistreazaActiune(menu + "_redenumire");
                }
                case 4 -> {
                    updateProjectBudgetFlow();
                    inregistreazaActiune(menu + "_buget");
                }
                case 5 -> {
                    changeProjectDeadlineFlow();
                    inregistreazaActiune(menu + "_deadline");
                }
                case 6 -> {
                    changeProjectStatusFlow();
                    inregistreazaActiune(menu + "_status");
                }
                case 7 -> {
                    addRequiredSkillFlow();
                    inregistreazaActiune(menu + "_skill");
                }
                case 8 -> {
                    reassignProjectDepartmentFlow();
                    inregistreazaActiune(menu + "_departament");
                }
                case 9 -> {
                    assignEmployeeToProjectFlow();
                    inregistreazaActiune(menu + "_asignare_angajat");
                }
                case 10 -> {
                    projectReportFlow();
                    inregistreazaActiune(menu + "_raport");
                }
                case 11 -> {
                    recommendOptimalTeamFlow();
                    inregistreazaActiune(menu + "_recomandare_echipa");
                }
                case 12 -> {
                    cancelProjectFlow();
                    inregistreazaActiune(menu + "_anulare");
                }
                case 13 -> {
                    deleteProjectFlow();
                    inregistreazaActiune(menu + "_stergere");
                }
                case 0 -> {
                    back = true;
                    inregistreazaActiune(menu + "_inapoi");
                }
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void taskMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- TASK-URI ---------");
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
            println("11. Sterge task");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu = "meniu_task";

            switch (choice) {
                case 1 -> {
                    addTaskFlow();
                    inregistreazaActiune(menu + "_adauga");
                }
                case 2 -> {
                    listTasksFlow();
                    inregistreazaActiune(menu + "_afisare");
                }
                case 3 -> {
                    assignTaskToEmployeeFlow();
                    inregistreazaActiune(menu + "_asignare");
                }
                case 4 -> {
                    completeTaskFlow();
                    inregistreazaActiune(menu + "_finalizare");
                }
                case 5 -> {
                    blockTaskFlow();
                    inregistreazaActiune(menu + "_blocare");
                }
                case 6 -> {
                    reopenTaskFlow();
                    inregistreazaActiune(menu + "_redeschidere");
                }
                case 7 -> {
                    showHighPriorityTasksFlow();
                    inregistreazaActiune(menu + "_prioritate_mare");
                }
                case 8 -> {
                    showSortedTasksFlow();
                    inregistreazaActiune(menu + "_sortare");
                }
                case 9 -> {
                    renameTaskFlow();
                    inregistreazaActiune(menu + "_redenumire");
                }
                case 10 -> {
                    rewriteTaskDescriptionFlow();
                    inregistreazaActiune(menu + "_descriere");
                }
                case 11 -> {
                    deleteTaskFlow();
                    inregistreazaActiune(menu + "_stergere");
                }
                case 0 -> {
                    back = true;
                    inregistreazaActiune(menu + "_inapoi");
                }
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void analyticsMenu() {
        boolean back = false;
        while (!back) {
            println("");
            println("--------- ANALIZE SI BUSINESS ---------");
            println("1. Fond salarial total");
            println("2. Cei mai performanti angajati");
            println("3. Cel mai bun developer pentru task");
            println("4. Evalueaza performanta angajatului");
            println("5. Verifica risc proiect");
            println("6. Cost si profit proiect");
            println("7. Estimeaza data finalizarii");
            println("8. Afiseaza rezumat companie");
            println("9. Optimizare resurse companie");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu = "meniu_analize";

            switch (choice) {
                case 1 -> {
                    println("Fond salarial total: " + service.totalPayroll());
                    inregistreazaActiune(menu + "_fond_salarial");
                }
                case 2 -> {
                    topPerformersFlow();
                    inregistreazaActiune(menu + "_top_performeri");
                }
                case 3 -> {
                    bestDeveloperForTaskFlow();
                    inregistreazaActiune(menu + "_best_developer");
                }
                case 4 -> {
                    evaluateEmployeePerformanceFlow();
                    inregistreazaActiune(menu + "_evaluare");
                }
                case 5 -> {
                    projectRiskFlow();
                    inregistreazaActiune(menu + "_risc_proiect");
                }
                case 6 -> {
                    projectCostProfitFlow();
                    inregistreazaActiune(menu + "_cost_profit");
                }
                case 7 -> {
                    estimateCompletionFlow();
                    inregistreazaActiune(menu + "_estimare");
                }
                case 8 -> {
                    service.printSummary();
                    inregistreazaActiune(menu + "_rezumat");
                }
                case 9 -> {
                    company.optimizeResourceAllocation();
                    inregistreazaActiune(menu + "_optimizare");
                }
                case 0 -> {
                    back = true;
                    inregistreazaActiune(menu + "_inapoi");
                }
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void addDepartmentFlow() {
        int id = readInt("ID departament: ");
        String name = readNonEmpty("Nume departament: ");
        String location = readNonEmpty("Locatie: ");

        Department department = new Department(id, name, location);
        departments.put(id, department);
        service.addDepartment(department);
        ruleazaPersistenta("creare departament", () -> serviciuDepartamenteJdbc.creeaza(department));

        println("Departament adaugat.");
    }

    private static void listDepartments() {
        if (departments.isEmpty()) {
            println("Nu exista departamente.");
            return;
        }

        println("=== Departamente ===");
        departments.values().forEach(d -> {
            String managerName = d.getManager() != null ? d.getManager().getFullName() : "niciunul";
            println(
                    "ID=" + d.getId() +
                            ", nume=" + d.getName() +
                            ", locatie=" + d.getLocation() +
                            ", manager=" + managerName +
                            ", numarAngajati=" + d.getEmployees().size()
            );
        });
    }

    private static void renameDepartmentFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        String newName = readNonEmpty("Nume nou: ");
        department.setName(newName);
        ruleazaPersistenta("actualizare departament", () -> serviciuDepartamenteJdbc.actualizeaza(department));
        println("Departamentul a fost redenumit.");
    }

    private static void relocateDepartmentFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        String newLocation = readNonEmpty("Noua locatie: ");
        department.setLocation(newLocation);
        ruleazaPersistenta("actualizare departament", () -> serviciuDepartamenteJdbc.actualizeaza(department));
        println("Locatia a fost schimbata.");
    }

    private static void appointManagerFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        Employee employee = chooseEmployee();
        if (employee == null) return;

        if (!(employee instanceof Manager manager)) {
            println("Angajatul ales nu este manager.");
            return;
        }

        department.appointManager(manager);
        ruleazaPersistenta("actualizare manager departament", () -> serviciuDepartamenteJdbc.actualizeaza(department));
        println("Manager atribuit departamentului.");
    }

    private static void showDepartmentDetailsFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        println(department.toString());
        println("Angajati:");
        department.getEmployees().forEach(e -> println(" - " + e));
        println("Salariu mediu de baza: " + department.getAverageMonthlySalary());
    }

    private static void deleteDepartmentFlow() {
        Department department = chooseDepartment();
        if (department == null) return;

        service.removeDepartment(department.getId());
        departments.remove(department.getId());
        ruleazaPersistenta("stergere departament", () -> serviciuDepartamenteJdbc.sterge(department.getId()));
        println("Departament sters.");
    }
    private static void addManagerFlow() {
        int id = readInt("ID angajat: ");
        String fullName = readNonEmpty("Nume complet: ");
        String email = readNonEmpty("Email: ");
        double salary = readDouble("Salariu: ");
        Department department = chooseDepartment();
        if (department == null) return;

        int teamSize = readInt("Marime echipa: ");
        double bonus = readDouble("Bonus management: ");

        Manager manager = new Manager(id, fullName, email, salary, department, teamSize, bonus);
        employees.put(id, manager);
        service.hireEmployee(manager, department.getId());
        ruleazaPersistenta("creare angajat", () -> serviciuAngajatiJdbc.creeaza(manager));

        println("Manager adaugat.");
    }

    private static void addDeveloperFlow() {
        int id = readInt("ID angajat: ");
        String fullName = readNonEmpty("Nume complet: ");
        String email = readNonEmpty("Email: ");
        double salary = readDouble("Salariu: ");
        Department department = chooseDepartment();
        if (department == null) return;

        String mainLanguage = readNonEmpty("Limbaj principal: ");

        Developer developer = new Developer(id, fullName, email, salary, department);
        developer.learnSkill(mainLanguage);
        employees.put(id, developer);
        service.hireEmployee(developer, department.getId());
        ruleazaPersistenta("creare angajat", () -> serviciuAngajatiJdbc.creeaza(developer));

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
        ruleazaPersistenta("actualizare angajat", () -> serviciuAngajatiJdbc.actualizeaza(employee));
        println("Numele a fost actualizat.");
    }

    private static void changeEmployeeEmailFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        String newEmail = readNonEmpty("Email nou: ");
        employee.setEmail(newEmail);
        ruleazaPersistenta("actualizare angajat", () -> serviciuAngajatiJdbc.actualizeaza(employee));
        println("Email-ul a fost actualizat.");
    }

    private static void applyRaiseFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        double percent = readDouble("Procent marire: ");
        employee.applyRaise(percent);
        ruleazaPersistenta("actualizare angajat", () -> serviciuAngajatiJdbc.actualizeaza(employee));
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
        ruleazaPersistenta("actualizare angajat", () -> serviciuAngajatiJdbc.actualizeaza(employee));

        println("Angajat transferat In departamentul: " + target.getName());
    }

    private static void evaluateEmployeePerformanceFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        double score = service.evaluateEmployeePerformance(employee.getId());
        double altScore = service.calculatePerformanceScore(employee.getId());

        println("=== Performanta ===");
        println("Angajat: " + employee.getFullName());
        println("Score evaluare: " + String.format("%.2f", score));
        println("Scor operational: " + String.format("%.2f", altScore));
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
            println("Angajatul nu este developer.");
            return;
        }

        int teamSize = readInt("Dimensiune noua a echipei: ");
        double bonus = readDouble("Bonus manager: ");

        Manager promoted = service.promoteDeveloperToManager(employee.getId(), teamSize, bonus);
        employees.put(promoted.getId(), promoted);
        ruleazaPersistenta("actualizare angajat", () -> serviciuAngajatiJdbc.actualizeaza(promoted));

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

    private static void deleteEmployeeFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        service.removeEmployee(employee.getId());
        employees.remove(employee.getId());
        ruleazaPersistenta("stergere angajat", () -> serviciuAngajatiJdbc.sterge(employee.getId()));
        println("Angajat sters.");
    }

    private static void developerOpsFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        if (!(employee instanceof Developer developer)) {
            println("Angajatul nu este developer.");
            return;
        }

        boolean back = false;
        while (!back) {
            println("");
            println("----- Optiuni developer: " + developer.getFullName() + " -----");
            println("1. Adauga skill");
            println("2. Elimina skill");
            println("3. Listeaza skill-uri");
            println("4. Verifica skill");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu = "meniu_developer";

            switch (choice) {
                case 1 -> {
                    String skill = readNonEmpty("Skill: ");
                    developer.learnSkill(skill);
                    ruleazaPersistenta("developer_update", () -> serviciuAngajatiJdbc.actualizeaza(developer));
                    inregistreazaActiune(menu + "_adauga_skill");
                }
                case 2 -> {
                    String skill = readNonEmpty("Skill de eliminat: ");
                    developer.forgetSkill(skill);
                    ruleazaPersistenta("developer_update", () -> serviciuAngajatiJdbc.actualizeaza(developer));
                    inregistreazaActiune(menu + "_sterge_skill");
                }
                case 3 -> {
                    println("Skill-uri: " + developer.getSkills());
                    inregistreazaActiune(menu + "_afisare_skilluri");
                }
                case 4 -> {
                    String skill = readNonEmpty("Skill: ");
                    println("Are skill? " + developer.hasSkill(skill));
                    inregistreazaActiune(menu + "_verificare_skill");
                }
                case 0 -> {
                    back = true;
                    inregistreazaActiune(menu + "_inapoi");
                }
                default -> println("Optiune invalida.");
            }
        }
    }

    private static void managerOpsFlow() {
        Employee employee = chooseEmployee();
        if (employee == null) return;

        if (!(employee instanceof Manager manager)) {
            println("Angajatul nu este manager.");
            return;
        }

        boolean back = false;
        while (!back) {
            println("");
            println("----- Optiuni manager: " + manager.getFullName() + " -----");
            println("1. Creste dimensiunea echipei");
            println("2. Scade dimensiunea echipei");
            println("3. Schimba bonus management");
            println("4. Afiseaza team size");
            println("5. Verifica aprobarea de buget");
            println("0. Inapoi");

            int choice = readInt("Alege: ");
            String menu = "meniu_manager";

            switch (choice) {
                case 1 -> {
                    manager.increaseTeamSize();
                    ruleazaPersistenta("manager_update", () -> serviciuAngajatiJdbc.actualizeaza(manager));
                    inregistreazaActiune(menu + "_creste_echipa");
                }
                case 2 -> {
                    manager.decreaseTeamSize();
                    ruleazaPersistenta("manager_update", () -> serviciuAngajatiJdbc.actualizeaza(manager));
                    inregistreazaActiune(menu + "_scade_echipa");
                }
                case 3 -> {
                    double bonus = readDouble("Bonus nou: ");
                    manager.setManagementBonus(bonus);
                    ruleazaPersistenta("manager_update", () -> serviciuAngajatiJdbc.actualizeaza(manager));
                    inregistreazaActiune(menu + "_bonus");
                }
                case 4 -> {
                    println("Dimensiune echipa: " + manager.getTeamSize());
                    inregistreazaActiune(menu + "_afisare_echipa");
                }
                case 5 -> {
                    double amount = readDouble("Suma buget: ");
                    println("Poate aproba? " + manager.canApproveBudget(amount));
                    inregistreazaActiune(menu + "_verificare_buget");
                }
                case 0 -> {
                    back = true;
                    inregistreazaActiune(menu + "_inapoi");
                }
                default -> println("Optiune invalida.");
            }
        }
    }
    private static void addClientFlow() {
        int id = readInt("ID client: ");
        String name = readNonEmpty("Nume: ");
        String email = readNonEmpty("Email: ");
        String industry = readNonEmpty("Industrie: ");

        Client client = new Client(id, name, email, industry);
        clients.put(id, client);
        service.addClient(client);
        ruleazaPersistenta("creare client", () -> serviciuClientiJdbc.creeaza(client));

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
                            ", nume=" + c.getName() +
                            ", industrie=" + c.getIndustry() +
                            ", proiecte=" + c.getProjects().size() +
                            ", scorSanatate=" + String.format("%.2f", c.calculateAccountHealthScore()) +
                            ", nivel=" + c.getAccountTier()
            );
        });
    }

    private static void updateClientContactFlow() {
        Client client = chooseClient();
        if (client == null) return;

        String newName = readNonEmpty("Nume nou: ");
        String newEmail = readNonEmpty("Email nou: ");
        client.updateContact(newName, newEmail);
        ruleazaPersistenta("actualizare client", () -> serviciuClientiJdbc.actualizeaza(client));
        println("Contact actualizat.");
    }

    private static void changeClientCompanyFlow() {
        Client client = chooseClient();
        if (client == null) return;

        String newCompany = readNonEmpty("Companie noua: ");
        client.changeCompany(newCompany);
        ruleazaPersistenta("actualizare client", () -> serviciuClientiJdbc.actualizeaza(client));
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
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));

        println("Client atribuit proiectului.");
    }

    private static void deleteClientFlow() {
        Client client = chooseClient();
        if (client == null) return;

        service.removeClient(client.getId());
        clients.remove(client.getId());
        ruleazaPersistenta("stergere client", () -> serviciuClientiJdbc.sterge(client.getId()));
        println("Client sters.");
    }

    private static void clientHealthFlow() {
        Client client = chooseClient();
        if (client == null) return;

        println("Client: " + client.getName());
        println("Scor sanatate: " + String.format("%.2f", client.calculateAccountHealthScore()));
        println("Nivel: " + client.getAccountTier());
        println("Proiecte active: " + client.getActiveProjectsCount());
        println("Proiecte finalizate: " + client.getCompletedProjectsCount());
        println("Proiecte cu risc: " + client.getRiskyProjectsCount());
    }

    private static void addProjectFlow() {
        int id = readInt("ID proiect: ");
        String name = readNonEmpty("Nume proiect: ");
        String description = readNonEmpty("Descriere: ");
        double budget = readDouble("Buget: ");
        LocalDate startDate = readDate("Data start (YYYY-MM-DD): ");
        LocalDate deadline = readDate("Termen limita (YYYY-MM-DD): ");
        Department department = chooseDepartment();
        if (department == null) return;

        Client client = null;
        String attachClient = readNonEmpty("Atasezi client acum? (y/n): ");
        if (attachClient.equalsIgnoreCase("y")) {
            client = chooseClient();
            if (client == null) return;
        }

            Project project = new Project(id, name, description, department, client, budget, deadline, startDate, ProjectStatus.PLANNED);
        projects.put(id, project);
        service.addProject(project);

        if (client != null) {
            client.registerProject(project);
        }
        ruleazaPersistenta("creare proiect", () -> serviciuProiecteJdbc.creeaza(project));

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
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        println("Proiect redenumit.");
    }

    private static void updateProjectBudgetFlow() {
        Project project = chooseProject();
        if (project == null) return;

        double budget = readDouble("Buget nou: ");
        project.updateBudget(budget);
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        println("Buget actualizat.");
    }

    private static void changeProjectDeadlineFlow() {
        Project project = chooseProject();
        if (project == null) return;

        LocalDate deadline = readDate("Termen limita nou (YYYY-MM-DD): ");
        project.changeDeadline(deadline);
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        println("Termenul limita a fost actualizat.");
    }

    private static void changeProjectStatusFlow() {
        Project project = chooseProject();
        if (project == null) return;

        ProjectStatus status = readProjectStatus("Status nou: ");
        project.changeStatus(status);
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
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
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        println("Skill adaugat proiectului.");
    }

    private static void reassignProjectDepartmentFlow() {
        Project project = chooseProject();
        if (project == null) return;

        Department department = chooseDepartment();
        if (department == null) return;

        project.reassignDepartment(department);
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        println("Departamentul proiectului a fost schimbat.");
    }

    private static void assignEmployeeToProjectFlow() {
        Project project = chooseProject();
        if (project == null) return;

        Employee employee = chooseEmployee();
        if (employee == null) return;

        service.assignEmployeeToProject(employee.getId(), project.getId());
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        println("Angajat asignat la proiect.");
    }

    private static void projectReportFlow() {
        Project project = chooseProject();
        if (project == null) return;

        println("=== Raport proiect ===");
        println(formatProject(project));
        println("Angajati in proiect:");
        project.getEmployees().forEach(e -> println(" - " + e.getFullName()));
        println("Skill-uri necesare: " + project.getRequiredSkills());
        println("Prioritate mare, cost, profit si risc:");
        println("Cost: " + service.calculateProjectCost(project.getId()));
        println("Profit: " + service.calculateProjectProfit(project.getId()));
        println("Cu risc: " + service.isProjectAtRisk(project.getId()));
        println("Finalizare estimata: " + service.estimateCompletionDate(project.getId()));
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
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        println("Proiect marcat ca anulat.");
    }

    private static void deleteProjectFlow() {
        Project project = chooseProject();
        if (project == null) return;

        service.removeProject(project.getId());
        projects.remove(project.getId());
        ruleazaPersistenta("stergere proiect", () -> serviciuProiecteJdbc.sterge(project.getId()));
        println("Proiect sters.");
    }

    private static void addTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int id = readInt("ID task: ");
        String title = readNonEmpty("Titlu: ");
        String description = readNonEmpty("Descriere: ");
        LocalDate dueDate = readDate("Data limita (YYYY-MM-DD): ");
        int difficulty = readInt("Dificultate (1-10): ");

        Task task = new Task(id, title, description, dueDate, difficulty);
        service.addTaskToProject(project.getId(), task);
        ruleazaPersistenta("creare task", () -> serviciuTaskuriJdbc.creeaza(task));
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));

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

        int taskId = readInt("ID task: ");
        Employee employee = chooseEmployee();
        if (employee == null) return;

        service.assignTaskToEmployee(project.getId(), taskId, employee.getId());
        Task task = findTask(project, taskId);
        if (task != null) {
            ruleazaPersistenta("actualizare task", () -> serviciuTaskuriJdbc.actualizeaza(task));
            ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        }
        println("Task asignat angajatului.");
    }

    private static void completeTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("ID task: ");
        service.completeTask(project.getId(), taskId);
        Task task = findTask(project, taskId);
        if (task != null) {
            ruleazaPersistenta("actualizare task", () -> serviciuTaskuriJdbc.actualizeaza(task));
            ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        }
        println("Task marcat ca finalizat.");
    }

    private static void blockTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("ID task: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        task.block();
        ruleazaPersistenta("actualizare task", () -> serviciuTaskuriJdbc.actualizeaza(task));
        println("Task blocat.");
    }

    private static void reopenTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("ID task: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        task.reopen();
        ruleazaPersistenta("actualizare task", () -> serviciuTaskuriJdbc.actualizeaza(task));
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

        int taskId = readInt("ID task: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        String newTitle = readNonEmpty("Titlu nou: ");
        task.setTitle(newTitle);
        ruleazaPersistenta("actualizare task", () -> serviciuTaskuriJdbc.actualizeaza(task));
        println("Task redenumit.");
    }

    private static void rewriteTaskDescriptionFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("ID task: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        String newDescription = readNonEmpty("Descriere noua: ");
        task.setDescription(newDescription);
        ruleazaPersistenta("actualizare task", () -> serviciuTaskuriJdbc.actualizeaza(task));
        println("Descriere actualizata.");
    }

    private static void deleteTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("ID task: ");
        Task task = findTask(project, taskId);
        if (task == null) return;

        project.removeTask(taskId);
        ruleazaPersistenta("stergere task", () -> serviciuTaskuriJdbc.sterge(taskId));
        ruleazaPersistenta("actualizare proiect", () -> serviciuProiecteJdbc.actualizeaza(project));
        println("Task sters.");
    }

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

        println("=== Cei mai performanti angajati ===");
        ranked.forEach(e -> println(
                e.getFullName() +
                        " | scor=" + String.format("%.2f", service.evaluateEmployeePerformance(e.getId())) +
                        " | rol=" + e.getRole()
        ));
    }

    private static void bestDeveloperForTaskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        int taskId = readInt("ID task: ");

        Employee best = service.assignBestDeveloperToTask(project.getId(), taskId);
        println("Cel mai bun developer pentru task: " + best);
    }

    private static void projectRiskFlow() {
        Project project = chooseProject();
        if (project == null) return;

        println("Proiect: " + project.getName());
        println("Cu risc: " + service.isProjectAtRisk(project.getId()));
        println("Progres: " + project.progress());
        println("Termen limita: " + project.getDeadline());
    }

    private static void projectCostProfitFlow() {
        Project project = chooseProject();
        if (project == null) return;

        println("Proiect: " + project.getName());
        println("Cost: " + service.calculateProjectCost(project.getId()));
        println("Profit: " + service.calculateProjectProfit(project.getId()));
        println("Cu risc: " + service.isProjectAtRisk(project.getId()));
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
        int id = readInt("ID departament ales: ");
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
        int id = readInt("ID angajat ales: ");
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
        int id = readInt("ID client ales: ");
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
        int id = readInt("ID proiect ales: ");
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
                + ", nume=" + e.getFullName()
                + ", rol=" + e.getRole()
                + ", salariu=" + e.getSalary()
                + ", departament=" + (e.getDepartment() != null ? e.getDepartment().getName() : "niciunul")
                + ", dataAngajare=" + e.getHireDate();
    }

    private static String formatProject(Project p) {
        return "ID=" + p.getId()
                + ", nume=" + p.getName()
                + ", status=" + p.getStatus()
                + ", buget=" + p.getBudget()
                + ", termenLimita=" + p.getDeadline()
                + ", progres=" + p.progress()
                + ", client=" + (p.getClient() != null ? p.getClient().getName() : "niciunul")
                + ", departament=" + (p.getDepartment() != null ? p.getDepartment().getName() : "niciunul");
    }

    private static String formatTask(Task t) {
        return "ID=" + t.getId()
                + ", titlu=" + t.getTitle()
                + ", status=" + t.getStatus()
                + ", prioritate=" + t.getPriority()
                + ", dificultate=" + t.getDifficulty()
                + ", dataLimita=" + t.getDueDate()
                + ", responsabil=" + (t.getEmployee() != null ? t.getEmployee().getFullName() : "niciunul");
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
