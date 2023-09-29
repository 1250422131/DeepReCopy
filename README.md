<div align="center">

# DeepReCopy

![Maven Central](https://img.shields.io/maven-central/v/com.imcys.deeprecopy/core)
![GitHub](https://img.shields.io/github/license/1250422131/bilibilias)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.10-blue.svg?logo=kotlin)](http://kotlinlang.org)

</div>

## Language:

[English](README.md) | [中文](README-zh.md)

## What It Does

DeepReCopy is a deep copy utility library for Kotlin's Data classes. It leverages KSP to generate
deep copy extension methods for Data classes, supporting DSL syntax.

## How to Use

### Library Integration

Since the project uses KSP, you need to add the KSP plugin at the top of your script, in each module
that uses KSP.
DeepReCopy is already available in the Maven Central repository, so you don't need to import any
additional repositories. You can directly use the following configurations, with the latest version
obtained from the Maven Central version mentioned at the top of this file.
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

## Why Deep Copy?

If you already understand why deep copy is needed, you can skip this section.

Imagine you define a `State` for a UI using a data class in Compose. Whether we use `Flow`
or `MutableState` to observe changes in this state, there is a problem.

The problem is updating the data. In `StateFlow`, we can update the data using a syntax similar to
the following, where `uiState` is a Kotlin data class:

```kotlin
update {
    uiState.copy(property = "newContent")
}
```

But when using `MutableState`, we need to do it like this:

```kotlin
uiState = uiState.copy(property = "newContent")
```

First of all, the syntax is not elegant. The former is not DSL-friendly, and the latter is
cumbersome to write. So, we iterate on the latter and make it look like this:

```kotlin
fun S.update(content: S.() -> S) {
    uiState = content.invoke(this)
}
```

But even this is not enough. In both cases, if you only perform a copy of the data class, although a