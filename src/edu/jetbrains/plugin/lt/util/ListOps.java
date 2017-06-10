package edu.jetbrains.plugin.lt.util;

import java.util.List;
import java.util.stream.Collectors;

public final class ListOps {
    private ListOps() {
    }

    public static <T> boolean hasSubSequence(List<T> sequence, List<T> subSequence) {
        int iSeq = 0;
        int iSubSeq = 0;
        while (true) {
            if (sequence.size() - iSeq < subSequence.size() - iSubSeq) return false;
            if (iSubSeq == subSequence.size()) return true;
            if (sequence.get(iSeq).equals(subSequence.get(iSubSeq))) ++iSubSeq;
            ++iSeq;
        }
    }

    public static List<String> removeInsipidSequences(List<String> lst) {
        lst = lst.stream().filter(s -> !"".equals(s)).collect(Collectors.toList());

        int i = 0;
        while (i < lst.size()) {
            if ("_".equals(lst.get(i))) {
                int j = i + 1;
                int lastP = i;
                while (j < lst.size() && lst.get(j).matches("_|\\s+")) {
                    if ("_".equals(lst.get(j))) lastP = j;
                    ++j;
                }
                if (i != lastP) {
                    for (int k = lastP; k > i; --k) lst.remove(k);
                }
            }
            ++i;
        }

        boolean changedOut = true;
        while (changedOut) {
            changedOut = false;
            boolean changed = true;
            while (changed) {
                changed = false;
                i = 0;
                while (i + 2 < lst.size()) {
                    if ("_".equals(lst.get(i))) {
                        if (lst.get(i + 1).matches("[(<\\{\\[]")) {
                            String cl = "";
                            switch (lst.get(i + 1).charAt(0)) {
                                case '(':
                                    cl = ")";
                                    break;
                                case '<':
                                    cl = ">";
                                    break;
                                case '[':
                                    cl = "]";
                                    break;
                                case '{':
                                    cl = "}";
                                    break;
                            }
                            String regex = "\\" + cl + "|_|\\s+";
                            int j = i + 2;
                            int lastB = i;
                            while (j < lst.size() && lst.get(j).matches(regex)) {
                                if (cl.equals(lst.get(j))) {
                                    lastB = j;
                                    break;
                                }
                                ++j;
                            }
                            if (i != lastB) {
                                for (int k = lastB; k > i; --k) {
                                    lst.remove(k);
                                    changedOut = changed = true;
                                }
                            }
                        }
                    }
                    ++i;
                }
            }
            changed = true;
            while (changed) {
                changed = false;
                i = 0;
                while (i + 2 < lst.size()) {
                    if ("_".equals(lst.get(i))
                            && "_".equals(lst.get(i + 2))
                            && lst.get(i + 1).length() == 1
                            && !lst.get(i + 1).matches("[()\\[\\]\\{\\}<>]")) {
                        lst.remove(i + 2);
                        lst.remove(i + 1);
                        changedOut = changed = true;
                    }
                    ++i;
                }
            }
        }
        return lst;
    }
}
