/**
 * OpenCPS is the open source Core Public Services software
 * Copyright (C) 2016-present OpenCPS community

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package org.opencps.util;

import java.util.Date;

import javax.portlet.PortletPreferences;

import org.opencps.accountmgt.model.Business;
import org.opencps.accountmgt.model.Citizen;
import org.opencps.dossiermgt.model.Dossier;
import org.opencps.dossiermgt.model.impl.DossierImpl;
import org.opencps.dossiermgt.service.DossierLocalServiceUtil;
import org.opencps.notificationmgt.message.SendNotificationMessage;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.Ticket;
import com.liferay.portal.model.TicketConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.TicketLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.SubscriptionSender;
import com.liferay.util.PwdGenerator;

/**
 * @author trungnt
 */
public class MessageBusUtil {

	public static void sendEmailAddressVerification(String uuid, User user,
			String emailAddress, String type, String emailConfigStep,
			String emailConfirmToAdmin, ServiceContext serviceContext)
			throws PortalException, SystemException {
		if (user.isEmailAddressVerified()
				&& StringUtil.equalsIgnoreCase(emailAddress,
						user.getEmailAddress())) {

			return;
		}

		Ticket ticket = TicketLocalServiceUtil.addDistinctTicket(
				user.getCompanyId(), User.class.getName(), user.getUserId(),
				TicketConstants.TYPE_EMAIL_ADDRESS, emailAddress, null,
				serviceContext);

		String verifyEmailAddressURL = serviceContext.getPortalURL()
				+ "/opencps-portlet/verify/email?token=" + uuid + "&ticketKey="
				+ ticket.getKey() + "&type=" + type + "&emailConfigStep="
				+ emailConfigStep + "&emailConfirmToAdmin="
				+ emailConfirmToAdmin;

		long plid = serviceContext.getPlid();

		if (plid > 0) {
			Layout layout = LayoutLocalServiceUtil.fetchLayout(plid);

			if (layout != null) {
				Group group = layout.getGroup();

				if (!layout.isPrivateLayout() && !group.isUser()) {
					verifyEmailAddressURL += "&p_l_id="
							+ serviceContext.getPlid();
				}
			}
		}

		String fromName = PrefsPropsUtil.getString(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_FROM_NAME);
		String fromAddress = PrefsPropsUtil.getString(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

		String toName = user.getFullName();
		String toAddress = emailAddress;

		String subject = PrefsPropsUtil.getContent(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_VERIFICATION_SUBJECT);

		String body = PrefsPropsUtil.getContent(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_VERIFICATION_BODY);

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		subscriptionSender.setBody(body);
		subscriptionSender.setCompanyId(user.getCompanyId());
		subscriptionSender.setContextAttributes("[$EMAIL_VERIFICATION_CODE$]",
				ticket.getKey(), "[$EMAIL_VERIFICATION_URL$]",
				verifyEmailAddressURL, "[$REMOTE_ADDRESS$]",
				serviceContext.getRemoteAddr(), "[$REMOTE_HOST$]",
				serviceContext.getRemoteHost(), "[$USER_ID$]",
				user.getUserId(), "[$USER_SCREENNAME$]", user.getScreenName(),
				"[$HO_TEN_NGUOI_DK$]", user.getFullName());
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setMailId("user", user.getUserId(),
				System.currentTimeMillis(), PwdGenerator.getPassword());
		subscriptionSender.setServiceContext(serviceContext);
		subscriptionSender.setSubject(subject);
		subscriptionSender.setUserId(user.getUserId());

		subscriptionSender.addRuntimeSubscribers(toAddress, null);

		subscriptionSender.flushNotificationsAsync();
	}

	public static void sendEmailActiveAccount(User user, String password,
			ServiceContext serviceContext) throws SystemException {

		if (!PrefsPropsUtil.getBoolean(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_USER_ADDED_ENABLED)) {

			return;
		}

		String fromName = PrefsPropsUtil.getString(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_FROM_NAME);
		String fromAddress = PrefsPropsUtil.getString(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

		String toName = user.getFullName();
		String toAddress = user.getEmailAddress();

		String subject = PrefsPropsUtil.getContent(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_USER_ADDED_SUBJECT);

		String body = null;

		if (Validator.isNotNull(password)) {
			body = PrefsPropsUtil.getContent(user.getCompanyId(),
					PropsKeys.ADMIN_EMAIL_USER_ADDED_BODY);
		} else {
			body = PrefsPropsUtil.getContent(user.getCompanyId(),
					PropsKeys.ADMIN_EMAIL_USER_ADDED_NO_PASSWORD_BODY);
		}

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		subscriptionSender.setBody(body);
		subscriptionSender.setCompanyId(user.getCompanyId());
		subscriptionSender.setContextAttributes("[$USER_ID$]",
				user.getUserId(), "[$USER_PASSWORD$]", password,
				"[$USER_SCREENNAME$]", user.getScreenName());
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setMailId("user", user.getUserId(),
				System.currentTimeMillis(), PwdGenerator.getPassword());
		subscriptionSender.setServiceContext(serviceContext);
		subscriptionSender.setSubject(subject);
		subscriptionSender.setUserId(user.getUserId());

		subscriptionSender.addRuntimeSubscribers(toAddress, null);

		subscriptionSender.flushNotificationsAsync();
	}

	public static void sendEmailWelcomeNewUser(User user,
			ServiceContext serviceContext) throws SystemException {

		String fromName = PrefsPropsUtil.getString(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_FROM_NAME);
		String fromAddress = PrefsPropsUtil.getString(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

		String toName = user.getFullName();
		String toAddress = user.getEmailAddress();

		String subject = PrefsPropsUtil.getContent(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_USER_ADDED_SUBJECT);

		PortletPreferences preferences = PrefsPropsUtil.getPreferences(
				serviceContext.getCompanyId(), true);

		/*
		 * String emailWelcomeSubject = GetterUtil .getString(preferences
		 * .getValue("WELCOME_NEW_USER_SUBJECT", StringPool.BLANK));
		 */
		String emailWelcomeBody = GetterUtil.getString(preferences.getValue(
				"WELCOME_NEW_USER_BODY", StringPool.BLANK));

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		subscriptionSender.setBody(emailWelcomeBody);
		subscriptionSender.setCompanyId(user.getCompanyId());

		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setMailId("user", user.getUserId(),
				System.currentTimeMillis(), PwdGenerator.getPassword());
		subscriptionSender.setServiceContext(serviceContext);
		subscriptionSender.setSubject(subject);
		subscriptionSender.setUserId(user.getUserId());

		subscriptionSender.addRuntimeSubscribers(toAddress, null);

		subscriptionSender.flushNotificationsAsync();
	}

	public static void sendEmailConfirmToAdmin(String uuid, User user,
			String emailAddress, String emailConfirmToAdmin, Business business,
			Citizen citizen, ServiceContext serviceContext)
			throws PortalException, SystemException {

		System.out
				.println("EMAIL ADMIN --------------**************************"
						+ emailConfirmToAdmin);

		String emailAdmins[] = null;

		String telNo = StringPool.BLANK;

		Date createDate = new Date();

		if (Validator.isNotNull(business)) {
			createDate = business.getCreateDate();
			telNo = business.getTelNo();
		}

		if (Validator.isNotNull(citizen)) {
			createDate = citizen.getCreateDate();
			telNo = citizen.getTelNo();
		}

		if (Validator.isNotNull(emailConfirmToAdmin)) {

			emailAdmins = emailConfirmToAdmin.split(";");

			for (int i = 0; i < emailAdmins.length; i++) {

				if (Validator.isEmailAddress(emailAdmins[i])) {

					String fromName = PrefsPropsUtil.getString(
							user.getCompanyId(),
							PropsKeys.ADMIN_EMAIL_FROM_NAME);

					String fromAddress = PrefsPropsUtil.getString(
							user.getCompanyId(),
							PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

					String toName = user.getFullName();

					String toAddress = emailAdmins[i];

					PortletPreferences preferences = PrefsPropsUtil
							.getPreferences(serviceContext.getCompanyId(), true);

					String subject = GetterUtil
							.getString(preferences.getValue(
									"EMAIL_CONFIRM_TO_ADMIN_SUBJECT",
									StringPool.BLANK));

					String body = GetterUtil.getString(preferences.getValue(
							"EMAIL_CONFIRM_TO_ADMIN_BODY", StringPool.BLANK));

					SubscriptionSender subscriptionSender = new SubscriptionSender();

					subscriptionSender.setBody(body);

					subscriptionSender.setCompanyId(user.getCompanyId());

					subscriptionSender.setContextAttributes(
							"[$TIME_REGISTER$]", createDate,
							"[$EMAIL_REGISTER$]", emailAddress,
							"[$PHONE_NUMBER$]", telNo, "[$HO_TEN_NGUOI_DK$]",
							toName, "[$USER_ID$]", user.getUserId());

					subscriptionSender.setFrom(fromAddress, fromName);

					subscriptionSender.setMailId("user", user.getUserId(),
							System.currentTimeMillis(),
							PwdGenerator.getPassword());

					subscriptionSender.setHtmlFormat(true);

					subscriptionSender.setServiceContext(serviceContext);

					subscriptionSender.setSubject(subject);

					subscriptionSender.setUserId(user.getUserId());

					subscriptionSender.addRuntimeSubscribers(toAddress, null);

					subscriptionSender.flushNotificationsAsync();
				}
			}
		}
	}

	public static void sendEmailNotification(SendNotificationMessage message,
			SendNotificationMessage.Infomations.Infomation info,
			ServiceContext serviceContext) throws PortalException,
			SystemException {

		User user = UserLocalServiceUtil
				.getUser(Long.valueOf(info.getUserId()));

		String fromName = PrefsPropsUtil.getString(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_FROM_NAME);
		String fromAddress = PrefsPropsUtil.getString(user.getCompanyId(),
				PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);

		String toAddress = user.getEmailAddress();

		PortletPreferences preferences = PrefsPropsUtil.getPreferences(
				serviceContext.getCompanyId(), true);

		String subject = GetterUtil.getString(preferences.getValue(
				"EMAIL_NOTIFICATION_SUBJECT", StringPool.BLANK));

		String body = GetterUtil.getString(preferences.getValue(
				"EMAIL_NOTIFICATION_BODY", StringPool.BLANK));

		long dossierId = Long.valueOf(message.getDossierId());

		Dossier dossier = new DossierImpl();

		if (dossierId > 0) {
			dossier = DossierLocalServiceUtil.getDossier(dossierId);
		}

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setSubject(subject);
		subscriptionSender.setBody(body);
		subscriptionSender.setContextAttributes("[$TO_USER_FULLNAME$]", info.getFullName(),
				"[$DOSSIER_ID$]", message.getDossierId(), "[$RECEPTION_NO$]",
				dossier.getReceptionNo(), "[$TO_CONTENT_NOTICE$]",
				message.getEventName());

		subscriptionSender.setUserId(user.getUserId());
		subscriptionSender.setMailId("user", user.getUserId(),
				System.currentTimeMillis(), PwdGenerator.getPassword());
		subscriptionSender.setCompanyId(user.getCompanyId());

		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setServiceContext(serviceContext);
		subscriptionSender.addRuntimeSubscribers(toAddress, null);
		subscriptionSender.flushNotificationsAsync();
	}

	public static final String EMAIL_NOTIFICATION_SUBJECT = "EMAIL_NOTIFICATION_SUBJECT";
}
