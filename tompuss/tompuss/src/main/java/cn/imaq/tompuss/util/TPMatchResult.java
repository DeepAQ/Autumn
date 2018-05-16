package cn.imaq.tompuss.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TPMatchResult<T> {
    private String matched;

    private T object;
}
