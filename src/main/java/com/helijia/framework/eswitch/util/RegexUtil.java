package com.helijia.framework.eswitch.util;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    public static Pattern[] compilePatterns(String[] inputs) {
        if (inputs == null)
            return null;
        Pattern[] patterns = new Pattern[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = escapeStringLiteralForWildCard(inputs[i].trim());
            inputs[i] = inputs[i].replace("*", "(.*)").replace("?", "(.{1})");
            Pattern p = Pattern.compile(inputs[i], Pattern.CASE_INSENSITIVE);
            patterns[i] = p;
        }
        return patterns;
    }

    /**
     * 判断传入的uri是否满足pattern
     * 
     * @param patterns
     * @param uri
     * @return
     */
    public static boolean isMatched(Pattern[] patterns, String str) {
        if (patterns != null) {
            str = str.trim();
            for (Pattern pattern : patterns) {
                if (isWildCardMatched(str, pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断传入的uri是否满足pattern
     * 
     * @param patterns
     * @param uri
     * @return
     */
    public static boolean isMatched(Collection<Pattern> patterns, String str) {
        if (patterns != null) {
            str = str.trim();
            for (Pattern pattern : patterns) {
                if (isWildCardMatched(str, pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 对指定的文本进行模糊匹配，支持* 和?，不区分大小写
     * 
     * @param text 要进行模糊匹配的文本
     * @param pattern 模糊匹配表达式
     * @return
     */
    public static boolean isWildCardMatched(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        return m.matches();
    }

    public static String escapeStringLiteralForWildCard(String original) {
        if (original == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < original.length(); i++) {
            char curChar = original.charAt(i);
            boolean stillAppend = true;
            switch (curChar) {
                case '$':
                case '(':
                case ')':
                case '+':
                case '.':
                case '[':
                case '\\':
                case '^':
                case '{':
                case '|':
                    result.append('\\');
                    break;
                case '\r':
                    result.append('\\').append('r');
                    stillAppend = false;
                    break;
                case '\n':
                    result.append('\\').append('n');
                    stillAppend = false;
                    break;
                case '\t':
                    result.append('\\').append('t');
                    stillAppend = false;
                    break;
                case '\f':
                    result.append('\\').append('f');
                    stillAppend = false;
                    break;
                case '\000':
                    result.append('\\').append('0');
                    stillAppend = false;
            }

            if (stillAppend) {
                result.append(curChar);
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String str = "127.0.0.1".replace(".", "\\.").replace("*", "(.*)").replace("?", "(.{1})");
        Pattern pattern = Pattern.compile(str);
        System.out.println(pattern.matcher("127.0.0.1").matches());
        System.out.println(pattern.matcher("127-0-0-1").matches());
    }
}
