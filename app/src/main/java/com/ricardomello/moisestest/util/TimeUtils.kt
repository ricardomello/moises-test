package com.ricardomello.moisestest.util

fun Int.toTimeString(): String = "%d:%02d".format(this / 60, this % 60)

fun remainingTimeString(progress: Int, duration: Int): String {
    val remaining = duration - progress
    return "-%d:%02d".format(remaining / 60, remaining % 60)
}
