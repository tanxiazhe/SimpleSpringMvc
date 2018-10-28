package com.maomao2.simplespringmvc.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maomao2.simplespringmvc.annotation.Controller;
import com.maomao2.simplespringmvc.annotation.Qualifier;
import com.maomao2.simplespringmvc.annotation.Repository;
import com.maomao2.simplespringmvc.annotation.RequestMapping;
import com.maomao2.simplespringmvc.annotation.Service;

@WebServlet(name = "dispatcherServlet", urlPatterns = "/*", loadOnStartup = 1, initParams = {
        @WebInitParam(name = "base-package", value = "com.maomao2.simplespringmvc,com.maomao2.test") })
public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 9030229924977300889L;

    private String basePackage;
    private List<String> packageNames = new ArrayList<String>();

    private Map<String, Object> beanInstanceMap = new HashMap<String, Object>();
    private Map<String, String> nameMap = new HashMap<String, String>();
    private Map<String, Method> urlMethodMap = new HashMap<String, Method>();
    private Map<Method, String> methodPackageMap = new HashMap<Method, String>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        basePackage = config.getInitParameter("base-package");
        scanBasePackage(basePackage);

        try {
            initParams(packageNames);
            springIOC();
            handlerUrlMethodMap();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.replaceAll(contextPath, "");

        Method method = urlMethodMap.get(path);
        if (method != null) {
            String packageName = methodPackageMap.get(method);
            String controllerName = nameMap.get(packageName);
            Object controller = beanInstanceMap.get(controllerName);
            method.setAccessible(true);
            try {
                method.invoke(controller);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void scanBasePackage(String basePackage) {
        if (basePackage.indexOf(",") > 0) {
            String[] packageNameArr = basePackage.split(",");
            for (String packageName : packageNameArr) {
                doScanBasePackage(packageName);
            }
        } else {
            doScanBasePackage(basePackage);
        }
    }

    private void doScanBasePackage(String basePackage) {
        String packageDirName = basePackage.replaceAll("\\.", "/");
        URL url = this.getClass().getClassLoader().getResource(packageDirName);
        File basePackageFile = new File(url.getPath());
        File[] childFiles = basePackageFile.listFiles();

        for (File file : childFiles) {
            if (file.isDirectory()) {
                doScanBasePackage(basePackage + "." + file.getName());
            } else if (file.isFile()) {
                packageNames.add(basePackage + "." + file.getName().split("\\.")[0]);
            }
        }
    }

    private void initParams(List<String> packageNames)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (packageNames.size() < 1) {
            return;
        }

        for (String packageName : packageNames) {
            Class<?> beanClass = Class.forName(packageName);

            if (beanClass.isAnnotationPresent(Controller.class)) {
                Controller controller = (Controller) beanClass.getAnnotation(Controller.class);
                String controllerName = controller.value();
                if (!"".equals(controllerName)) {
                    beanInstanceMap.put(controllerName, beanClass.newInstance());
                    nameMap.put(packageName, controllerName);
                }
            } else if (beanClass.isAnnotationPresent(Service.class)) {
                Service service = (Service) beanClass.getAnnotation(Service.class);
                String serviceName = service.value();
                if (!"".equals(serviceName)) {
                    beanInstanceMap.put(serviceName, beanClass.newInstance());
                    nameMap.put(packageName, serviceName);
                }

            } else if (beanClass.isAnnotationPresent(Repository.class)) {
                Repository repository = (Repository) beanClass.getAnnotation(Repository.class);
                String repositoryName = repository.value();
                if (!"".equals(repositoryName)) {
                    beanInstanceMap.put(repositoryName, beanClass.newInstance());
                    nameMap.put(packageName, repositoryName);
                }
            }
        }
    }

    private void springIOC() throws IllegalAccessException {
        for (Map.Entry<String, Object> entry : beanInstanceMap.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    String fieldName = qualifier.value();
                    field.setAccessible(true);
                    field.set(entry.getValue(), beanInstanceMap.get(fieldName));
                }
            }
        }
    }

    private void handlerUrlMethodMap() throws ClassNotFoundException {
        if (packageNames.size() < 1) {
            return;
        }
        for (String packageName : packageNames) {
            Class<?> controllerBeanClass = Class.forName(packageName);

            if (controllerBeanClass.isAnnotationPresent(Controller.class)) {
                Method[] methods = controllerBeanClass.getMethods();
                String baseUrl = null;

                if (controllerBeanClass.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = (RequestMapping) controllerBeanClass
                            .getAnnotation(RequestMapping.class);
                    String classRequestMappingName = requestMapping.value();
                    baseUrl = classRequestMappingName;
                }

                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = (RequestMapping) method.getAnnotation(RequestMapping.class);
                        String methodRequestMappingName = requestMapping.value();
                        String methodRequestMappingUrl = baseUrl + methodRequestMappingName;

                        if (methodRequestMappingUrl != null && !"".equals(methodRequestMappingUrl)) {
                            if (urlMethodMap.containsKey(methodRequestMappingUrl.toString())) {
                                throw new RuntimeException("RequestMapping url is not allow duplicated！");
                            }

                            urlMethodMap.put(methodRequestMappingUrl.toString(), method);
                            methodPackageMap.put(method, packageName);
                        }
                    }
                }
            }
        }
    }
}
