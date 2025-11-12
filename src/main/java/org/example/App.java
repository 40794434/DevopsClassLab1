package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class App {

    private Connection con = null;

    // -----------------------
    // Database Connection
    // -----------------------
    public void connect(String location, int delay) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                Thread.sleep(delay);
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + location + "/employees?useSSL=false&allowPublicKeyRetrieval=true",
                        "root", "example");
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    // -----------------------
    // Queries
    // -----------------------
    public Employee getEmployee(int ID) {
        try {
            Statement stmt = con.createStatement();
            String strSelect = "SELECT emp_no, first_name, last_name FROM employees WHERE emp_no = " + ID;
            ResultSet rset = stmt.executeQuery(strSelect);

            if (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                return emp;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    public ArrayList<Employee> getAllSalaries() {
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
                            + "FROM employees, salaries "
                            + "WHERE employees.emp_no = salaries.emp_no AND salaries.to_date = '9999-01-01' "
                            + "ORDER BY employees.emp_no ASC";
            ResultSet rset = stmt.executeQuery(strSelect);

            ArrayList<Employee> employees = new ArrayList<>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                employees.add(emp);
            }
            return employees;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary details");
            return null;
        }
    }

    public ArrayList<Employee> getSalariesByRole(String role) {
        try {
            String sql = "SELECT e.emp_no, e.first_name, e.last_name, s.salary "
                    + "FROM employees e "
                    + "JOIN titles t ON e.emp_no = t.emp_no "
                    + "JOIN salaries s ON e.emp_no = s.emp_no "
                    + "WHERE t.to_date = '9999-01-01' AND s.to_date = '9999-01-01' "
                    + "AND t.title = ? "
                    + "ORDER BY e.emp_no ASC";

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, role);
            ResultSet rset = pstmt.executeQuery();

            ArrayList<Employee> employees = new ArrayList<>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.salary = rset.getInt("salary");
                employees.add(emp);
            }
            return employees;
        } catch (Exception e) {
            System.out.println("Failed to get salaries by role");
            System.out.println(e.getMessage());
            return null;
        }
    }

    // -----------------------
    // Output to Console & File
    // -----------------------
    public void printSalaries(ArrayList<Employee> employees) {
        if (employees == null) {
            System.out.println("No employees");
            return;
        }

        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));
        for (Employee emp : employees) {
            if (emp == null)
                continue;
            System.out.println(String.format("%-10s %-15s %-20s %-8s",
                    emp.emp_no, emp.first_name, emp.last_name, emp.salary));
        }
    }

    public void outputEmployees(ArrayList<Employee> employees, String filename) {
        if (employees == null) {
            System.out.println("No employees to write");
            return;
        }

        try {
            java.io.File dir = new java.io.File("reports");
            if (!dir.exists()) {
                dir.mkdir();
            }

            FileWriter writer = new FileWriter("reports/" + filename);
            writer.write("# Employee Salaries Report\n\n");
            writer.write("| Emp No | First Name | Last Name | Salary |\n");
            writer.write("|--------|-------------|------------|---------|\n");

            for (Employee emp : employees) {
                if (emp == null) continue;
                writer.write(String.format("| %d | %s | %s | %d |\n",
                        emp.emp_no, emp.first_name, emp.last_name, emp.salary));
            }
            writer.close();
            System.out.println("Report written to reports/" + filename);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    // -----------------------
    // Main Method
    // -----------------------
    public static void main(String[] args) {
        App app = new App();

        // Connect to database
        if (args.length < 1) {
            app.connect("localhost:33060", 0);
        } else {
            app.connect(args[0], Integer.parseInt(args[1]));
        }

        // Get all managers' salaries and output them
        ArrayList<Employee> employees = app.getSalariesByRole("Manager");
        app.printSalaries(employees);
        app.outputEmployees(employees, "ManagerSalaries.md");

        // Disconnect
        app.disconnect();
    }
}
