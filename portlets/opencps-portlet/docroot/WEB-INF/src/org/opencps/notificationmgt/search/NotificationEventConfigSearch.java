/*******************************************************************************
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package org.opencps.notificationmgt.search;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import org.opencps.datamgt.model.DictItem;
import org.opencps.util.DateTimeUtil;

import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * @author nhanhoang
 *
 */
public class NotificationEventConfigSearch extends SearchContainer<DictItem> {
	static List<String> headerNames = new ArrayList<String>();
	static {
		headerNames.add("row-index");
		headerNames.add("status");
		headerNames.add("event-name");
		headerNames.add("description");
		headerNames.add("noti-types");
		headerNames.add("create-date");
		headerNames.add("modified-date");
		headerNames.add("action");

	}

	public static final String EMPTY_RESULTS_MESSAGE = "no-notification-event-config-where-found";

	public NotificationEventConfigSearch(PortletRequest portletRequest, int delta,
			PortletURL iteratorURL) {

		super(portletRequest,
				new NotificationEventConfigDisplayTerms(portletRequest),
				new NotificationEventConfigSearchTerms(portletRequest),
				DEFAULT_CUR_PARAM, delta, iteratorURL, headerNames,
				EMPTY_RESULTS_MESSAGE);

		NotificationEventConfigDisplayTerms displayTerms = (NotificationEventConfigDisplayTerms) getDisplayTerms();

		iteratorURL.setParameter(NotificationEventConfigDisplayTerms.CREATE_DATE,
				DateTimeUtil.convertDateToString(displayTerms.getCreateDate(),
						DateTimeUtil._VN_DATE_TIME_FORMAT));
		iteratorURL.setParameter(NotificationEventConfigDisplayTerms.MODIFIED_DATE,
				DateTimeUtil.convertDateToString(
						displayTerms.getModifiedDate(),
						DateTimeUtil._VN_DATE_TIME_FORMAT));
		iteratorURL.setParameter(NotificationEventConfigDisplayTerms.USER_ID,
				String.valueOf(displayTerms.getUserId()));

		
		iteratorURL.setParameter(
				NotificationEventConfigDisplayTerms.NOTICE_EVENT_CONFIG_ID,
				String.valueOf(displayTerms.getNotiEventConfigId()));
		iteratorURL.setParameter(
				NotificationEventConfigDisplayTerms.NOTICE_STATUS_CONFIG_ID,
				String.valueOf(displayTerms.getNotiStatusConfigId()));
		iteratorURL.setParameter(
				NotificationEventConfigDisplayTerms.EVENT_NAME,
				String.valueOf(displayTerms.getEventName()));
		iteratorURL.setParameter(
				NotificationEventConfigDisplayTerms.DESCRIPTION,
				String.valueOf(displayTerms.getDescription()));
		iteratorURL.setParameter(
				NotificationEventConfigDisplayTerms.PATTERN,
				String.valueOf(displayTerms.getPattern()));
		iteratorURL.setParameter(
				NotificationEventConfigDisplayTerms.ACTIVE,
				String.valueOf(displayTerms.isActive()));

	}

	public NotificationEventConfigSearch(PortletRequest portletRequest,
			PortletURL iteratorURL) {

		this(portletRequest, DEFAULT_DELTA, iteratorURL);
	}

	private static Log _log = LogFactoryUtil
			.getLog(NotificationEventConfigSearch.class);

}
