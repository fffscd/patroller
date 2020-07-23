package com.preapm.agent.plugin.interceptor;

import com.preapm.agent.common.interceptor.ClassInterceptor;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;

public class JdkThreadInterceptor implements ClassInterceptor {

	@Override
	public String name() {
		return this.getClass().getName();
	}

	@Override
	public void callback(CtClass ctClass) {
		try {
			CtField f = CtField.make(
					"private zipkin.Span span =  com.preapm.sdk.zipkin.ZipkinClientContext.getClient().getSpan();",
					ctClass);
			ctClass.addField(f);
		} catch (CannotCompileException e) {
			e.printStackTrace();
		}

	}

}
