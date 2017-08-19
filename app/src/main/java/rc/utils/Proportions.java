package rc.utils;

public class Proportions {
    public static float linearProportion(float val, float valMin, float valMax,
                                   float targetMin, float targetMax) {
        return (val - valMin) / (valMax - valMin) * (targetMax - targetMin)
                + targetMin;
    }

    public static float linearProportion(float val, float valMin, float valMax,
                                         float targetMin, float targetCenter, float targetMax) {
        float valHalf = (valMax + valMin) / 2;
        if (val > valHalf) {
            return linearProportion(val, valHalf, valMax, targetCenter, targetMax);
        } else if (val < valHalf) {
            return linearProportion(val, valMin, valHalf, targetMin, targetCenter);
        } else {
            return targetCenter;
        }

    }
}
