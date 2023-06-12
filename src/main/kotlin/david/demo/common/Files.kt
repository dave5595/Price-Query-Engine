package david.demo.common

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import kotlin.jvm.Throws


class Files {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun <T> loadFrom(filePath: String, transform: (BufferedReader)-> T): T {
            return try {
                BufferedReader(FileReader(filePath)).use { reader ->
                    transform(reader)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                throw e
            }
        }
    }
}