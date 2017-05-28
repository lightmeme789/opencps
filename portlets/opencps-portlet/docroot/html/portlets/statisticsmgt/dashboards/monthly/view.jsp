
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

<%@page import="com.liferay.portal.kernel.json.JSONObject"%>
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
			<c:when test="<%=jsonArray != null && jsonArray.length() > 0 %>">
				<%
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					JSONArray statsMonths = jsonObject.getJSONArray("stats-months");
					JSONArray periodLabels = jsonObject.getJSONArray("period-labels");
					
					if(periodLabels != null && periodLabels.length() > 0){
						for(int i = 0 ; i < periodLabels.length(); i++){
							String periodLabel = periodLabels.getString(i);
						}
					}
				%>	
				
				<<!-- script type="text/javascript">
				    var json = JSON.parse(JSON.stringify(eval(${chartData})));
				    
				    var labels = json['labels'];
				    var datasets = json['datasets'];
				    
				    var data = {
				        labels: labels,
				        datasets: datasets
				    };
				    
				    var ctx = document.getElementById("domainChart").getContext("2d");
				   
				    ctx.canvas.height = 250;
				    
				    var chart = new Chart(ctx, {
				        type: 'bar',
				        data: data,
				        
				        options: {
				            barValueSpacing: 20,
				            responsive: true,
				            scales: {
				    			xAxes: [{
				    				
				    				scaleLabel: {
				    					display: true,
				    					labelString: "",
				    					barPercentage: 0.4
				    				}
				    			}],
				    
				    			yAxes: [{
				    				type: "linear",
				    				position: "left",
				    				id: "y-axis-1",
				    			
				    				scaleLabel: {
				    					display: true,
				    					labelString: ""
				    				}
				    			}]
				    		},
				    		animation: {
								onComplete: function () {
									var ctx = this.chart.ctx;
									ctx.textAlign = "center";
									ctx.textBaseline = "middle";
									var chart = this;
									var datasets = this.config.data.datasets;
				                    
									datasets.forEach(function (dataset, i) {
									    
										ctx.font = "15px Arial";
										ctx.fillStyle = "#888";
										chart.getDatasetMeta(i).data.forEach(function (p, j) {
										    var value = parseInt(datasets[i].data[j]);
										    if(value > 0){
										        ctx.fillText(value, p._model.x, p._model.y + - 5);
										    }
										});
									});
								}
							}
				            
				        }
				    });
  
				</script> -->
			</c:when>
			
			<c:otherwise>
				<div class="portlet-msg-alert"><liferay-ui:message key="not-found-stats"/></div>
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>

