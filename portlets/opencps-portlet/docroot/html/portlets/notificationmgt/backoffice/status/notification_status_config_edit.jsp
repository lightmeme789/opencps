<%
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
	 * along with this program. If not, see <http://www.gnu.org/licenses/>.
	 */
%>
<%@ include file="../../init.jsp"%>

<portlet:actionURL var="updateNofificationStatusConfigURL" name="updateNotificationStatusConfig" />

<%
	String dossierNextStatus = ParamUtil.getString(request,NotificationStatusConfigDisplayTerms.DOSSIER_NEXT_STATUS,StringPool.BLANK);

	NotificationStatusConfig notificationConfig = new NotificationStatusConfigImpl();

	if (dossierNextStatus.trim().length() > 0) {

		try {
			notificationConfig = NotificationStatusConfigLocalServiceUtil
					.getByDossierNextStatus(dossierNextStatus);
		} catch (Exception e) {
			_log.error(e);
		}
	}
	
	
	DataMgtUtils dataMgtUtils = new DataMgtUtils();
	
	List<DictItem> dictItems = dataMgtUtils.getDictItemList(themeDisplay.getScopeGroupId(), PortletPropsValues.DATAMGT_MASTERDATA_DOSSIER_STATUS);
%>

<liferay-ui:header
	title='<%=Validator.isNull(notificationConfig) ? "add-notification-config"
					: "update-notification-config"%>' />


<div class=" opencps-bound-wrapper pd20 default-box-shadow"">
	<div class="edit-form">

		<liferay-ui:error key="<%= MessageKeys.NOTIFICATION_STATUS_SYSTEM_EXCEPTION_OCCURRED%>" 
		message="<%=MessageKeys.NOTIFICATION_STATUS_SYSTEM_EXCEPTION_OCCURRED %>" />
		
		<liferay-ui:success key="<%= MessageKeys.NOTIFICATION_STATUS_ADD_SUCESS%>" 
		message="<%= MessageKeys.NOTIFICATION_STATUS_ADD_SUCESS%>"/>
		
		<liferay-ui:success key="<%= MessageKeys.NOTIFICATION_STATUS_UPDATE_SUCESS%>" 
		message="<%= MessageKeys.NOTIFICATION_STATUS_UPDATE_SUCESS%>"/>

		<aui:form action="<%=updateNofificationStatusConfigURL.toString()%>"
			method="post" name="fm">

			<aui:model-context bean="<%=notificationConfig%>"
				model="<%=NotificationStatusConfig.class%>" />
				
			<aui:fieldset>

				<aui:select name="<%=NotificationStatusConfigDisplayTerms.DOSSIER_NEXT_STATUS%>"
					label="status">
					
					<% for (DictItem dictItem: dictItems){ %>
					
					<aui:option label="<%= dictItem.getItemName(themeDisplay.getLocale()) %>" 
								selected="<%=Validator.isNotNull(notificationConfig)? dictItem.getItemCode().equals(notificationConfig.getDossierNextStatus()):false%>" 
								value="<%= dictItem.getItemCode() %>" />
					<%} %>
				</aui:select>
				
				<aui:input name="<%=NotificationStatusConfigDisplayTerms.ACTIVE%>" 
							type="checkbox" 
							label="inuse" 
							value="<%=Validator.isNotNull(notificationConfig) ?notificationConfig.getActive():false %>"></aui:input>
				
			</aui:fieldset>

			<aui:fieldset>
				<aui:button type="submit" name="submit" icon="icon-save" />
			</aui:fieldset>
		</aui:form>
	</div>
</div>

<%!private Log _log = LogFactoryUtil
			.getLog("html.portlets.notificationmgt.backoffice.notification_status_config_edit");%>
