package temperature;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    
    public static void processBatch(String filename) {
        List<String> lines;
        
        try {
            lines = Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            System.out.println("Error: File not found.");
            return;
        }
        
        List<Double> temps = new ArrayList<>();
        List<String> timestamps = new ArrayList<>();
        int errors = 0;
        List<String> badLines = new ArrayList<>();
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            
            String[] parts = line.split(",");
            if (parts.length != 2) {
                errors++;
                badLines.add("  Line " + (i + 1) + ": " + line);
                continue;
            }
            
            String timestamp = parts[0].strip();
            String value = parts[1].strip();
            
            // Validate timestamp
            if (timestamp.split(":").length != 3) {
                errors++;
                badLines.add("  Line " + (i + 1) + ": " + line);
                continue;
            }
            
            double temp;
            try {
                temp = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                errors++;
                badLines.add("  Line " + (i + 1) + ": " + line);
                continue;
            }
            
            // Drop impossible temperatures
            if (temp < -100 || temp > 200) {
                errors++;
                badLines.add("  Line " + (i + 1) + ": " + line);
                continue;
            }
            
            temps.add(temp);
            timestamps.add(timestamp);
        }
        
        if (temps.isEmpty()) {
            System.out.println("No valid temperature data found.");
            return;
        }
        
        // Calculate statistics
        double maxTemp = Collections.max(temps);
        double minTemp = Collections.min(temps);
        double avgTemp = temps.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // Print summary
        System.out.println("============================================================");
        System.out.println("Temperature Analysis Summary");
        System.out.println("============================================================");
        System.out.println("Total readings: " + lines.size());
        System.out.println("Valid readings: " + temps.size());
        System.out.println("Errors: " + errors);
        System.out.println("------------------------------------------------------------");
        System.out.printf("Max temperature: %.2f%n", maxTemp);
        System.out.printf("Min temperature: %.2f%n", minTemp);
        System.out.printf("Average temperature: %.2f%n", avgTemp);
        System.out.println("------------------------------------------------------------");
        
        // Print invalid lines (verbose)
        if (errors > 0) {
            System.out.println("Invalid lines:");
            for (String badLine : badLines) {
                System.out.println(badLine);
            }
        }
        
        // Save report
        String outName = filename + "_summary.txt";
        try (PrintWriter out = new PrintWriter(new FileWriter(outName))) {
            out.println("Temperature Analysis Summary");
            out.println("==================================================");
            out.println("File analyzed: " + filename);
            out.println("Total readings: " + lines.size());
            out.println("Valid readings: " + temps.size());
            out.println("Errors: " + errors);
            out.printf("Max temperature: %.2f%n", maxTemp);
            out.printf("Min temperature: %.2f%n", minTemp);
            out.printf("Average temperature: %.2f%n", avgTemp);
            out.println("------------------------------------------------------------");
            if (errors > 0) {
                out.println("\nInvalid lines:");
                for (String badLine : badLines) {
                    out.println(badLine);
                }
            }
            System.out.println("Report saved to " + outName);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        // Generate test data file
        String testFilename = "test_temps.csv";
        String[] testData = {
            "09:15:30,23.5",
            "09:16:00,24.1",
            "09:16:30,22.8",
            "09:17:00,25.3",
            "09:17:30,23.9",
            "09:18:00,24.7",
            "09:18:30,22.4",
            "09:19:00,26.1",
            "09:19:30,23.2",
            "09:20:00,25.0"
        };
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(testFilename))) {
            for (String line : testData) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error creating test file: " + e.getMessage());
            return;
        }
        
        System.out.println("Created test file: " + testFilename);
        
        // Process the test file
        processBatch(testFilename);
        
        // Verify the summary file was created
        String summaryFile = testFilename + "_summary.txt";
        Path summaryPath = Paths.get(summaryFile);
        if (Files.exists(summaryPath)) {
            System.out.println("\nSummary file created: " + summaryFile);
            try {
                String content = Files.readString(summaryPath);
                assert content.contains("Total readings: 10") : "Total readings assertion failed";
                assert content.contains("Valid readings: 10") : "Valid readings assertion failed";
                assert content.contains("Errors: 0") : "Errors assertion failed";
                System.out.println("✓ Summary file contents verified");
            } catch (IOException e) {
                System.out.println("Error reading summary file: " + e.getMessage());
            }
        }
        
        // Clean up test files
        try {
            Files.deleteIfExists(Paths.get(testFilename));
            Files.deleteIfExists(summaryPath);
        } catch (IOException e) {
            System.out.println("Error cleaning up files: " + e.getMessage());
        }
    }
}