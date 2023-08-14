package dev.flydzen.diskspaceprofiler

import java.io.File
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import javax.swing.SwingUtilities
import kotlin.math.min

class Controller {
    var root: Node? = null
    private val service = Executors.newFixedThreadPool(
        min(Runtime.getRuntime().availableProcessors(), 4)
    )

    fun setVisibility(node: Node) {
        node.visible = !node.visible
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
        val file = path.toFile()
        if (!file.isDirectory)
            return

        root = Node(file)
        process(root!!, onUpdate)
        onUpdate()
    }

    private fun process(current: Node, onUpdate: () -> Unit) {
        val childFiles = current.file.listFiles()
        childFiles?.let {
            val childNodes = childFiles.map {
                val size = if (it.isFile) it.length() else 0
                Node(current, it, AtomicLong(size), mutableListOf(), it.isFile)
            }.sortedBy { it.size.get() }
            current.children.addAll(childNodes)
            if (current.visible) {
                SwingUtilities.invokeLater { onUpdate() }
            }
            childNodes.forEach { node ->
                service.submit { process(node, onUpdate) }
            }

        }
        if (current.file.isFile) {
            var upNode: Node? = current
            while (upNode != root && upNode != null) {
                upNode = upNode.parent
                upNode?.addSize(current.size.get()) { onUpdate() }
            }
        }
    }
}