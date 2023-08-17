package dev.flydzen.diskspaceprofiler

import java.io.File
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.swing.SwingUtilities
import kotlin.math.min


const val MAX_THREAD_NUM = 4


enum class Status {
    IN_PROGRESS,
    EMPTY,
    FINISHED,
}

class Controller {
    var root: Node? = null
    private lateinit var service: ExecutorService
    private val taskCounter = AtomicInteger(0)

    fun setVisibility(node: Node) {
        node.visible = !node.visible
    }

    fun getStatus(): Status {
        if (root == null) return Status.EMPTY
        return if (taskCounter.get() == 0)
            Status.FINISHED
        else
            Status.IN_PROGRESS
    }

    fun delete(node: Node, onUpdate: () -> Unit) {
        val isDeleted = deleteOnDisk(node.file)
        if (isDeleted) {
            val parent = node.parent
            node.parent = null
            val nodeSize = node.size.get()
            node.size.getAcquire()

            if (parent != null) {
                parent.children.remove(node)
                var upNode: Node = parent
                while (upNode != root!!) {
                    upNode = upNode.parent!!
                    upNode.addSize(-nodeSize) { }
                }
            } else {
                root = null
            }
            onUpdate()
        }
    }

    private fun deleteOnDisk(dirOrFile: File) = dirOrFile.deleteRecursively()

    fun submit(path: Path, onUpdate: () -> Unit) {
        if (getStatus() == Status.IN_PROGRESS) {
            interrupt()
        } else {
            service = Executors.newFixedThreadPool(
                min(Runtime.getRuntime().availableProcessors(), MAX_THREAD_NUM)
            )
            val file = path.toFile()
            if (!file.isDirectory)
                return

            root = Node(file)
            process(root!!, onUpdate)
        }
        onUpdate()
    }

    private fun interrupt() {
        service.shutdown()
        try {
            if (!service.awaitTermination(2, TimeUnit.SECONDS)) {
                service.shutdownNow()
                service.awaitTermination(1, TimeUnit.SECONDS)
            }
            taskCounter.set(0)
        } catch (e: InterruptedException) {
            service.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    private fun process(current: Node, onUpdate: () -> Unit) {
        try {
            val childFiles = current.file.listFiles()
            childFiles?.let {
                val childNodes = childFiles.map {
                    val size = if (it.isFile) it.length() else 0
                    Node(current, it, AtomicLong(size), mutableListOf(), it.isFile)
                }.sortedBy { it.size.get() }

                if (Thread.currentThread().isInterrupted)
                    return

                current.children.addAll(childNodes)
                if (current.visible) {
                    SwingUtilities.invokeLater { onUpdate() }
                }

                childNodes.forEach { node ->
                    taskCounter.incrementAndGet()
                    service.submit {
                        process(node, onUpdate)
                        taskCounter.decrementAndGet()
                    }
                }

            }
            if (current.file.isFile) {
                var upNode: Node? = current
                while (upNode != root && upNode != null && !Thread.currentThread().isInterrupted) {
                    upNode = upNode.parent
                    upNode?.addSize(current.size.get()) { onUpdate() }
                }
            }
        } catch (e: InterruptedException) {
            return
        }
    }
}
