package com.polaris.sort.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    private double x1;

    private double y1;

    private double x2;

    private double y2;

    private double score;

    public double[] toArray() {
        return new double[]{x1, y1, x2, y2, score};
    }


}
