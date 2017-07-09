package com.zhou

/**
 *
 * Created by ZhOu on 2017/5/22.
 */
fun main(args: Array<String>) {
    println(sum_1(4, 6))
    var name = "zhou"
    /**
     * 等价与
     * String a;
     * int length = a!=null?a.length():-1
     * -->
     * var length = if(a!=null) a.length else -1
     * 使用elvis操作符:
     *      问号（?）判断表达式，如果a为null，表达式就会返回null
     *      如果elvis操作符左侧为空，那么返回右侧，否则返回左侧的值
     * var length = a?.length?:-1
     */
    var a: String? = null
    var length = a?.length ?:-1
    println(a?.length)
    println("$length")
}

/**
 * sum
 */
fun sum_1(a: Int, b: Int): Int {
    return a + b
}

fun sum_2(a: Int, b: Int): Int = a + b

fun sum_3(a: Int, b: Int) = a + b