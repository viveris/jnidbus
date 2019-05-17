# JNIDBus - A JNI wrapper around libdbus

JNIDbus is a Java JNI wrapper around the native library libdbus allowing an easy and straightforward use of Dbus from Java code in an object oriented way. Keep in mind that this library is a wrapper around the official library and not a protocol implementation like [this one](https://github.com/hypfvieh/dbus-java).

JNIDbus is built around a single-threaded event loop, making it lightweight and easy to use but the developer must keep in mind he must avoid having long running code in its handlers or else the event loop will stall.

This library is distributed under the [AFL licence](https://opensource.org/licenses/AFL-3.0).

## Message serialization

JNIdbus uses annotations and reflection to serialize POJO into DBus messages. The following types are supported:

- Integer (and primitive int)
- String
- Nested serializable objects
- Lists

### The basics

In order to make an object serializable for JNIDBus, it must extends the `Message` class and have the `DBusType` annotation. The library will try to cache the POJO reflection information and check that it respect the signature given in the `signature` property of the signature so the developer can quickly detect errors in the object mappings.

*<u>example for a basic string message:</u>*

```java
@DBusType(
    /* please refer to the DBus documentation for more information
     * about signature format
     */
    signature = "s",
    /* fields mapped to the signature, if your signature contains multiple elements
     * the fields will be mapped from left to right to the first element found
     */
    fields = "string"
)
public class StringMessage extends Message {
    /* fields exposed to JNIDBus must have setters and getters respecting the classic
     * getXXX and setXXX format.
     */
    private String string;

    public String getString() {
        return string;
    }
    public void setString(String string) {
        this.string = string;
    }
}
```

### Nested objects

When dealing with nested objects, the signature of the parent object must include the signature of its child object. This choice was made to avoid implicit signature modification when modifying the child object. If your child object is an inner class, don't forget to make it public and static so JNIDBus can instantiate it.

In the example below if you change the signature of `SubObject` you will have to explicitely update the signature of `NestedObject` too or else JNIDBus will throw en exception.

*<u>example for a nested object message:</u>*

```java
@DBusType(
    /* The following signature means we have a root object containing an integer
     * and another object. The child object contain a single string. Please note that
     * you have to specify the child object signature in the parent object and in the
     * child object
     */
    signature = "i(s)",
    fields = {"integer","object"}
)
public class NestedObject extends Message {
    private int integer;
    private SubObject object;

    public int getInteger() ...
    public void setInteger(int integer) ...

    public SubObject getObject() ...
    public void setObject(SubObject object) ...
}

@DBusType(
    signature = "s",
    fields = {"string"}
)
public class SubObject extends Message{
    private String string;

    public String getString() ...
    public void setString(String string) ...
}

```

### Arrays

DBus array type is mapped to the `List` collection. A List can contain anything serializable, including other lists. As JNIDBus uses reflection to know the type of the List items, the generic type must always be explicitly used in the getters and setters

*<u>example for a nested list message:</u>*

```java
@DBusType(
    /* This signature correspond to an array containing other arrays of integers */
    signature = "aai",
    fields = "array"
)
public class CollectionOfCollectionArray extends Message {
    private List<List<Integer>> array;

    //always explicitly give the precise generic type
    public List<List<Integer>> getArray() ...
        
	//for setters too
    public void setArray(List<List<Integer>> array) ...
}
```

### The EmptyMessage

if your message does not contain any data, the `Message` class contains a static `EMPTY` property which contains an empty Message that can be used. Using this object allows some internal optimizations and it is recommended to use it whenever you can.

## How to use the DBus object

### Handlers

Handlers are classes that will be able to receive signals and expose method calls to DBus. Those classes must extends the `GenericHandler` class and be annotated with the `Handler` annotation which will give JNIDBus informations about the object path and DBus interface you want to match.

Your handler methods must be annotated with the `HandlerMethod` annotation which will define the member (method or signal name) and type of handler (Signal or Call). Please note that non-annotated methods will be ignored and left unprocessed.

Signal handler methods must return void and Call handler methods must return an object extending the  `Message` class. The signatures of the handler method will be inferred from their parameter types and return type. You can have multiple methods bound to the same member as long as they have different input signature.

Handlers will be executed on the same thread as the event loop which means that the kind of code you write in your handler will have a great impact on the overall latency of the event loop. You must avoid blocking code at all cost.

*<u>example of a Signal and Call handler:</u>*

```java
@Handler(
    /* please make sure tor espect the Dbus object path format (which is pretty much
     * the same as Java namespace format)
     */
    path = "/some/object/path",
    interfaceName = "some.dbus.interface"
)
public class SomeHandler extends GenericHandler {
    @HandlerMethod(
        //the member can be different from the java method name
        member = "someSignal",
        type = HandlerType.SIGNAL
    )
    //Here our signal do not have any data so we can use the EmptyMessage
    public void someSignal(Message.EmptyMessage emptyMessage) ...

    @HandlerMethod(
        member = "stringSignal",
        type = HandlerType.METHOD
    )
    public SomeOutput someCall(SomeInput input) ...
}
```

*<u>how to use register the handler to DBus:</u>*

```java
//connect to the DBus deamon
Dbus receiver = new Dbus(BusType.SESSION,"my.bus.name");
//instantiate your handler
SomeHandler handler = new SomeHandler();
//add it to DBus, JNIDBus will automatically check your bindings and throw if something is wrong.
this.receiver.addHandler(handler);
```

### Send a Signal

Signals are classes extending the `Signal` class and annotated with the `DbusSignal` annotation. The `Signal` class is generic and its generic type correspond to the `Message` you want to send.

Please note that sending is asynchronous in JNIDBus and the sending queue have a limited size which means calls to `sendSignal()` might throw an exception if the queue is full.

*<u>example of a string signal and of an empty signal:</u>*

```java
@DbusSignal(
    path = "/object/path/of/the/signal",
    interfaceName = "interface.of.the.signal",
    member = "signalName"
)
public static class EmptySignal extends Signal<Message.EmptyMessage>{
    public EmptySignal() {
        //the super constructor takes as argument an instance of the Message to send
        super(Message.EMPTY);
    }
}

@DbusSignal(
    path = "/object/path/of/the/signal",
    interfaceName = "interface.of.the.signal",
    member = "signalName"
)
public static class StringSignal extends Signal<StringMessage>{
    public StringSignal(StringMessage msg) {
        super(msg);
    }
}
```

*<u>how to send the signals:</u>*

```java
//connect to the DBus deamon
Dbus sender = new Dbus(BusType.SESSION,"my.bus.name");

//create and fill your message
StringMessage msg = new StringMessage();
msg.setString("A string");

//simply send it
sender.sendSignal(new StringSignal(msg));
```

### Send a call

Calls are built the same way as signals. Call classes must extends the `Call` class and have the `DbusMethodCall` annotation. The `Call` class have two generic types, the input and the output of the destination call.

When sending a call, JNIDBus will return you a `PendingCall` object to which you can bind a listener that will be notified when a result or an error is received. A listener will be notified once and further results/errors will be ignored. you can cancel a call by manually calling the `fail()` method on the `PendingCall` which will notify the listener and block any incoming result. The Listener will be executed on same thread as handlers and as the event loop so you must avoid blocking code at all cost.

Result or errors ignored will be stored in the `PendingCall` object for debug purposes.

*<u>example of a call without input that returns a string:</u>*

```java
@DbusMethodCall(
    destination = "destination.bus.name",
    path = "/call/object/path",
    interfaceName = "call.interface.name",
    member = "someCall"

)
public static class StringCall extends Call<Message.EmptyMessage,StringMessage> {
    public StringCall() {
        super(Message.EMPTY,StringMessage.class);
    }
}
```

*<u>example of a listener for the above call:</u>*

```java
public class StringListener implements PendingCall.Listener<StringMessage>{
    @Override
    public void notify(StringMessage value) {
        //do something
    }

    @Override
    public void error(DBusException t) {
        //do something
    }
}

```

*<u>how to use all of the above:</u>*

```java
//connect to the DBus deamon
Dbus sender = new Dbus(BusType.SESSION,"my.bus.name");

//create your message and fill it
StringMessage msg = new StringMessage();
msg.setString("test");

//send the call
PendingCall<StringMessage> pending = sender.call(new StringCall());

//create the listener
StringListener l = new StringListener();

//bind it, now the listener will be executed when a result arrives
pending.setListener(l);
```

### Exception handling

Exception handling is made through the `DBusException` class, which contains two fields: `code` and `message` which correspond to the DBus error fields of the same name. If an error happens in your handler method call, you should catch it and throw a new `DBusException` with tis field correctly set so the caller can process the error.

If an unexpected exception happens in a handler, a `DBusException` will be automatically created with its code being the name of the exception class and its message the message of the exception.

## Planned features

- Asynchronous handler return type in the same fashion as the `PendingCall`
- For now, when binding a Listener to a `PendingCall` that already received a result, the listener will be called on the same thread as the `setListener` call, dispatch it in the event loop instead.
- Provide a way to know when the event loop is ready to send more messages
- Integrate Log4J for a easier debugging
- Extension functions for Kotlin and support for coroutines

## FAQ

##### Which java versions are compatible?

Java 7 and Java 11 where tested. You will also need `libc` and `libdbus-1` to run the native code 

##### How fast is this library

I was able to get around 35k complex signals (nested lists and objects) sent and received on a single event loop on my modest i3-4130 work machine. I was able to get around 58k empty signals with the same setup. This is fast enough for most of the use cases so unless you really need to push DBus to its limit it's enough.

##### I found a bug

Fell free to post an issue describing precisely what behavior you got and whet result you expected. If you can provide a small reproducer we will be able to get rid of this bug even faster.

##### How to contribute

Fork this project and send us a merge request, we will make sure to get back to you as soon as possible. You should also open an issue beforehand so we can give you feedback before you write any code.