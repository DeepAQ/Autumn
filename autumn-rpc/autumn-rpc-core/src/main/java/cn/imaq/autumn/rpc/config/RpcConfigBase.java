package cn.imaq.autumn.rpc.config;

import cn.imaq.autumn.rpc.serialization.JsonSerialization;
import cn.imaq.autumn.rpc.serialization.RpcSerialization;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@Setter
@SuperBuilder
public abstract class RpcConfigBase {
    @Builder.Default
    private RpcSerialization serialization = new JsonSerialization();

    public String toConfigStr() {
        return Arrays.stream(RpcConfigBase.class.getDeclaredFields()).map(f -> {
            try {
                f.setAccessible(true);
                return f.getName() + "=" + f.get(this).getClass().getName();
            } catch (Exception ignored) {
            }
            return "";
        }).collect(Collectors.joining(","));
    }

    public static void applyConfigStr(String configStr, RpcConfigBase config) {
        String[] configEntries = configStr.split(",");
        for (String entry : configEntries) {
            String[] kv = entry.split("=", 2);
            if (kv.length == 2) {
                try {
                    Field field = RpcConfigBase.class.getDeclaredField(kv[0]);
                    field.setAccessible(true);
                    field.set(config, Class.forName(kv[1]).newInstance());
                } catch (Exception ignored) {
                }
            }
        }
    }
}
