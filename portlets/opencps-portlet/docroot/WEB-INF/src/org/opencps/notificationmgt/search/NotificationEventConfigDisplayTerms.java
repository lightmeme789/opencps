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

import java.util.Date;

import javax.portlet.PortletRequest;

import org.opencps.util.DateTimeUtil;

import com.liferay.portal.kernel.dao.search.DisplayTerms;
import com.liferay.portal.kernel.util.ParamUtil;

/**
 * @author nhanhoang
 */
public class NotificationEventConfigDisplayTerms extends DisplayTerms {
	
	public static final String NOTICE_EVENT_CONFIG_ID = "notiEventConfigId";

	public static final String NOTICE_STATUS_CONFIG_ID = "notiStatusConfigId";

	public static final String EVENT_NAME = "eventName";

	public static final String DESCRIPTION = "description";

	public static final String PATTERN = "pattern";

	public static final String ACTIVE = "active";

	public static final String CREATE_DATE = "createDate";

	public static final String MODIFIED_DATE = "modifiedDate";

	public static final String USER_ID = "userId";
	
	public static final String NOTICE_REDIRECT_CONFIG_ID = "notiRedirectConfigId";
	
	public static final String PLID = "plId";
	
	public static final String JSP_REDIRECT = "jspRedirect";
	
	public static final String USER_EXCEPT_LIST = "userExceptListValues";

	public NotificationEventConfigDisplayTerms(PortletRequest portletRequest) {
		super(portletRequest);

		createDate = ParamUtil.getDate(portletRequest, CREATE_DATE,
				DateTimeUtil
						.getDateTimeFormat(DateTimeUtil._VN_DATE_TIME_FORMAT));
		modifiedDate = ParamUtil.getDate(portletRequest, MODIFIED_DATE,
				DateTimeUtil
						.getDateTimeFormat(DateTimeUtil._VN_DATE_TIME_FORMAT));
		userId = ParamUtil.getLong(portletRequest, USER_ID);
		notiEventConfigId = ParamUtil.getLong(portletRequest,
				NOTICE_EVENT_CONFIG_ID);
		notiStatusConfigId = ParamUtil.getLong(portletRequest,
				NOTICE_STATUS_CONFIG_ID);
		eventName = ParamUtil.getString(portletRequest, EVENT_NAME);
		description = ParamUtil.getString(portletRequest, DESCRIPTION);
		pattern = ParamUtil.getString(portletRequest, PATTERN);
		active = ParamUtil.getBoolean(portletRequest, ACTIVE);
	}
	
	protected long notiEventConfigId;
	protected long notiStatusConfigId;
	protected String eventName;
	protected String description;
	protected String pattern;
	protected boolean active;
	protected Date createDate;
	protected Date modifiedDate;
	protected long userId;
	

	public long getNotiEventConfigId() {
		return notiEventConfigId;
	}

	public void setNotiEventConfigId(long notiEventConfigId) {
		this.notiEventConfigId = notiEventConfigId;
	}

	public long getNotiStatusConfigId() {
		return notiStatusConfigId;
	}

	public void setNotiStatusConfigId(long notiStatusConfigId) {
		this.notiStatusConfigId = notiStatusConfigId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

}
