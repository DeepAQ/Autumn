package cn.imaq.tompuss.util;

import cn.imaq.tompuss.servlet.TPServletContext;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TPXmlUtil {
    public static void parseWebXml(TPServletContext context, File xmlFile) {
        log.info("Loading XML config file {}", xmlFile);
        try {
            Element root = new SAXReader().read(xmlFile).getRootElement();
            for (Element element : root.elements()) {
                switch (element.getName()) {
                    case "context-param":
                        context.setInitParameter(
                                element.elementTextTrim("param-name"),
                                element.elementText("param-value")
                        );
                        break;
                    case "servlet":
                        ServletRegistration.Dynamic sRegDyn = context.addServlet(
                                element.elementTextTrim("servlet-name"),
                                element.elementTextTrim("servlet-class")
                        );
                        for (Element initParam : element.elements("init-param")) {
                            sRegDyn.setInitParameter(
                                    initParam.elementTextTrim("param-name"),
                                    initParam.elementText("param-value")
                            );
                        }
                        Element loadOnStartup = element.element("load-on-startup");
                        if (loadOnStartup != null) {
                            sRegDyn.setLoadOnStartup(Integer.parseInt(loadOnStartup.getTextTrim()));
                        }
                        Element asyncSupported = element.element("async-supported");
                        if (asyncSupported != null) {
                            sRegDyn.setAsyncSupported(asyncSupported.getTextTrim().equals("true"));
                        }
                        break;
                    case "servlet-mapping":
                        ServletRegistration sReg = context.getServletRegistration(element.elementTextTrim("servlet-name"));
                        if (sReg != null) {
                            sReg.addMapping(element.elements("url-pattern").stream().map(Element::getTextTrim).toArray(String[]::new));
                        }
                        break;
                    case "filter":
                        FilterRegistration.Dynamic fRegDyn = context.addFilter(
                                element.elementTextTrim("filter-name"),
                                element.elementTextTrim("filter-class")
                        );
                        for (Element initParam : element.elements("init-param")) {
                            fRegDyn.setInitParameter(
                                    initParam.elementTextTrim("param-name"),
                                    initParam.elementText("param-value")
                            );
                        }
                        asyncSupported = element.element("async-supported");
                        if (asyncSupported != null) {
                            fRegDyn.setAsyncSupported(asyncSupported.getTextTrim().equals("true"));
                        }
                        break;
                    case "filter-mapping":
                        FilterRegistration fReg = context.getFilterRegistration(element.elementTextTrim("filter-name"));
                        if (fReg != null) {
                            Set<DispatcherType> dispatcherTypeSet = element.elements("dispatcher").stream()
                                    .map(e -> DispatcherType.valueOf(e.getTextTrim()))
                                    .collect(Collectors.toSet());
                            EnumSet<DispatcherType> dispatcherTypes;
                            if (dispatcherTypeSet.isEmpty()) {
                                dispatcherTypes = EnumSet.allOf(DispatcherType.class);
                            } else {
                                dispatcherTypes = EnumSet.copyOf(dispatcherTypeSet);
                            }
                            if (dispatcherTypes.isEmpty()) {
                                dispatcherTypes = EnumSet.of(DispatcherType.REQUEST);
                            }
                            String[] servletNames = element.elements("servlet-name").stream().map(Element::getTextTrim).toArray(String[]::new);
                            String[] urlPatterns = element.elements("url-pattern").stream().map(Element::getTextTrim).toArray(String[]::new);
                            if (servletNames.length > 0) {
                                fReg.addMappingForServletNames(dispatcherTypes, true, servletNames);
                            }
                            if (urlPatterns.length > 0) {
                                fReg.addMappingForUrlPatterns(dispatcherTypes, true, urlPatterns);
                            }
                        }
                        break;
                    case "listener":
                        context.addListener(element.elementTextTrim("listener-class"));
                        break;
                }
            }
        } catch (Exception e) {
            log.warn("Load XML config failed: {}", String.valueOf(e));
        }
    }
}
