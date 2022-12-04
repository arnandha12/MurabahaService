package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.mora.murabaha.utils.ErrorCodeEnum;
import com.temenos.infinity.api.commons.encrypt.BCrypt;

public class RetailerLogin implements JavaService2 {
	
	private static final Logger logger = LogManager.getLogger(RetailerLogin.class);
			
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
		Result result = new Result();
		logger.error("Retailer Login");
		if(preprocess(request, response)) {
			HashMap<String, Object> input = new HashMap<String, Object>();
			input.put("$filter", "UserName eq " + request.getParameter("UserName"));
			String res = DBPServiceExecutorBuilder.builder()
							.withServiceId("DBXDBRetailerServices")
							.withOperationId("dbxdb_retailer_get")
							.withRequestParameters(input)
							.build().getResponse();
			JsonObject retailerResponse = new JsonParser().parse(res).getAsJsonObject();
			if(!retailerResponse.getAsJsonArray("retailer").isJsonNull()) {
				String password = retailerResponse.getAsJsonArray("retailer").get(0).getAsJsonObject().get("password").getAsString();
				if(validatePassword(password, request.getParameter("Password"))) {
					Record securityAttrRecord = new Record();
					securityAttrRecord.setId("security_attributes");
					//generate session token
					String sessionToken = BCrypt.hashpw(request.getParameter("UserName").toString(), BCrypt.gensalt());
					securityAttrRecord.addParam(new Param("session_token", sessionToken));
					
					Record userAttrRecord = new Record();
					String userId = retailerResponse.getAsJsonArray("retailer").get(0).getAsJsonObject().get("userId").getAsString();
					userAttrRecord.setId("user_attributes");
					userAttrRecord.addParam(new Param("user_id", userId));
					result.addRecord(securityAttrRecord);
					result.addRecord(userAttrRecord);
				}
			} else {
				ErrorCodeEnum.ERR_90001.setErrorCode(result);
			}
		} else {
			ErrorCodeEnum.ERR_90000.setErrorCode(result);
		}
		return result;
	}
	
	private boolean preprocess(DataControllerRequest request, DataControllerResponse response) {
		boolean status = true;
		String username = request.getParameter("UserName");
		String password = request.getParameter("Password");
		if(username.isEmpty() || password.isEmpty()) {
			return false;
		}
		return status;
	}
	
	private Boolean validatePassword(String dbPassword, String currentPassword) throws Exception {
		boolean isPasswordValid = false;
		try {
			isPasswordValid = BCrypt.checkpw(currentPassword, dbPassword);
		} catch (Exception exception) {
			logger.error("Error in validating password", exception);
			throw exception;
		}
		logger.debug(
				(new StringBuilder()).append("Response from isPasswordValid  : ").append(isPasswordValid).toString());
		return Boolean.valueOf(isPasswordValid);
	}
}
