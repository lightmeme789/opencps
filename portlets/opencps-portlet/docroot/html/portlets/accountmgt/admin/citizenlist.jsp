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
<%@page import="org.opencps.util.ActionKeys"%>
<%@page import="org.opencps.accountmgt.permissions.CitizenPermission"%>
<%@page import="org.opencps.util.DateTimeUtil"%>
<%@page import="org.opencps.util.PortletUtil"%>
<%@page import="org.opencps.accountmgt.search.CitizenSearch"%>
<%@page import="org.opencps.accountmgt.service.CitizenLocalServiceUtil"%>
<%@page import="org.opencps.accountmgt.search.CitizenSearchTerm"%>
<%@page import="com.liferay.portal.kernel.dao.search.SearchContainer"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="javax.persistence.metamodel.ListAttribute"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="org.opencps.util.WebKeys"%>
<%@page import="org.opencps.accountmgt.model.Citizen"%>
<%@page import="com.liferay.portal.kernel.dao.search.SearchEntry"%>
<%@page import="org.opencps.accountmgt.search.CitizenDisplayTerms"%>
<%@page import="org.opencps.accountmgt.util.AccountMgtUtil"%>
<%@page import="org.opencps.util.PortletConstants"%>
<%@page import="com.liferay.portal.kernel.language.LanguageUtil"%>

<%@ include file="../init.jsp" %>


<liferay-util:include page="/html/portlets/accountmgt/admin/toptabs.jsp" servletContext="<%=application %>" />

<%
	if(request.getAttribute(WebKeys.CITIZEN_ENTRY) != null){
		citizen = (Citizen) request.getAttribute(WebKeys.CITIZEN_ENTRY);	
	}

	
	PortletURL iteratorURL = renderResponse.createRenderURL();
	
	List<Citizen> citizens = new ArrayList<Citizen>();
	
	long citizenId = citizen != null ? citizen.getCitizenId() : 0L;
	
	int accountStatus = ParamUtil.getInteger(request, CitizenDisplayTerms.CITIZEN_ACCOUNTSTATUS, -1);
	
	int countRegistered = CitizenLocalServiceUtil.countByG_S(scopeGroupId, PortletConstants.ACCOUNT_STATUS_REGISTERED);
	
	int countConfirmed = CitizenLocalServiceUtil.countByG_S(scopeGroupId, PortletConstants.ACCOUNT_STATUS_CONFIRMED);

	int countApproved = CitizenLocalServiceUtil.countByG_S(scopeGroupId, PortletConstants.ACCOUNT_STATUS_APPROVED);

	int countLocked = CitizenLocalServiceUtil.countByG_S(scopeGroupId, PortletConstants.ACCOUNT_STATUS_LOCKED);
	
	int totalCount = 0;
	
	String searchKeyword = ParamUtil.getString(request, "keywords");
	
	iteratorURL.setParameter("mvcPath", templatePath + "citizenlist.jsp");
	iteratorURL.setParameter(CitizenDisplayTerms.CITIZEN_ACCOUNTSTATUS, String.valueOf(accountStatus));
	
%>


<c:if test="<%=CitizenPermission.contains(permissionChecker, scopeGroupId, ActionKeys.ADD_CITIZEN) %>" >
	<liferay-util:include page='<%=templatePath + "toolbar.jsp" %>' servletContext="<%=application %>" />
</c:if>


<aui:row cssClass="mg-b-20 text-align-right">
	<aui:col width="100">
		<span class="span4 bold">
			<liferay-ui:message key="account.status.total" />  : <%=countLocked +
				countConfirmed + countRegistered + countApproved
			%>
		</span>
		<span class="span2">
			<liferay-ui:message key="account.status.registered" />  : <%=countRegistered %>
		</span>
		
		<span class="span2">
			<liferay-ui:message key="account.status.confirmed" />  : <%=countConfirmed %>
		</span>
		
		<span class="span2">
			<liferay-ui:message key="account.status.approved" />  : <%=countApproved %>
		</span>
		
		<span class="span2">
			<liferay-ui:message key="account.status.locked" />  : <%=countLocked %>
		</span>
	</aui:col>
</aui:row>


<div class="opencps-searchcontainer-wrapper-width-header default-box-shadow radius8">

	<liferay-ui:search-container searchContainer="<%= new CitizenSearch(
		renderRequest ,SearchContainer.DEFAULT_DELTA, iteratorURL) %>">
		
		<liferay-ui:search-container-results>
			<%
				CitizenSearchTerm searchTerms = (CitizenSearchTerm) searchContainer.getSearchTerms();
				
				citizens = CitizenLocalServiceUtil.searchCitizen(scopeGroupId, searchKeyword, accountStatus, searchContainer.getStart(), searchContainer.getEnd());
				totalCount = CitizenLocalServiceUtil.countCitizen(scopeGroupId, searchKeyword, accountStatus);
				 
				total = totalCount;
				results = citizens;
				
				pageContext.setAttribute("results", results);
				pageContext.setAttribute("total", total);
			%>
		
		</liferay-ui:search-container-results>
		<liferay-ui:search-container-row 
			className="org.opencps.accountmgt.model.Citizen" 
			modelVar="citiZen" 
			keyProperty="citizenId"
		>
			<%
				String gender = StringPool.BLANK;
				gender = PortletUtil.getGender(citiZen.getGender(), themeDisplay.getLocale());
				
				String accoutStatus = StringPool.BLANK;
				
				accoutStatus = LanguageUtil.get(portletConfig, themeDisplay.getLocale(), PortletUtil.getAccountStatus(citiZen.getAccountStatus(), themeDisplay.getLocale()));
				
				row.setClassName("opencps-searchcontainer-row");
				
				row.addText(citiZen.getPersonalId());
				row.addText(citiZen.getFullName());
				row.addText(gender);
				row.addText(DateTimeUtil.convertDateToString(citiZen.getBirthdate(), DateTimeUtil._VN_DATE_FORMAT));
				row.addText(citiZen.getEmail());
				row.addText(accoutStatus);
				row.addJSP("center", SearchEntry.DEFAULT_VALIGN,  "/html/portlets/accountmgt/admin/citizen_actions.jsp", config.getServletContext(), request, response);
				
			%>
			
		</liferay-ui:search-container-row>
		<liferay-ui:search-iterator type="opencs_page_iterator"/>
	</liferay-ui:search-container>	
</div>