package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class AppIntegrationTest {
    static App app;

    @BeforeAll
    static void init() {
        app = new App();
        app.connect("localhost:33060", 30000);

    }

    @Test
    void testDatabaseConnection() {
        // Test that we can connect and query the database
        ArrayList<Employee> employees = app.getAllSalaries();
        assertNotNull(employees, "Should be able to retrieve employees");
        assertTrue(employees.size() > 0, "Should have at least one employee");
    }
    @Test
    void testGetEmployee() {
        Employee emp = app.getEmployee(255530);
        assertNotNull(emp, "Employee should not be null. Check DB initialization.");
        if (emp != null) {
            System.out.print("Employee ID : " + emp.first_name);
            assertEquals(255530, emp.emp_no);
            assertEquals("Ronghao", emp.first_name);
            assertEquals("Garigliano", emp.last_name);
        }
    }


}