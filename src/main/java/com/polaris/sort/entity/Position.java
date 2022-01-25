package com.polaris.sort.entity;

import lombok.Data;

@Data
public class Position {

    private double x;

    private double y;

    private double width;

    private double height;

    public Position(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
