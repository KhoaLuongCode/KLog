package io.github.khoaluong.logging.io

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.io.OutputStream


object KLogWriter {
    private val channelWriterList = mutableMapOf<String, ChannelWriter>()
    private val supervisor = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisor)

    private fun addChannelWriter(id: String, channel: Channel<String>, outputStream: OutputStream) {
        channelWriterList[id] = ChannelWriter(channel, outputStream, id)
    }

    private fun initWriter(cw: ChannelWriter): Job {
        return scope.launch {
            while (true) {
                try {
                    val message = cw.channel.receive()
                    cw.outputStream.write(message.toByteArray())
                    cw.outputStream.flush()
                } catch (e: ClosedReceiveChannelException) {
                    break
                } catch (e: CancellationException) {
                    println("Writer ${cw.id} cancelled.")
                    throw e
                } catch (e: Exception) {
                    System.err.println("Error writing log in writer ${cw.id}: ${e.message}")
                    break
                }
            }
            try {
                cw.outputStream.close()
            } catch (e: Exception) {
                System.err.println("Error closing stream for writer ${cw.id}: ${e.message}")
            }
        }
    }

    fun createWriter(id: String, bufferSize: Int, stream: OutputStream): Channel<String> {
        val channel = Channel<String>(bufferSize)
        addChannelWriter(id, channel, stream)
        return channel
    }

    fun startWriter(id: String) {
        val cw = channelWriterList[id] ?: return
        if (!cw.running) {
            cw.job = initWriter(cw)
            cw.running = true
        }
    }

    suspend fun stopWriter(id: String) {
        val cw = channelWriterList[id] ?: return
        if (cw.running) {
            cw.running = false
            cw.job?.cancelAndJoin()
            channelWriterList.remove(id)
        }
    }

    suspend fun stopAllWriters() {
        channelWriterList.values.forEach {
            it.job?.join()
        }
        channelWriterList.clear()
    }

}