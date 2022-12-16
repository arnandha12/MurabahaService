package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Result;
import com.mora.murabaha.utils.ErrorCodeEnum;
import com.temenos.infinity.api.commons.encrypt.BCrypt;

public class CreateUserService implements JavaService2{

	private static final Logger logger = LogManager.getLogger(CreateUserService.class);
			
	@SuppressWarnings("unchecked")
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result(); 
		if(preprocess(request, response)) {
			HashMap<String, Object> getinput = new HashMap<String, Object>();
			getinput.put("$filter", "UserId eq " + request.getParameter("userid"));
			String res = DBPServiceExecutorBuilder.builder()
							.withServiceId("RetailerDBService")
							.withOperationId("dbxdb_retailer_get")
							.withRequestParameters(getinput)
							.build().getResponse();
			logger.error("Response :: "+res);
			JsonObject retailerResponse = new JsonParser().parse(res).getAsJsonObject();
			logger.error("Size :: "+retailerResponse.getAsJsonArray("retailer").size());
			
			if(retailerResponse.getAsJsonArray("retailer").size() == 0) {
				HashMap<String, Object> params = (HashMap<String, Object>)inputArray[1];
				HashMap<String, Object> input = new HashMap<String, Object>();
				input.put("UserId", params.get("userid"));
				input.put("UserName", params.get("retailername"));
				input.put("Role", params.get("role"));
				input.put("PhoneNo", params.get("phonenumber"));
				input.put("EmailId", params.get("email"));
				input.put("RetailerId", params.get("retailerid"));
				input.put("TempPassword", generateActivationCode());
				String dbresponse = DBPServiceExecutorBuilder.builder()
									.withServiceId("RetailerDBService")
									.withOperationId("dbxdb_retailer_create")
									.withRequestParameters(input)
									.build().getResponse();
				JsonObject jsonResponse = new JsonParser().parse(dbresponse).getAsJsonObject();
				if(jsonResponse.getAsJsonArray("retailer").size() != 0) {
					result = JSONToResult.convert(jsonResponse.toString());
				} else {
					ErrorCodeEnum.ERR_90004.setErrorCode(result);
				}
			} else {
				ErrorCodeEnum.ERR_90005.setErrorCode(result);
			}
		} else {
			ErrorCodeEnum.ERR_90000.setErrorCode(result);
		}
		return result;
	}

	private boolean preprocess(DataControllerRequest request, DataControllerResponse response) {
		boolean status = true;
		String username = "",role = "", phonenumber = "", email = "", userid = "";
		username = request.getParameter("username");
		role = request.getParameter("role");
		phonenumber = request.getParameter("phonenumber");
		email = request.getParameter("email");
		userid = request.getParameter("userid");
		if(StringUtils.isBlank(username) || StringUtils.isBlank(role) || StringUtils.isBlank(phonenumber) || StringUtils.isBlank(email) || StringUtils.isBlank(userid)) {
			status = false;
		}
		return status;
	}

	private String generateActivationCode() {
		StringBuilder sb = new StringBuilder(8);
		String alphaNumbericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
		for (int i = 0; i < 8; i++) {
			int index = (int) (alphaNumbericString.length() * Math.random());
			sb.append(alphaNumbericString.charAt(index));
		}
		String Password = sb.toString();
		String hashedPwd = BCrypt.hashpw(Password, BCrypt.gensalt());
		return hashedPwd;
	}
}
