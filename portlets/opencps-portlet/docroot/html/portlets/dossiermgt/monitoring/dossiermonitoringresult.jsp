<%@page import="org.opencps.util.DateTimeUtil"%>
<%@page import="java.text.SimpleDateFormat"%>
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

<%@ include file="../init.jsp"%>

<%@page import="org.opencps.dossiermgt.util.DossierMgtUtil"%>
<%@page import="org.opencps.util.PortletConstants"%>
<%@page import="com.liferay.portal.kernel.dao.orm.QueryUtil"%>
<%@page import="org.opencps.dossiermgt.search.DossierDisplayTerms"%>
<%@page import="org.opencps.util.WebKeys"%>
<%@page import="com.liferay.portal.kernel.language.LanguageUtil"%>
<%@page import="org.opencps.datamgt.service.DictItemLocalServiceUtil"%>
<%@page import="org.opencps.datamgt.model.DictItem"%>
<%@page import="org.opencps.dossiermgt.service.DossierLogLocalServiceUtil"%>
<%@page import="java.util.List"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="com.liferay.portal.kernel.util.FastDateFormatFactoryUtil"%>
<%@page import="java.text.Format"%>
<%@page import="org.opencps.util.DictItemUtil"%>
<%@page import="org.opencps.servicemgt.service.ServiceInfoLocalServiceUtil"%>
<%@page import="org.opencps.servicemgt.model.ServiceInfo"%>
<%@page import="org.opencps.dossiermgt.NoSuchDossierException"%>
<%@page import="org.opencps.dossiermgt.service.DossierLocalServiceUtil"%>
<%@page import="org.opencps.dossiermgt.model.Dossier"%>
<%@page import="org.opencps.dossiermgt.model.DossierLog"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@page import="com.liferay.portal.kernel.log.LogFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.log.Log"%>
<%@ taglib prefix="opencps-ui" uri="/WEB-INF/tld/opencps-ui.tld"%>

<%
	long dossierId = ParamUtil.getLong(request, DossierDisplayTerms.DOSSIER_ID);
	Dossier dossier = DossierMgtUtil.getDossierByDossierId(dossierId);
	ServiceInfo serviceInfo = null;
	PortletURL iteratorURL = renderResponse.createRenderURL();
	iteratorURL.setParameter("mvcPath", templatePath + "dossiermonitoringresult.jsp");
	
	if(Validator.isNotNull(dossier)) {
		try {
			serviceInfo = ServiceInfoLocalServiceUtil.getServiceInfo(dossier.getServiceInfoId());
		}
		catch (Exception ex) {
			
		}
	}
	
	Format dateFormatDate = FastDateFormatFactoryUtil.getDate(locale, timeZone);

	String keywordSearch = ParamUtil.getString(request, "keywords", StringPool.BLANK);
%>

<portlet:renderURL var="backURL">
	<portlet:param name="mvcPath" 
		value="/html/portlets/dossiermgt/monitoring/dossiermonitoringdossierlist.jsp" />	
	<portlet:param name="keywords" value="<%= keywordSearch %>" />	
</portlet:renderURL>

<liferay-ui:header backURL="<%= backURL.toString() %>" title="dossier-list" 
	backLabel="back" />

<div class="head">
	<h3 class="headh3"><liferay-ui:message key="ket-qua-tra-cuu-ho-so"/> 
		<%= Validator.isNotNull(dossier)?dossier.getReceptionNo():StringPool.BLANK %>
	</h3>
	<div class = "page-search">
		<liferay-util:include page="/html/portlets/dossiermgt/monitoring/toolbar.jsp" servletContext="<%=application %>" />
	</div>
</div>

<c:choose>
	<c:when test="<%= Validator.isNotNull(dossier) %>">
	<div class="detail-left">
		<h4 class="matiepnhan">
			<liferay-ui:message key="reception-no" />
			-
			<%=Validator.isNotNull(dossier.getReceptionNo()) ? dossier
						.getReceptionNo() : LanguageUtil.get(pageContext,
						"monitoring-chua-co")%>
		</h4>

		<h4 class="thutuc">
			<liferay-ui:message key="service-name" />
		</h4>

		<p>
			-
			<%=Validator.isNotNull(serviceInfo) ? serviceInfo
						.getServiceName() : LanguageUtil.get(pageContext,
						"monitoring-chua-co")%>
		</p>

		<h4 class="coquanthuchien">
			<liferay-ui:message key="administration-name" />
		</h4>

		<p>
			-
			<%=Validator.isNotNull(dossier) ? dossier
						.getGovAgencyName() : LanguageUtil.get(pageContext,
						"monitoring-chua-co")%>
		</p>

		<h4 class="chuhoso">
			<liferay-ui:message key="subject-name" />
		</h4>

		<p>
			-
			<%=Validator.isNotNull(dossier.getSubjectName()) ? dossier
						.getSubjectName() : LanguageUtil.get(pageContext,
						"monitoring-chua-co")%></p>

		<h4 class="diachi">
			<liferay-ui:message key="address" />
		</h4>

		<p>
			-
			<%=Validator.isNotNull(dossier.getAddress()) ? dossier
						.getAddress() : LanguageUtil.get(pageContext,
						"monitoring-chua-co")%></p>

		<h4 class="ngaytiepnhan">
			<liferay-ui:message key="receive-datetime" />
		</h4>

		<p>
			-
			<%=(Validator.isNotNull(dossier.getReceiveDatetime())) ? dateFormatDate
						.format(dossier.getReceiveDatetime()) : LanguageUtil
						.get(pageContext, "monitoring-chua-co")%></p>

		<h4 class="ngayhentra">
			<liferay-ui:message key="estimate-datetime" />
		</h4>

		<p>
			-
			<%=(Validator.isNotNull(dossier.getEstimateDatetime())) ? dateFormatDate
						.format(dossier.getEstimateDatetime()) : LanguageUtil
						.get(pageContext, "monitoring-chua-co")%></p>

		<h4 class="ngayhoanthanh">
			<liferay-ui:message key="finish-datetime" />
		</h4>

		<p>
			-
			<%=(Validator.isNotNull(dossier.getFinishDatetime())) ? dateFormatDate
						.format(dossier.getFinishDatetime()) : LanguageUtil
						.get(pageContext, "monitoring-chua-co")%>
		</p>

	</div>

	<div class="detail-right">
		<h4>
			<liferay-ui:message key="qua-trinh-xu-ly-ho-so" />
		</h4>
		<%
			int[] logFitter = { 0,
						PortletConstants.DOSSIER_FILE_SYNC_STATUS_REQUIREDSYNC,
						PortletConstants.DOSSIER_FILE_SYNC_STATUS_SYNCSUCCESS };
				List<DossierLog> dossierLogs = null;
				try {
// 					dossierLogs = DossierLogLocalServiceUtil
// 							.getDossierLogByDossierId(dossierId, logFitter);
					
					dossierLogs = DossierLogLocalServiceUtil.findDossierLog(1, dossierId, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
				} catch (Exception e) {
					_log.error(e);
				}
		%>

		<div class="info">
			<%
				for (DossierLog dossierLog : dossierLogs) {
						if (!dossierLog.getDossierStatus().equalsIgnoreCase(
								"system")
								&& !dossierLog.getDossierStatus().equalsIgnoreCase(
										"error")) {					
			%>
			<div class="date">
				<p>
					<%= Validator.isNotNull(dossierLog.getModifiedDate()) ? DateTimeUtil.convertDateToString(dossierLog.getModifiedDate(), DateTimeUtil._VN_DATE_TIME_FORMAT) : StringPool.BLANK %>
				</p>
				<p>
					<%= Validator.isNotNull(dossierLog.getDossierStatus())? LanguageUtil.get(pageContext, dossierLog.getDossierStatus()+"-cus"):StringPool.BLANK %>
				</p>
			</div>
			<div>
				<p>
					<span><liferay-ui:message key="doi-tuong" />:</span>
					<%= Validator.isNotNull(dossierLog.getActor())? LanguageUtil.get(pageContext, dossierLog.getActor()+"-cus"):StringPool.BLANK %>
				</p>
				<p>
					<span><liferay-ui:message key="ghi-chu" />:</span>
					<liferay-ui:message key="<%= dossierLog.getActionInfo() %>" />
				</p>
			</div>
			
			<%			}
				} 
			%>

		</div>

	</div>
	</c:when>
	<c:otherwise>
		<div class="error-msg">
			<liferay-ui:message key="no-dossier-found" />
		</div>
	</c:otherwise>
</c:choose>

<%!
	private Log _log = LogFactoryUtil.getLog("html.portlets.dossiermgt.monitoring.result.jsp");
%>