/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.almibe.multipage.demo

import javafx.embed.swing.JFXPanel
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.TextArea
import org.almibe.multipage.MultiPageDisplay
import org.almibe.multipage.Page
import org.almibe.multipage.PageBuilder
import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.*

object MultiPageDisplayDemo {

    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            val frame = JFrame("Demo")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

            frame.contentPane.add(create().component)
            frame.pack()
            frame.isVisible = true
            frame.setSize(1000, 400)
        }
    }

    private fun create(): MultiPageDisplay {
        val multiPageDisplay = MultiPageDisplay {
            PageBuilder()
                .setTitle("Hello Demo")
                .setIcon(createImageView())
                .setContent(JLabel("Hello Demo Content")).createPage()
        }

        val page = PageBuilder()
            .setTitle("Hello Demo 1")
            .setIcon(createImageView())
            .setContent(JLabel("Hello Demo Content 2")).createPage()

        val pagefx = PageBuilder()
            .setTitle("Java FX Example")
            .setIcon(createImageView())
            .setContent(createJavaFXContent()).createPage()

        val page2 = PageBuilder()
            .setTitle("Hello No Icon 2")
            .setContent(JTextField("Hello Demo Content 2?")).createPage()

        val page3 = PageBuilder()
            .setTitle("Hello Demo 3")
            .setIcon(createImageView())
            .setContent(JLabel("Hello Demo Content 3!")).createPage()

        val page4 = PageBuilder()
            .setTitle("Hello Demo 4")
            .setIcon(createImageView())
            .setContent(JLabel("Hello Demo Content 4?!?!!")).createPage()

        val page5 = PageBuilder()
            .setTitle("Prompt to close")
            .setIcon(createImageView())
            .setContent(JLabel("Click yes to close"))
            .setAllowClose {
                val reply = JOptionPane.showConfirmDialog(null, "Close tab?", "Close tab?", JOptionPane.YES_NO_OPTION)
                reply == JOptionPane.YES_OPTION
            }.createPage()

        val page6 = PageBuilder()
            .setTitle("Test")
            .setIcon(createImageView())
            .setContent(JLabel("Press W"))
            .setKeyEventDispatcher(KeyEventDispatcher { e ->
                if (e.keyCode == KeyEvent.VK_W) {
                    println("You Pressed W")
                }
                true
            }).createPage()

        Arrays.asList(page, page2, page3, page4, page5, page6, pagefx).forEach { it -> multiPageDisplay.addPage(it) }

        for (x in 0..19) {
            multiPageDisplay.addPage(createAnonPage())
        }

        return multiPageDisplay
    }

    private var x = 0
    private fun createAnonPage(): Page {
        return PageBuilder()
            .setTitle("Anon " + ++x)
            .setIcon(createImageView())
            .setContent(JLabel("Hello Demo Content " + x)).createPage()
    }

    private fun createImageView(): ImageIcon {
        return ImageIcon(MultiPageDisplayDemo::class.java.getResource("page_white_text.png"))
    }

    private fun createJavaFXContent(): JPanel {
        val panel = JPanel()
        val jfxPanel = JFXPanel()

        panel.add(jfxPanel)
        val scene = createScene()
        jfxPanel.scene = scene
        return panel
    }

    private fun createScene(): Scene {
        val root = Group()
        val scene = Scene(root)
        val text = TextArea("Sample text")

        root.children.add(text)
        return scene
    }
}
