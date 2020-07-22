package com.preapm.agent.plugin.interceptor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import com.preapm.agent.common.bean.MethodInfo;
import com.preapm.agent.common.interceptor.AroundInterceptor;
import com.preapm.sdk.zipkin.ZipkinClientContext;
import com.preapm.sdk.zipkin.util.InetAddressUtils;
import com.preapm.sdk.zipkin.util.TraceKeys;

import zipkin.Endpoint;
import zipkin.Span;

/**
 * sun.net.www.protocol.http.HttpURLConnection.getInputStream()
 * 
 * 
 * 
 * @author Zengmin.Zhang
 *
 */
public class JdkConnectHttpInterceptor implements AroundInterceptor {

	// private final Logger logger =
	// LoggerFactory.getLogger(JdkConnectHttpInterceptor.class);

	public static final String SPAN_NAME_STR = "jdkhttp";

	@Override
	public void before(MethodInfo methodInfo) {
	}

	@Override
	public void exception(MethodInfo methodInfo) {
	}

	@Override
	public void after(MethodInfo methodInfo) {

		try {
			HttpURLConnection connection = (HttpURLConnection) methodInfo.getTarget();
			if (connection == null) {
				// logger.info("com.preapm.agent.plugin.interceptor.JdkConnectHttpInterceptor
				// start connection isull>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			}

			synchronized (connection) {
				if (connection != null) {
					/*
					 * String headerField =
					 * JdkResponseHttpInterceptor.getHeader(com.preapm.sdk.zipkin.util.TraceKeys.
					 * PRE_AGENT_NOT_TRACE_TAG, connection); if(headerField != null) { return; }
					 */
					// logger.info("com.preapm.agent.plugin.interceptor.JdkConnectHttpInterceptor
					// start >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					URL url = connection.getURL();
					int ipv4 = InetAddressUtils.localIpv4();
					Endpoint endpoint = Endpoint.builder().serviceName(ZipkinClientContext.serverName).ipv4(ipv4)
							.build();
					methodInfo.setLocalVariable(new Object[] { endpoint });
					ZipkinClientContext.getClient().startSpan(SPAN_NAME_STR);
					ZipkinClientContext.getClient().sendAnnotation(TraceKeys.CLIENT_SEND, endpoint);
					ZipkinClientContext.getClient().sendBinaryAnnotation(com.preapm.sdk.zipkin.util.TraceKeys.PRE_NAME,
							Arrays.toString(methodInfo.getPlugins()), endpoint);
					ZipkinClientContext.getClient().sendBinaryAnnotation(com.preapm.sdk.zipkin.util.TraceKeys.HTTP_URL,
							url.toString(), endpoint);
					String method = connection.getRequestMethod();
					String host = url.getHost();
					ZipkinClientContext.getClient().sendBinaryAnnotation(com.preapm.sdk.zipkin.util.TraceKeys.HTTP_HOST,
							host, endpoint);
					ZipkinClientContext.getClient()
							.sendBinaryAnnotation(com.preapm.sdk.zipkin.util.TraceKeys.HTTP_METHOD, method, endpoint);
					Span span = ZipkinClientContext.getClient().getSpan();
					if (span != null) {
						connection.addRequestProperty(com.preapm.sdk.zipkin.util.TraceKeys.TRACE_ID,
								Long.toHexString(span.traceId));
						connection.addRequestProperty(com.preapm.sdk.zipkin.util.TraceKeys.SPAN_ID,
								Long.toHexString(span.id));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String name() {
		return this.getClass().getName();
	}

}
