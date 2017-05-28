
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
<%@page import="org.json.XML"%>
<%@page import="com.liferay.portal.kernel.json.JSONFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.json.JSONObject"%>
<%@page import="org.opencps.datamgt.service.DictItemLocalServiceUtil"%>
<%@page import="org.opencps.datamgt.model.DictItem"%>
<%@page import="org.opencps.util.PortletPropsValues"%>
<%@page import="org.opencps.datamgt.service.DictCollectionLocalServiceUtil"%>
<%@page import="org.opencps.datamgt.model.DictCollection"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.opencps.statisticsmgt.service.DossiersStatisticsLocalServiceUtil"%>
<%@page import="org.opencps.statisticsmgt.model.DossiersStatistics"%>
<%@page import="org.opencps.statisticsmgt.service.DossiersStatisticsServiceUtil"%>
<%@page import="com.liferay.portal.kernel.json.JSONArray"%>

<%@ include file="init.jsp" %>

<%
	List<String> codes = new ArrayList<String>();

	if (domainDeepLevel > 1 && Validator.isNotNull(domainCodes) &&
		!domainCodes.equalsIgnoreCase("all")) {
		
		String[] arrCode = StringUtil.split(domainCodes);
		
		DictCollection dictCollection =
			DictCollectionLocalServiceUtil.getDictCollection(
				scopeGroupId,
				PortletPropsValues.DATAMGT_MASTERDATA_SERVICE_DOMAIN);

		if (dictCollection != null) {
			for (int i = 0; i < arrCode.length; i++) {
				try {
					String code = arrCode[i];
					
					DictItem dictItem =
						DictItemLocalServiceUtil.getDictItemInuseByItemCode(
							dictCollection.getDictCollectionId(), code);
					
					List<DictItem> dictItems =
						DictItemLocalServiceUtil.getTreeItems(
							dictItem.getTreeIndex(), 1);
					
					codes.add(code);
					
					if(dictItems != null){
						for(DictItem item :dictItems){
							codes.add(item.getItemCode());
						}
					}
				}
				catch (Exception e) {

				}

			}
		}
	}
	
	if(codes != null && !codes.isEmpty()){
		domainCodes = StringUtil.merge(codes);
		
	}

	List<DossiersStatistics> dossiersStatistics =
		DossiersStatisticsLocalServiceUtil.getStatsByGovAndDomain(
			scopeGroupId, startMonth, startYear, period, govCodes,
			domainCodes, level, domainDeepLevel);
	
	JSONArray jsonArray =
		StatisticsUtil.renderData(
			scopeGroupId, dossiersStatistics, fieldDatasShemas,
			filterKey, startMonth, startYear, period, currentMonth, currentYear, level, 
			domainDeepLevel, locale);
	
	JSONArray sortedJsonArray = StatisticsUtil.sortByCodes(jsonArray, StringUtil.split(domainCodes));
	
	//System.out.println(sortedJsonArray.toString());
	
	//org.json.JSONArray array = new org.json.JSONArray(sortedJsonArray.toString());
	//String xml = XML.toString(array, "data");
	//System.out.println(xml);
	
%>
<c:choose>
	<c:when test="<%=portletDisplayDDMTemplateId > 0 %>">
		<%
			Map<String, Object> contextObjects = new HashMap<String, Object>();

			contextObjects.put("jsonData", sortedJsonArray.toString());
			contextObjects.put("periodMap", StatisticsUtil.getPeriodMap(startMonth, startYear, period));
			//contextObjects.put("xmlData", xml);
		%>
		<%= PortletDisplayTemplateUtil.renderDDMTemplate(pageContext, portletDisplayDDMTemplateId, dossiersStatistics, contextObjects) %>
	</c:when>
	<c:otherwise>
	</c:otherwise>
</c:choose>

