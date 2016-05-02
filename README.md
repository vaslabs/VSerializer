# VSerializer
A library to serialize and deserialize objects with minimum memory usage.

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

# Disadvantages
- Less forgiving for changed classes. A mechanism to manage changes will be in place but since the meta data for the classes won't be carried over it will never be the same as the defaults.

# Use case
- Any data structure that matches a timestamp with other primitive values would be highly optimised in terms of space when saving the data using this approach. You can save millions of key/value pairs for data like timestamp/location history graph.
- Short lived cache data are in less danger to cause problems when you do class changes. You can benefit by reducing the memory usage in your caching mechanism and not worry much about versioning problems.

# Try it out

Follow instructions here https://jitpack.io/#vaslabs/VSerializer
