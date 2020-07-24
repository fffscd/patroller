package com.preapm.agent.common.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.preapm.agent.common.bean.MethodInfo;
import com.preapm.agent.common.interceptor.AroundInterceptor;

public class AroundInterceptorContext {

	public static Set<AroundInterceptor> interceptors = new HashSet<AroundInterceptor>();

	public static Map<String, AroundInterceptor> interceptorsMap = new ConcurrentHashMap<String, AroundInterceptor>();

	public static void start(ClassLoader classLoader,MethodInfo methodInfo) {
		start(classLoader,methodInfo, methodInfo.getPlugins());
	}

	public static void start(ClassLoader classLoader,MethodInfo methodInfo, String... names) {
		for (AroundInterceptor i : get(classLoader,names)) {
			i.before(methodInfo);
		}
	}

	static {
		init();
	}

	public static void after(ClassLoader classLoader,MethodInfo methodInfo) {
		after(classLoader,methodInfo, methodInfo.getPlugins());
	}

	public static void exception(ClassLoader classLoader,MethodInfo methodInfo) {
		exception(classLoader,methodInfo, methodInfo.getPlugins());
	}

	public static void after(ClassLoader classLoader,MethodInfo methodInfo, String... names) {
		for (AroundInterceptor i : get(classLoader,names)) {
			i.after(methodInfo);
		}
	}

	public static void exception(ClassLoader classLoader,MethodInfo methodInfo, String... names) {
		for (AroundInterceptor i : get(classLoader,names)) {
			i.exception(methodInfo);
		}
	}

	public static List<AroundInterceptor> get(Set<String> names,ClassLoader classLoader) {
		checkName(names,classLoader);
		// System.out.println("com.preapm.agent.common.context.AroundInterceptorContext.get(Set<String>)参数："+names.size());
		// System.out.println("com.preapm.agent.common.context.AroundInterceptorContext.get(Set<String>)interceptorsMap参数："+interceptorsMap.size());
		List<AroundInterceptor> list = new ArrayList<AroundInterceptor>();
		/*for (Entry<String, AroundInterceptor> e : interceptorsMap.entrySet()) {
			String name = e.getKey();
			// System.out.println("插件map包的名字："+name);
			if (names.contains(name)) {
				list.add(e.getValue());
				// System.out.println("执行插件名称："+name);
			}
		}*/
		for(String n:names) {
			AroundInterceptor aroundInterceptor = interceptorsMap.get(n);
			if(aroundInterceptor!=null) {
				list.add(aroundInterceptor);
			}
		}
		return list;
	}

	public static void checkName(Set<String> names,ClassLoader classLoader) {
		for (String n : names) {
			if (!interceptorsMap.containsKey(n)) {
				init(n,classLoader);
			}
		}
	}

	public static boolean init(String namePlugin,ClassLoader classLoader) {
		try {
			Class<?> classPlugin = Class.forName(namePlugin,false,classLoader);
			AroundInterceptor newInstance = (AroundInterceptor) classPlugin.newInstance();
			interceptorsMap.put(namePlugin, newInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("初始化成功 name:" + namePlugin);
		return true;
	}
	
	
	public static MethodInfo loader(ClassLoader classLoader) {
		try {
			if(classLoader == null) {
				classLoader = ClassLoader.getSystemClassLoader();
			}
			Class<?> classPlugin = Class.forName("com.preapm.agent.common.bean.MethodInfo",false,classLoader);
			MethodInfo newInstance = (MethodInfo) classPlugin.newInstance();
			return newInstance;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}

	public static List<AroundInterceptor> get(ClassLoader classLoader,String... names) {
		Set<String> set = new LinkedHashSet<>();
		for (String n : names) {
			set.add(n);
		}
		return get(set,classLoader);
	}

	public static void init() {
		synchronized (AroundInterceptorContext.class) {
			// if (interceptors == null || interceptors.size() == 0) {
			ServiceLoader<AroundInterceptor> serviceLoader = ServiceLoader.loadInstalled(AroundInterceptor.class);
			Iterator<AroundInterceptor> iterator = serviceLoader.iterator();
			while (iterator.hasNext()) {
				AroundInterceptor animal = iterator.next();
				interceptors.add(animal);
				String name = animal.name();
				interceptorsMap.put(name, animal);
			}
			// }
		}

	}

	public static void addInterceptor(AroundInterceptor aroundInterceptor) {
		interceptors.add(aroundInterceptor);
	}
	public static boolean containsInterceptor(String namePlugin) {
		return interceptorsMap.containsKey(namePlugin);
	}
	
	public static void addInterceptor(String namePlugin,AroundInterceptor aroundInterceptor) {
		if(interceptorsMap.containsKey(namePlugin)) {
			return ;
		}
		interceptorsMap.put(namePlugin,aroundInterceptor);
	}
}
