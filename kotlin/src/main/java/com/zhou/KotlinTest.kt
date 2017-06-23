package com.zhou

/**
 *
 * Created by ZhOu on 2017/5/22.
 */
fun main(args: Array<String>) {
    println(sum_1(4, 6))
}

/**
 * sum
 */
fun sum_1(a: Int, b: Int): Int {
    return a + b
}

fun sum_2(a: Int, b: Int): Int = a + b

fun sum_3(a: Int, b: Int) = a + b