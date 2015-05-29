package com.igumnov.common;

import java.util.stream.Stream;

public class Strings {

    public static Stream<Character> stream(String s) {
        Stream.Builder<Character> builder = Stream.builder();
        for(int i = 0, length = s.length(); i < length; i++){
            builder.add(s.charAt(i));
        }
        return builder.build();
    }
}
