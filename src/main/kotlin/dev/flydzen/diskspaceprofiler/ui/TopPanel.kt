package dev.flydzen.diskspaceprofiler.ui

import dev.flydzen.diskspaceprofiler.Controller
import dev.flydzen.diskspaceprofiler.Status
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.io.path.Path


class TopPanel(private val controller: Controller, onUpdate: () -> Unit) : JPanel() {
    private val navEditor = JTextField(50)
    private val chooseBtn = JButton("\uD83D\uDD0D")
    private val submitBtn = JButton("Submit")

    fun setStatus(status: Status) {
        submitBtn.background = when (status) {
            Status.EMPTY -> Color.LIGHT_GRAY
            Status.IN_PROGRESS -> Color.YELLOW
            Status.FINISHED -> Color.GREEN
        }
        submitBtn.text = if (status == Status.IN_PROGRESS) "Stop" else "Submit"
    }

    init {
        layout = FlowLayout()
        background = Color(80, 80, 90)

        add(navEditor)
        add(chooseBtn)
        add(submitBtn)
        border = BorderFactory.createLineBorder(Color.black)

        submitBtn.background = Color.LIGHT_GRAY
        submitBtn.addActionListener {
            val path = Path(navEditor.text)
            controller.submit(path, onUpdate)
            setStatus(controller.getStatus())
        }

        chooseBtn.addActionListener {
            val fileChooser = JFileChooser()
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

            val result = fileChooser.showOpenDialog(parent)
            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedFolder = fileChooser.selectedFile
                navEditor.text = selectedFolder.toPath().toString()
            }
        }
    }
}
