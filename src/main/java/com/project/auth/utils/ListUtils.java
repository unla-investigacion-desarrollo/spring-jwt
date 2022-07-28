package com.project.auth.utils;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {

    private ListUtils() {
    }

    public static <T> List<List<T>> splitLists(List<T> list, int length) {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += length) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(N, i + length))));
        }
        return parts;
    }

    public static List<List<Long>> listOfIdsLists(List<List<Long>> splitList, int sizeList) {
        List<List<Long>> regroupedList = new ArrayList<>();

        if (!splitList.isEmpty() && splitList.size() <= sizeList) {
            for (int i = 0; i < sizeList; i++) {
                if (splitList.size() >= i + 1) {
                    regroupedList.add(splitList.get(i));
                } else {
                    regroupedList.add(new ArrayList<>());
                }
            }
        } else {
            for (int i = 0; i < sizeList; i++) {
                regroupedList.add(null);
            }
        }
        return regroupedList;
    }
}
