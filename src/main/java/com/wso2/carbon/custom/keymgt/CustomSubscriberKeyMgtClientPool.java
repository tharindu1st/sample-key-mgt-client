package com.wso2.carbon.custom.keymgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClientPool;

public class CustomSubscriberKeyMgtClientPool {

	private static final Log log = LogFactory.getLog(SubscriberKeyMgtClientPool.class);

	private static final CustomSubscriberKeyMgtClientPool instance = new CustomSubscriberKeyMgtClientPool();

	private final ObjectPool clientPool;

	private String serverURL;

	private String username;

	private String password;

	private CustomSubscriberKeyMgtClientPool() {
		log.debug("Initializing Custom API Key Management Client Pool");
		clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
			@Override
			public Object makeObject() throws Exception {
				log.debug("Initializing new Custom SubscriberKeyMgtClient instance");
				return new CustomSubscriberKeyMgtClient(serverURL, username, password);
			}
		}, 20, 5);
	}

	public static CustomSubscriberKeyMgtClientPool getInstance() {
		return instance;
	}

	public CustomSubscriberKeyMgtClient get() throws Exception {
		return (CustomSubscriberKeyMgtClient) clientPool.borrowObject();
	}

	public void release(SubscriberKeyMgtClient client) {
		try {
			clientPool.returnObject(client);
		} catch (Exception e) {
			log.error("Error occurred while returning client back to pool.");
		}
	}

	public void cleanup() {
		try {
			clientPool.close();
		} catch (Exception e) {
			log.warn("Error while cleaning up the object pool", e);
		}
	}

	public void setConfiguration(KeyManagerConfiguration configuration) {
		this.serverURL = configuration.getParameter("ServerURL");
		this.username = configuration.getParameter("Username");
		this.password = configuration.getParameter("Password");
	}
}
