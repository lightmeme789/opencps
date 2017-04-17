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

import com.liferay.portal.kernel.util.ParamUtil;

/**
 * @author nhanhoang
 */
public class NotificationStatusConfigSearchTerms extends
		NotificationStatusConfigDisplayTerms {

	public NotificationStatusConfigSearchTerms(PortletRequest portletRequest) {
		super(portletRequest);

		createDate = ParamUtil.getDate(portletRequest, CREATE_DATE,
				DateTimeUtil
						.getDateTimeFormat(DateTimeUtil._VN_DATE_TIME_FORMAT));
		modifiedDate = ParamUtil.getDate(portletRequest, MODIFIED_DATE,
				DateTimeUtil
						.getDateTimeFormat(DateTimeUtil._VN_DATE_TIME_FORMAT));
		userId = ParamUtil.getLong(portletRequest, USER_ID);
		notiStatusConfigId = ParamUtil.getLong(portletRequest, NOTICE_CONFIG_ID);
		dossierCurrentStatus = ParamUtil.getString(portletRequest,
				DOSSIER_CURRENT_STATUS);
		dossierNextStatus = ParamUtil.getString(portletRequest,
				DOSSIER_NEXT_STATUS);
		active = ParamUtil.getBoolean(portletRequest,
				ACTIVE);

	}

	protected long notiStatusConfigId;
	protected String dossierCurrentStatus;
	protected String dossierNextStatus;
	protected boolean active;
	

	protected Date createDate;
	protected Date modifiedDate;
	protected long userId;

	public long getNotiStatusConfigId() {
		return notiStatusConfigId;
	}

	public void setNotiStatusConfigId(long notiStatusConfigId) {
		this.notiStatusConfigId = notiStatusConfigId;
	}

	public String getDossierCurrentStatus() {
		return dossierCurrentStatus;
	}

	public void setDossierCurrentStatus(String dossierCurrentStatus) {
		this.dossierCurrentStatus = dossierCurrentStatus;
	}

	public String getDossierNextStatus() {
		return dossierNextStatus;
	}

	public void setDossierNextStatus(String dossierNextStatus) {
		this.dossierNextStatus = dossierNextStatus;
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
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
