package inflationmeter.utils;


import static inflationmeter.utils.Maths.rnd;

public class Wait {

    public static void freeze (int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void freeze (int minMills, int maxMills){
        int random = rnd(minMills, maxMills);
        freeze(random);
    }

}
