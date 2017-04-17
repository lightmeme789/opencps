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

package org.opencps.notificationmgt.portlet;

import java.io.IOException;
import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.opencps.notificationmgt.NoSuchNotificationEventConfigException;
import org.opencps.notificationmgt.model.NotificationEventConfig;
import org.opencps.notificationmgt.model.NotificationStatusConfig;
import org.opencps.notificationmgt.search.NotificationEventConfigDisplayTerms;
import org.opencps.notificationmgt.search.NotificationStatusConfigDisplayTerms;
import org.opencps.notificationmgt.service.NotificationEventConfigLocalServiceUtil;
import org.opencps.notificationmgt.service.NotificationStatusConfigLocalServiceUtil;
import org.opencps.util.MessageKeys;
import org.opencps.util.WebKeys;

import com.liferay.counter.service.CounterLocalServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.WindowStateFactory;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * @author nhanhoang
 */
public class NotificationPortlet extends MVCPortlet {

	private static Log _log = LogFactoryUtil.getLog(NotificationPortlet.class);

	public void updateNotificationStatusConfig(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException,
			WindowStateException {

		String dossierNextStatus = ParamUtil.getString(actionRequest,
				NotificationStatusConfigDisplayTerms.DOSSIER_NEXT_STATUS);
		boolean active = ParamUtil.getBoolean(actionRequest,
				NotificationStatusConfigDisplayTerms.ACTIVE,
				false);


		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		NotificationStatusConfig notificationConfig = null;

		try {

			try {

				notificationConfig = NotificationStatusConfigLocalServiceUtil
						.getByDossierNextStatus(dossierNextStatus);
			} catch (Exception e) {

			}

			if (Validator.isNull(notificationConfig)) {

				notificationConfig = NotificationStatusConfigLocalServiceUtil
						.createNotificationStatusConfig(CounterLocalServiceUtil
								.increment(NotificationStatusConfig.class
										.getName()));

				notificationConfig.setCompanyId(themeDisplay.getCompanyId());
				notificationConfig.setGroupId(themeDisplay.getScopeGroupId());
				notificationConfig.setUserId(themeDisplay.getUserId());
				notificationConfig.setCreateDate(new Date());
				notificationConfig.setModifiedDate(new Date());

				notificationConfig.setDossierNextStatus(dossierNextStatus);
				notificationConfig.setActive(active);

				NotificationStatusConfigLocalServiceUtil
						.addNotificationStatusConfig(notificationConfig);
				SessionMessages.add(actionRequest,
						MessageKeys.NOTIFICATION_STATUS_ADD_SUCESS);

			} else {

				notificationConfig.setCompanyId(themeDisplay.getCompanyId());
				notificationConfig.setGroupId(themeDisplay.getScopeGroupId());
				notificationConfig.setUserId(themeDisplay.getUserId());
				notificationConfig.setModifiedDate(new Date());

				notificationConfig.setDossierNextStatus(dossierNextStatus);
				notificationConfig.setActive(active);

				NotificationStatusConfigLocalServiceUtil
						.updateNotificationStatusConfig(notificationConfig);

				SessionMessages.add(actionRequest,
						MessageKeys.NOTIFICATION_STATUS_UPDATE_SUCESS);

			}

		} catch (Exception e) {
			_log.error(e);
			SessionMessages.add(actionRequest,
					MessageKeys.NOTIFICATION_STATUS_SYSTEM_EXCEPTION_OCCURRED);
		} finally {

			PortletURL redirectURL = PortletURLFactoryUtil.create(
					PortalUtil.getHttpServletRequest(actionRequest),
					(String) actionRequest.getAttribute(WebKeys.PORTLET_ID),
					themeDisplay.getLayout().getPlid(),
					PortletRequest.RENDER_PHASE);
			redirectURL.setParameter(
					NotificationStatusConfigDisplayTerms.DOSSIER_NEXT_STATUS,
					String.valueOf(notificationConfig.getDossierNextStatus()));
			WindowState state = WindowStateFactory
					.getWindowState(LiferayWindowState.POP_UP.toString());
			redirectURL.setWindowState(state);

			redirectURL
					.setParameter(
							"mvcPath",
							"/html/portlets/notificationmgt/backoffice/status/notification_status_config_edit.jsp");
			actionResponse.sendRedirect(redirectURL.toString());

		}

	}

	public void changeNotificationStatusConfig(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		String dossierNextStatus = ParamUtil.getString(actionRequest,
				NotificationStatusConfigDisplayTerms.DOSSIER_NEXT_STATUS);

		boolean active = ParamUtil.getBoolean(actionRequest,
				NotificationStatusConfigDisplayTerms.ACTIVE,
				false);
		
		String redirectUrl = ParamUtil.getString(actionRequest,
				WebKeys.REDIRECT_URL, StringPool.BLANK);

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		NotificationStatusConfig notificationConfig = null;

		try {

			try {
				notificationConfig = NotificationStatusConfigLocalServiceUtil
						.getByDossierNextStatus(dossierNextStatus);

			} catch (Exception e) {

			}

			if (Validator.isNotNull(notificationConfig)) {

				notificationConfig.setCompanyId(themeDisplay.getCompanyId());
				notificationConfig.setGroupId(themeDisplay.getScopeGroupId());
				notificationConfig.setUserId(themeDisplay.getUserId());
				notificationConfig.setModifiedDate(new Date());

				notificationConfig.setActive(active);

				NotificationStatusConfigLocalServiceUtil
						.updateNotificationStatusConfig(notificationConfig);

				SessionMessages.add(actionRequest,
						MessageKeys.NOTIFICATION_STATUS_UPDATE_SUCESS);

			}

		} catch (Exception e) {
			_log.error(e);
			SessionMessages.add(actionRequest,
					MessageKeys.NOTIFICATION_STATUS_SYSTEM_EXCEPTION_OCCURRED);
		}finally{
			actionResponse.sendRedirect(redirectUrl);
		}

	}

	public void changeNotiEventConfig(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException, WindowStateException {

		long notiEventConfigId = ParamUtil.getLong(actionRequest,
				NotificationEventConfigDisplayTerms.NOTICE_EVENT_CONFIG_ID);

		boolean active = ParamUtil.getBoolean(actionRequest,
				NotificationEventConfigDisplayTerms.ACTIVE, false);
		
		String redirectUrl = ParamUtil.getString(actionRequest,
				WebKeys.REDIRECT_URL, StringPool.BLANK);

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		NotificationEventConfig notiEventConfig = null;

		try {

			try {
				notiEventConfig = NotificationEventConfigLocalServiceUtil
						.getNotificationEventConfig(notiEventConfigId);

			} catch (NoSuchNotificationEventConfigException e) {

			}

			if (Validator.isNotNull(notiEventConfig)) {

				notiEventConfig.setCompanyId(themeDisplay.getCompanyId());
				notiEventConfig.setGroupId(themeDisplay.getScopeGroupId());
				notiEventConfig.setUserId(themeDisplay.getUserId());
				notiEventConfig.setModifiedDate(new Date());

				notiEventConfig.setActive(active);

				NotificationEventConfigLocalServiceUtil
						.updateNotificationEventConfig(notiEventConfig);

				SessionMessages.add(actionRequest,
						MessageKeys.NOTIFICATION_EVENT_UPDATE_SUCESS);

			}
			
			

		} catch (Exception e) {
			_log.error(e);
			SessionMessages.add(actionRequest,
					MessageKeys.NOTIFICATION_EVENT_SYSTEM_EXCEPTION_OCCURRED);
		} finally{
			
			actionResponse.sendRedirect(redirectUrl);
		}

	}

	public void updateNotificationEventConfig(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException,
			WindowStateException {

		long notiEventConfigId = ParamUtil.getLong(actionRequest,
				NotificationEventConfigDisplayTerms.NOTICE_EVENT_CONFIG_ID);

		long notiStatusConfigId = ParamUtil.getLong(actionRequest,
				NotificationStatusConfigDisplayTerms.NOTICE_CONFIG_ID);

		boolean active = ParamUtil.getBoolean(actionRequest,
				NotificationEventConfigDisplayTerms.ACTIVE, false);

		String eventName = ParamUtil.getString(actionRequest,
				NotificationEventConfigDisplayTerms.EVENT_NAME);
		String description = ParamUtil.getString(actionRequest,
				NotificationEventConfigDisplayTerms.DESCRIPTION);
		String pattern = ParamUtil.getString(actionRequest,
				NotificationEventConfigDisplayTerms.PATTERN);
		
		String[] userExceptionList = ParamUtil.getParameterValues(actionRequest, NotificationEventConfigDisplayTerms.USER_EXCEPT_LIST);
		
		String plId = ParamUtil.getString(actionRequest, NotificationEventConfigDisplayTerms.NOTICE_REDIRECT_CONFIG_ID);

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		NotificationEventConfig notiEventConfig = null;

		try {

			try {
				notiEventConfig = NotificationEventConfigLocalServiceUtil
						.getNotificationEventConfig(notiEventConfigId);

			} catch (NoSuchNotificationEventConfigException e) {

			}

			if (Validator.isNotNull(notiEventConfig)) {

				notiEventConfig.setCompanyId(themeDisplay.getCompanyId());
				notiEventConfig.setGroupId(themeDisplay.getScopeGroupId());
				notiEventConfig.setUserId(themeDisplay.getUserId());
				notiEventConfig.setModifiedDate(new Date());

				notiEventConfig.setNotiStatusConfigId(notiStatusConfigId);
				notiEventConfig.setEventName(eventName);
				notiEventConfig.setDescription(description);
				notiEventConfig.setActive(active);
				notiEventConfig.setPattern(pattern.toUpperCase());
				notiEventConfig.setPlId(plId);
				notiEventConfig.setUserExcept(String.valueOf(StringUtil.merge(userExceptionList)));

				NotificationEventConfigLocalServiceUtil
						.updateNotificationEventConfig(notiEventConfig);

				SessionMessages.add(actionRequest,
						MessageKeys.NOTIFICATION_EVENT_UPDATE_SUCESS);

			} else {

				notiEventConfig = NotificationEventConfigLocalServiceUtil
						.createNotificationEventConfig(CounterLocalServiceUtil
								.increment(NotificationEventConfig.class
										.getName()));

				notiEventConfig.setCompanyId(themeDisplay.getCompanyId());
				notiEventConfig.setGroupId(themeDisplay.getScopeGroupId());
				notiEventConfig.setUserId(themeDisplay.getUserId());
				notiEventConfig.setCreateDate(new Date());
				notiEventConfig.setModifiedDate(new Date());

				notiEventConfig.setNotiStatusConfigId(notiStatusConfigId);
				notiEventConfig.setEventName(eventName);
				notiEventConfig.setDescription(description);
				notiEventConfig.setActive(active);
				notiEventConfig.setPattern(pattern.toUpperCase());
				notiEventConfig.setPlId(plId);
				notiEventConfig.setUserExcept(String.valueOf(StringUtil.merge(userExceptionList)));

				NotificationEventConfigLocalServiceUtil
						.addNotificationEventConfig(notiEventConfig);

				SessionMessages.add(actionRequest,
						MessageKeys.NOTIFICATION_EVENT_ADD_SUCESS);
			}
		} catch (Exception e) {
			_log.error(e);
		} finally {

			PortletURL redirectURL = PortletURLFactoryUtil.create(
					PortalUtil.getHttpServletRequest(actionRequest),
					(String) actionRequest.getAttribute(WebKeys.PORTLET_ID),
					themeDisplay.getLayout().getPlid(),
					PortletRequest.RENDER_PHASE);
			redirectURL.setParameter(
					NotificationEventConfigDisplayTerms.NOTICE_EVENT_CONFIG_ID,
					String.valueOf(notiEventConfig.getNotiEventConfigId()));
			WindowState state = WindowStateFactory
					.getWindowState(LiferayWindowState.POP_UP.toString());
			redirectURL.setWindowState(state);

			redirectURL
					.setParameter(
							"mvcPath",
							"/html/portlets/notificationmgt/backoffice/event/notification_event_config_edit.jsp");
			actionResponse.sendRedirect(redirectURL.toString());

		}

	}

}
