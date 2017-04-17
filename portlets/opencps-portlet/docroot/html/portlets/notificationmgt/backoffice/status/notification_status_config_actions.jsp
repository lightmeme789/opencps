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


<%
	ResultRow row = (ResultRow) request
			.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);
	NotificationStatusConfig notificationStatusConfig = (NotificationStatusConfig) row.getObject();
	
	String iteratorURL = row.getParameter(WebKeys.REDIRECT_URL).toString();
	
%>

<c:if
	test="<%=true%>">
	<portlet:renderURL var="editNotificationConfigURL" windowState="<%=LiferayWindowState.POP_UP.toString() %>">
	
		<portlet:param name="mvcPath"
			value='<%=templatePath +"backoffice/status/notification_status_config_edit.jsp"%>'/>
		<portlet:param name="<%=NotificationStatusConfigDisplayTerms.DOSSIER_NEXT_STATUS%>"
			value="<%=String.valueOf(notificationStatusConfig.getDossierNextStatus())%>" />
	</portlet:renderURL>
	
	<aui:button
		cssClass="btn-default" 
		icon="icon-edit"
		value="edit"
		useDialog="true"
		href="<%=editNotificationConfigURL.toString()%>" />	
	
</c:if>


<c:if test="<%=notificationStatusConfig.getActive() == true%>">
	<portlet:actionURL var="deactiveNotificationConfigURL" name="changeNotificationStatusConfig">
	
		<portlet:param name="<%=NotificationStatusConfigDisplayTerms.DOSSIER_NEXT_STATUS%>"
			value="<%=String.valueOf(notificationStatusConfig.getDossierNextStatus())%>" />	
		<portlet:param name="<%=NotificationStatusConfigDisplayTerms.ACTIVE%>"
			value="false" />
		<portlet:param name="<%=WebKeys.REDIRECT_URL %>" value="<%=iteratorURL%>" />
			
	</portlet:actionURL>

	<aui:button 
		cssClass="btn-success" 
		icon="icon-eye-open"
		value="active"
		href="<%=deactiveNotificationConfigURL.toString()%>" />

</c:if>

<c:if test="<%=notificationStatusConfig.getActive() == false%>">
	
	<portlet:actionURL var="activeNotificationConfigURL" name="changeNotificationStatusConfig">
	
		<portlet:param name="<%=NotificationStatusConfigDisplayTerms.DOSSIER_NEXT_STATUS%>"
			value="<%=String.valueOf(notificationStatusConfig.getDossierNextStatus())%>" />
		<portlet:param name="<%=NotificationStatusConfigDisplayTerms.ACTIVE%>"
			value="true" />
		<portlet:param name="<%=WebKeys.REDIRECT_URL %>" value="<%=iteratorURL%>" />	
	</portlet:actionURL>

	<aui:button
		cssClass="btn-danger" 
		icon="icon-eye-close"
		value="inactive"
		href="<%=activeNotificationConfigURL.toString()%>" />
</c:if>

