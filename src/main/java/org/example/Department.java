package org.example;

public class Department {
    public String dept_no;
    public String dept_name;

    @Override
    public String toString() {
        return "Department{" +
                "dept_no='" + dept_no + '\'' +
                ", dept_name='" + dept_name + '\'' +
                ", manager=" + manager +
                '}';
    }

    public Employee manager;
}
