package com.lag.lanlord.util;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class ArrayUtil {

    /**
     * 获取某个元素在数组中的位置
     *
     * @param _arr int类型的数组
     * @param _e   int类型的元素
     * @return index
     */
    public static int getIndex(int[] _arr, int _e) {
        return Arrays.binarySearch(_arr, _e);
    }

    /**
     * 验证数组中是否包含某个元素
     *
     * @param _arr int类型的数组
     * @param _e   int类型的元素
     * @return 包含true;不包含false
     */
    public static boolean contains(int[] _arr, int _e) {
        int i = getIndex(_arr, _e);
        if (i >= 0)
            return true;
        return false;
    }

    /**
     * 验证list集合中是否有重复的元素
     *
     * @param list List<? extends Object>
     * @return 有重复元素，true,没有重复元素false，集合为空false
     */
    public static boolean hasSame(List<? extends Object> list) {
        if (null == list || list.isEmpty())
            return false;
        return list.size() != new TreeSet<>(list).size();
    }

    /**
     * 没用重复元素的整形数组，是不是连续的（等差数列，差为1）
     *
     * @param array 整数类型数组 没重复元素的，并且是按照从小到大排序的
     * @return 是连续的true，不是false,数组为空false
     */
    public static boolean isSerial(Integer[] array) {
        if (null == array)
            return false;
        int length = array.length;
        if (length < 1)
            return false;
        return array[length - 1] - array[0] == length - 1;
    }

}
