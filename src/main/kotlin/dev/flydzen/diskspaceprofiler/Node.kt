package dev.flydzen.diskspaceprofiler

import java.io.File
import java.util.Collections
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

data class Node(
    var parent: Node?,
    val file: File,
    var size: AtomicLong,
    val children: MutableList<Node>,
    val isFile: Boolean,
    var visible: Boolean = false) {
    constructor(file: File): this(null, file, AtomicLong(0), mutableListOf(), false)

    fun addSize(value: Long, onUpdate: () -> Unit) {
        val newSize = size.addAndGet(value)
        if (parent != null && parent?.visible != false) {
            val neighbors = parent!!.children
            val pos = neighbors.indexOf(this)
            if (pos != 0 && newSize > neighbors[pos - 1].size.get()) {
                neighbors.sortByDescending { it.size.get() }
                onUpdate()
            }
        }
    }

    fun getChildrenNodes() = children.toList()

    operator fun compareTo(o: Node) = size.get().compareTo(o.size.get())

    override fun hashCode(): Int {
        return file.path.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (file != other.file) return false
        return isFile == other.isFile
    }

    override fun toString(): String {
        return "$file | ${size.get()}"
    }

    companion object {
        val r = Random(12)
    }
}