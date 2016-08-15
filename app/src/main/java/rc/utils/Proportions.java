package rc.utils;

public class Proportions {
    public static float linearProportion(float val, float valMin, float valMax,
                                   float targetMin, float targetMax) {
        return (val - valMin) / (valMax - valMin) * (targetMax - targetMin)
                + targetMin;
    }
}
