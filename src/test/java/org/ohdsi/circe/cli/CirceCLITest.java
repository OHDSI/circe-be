package org.ohdsi.circe.cli;

import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Test for the CirceCLI wrapper functionality
 */
public class CirceCLITest {

    @Test
    public void testVersionCommand() {
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Test version command
            CirceCLI.main(new String[]{"version"});
            String output = outContent.toString();
            assertTrue("Version output should contain version info", 
                output.contains("Circe library version"));
            assertTrue("Version output should contain version number", 
                output.contains("1.12.1-SNAPSHOT"));
        } finally {
            // Restore original output
            System.setOut(originalOut);
        }
    }

    @Test
    public void testNoArgumentsShowsUsage() {
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Test with no arguments
            CirceCLI.main(new String[]{});
            String output = outContent.toString();
            assertTrue("Should show usage information", 
                output.contains("Usage: circe-cli"));
            assertTrue("Should show available commands", 
                output.contains("Commands:"));
        } finally {
            // Restore original output
            System.setOut(originalOut);
        }
    }

    @Test
    public void testInvalidCommand() {
        // Capture system error output
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        // Capture system output for usage
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Test with invalid command
            CirceCLI.main(new String[]{"invalid-command"});
            String errorOutput = errContent.toString();
            String output = outContent.toString();
            
            assertTrue("Should show error for unknown command", 
                errorOutput.contains("Unknown command"));
            assertTrue("Should show usage after unknown command", 
                output.contains("Usage: circe-cli"));
        } finally {
            // Restore original outputs
            System.setErr(originalErr);
            System.setOut(originalOut);
        }
    }

    @Test
    public void testValidateCohortWithInvalidJson() {
        // Capture system error output
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Test with invalid JSON
            CirceCLI.main(new String[]{"validate-cohort", "invalid-json"});
            String errorOutput = errContent.toString();
            assertTrue("Should show error for invalid JSON", 
                errorOutput.contains("Error validating cohort"));
        } finally {
            // Restore original error output
            System.setErr(originalErr);
        }
    }

    @Test
    public void testValidateConceptSetWithInvalidJson() {
        // Capture system error output
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Test with invalid JSON
            CirceCLI.main(new String[]{"validate-conceptset", "invalid-json"});
            String errorOutput = errContent.toString();
            assertTrue("Should show error for invalid JSON", 
                errorOutput.contains("Error validating concept set"));
        } finally {
            // Restore original error output
            System.setErr(originalErr);
        }
    }
}