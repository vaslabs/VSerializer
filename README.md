[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-VSerializer-green.svg?style=true)](https://android-arsenal.com/details/1/3563)
# VSerializer
A library to serialize and deserialize objects with minimum memory usage.

# Gradle dependencies
```ruby
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
}
```

```ruby
dependencies {
    compile 'com.github.vaslabs:VSerializer:2.2'
}
```

# Example - Simple object
```java
VSerializer vSerializer = new AlphabeticalSerializer();
TestUtils.AllEncapsulatedData allEncapsulatedData = new TestUtils.AllEncapsulatedData();
allEncapsulatedData.a = -1L;
allEncapsulatedData.b = 1;
allEncapsulatedData.c = 127;
allEncapsulatedData.d = -32768;
allEncapsulatedData.e = true;
allEncapsulatedData.f = 'h';

byte[] data = vSerializer.serialize(allEncapsulatedData);

TestUtils.AllEncapsulatedData recoveredData = 
	vSerializer.deserialise(data, TestUtils.AllEncapsulatedData.class);
```

#Example - List
```java
List<TestUtils.EncapsulatedData> encapsulatedDataList = initList(...);
byte[] data = vSerializer.serialize(encapsulatedDataList);
List<TestUtils.EncapsulatedData> recoveredList = 
	vSerializer.deserialise(data, List.class, TestUtils.EncapsulatedData.class);
```
# Motivation

Memory on Android is precious. Every application should be using the minimum available memory both volatile and persistent.
However, the complexity of doing such a thing is too much for the average developer that wants to ship the application as 
fast as possible. The aim of this library is to automate the whole process and replace ideally the default serialization mechanism.

That can achieve:
- Lazy compression and decompression on the fly to keep volatile memory usage low for objects that are not used frequently (e.g. cached objects with low hit/miss ratio).
- Occupying less persistent memory when saving objects on disk.


# How does it work?

This project is under development and very young. However, you can use it if you are curious or you want to be a step ahead by 
following the examples in the unit test classes.

# Advantages
- A lot less memory usage when serializing objects compared to JVM or json.
- Faster processing for serialization/deserialization
- Extensible: will be able to easily encrypt and decrypt your serialized objects
- Out of the box deep cloning.

# Disadvantages
- Less forgiving for changed classes. A mechanism to manage changes will be in place but since the meta data for the classes won't be carried over it will never be the same as the defaults.
- ~~Does not maintain the object graph meaning that a cyclic data structure will not be possible to be serialized~~ (The new version provides an experimental serializer that can serialize circular data structures. Use with care as it's still in beta.)

## Example 
```java
circularDS = new CircularDS();
circularDS.pointsTo = circularDS;
circularDS.justANumber = 5;
VSerializer vSerializer = new ReferenceSensitiveAlphabeticalSerializer();
byte[] data = vSerializer.serialize(circularDS);
CircularDS recoveredCircularDS = vSerializer.deserialise(data, CircularDS.class);
```

# Use case
- Any data structure that matches a timestamp with other primitive values would be highly optimised in terms of space when saving the data using this approach. You can save millions of key/value pairs for data like timestamp/location history graph.
- Short lived cache data are in less danger to cause problems when you do class changes. You can benefit by reducing the memory usage in your caching mechanism and not worry much about versioning problems.

