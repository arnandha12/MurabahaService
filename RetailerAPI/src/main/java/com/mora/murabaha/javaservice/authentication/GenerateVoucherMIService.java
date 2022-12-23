package com.mora.murabaha.javaservice.authentication;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class GenerateVoucherMIService implements JavaService2 {

	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
		Result result = new Result();
		if(preprocess(request, response)) {
			HashMap<String, Object> getinput = new HashMap<String, Object>();
			getinput.put("$filter", "vouchercode eq " + request.getParameter("vouchercode"));
			String res = DBPServiceExecutorBuilder.builder()
							.withServiceId("RetailerDBService")
							.withOperationId("dbxdb_voucher_get")
							.withRequestParameters(getinput)
							.build().getResponse();
		}
		return result;
	}

	private boolean preprocess(DataControllerRequest request, DataControllerResponse response) {
		String startDate = request.getParameter("startDate");
		String expiryDate = request.getParameter("expiryDate");
		String status = request.getParameter("status");
		String vouchercode = request.getParameter("vouchercode");
		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(expiryDate) && StringUtils.isNotBlank(status)) {
			return true;
		}
		return false;
	}

}
