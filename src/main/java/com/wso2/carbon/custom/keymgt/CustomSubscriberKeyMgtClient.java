package com.wso2.carbon.custom.keymgt;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceAPIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceAPIManagementException;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceIdentityException;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

public class CustomSubscriberKeyMgtClient extends SubscriberKeyMgtClient {
	private static Log log = LogFactory.getLog(CustomSubscriberKeyMgtClient.class);

	private APIKeyMgtSubscriberServiceStub subscriberServiceStub;

	public CustomSubscriberKeyMgtClient(String backendServerURL, String username, String password) throws Exception {
		super(backendServerURL, username, password);
		try {
			subscriberServiceStub = new APIKeyMgtSubscriberServiceStub(
					null, backendServerURL + "APIKeyMgtSubscriberService");
			ServiceClient client = subscriberServiceStub._getServiceClient();
			Options options = client.getOptions();
			options.setManageSession(true);
			CarbonUtils.setBasicAccessSecurityHeaders(username, password,
			                                          true, subscriberServiceStub._getServiceClient());

		} catch (Exception e) {
			String errorMsg = "Error when instantiating Custom SubscriberKeyMgtClient.";
			log.error(errorMsg, e);
			throw e;
		}
	}

	@Override
	public OAuthApplicationInfo updateOAuthApplication(String userId,
	                                                   String applicationName,
	                                                   String callbackUrl,
	                                                   String consumerKey,
	                                                   String[] grantTypes)
			throws RemoteException, APIKeyMgtSubscriberServiceAPIManagementException,
			       APIKeyMgtSubscriberServiceAPIKeyMgtException, APIKeyMgtSubscriberServiceIdentityException {
		return subscriberServiceStub
				.updateOAuthApplication(userId, applicationName, callbackUrl, consumerKey, grantTypes);
	}
}
