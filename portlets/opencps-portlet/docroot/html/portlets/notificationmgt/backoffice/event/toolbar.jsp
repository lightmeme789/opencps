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
	long notiStatusConfigId = ParamUtil.getLong(request,
			NotificationEventConfigDisplayTerms.NOTICE_STATUS_CONFIG_ID,
			0);

	List<NotificationStatusConfig> notiStatusConfigs = new ArrayList<NotificationStatusConfig>();

	notiStatusConfigs = NotificationStatusConfigLocalServiceUtil
			.getNotificationStatusConfigs(QueryUtil.ALL_POS,
					QueryUtil.ALL_POS);
%>


<aui:nav-bar cssClass="opencps-toolbar custom-toolbar">
	<aui:nav id="toolbarContainer"
		cssClass="nav-button-container  nav-display-style-buttons pull-left">
		<c:if test="<%=true%>">

			<portlet:renderURL var="addNotiEventConfigURL" windowState="<%=LiferayWindowState.POP_UP.toString() %>">
				<portlet:param name="mvcPath"
					value="/html/portlets/notificationmgt/backoffice/event/notification_event_config_edit.jsp" />
				<portlet:param name="<%=NotificationEventConfigDisplayTerms.NOTICE_EVENT_CONFIG_ID %>" value="<%=String.valueOf(notiStatusConfigId) %>"/>
			</portlet:renderURL>

			<aui:button 
				icon="icon-plus"
				cssClass="action-button"
				value='<%=String.valueOf(LanguageUtil.get(pageContext,
								"add-notification-event-config"))%>'
				id="popup_id"
				useDialog="true"
				href="<%=addNotiEventConfigURL.toString()%>" 
			 />
		</c:if>
	</aui:nav>
	
	<aui:nav-bar-search cssClass="pull-right">
		<div class="form-search">
			<aui:select  name="<%=NotificationEventConfigDisplayTerms.NOTICE_STATUS_CONFIG_ID%>"
					label=""
					onChange="searchNotiEvent()">
					<aui:option value="" />
					<%
						for (NotificationStatusConfig notiStatusConfig : notiStatusConfigs) {
								
							boolean statusSelect = false;
							
							if(notiStatusConfigId == notiStatusConfig.getNotiStatusConfigId()){
								statusSelect = true;
							}			
					%>
			
						<aui:option selected="<%=statusSelect%>"
							value="<%=notiStatusConfig.getNotiStatusConfigId()%>"
						>
							<%=DataMgtUtils.getDictItemName(themeDisplay.getScopeGroupId(),PortletPropsValues.DATAMGT_MASTERDATA_DOSSIER_STATUS
									,notiStatusConfig.getDossierNextStatus(), locale)%>
						</aui:option>
			
					<%
						}
					%>
			</aui:select>
		</div>	
	</aui:nav-bar-search>
</aui:nav-bar>

<script type="text/javascript">
function searchNotiEvent(){
	AUI().use('liferay-portlet-url',function(A){
		
		var notiStatusId = A.one('#<portlet:namespace /><%=NotificationEventConfigDisplayTerms.NOTICE_STATUS_CONFIG_ID%>').val();
				
		var portletURL = Liferay.PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, WebKeys.NOTIFICATION_MGT_PORTLET, themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>');
		portletURL.setParameter("tabs1", "<%=PortletKeys.TOP_TABS_NOTI_CONFIG_EVENT%>");
		portletURL.setParameter("<%=NotificationEventConfigDisplayTerms.NOTICE_STATUS_CONFIG_ID%>", notiStatusId);
		portletURL.setWindowState("<%=LiferayWindowState.NORMAL.toString()%>");
		portletURL.setPortletMode("normal");
		
		window.location.href = portletURL.toString();
		
	});
}
</script>

<%!private Log _log = LogFactoryUtil
			.getLog("html.portlets.notificationmgt.backoffice.event.toolbar");%>