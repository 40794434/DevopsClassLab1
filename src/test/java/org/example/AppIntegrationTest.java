package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class AppIntegrationTest {
    static App app;

    @BeforeAll
    static void init() {
        app = new App();
        System.out.println("=== INITIALIZING DATABASE CONNECTION ===");
        app.connect("localhost:33060", 30000);
        System.out.println("=== DATABASE CONNECTION COMPLETED ===");
    }

    @Test
    void testDatabaseConnection() {
        System.out.println("=== STARTING testDatabaseConnection ===");

        // Test 1: Check if connection object exists
        System.out.println("1. Checking connection object...");
        assertNotNull(app.con, "Database connection should not be null");
        System.out.println("✓ Connection object exists");

        // Test 2: Check if we can execute a simple query
        System.out.println("2. Testing basic SQL execution...");
        try {
            Statement stmt = app.con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1 as test_value");
            assertTrue(rs.next(), "Should be able to execute simple query");
            assertEquals(1, rs.getInt("test_value"));
            System.out.println("✓ Basic SQL query works");
        } catch (Exception e) {
            System.out.println("✗ Basic SQL query failed: " + e.getMessage());
            fail("Basic SQL execution failed: " + e.getMessage());
        }

        // Test 3: Check what databases exist
        System.out.println("3. Checking available databases...");
        try {
            Statement stmt = app.con.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW DATABASES");
            System.out.println("Available databases:");
            boolean foundEmployeesDB = false;
            while (rs.next()) {
                String dbName = rs.getString(1);
                System.out.println("   - " + dbName);
                if ("employees".equals(dbName)) {
                    foundEmployeesDB = true;
                }
            }
            assertTrue(foundEmployeesDB, "Employees database should exist");
            System.out.println("✓ Employees database exists");
        } catch (Exception e) {
            System.out.println("✗ Failed to list databases: " + e.getMessage());
        }

        // Test 4: Check tables in employees database
        System.out.println("4. Checking tables in employees database...");
        try {
            Statement stmt = app.con.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES FROM employees");
            System.out.println("Tables in employees database:");
            boolean hasTables = false;
            while (rs.next()) {
                String tableName = rs.getString(1);
                System.out.println("   - " + tableName);
                hasTables = true;
            }
            if (!hasTables) {
                System.out.println("   ✗ No tables found in employees database!");
            } else {
                System.out.println("✓ Employees database has tables");
            }
        } catch (Exception e) {
            System.out.println("✗ Failed to list tables: " + e.getMessage());
        }

        // Test 5: Check if employees table has data
        System.out.println("5. Checking employees table data...");
        try {
            Statement stmt = app.con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM employees.employees");
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("   Employees table has " + count + " records");
                if (count == 0) {
                    System.out.println("   ✗ Employees table is empty!");
                } else {
                    System.out.println("✓ Employees table has data");
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Failed to count employees: " + e.getMessage());
        }

        // Test 6: Now try getAllSalaries()
        System.out.println("6. Testing getAllSalaries() method...");
        try {
            ArrayList<Employee> employees = app.getAllSalaries();
            System.out.println("   getAllSalaries() returned: " + employees);

            if (employees == null) {
                System.out.println("   ✗ getAllSalaries() returned null");
                // Let's see why by testing the query directly
                testGetAllSalariesQueryDirectly();
            } else {
                System.out.println("   ✓ getAllSalaries() returned " + employees.size() + " employees");
            }

            assertNotNull(employees, "Should be able to retrieve employees");
            assertTrue(employees.size() > 0, "Should have at least one employee");

        } catch (Exception e) {
            System.out.println("✗ getAllSalaries() threw exception: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== testDatabaseConnection COMPLETED ===");
    }

    private void testGetAllSalariesQueryDirectly() {
        System.out.println("   Debugging getAllSalaries query...");
        try {
            Statement stmt = app.con.createStatement();
            String testQuery =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary " +
                            "FROM employees, salaries " +
                            "WHERE employees.emp_no = salaries.emp_no AND salaries.to_date = '9999-01-01' " +
                            "ORDER BY employees.emp_no ASC LIMIT 5";

            ResultSet rs = stmt.executeQuery(testQuery);
            System.out.println("   Direct query results:");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("     - " + rs.getInt("emp_no") + ": " +
                        rs.getString("first_name") + " " +
                        rs.getString("last_name") + " - $" +
                        rs.getInt("salary"));
            }
            if (count == 0) {
                System.out.println("     ✗ No results from direct query - checking individual tables...");
                checkIndividualTables();
            }
        } catch (Exception e) {
            System.out.println("   ✗ Direct query failed: " + e.getMessage());
        }
    }

    private void checkIndividualTables() {
        try {
            Statement stmt = app.con.createStatement();

            // Check employees table
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM employees.employees");
            if (rs.next()) {
                System.out.println("     Employees table count: " + rs.getInt("count"));
            }

            // Check salaries table
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM employees.salaries");
            if (rs.next()) {
                System.out.println("     Salaries table count: " + rs.getInt("count"));
            }

            // Check if we have current salaries
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM employees.salaries WHERE to_date = '9999-01-01'");
            if (rs.next()) {
                System.out.println("     Current salaries count: " + rs.getInt("count"));
            }

        } catch (Exception e) {
            System.out.println("   ✗ Individual table check failed: " + e.getMessage());
        }
    }

    @Test
    void testSimpleEmployeeQuery() {
        System.out.println("=== Testing simple employee query ===");
        try {
            // Try a much simpler query first
            Statement stmt = app.con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT emp_no, first_name, last_name FROM employees.employees LIMIT 1");

            if (rs.next()) {
                Employee emp = new Employee();
                emp.emp_no = rs.getInt("emp_no");
                emp.first_name = rs.getString("first_name");
                emp.last_name = rs.getString("last_name");
                System.out.println("Found employee: " + emp.first_name + " " + emp.last_name);
                assertNotNull(emp.first_name);
            } else {
                System.out.println("No employees found in database!");
                fail("No employees found in database");
            }
        } catch (Exception e) {
            System.out.println("Simple employee query failed: " + e.getMessage());
            fail("Simple employee query failed: " + e.getMessage());
        }
    }
}