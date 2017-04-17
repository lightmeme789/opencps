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

package org.opencps.notificationmgt.engine;

import javax.portlet.WindowState;

import org.opencps.dossiermgt.search.DossierDisplayTerms;
import org.opencps.notificationmgt.utils.PortletKeys;
import org.opencps.paymentmgt.search.PaymentFileDisplayTerms;
import org.opencps.processmgt.search.ProcessOrderDisplayTerms;
import org.opencps.util.WebKeys;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.notifications.BaseUserNotificationHandler;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.UserNotificationEvent;
import com.liferay.portal.service.ServiceContext;

/**
 * @author nhanhoang
 */
public class UserNotificationHandler extends BaseUserNotificationHandler {

	private static Log _log = LogFactoryUtil
			.getLog(UserNotificationHandler.class);

	public static final String PORTLET_ID = "2_WAR_notificationsportlet";

	public UserNotificationHandler() {

		setPortletId(UserNotificationHandler.PORTLET_ID);

	}

	@Override
	protected String getBody(UserNotificationEvent userNotificationEvent,
			ServiceContext serviceContext) throws Exception {

		JSONObject jsonObject = JSONFactoryUtil
				.createJSONObject(userNotificationEvent.getPayload());

		String title = jsonObject.getString("title");
		String bodyText = jsonObject.getString("notificationText");

		String body = StringUtil.replace(getBodyTemplate(), new String[] {
				"[$TITLE$]", "[$BODY_TEXT$]" },
				new String[] { title, bodyText });

		return body;
	}

	protected String getBodyTemplate() throws Exception {

		StringBundler sb = new StringBundler(5);
		sb.append("<div class=\"title\">[$TITLE$]</div> ");
		sb.append("<div class=\"body\">[$BODY_TEXT$]</div>");
		return sb.toString();
	}

	@Override
	protected String getLink(UserNotificationEvent userNotificationEvent,
			ServiceContext serviceContext) throws Exception {

		String pattern = StringPool.BLANK;
		long processOrderId = 0;
		long dossierId = 0;
		long paymentFileId = 0;

		LiferayPortletResponse liferayPortletResponse = serviceContext
				.getLiferayPortletResponse();

		JSONObject jsonObject = JSONFactoryUtil
				.createJSONObject(userNotificationEvent.getPayload());

		dossierId = jsonObject.getLong("dossierId");
		paymentFileId = jsonObject.getLong("paymentFileId");
		processOrderId = jsonObject.getLong("processOrderId");
		pattern = jsonObject.getString("patternConfig");

		long plId = jsonObject.getString("plId").trim().length() > 0 ? Long
				.parseLong(jsonObject.getString("plId")) : 0;

		LiferayPortletURL viewURL = null;
		Layout layOut = null;

		if (pattern.toUpperCase().contains(PortletKeys.EMPLOYEE) && paymentFileId <= 0
				&& processOrderId > 0) {

			viewURL = liferayPortletResponse
					.createRenderURL(WebKeys.PROCESS_ORDER_PORTLET);

			viewURL.setParameter("mvcPath", "/html/portlets/processmgt/processorder/process_order_detail.jsp");
			viewURL.setParameter(ProcessOrderDisplayTerms.PROCESS_ORDER_ID,
					String.valueOf(processOrderId));
			viewURL.setPlid(plId);
			viewURL.setWindowState(WindowState.NORMAL);

		} else if (pattern.toUpperCase().contains(PortletKeys.CITIZEN) && paymentFileId <= 0
				&& dossierId > 0) {

			viewURL = liferayPortletResponse
					.createRenderURL(WebKeys.DOSSIER_MGT_PORTLET);

			viewURL.setParameter("mvcPath", "/html/portlets/dossiermgt/frontoffice/edit_dossier.jsp");
			viewURL.setParameter(DossierDisplayTerms.DOSSIER_ID,
					String.valueOf(dossierId));
			viewURL.setParameter("isEditDossier", String.valueOf(false));
			viewURL.setPlid(plId);
			viewURL.setWindowState(WindowState.NORMAL);

		} else if (pattern.toUpperCase().contains(PortletKeys.EMPLOYEE) && paymentFileId > 0) {

			viewURL = liferayPortletResponse
					.createRenderURL(WebKeys.PAYMENT_MANAGER_PORTLET);

			viewURL.setParameter("mvcPath", "/html/portlets/paymentmgt/backoffice/backofficepaymentdetail.jsp");
			viewURL.setParameter(PaymentFileDisplayTerms.PAYMENT_FILE_ID,
					String.valueOf(paymentFileId));
			viewURL.setPlid(plId);
			viewURL.setWindowState(WindowState.NORMAL);

		} else if (pattern.toUpperCase().contains(PortletKeys.CITIZEN) && paymentFileId > 0) {

			viewURL = liferayPortletResponse
					.createRenderURL(WebKeys.PAYMENT_MGT_PORTLET);

			viewURL.setParameter("mvcPath", "/html/portlets/paymentmgt/frontoffice/frontofficepaymentdetail.jsp");
			viewURL.setParameter(PaymentFileDisplayTerms.PAYMENT_FILE_ID,
					String.valueOf(paymentFileId));
			viewURL.setPlid(plId);
			viewURL.setWindowState(WindowState.NORMAL);

		}

		return Validator.isNotNull(viewURL) ? viewURL.toString() : "";
	}
}
