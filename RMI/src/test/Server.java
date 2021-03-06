package test;

import message.RemoteMsg;
import message.RemoteMsgType;
import registry.FileRegistry;
import remote.ObjectTable;
import remote.RemoteInterface;
import remote.RemoteObjectRef;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Wei on 10/3/14.
 */
public class Server {
	public static final int LISTEN_PORT = 9901;

	public static String REG_PATH = "/Users/parasitew/Documents/CMU/15640/lab/lab2/registry/reg.dat";

	private String IPAddress = "localhost";
	private int dispatcherPort = 12345;
	private boolean isRunning;
	private Dispatcher dispatcher;
	private ObjectTable objTbl;
	private HashMap rorTbl;
	private int counter = 0; // objKey
	private FileRegistry registry;


	public Server() {
		this.dispatcher = new Dispatcher();
		this.isRunning = true;
		this.objTbl = new ObjectTable();
		this.rorTbl = new HashMap();
		this.registry = new FileRegistry(REG_PATH);
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Server srv = new Server();
		srv.registService();
		srv.startDispatcher();
	}

	public void registerRemoteObject(Object obj, String serviceName) throws IOException, ClassNotFoundException {
		objTbl.addObject(this.counter, obj);
		RemoteObjectRef ror = new RemoteObjectRef("localhost", 12345, this.counter, serviceName);
		this.rorTbl.put(obj, ror);
		this.registry.rebind(serviceName, ror);

		this.counter++;
	}

	public void registService() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
		String serviceName = "remote.TestService";

		// Create a remote object.
		Class<?> c = Class.forName(serviceName + "Impl");
		Object obj = c.newInstance();
		this.registerRemoteObject(obj, serviceName);
	}

	public void startDispatcher() {
		new Thread(this.dispatcher).start();
	}

	// Parse remoteMsg, return result by calling local methods.
	public RemoteMsg dispatch(RemoteMsg msg) throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, InvocationTargetException {
		RemoteMsg rtnMsg = new RemoteMsg();

		if (msg.getMsgType() == RemoteMsgType.MSG_INVOKE) {
			System.out.println("Invoking method.....");

			// The method to invoke.
			String methodName = msg.getMethodName();

			RemoteObjectRef ror = (RemoteObjectRef) msg.getContent();
			Object obj = objTbl.findObject(ror.getObjKey());

			Class<?> c = Class.forName(ror.getRemoteInterfaceName() + "Impl");
			Method[] methods = c.getDeclaredMethods();
			Method method = null;

			for (Method m : methods) {

				if (m.getName().equals(msg.getMethodName()) && msg.getParams().size() ==
						m.getParameterTypes().length) {
					method = m;
				}
			}

			assert method != null;
			Object[] params = this.unmarshallParams(msg.getParams());

			for (int i = 0; i < msg.getParams().size(); i++) {
				params[i] = msg.getParams().get(i);
			}

			Object rtn = method.invoke(obj, params);

			// Replace remote objects with stubs.
			rtnMsg = marshallReturnValue(rtn, method.getReturnType());

		} else {
			rtnMsg.setMsgType(RemoteMsgType.MSG_ERROR);
			System.out.println("Remote invocation error.");
		}

		System.out.println("return value: " + (String) rtnMsg.getContent());

		return rtnMsg;
	}

	// Get local object from RemoteObjectReference
	private Object[] unmarshallParams(List<Object> params) {
		Object[] res = new Object[params.size()];

		for (int i = 0; i < params.size(); i++) {
			if (params.get(i) instanceof RemoteObjectRef &&
					((RemoteObjectRef) params.get(i)).getIPAddress().equals(this.IPAddress)
					&& ((RemoteObjectRef) params.get(i)).getPort() == this.dispatcherPort) {
				int objKey = ((RemoteObjectRef) params.get(i)).getObjKey();

				res[i] = this.objTbl.findObject(objKey);
			} else {
				res[i] = params.get(i);
			}
		}

		System.out.println("Get local object from ROR (params).");

		return res;
	}

	public RemoteMsg marshallReturnValue(Object obj, Class c) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
		RemoteObjectRef ror = null;
		RemoteMsg rtnMsg = new RemoteMsg(RemoteMsgType.MSG_RETURN);

		if (obj instanceof RemoteInterface) {
			ror = (RemoteObjectRef) this.rorTbl.get(obj);
			rtnMsg.setContent(ror.localise());

			System.out.println("Return ROR to remote client.");
		} else {
			rtnMsg.setContent(obj);

			System.out.println("Return object to remote client.");
		}

		return rtnMsg;
	}

	// A proxy dispatcher that listens for the remote invocation from stub, dispatch it
	// to some local method and returns the result.
	class Dispatcher implements Runnable {

		@Override
		public void run() {
			try {
				ServerSocket srvSock = new ServerSocket(dispatcherPort);
				while (isRunning) {
					System.out.println("dispatcher is listening.");
					Socket reqSock = srvSock.accept();

					System.out.println("Receive remote connection.");
					ObjectInputStream ois = new ObjectInputStream(reqSock.getInputStream());
					ObjectOutputStream oos = new ObjectOutputStream(reqSock.getOutputStream());

					RemoteMsg msg = (RemoteMsg) ois.readObject();
					System.out.println("Read remote msg.");
					RemoteMsg rtnMsg = dispatch(msg);
					System.out.println("Write remote msg.");
					oos.writeObject(rtnMsg);

					System.out.println("Send return value back to remote.");
					oos.close();
					ois.close();
					reqSock.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

}