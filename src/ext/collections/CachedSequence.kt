package ext.collections

class CachingSequence<T>(
        original: Sequence<T>
) : Sequence<T> {
    private val knownElements = ArrayList<T>()
    private val baseIterator = original.iterator()

    override fun iterator() = object : Iterator<T> {
        private var currentIndex = 0

        override fun hasNext() = knownElements.size > currentIndex || baseIterator.hasNext()

        override fun next(): T {
            if (knownElements.size > currentIndex) {
                return knownElements[currentIndex++]
            }
            else {
                val next = baseIterator.next()
                knownElements.add(next)
                currentIndex++
                return next
            }
        }
    }
}

val <T> Sequence<T>.cached: Sequence<T>
    get() = if (this is CachingSequence<*>) this else CachingSequence(this)