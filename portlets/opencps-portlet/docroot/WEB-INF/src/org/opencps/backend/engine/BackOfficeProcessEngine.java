/**
 * OpenCPS is the open source Core Public Services software
 * Copyright (C) 2016-present OpenCPS community
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package org.opencps.backend.engine;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opencps.backend.message.SendToBackOfficeMsg;
import org.opencps.backend.message.SendToEngineMsg;
import org.opencps.backend.util.BackendUtils;
import org.opencps.backend.util.DossierNoGenerator;
import org.opencps.backend.util.PaymentRequestGenerator;
import org.opencps.backend.util.PaymentUrlGenerator;
import org.opencps.dossiermgt.model.Dossier;
import org.opencps.dossiermgt.model.DossierLog;
import org.opencps.dossiermgt.model.ServiceConfig;
import org.opencps.dossiermgt.service.DossierLogLocalServiceUtil;
import org.opencps.dossiermgt.service.ServiceConfigLocalServiceUtil;
import org.opencps.dossiermgt.util.ActorBean;
import org.opencps.holidayconfig.util.HolidayCheckUtils;
import org.opencps.notificationmgt.message.SendNotificationMessage;
import org.opencps.notificationmgt.utils.NotificationUtils;
import org.opencps.paymentmgt.model.PaymentFile;
import org.opencps.paymentmgt.model.impl.PaymentFileImpl;
import org.opencps.paymentmgt.service.PaymentFileLocalServiceUtil;
import org.opencps.processmgt.model.ProcessOrder;
import org.opencps.processmgt.model.ProcessStep;
import org.opencps.processmgt.model.ProcessWorkflow;
import org.opencps.processmgt.model.impl.ProcessStepImpl;
import org.opencps.processmgt.service.ProcessOrderLocalServiceUtil;
import org.opencps.processmgt.service.ProcessWorkflowLocalServiceUtil;
import org.opencps.processmgt.util.ProcessMgtUtil;
import org.opencps.processmgt.util.ProcessUtils;
import org.opencps.util.PortletConstants;
import org.opencps.util.WebKeys;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.messaging.MessageListenerException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author khoavd
 */
public class BackOfficeProcessEngine implements MessageListener {

	@Override
	public void receive(Message message)
		throws MessageListenerException {

		_doRecevie(message);
	}

	private void _doRecevie(Message message) {

		SendToEngineMsg toEngineMsg = (SendToEngineMsg) message.get("msgToEngine");

		List<SendNotificationMessage> lsNotification = new ArrayList<SendNotificationMessage>();

		String actionName = StringPool.BLANK;
		String stepName = StringPool.BLANK;
		ProcessOrder processOrder = null;
		long curStepId = 0;
		long processStepId = 0;

		long ownerUserId = 0;
		long ownerOrganizationId = 0;

		Dossier dossier = BackendUtils.getDossier(toEngineMsg.getDossierId());

		long serviceInfoId = 0;
		long dossierTemplateId = 0;
		String govAgencyCode = StringPool.BLANK;
		String govAgencyName = StringPool.BLANK;
		long govAgencyOrganizationId = 0;
		long serviceProcessId = 0;

		long actionUserId = toEngineMsg.getActionUserId();

		ActorBean actorBean = new ActorBean(toEngineMsg.getActorType(), actionUserId);

		if (Validator.isNotNull(dossier)) {
			serviceInfoId = dossier.getServiceInfoId();
			dossierTemplateId = dossier.getDossierTemplateId();
			govAgencyCode = dossier.getGovAgencyCode();
			govAgencyName = dossier.getGovAgencyName();
			govAgencyOrganizationId = dossier.getGovAgencyOrganizationId();

			try {

				ServiceConfig serviceConfig =
					ServiceConfigLocalServiceUtil.getServiceConfigByG_S_G(
						toEngineMsg.getGroupId(), serviceInfoId, govAgencyCode);
				serviceProcessId = serviceConfig.getServiceProcessId();

			}
			catch (Exception e) {
				_log.error(e);
			}
		}

		SendToBackOfficeMsg toBackOffice = new SendToBackOfficeMsg();

		toBackOffice.setSubmitDateTime(toEngineMsg.getActionDatetime());

		toBackOffice.setActor(actorBean.getActor());
		toBackOffice.setActorId(actorBean.getActorId());
		toBackOffice.setActorName(actorBean.getActorName());

		long processWorkflowId = toEngineMsg.getProcessWorkflowId();

		long processOrderId = toEngineMsg.getProcessOrderId();

		try {
			if (Validator.isNull(processOrderId)) {
				// Check processOrder
				processOrder =
					BackendUtils.getProcessOrder(
						toEngineMsg.getDossierId(), toEngineMsg.getFileGroupId());

				if (Validator.isNull(processOrder)) {

					// Init process order
					processOrder =
						ProcessOrderLocalServiceUtil.initProcessOrder(
							toEngineMsg.getUserId(), toEngineMsg.getCompanyId(),
							toEngineMsg.getGroupId(), serviceInfoId, dossierTemplateId,
							govAgencyCode, govAgencyName, govAgencyOrganizationId,
							serviceProcessId, toEngineMsg.getDossierId(),
							toEngineMsg.getFileGroupId(), toEngineMsg.getProcessWorkflowId(),
							new Date(), StringPool.BLANK, StringPool.BLANK,
							StringPool.BLANK, 0, 0, 0, PortletConstants.DOSSIER_STATUS_SYSTEM);

					// Add DossierLog for create ProcessOrder

					ActorBean actorBeanSys = new ActorBean(0, 0);

					DossierLog dossierLog =
						DossierLogLocalServiceUtil.addDossierLog(
							toEngineMsg.getUserId(), toEngineMsg.getGroupId(),
							toEngineMsg.getCompanyId(), toEngineMsg.getDossierId(),
							toEngineMsg.getFileGroupId(), PortletConstants.DOSSIER_STATUS_SYSTEM,
							PortletConstants.DOSSIER_ACTION_CREATE_PROCESS_ORDER,
							PortletConstants.DOSSIER_ACTION_CREATE_PROCESS_ORDER, new Date(), 0, 0,
							actorBeanSys.getActor(), actorBeanSys.getActorId(),
							actorBeanSys.getActorName(), BackOfficeProcessEngine.class.getName() +
								".createProcessOrder()", processOrder.getProcessOrderId(), 0, false);

					toBackOffice.setDossierLogOId(dossierLog.getOId());
				}

				processOrderId = processOrder.getProcessOrderId();

			}
			else {
				// Find process order by processOrderId
				processOrder = ProcessOrderLocalServiceUtil.fetchProcessOrder(processOrderId);

				processOrderId = processOrder.getProcessOrderId();

				curStepId = processOrder.getProcessStepId();
			}

			long assignToUserId = toEngineMsg.getAssignToUserId();

			ProcessWorkflow processWorkflow = null;

			// Find workflow
			if (Validator.isNull(processWorkflowId)) {

				processWorkflow =
					ProcessWorkflowLocalServiceUtil.getProcessWorkflowByEvent(
						serviceProcessId, toEngineMsg.getEvent(), curStepId);
			}
			else {

				processWorkflow =
					ProcessWorkflowLocalServiceUtil.fetchProcessWorkflow(processWorkflowId);

			}

			if (Validator.isNull(assignToUserId)) {

				assignToUserId =
					ProcessMgtUtil.getAssignUser(
						processWorkflow.getProcessWorkflowId(), processOrderId,
						processWorkflow.getPostProcessStepId());
			}

			// Do Workflow

			if (Validator.isNotNull(processWorkflow)) {

				
				actionName = processWorkflow.getActionName();

				processStepId = processWorkflow.getPostProcessStepId();

				long changeStepId = processWorkflow.getPostProcessStepId();

				ProcessStep changeStep = ProcessUtils.getPostProcessStep(changeStepId);
				
				curStepId = processWorkflow.getPreProcessStepId();

				ProcessStep currStep = new ProcessStepImpl();

				if (curStepId != 0) {
					currStep = ProcessUtils.getProcessStep(curStepId);
					stepName = currStep.getStepName();
				}
				
				toBackOffice.setCurStepId(curStepId);
				toBackOffice.setCurStepName(stepName);

				String changeStatus = StringPool.BLANK;

				boolean isResubmit = false;

				if (changeStepId != 0) {

					// Set Receive Date

					if (currStep.getDossierStatus().contains(
						PortletConstants.DOSSIER_STATUS_RECEIVING) &&
						changeStep.getDossierStatus().contains("processing")) {
						toBackOffice.setReceiveDatetime(new Date());
					}

					if (Validator.isNotNull(changeStep)) {
						changeStatus = changeStep.getDossierStatus();

						if (Validator.equals(
							changeStep.getDossierStatus(), PortletConstants.DOSSIER_STATUS_WAITING)) {

							isResubmit = true;
						}

					}
				}
				else {
					changeStatus = PortletConstants.DOSSIER_STATUS_DONE;
				}

				int syncStatus = 0;

				if (!changeStatus.contentEquals(toEngineMsg.getDossierStatus())) {
					syncStatus = 2;
				}

				// Update process order to SYSTEM
				ProcessOrderLocalServiceUtil.updateProcessOrderStatus(
					processOrderId, PortletConstants.DOSSIER_STATUS_SYSTEM);

				// Update process order
				ProcessOrderLocalServiceUtil.updateProcessOrder(
					processOrderId, processStepId, processWorkflow.getProcessWorkflowId(),
					toEngineMsg.getActionUserId(), toEngineMsg.getActionDatetime(),
					toEngineMsg.getActionNote(), assignToUserId, stepName, actionName, 0, 0,
					PortletConstants.DOSSIER_STATUS_SYSTEM);

				toBackOffice.setStepName(stepName);
				toBackOffice.setProcessWorkflowId(processWorkflow.getProcessWorkflowId());

				toBackOffice.setProcessOrderId(processOrderId);
				toBackOffice.setDossierId(toEngineMsg.getDossierId());
				toBackOffice.setFileGroupId(toEngineMsg.getFileGroupId());
				toBackOffice.setDossierStatus(changeStatus);
				toBackOffice.setSyncStatus(syncStatus);

				if (changeStatus.equals(PortletConstants.DOSSIER_STATUS_WAITING)) {
					toBackOffice.setRequestCommand(WebKeys.DOSSIER_LOG_RESUBMIT_REQUEST);
				}

				toBackOffice.setActionInfo(processWorkflow.getActionName());
				toBackOffice.setSendResult(0);

				if (changeStatus.equals(PortletConstants.DOSSIER_STATUS_PAYING)) {
					toBackOffice.setRequestPayment(1);
				}
				else {
					toBackOffice.setRequestPayment(0);
				}

				toBackOffice.setUpdateDatetime(new Date());

				if (Validator.isNull(toEngineMsg.getReceptionNo())) {
					String pattern = processWorkflow.getReceptionNoPattern();
					if (Validator.isNotNull(pattern) && StringUtil.trim(pattern).length() != 0) {

						toBackOffice.setReceptionNo(DossierNoGenerator.genaratorNoReception(
							pattern, toEngineMsg.getDossierId()));
						
						toBackOffice.setReceiveDatetime(new Date());
						// Add log create dossier

					}
					else {
						toBackOffice.setReceptionNo(dossier.getReceptionNo());
					}
				}
				else {
					toBackOffice.setReceptionNo(toEngineMsg.getReceptionNo());
				}

				if (processWorkflow.getIsFinishStep()) {
					toBackOffice.setFinishDatetime(new Date());
				}

				toBackOffice.setCompanyId(toEngineMsg.getCompanyId());
				toBackOffice.setGovAgencyCode(govAgencyCode);

				toBackOffice.setUserActorAction(toEngineMsg.getActionUserId());

				if (dossier.getOwnerOrganizationId() != 0) {
					ownerUserId = 0;
					ownerOrganizationId = dossier.getOwnerOrganizationId();
				}
				else {
					ownerUserId = dossier.getUserId();
				}

				boolean isPayment = false;

				PaymentFile paymentFile = new PaymentFileImpl();

				// Update Paying
				if (processWorkflow.getRequestPayment()) {

					int totalPayment =
						PaymentRequestGenerator.getTotalPayment(
							processWorkflow.getPaymentFee(), dossier.getDossierId());

					List<String> paymentMethods =
						PaymentRequestGenerator.getPaymentMethod(processWorkflow.getPaymentFee());

					String paymentOptions = StringUtil.merge(paymentMethods);

					List<String> paymentMessages =
						PaymentRequestGenerator.getMessagePayment(processWorkflow.getPaymentFee());

					String paymentName =
						(paymentMessages.size() != 0) ? paymentMessages.get(0) : StringPool.BLANK;

					paymentFile =
						PaymentFileLocalServiceUtil.addPaymentFile(
							toEngineMsg.getDossierId(), toEngineMsg.getFileGroupId(), ownerUserId,
							ownerOrganizationId, govAgencyOrganizationId, paymentName, new Date(),
							(double) totalPayment, paymentName, StringPool.BLANK, paymentOptions);

					if (paymentMethods.contains(PaymentRequestGenerator.PAY_METHOD_KEYPAY)) {

						paymentFile =
								PaymentUrlGenerator.generatorPayURL(
								processWorkflow.getGroupId(), govAgencyOrganizationId,
								paymentFile.getPaymentFileId(), processWorkflow.getPaymentFee(),
								toEngineMsg.getDossierId());

					}

					isPayment = true;

					toBackOffice.setRequestCommand(WebKeys.DOSSIER_LOG_PAYMENT_REQUEST);
					toBackOffice.setPaymentFile(paymentFile);

					Locale vnLocale = new Locale("vi", "VN");

					NumberFormat vnFormat = NumberFormat.getCurrencyInstance(vnLocale);

					// setPayment message in pattern in message Info

					StringBuffer sb = new StringBuffer();

					sb.append(paymentMessages.get(0));
					sb.append(StringPool.SPACE);
					sb.append(StringPool.OPEN_PARENTHESIS);
					sb.append(vnFormat.format(totalPayment));
					sb.append(StringPool.CLOSE_PARENTHESIS);
					sb.append(StringPool.SEMICOLON);
					sb.append(toEngineMsg.getActionNote());

					toBackOffice.setMessageInfo(sb.toString());

				}
				else {
					toBackOffice.setRequestPayment(0);

					toBackOffice.setMessageInfo(toEngineMsg.getActionNote());
				}

				toBackOffice.setPayment(isPayment);
				toBackOffice.setResubmit(isResubmit);
				
				if(Validator.isNotNull(toEngineMsg.getEstimateDatetime())){
					
					toBackOffice.setEstimateDatetime(toEngineMsg.getEstimateDatetime());
					
				}else{
					
					Date estimateDate = null;
					
					Date receiveDate =null;
					
					receiveDate = toBackOffice.getReceiveDatetime();
					
					String deadlinePattern = processWorkflow.getDeadlinePattern();
					
					
					if(processWorkflow != null && processWorkflow.getGenerateDeadline() && Validator.isNotNull(receiveDate) && Validator.isNotNull(deadlinePattern)){
						
						estimateDate = HolidayCheckUtils.getEndDate(receiveDate, deadlinePattern);
						
						toBackOffice.setEstimateDatetime(estimateDate);
					}
					
				}
				

				long preProcessStepId = -1;
				String autoEvent = StringPool.BLANK;
				Date estimateDatetime = null;

				preProcessStepId = processWorkflow.getPreProcessStepId();
				autoEvent = processWorkflow.getAutoEvent();

				

				if (preProcessStepId == 0 &&
					autoEvent.equals(WebKeys.AUTO_EVENT_SUBMIT) &&
					processWorkflow.getGenerateDeadline() &&
					changeStep.getDossierStatus().contains(
						PortletConstants.DOSSIER_STATUS_RECEIVING) &&
					currStep.getDossierStatus().equals(StringPool.BLANK)) {

					estimateDatetime =
						HolidayCheckUtils.getEndDate(new Date(), processWorkflow.getDeadlinePattern());

					toBackOffice.setEstimateDatetime(estimateDatetime);

				}
				
				lsNotification = NotificationUtils.sendNotification(
						processWorkflow.getProcessWorkflowId(),
						Validator.isNotNull(dossier) ? dossier.getDossierId()
								: 0,
						Validator.isNotNull(paymentFile) ? paymentFile
								.getPaymentFileId() : 0, processOrderId);

				toBackOffice.setListNotifications(lsNotification);

				Message sendToBackOffice = new Message();

				sendToBackOffice.put("toBackOffice", toBackOffice);

				MessageBusUtil.sendMessage("opencps/backoffice/out/destination", sendToBackOffice);

			}
			else {
				// Send message to backoffice/out/destination
				toBackOffice.setProcessOrderId(processOrderId);
				toBackOffice.setDossierId(toEngineMsg.getDossierId());
				toBackOffice.setFileGroupId(toEngineMsg.getFileGroupId());
				toBackOffice.setDossierStatus(PortletConstants.DOSSIER_STATUS_ERROR);
				toBackOffice.setCompanyId(toEngineMsg.getCompanyId());
				toBackOffice.setGovAgencyCode(govAgencyCode);
				toBackOffice.setReceptionNo(toEngineMsg.getReceptionNo());
				toBackOffice.setUserActorAction(toEngineMsg.getActionUserId());
				toBackOffice.setStepName(stepName);

				Message sendToBackOffice = new Message();

				sendToBackOffice.put("toBackOffice", toBackOffice);

				MessageBusUtil.sendMessage("opencps/backoffice/out/destination", sendToBackOffice);

			}

		}
		catch (Exception e) {
			_log.error(e);
		}
	}



	private Log _log = LogFactoryUtil.getLog(BackOfficeProcessEngine.class);

}