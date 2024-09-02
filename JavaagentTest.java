package com.devexperts.rmi.impl;

import com.devexperts.io.Marshalled;
import com.devexperts.qd.qtp.AgentAdapter;
import com.devexperts.qd.qtp.QDEndpoint;
import com.devexperts.qd.stats.QDStats;
import com.devexperts.rmi.RMIEndpoint;
import com.devexperts.rmi.RMIOperation;
import com.devexperts.rmi.message.RMIRequestMessage;
import com.devexperts.rmi.message.RMIRequestType;
import com.devexperts.rmi.message.RMIRoute;
import com.devexperts.rmi.task.RMIService;
import com.devexperts.util.TypedMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;

class JavaagentTest {

	@BeforeAll
	public static void init() throws Exception {
		String jarFilePath = "build/libs/traffic-inspect-agent.jar";
		String options = "onlyPayload=false:verbose=true" +
				":policyFile=../targetServiceExample/src/test/resources/traffic_inspection_policy_test.txt" +
				":dependenciesPath=build/libs";
		System.out.println(new File(".").getAbsolutePath());
		// todo: create jar file
//		AgentLoader.loadAgent(jarFilePath, options);
	}

	@Test
	public void test1() throws NoSuchMethodException {
//		xxx();


		long requestId = 1L;
//		RMIConnection rmiConnection = Mockito.mock(RMIConnection.class);

		QDEndpoint qdEndpoint = QDEndpoint.newBuilder().build();
		AgentAdapter.Factory factory = new AgentAdapter.Factory(qdEndpoint, null);
		RMIEndpointImpl rmiEndpoint = new RMIEndpointImpl(RMIEndpoint.Side.SERVER, qdEndpoint, factory, null);
		RMIConnection rmiConnection = new RMIConnection(rmiEndpoint,
				QDStats.VOID, null, ServiceFilter.ANYTHING, AdvertisementFilter.ALL, 100);
		rmiConnection.messageAdapter.setConnectionVariables(new TypedMap());
		RMIService rmiService = Mockito.mock(RMIService.class);
		Executor executor = Mockito.mock(Executor.class);


		List<RMIRequestMessage> requests = List.of(createFoo1RequestMessage());
//		RMIRequestMessage request = createFoo2RequestMessage();
//		List<RMIRequestMessage> requests  = List.of(
//				createFoo3RequestMessage("I'm cool. Bla bla bla"),
//				createFoo3RequestMessage("I'm cool. foo"),
//				createFoo3RequestMessage("I'm cool. DROP DATABASE; COMMIT;"),
//				createFoo3RequestMessage("I'm cool. LoG eM"),
//				createFoo3RequestMessage("I'm cool. forgetMe"));

		for (RMIRequestMessage request : requests) {
			RMITaskImpl task = RMITaskImpl.createTopLevelTask(Marshalled.forObject("SUBJECT"),
					request, rmiConnection, requestId);

			RMIExecutionTaskImpl rmiExecutionTask = new RMIExecutionTaskImpl<>(requestId, rmiConnection, task, rmiService, executor);

			try {
				rmiExecutionTask.enqueueForSubmissionSerially();

				System.out.println(rmiExecutionTask.getTask().getState());
			} catch (Throwable t) {
				t.printStackTrace();
			}

		}

		System.out.println("DONE");
	}


	@NotNull
	private static RMIRequestMessage createFoo1RequestMessage() throws NoSuchMethodException {
		Method method = FooService.class.getDeclaredMethod("foo1", String.class, String.class);
		RMIOperation operation = RMIOperation.valueOf(FooService.class, method);
		Object[] payload = new Object[] {"param_XXX", "param_YYY"};
		return new RMIRequestMessage(RMIRequestType.DEFAULT, operation, getBinaryPayload(operation, payload), RMIRoute.EMPTY, null);
	}

	public static Marshalled<Object> getBinaryPayload(RMIOperation operation, Object[] payload) {
		Marshalled parameters = Marshalled.forObject(payload, operation.getParametersMarshaller());
		return Marshalled.forBytes(parameters.getBytes(), operation.getParametersMarshaller());
	}

	public interface FooService {
		String foo1(String param1, String param2);
	}
}