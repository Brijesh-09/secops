package com.mycompany;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class
 * This is the Java code that gets scanned by SonarCloud
 * and packaged into a .jar by Maven
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        App app = new App();
        logger.info("DevSecOps Pipeline is working!");
        System.out.println(app.greet("DevSecOps"));
    }

    /**
     * Returns a greeting message
     * @param name the name to greet
     * @return greeting string
     */
    public String greet(String name) {
        if (name == null || name.isEmpty()) {
            return "Hello, World!";
        }
        return "Hello, " + name + "!";
    }

    /**
     * Adds two numbers — simple logic for SonarCloud to analyse
     */
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * Checks if a number is positive
     */
    public boolean isPositive(int number) {
        return number > 0;
    }
}
