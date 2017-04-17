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

<%@ include file="/init.jsp" %>



<%@page import="com.liferay.portal.service.permission.PortletPermissionUtil"%>
<%@page import="com.liferay.portal.kernel.language.LanguageUtil"%>
<%@page import="com.liferay.portal.kernel.util.Constants"%>
<%@page import="com.liferay.portal.kernel.util.HtmlUtil"%>
<%@page import="com.liferay.util.dao.orm.CustomSQLUtil"%>
<%@page import="com.liferay.portal.kernel.log.LogFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.log.Log"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.portal.kernel.dao.orm.QueryUtil"%>
<%@page import="com.liferay.portal.kernel.dao.search.SearchEntry"%>
<%@page import="com.liferay.portal.kernel.dao.search.ResultRow"%>
<%@page import="com.liferay.portal.kernel.dao.search.DisplayTerms"%>
<%@page import="com.liferay.portal.model.User"%>
<%@page import="com.liferay.portlet.PortletURLFactoryUtil"%>
<%@page import="com.liferay.portal.service.LayoutLocalServiceUtil"%>
<%@page import="com.liferay.portal.model.Layout"%>
<%@page import="com.liferay.portal.kernel.dao.orm.QueryUtil"%>
<%@page import="com.liferay.portal.kernel.util.KeyValuePair"%>
<%@page import="com.liferay.portal.kernel.util.ArrayUtil"%>


<%@page import="javax.portlet.WindowState"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="javax.portlet.PortletPreferences"%>
<%@page import="javax.portlet.PortletRequest"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.portlet.PortletURLFactoryUtil"%>

<%@page import="org.opencps.util.WebKeys"%>
<%@page import="org.opencps.util.ActionKeys"%>
<%@page import="org.opencps.notificationmgt.utils.PortletKeys"%>
<%@page import="org.opencps.util.PortletPropsValues"%>
<%@page import="org.opencps.util.DataMgtUtils"%>
<%@page import="org.opencps.util.ActionKeys"%>
<%@page import="org.opencps.util.MessageKeys"%>
<%@page import="org.opencps.util.LayoutView"%>
<%@page import="org.opencps.util.LayoutLister"%>

<%@page import="org.opencps.notificationmgt.service.NotificationEventConfigLocalServiceUtil"%>
<%@page import="org.opencps.notificationmgt.model.NotificationEventConfig"%>
<%@page import="org.opencps.notificationmgt.search.NotificationEventConfigSearch"%>
<%@page import="org.opencps.notificationmgt.search.NotificationEventConfigDisplayTerms"%>
<%@page import="org.opencps.notificationmgt.NoSuchNotificationEventConfigException"%>
<%@page import="org.opencps.notificationmgt.model.impl.NotificationEventConfigImpl"%>

<%@page import="org.opencps.notificationmgt.search.NotificationStatusConfigDisplayTerms"%>
<%@page import="org.opencps.notificationmgt.search.NotificationStatusConfigSearch"%>
<%@page import="org.opencps.notificationmgt.model.impl.NotificationStatusConfigImpl"%>
<%@page import="org.opencps.notificationmgt.model.NotificationStatusConfig"%>
<%@page import="org.opencps.notificationmgt.service.NotificationStatusConfigLocalServiceUtil"%>

<%@page import="org.opencps.usermgt.service.EmployeeLocalServiceUtil"%>
<%@page import="org.opencps.usermgt.model.Employee"%>

<%@page import="org.opencps.datamgt.model.DictItem"%>
<%@page import="org.opencps.datamgt.service.DictCollectionLocalServiceUtil"%>
<%@page import="org.opencps.datamgt.model.impl.DictCollectionImpl"%>
<%@page import="org.opencps.datamgt.model.DictCollection"%>

<%@page import="org.opencps.util.PortletUtil"%>
<%@page import="org.opencps.util.DateTimeUtil"%>
<%@page import="java.util.Date"%>
<%
	PortletPreferences preferences = renderRequest.getPreferences();
	
	long rootGroupId = GetterUtil.getLong(preferences.getValue(PortletKeys.PREFER__ROOTGROUPID__,StringPool.BLANK),0);
	
	boolean isPermisson = false;
	 
%>

