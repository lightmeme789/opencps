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

<liferay-util:include page='<%=templatePath + "backoffice/event/toolbar.jsp"%>'
	servletContext="<%=application%>" />

<%

	long notiStatusId = ParamUtil.getLong(request, NotificationEventConfigDisplayTerms.NOTICE_STATUS_CONFIG_ID,0);
	
	PortletURL iteratorURL = renderResponse.createRenderURL();
	iteratorURL.setParameter("tabs1", PortletKeys.TOP_TABS_NOTI_CONFIG_EVENT);
	iteratorURL.setParameter(NotificationEventConfigDisplayTerms.NOTICE_STATUS_CONFIG_ID, String.valueOf(notiStatusId));
	

%>

<div class="opencps-searchcontainer-wrapper-width-header default-box-shadow radius8">

	<liferay-ui:error key="<%= MessageKeys.NOTIFICATION_STATUS_SYSTEM_EXCEPTION_OCCURRED%>" 
		message="<%=MessageKeys.NOTIFICATION_STATUS_SYSTEM_EXCEPTION_OCCURRED %>" />

	<liferay-ui:search-container
		searchContainer="<%=new NotificationEventConfigSearch(renderRequest,
						SearchContainer.DEFAULT_DELTA, iteratorURL)%>">
						
		<liferay-ui:search-container-results>
			<%
			List<NotificationEventConfig> notiEventConfigs = new ArrayList<NotificationEventConfig>();
			int totalSize = 0;
			
			if (notiStatusId > 0) {
				notiEventConfigs = NotificationEventConfigLocalServiceUtil
						.getByNotiStatusConfigIds(notiStatusId);

				totalSize = NotificationEventConfigLocalServiceUtil
						.countByNotiStatusConfigIds(notiStatusId);

			}else{
				notiEventConfigs = NotificationEventConfigLocalServiceUtil.getNotificationEventConfigs(searchContainer.getStart(),
						searchContainer.getEnd());
				
				totalSize = NotificationEventConfigLocalServiceUtil.getNotificationEventConfigsCount();
			}

			pageContext.setAttribute("results", notiEventConfigs);
			pageContext.setAttribute("total", totalSize);
			%>

		</liferay-ui:search-container-results>
		<liferay-ui:search-container-row
			className="org.opencps.notificationmgt.model.NotificationEventConfig"
			modelVar="notiEventConfig" keyProperty="notiEventConfigId">
			<%

			row.setClassName("opencps-searchcontainer-row");

			row.addText(String.valueOf(row.getPos() +1));
			
			NotificationStatusConfig notiStatus = new NotificationStatusConfigImpl();
			
			notiStatus = NotificationStatusConfigLocalServiceUtil.getNotificationStatusConfig(notiEventConfig.getNotiStatusConfigId());
			
			row.addText(DataMgtUtils.getDictItemName(themeDisplay.getScopeGroupId(),PortletPropsValues.DATAMGT_MASTERDATA_DOSSIER_STATUS
					, notiStatus.getDossierNextStatus(), locale));
			
			row.addText(notiEventConfig.getEventName());
			
			row.addText(notiEventConfig.getDescription());
			
			row.addText(notiEventConfig.getPattern());
			
			row.addText(DateTimeUtil.convertDateToString(notiEventConfig.getCreateDate(),DateTimeUtil._VN_DATE_TIME_FORMAT));
			
			row.addText(DateTimeUtil.convertDateToString(notiEventConfig.getModifiedDate(),DateTimeUtil._VN_DATE_TIME_FORMAT));

			row.addJSP(
					"center",
					SearchEntry.DEFAULT_VALIGN,
					templatePath+"backoffice/event/notification_event_config_actions.jsp",
					config.getServletContext(), request, response);
			row.setParameter(WebKeys.REDIRECT_URL, iteratorURL);
						
			%>
			

		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator type="opencs_page_iterator"/>
	</liferay-ui:search-container>

</div>



<%!private static Log _log = LogFactoryUtil
			.getLog("html.portlets.notificationmgt.backoffice.event.notification_event_config_list");%>