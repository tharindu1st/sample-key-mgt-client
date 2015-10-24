package com.wso2.carbon.custom.keymgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.keymgt.AMDefaultKeyManagerImpl;

public class CustomKeyMgtService extends AMDefaultKeyManagerImpl {
	private static final Log log = LogFactory.getLog(CustomKeyMgtService.class);

	@Override public OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws APIManagementException {
		OAuthApplicationInfo oAuthApplicationInfo = appInfoDTO.getOAuthApplicationInfo();
		CustomSubscriberKeyMgtClient keyMgtClient = null;
		ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
		try {
			keyMgtClient = CustomSubscriberKeyMgtClientPool.getInstance().get();

			String userId = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME);
			String applicationName = oAuthApplicationInfo.getClientName();
			log.debug("Updating OAuth Client with ID : " + oAuthApplicationInfo.getClientId());
			String tokenType = apiMgtDAO.getApplicationIdAndTokenTypeByConsumerKey(oAuthApplicationInfo.getClientId())
			                            .get("token_type");
			if (log.isDebugEnabled() && oAuthApplicationInfo.getCallBackURL() != null) {
				log.debug("CallBackURL : " + oAuthApplicationInfo.getCallBackURL());
			}

			if (log.isDebugEnabled() && oAuthApplicationInfo.getClientName() != null) {
				log.debug("Client Name : " + oAuthApplicationInfo.getClientName());
			}
			String[] grantTypes = { "client_credential,application_token,password,refresh_token" };
			try {
				JSONObject params = new JSONObject(oAuthApplicationInfo.getJsonString());
				if (params.has(ApplicationConstants.OAUTH_CLIENT_GRANT)) {
					grantTypes = ((String) params.get(ApplicationConstants.OAUTH_CLIENT_GRANT)).split(",");
				}
			} catch (JSONException e) {
				log.error("Can not retrieve grant_types from json Object", e);
			}
			applicationName = applicationName + "_" + tokenType.toUpperCase();
			org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationInfo = keyMgtClient
					.updateOAuthApplication(userId, applicationName, oAuthApplicationInfo.getCallBackURL(),
					                        oAuthApplicationInfo.getClientId(), grantTypes);
			OAuthApplicationInfo newAppInfo = new OAuthApplicationInfo();
			newAppInfo.setClientId(applicationInfo.getClientId());
			newAppInfo.setCallBackURL(applicationInfo.getCallBackURL());
			newAppInfo.setClientSecret(applicationInfo.getClientSecret());

			return newAppInfo;
		} catch (Exception e) {
			log.error("Error occurred while updating OAuth Client : ", e);
		} finally {
			if (keyMgtClient != null) {
				CustomSubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
			}
		}
		return null;
	}

	@Override public void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException {
		super.loadConfiguration(configuration);
		CustomSubscriberKeyMgtClientPool.getInstance().setConfiguration(configuration);
	}
}
