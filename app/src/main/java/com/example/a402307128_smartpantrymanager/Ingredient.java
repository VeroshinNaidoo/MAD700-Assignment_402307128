package com.example.a402307128_smartpantrymanager;

/**
 * Model class for one pantry item (an ingredient the user currently owns).
 * Maps to one row of the "pantry" table in the SQLite database.
 */
public class Ingredient {

    private long id;          // primary key (assigned by SQLite)
    private String name;      // e.g. "Tomato"
    private double quantity;  // e.g. 2.5
    private String unit;      // one of the values in R.array.units_array
    private String expiry;    // ISO date "yyyy-MM-dd", or null when no expiry was entered

    /** Full constructor, used when reading a row back from the database. */
    public Ingredient(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }

    /** Convenience constructor for a new item that has not been saved yet. */
    public Ingredient(String name, double quantity, String unit, String expiry) {
        this(0, name, quantity, unit, expiry);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiry() { return expiry; }
}

