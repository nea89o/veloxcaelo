package moe.nea.caelo.util

class Histogram<T>(val maxSize: Int) : Iterable<T> {
    private val dequeue = ArrayDeque<T>()
    fun append(element: T) {
        dequeue.addLast(element)
        if (dequeue.size > maxSize) {
            dequeue.removeFirst()
        }
    }

    val size get() = dequeue.size

    override fun iterator(): Iterator<T> {
        return dequeue.iterator()
    }
}