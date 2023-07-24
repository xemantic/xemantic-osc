# xemantic-osc

_Kotlin idiomatic and multiplatform OSC protocol support._

> Open Sound Control (OSC) is a protocol for networking sound synthesizers,
> computers, and other multimedia devices for purposes such as musical
> performance or show control.
> https://en.wikipedia.org/wiki/Open_Sound_Control

Official OSC protocol website: https://opensoundcontrol.stanford.edu/


## Usage

For a Kotlin JVM project add the following to `build.gradle.kts`:

```kotlin
dependencies {
  implementation("com.xemantic.osc:xemantic-osc-udp:1.0-SNAPSHOT")
}
```

Listen to _OSC Messages_ on the UDP port `12345`:

```kotlin
UdpOscTransport(
  port = 12345,
  input = OscInput {
    route<String>("/ping") { message ->
      println("OSC Message received from: ${message.peer}")
      println("The ping message is: ${message.value}")
    }
  }
)
```

In order to send a single message to such a port:

```kotlin
UdpOscTransport().use { transport ->
  transport.output(
    hostname = "localhost",
    port = 12345
  ) {
    route<String>("/ping")
  }
  output.send("/ping", "pong")
}
```

Routes defined for an `OscInput` can have associated action, like the
one defined for the `/ping` address above. It is also possible to listen
to generic messages.

```kotlin
UdpOscTransport(
  port = 12345,
  input = OscInput {
    route<Int>("/volume")
    route<Float>("/temperature")
  }
).use { transport ->
  runBlocking {
    transport.input.messages.collect { message ->
      println("OSC Message received from: ${message}")
    }
  }
}
```

See more examples:

* [demo module](xemantic-osc-demo)
* [xemantic-osc-demo-jvm](https://github.com/xemantic/xemantic-osc-demo-jvm) project
* [xemantic-osc-demo-native](https://github.com/xemantic/xemantic-osc-demo-native) project


## API Design

The main entry points of the API are:

 * `OscInput`
 * `OscOutput`
 * `OscTransport`

The `OscInput` is receiving an input stream from the network packets,
regardless of the transport protocol being used (`udp`, `tcp`, etc.).
Several instances of the `OscTransport` can feed the same `OscInput`
instance with _OSC Messages_. The logic of processing the OSC protocol
is common and implemented in multiplatform manner in the
[api](xemantic-osc-api) module, while other transports can be implemented
separately per protocol and per platform.

This library doesn't make any assumptions regarding _OSC types_ being
transmitted, which would be possible with mechanism like
Java reflection. Instead, it requires precise specification of expected
type for each _OSC address_ route. These types are associated with
precise `OscEncoder`s  and `OscDecoder`s for maximal performance.

Another design principle is to allow full configuration of `OscInput`
and `OscOutput` on the fly, possibly through GUI, without any need to
re-instantiate the component. To support this functionality the
[collections](xemantic-osc-collections) module was provided as a
base for holding the route information with associated type
decoders and encoders.

:warning: At the moment the API defines support for _OSC Bundle_, however
this part is still not implemented in version `1.0`.


## Modules

* [collections](xemantic-osc-collections): thread-safer `Map` and `Iterable`
* [api](xemantic-osc-api): the API of this library.
* [test](xemantic-osc-test): the test suite to be used across
  transport implementations.
* [udp](xemantic-osc-udp): the UDP protocol support.
* [ableton](xemantic-osc-ableton): support for Ableton Live / Max for life
  conventions of sending MIDI notes through OSC.

Modules planned in the future:

* `tcp`: the TCP protocol support.
* `query`: an implementation of OSC query protocol extensions.
* `websockets`: OSC over websockets.


## Use Cases

### Guarantee sequential order of OSC Messages

This library is using `Dispatchers.IO` by default to process incoming and
outgoing _OSC Packets_ concurrently for maximal performance and minimal
latency. It might result in packet reordering though. There are use cases
where the ordering is important, for example when there is implicit connection
between _OSC Messages_ being sent. For example [ableton](xemantic-osc-ableton)
module depends on sequential processing of `note` and `velocity` messages.
It is possible to declare single threaded dispatcher as well.

```kotlin
UdpOscTransport(
  port = 12345,
  dispatcher = newSingleThreadContext("notes"),
  input = OscInput {
    route<Int>(
      addressMatcher = { it.startsWith("/Note") }
    )
    route<Int>(
      addressMatcher = { it.startsWith("/Velocity") }
    )    
  }
).use { transport ->
  // ...
  val output = transport.output(
    12345,
    dispatcher = newSingleThreadContext("foo")
  )
  output.route<Int>("/foo")
  runBlocking {
    (1..100000).forEach {
      output.suspendedSend("/foo", it)
    }
  }
}
```

### Using generic OSC Message types

### Changing from UTF-8 to another character encoding

The default encoder/decoder works with UTF-8, it is possible to
replace it.

For particular input route:

```kotlin
input.route<String>(
  address = "/foo",
  decoder = { assertTypeTag("s"); string(Charset.ISO8859_1) }
)
```

For the whole `OscInput`:

```kotlin
input.decode<String> { assertTypeTag("s"); string(Charset.ISO8859_1) }
```

For the whole `OscOutput`:

```kotlin
output.encode<String> { typeTag("s"); string(Charset.ISO8859_1) }
```


## Implementation notes

The `xemantic-osc` library is built on top of the [Ktor](https://ktor.io/)
library.


## Development

### Updating dependencies

run:

```shell
./gradlew dependencyUpdates
```

,and apply changes in [gradle/libs.versions.toml](gradle/libs.versions.toml).
