Enterpackage com.example.killlightning;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LightningPayload(double x, double y, double z) implements CustomPayload {

    public static final CustomPayload.Id<LightningPayload> ID =
            new CustomPayload.Id<>(Identifier.of(KillLightning.MOD_ID, "lightning"));

    public static final PacketCodec<RegistryByteBuf, LightningPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, LightningPayload::x,
            PacketCodecs.DOUBLE, LightningPayload::y,
            PacketCodecs.DOUBLE, LightningPayload::z,
            LightningPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
