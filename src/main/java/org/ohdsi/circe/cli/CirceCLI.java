package org.ohdsi.circe.cli;

/**
 * Simple CLI wrapper for Circe library functionality
 * This is a minimal implementation for backward compatibility
 */
public class CirceCLI {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            showHelp();
            return;
        }

        String command = args[0].toLowerCase();
        
        switch (command) {
            case "version":
                showVersion();
                break;
            case "help":
            case "-h":
            case "--help":
                showHelp();
                break;
            default:
                System.err.println("Unknown command: " + command);
                showHelp();
        }
    }
    
    private static void showVersion() {
        System.out.println("Circe library version: 1.12.1-SNAPSHOT");
        System.out.println("Build: Shared Library Edition");
    }
    
    private static void showHelp() {
        System.out.println("Circe CLI - Cohort Definition Library");
        System.out.println("");
        System.out.println("Usage: java -jar circe-cli.jar [command]");
        System.out.println("");
        System.out.println("Commands:");
        System.out.println("  version    Show version information");
        System.out.println("  help       Show this help message");
        System.out.println("");
        System.out.println("Note: This is a shared library build. Use the native library");
        System.out.println("      functions for cohort definition operations.");
    }
}
