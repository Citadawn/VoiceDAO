package com.citadawn.speechapp.ui.test;

public class TestCase {
    public final String id;
    public String name;
    public String description;
    public boolean selected;

    public TestCase(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.selected = false;
    }
} 