package gr.exm.agroxm.util

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

fun LocalDateTime.startOfHour(): LocalDateTime {
    return this.withMinute(0).withSecond(0).withNano(0)
}

fun LocalDateTime.endOfHour(): LocalDateTime {
    return this.plusHours(1).startOfHour()
}

fun LocalDateTime.endOfDay(): LocalDateTime {
    return this.apply {
        plusDays(1)
        withHour(0)
        withMinute(0)
        withSecond(0)
        withNano(0)
    }
}

fun LocalDateTime.millis(): Long {
    return TimeUnit.SECONDS.toMillis(this.atZone(ZoneOffset.systemDefault()).toEpochSecond())
}