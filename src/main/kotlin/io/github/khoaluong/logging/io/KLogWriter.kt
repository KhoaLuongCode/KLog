package io.github.khoaluong.logging.io

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.OutputStream


object KLogWriter {
    private val channelWriterList = mutableMapOf<String, ChannelWriter>()
    private val supervisor = SupervisorJob()

    private fun addChannelWriter(id: String, channel: Channel<String>, outputStream: OutputStream) {
        channelWriterList[id] = ChannelWriter(channel, outputStream, id)
    }

    private fun initWriter(cw: ChannelWriter): Job {
        //println("Starting writer for ${cw.id}")
        return CoroutineScope(Dispatchers.IO + supervisor).launch {
            while (true) {
                val message = cw.channel.receive()
                cw.outputStream.write(message.toByteArray())
                cw.outputStream.flush()
            }
        }
    }
    fun createWriter(id: String, bufferSize: Int, stream: OutputStream): Channel<String> {
        val channel = Channel<String>(bufferSize)
        addChannelWriter(id, channel, stream)
        return channel
    }

    fun startWriter(id: String) {
        //println(id)
        val cw = channelWriterList[id] ?: return
        if (!cw.running) {
            cw.job = initWriter(cw)
            cw.running = true
        }
    }

    fun stopWriter(id: String) {
        val cw = channelWriterList[id] ?: return
        if (cw.running) {
            cw.job?.cancel()
            cw.running = false
            cw.outputStream.close()
        }
    }

    fun stopAllWriters() {
        supervisor.cancel()
        channelWriterList.clear()
    }

}