package rs.moma.flightforge.model;

import lombok.Getter;

@Getter
public class WeightHolder {
    private double maxCheckedWeight;

    public boolean recordAndReturn(double weight) {
        maxCheckedWeight = Math.max(maxCheckedWeight, weight);
        return true;
    }
}