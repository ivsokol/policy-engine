package io.github.ivsokol.poe.examples

import java.io.File

object ExampleWriter {
  fun save(folder: String, name: String, content: String) {
    val path = "build/examples/$folder"
    val folderToWrite = File(path)
    if (!folderToWrite.exists()) folderToWrite.mkdirs()
    File("$path/$name.json").writeText(content)
  }
}
