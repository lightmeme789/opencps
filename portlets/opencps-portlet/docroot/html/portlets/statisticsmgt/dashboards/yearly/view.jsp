
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

<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.liferay.portal.kernel.json.JSONArray"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.opencps.statisticsmgt.service.DossiersStatisticsLocalServiceUtil"%>
<%@page import="org.opencps.statisticsmgt.model.DossiersStatistics"%>

<%@ include file="init.jsp" %>

<%
	/* List<DossiersStatistics> dossiersStatistics = new ArrayList<DossiersStatistics>();
	try {
		for(int i = startMonth; i <= period; i++){
			DossiersStatistics statistics = DossiersStatisticsLocalServiceUtil
					.getDossiersStatisticsByG_GC_DC_M_Y_L(scopeGroupId, StringPool.BLANK,
							StringPool.BLANK, i, startYear, 0);
			dossiersStatistics.add(statistics);
		}
	} catch (Exception e) {

	} */
	
	List<DossiersStatistics> dossiersStatistics =
					DossiersStatisticsLocalServiceUtil.getStatsByGovAndDomain(
						scopeGroupId, startMonth, startYear, period, StringPool.BLANK,
						StringPool.BLANK, 0, 0);
	
	JSONArray jsonArray =
					StatisticsUtil.renderData(
						scopeGroupId, dossiersStatistics, fieldDatasShemas,
						StringPool.BLANK, startMonth, startYear, period, currentMonth, currentYear, 0, 
						0, locale);
	
%>

<c:choose>
	<c:when test="<%=portletDisplayDDMTemplateId > 0 %>">
		<%
			Map<String, Object> contextObjects = new HashMap<String, Object>();

			contextObjects.put("jsonData", jsonArray.toString());
			contextObjects.put("periodMap", StatisticsUtil.getPeriodMap(startMonth, startYear, period));
			//contextObjects.put("xmlData", xml);
		%>
		<%= PortletDisplayTemplateUtil.renderDDMTemplate(pageContext, portletDisplayDDMTemplateId, dossiersStatistics, contextObjects) %>
	</c:when>
	
	<c:otherwise>
		<c:choose>
			<c:when test="<%=dossiersStatistics != null && !dossiersStatistics.isEmpty() %>">
				<%
					int remainingNumber = 0;
					int receivedNumber = 0;
					int ontimeNumber = 0;
					int overtimeNumber = 0;
					int processingNumber = 0;
					int delayingNumber = 0;
					
					for(DossiersStatistics statistics : dossiersStatistics){
						receivedNumber += statistics.getReceivedNumber();
						ontimeNumber += statistics.getOntimeNumber();
						overtimeNumber += statistics.getOvertimeNumber();
					}
					
					processingNumber += dossiersStatistics.get(dossiersStatistics.size() - 1).getProcessingNumber();
					delayingNumber += dossiersStatistics.get(dossiersStatistics.size() - 1).getDelayingNumber();
				%>
				
				
					
				<div class="widget-wrapper">
					<%-- <div class="widget-header">
						<span class="span8 key">
							<liferay-ui:message key="stats-in-year"/>
						</span>
						<span class="span4 number"><%=currentYear %></span>
					</div> --%>
					
					<ul class="widget-content">
						<li class="widget-row">
							<span class="widget-cell">
								<span class="content">
									<span class="number"><%=remainingNumber %></span>
									<span class="key">
										<liferay-ui:message key="remaining-number"/>
									</span>
								</span>
							</span>
						</li>
						
						<li class="widget-row">
							<span class="widget-cell">
								<span class="content">
									<span class="number"><%=receivedNumber %></span>
									<span class="key">
										<liferay-ui:message key="received-number"/>
									</span>
								</span>
							</span>
						</li>
						
						<li class="widget-row">
							<span class="widget-cell">
								<span class="content">
									<span class="number"><%=ontimeNumber %></span>
									<span class="key">
										<liferay-ui:message key="ontime-number"/>
									</span>
								</span>
							</span>
						</li>
						
						<li class="widget-row">
							<span class="widget-cell">
								<span class="content">
									<span class="number"><%=overtimeNumber %></span>
									<span class="key">
										<liferay-ui:message key="overtime-number"/>
									</span>
								</span>
							</span>
						</li>
						
						<li class="widget-row">
							<span class="widget-cell">
								<span class="content">
									<span class="number"><%=processingNumber %></span>
									<span class="key">
										<liferay-ui:message key="processing-number"/>
									</span>
								</span>
							</span>							
						</li>
						
						<li class="widget-row">
					<span class="widget-cell">
						<span class="content">
							<span class="number"><%=delayingNumber %></span>
							<span class="key">
								<liferay-ui:message key="delaying-number"/>
							</span>
						</span>
					</span>
				</li>
			</ul>
		</div>
	</c:when>
	
	<c:otherwise>
		<div class="portlet-msg-alert"><liferay-ui:message key="not-found-stats"/></div>
	</c:otherwise>
</c:choose>
	</c:otherwise>
</c:choose>

