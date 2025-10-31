package com.health_sync.Util;

import java.sql.Connection;
import java.sql.Statement;

public class DBcreate {
    public static void main(String[] args) {
        try (Connection con = DBconnection.getConnection();
             Statement stmt = con.createStatement()) {

            // Create Patient Table
            String patientTable = """
                CREATE TABLE IF NOT EXISTS Patient (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    password VARCHAR(50) NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    age INT NOT NULL,
                    sex VARCHAR(10) NOT NULL,
                    address VARCHAR(255) NOT NULL
                );
            """;

            // Create Doctor Table
            String doctorTable = """
                CREATE TABLE IF NOT EXISTS Doctor (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    password VARCHAR(50) NOT NULL,
                    mbbs_id VARCHAR(50) NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    clinic_name VARCHAR(100) NOT NULL,
                    clinic_address VARCHAR(255) NOT NULL,
                    specialisation VARCHAR(100),
                    rating FLOAT DEFAULT 1,
                    working_days VARCHAR(100),
                    working_hours VARCHAR(100)
                );
            """;

            // Create PatientHistory Table
            String patientHistoryTable = """
                CREATE TABLE IF NOT EXISTS PatientHistory (
                    history_id INT AUTO_INCREMENT PRIMARY KEY,
                    patient_id INT NOT NULL,
                    doctor_id INT NOT NULL,
                    disease VARCHAR(100) NOT NULL,
                    appointment_date DATE NOT NULL,
                    appointment_time TIME NOT NULL,
                    fees DECIMAL(10,2) NOT NULL,
                    disease_description TEXT,
                    FOREIGN KEY (patient_id) REFERENCES Patient(id),
                    FOREIGN KEY (doctor_id) REFERENCES Doctor(id)
                );
            """;

            String admin = """ 
                CREATE TABLE IF NOT EXISTS Admin (
                    admin_id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(50) NOT NULL
            );
        """;
        String feedback = """
                CREATE TABLE IF NOT EXISTS Feedback (
                    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
                    doctor_id INT NOT NULL,
                    patient_id INT NOT NULL,
                    rating INT CHECK (rating BETWEEN 1 AND 5),
                    comments TEXT,
                    feedback_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (doctor_id) REFERENCES Doctor(id),
                    FOREIGN KEY (patient_id) REFERENCES Patient(id)
                );
                """;


        stmt.execute(feedback);

        boolean result = stmt.execute(admin);
        stmt.execute(patientTable);
        stmt.execute(doctorTable);
        stmt.execute(patientHistoryTable);
        if (result == true) {                             //here if admi table is created for the first time then only insert default admin
    String insertAdmin = """                            
    INSERT INTO Admin (username, password)
    VALUES ('health-sync', 'health@123');
    """;
    stmt.executeUpdate(insertAdmin);
    System.out.println("Admin table created and default admin inserted.");
}

            System.out.println("All Tables Created Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

