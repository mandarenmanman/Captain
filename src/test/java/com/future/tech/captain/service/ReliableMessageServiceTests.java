/**
 * 
 */
package com.future.tech.captain.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.future.tech.captain.AbstractSpringTests;
import com.future.tech.captain.api.CorrelationData;

/**
 * 
 * Title: ReliableMessageServiceTests.java<br>
 * Description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company:  FutureTech<br>
 * 
 * @author weilai
 * May 19, 2017
 */

public class ReliableMessageServiceTests extends AbstractSpringTests{
	@Autowired
	private ReliableMessageService reliableMessageService;
	
	@Test
	public void prepare() {
		CorrelationData correlationData = new CorrelationData();
		correlationData.setId("3");
		String message = "Captain Test";
		reliableMessageService.prepare(correlationData, message);
	}
	
	@Test
	public void confirm() {
		CorrelationData correlationData = new CorrelationData();
		correlationData.setId("3");
		reliableMessageService.confirm(correlationData);
	}
	
	@Test
	public void cancel() {
		CorrelationData correlationData = new CorrelationData();
		correlationData.setId("2");
		reliableMessageService.cancel(correlationData);
	}
}