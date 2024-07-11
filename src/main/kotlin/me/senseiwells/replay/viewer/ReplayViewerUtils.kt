package me.senseiwells.replay.viewer

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.senseiwells.replay.ducks.`ServerReplay$ReplayViewable`
import me.senseiwells.replay.mixin.viewer.ClientboundPlayerInfoUpdatePacketAccessor
import net.fabricmc.fabric.impl.networking.payload.RetainedPayload
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.EnumSet
import com.replaymod.replaystudio.protocol.Packet as ReplayPacket

object ReplayViewerUtils {
    fun ReplayPacket.toClientboundPlayPacket(): Packet<*> {
        val buf = toFriendlyByteBuf(this.buf)
        try {
            return ConnectionProtocol.PLAY.createPacket(PacketFlow.CLIENTBOUND, this.id, buf)
                ?: throw IllegalStateException("Failed to create play packet with id ${this.id}")
        } finally {
            buf.release()
        }
    }

    private fun toFriendlyByteBuf(buf: com.github.steveice10.netty.buffer.ByteBuf): FriendlyByteBuf {
        // When we compile we map steveice10.netty -> io.netty
        // We just need this check for dev environment
        @Suppress("USELESS_IS_CHECK")
        if (buf is ByteBuf) {
            return FriendlyByteBuf(buf)
        }

        val array = ByteArray(buf.readableBytes())
        buf.readBytes(array)
        return FriendlyByteBuf(Unpooled.wrappedBuffer(array))
    }

    fun ServerGamePacketListenerImpl.sendReplayPacket(packet: Packet<*>) {
        (this as `ServerReplay$ReplayViewable`).`replay$sendReplayViewerPacket`(packet)
    }

    fun ServerGamePacketListenerImpl.startViewingReplay(viewer: ReplayViewer) {
        (this as `ServerReplay$ReplayViewable`).`replay$startViewingReplay`(viewer)
    }

    fun ServerGamePacketListenerImpl.stopViewingReplay() {
        (this as `ServerReplay$ReplayViewable`).`replay$stopViewingReplay`()
    }

    fun ServerGamePacketListenerImpl.getViewingReplay(): ReplayViewer? {
        return (this as `ServerReplay$ReplayViewable`).`replay$getViewingReplay`()
    }

    fun createClientboundPlayerInfoUpdatePacket(
        actions: EnumSet<Action>,
        entries: List<Entry>
    ): ClientboundPlayerInfoUpdatePacket {
        val packet = ClientboundPlayerInfoUpdatePacket(actions, listOf())
        @Suppress("KotlinConstantConditions")
        (packet as ClientboundPlayerInfoUpdatePacketAccessor).setEntries(entries)
        return packet
    }
}