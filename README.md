<div align="center">

# DeepReCopy

![Maven Central](https://img.shields.io/maven-central/v/com.imcys.deeprecopy/core)
![GitHub](https://img.shields.io/github/license/1250422131/bilibilias)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.10-blue.svg?logo=kotlin)](http://kotlinlang.org)

</div>

## Language:

[English](README.md) | [中文](README-zh.md)

## What can it do

DeepReCopy is a deep copy utility library developed for Kotlin's data classes. It utilizes KSP to
generate deep copy extension methods for data classes, supporting DSL syntax.

## How to use

### Library Integration

Since the project uses KSP, you need to add the KSP plugin at the top of your script, and it is
required for each module that uses KSP.
DeepReCopy has been submitted to the Maven Central repository, so there is no need to import
additional repositories. You can directly configure it as shown below. The latest version should
match the version obtained from the Maven Central badge at the top of this file.
groovy

```groovy
plugins {
    id 'com.google.devtools.ksp' version '1.9.0-1.0.11'
}

implementation 'com.imcys.deeprecopy:core:<version>'
ksp 'com.imcys.deeprecopy:compiler:<version>'
```

kts

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.11"
}

implementation("com.imcys.deeprecopy:core:<version>")
ksp("com.imcys.deeprecopy:compiler:<version>")
```

## Why deep copy?

If you already understand why deep copy is needed, you can skip the following section.

Imagine that you define the `State` of a user interface in Compose using a data class. Whether we
use `Flow` or `MutableState` to listen for changes in this state, we encounter a problem.

The problem is updating the data. In `StateFlow`, we can update the data using a syntax similar to
the following, where `uiState` is a Kotlin data class:

```kotlin
update {
    uiState.copy(property = "newContent")
}
```

When using `MutableState`, we need to do it like this:

```kotlin
uiState = uiState.copy(property = "newContent")
```

First of all, the syntax is not elegant. The former is not appropriate for DSL, and the latter is
simply unpleasant to write. So we iterate on the latter, like this:

```kotlin
fun S.update(content: S.() -> S) {
    uiState = content.invoke(this)
}
```

But this is not enough. In either case, if you only perform a copy operation on the data class,
although a new object is created for the data class, the objects inside the data class are not newly
created. Instead, their references are directly copied. This will cause the objects inside the old
data class to be updated, but the objects inside the new data class will also be updated. Once
compared, it is found that they are the same, and no data changes have occurred. This can lead to
problems.

In addition, you may also use `ListAdapter` in `RecyclerView`, which requires the data class to be a
new object in order to continue listening for internal changes. This can often lead to the situation
described above.

## Getting Started

### Understanding the DeepReCopy Annotations

At this stage, DeepReCopy provides an annotation called `EnhancedData` and a planned annotation
called `DeepCopy`, which are used to annotate data classes and non-data classes that need to be deep
copied, respectively. Currently, you can directly annotate non-data classes with `@EnhancedData`,
but there may be unexpected issues.

Let's give it a try, shall we?

`EnhancedData` is used to enhance data classes. Currently, it only provides the ability to extend
data classes, allowing them to be deep copied while supporting DSL syntax.

Let's take an example:

```kotlin
    @EnhancedData
data class AData(val name: String, val title: String, val bData: BData)

@EnhancedData
data class BData(val doc: String, val content: String)
```

After annotating `AData` and `BData` with `EnhancedData`, we click Build in Android Studio.

```kotlin
data class _ADataCopyFun(
    var name: kotlin.String,
    var title: kotlin.String,
    var bData: com.imcys.deeprecopy.demo.BData,
)


fun AData.deepCopy(
    name: kotlin.String = this.name,
    title: kotlin.String = this.title,
    bData: com.imcys.deeprecopy.demo.BData = this.bData,
): AData {
    return AData(name, title, bData.deepCopy())
}


fun AData.deepCopy(
    copyFunction: _ADataCopyFun.() -> Unit
): AData {
    val copyData = _ADataCopyFun(name, title, bData)
    copyData.copyFunction()
    return this.deepCopy(copyData.name, copyData.title, copyData.bData)
}


```

The `@EnhancedData` annotation is used to indicate the data class that needs to be extended with a
deep copy function. Since `BData` is also used in `AData`, it also needs to be annotated
with `EnhancedData`. This is the generated data class. In fact, we can see that `deepCopy` now
returns a new `AData`, and `bData` is also newly created, ensuring that the internal objects have
indeed changed.

```kotlin
var aData = AData("name", "title", BData("doc", "content"))
aData = aData.deepCopy {
    name = "newName"
    bData = BData("newDoc", "newContent")
}
```

Now we can perform a deep copy of `aData` by simply writing it like this. Of course, this syntax is
not very friendly to `MutableState`, which will be addressed later.

This is the complete usage of DeepReCopy at the moment.

## Special Note

DeepCopy is still in the testing phase and may encounter some unexpected issues. If you want to use
it, please make sure to follow the usage rules mentioned above. If you have any problems, please
feel free to open an issue ❤.

## Source Code Related

The source code for DeepCopy is still being organized, so it may be messy at the moment.