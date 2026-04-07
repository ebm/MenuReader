package com.example.menureader;

import com.example.menureader.Handling.Controller;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ControllerTest {
    @Test
    public void testControllerStringSize() {
        assertEquals(50, Controller.getStringSize(""));
        assertEquals(50 + 2, Controller.getStringSize("a"));
        assertEquals(50 + 8, Controller.getStringSize("abcd"));
    }
}
