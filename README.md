
<div align="center">
    
# DeepReCopy

[![](https://jitpack.io/v/1250422131/DeepReCopy.svg)](https://jitpack.io/#1250422131/DeepReCopy)
![GitHub](https://img.shields.io/github/license/1250422131/bilibilias)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.10-blue.svg?logo=kotlin)](http://kotlinlang.org)

</div>

## 它可以做什么

DeepReCopy是针对Kotlin的Data类所开发的深度拷贝功能库，利用KSP可以生成Data类的深度拷贝扩展方法，支持DSL写法。

## 如何使用

### 库的引入
由于项目使用了KSP，需要在脚本顶部添加KSP插件，在每个使用KSP的模块都需要哦。
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

## 为什么要深拷贝

如果你已经了解为什么要深拷贝那就跳过下面

设想一下，假如你在Compose中用数据类定义了一个界面的`State`，无论是我们用`Flow`
还是`MutableState`来监听这个状态的变化，都会有一个问题。

那就是更新数据，在`SateFlow`中，我们可以用类似下面这种方式更新数据，其中uiState是一个Kotlin的Data类。

```kotlin
update {
    uiState.copy(property = "newContent")
}
```

而用MutableState时要这样做

```kotlin
uiState = uiState.copy(property = "newContent")
```

首先从写法上是不太优雅的，前者是dsl不贴切，后者是本质上写着就恶心，于是我们对后者迭代，就像是下面这样。

```kotlin
fun S.update(content: S.() -> S) {
    uiState = content.invoke(this)
}
```

但这样都不够，无论那种情况，假设你只是对Data类进行了拷贝，尽管Data产生了新对象，但Data里的对象却没有重新new，而是直接把引用拿过来了，这会导致旧的Data内对象更新，但是你新的Data内对象也会更新，一旦有对比就发现他们是同一个东西，数据没有任何改变，这就要出问题了。

除此以外，你可能还会在`Recyclerview`用`ListAdapter`,它就要求Data类必须是新对象，才会继续监听内部变化，这就很有可能会出现上面的情况。

## 快速开始

### 认识DeepReCopy注解

DeepReCopy现阶段提供了一个注解，`EnhancedData`和未实现的计划注解`DeepCopy`
，分别用来注解Data类和需要被深拷贝的Class类，后者通常是需要深拷贝的非Data类，导入你现在可以直接给非Data类注解@EnhancedData，但是不确定是否有意料之外的问题。

让我们试试看？

EnhancedData是用来增强Data类的，现阶段它只有对Data类进行扩展的功能，可以让Data深拷贝的同时支持DSL写法。

我们看一则例子：

    ```kotlin
    @EnhancedData
    data class AData(val name: String, val title: String, val bData: BData)
    
    @EnhancedData
    data class BData(val doc: String, val content: String)
    ```

当对AData和BData顶上注解后我们点击Android Studio的Build。

```kotlin
data class _ADataCopyFun(
    var name : kotlin.String,
    var title : kotlin.String,
    var bData : com.imcys.deeprecopy.demo.BData,
)


fun AData.deepCopy(
    name : kotlin.String = this.name,
    title : kotlin.String = this.title,
    bData : com.imcys.deeprecopy.demo.BData = this.bData,
): AData {
    return AData(name, title, bData.deepCopy())
}


fun AData.deepCopy(
    copyFunction:_ADataCopyFun.()->Unit): AData{
    val copyData = _ADataCopyFun(name, title, bData)
    copyData.copyFunction()
    return this.deepCopy(copyData.name, copyData.title, copyData.bData)
}


```
其中@EnhancedData是注解需要扩展深拷贝函数的Data类，其中因为BData也在AData中，所以也需要给BData注解EnhancedData。
这是生成后的数据类，事实上，我们发现deepCopy重新返回了一个新的AData，同时，bData也被重新new了一个出来，这就确保了内部对象确实发生了改变。

```kotlin
var aData = AData("name", "title", BData("doc", "content"))
aData = aData.deepCopy {
    name = "newName"
    bData = BData("newDoc", "newContent")
}
```

接下来我们只需要这样写就可以完成对aData的深拷贝了，当然，这样的写法对`MutableState`不太友好，这个会等后面进行更近。

以上就是它目前支持的完整用法

## 特别注意
DeepCopy还在测试阶段，可能会遇到一些意料之外的问题，如果你要使用，请确保符合上面的使用规则，有任何问题可以提issue ❤。

## 源代码相关
DeepCopy源代码仍然在整理，它现在会比较乱。
