/**
 * 
 */
package com.future.tech.captain.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.future.tech.captain.api.CorrelationData;
import com.future.tech.captain.config.CaptainConfig;
import com.future.tech.captain.domain.MessageWrapper;
import com.future.tech.captain.domain.MessageWrapperIdentity;
import com.future.tech.captain.factory.MessageWrapperFactory;
import com.future.tech.captain.mq.MessageSender;
import com.future.tech.captain.repository.MessageRepository;
import com.future.tech.captain.service.ReliableMessageService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Title: ReliableMessageServiceImpl.java<br>
 * Description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: FutureTech<br>
 * 
 * @author weilai May 19, 2017
 */
@Service
@Slf4j
public class ReliableMessageServiceImpl implements ReliableMessageService {

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private MessageWrapperFactory messageWrapperFactory;

	@Autowired
	private CaptainConfig config;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.future.tech.captain.service.ReliableMessageService#prepare(com.future
	 * .tech.captain.api.CorrelationData, java.lang.Object)
	 */
	@Override
	public boolean prepare(CorrelationData correlationData, Object message) {
		if ( correlationData==null || StringUtils.isEmpty(correlationData.getId()) ) {
			log.error("correlationData.id must not be empty!");
			return false;
		}
		if (config.findMessageSender(correlationData.getMessageSenderName()) == null) {
			log.error("Can not find message sender, sender name = " + correlationData.getMessageSenderName());
			return false;
		}
		if (config.findMessageConfirmChecker(correlationData.getMessageConfirmCheckerName()) == null) {
			log.error("Can not find message confirm checker, checker name = "
					+ correlationData.getMessageConfirmCheckerName());
			return false;
		}
		try {
			MessageWrapper messageWrapper = messageWrapperFactory.make(config.getAppName(), correlationData, message);
			messageWrapper = messageRepository.store(messageWrapper);
			if (messageWrapper != null) {
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.future.tech.captain.service.ReliableMessageService#confirm(com.future
	 * .tech.captain.api.CorrelationData)
	 */
	@Override
	public void confirm(CorrelationData correlationData) {
		MessageWrapperIdentity messageWrapperIdentity = new MessageWrapperIdentity(config.getAppName(),
				correlationData.getId());
		MessageWrapper messageWrapper = messageRepository.loadMessage(messageWrapperIdentity);
		if (messageWrapper != null && messageWrapper.isReady2Confirm()) {
			MessageSender messageSender = config.findMessageSender(messageWrapper.getMessageSenderName());
			if (messageSender == null) {
				// if can not find message sender, do nothing here
				return;
			}
			if (messageSender.send(messageWrapper.getId(), messageWrapper.getMessage())
					&& messageSender.isSynConfirm()) {
				if (messageWrapper.confirm()) {
					messageRepository.store(messageWrapper);
				}
			}
		} else {
			log.error("Confirm ERROR!, msgWrapper = " + messageWrapper);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.future.tech.captain.service.ReliableMessageService#cancel(com.future.
	 * tech.captain.api.CorrelationData)
	 */
	@Override
	public void cancel(CorrelationData correlationData) {
		MessageWrapperIdentity messageWrapperIdentity = new MessageWrapperIdentity(config.getAppName(),
				correlationData.getId());
		MessageWrapper messageWrapper = messageRepository.loadMessage(messageWrapperIdentity);
		if (messageWrapper != null && messageWrapper.cancel()) {
			messageRepository.store(messageWrapper);
		} else {
			log.error("Cancel ERROR!, msgWrapper = " + messageWrapper);
		}
	}
}
