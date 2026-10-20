package vt.project.antares.tuto;

public class Program {


    static void main() {
        String s = "MCMXCIV"; //1994
        System.out.println("input : " + s + " output : " + romanToInt(s));
    }

    public static int romanToInt(String s) {
        String[] split = s.split("");
        int result = 0;
        for (int i = 0; i < split.length; i++) {
            boolean last = i == split.length - 1;
            if (!last && (split[i].equals("I") && split[i + 1].equals("V"))) {
                i++;
                result += 4;
            } else if (!last && (split[i].equals("I") && split[i + 1].equals("X"))) {
                i++;
                result += 9;
            } else if (!last && (split[i].equals("X") && split[i + 1].equals("L"))) {
                i++;
                result += 40;
            } else if (!last && (split[i].equals("X") && split[i + 1].equals("C"))) {
                i++;
                result += 90;
            }else if (!last && (split[i].equals("C") && split[i + 1].equals("D"))) {
                i++;
                result += 400;
            } else if (!last && (split[i].equals("C") && split[i + 1].equals("M"))) {
                result += 900;
                i++;
            } else switch (split[i]) {
                case "I":
                    result += 1;
                    break;
                case "V":
                    result += 5;
                    break;
                case "X":
                    result += 10;
                    break;
                case "L":
                    result += 50;
                    break;
                case "C":
                    result += 100;
                    break;
                case "D":
                    result += 500;
                    break;
                case "M":
                    result += 1000;
                    break;
            }
        }
        return result;
    }


}
