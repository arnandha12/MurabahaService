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

public class GetAllUserListService implements JavaService2 {

	private static final Logger logger = LogManager.getLogger(GetAllUserListService.class);
			
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
		Result result = new Result();
		if(preProcess(request)) {
			HashMap<String, Object> input = new HashMap<String, Object>();
			input.put("$filter", "retailerid eq " + request.getParameter("retailerid"));
			String res = DBPServiceExecutorBuilder.builder()
							.withServiceId("RetailerDBService")
							.withOperationId("dbxdb_retailer_get")
							.withRequestParameters(input)
							.build().getResponse();
			logger.error("Response :: "+res);
			JsonObject retailerResponse = new JsonParser().parse(res).getAsJsonObject();
			result = JSONToResult.convert(retailerResponse.toString());
		}
		return result;
	}

	private boolean preProcess(DataControllerRequest request) {
		boolean status = true;
		String userid = "",retailerid = "";
		userid = request.getParameter("userid");
		retailerid = request.getParameter("retailerid");
		if (StringUtils.isBlank(userid) || StringUtils.isBlank(retailerid)) {
			status = false;
		}
		return status;
	}
}
