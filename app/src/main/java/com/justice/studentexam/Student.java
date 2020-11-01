package com.justice.studentexam;

public class Student {
    private String firstName;
    private String lastName;
    private String studentId;
    private Results results;

    public Student() {
    }

    public Student(String firstName, String lastName,String studentId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentId = studentId;


    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
