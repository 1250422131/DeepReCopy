package com.imcys.deeprecopy.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * 序列化类进行深拷贝：这样的深拷贝内部对象可能还是不会得到深拷贝
 */
object SerializableUtils {

    fun <T> deepCopy(kClass: KClass<*>): T {
        val serObject = kClass.createInstance() as Serializable

        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.javaClass.kotlin
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(serObject)
        objectOutputStream.flush()

        val byteArrayInputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        return objectInputStream.readObject() as T
    }
}
