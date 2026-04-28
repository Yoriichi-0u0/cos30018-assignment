package models;

import java.io.Serializable;

public class Car implements Serializable {
    private String make;
    private String model;
    private int year;
    private double price;

    // Constructor
    public Car(String make, String model, int year, double price) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
    }

    // Getters
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public double getPrice() { return price; }

    // Setters
    public void setMake(String make) { this.make = make; }
    public void setModel(String model) { this.model = model; }
    public void setYear(int year) { this.year = year; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        // Example output: 2014 Perodua Viva 1.0L - RM15000.0
        return year + " " + make + " " + model + " - RM" + price;
    }
}