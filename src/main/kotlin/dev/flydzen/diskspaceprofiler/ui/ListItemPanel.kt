package dev.flydzen.diskspaceprofiler.ui

import dev.flydzen.diskspaceprofiler.Controller
import dev.flydzen.diskspaceprofiler.Node
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.BorderFactory
import javax.swing.JOptionPane


class ListItemPanel(
    private val controller: Controller,
    private val node: Node,
    level: Int,
    private val onUpdate: () -> Unit,
): JPanel() {
    private val inBtn = JButton(">")
    private val pathLabel = JTextField(node.file.toString(), 192)
    private val sizeLabel = JLabel(bytesToStr(node.size.get()), SwingConstants.LEFT)
    private val delBtn = JButton("X")

    init {
        layout = GridBagLayout()

        minimumSize = Dimension(500, 28)
        maximumSize = Dimension(1000, 28)

        delBtn.background = Color(140, 5, 50)
        border = BorderFactory.createMatteBorder(1, level * 8, 1, 1, Color(38, 38, 38))
        setBtnImg()

        val constraints = GridBagConstraints()

        constraints.gridx = 0
        constraints.gridy = 0
        constraints.weightx = 0.1
        constraints.fill = GridBagConstraints.NONE
        constraints.anchor = GridBagConstraints.FIRST_LINE_START
        add(inBtn, constraints)

        constraints.gridx = 1
        constraints.gridy = 0
        constraints.weightx = 0.6
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.anchor = GridBagConstraints.FIRST_LINE_START
        pathLabel.isEnabled = false
        pathLabel.disabledTextColor = Color.DARK_GRAY
        add(pathLabel, constraints)

        constraints.gridx = 2
        constraints.gridy = 0
        constraints.weightx = 0.2
        constraints.fill = GridBagConstraints.NONE
        constraints.anchor = GridBagConstraints.FIRST_LINE_END
        add(sizeLabel, constraints)

        constraints.gridx = 3
        constraints.gridy = 0
        constraints.weightx = 0.1
        constraints.fill = GridBagConstraints.NONE
        constraints.anchor = GridBagConstraints.FIRST_LINE_END
        add(delBtn, constraints)

        inBtn.addActionListener {
            println("Clicked on ${node.file}")
            controller.setVisibility(node)
            setBtnImg()
            onUpdate()
        }

        delBtn.addActionListener {
            println("Deleting ${node.file}")
            if (showDeleteConfirmationDialog()) {
                controller.delete(node) { onUpdate() }
            }
        }
    }

    private fun showDeleteConfirmationDialog(): Boolean {
        val options = arrayOf("Delete", "Cancel")
        val choice = JOptionPane.showOptionDialog(
            null,
            "Are you sure you want to delete '${node.file}'?",
            "Delete Confirmation",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[1]
        )

        return choice == 0
    }

    private fun setBtnImg() {
        if (node.isFile) {
            inBtn.text = "F"
            inBtn.background = Color(88, 188, 88)
        } else {
            inBtn.text = if (node.visible) "˅" else "˃"
            inBtn.background = Color(88, 88, 188)
        }
    }

    fun updateSize() {
        sizeLabel.text = bytesToStr(node.size.getAcquire())
    }

    private fun bytesToStr(bytes: Long): String {
        val kiloByte = 1024L
        val megaByte = kiloByte * 1024
        val gigaByte = megaByte * 1024
        val teraByte = gigaByte * 1024

        return when {
            bytes >= teraByte -> "%.1f Tb".format(bytes.toDouble() / teraByte)
            bytes >= gigaByte -> "%.1f Gb".format(bytes.toDouble() / gigaByte)
            bytes >= megaByte -> "%.1f Mb".format(bytes.toDouble() / megaByte)
            else -> "%.1f Kb".format(bytes.toDouble() / kiloByte)
        }
    }
}
