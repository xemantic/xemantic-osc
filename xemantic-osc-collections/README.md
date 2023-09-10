# xemantic-osc-collections

Thread-safer Kotlin `Map` and `Iterable` implementations,
which will not throw `ConcurrentModificationException`, offering lack
of synchronization in favor of eventual consistency, which is good
enough for OSC use cases.
