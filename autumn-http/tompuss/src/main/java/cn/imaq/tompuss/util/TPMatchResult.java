package cn.imaq.tompuss.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TPMatchResult<T> {
    private int length;

    private T object;
}
