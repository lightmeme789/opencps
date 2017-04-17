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
	 * along with this program. If not, see <http://www.gnu.org/licenses/>
	 */
%>

<%@ include file="../../init.jsp"%>

<liferay-util:include page='<%=templatePath + "backoffice/status/toolbar.jsp"%>'
	servletContext="<%=application%>" />

<%
	long notiStatusId = ParamUtil.getLong(request, NotificationStatusConfigDisplayTerms.NOTICE_CONFIG_ID,0);	

	PortletURL iteratorURL = renderResponse.createRenderURL();
	iteratorURL.setParameter("tabs1", PortletKeys.TOP_TABS_NOTI_CONFIG_STATUS);
	iteratorURL.setParameter(NotificationStatusConfigDisplayTerms.NOTICE_CONFIG_ID, String.valueOf(notiStatusId));
	
%>
<div
	class="opencps-searchcontainer-wrapper-width-header default-box-shadow radius8">
	
	<liferay-ui:search-container
		searchContainer="<%=new NotificationStatusConfigSearch(renderRequest,
						SearchContainer.DEFAULT_DELTA, iteratorURL)%>">

		<liferay-ui:search-container-results>
			<%
				List<NotificationStatusConfig> notificationStatusConfigs = NotificationStatusConfigLocalServiceUtil
											.getNotificationStatusConfigs(searchContainer.getStart(),
													searchContainer.getEnd());
		

				int totalSize = NotificationStatusConfigLocalServiceUtil
						.getNotificationStatusConfigsCount();

				pageContext.setAttribute("results", notificationStatusConfigs);
				pageContext.setAttribute("total", totalSize);
			%>

		</liferay-ui:search-container-results>
		<liferay-ui:search-container-row
			className="org.opencps.notificationmgt.model.NotificationStatusConfig"
			modelVar="notificationStatusConfig" keyProperty="notiStatusConfigId">
			<%

				row.setClassName("opencps-searchcontainer-row");

				row.addText(String.valueOf(row.getPos() +1));
				row.addText(DataMgtUtils.getDictItemName(themeDisplay.getScopeGroupId(),PortletPropsValues.DATAMGT_MASTERDATA_DOSSIER_STATUS
						, notificationStatusConfig.getDossierNextStatus(), locale));
				
				row.addText(DateTimeUtil.convertDateToString(
						notificationStatusConfig.getCreateDate(),
						DateTimeUtil._VN_DATE_TIME_FORMAT));
				
				row.addText(DateTimeUtil.convertDateToString(
						notificationStatusConfig.getModifiedDate(),
						DateTimeUtil._VN_DATE_TIME_FORMAT));

				
				row.addJSP(
						"center",
						SearchEntry.DEFAULT_VALIGN,
						templatePath+"backoffice/status/notification_status_config_actions.jsp",
						config.getServletContext(), request, response);
				row.setParameter(WebKeys.REDIRECT_URL, iteratorURL);
						
			%>

		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator type="opencs_page_iterator" />
	</liferay-ui:search-container>
</div>

<%!private static Log _log = LogFactoryUtil
			.getLog("html.portlets.notificationmgt.backoffice.status.notification_status_config_list");%>