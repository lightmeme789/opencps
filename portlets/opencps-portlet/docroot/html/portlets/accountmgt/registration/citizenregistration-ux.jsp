

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
<%@page import="javax.portlet.PortletRequest"%>
<%@page import="javax.portlet.WindowState"%>
<%@page import="com.liferay.portlet.PortletURLFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.language.UnicodeLanguageUtil"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="org.opencps.accountmgt.DuplicateCitizenEmailException"%>
<%@page import="org.opencps.accountmgt.OutOfLengthCitizenEmailException"%>
<%@page import="org.opencps.accountmgt.OutOfLengthCitizenAddressException"%>
<%@page import="org.opencps.accountmgt.OutOfLengthCitizenNameException"%>
<%@page import="org.opencps.accountmgt.DuplicateCitizenPersonalIdException"%>
<%@page import="com.liferay.portal.ContactBirthdayException"%>
<%@page import="org.opencps.util.WebKeys"%>
<%@page import="org.opencps.accountmgt.model.Citizen"%>
<%@page import="org.opencps.accountmgt.search.CitizenDisplayTerms"%>
<%@page import="org.opencps.util.MessageKeys"%>
<%@page import="com.liferay.portal.kernel.language.LanguageUtil"%>
<%@page import="org.opencps.accountmgt.FileTypeFailException"%>
<%@page import="org.opencps.accountmgt.OutOfSizeFileUploadException"%>
<%@page import="org.opencps.accountmgt.InvalidFileUploadException"%>
<%@page import="org.opencps.accountmgt.InvalidWardCodeException"%>
<%@page import="org.opencps.accountmgt.InvalidDistricCodeException"%>
<%@page import="org.opencps.accountmgt.InvalidCityCodeException"%>
<%@page import="org.opencps.util.PortletPropsValues"%>
<%@page import="java.util.Date"%>
<%@page import="org.opencps.util.PortletUtil"%>
<%@page import="org.opencps.util.DateTimeUtil"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>

<%@ include file="../init.jsp" %>

<%
	if(request.getAttribute(WebKeys.CITIZEN_ENTRY) != null){
		citizen = (Citizen) request.getAttribute(WebKeys.CITIZEN_ENTRY);
	}

	long citizenId = citizen != null ? citizen.getCitizenId() : 0L;
	
	Date defaultBirthDate = DateTimeUtil.convertStringToDate("01/01/1970");
 		
 	PortletUtil.SplitDate spd = new PortletUtil.SplitDate(defaultBirthDate);
 	
 	Citizen citizenValidate = (Citizen) request.getAttribute("citizenValidate");
 	List<String> cdw = new ArrayList<String>();
 	if(Validator.isNotNull(citizenValidate)) {
 		String cityId = citizenValidate.getCityCode();
 		String districtId = citizenValidate.getDistrictCode();
 		String wardId = citizenValidate.getWardCode();
 		cdw.add(cityId);
 		cdw.add(districtId);
 		cdw.add(wardId);
 	}
%>

<div class="opencps-register-wrapper">
	
	<liferay-ui:error 
		exception="<%= OutOfLengthCitizenAddressException.class %>" 
		message="<%= OutOfLengthCitizenAddressException.class.getName() %>" 
	/>
	
	<liferay-ui:error 
		exception="<%= OutOfLengthCitizenEmailException.class %>" 
		message="<%= OutOfLengthCitizenEmailException.class.getName() %>" 
	/>
	
	<liferay-ui:error 
		exception="<%= OutOfLengthCitizenNameException.class %>" 
		message="<%= OutOfLengthCitizenNameException.class.getName() %>" 
	/>
	
	<liferay-ui:error 
		exception="<%= DuplicateCitizenEmailException.class %>" 
		message="<%= DuplicateCitizenEmailException.class.getName() %>" 
	/>
	
	<liferay-ui:error 
		key="<%=MessageKeys.ACCOUNT_SYSTEM_EXCEPTION_OCCURRED %>" 
		message="system.exception.occured" 
	/>
	
	<liferay-ui:error 
		exception="<%= InvalidCityCodeException.class %>" 
		message="<%= InvalidCityCodeException.class.getName() %>" 
	/>
	<liferay-ui:error 
		exception="<%= InvalidDistricCodeException.class %>" 
		message="<%= InvalidDistricCodeException.class.getName() %>" 
	/>
	<liferay-ui:error 
		exception="<%= InvalidWardCodeException.class %>" 
		message="<%= InvalidWardCodeException.class.getName() %>" 
	/>
	<liferay-ui:error 
		exception="<%= InvalidFileUploadException.class %>" 
		message="<%= InvalidFileUploadException.class.getName() %>" 
	/>
	<liferay-ui:error 
		exception="<%= FileTypeFailException.class %>" 
		message="<%= FileTypeFailException.class.getName() %>" 
	/>
	<liferay-ui:error 
		exception="<%= OutOfSizeFileUploadException.class %>" 
		message="<%= OutOfSizeFileUploadException.class.getName() %>" 
	/>
	<liferay-ui:error 
		exception="<%= DuplicateCitizenPersonalIdException.class %>" 
		message="<%= DuplicateCitizenPersonalIdException.class.getName() %>" 
	/>
	<liferay-ui:error 
		exception="<%= ContactBirthdayException.class %>" 
		message="<%= ContactBirthdayException.class.getName() %>" 
	/>
	
	<%
		String ACCOUNT_UPDATE_CUCCESS = StringPool.BLANK;
		if (Validator.isNotNull(messageSuccessfullRegistration)){
			ACCOUNT_UPDATE_CUCCESS = messageSuccessfullRegistration;
		} else {
			ACCOUNT_UPDATE_CUCCESS = MessageKeys.ACCOUNT_UPDATE_CUCCESS;
		}
	%>
	<liferay-ui:success 
		key="<%=MessageKeys.ACCOUNT_UPDATE_CUCCESS %>" 
		message="<%=ACCOUNT_UPDATE_CUCCESS %>"
	/>
	
	<portlet:actionURL var="updateCitizenURL" name="updateCitizen">
		<portlet:param 
			name="<%=CitizenDisplayTerms.CITIZEN_ID %>" 
			value="<%=String.valueOf(citizenId) %>"
		/>
		<portlet:param name="currentURL" value="<%=currentURL %>"/>
	</portlet:actionURL>
	
	<aui:form 
		action="<%=updateCitizenURL.toString() %>"
		name="fm"
		method="post"
		enctype="multipart/form-data"
		onSubmit='<%= "event.preventDefault(); " + renderResponse.getNamespace() + "registerAccount();" %>'
	>
	
		<aui:model-context bean='<%=citizenValidate %>' model='<%=Citizen.class %>'/>
		<aui:input name="citizenRegStep_cfg" value="<%=citizenRegStep_cfg %>" type="hidden"></aui:input>
		
		<aui:input name="citizenEmailConfirmToAdmin" value="<%=emailConfirmToAdmin %>" type="hidden"></aui:input>
			
		<div class="register-content">
		
			<div class="opencps accountmgt fm-registration header">
				<aui:row>
					<aui:col width="30" cssClass="title-text">
						<label ><liferay-ui:message key="register"></liferay-ui:message></label>
					</aui:col>
					<c:choose>
					<c:when test='<%=allowBussinessRegistration && allowCitizenRegistration %>'>
						<aui:col width="30" cssClass="register-options">
							<aui:row>
								<aui:col width="50">
									<aui:input type="radio" name="typeOfRegister" value="citizen" inlineLabel="right" label="citizen" checked="true"/>
								</aui:col>
								<aui:col width="50">
									<aui:input type="radio" name="typeOfRegister" value="business" inlineLabel="right" label="business"/>
								</aui:col>
							</aui:row>
						</aui:col>	
					</c:when>
					<c:otherwise>
						<aui:col width="30" cssClass="register-options">
							<aui:row>
								<aui:col width="50">
									<aui:input type="radio" name="typeOfRegister" disabled="true" value="citizen" inlineLabel="right" label="citizen" checked="true"/>
								</aui:col>
								<aui:col width="50">
									<aui:input type="radio" name="typeOfRegister" disabled="true" value="business" inlineLabel="right" label="business"/>
								</aui:col>
							</aui:row>
						</aui:col>	
					</c:otherwise>
					</c:choose>
					<aui:col width="30" cssClass="login-redirect">
						<a href='<%=themeDisplay.getURLSignIn() %>'><liferay-ui:message key="login" /></a>
					</aui:col>
				</aui:row>
			</div>
			
			<div class="bottom-horizontal-line"></div>
			
			<div class="content-part left">
				<aui:row>
					<aui:input 
	 					name="<%=CitizenDisplayTerms.CITIZEN_FULLNAME %>" 
	 					cssClass="input100"
	 					placeholder="citizen-full-name-require"
	 					maxLength="255"
						type="text"
						label=""
	 				>
		 				<aui:validator name="required" />
		 				<aui:validator name="maxLength">255</aui:validator>
	 				</aui:input>
				</aui:row>
				
				<aui:row>
					<aui:input 
						name="<%=CitizenDisplayTerms.CITIZEN_EMAIL %>"
						cssClass="input100"
						placeholder="email-require"
						maxLength="255"
						type="text"
						label=""
					>
						<aui:validator name="required" />
						<aui:validator name="email" />
						<aui:validator name="maxLength">255</aui:validator>	
					</aui:input>
				</aui:row>
				
				<aui:row>
					<aui:input 
	 					name="<%=CitizenDisplayTerms.CITIZEN_PERSONALID %>"
	 					cssClass="input100"
	 					placeholder="<%=CitizenDisplayTerms.CITIZEN_PERSONALID %>"
	 					maxLength="75"
						type="text"
						label=""
	 				>
						<aui:validator name="maxLength">75</aui:validator>	
	 				</aui:input>
				</aui:row>
				
				<aui:row>
					<div id = "<portlet:namespace/>def">
					<aui:input name="<%=CitizenDisplayTerms.CITIZEN_TELNO %>" 
						cssClass="input100" 
						placeholder="<%=CitizenDisplayTerms.CITIZEN_TELNO %>" 
						maxLength="20"
						type="text"
						label="">
						<aui:validator name="maxLength">20</aui:validator>	
					</aui:input>
					</div>
					<div  id="<portlet:namespace/>defErr" style="text-align: left; color: #b50303; margin-left:7px; margin-bottom: 10px; display: none;">
						<liferay-ui:message key="Error_TelNo_message"/>
					</div>
					<div  id="<portlet:namespace/>defErr2" style="text-align: left; color: #b50303; margin-left:7px; margin-bottom: 10px; display: none;">
						<liferay-ui:message key="Error_TelNo_message2"/>
					</div>
				</aui:row>
				
				<aui:row>
					<label class="control-label custom-lebel" for='<portlet:namespace/><%=CitizenDisplayTerms.CITIZEN_BIRTHDATE + "_REG" %>'>
	 					<liferay-ui:message key="birth-date-full"/> <span style="color:red">*</span>
	 				</label>
					<aui:input name="<%=CitizenDisplayTerms.BIRTH_DATE_DAY %>" type="hidden" />
					<aui:input name="<%=CitizenDisplayTerms.BIRTH_DATE_MONTH %>" type="hidden" />
					<aui:input name="<%=CitizenDisplayTerms.BIRTH_DATE_YEAR %>" type="hidden" />
	 				<aui:input name="<%=CitizenDisplayTerms.CITIZEN_BIRTHDATE + \"_REG\" %>" type="text" label=" " cssClass="input100" placeholder="<%=LanguageUtil.get(pageContext, \"ngay-sinh-placehoder\") %>" >
	 					<aui:validator name="required" />
	 					<aui:validator name="custom" errorMessage="<%=LanguageUtil.get(pageContext, \"ngay-sinh-pattern\") %>">
							function (val, fieldNode, ruleValue) {
								var result = false;

								var dateParts = val.split("/");
                
								if(dateParts.length == 1 && new Date(val) != 'Invalid Date'){
								    result = true;
								}else{
								  val = dateParts[1] + '/' + dateParts[0] + '/' + dateParts[2];
				
								  if (new Date(val) == 'Invalid Date') {
								    result = false;
								  }else{
								    result = true;
								  }

								}
				
								return result;
							}
						</aui:validator>
	 				</aui:input>
				</aui:row>
				
				<aui:row cssClass="input-file">
					<%
						String attachFileX = StringPool.BLANK;
						attachFileX =  "<a class=\"detail-terms-links\">"+LanguageUtil.get(pageContext, "term-detail-tai-day")+"</a>";
					%>
					<aui:input 
						type="file" 
						name="<%=CitizenDisplayTerms.CITIZEN_ATTACHFILE %>" 
						label="<%= LanguageUtil.format(pageContext, \"attachFile-x\", attachFileX) %>"
					>
						<aui:validator name="acceptFiles">
							'<%= StringUtil.merge( PortletPropsValues.ACCOUNTMGT_FILE_TYPE) %>'
						</aui:validator>
					</aui:input>
				</aui:row>
			</div>
			
			<div class="content-part right">
				<aui:row>
					<aui:select 
	 					name="<%=CitizenDisplayTerms.CITIZEN_GENDER %>"
	 					cssClass="input100"
	 				>
	 					<aui:option label="<%=CitizenDisplayTerms.CITIZEN_GENDER %>" value="<%=CitizenDisplayTerms.CITIZEN_GENDER %>" />
	 					<%
	 						if(PortletPropsValues.USERMGT_GENDER_VALUES != null && 
	 							PortletPropsValues.USERMGT_GENDER_VALUES.length > 0){
	 							for(int g = 0; g < PortletPropsValues.USERMGT_GENDER_VALUES.length; g++){
	 								%>
	 									<aui:option 
	 										value="<%= PortletPropsValues.USERMGT_GENDER_VALUES[g]%>"
	 										selected="<%=citizen != null && citizen.getGender() == PortletPropsValues.USERMGT_GENDER_VALUES[g]%>"
	 									>
	 										<%= PortletUtil.getGender(PortletPropsValues.USERMGT_GENDER_VALUES[g], locale)%>
	 									</aui:option>
	 								<%
	 							}
	 						}
	 					%>
	 				</aui:select>
				</aui:row>
				
				<aui:row>
					<aui:input 
						name="<%=CitizenDisplayTerms.CITIZEN_ADDRESS %>" 
						cssClass="input100"
						placeholder="address-place-holder"
						maxLength="500"
						type="text"
						label=""
					>
						<aui:validator name="maxLength">500</aui:validator>
						<aui:validator name="required"/>
					</aui:input>
				</aui:row>
				
				<aui:row>
				
					<c:choose>
						<c:when test="<%=!showLabelTaglibDatamgt %>">
							<datamgt:ddr 
								cssClass="input100"
								depthLevel="3" 
								dictCollectionCode="ADMINISTRATIVE_REGION"
								itemNames="cityId,districtId,wardId"
								itemsEmptyOption="true,true,true"
								displayStyle="vertical"
								emptyOptionLabels="cityId,districtId,wardId"
								showLabel="false"
								selectedItems="<%= StringUtil.merge(cdw, StringPool.COMMA) %>"
							/>	
						</c:when>
						<c:otherwise>
							<datamgt:ddr 
								cssClass="input100"
								depthLevel="3" 
								dictCollectionCode="ADMINISTRATIVE_REGION"
								itemNames="cityId,districtId,wardId"
								itemsEmptyOption="true,true,true"
								displayStyle="vertical"
								showLabel="true"
								selectedItems="<%= StringUtil.merge(cdw, StringPool.COMMA) %>"
							/>
						</c:otherwise>
					</c:choose>
				</aui:row>
				<div class="term-user">
					<aui:row>
						<liferay-portlet:renderURL var="linkToPage" windowState="<%=LiferayWindowState.EXCLUSIVE.toString() %>">
							<liferay-portlet:param name="mvcPath" value="/html/portlets/accountmgt/registration/termOfUse.jsp"/>
						</liferay-portlet:renderURL>
						<aui:input name="linkToPageURL" value="<%=linkToPage %>" type="hidden"></aui:input>
						<%
							String chiTiet = StringPool.BLANK;
							String popupURL = renderResponse.getNamespace() +  "openDialogTermOfUse();";
							chiTiet =  "<a href=\"http://dichvucong.mt.gov.vn/-ieu-khoan-su-dung\" target=\"_blank\" class=\"detail-terms-links\">"+LanguageUtil.get(pageContext, "term-detail")+"</a>";
						%>
						<aui:input 
							name="termsOfUse"
							type="checkbox" 
							label='<%= LanguageUtil.get(themeDisplay.getLocale(), "terms-of-use") %>'
						>
							<a href="javascript:void(0)"  onclick="<%=popupURL%>" class="detail-terms-links"><%=LanguageUtil.get(themeDisplay.getLocale(), "term-detail")%></a>
						</aui:input>
						
						
					</aui:row>
				</div>
			</div>
			<aui:row>
				<aui:button name="register" type="submit" value="register" disabled="true" />
			</aui:row>
		</div>
		
	</aui:form>
</div>

<aui:script use="liferay-portlet-url">
	AUI().ready(function(A) {
		var termsOfUseCheckbox = A.one('#<portlet:namespace />termsOfUseCheckbox');
		var allRadios = A.all( "input[type='radio']" );
		
		var typeValue =  A.one("input[name=<portlet:namespace/>typeOfRegister]:checked").get("value");
				
		allRadios.on( 'change', function(e) {
			var selectedRadioButtonValue = e.currentTarget.get('value');
			var url =Liferay.PortletURL.createRenderURL(); 
			url.setPortletId("<%= themeDisplay.getPortletDisplay().getId() %>");
			url.setWindowState('normal');
			url.setParameter("mvcPath", "/html/portlets/accountmgt/registration/registration.jsp");
			url.setParameter("type", selectedRadioButtonValue);
			
			window.location.href = url.toString();
		}); 
		
		if(termsOfUseCheckbox) {
			termsOfUseCheckbox.on('click',function() {
				
				var termsOfUse = A.one('#<portlet:namespace />termsOfUse');
				
				var register = A.one('#<portlet:namespace />register');
				
				if(termsOfUse.val() == 'true'){
					register.removeClass('disabled');
					register.removeAttribute('disabled');
				}else{
					register.addClass('disabled');
					register.setAttribute('disabled' , 'true');
				}
			});
		}
	
	
	});

	Liferay.provide(window, '<portlet:namespace />registerAccount', function() {
		A = AUI();
		var register = A.one('#<portlet:namespace />register');
		var termsOfUse = A.one('#<portlet:namespace />termsOfUse');
		var checkTel = telephoneCheck();
		var checkDate = checkBirthDate();
		if(termsOfUse.val() == 'true' && checkTel == true && checkDate == true){
			submitForm(document.<portlet:namespace />fm);
		}else{
			return;
		}
	});
	
	Liferay.provide(window, '<portlet:namespace/>openDialogTermOfUse', function() {
		var A = AUI();
		var linkToPageURL = A.one('#<portlet:namespace />linkToPageURL');
		openDialog(linkToPageURL.val(), '<portlet:namespace />dieuKhoanSuDung', '<%= UnicodeLanguageUtil.get(pageContext, "dieu-khoan-su-dung") %>');
		
	},['aui-io','liferay-portlet-url']);
	
	function checkBirthDate() {
		var A = AUI();
		var birthDateDay = A.one('#<portlet:namespace /><%=CitizenDisplayTerms.BIRTH_DATE_DAY %>');
		var birthDateMonth = A.one('#<portlet:namespace /><%=CitizenDisplayTerms.BIRTH_DATE_MONTH %>');
		var birthDateYear = A.one('#<portlet:namespace /><%=CitizenDisplayTerms.BIRTH_DATE_YEAR %>');

		var birthDateReg = A.one('#<portlet:namespace /><%=CitizenDisplayTerms.CITIZEN_BIRTHDATE + "_REG" %>');

		var dateParts = birthDateReg.val().split("/");

		var result = false;
                if(dateParts.length == 1 && new Date(birthDateReg.val()) != 'Invalid Date'){
			result = true;
			birthDateDay.val(1);
			birthDateMonth.val(0)
			birthDateYear.val(birthDateReg.val());
		} else if(dateParts.length == 3){
			result = true;
			birthDateDay.val(parseInt(dateParts[0]));
			//birthDateDay.attr("value", dateParts[0]);
                        birthDateMonth.val(parseInt(dateParts[1]) - 1);
			//birthDateMonth.attr("value", dateParts[1]);
                        birthDateYear.val(parseInt(dateParts[2]));
			//birthDateYear.attr("value", dateParts[2]);
			//console.log(birthDateDay);
			//console.log(birthDateMonth);
			//console.log(birthDateYear);
			//birthDate.val(dateParts[1] + '/' + dateParts[0] + '/' + dateParts[2]);
		}

		return result;
	}
	
	function telephoneCheck() {
		var numRegex = A.one("#<portlet:namespace/>telNo").val();
		
		var num = A.one("#<portlet:namespace/>telNo").val().toString().length;
		
		var temp = A.one("#<portlet:namespace/>telNo");
		var isphone = /^[+]*[(]{0,1}[0-9]{1,3}[)]{0,1}[-\s\./0-9]*$/;
		if(isphone.test(numRegex) && num>=10){
			A.one("#<portlet:namespace/>telNo").removeClass('changeDefErr');
			A.one("#<portlet:namespace/>defErr").removeClass('displayDefErr');
			A.one("#<portlet:namespace/>defErr2").removeClass('displayDefErr');
			return true;
		  }  
		  else {
			  if(num==0){return true;}
			  else {
				 if(num<10){
				  A.one("#<portlet:namespace/>telNo").addClass('changeDefErr');
				  A.one("#<portlet:namespace/>defErr").removeClass('displayDefErr');
				  A.one("#<portlet:namespace/>defErr2").addClass('displayDefErr');
				  return false;
			  } else if(!isphone.test(numRegex)) {
				  A.one("#<portlet:namespace/>telNo").addClass('changeDefErr');
				  A.one("#<portlet:namespace/>defErr").addClass('displayDefErr');
				  A.one("#<portlet:namespace/>defErr2").removeClass('displayDefErr');
			  }
				 }
			  
			  
			}
		}
 
</aui:script>
