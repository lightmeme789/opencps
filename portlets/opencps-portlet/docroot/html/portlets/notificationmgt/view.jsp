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

<%@ include file="init.jsp"%>

<%
	
	String[] names = new String[]{PortletKeys.TOP_TABS_NOTI_CONFIG_STATUS, PortletKeys.TOP_TABS_NOTI_CONFIG_EVENT};

	String tabValue = ParamUtil.getString(request, "tabs1", PortletKeys.TOP_TABS_NOTI_CONFIG_STATUS);
	
	List<String> urls = new ArrayList<String>();
	
	PortletURL tabURL = null;
	
	if (PortletPermissionUtil.contains(permissionChecker, plid, portletDisplay.getId(), ActionKeys.VIEW) ) {
		tabURL = PortletURLFactoryUtil.create(request, themeDisplay.getPortletDisplay().getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
	}
	
%>
<liferay-ui:tabs names="<%= StringUtil.merge(names) %>" url="<%=tabURL.toString()%>" tabsValues="<%= StringUtil.merge(names) %>" value="<%=tabValue %>">

	<c:if test='<%= tabValue.equalsIgnoreCase(PortletKeys.TOP_TABS_NOTI_CONFIG_STATUS)%>' >
		<jsp:include page="/html/portlets/notificationmgt/backoffice/status/notification_status_config_list.jsp" flush="true" />
	</c:if>

	<c:if test='<%= tabValue.equalsIgnoreCase(PortletKeys.TOP_TABS_NOTI_CONFIG_EVENT)%>' >
		<jsp:include page="/html/portlets/notificationmgt/backoffice/event/notification_event_config_list.jsp" flush="true" />
	</c:if>
</liferay-ui:tabs>	