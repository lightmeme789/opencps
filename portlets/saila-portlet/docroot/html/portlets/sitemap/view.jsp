<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme"%>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui"%>

<portlet:defineObjects />

<liferay-theme:defineObjects />

<ul class="sitemap-class">
    <li><a href="/login" class="sm-tutorial"><%-- <liferay-ui:message key="dvc.site-map.tutorial" /> --%>
        <img alt="">
    </a></li>
    <li><a href="/login" class="sm-login">
            <%-- <liferay-ui:message key="dvc.site-map.register-account" /> --%>
            <img alt=""></a></li>
    <li><a href="/login" class="sm-uw"><img alt="">
            <%-- <liferay-ui:message key="dvc.site-map.login" /> --%></a></li>
    <li><a href="/login" class="sm-result"><img alt="">
            <%-- <liferay-ui:message key="dvc.site-map.submit-online" /> --%></a></li>
   <%--  <li><a href="/can-bo-xu-ly-ho-so" class="sm-process"><img alt="">
            <liferay-ui:message key="dvc.site-map.cbxl" /></a></li> --%>
</ul>
