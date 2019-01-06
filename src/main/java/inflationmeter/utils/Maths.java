package inflationmeter.utils;

import java.util.Random;

public class Maths {

    public static int rnd(int min, int max) {
        return min + new Random().nextInt(max - min);
    }

}
