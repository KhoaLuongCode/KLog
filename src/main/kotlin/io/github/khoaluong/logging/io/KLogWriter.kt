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
            try {
                while (isActive) {
                    try {
                        val message = cw.channel.receive()
                        cw.outputStream.write(message.toByteArray())
                        cw.outputStream.flush()
                    } catch (e: ClosedReceiveChannelException) {
                        break
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        System.err.println("ERROR in KLogWriter ${cw.id}: ${e.message}")
                        e.printStackTrace(System.err)
                        break
                    }
                }
            } finally {
                try {
                    cw.outputStream.close()
                } catch (e: Exception) {
                    System.err.println("ERROR closing stream for writer ${cw.id}: ${e.message}")
                }
            }
        }
    }

    fun createWriter(id: String, bufferSize: Int, stream: OutputStream): Channel<String> {
        synchronized(channelWriterList) {
            if (channelWriterList.containsKey(id)) {
                println("WARN: KLogWriter already exists for id=$id, reusing/overwriting.")
            }
            val channel = Channel<String>(bufferSize)
            addChannelWriter(id, channel, stream)
            return channel
        }
    }

    fun startWriter(id: String) {
        synchronized(channelWriterList) {
            val cw = channelWriterList[id] ?: run {
                System.err.println("ERROR: Cannot start KLogWriter, ID not found: $id")
                return
            }
            if (!cw.running) {
                cw.job = initWriter(cw)
                cw.running = true
            }
        }
    }

    suspend fun stopWriter(id: String) {
        val cw: ChannelWriter?
        synchronized(channelWriterList) {
            cw = channelWriterList[id]
        }

        if (cw != null && cw.running) {
            cw.running = false
            try {
                cw.job?.cancelAndJoin()
            } catch (e: Exception) {
                System.err.println("ERROR during KLogWriter job cancel/join for id=${cw.id}: ${e.message}")
            }
            synchronized(channelWriterList) {
                channelWriterList.remove(id)
            }
        } else if (cw != null) {
            synchronized(channelWriterList) { channelWriterList.remove(id) }
        }
    }

    suspend fun stopAllWriters() {
        val idsToStop: List<String>
        synchronized(channelWriterList) {
            idsToStop = channelWriterList.keys.toList()
        }
        if (idsToStop.isEmpty()) {
            return
        }

        coroutineScope {
            idsToStop.forEach { id ->
                launch {
                    stopWriter(id)
                }
            }
        }
        synchronized(channelWriterList) { channelWriterList.clear() }
    }
}