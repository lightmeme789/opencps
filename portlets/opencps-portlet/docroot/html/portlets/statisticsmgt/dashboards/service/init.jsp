
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

<%@ include file="../../init.jsp" %>

<%
	String chartTitle =  GetterUtil.getString(portletPreferences
		.getValue("chartTitle", StringPool.BLANK));

	String xaxisUnit = GetterUtil.getString(portletPreferences
		.getValue("xaxisUnit", StringPool.BLANK));
	
	String yaxisUnit = GetterUtil.getString(portletPreferences
		.getValue("yaxisUnit", StringPool.BLANK));
	
	String govCode = GetterUtil.getString(portletPreferences
		.getValue("govCode", StringPool.BLANK));
	
	String domainCode = GetterUtil.getString(portletPreferences
		.getValue("domainCode", StringPool.BLANK));
	
	int startMonth = GetterUtil.getInteger(portletPreferences
		.getValue("startMonth", String.valueOf(1)));
	
	
	
	int startYear = GetterUtil.getInteger(portletPreferences
		.getValue("startYear", String.valueOf(1)));
	
	int period = GetterUtil.getInteger(portletPreferences
		.getValue("period", String.valueOf(1)));

%>

