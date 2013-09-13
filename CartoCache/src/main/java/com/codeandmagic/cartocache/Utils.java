package com.codeandmagic.cartocache;

import java.util.ArrayList;

/**
 * Created by evelyne24.
 */
public class Utils {

    /**
     * Find all elements in first array that are not found in the second one.
     *
     * @param firstArray
     * @param secondArray
     * @param <T>
     * @return
     */
    public static <T> T[] diff(T[] firstArray, T[] secondArray, T[] empty) {
        if (firstArray == null) {
            return empty;
        }
        if (secondArray == null) {
            return firstArray;
        }
        ArrayList<T> diff = new ArrayList<T>();
        for (T e1 : firstArray) {
            boolean found = false;
            for (int i = 0; i < secondArray.length; ++i) {
                if (e1.equals(secondArray[i])) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                diff.add(e1);
            }
        }
        return diff.toArray(empty);
    }
}
