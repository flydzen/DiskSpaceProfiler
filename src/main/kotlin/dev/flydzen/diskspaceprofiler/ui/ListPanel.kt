package dev.flydzen.diskspaceprofiler.ui

import dev.flydzen.diskspaceprofiler.Controller
import dev.flydzen.diskspaceprofiler.Node
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JPanel

class ListPanel(private val controller: Controller) : JPanel() {
    private var itemsList: List<ListItemPanel> = listOf()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        preferredSize = Dimension(800, 800)
        maximumSize = Dimension(800, 1000)
    }

    fun onStructureUpdate() {
        val rootNode = controller.root
        itemsList = rootNode?.let { getNodeItems(rootNode) } ?: mutableListOf()

        removeAll()
        itemsList.forEach { add(it) }

        revalidate()
        repaint()
        preferredSize = Dimension(size.width, itemsList.size * 28)
    }

    private fun getNodeItems(node: Node, level: Int = 0): MutableList<ListItemPanel> {
        val result = mutableListOf(ListItemPanel(controller, node, level) { onStructureUpdate() })
        if (node.visible) {
            result.addAll(
                node.getChildrenNodes()
                    .sortedByDescending { it.size.get() }
                    .flatMap { getNodeItems(it, level + 1) }
            )
        }
        return result
    }

    fun updateValues() {
        itemsList.forEach { it.updateSize() }
    }
}
