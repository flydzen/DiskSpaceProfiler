package dev.flydzen.diskspaceprofiler.ui

import dev.flydzen.diskspaceprofiler.Controller
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.Timer


const val  UPDATE_DELAY = 500 // ms

class View(controller: Controller) : JFrame("Disk space profiler") {
    private val topPanel = TopPanel(controller) { onUpdate() }
    private val leftPanel = ListPanel(controller)
    private val timer = Timer(UPDATE_DELAY) {
        leftPanel.updateValues()
        topPanel.setStatus(controller.getStatus())
    }

    init {
        layout = BorderLayout()
        defaultCloseOperation = EXIT_ON_CLOSE

        val scrollPane = JScrollPane(
            leftPanel,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
        )

        add(topPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.EAST)

        pack()
        isVisible = true
        topPanel.maximumSize = topPanel.size

        timer.start()
    }

    private fun onUpdate() {
        leftPanel.onStructureUpdate()
    }
}
