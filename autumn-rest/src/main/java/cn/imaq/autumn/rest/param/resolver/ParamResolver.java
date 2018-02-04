package cn.imaq.autumn.rest.param.resolver;

import cn.imaq.autumn.rest.param.value.ParamValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;

public interface ParamResolver {
    ParamValue resolve(Parameter param, HttpServletRequest request, HttpServletResponse response);
}
