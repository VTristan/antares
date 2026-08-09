package vt.project.antares;

import java.util.Arrays;
import java.util.List;

public class Program {

    static void main() {

        List<String> list = Arrays.asList("1", "2", "trois");
        List<String> result = list.stream()
                .map(String::toLowerCase)
                .toList();

        System.out.println(result);

        list.add("x");

        System.out.println(result);

    }

}
