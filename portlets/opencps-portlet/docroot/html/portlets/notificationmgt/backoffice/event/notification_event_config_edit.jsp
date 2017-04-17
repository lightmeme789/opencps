<%@ include file="../../init.jsp"%>
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

<%
	long notiEventConfigId = ParamUtil.getLong(request,NotificationEventConfigDisplayTerms.NOTICE_EVENT_CONFIG_ID,0);
	String backURL = ParamUtil.getString(request, WebKeys.BACK_URL,StringPool.BLANK);

	NotificationEventConfig notiEventConfig = null;

	if (notiEventConfigId > 0) {

		try {
			notiEventConfig = NotificationEventConfigLocalServiceUtil.fetchNotificationEventConfig(notiEventConfigId);
			
		} catch (Exception e) {
			
		}
	}
	
	boolean eventConfig = false;
	
	if(Validator.isNotNull(notiEventConfig)){
		eventConfig = true;
	}
	
	LayoutLister layoutLister = new LayoutLister();

	String rootNodeName = StringPool.BLANK;
	
	LayoutView layoutView  = layoutLister.getLayoutView(layout.getGroupId(), layout.isPrivateLayout(), rootNodeName, locale);
	
	List layoutList = layoutView.getList();
	
	List<NotificationStatusConfig> notiStatusConfigs = new ArrayList<NotificationStatusConfig>();
	
	notiStatusConfigs = NotificationStatusConfigLocalServiceUtil.getNotificationStatusConfigs(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	
	///////////////////////////////////////////
	String[] emailListException = StringUtil.split(notiEventConfig.getUserExcept());
	
	List leftList = new ArrayList();
	
	List<Employee> employees = new ArrayList<Employee>();
	employees = EmployeeLocalServiceUtil.getEmployees(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	
	for(Employee employee1:employees){
		if(!ArrayUtil.contains(emailListException, employee1.getEmail())){
			KeyValuePair keyValuePair =  new KeyValuePair(employee1.getEmail(), employee1.getEmail());
			leftList.add(keyValuePair);
		}
	}
	
	List rightList = new ArrayList();
	
	if(Validator.isNotNull(emailListException)){
		
		for(int i=0;i<emailListException.length;i++){
			
			try{
				Employee employee2 = EmployeeLocalServiceUtil.getEmployeeByEmail(themeDisplay.getScopeGroupId(), emailListException[i]);
				KeyValuePair keyValuePair =  new KeyValuePair(employee2.getEmail(), employee2.getEmail());
				rightList.add(keyValuePair);
			}catch(Exception e){
				continue;
			}
		}
		
	}
	////////////////////////////////////////////////
%>

<liferay-ui:header
	title='<%=eventConfig? "add-notification-event-config"
					: "update-notification-event-config"%>' />


<div class=" opencps-bound-wrapper pd20 default-box-shadow"">
	<div class="edit-form">

		<liferay-ui:error key="<%= MessageKeys.NOTIFICATION_STATUS_SYSTEM_EXCEPTION_OCCURRED%>" 
		message="<%=MessageKeys.NOTIFICATION_STATUS_SYSTEM_EXCEPTION_OCCURRED %>" />
		
		<liferay-ui:success key="<%= MessageKeys.NOTIFICATION_STATUS_ADD_SUCESS%>" 
		message="<%= MessageKeys.NOTIFICATION_STATUS_ADD_SUCESS%>"/>
		
		<liferay-ui:success key="<%= MessageKeys.NOTIFICATION_STATUS_UPDATE_SUCESS%>" 
		message="<%= MessageKeys.NOTIFICATION_STATUS_UPDATE_SUCESS%>"/>
		
		<portlet:actionURL var="updateNotificationEventConfigURL" name="updateNotificationEventConfig" />

		<aui:form action="<%=updateNotificationEventConfigURL.toString()%>"
			onSubmit='<%= "event.preventDefault(); " + renderResponse.getNamespace() + "getValueRightBox();" %>'> 
			method="post" name="fm">

			<aui:model-context bean="<%=notiEventConfig%>"
				model="<%=NotificationEventConfig.class%>" />
				
			<aui:input name="<%=NotificationEventConfigDisplayTerms.NOTICE_EVENT_CONFIG_ID%>"
				type="hidden"
				value="<%=eventConfig ? String.valueOf(notiEventConfig.getNotiEventConfigId()):StringPool.BLANK %>" />
				
			<aui:input name="<%=WebKeys.BACK_URL%>" type="hidden"
				value="<%=backURL%>" />
				
			<aui:input name="<%=WebKeys.CURRENT_URL%>" type="hidden"
				value="<%=currentURL%>" />
				
			<aui:select  name="<%=NotificationStatusConfigDisplayTerms.NOTICE_CONFIG_ID%>"
				label="noti-status-config">
				<%
					for (NotificationStatusConfig notiStatusConfig : notiStatusConfigs) {
							
						boolean statusSelect = false;
						
							if (eventConfig) {

								if (notiStatusConfig.getNotiStatusConfigId() == notiEventConfig
										.getNotiStatusConfigId()) {
									
									statusSelect = true;
								}
							}
				%>

				<aui:option selected="<%=true%>"
					value="<%=notiStatusConfig.getNotiStatusConfigId()%>"
				>
					<%=DataMgtUtils.getDictItemName(themeDisplay.getScopeGroupId(),PortletPropsValues.DATAMGT_MASTERDATA_DOSSIER_STATUS
									,notiStatusConfig.getDossierNextStatus(), locale)%>
				</aui:option>

				<%
				}
				%>
			</aui:select>
			
			<aui:fieldset>
			
				<aui:input type="text" name="<%=NotificationEventConfigDisplayTerms.EVENT_NAME %>" 
				value="<%=eventConfig ? notiEventConfig.getEventName():StringPool.BLANK %>"
				title="event-name"/>
				
				<aui:input type="text" name="<%=NotificationEventConfigDisplayTerms.DESCRIPTION %>" 
				value="<%=eventConfig ? notiEventConfig.getDescription() :StringPool.BLANK %>"
				title="description"/>
				
				<aui:input type="text" name="<%=NotificationEventConfigDisplayTerms.PATTERN %>" 
				value="<%=eventConfig ? notiEventConfig.getPattern() :StringPool.BLANK %>"
				title="pattern">
					<liferay-ui:icon-help message="tooltip-one"/>
				</aui:input>
				
	
				<aui:select label="page-redirect" name="<%=NotificationEventConfigDisplayTerms.NOTICE_REDIRECT_CONFIG_ID %>">
					<aui:option value="" />

				<%
					for (int i = 0; i < layoutList.size(); i++) {
					
							// id | parentId | ls | obj id | name | img | depth
					
							String layoutDesc = (String)layoutList.get(i);
					
							String[] nodeValues = StringUtil.split(layoutDesc, '|');
					
							long objId = GetterUtil.getLong(nodeValues[3]);
							String name = HtmlUtil.escape(nodeValues[4]);
					
							int depth = 0;
					
							if (i != 0) {
								depth = GetterUtil.getInteger(nodeValues[6]);
							}
					
							for (int j = 0; j < depth; j++) {
								name = "-&nbsp;" + name;
							}
					
							Layout curRootLayout = null;
					
							try {
								curRootLayout = LayoutLocalServiceUtil.getLayout(objId);
							}
							catch (Exception e) {
							}
					
							if (curRootLayout != null) {
								
								boolean select = false;
								
								if(eventConfig){
									
											if (curRootLayout.getPlid() == Long
													.valueOf(notiEventConfig.getPlId())) {

												select = true;
											}
										}
				%>
	
					<aui:option label="<%= name %>" 
						selected="<%=select%>" 
						value="<%= curRootLayout.getPlid() %>" />
	
				<%
					}
				}
				%>

				</aui:select>
				
				<aui:fieldset label="dossier-status-treemenu-display">
						<liferay-ui:input-move-boxes
							leftBoxName="availableEmployeeUser"
							leftList="<%= leftList %>"
							leftReorder="true"
							leftTitle="employee-list"
							rightBoxName="userExceptList"
							rightList="<%= rightList %>"
							rightTitle="employee-except"
							rightReorder="true"
						
						/>
				</aui:fieldset>
				<aui:input name="userExceptListValues" type="hidden"></aui:input>
				<aui:input name="<%=NotificationEventConfigDisplayTerms.ACTIVE%>" 
							type="checkbox" 
							label="inuse" 
							value="<%=eventConfig ?notiEventConfig.isActive():false %>"></aui:input>
							
				<aui:button type="submit" name="submit" icon="icon-save" />
			</aui:fieldset>
		</aui:form>
	</div>
</div>

<aui:script>
	
	Liferay.provide(window, '<portlet:namespace />getValueRightBox', function() {
		var A = AUI();
		var userExceptList = Liferay.Util.listSelect(document.<portlet:namespace />fm.<portlet:namespace />userExceptList);
		var userExceptListValues = A.one('#<portlet:namespace />userExceptListValues');
		 console.log("userExceptList:"+userExceptList);
		 console.log("userExceptListValues:"+userExceptListValues);
		userExceptListValues.val(userExceptList);
		submitForm(document.<portlet:namespace />fm);
	}, ['liferay-util-list-fields']);
</aui:script>

<%!private Log _log = LogFactoryUtil
			.getLog("html.portlets.notificationmgt.backoffice.notification_status_config_edit");%>
