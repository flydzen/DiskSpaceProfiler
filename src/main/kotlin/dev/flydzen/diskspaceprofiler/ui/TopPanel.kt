package dev.flydzen.diskspaceprofiler.ui

import dev.flydzen.diskspaceprofiler.Controller
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

    init {
        layout = FlowLayout()
        background = Color(80, 80, 90)

        add(navEditor)
        add(chooseBtn)
        add(submitBtn)
        border = BorderFactory.createLineBorder(Color.black)

        submitBtn.addActionListener {
            val path = Path(navEditor.text)
            controller.submit(path, onUpdate)
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
