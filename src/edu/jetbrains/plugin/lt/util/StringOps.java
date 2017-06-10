package edu.jetbrains.plugin.lt.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringOps {
    private StringOps() {
    }

    public static String removeInsipidSequences(String s) {
        s = s.replaceAll("_(\\s|_)*_", "_");
        boolean changedOut = true;
        while (changedOut) {
            changedOut = false;
            boolean changed = true;
            while (changed) {
                changed = false;
                Pattern pattern = Pattern.compile("_((\\((\\s|_)*\\))|(\\[(\\s|_)*\\])|(<(\\s|_)*>)|(\\{(\\s|_)*\\}))");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    s = s.substring(0, matcher.start()) + "_" + s.substring(matcher.end());
                    changedOut = changed = true;
                }
            }
            changed = true;
            while (changed) {
                changed = false;
                Pattern pattern = Pattern.compile("_[^()\\[\\]\\{\\}<>]_");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    s = s.substring(0, matcher.start()) + "_" + s.substring(matcher.end());
                    changedOut = changed = true;
                }
            }
        }
        return s;
    }

    public static Set<String> product(List<Set<String>> parts) {
        if (parts.size() == 1) return parts.get(0);
        Set<String> firstPart = parts.get(0);
        Set<String> secondPart = parts.get(1);
        Set<String> newPart = new HashSet<>();
        for (String s1 :
                firstPart) {
            for (String s2 :
                    secondPart) {
                newPart.add((s1 + s2).replaceAll("_(_|\\s)*_", "_"));
            }
        }
        parts.remove(0);
        parts.remove(0);
        parts.add(0, newPart);
        return product(parts);
    }

    public static Predicate<String> containsAnyPredicate(Collection<String> strings) {
        Predicate<String> predicate = s -> false;
        for (String ss : strings) predicate = predicate.or(s -> s.contains(ss));
        return predicate;
    }

    public static boolean isRightCode(String s) {
        String s1 = s.replaceAll("[^()]+", "");
        String s2 = s.replaceAll("[^\\[\\]]+", "").replace('[', '(').replace(']', ')');
        String s3 = s.replaceAll("[^\\{}]+", "").replace('{', '(').replace('}', ')');
        String s4 = s.replaceAll("[^<>]+", "").replace('<', '(').replace('>', ')');
        return isRightSequence(s1) && isRightSequence(s2) && isRightSequence(s3) && isRightSequence(s4);
    }

    private static boolean isRightSequence(String s) {
        int depth = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == '(') {
                ++depth;
            } else {
                --depth;
            }
            if (depth < 0) return false;
        }
        return depth == 0;
    }

}
