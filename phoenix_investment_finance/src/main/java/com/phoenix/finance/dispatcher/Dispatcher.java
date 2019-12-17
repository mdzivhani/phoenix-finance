package com.phoenix.finance.dispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.phoenix.finance.web.Controller;

@WebServlet(urlPatterns = "/finance/*", loadOnStartup = 1)
@SuppressWarnings("serial")
public class Dispatcher extends HttpServlet {

	private static final Map<String, Method> MAPPED_METHODS = new HashMap<String, Method>();

	private static final Map<String, Controller> CONTROLLERS = new HashMap<String, Controller>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo.contains(".jsp")) {
			RequestDispatcher dispatcher = req.getRequestDispatcher(Controller.JSP_PATH + pathInfo);
			dispatcher.forward(req, resp);
		} else {
			if (MAPPED_METHODS.containsKey(pathInfo)) {
				Method method = MAPPED_METHODS.get(pathInfo);
				try {
					method.invoke(CONTROLLERS.get(pathInfo.split("/")[1]), req, resp);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					req.setAttribute("message", "A techincal error has occured.");
					RequestDispatcher dispatcher = req.getRequestDispatcher(Controller.JSP_PATH + "error.jsp");
					dispatcher.forward(req, resp);
					e.printStackTrace();
				}
			} else {
				req.setAttribute("message", "Oops! There is no such page");
				RequestDispatcher dispatcher = req.getRequestDispatcher(Controller.JSP_PATH + "error.jsp");
				dispatcher.forward(req, resp);
			}
		}
	}

	@Override
	public void init() throws ServletException {
		try {
			Properties properties = new Properties();
			InputStream in = Dispatcher.class.getClassLoader().getResourceAsStream("mapping.properties");
			properties.load(in);

			Set<Object> keySet = properties.keySet();
			for (Object key : keySet) {
				String stringKey = (String) key;
				String[] keySplit = stringKey.split("/");
				String[] valueSplit = properties.getProperty(stringKey).split("#");
				String controllerKey = keySplit[1];
				String controllerClassName = valueSplit[0];
				String methodName = valueSplit[1];

				addController(controllerKey, controllerClassName);
				addMethod(methodName, controllerKey, stringKey);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void addController(String controllerKey, String controllerClassName) {
		BeanManager beanManager = CDI.current().getBeanManager();
		Set<Bean<?>> beans = beanManager.getBeans(Object.class);
		// looping through the beans that the container created to wrap controllers
		for (Bean<?> bean : beans) {
			if (bean.getBeanClass().getName().equals(controllerClassName)) {
				Controller controller = (Controller) beanManager.getReference(bean, bean.getBeanClass(),
						beanManager.createCreationalContext(bean));
				CONTROLLERS.put(controllerKey, controller);
			}
		}
	}

	private void addMethod(String methodName, String controllerKey, String stringKey) {
		if (!MAPPED_METHODS.containsKey(stringKey)) {
			try {
				Method method = CONTROLLERS.get(controllerKey).getClass().getMethod(methodName, HttpServletRequest.class,
						HttpServletResponse.class);
				MAPPED_METHODS.put(stringKey, method);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
