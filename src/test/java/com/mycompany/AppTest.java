package com.mycompany;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for App.java
 * JaCoCo measures how much of App.java these tests cover
 * SonarCloud shows that coverage % on its dashboard
 */
class AppTest {

    private final App app = new App();

    @Test
    @DisplayName("Greet with a valid name")
    void testGreetWithName() {
        assertEquals("Hello, Brijesh!", app.greet("Brijesh"));
    }

    @Test
    @DisplayName("Greet with null returns default")
    void testGreetWithNull() {
        assertEquals("Hello, World!", app.greet(null));
    }

    @Test
    @DisplayName("Greet with empty string returns default")
    void testGreetWithEmpty() {
        assertEquals("Hello, World!", app.greet(""));
    }

    @Test
    @DisplayName("Add two positive numbers")
    void testAdd() {
        assertEquals(5, app.add(2, 3));
    }

    @Test
    @DisplayName("Add negative numbers")
    void testAddNegative() {
        assertEquals(-1, app.add(-3, 2));
    }

    @Test
    @DisplayName("Positive number returns true")
    void testIsPositive() {
        assertTrue(app.isPositive(10));
    }

    @Test
    @DisplayName("Negative number returns false")
    void testIsNotPositive() {
        assertFalse(app.isPositive(-5));
    }

    @Test
    @DisplayName("Zero is not positive")
    void testZeroIsNotPositive() {
        assertFalse(app.isPositive(0));
    }
}
