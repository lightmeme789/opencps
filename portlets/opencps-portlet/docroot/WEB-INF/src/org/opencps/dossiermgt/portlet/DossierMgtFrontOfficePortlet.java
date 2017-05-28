/**
 * OpenCPS is the open source Core Public Services software
 * Copyright (C) 2016-present OpenCPS community

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package org.opencps.dossiermgt.portlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.opencps.accountmgt.NoSuchAccountException;
import org.opencps.accountmgt.NoSuchAccountFolderException;
import org.opencps.accountmgt.NoSuchAccountOwnOrgIdException;
import org.opencps.accountmgt.NoSuchAccountOwnUserIdException;
import org.opencps.accountmgt.NoSuchAccountTypeException;
import org.opencps.accountmgt.model.Business;
import org.opencps.accountmgt.model.Citizen;
import org.opencps.backend.message.UserActionMsg;
import org.opencps.backend.util.BackendUtils;
import org.opencps.datamgt.model.DictCollection;
import org.opencps.datamgt.model.DictItem;
import org.opencps.datamgt.service.DictCollectionLocalServiceUtil;
import org.opencps.datamgt.service.DictItemLocalServiceUtil;
import org.opencps.dossiermgt.CreateDossierFolderException;
import org.opencps.dossiermgt.DuplicateFileGroupException;
import org.opencps.dossiermgt.EmptyDossierAddressException;
import org.opencps.dossiermgt.EmptyDossierCityCodeException;
import org.opencps.dossiermgt.EmptyDossierContactNameException;
import org.opencps.dossiermgt.EmptyDossierDistrictCodeException;
import org.opencps.dossiermgt.EmptyDossierFileException;
import org.opencps.dossiermgt.EmptyDossierSubjectIdException;
import org.opencps.dossiermgt.EmptyDossierSubjectNameException;
import org.opencps.dossiermgt.EmptyDossierWardCodeException;
import org.opencps.dossiermgt.EmptyFileGroupException;
import org.opencps.dossiermgt.InvalidDossierObjectException;
import org.opencps.dossiermgt.NoSuchDossierException;
import org.opencps.dossiermgt.NoSuchDossierFileException;
import org.opencps.dossiermgt.NoSuchDossierPartException;
import org.opencps.dossiermgt.NoSuchDossierTemplateException;
import org.opencps.dossiermgt.OutOfLengthDossierAddressException;
import org.opencps.dossiermgt.OutOfLengthDossierContactEmailException;
import org.opencps.dossiermgt.OutOfLengthDossierContactNameException;
import org.opencps.dossiermgt.OutOfLengthDossierContactTelNoException;
import org.opencps.dossiermgt.OutOfLengthDossierSubjectIdException;
import org.opencps.dossiermgt.OutOfLengthDossierSubjectNameException;
import org.opencps.dossiermgt.PermissionDossierException;
import org.opencps.dossiermgt.RequiredDossierPartException;
import org.opencps.dossiermgt.bean.AccountBean;
import org.opencps.dossiermgt.model.Dossier;
import org.opencps.dossiermgt.model.DossierFile;
import org.opencps.dossiermgt.model.DossierPart;
import org.opencps.dossiermgt.model.DossierTemplate;
import org.opencps.dossiermgt.model.ServiceConfig;
import org.opencps.dossiermgt.search.DossierDisplayTerms;
import org.opencps.dossiermgt.search.DossierFileDisplayTerms;
import org.opencps.dossiermgt.service.DossierFileLocalServiceUtil;
import org.opencps.dossiermgt.service.DossierFileLogLocalServiceUtil;
import org.opencps.dossiermgt.service.DossierLocalServiceUtil;
import org.opencps.dossiermgt.service.DossierLogLocalServiceUtil;
import org.opencps.dossiermgt.service.DossierPartLocalServiceUtil;
import org.opencps.dossiermgt.service.DossierTemplateLocalServiceUtil;
import org.opencps.dossiermgt.service.FileGroupLocalServiceUtil;
import org.opencps.dossiermgt.service.ServiceConfigLocalServiceUtil;
import org.opencps.dossiermgt.util.ActorBean;
import org.opencps.dossiermgt.util.DossierMgtUtil;
import org.opencps.jasperreport.util.JRReportUtil;
import org.opencps.jasperreport.util.JRReportUtil.DocType;
import org.opencps.processmgt.model.ProcessStep;
import org.opencps.processmgt.util.ReportUtils;
import org.opencps.servicemgt.model.ServiceInfo;
import org.opencps.servicemgt.service.ServiceInfoLocalServiceUtil;
import org.opencps.util.AccountUtil;
import org.opencps.util.DLFileEntryUtil;
import org.opencps.util.DLFolderUtil;
import org.opencps.util.DateTimeUtil;
import org.opencps.util.MessageKeys;
import org.opencps.util.PortletConstants;
import org.opencps.util.PortletConstants.FileSizeUnit;
import org.opencps.util.PortletPropsValues;
import org.opencps.util.PortletUtil;
import org.opencps.util.PortletUtil.SplitDate;
import org.opencps.util.SignatureUtil;
import org.opencps.util.WebKeys;

import com.liferay.portal.RolePermissionsException;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.servlet.PortalSessionContext;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.documentlibrary.DuplicateFileException;
import com.liferay.portlet.documentlibrary.DuplicateFolderNameException;
import com.liferay.portlet.documentlibrary.FileExtensionException;
import com.liferay.portlet.documentlibrary.FileSizeException;
import com.liferay.portlet.documentlibrary.NoSuchFileEntryException;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLAppServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * @author trungnt
 */
public class DossierMgtFrontOfficePortlet extends MVCPortlet {

	/**
	 * @param resourceRequest
	 * @param resourceResponse
	 * @throws IOException
	 */
	public void serveResource(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException {
		
		int functionCase = ParamUtil.getInteger(resourceRequest, "functionCase");
		
		switch (functionCase) {
		case PortletConstants.SIGNATURE_REQUEST_DATA:
			signatureDataRequest(resourceRequest, resourceResponse);
			break;
		case PortletConstants.SIGNATURE_UPDATE_DATA_AFTER_SIGN:
			signatureUpdateData(resourceRequest, resourceResponse);
			break;

		default:
			break;
		}

	}
	
	private void signatureDataRequest(ResourceRequest resourceRequest, ResourceResponse resourceResponse) {
		//to do something.
		long dossierFileId = ParamUtil
				.getLong(resourceRequest, "dossierFileId");
		// String imgSrcName = ParamUtil.getString(resourceRequest, "imgSrcName");
		try {
			DLFileEntry dlFileEntry = getDLFileFromDossierFile(dossierFileId);
			InputStream is = dlFileEntry.getContentStream();
			// String condauImageSrc = ReportUtils.getTemplateReportFilePath(resourceRequest) + "resources/" + imgSrcName + "_condau.png";
			
			byte [] bytes = IOUtils.toByteArray(is);
		//	byte[] byteArray = Files.readAllBytes(new File(condauImageSrc).toPath());
			
		//	String imgContentBase64Str = Base64.encode(byteArray);
			String base64ContentString = Base64.encode(bytes);
			String fileName = dlFileEntry.getTitle();
			JSONObject jsonResponse = JSONFactoryUtil.createJSONObject();
			jsonResponse.put("base64ContentString", base64ContentString);
			jsonResponse.put("fileName", fileName);
		//	jsonResponse.put("condauImageSrc", condauImageSrc);
		//	jsonResponse.put("imgContentBase64Str", imgContentBase64Str);
			PrintWriter out = resourceResponse.getWriter();
			out.print(jsonResponse.toString());
		} catch (Exception e) {
			_log.equals(e);
		}
	}
	
	/**
	 * @param resourceRequest
	 * @param resourceResponse
	 */
	private void signatureUpdateData(ResourceRequest resourceRequest, ResourceResponse resourceResponse) {
		String dataSigned = ParamUtil.getString(resourceRequest, "dataSigned");
		long dossierFileId = ParamUtil
				.getLong(resourceRequest, "dossierFileId");
		try {

			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(resourceRequest);
			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);
			
			if (Validator.isNotNull(dataSigned)) {

				byte [] bytes = Base64.decode(dataSigned);
				
				updateFileSigned(dossierFileId, bytes, serviceContext);

				JSONObject jsonResponse = JSONFactoryUtil.createJSONObject();
				jsonResponse.put("msg", "success");
				// jsonResponse.put("fileName", fileEntry.getTitle());

				PrintWriter out = resourceResponse.getWriter();
				out.print(jsonResponse.toString());
			}
		} catch (Exception e) {
			_log.error(e);
		}
	}
	
	/**
	 * @param dossierFileId
	 * @param bytes
	 * @param serviceContext
	 * @throws Exception 
	 */
	private void updateFileSigned(long dossierFileId, byte[] bytes,
			ServiceContext serviceContext) throws Exception {
		
		// DLFileEntry dlFileEntry = getDLFileFromDossierFile(dossierFileId);
		
		DossierFile dossierFile = null;
		
		dossierFile = DossierFileLocalServiceUtil
				.getDossierFile(dossierFileId);
		
		DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil
				.getDLFileEntry(dossierFile.getFileEntryId());
		
		File file = null;
		
		file = FileUtil.createTempFile(bytes);
		
		if(Validator.isNotNull(file) && Validator.isNotNull(dlFileEntry.getTitle())) {
		
			int signCheck = SignatureUtil.getSignCheck(file.getPath(), FileUtil.getExtension(dlFileEntry.getTitle()));
			
			String signInfo = SignatureUtil.getSignInfo(file.getPath(), FileUtil.getExtension(dlFileEntry.getTitle()));

			dossierFile.setSignCheck(signCheck);
			
			dossierFile.setSignInfo(signInfo);
			
			DossierFileLocalServiceUtil.updateDossierFile(dossierFile);
		}
		
		FileUtil.delete(file);
		
		DLAppServiceUtil.updateFileEntry(dlFileEntry.getFileEntryId(), dlFileEntry.getTitle(), dlFileEntry.getMimeType(),
				dlFileEntry.getTitle(), dlFileEntry.getDescription(), StringPool.BLANK, false, bytes, serviceContext);
		
	}
	
	private DLFileEntry getDLFileFromDossierFile(long dossierFileId)
			throws PortalException, SystemException {
		
		DossierFile dossierFile = null;
		
		dossierFile = DossierFileLocalServiceUtil
				.getDossierFile(dossierFileId);

		 return DLFileEntryLocalServiceUtil
				.getDLFileEntry(dossierFile.getFileEntryId());
		
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void addAttachmentFile(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException, Exception {
		
		int functionCase = ParamUtil.getInteger(actionRequest, "functionCase");
		switch (functionCase) {
		case PortletConstants.SIGNATURE_REQUEST_DATA:
			requestDataSignature(actionRequest, actionResponse);
			break;
		case PortletConstants.SIGNATURE_UPDATE_DATA_AFTER_SIGN:
			addFileAfterSign(actionRequest, actionResponse);
			break;
		default:
			addAttachmentFileDefault(actionRequest, actionResponse);
			break;
		}
	}
	
	private void addAttachmentFileDefault(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException, Exception {

		AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

		UploadPortletRequest uploadPortletRequest = PortalUtil
				.getUploadPortletRequest(actionRequest);

		Dossier dossier = null;
		DossierFile dossierFile = null;
		DossierPart dossierPart = null;

		boolean updated = false;

		long dossierId = ParamUtil.getLong(uploadPortletRequest,
				DossierDisplayTerms.DOSSIER_ID);

		long dossierFileId = ParamUtil.getLong(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);

		long dossierPartId = ParamUtil.getLong(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);

		long groupDossierPartId = ParamUtil.getLong(uploadPortletRequest,
				"groupDossierPartId");

		long fileGroupId = ParamUtil.getLong(uploadPortletRequest,
				DossierDisplayTerms.FILE_GROUP_ID);

		long size = uploadPortletRequest
				.getSize(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

		int dossierFileType = ParamUtil.getInteger(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_TYPE);

		int dossierFileOriginal = ParamUtil.getInteger(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ORIGINAL);

		String groupName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.GROUP_NAME);

		String displayName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DISPLAY_NAME);

		String dossierFileNo = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_NO);

		String dossierFileDate = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_DATE);

		String sourceFileName = uploadPortletRequest
				.getFileName(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

		String fileTypes = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.FILE_TYPES);

		float maxUploadFileSize = ParamUtil.getFloat(uploadPortletRequest,
				DossierFileDisplayTerms.MAX_UPLOAD_FILE_SIZE);

		String maxUploadFileSizeUnit = ParamUtil.getString(
				uploadPortletRequest,
				DossierFileDisplayTerms.MAX_UPLOAD_FILE_SIZE_UNIT);

		float maxTotalUploadFileSize = ParamUtil.getFloat(uploadPortletRequest,
				DossierFileDisplayTerms.MAX_TOTAL_UPLOAD_FILE_SIZE);

		String maxTotalUploadFileSizeUnit = ParamUtil.getString(
				uploadPortletRequest,
				DossierFileDisplayTerms.MAX_TOTAL_UPLOAD_FILE_SIZE_UNIT);

		/*
		 * sourceFileName = sourceFileName
		 * .concat(PortletConstants.TEMP_RANDOM_SUFFIX).concat(StringUtil
		 * .randomString());
		 */

		String redirectURL = ParamUtil.getString(uploadPortletRequest,
				"redirectURL");

		InputStream inputStream = null;

		Date fileDate = null;

		if (Validator.isNotNull(dossierFileDate)) {
			fileDate = DateTimeUtil.convertStringToDate(dossierFileDate);
		}

		try {
			inputStream = uploadPortletRequest
					.getFileAsStream(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

			validateAddAttachDossierFile(dossierId, dossierPartId,
					dossierFileId, displayName, size, sourceFileName,
					inputStream, accountBean, fileTypes, maxUploadFileSize,
					maxUploadFileSizeUnit, maxTotalUploadFileSize,
					maxTotalUploadFileSizeUnit);

			String filePath = uploadPortletRequest.getFile(
					DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD).getPath();

			File file = new File(filePath);

			System.out.println(file.getPath());

			String extension = FileUtil.getExtension(sourceFileName);

			String signInfo = SignatureUtil.getSignInfo(filePath, extension);

			int signCheck = SignatureUtil.getSignCheck(filePath, extension);

			SignatureUtil.getSignInfo(filePath, extension);

			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(uploadPortletRequest);

			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);

			dossier = DossierLocalServiceUtil.getDossier(dossierId);

			if (dossierFileId > 0) {
				dossierFile = DossierFileLocalServiceUtil
						.getDossierFile(dossierFileId);
			}

			dossierPart = DossierPartLocalServiceUtil
					.getDossierPart(dossierPartId);

			String contentType = uploadPortletRequest
					.getContentType(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

			DossierFile dossierFileAdded = DossierFileLocalServiceUtil
					.addDossierFile(serviceContext.getUserId(), dossierId,
							dossierPartId, dossierPart.getTemplateFileNo(),
							groupName, fileGroupId, groupDossierPartId,
							accountBean.getOwnerUserId(), accountBean
									.getOwnerOrganizationId(), displayName,
							StringPool.BLANK,
							dossierFile != null ? dossierFile.getFileEntryId()
									: 0,
							PortletConstants.DOSSIER_FILE_MARK_UNKNOW,
							dossierFileType, dossierFileNo, fileDate,
							dossierFileOriginal,
							PortletConstants.DOSSIER_FILE_SYNC_STATUS_NOSYNC,
							dossier.getFolderId(), sourceFileName, contentType,
							displayName, StringPool.BLANK, StringPool.BLANK,
							inputStream, size, signCheck, signInfo,
							serviceContext);

			int actor = 0;

			if (accountBean.isEmployee()) {
				actor = 2;
			} else if (accountBean.isBusiness() || accountBean.isCitizen()) {
				actor = 1;
			}

			ActorBean actorBean = new ActorBean(actor,
					serviceContext.getUserId());

			ProcessStep processStep = BackendUtils
					.getProcessStepByDossierId(dossierId);

			DossierFileLogLocalServiceUtil.addFileLog(
					serviceContext.getUserId(),
					actorBean.getActorName(),
					dossierId,
					fileGroupId,
					processStep.getProcessStepId(),
					false,
					displayName,
					1,
					StringPool.BLANK,
					PortletConstants.DOSSIER_FILE_ADD,
					dossierFileAdded != null ? dossierFileAdded
							.getFileEntryId() : 0, WebKeys.ACTOR_CITIZEN);

			// Add DossierLog for Add File
			/*
			 * int actor = 0;
			 * 
			 * if (accountBean.isEmployee()) { actor = 2; } else if
			 * (accountBean.isBusiness() || accountBean.isCitizen()) { actor =
			 * 1; }
			 * 
			 * ActorBean actorBean = new ActorBean(actor,
			 * serviceContext.getUserId());
			 * 
			 * DossierLogLocalServiceUtil.addDossierLog(
			 * serviceContext.getUserId(), serviceContext.getScopeGroupId(),
			 * serviceContext.getCompanyId(), dossierId, fileGroupId,
			 * dossier.getDossierStatus(),
			 * PortletConstants.DOSSIER_ACTION_ADD_ATTACHMENT_FILE,
			 * PortletConstants.DOSSIER_ACTION_ADD_ATTACHMENT_FILE +
			 * StringPool.SPACE + StringPool.COLON + StringPool.SPACE +
			 * displayName, new Date(), 0, 0, actorBean.getActor(),
			 * actorBean.getActorId(), actorBean.getActorName(),
			 * ProcessOrderPortlet.class.getName() + ".addAttachmentFile()");
			 */
			updated = true;

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

			SessionErrors.clear(actionRequest);
			
			FileUtil.delete(file);
		} catch (Exception e) {
			updated = false;
			if (e instanceof DuplicateFileException) {
				SessionErrors.add(actionRequest, DuplicateFileException.class);
			} else if (e instanceof NoSuchDossierException) {
				SessionErrors.add(actionRequest, NoSuchDossierException.class);
			} else if (e instanceof NoSuchDossierPartException) {
				SessionErrors.add(actionRequest,
						NoSuchDossierPartException.class);
			} else if (e instanceof NoSuchAccountException) {
				SessionErrors.add(actionRequest, NoSuchAccountException.class);
			} else if (e instanceof NoSuchAccountTypeException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountTypeException.class);
			} else if (e instanceof NoSuchAccountFolderException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountFolderException.class);
			} else if (e instanceof NoSuchAccountOwnUserIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnUserIdException.class);
			} else if (e instanceof NoSuchAccountOwnOrgIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnOrgIdException.class);
			} else if (e instanceof PermissionDossierException) {
				SessionErrors.add(actionRequest,
						PermissionDossierException.class);
			} else if (e instanceof FileSizeException) {
				SessionErrors.add(actionRequest, FileSizeException.class);
			} else if (e instanceof FileExtensionException) {
				SessionErrors.add(actionRequest, FileExtensionException.class);
			} else {
				SessionErrors.add(actionRequest, "upload-error");

			}
			_log.error(e);

		} finally {
			if (updated) {
				if (Validator.isNotNull(redirectURL)) {
					actionResponse.sendRedirect(redirectURL);
				}
			} else {
				actionResponse.setRenderParameter("redirectURL", redirectURL);
				actionResponse.setRenderParameter("content", "upload-file");
				actionResponse
						.setRenderParameter("jspPage",
								"/html/portlets/dossiermgt/frontoffice/modal_dialog.jsp");
			}
		}
	}
	@Deprecated
	private void requestDataSignature (ActionRequest actionRequest, ActionResponse actionResponse) {
		
		String imgSrcName = ParamUtil.getString(actionRequest, "imgSrcName");
		String condauImageSrc = ReportUtils.getTemplateReportFilePath(actionRequest) + "resources/" + imgSrcName + "_condau.png";
		try {
				byte[] imageBytes = Files.readAllBytes(new File(condauImageSrc).toPath());
				String imageBase64Encode = Base64.encode(imageBytes);
				
				JSONObject jsonResponse = JSONFactoryUtil.createJSONObject();
				
				jsonResponse.put("condauImageSrc", condauImageSrc);
				jsonResponse.put("imageBase64Encode", imageBase64Encode);
				
				/*HttpServletResponse response = PortalUtil.getHttpServletResponse(actionResponse);
				PrintWriter out = response.getWriter();
				out.print(jsonResponse.toString());*/
				
				PortletUtil.writeJSON(actionRequest, actionResponse, jsonResponse);
		} catch (Exception e) {
			_log.error(e);
		}
	}
	
	private void addFileAfterSign(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException {
		AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);
		
		Dossier dossier = null;
		DossierFile dossierFile = null;
		DossierPart dossierPart = null;
		
		UploadPortletRequest uploadPortletRequest = PortalUtil
				.getUploadPortletRequest(actionRequest);
		
		boolean updated = false;

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);

		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);

		long dossierPartId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);

		long groupDossierPartId = ParamUtil.getLong(actionRequest,
				"groupDossierPartId");

		long fileGroupId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.FILE_GROUP_ID);
		
		String signedFileBase64 = ParamUtil.getString(uploadPortletRequest, "dossierFileSigned");

		int dossierFileType = ParamUtil.getInteger(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_TYPE);

		int dossierFileOriginal = ParamUtil.getInteger(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ORIGINAL);

		String groupName = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.GROUP_NAME);

		String displayName = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.DISPLAY_NAME);

		String dossierFileNo = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_NO);

		String dossierFileDate = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_DATE);

		String sourceFileName = ParamUtil.getString(actionRequest, "signatureFileName");

		String fileTypes = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.FILE_TYPES);

		float maxUploadFileSize = ParamUtil.getFloat(actionRequest,
				DossierFileDisplayTerms.MAX_UPLOAD_FILE_SIZE);

		String maxUploadFileSizeUnit = ParamUtil.getString(
				actionRequest,
				DossierFileDisplayTerms.MAX_UPLOAD_FILE_SIZE_UNIT);

		float maxTotalUploadFileSize = ParamUtil.getFloat(actionRequest,
				DossierFileDisplayTerms.MAX_TOTAL_UPLOAD_FILE_SIZE);

		String maxTotalUploadFileSizeUnit = ParamUtil.getString(
				actionRequest,
				DossierFileDisplayTerms.MAX_TOTAL_UPLOAD_FILE_SIZE_UNIT);

		/*
		 * sourceFileName = sourceFileName
		 * .concat(PortletConstants.TEMP_RANDOM_SUFFIX).concat(StringUtil
		 * .randomString());
		 */

		String redirectURL = ParamUtil.getString(actionRequest,
				"redirectURL");

		InputStream inputStream = null;

		Date fileDate = null;

		if (Validator.isNotNull(dossierFileDate)) {
			fileDate = DateTimeUtil.convertStringToDate(dossierFileDate);
		}

		try {
			
			if(Validator.isNotNull(signedFileBase64)) {
				byte[] signedFilebytes = Base64.decode(signedFileBase64);
				File file = null;
				file = FileUtil.createTempFile(signedFilebytes);
				
				if(Validator.isNotNull(file) && Validator.isNotNull(sourceFileName)) {
					int signCheck = SignatureUtil.getSignCheck(file.getPath(), FileUtil.getExtension(sourceFileName));
					String signInfo = SignatureUtil.getSignInfo(file.getPath(), FileUtil.getExtension(sourceFileName));
					int size = (int) (file.length()/1024);
					inputStream = new ByteArrayInputStream(signedFilebytes);

					validateAddAttachDossierFile(dossierId, dossierPartId,
							dossierFileId, displayName, size, sourceFileName,
							inputStream, accountBean, fileTypes, maxUploadFileSize,
							maxUploadFileSizeUnit, maxTotalUploadFileSize,
							maxTotalUploadFileSizeUnit);

					ServiceContext serviceContext = ServiceContextFactory
							.getInstance(actionRequest);

					serviceContext.setAddGroupPermissions(true);
					serviceContext.setAddGuestPermissions(true);

					dossier = DossierLocalServiceUtil.getDossier(dossierId);

					if (dossierFileId > 0) {
						dossierFile = DossierFileLocalServiceUtil
								.getDossierFile(dossierFileId);
					}

					dossierPart = DossierPartLocalServiceUtil
							.getDossierPart(dossierPartId);
					
					String contentType = PortletUtil.getContentTypeFromFile(file);

					DossierFile dossierFileAdded = DossierFileLocalServiceUtil
							.addDossierFile(serviceContext.getUserId(), dossierId,
									dossierPartId, dossierPart.getTemplateFileNo(),
									groupName, fileGroupId, groupDossierPartId,
									accountBean.getOwnerUserId(), accountBean
											.getOwnerOrganizationId(), displayName,
									StringPool.BLANK,
									dossierFile != null ? dossierFile.getFileEntryId()
											: 0,
									PortletConstants.DOSSIER_FILE_MARK_UNKNOW,
									dossierFileType, dossierFileNo, fileDate,
									dossierFileOriginal,
									PortletConstants.DOSSIER_FILE_SYNC_STATUS_NOSYNC,
									dossier.getFolderId(), sourceFileName, contentType,
									displayName, StringPool.BLANK, StringPool.BLANK,
									inputStream, size, signCheck, signInfo,
									serviceContext);

					int actor = 0;

					if (accountBean.isEmployee()) {
						actor = 2;
					} else if (accountBean.isBusiness() || accountBean.isCitizen()) {
						actor = 1;
					}

					ActorBean actorBean = new ActorBean(actor,
							serviceContext.getUserId());

					ProcessStep processStep = BackendUtils
							.getProcessStepByDossierId(dossierId);

					DossierFileLogLocalServiceUtil.addFileLog(
							serviceContext.getUserId(),
							actorBean.getActorName(),
							dossierId,
							fileGroupId,
							processStep.getProcessStepId(),
							false,
							displayName,
							1,
							StringPool.BLANK,
							PortletConstants.DOSSIER_FILE_ADD,
							dossierFileAdded != null ? dossierFileAdded
									.getFileEntryId() : 0, WebKeys.ACTOR_CITIZEN);

					// Add DossierLog for Add File
					/*
					 * int actor = 0;
					 * 
					 * if (accountBean.isEmployee()) { actor = 2; } else if
					 * (accountBean.isBusiness() || accountBean.isCitizen()) { actor =
					 * 1; }
					 * 
					 * ActorBean actorBean = new ActorBean(actor,
					 * serviceContext.getUserId());
					 * 
					 * DossierLogLocalServiceUtil.addDossierLog(
					 * serviceContext.getUserId(), serviceContext.getScopeGroupId(),
					 * serviceContext.getCompanyId(), dossierId, fileGroupId,
					 * dossier.getDossierStatus(),
					 * PortletConstants.DOSSIER_ACTION_ADD_ATTACHMENT_FILE,
					 * PortletConstants.DOSSIER_ACTION_ADD_ATTACHMENT_FILE +
					 * StringPool.SPACE + StringPool.COLON + StringPool.SPACE +
					 * displayName, new Date(), 0, 0, actorBean.getActor(),
					 * actorBean.getActorId(), actorBean.getActorName(),
					 * ProcessOrderPortlet.class.getName() + ".addAttachmentFile()");
					 */
					updated = true;

					SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

					SessionErrors.clear(actionRequest);
				}
				
			}
			
		} catch (Exception e) {
			updated = false;
			if (e instanceof DuplicateFileException) {
				SessionErrors.add(actionRequest, DuplicateFileException.class);
			} else if (e instanceof NoSuchDossierException) {
				SessionErrors.add(actionRequest, NoSuchDossierException.class);
			} else if (e instanceof NoSuchDossierPartException) {
				SessionErrors.add(actionRequest,
						NoSuchDossierPartException.class);
			} else if (e instanceof NoSuchAccountException) {
				SessionErrors.add(actionRequest, NoSuchAccountException.class);
			} else if (e instanceof NoSuchAccountTypeException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountTypeException.class);
			} else if (e instanceof NoSuchAccountFolderException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountFolderException.class);
			} else if (e instanceof NoSuchAccountOwnUserIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnUserIdException.class);
			} else if (e instanceof NoSuchAccountOwnOrgIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnOrgIdException.class);
			} else if (e instanceof PermissionDossierException) {
				SessionErrors.add(actionRequest,
						PermissionDossierException.class);
			} else if (e instanceof FileSizeException) {
				SessionErrors.add(actionRequest, FileSizeException.class);
			} else if (e instanceof FileExtensionException) {
				SessionErrors.add(actionRequest, FileExtensionException.class);
			} else {
				SessionErrors.add(actionRequest, "upload-error");

			}
			_log.error(e);

		} finally {
			if (updated) {
				if (Validator.isNotNull(redirectURL)) {
					actionResponse.sendRedirect(redirectURL);
				}
			} else {
				actionResponse.setRenderParameter("redirectURL", redirectURL);
				actionResponse.setRenderParameter("content", "upload-file");
				actionResponse
						.setRenderParameter("jspPage",
								"/html/portlets/dossiermgt/frontoffice/modal_dialog.jsp");

			}
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void addIndividualPartGroup(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		boolean updated = false;

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);

		long dossierPartId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);

		String partName = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.PART_NAME);

		String redirectURL = ParamUtil.getString(actionRequest, "redirectURL");

		try {
			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);
			valiadateFileGroup(dossierId, partName);
			FileGroupLocalServiceUtil.addFileGroup(serviceContext.getUserId(),
					dossierId, dossierPartId, partName,
					PortletConstants.DOSSIER_FILE_SYNC_STATUS_NOSYNC,
					serviceContext);

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);
		} catch (Exception e) {
			updated = false;

			if (e instanceof NoSuchDossierException) {
				SessionErrors.add(actionRequest, NoSuchDossierException.class);
			} else if (e instanceof EmptyFileGroupException) {
				SessionErrors.add(actionRequest, EmptyFileGroupException.class);
			} else if (e instanceof DuplicateFileGroupException) {
				SessionErrors.add(actionRequest,
						DuplicateFileGroupException.class);
			} else {
				SessionErrors.add(actionRequest,
						MessageKeys.DOSSIER_SYSTEM_EXCEPTION_OCCURRED);
			}

			_log.error(e);
		} finally {
			if (updated) {
				if (Validator.isNotNull(redirectURL)) {
					actionResponse.sendRedirect(redirectURL);
				}
			} else {
				actionResponse.setRenderParameter("redirectURL", redirectURL);
				actionResponse.setRenderParameter("content", "individual");
				actionResponse
						.setRenderParameter("jspPage",
								"/html/portlets/dossiermgt/frontoffice/modal_dialog.jsp");

			}
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 */
	public void addTempFile(ActionRequest actionRequest,
			ActionResponse actionResponse) {

		UploadPortletRequest uploadPortletRequest = PortalUtil
				.getUploadPortletRequest(actionRequest);

		long folderId = ParamUtil.getLong(uploadPortletRequest,
				DossierFileDisplayTerms.FOLDE_ID);

		long dossierPartId = ParamUtil.getLong(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);

		int index = ParamUtil.getInteger(uploadPortletRequest,
				DossierFileDisplayTerms.INDEX);

		int level = ParamUtil.getInteger(uploadPortletRequest,
				DossierFileDisplayTerms.LEVEL);

		String groupName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.GROUP_NAME);

		String templateFileNo = ParamUtil.getString(uploadPortletRequest,
				DossierDisplayTerms.TEMPLATE_FILE_NO);

		String fileName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.FILE_NAME);

		String partType = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.PART_TYPE);

		String tempFolderName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.TEMP_FOLDER_NAME);

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		String redirectURL = ParamUtil.getString(uploadPortletRequest,
				"redirectURL");

		String displayName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DISPLAY_NAME);

		String dossierFileNo = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_NO);

		String dossierFileDate = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_DATE);

		String sourceFileName = uploadPortletRequest
				.getFileName(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

		sourceFileName = sourceFileName.concat(
				PortletConstants.TEMP_RANDOM_SUFFIX).concat(
				StringUtil.randomString());

		InputStream inputStream = null;

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		try {

			inputStream = uploadPortletRequest
					.getFileAsStream(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

			String contentType = uploadPortletRequest
					.getContentType(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

			FileEntry fileEntry = DLAppServiceUtil.addTempFileEntry(
					themeDisplay.getScopeGroupId(), folderId, sourceFileName,
					tempFolderName, inputStream, contentType);

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_FILE_NO,
					dossierFileNo);

			jsonObject.put(DossierFileDisplayTerms.DISPLAY_NAME, displayName);

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_FILE_DATE,
					dossierFileDate);

			jsonObject.put(DossierFileDisplayTerms.FILE_TITLE,
					fileEntry.getTitle());

			jsonObject.put(DossierFileDisplayTerms.MIME_TYPE,
					fileEntry.getMimeType());

			jsonObject.put(DossierFileDisplayTerms.FILE_NAME, fileName);

			jsonObject.put(DossierFileDisplayTerms.FILE_ENTRY_ID,
					fileEntry.getFileEntryId());
			jsonObject.put(DossierFileDisplayTerms.FOLDE_ID,
					fileEntry.getFolderId());

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_PART_ID,
					dossierPartId);

			jsonObject.put(DossierFileDisplayTerms.INDEX, index);

			jsonObject.put(DossierFileDisplayTerms.LEVEL, level);

			jsonObject.put(DossierFileDisplayTerms.PART_TYPE, partType);

			jsonObject.put(DossierFileDisplayTerms.GROUP_NAME, groupName);

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_FILE_ORIGINAL,
					PortletConstants.DOSSIER_FILE_ORIGINAL);

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_FILE_TYPE,
					PortletConstants.DOSSIER_FILE_TYPE_INPUT);

			jsonObject
					.put(DossierDisplayTerms.TEMPLATE_FILE_NO, templateFileNo);

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			_log.error(e);
			SessionErrors.add(actionRequest, "upload-error");
		} finally {
			StreamUtil.cleanUp(inputStream);
			HttpServletRequest request = PortalUtil
					.getHttpServletRequest(actionRequest);
			request.setAttribute(WebKeys.RESPONSE_UPLOAD_TEMP_DOSSIER_FILE,
					jsonObject);

			if (Validator.isNotNull(redirectURL)) {
				actionResponse
						.setRenderParameter("jspPage",
								"/html/portlets/dossiermgt/frontoffice/upload_dossier_file.jsp");
			}
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 * @throws SystemException
	 * @throws PortalException
	 */
	public void cancelDossier(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException, PortalException,
			SystemException {

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);

		try {
			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);

			AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

			if (accountBean != null
					&& (accountBean.isBusiness() || accountBean.isCitizen())) {

				Dossier dossier = DossierLocalServiceUtil.getDossier(dossierId);

				UserActionMsg actionMsg = new UserActionMsg();

				Message message = new Message();

				actionMsg.setAction(WebKeys.ACTION_CANCEL_VALUE);

				actionMsg.setDossierId(dossierId);

				actionMsg.setLocale(serviceContext.getLocale());

				actionMsg.setUserId(serviceContext.getUserId());

				actionMsg.setGroupId(serviceContext.getScopeGroupId());

				actionMsg.setGovAgencyCode(dossier.getGovAgencyCode());

				actionMsg.setCompanyId(dossier.getCompanyId());

				message.put("msgToEngine", actionMsg);

				SessionMessages.add(actionRequest, "cancel-dossier-success");

				// Add DossierLog for cancelDossier

				/*
				 * int actor = 0;
				 * 
				 * if (accountBean.isEmployee()) { actor = 2; } else if
				 * (accountBean.isBusiness() || accountBean.isCitizen()) { actor
				 * = 1; }
				 * 
				 * ActorBean actorBean = new ActorBean(actor,
				 * serviceContext.getUserId());
				 * 
				 * long fileGroupId = 0;
				 * 
				 * DossierLogLocalServiceUtil.addDossierLog(
				 * serviceContext.getUserId(), serviceContext.getScopeGroupId(),
				 * serviceContext.getCompanyId(), dossierId, fileGroupId,
				 * dossier.getDossierStatus(),
				 * PortletConstants.DOSSIER_ACTION_CANCEL_DOSSER_REQUEST,
				 * PortletConstants.DOSSIER_ACTION_CANCEL_DOSSER_REQUEST, new
				 * Date(), 0, 0, actorBean.getActor(), actorBean.getActorId(),
				 * actorBean.getActorName(), ProcessOrderPortlet.class.getName()
				 * + ".cancelDossier()");
				 */
				MessageBusUtil.sendMessage(
						"opencps/frontoffice/out/destination", message);
			} else {
				SessionErrors.add(actionRequest,
						"user-not-have-permission-cancel-dossier");
			}
		} catch (Exception e) {
			_log.error(e);
			SessionErrors.add(actionRequest,
					"user-not-have-permission-cancel-dossier");
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 * @throws PortalException
	 * @throws SystemException
	 */
	public void changeDossier(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException, PortalException,
			SystemException {

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);

		try {
			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);

			AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

			if (accountBean != null
					&& (accountBean.isBusiness() || accountBean.isCitizen())) {

				Dossier dossier = DossierLocalServiceUtil.getDossier(dossierId);

				UserActionMsg actionMsg = new UserActionMsg();

				Message message = new Message();

				actionMsg.setAction(WebKeys.ACTION_REPAIR_VALUE);

				actionMsg.setDossierId(dossierId);

				// actionMsg.setFileGroupId(fileGroupId);

				actionMsg.setLocale(serviceContext.getLocale());

				actionMsg.setUserId(serviceContext.getUserId());

				actionMsg.setGroupId(serviceContext.getScopeGroupId());

				actionMsg.setGovAgencyCode(dossier.getGovAgencyCode());

				actionMsg.setCompanyId(dossier.getCompanyId());

				// actionMsg.setDossierStatus(dossierStatus);

				message.put("msgToEngine", actionMsg);

				SessionMessages.add(actionRequest, "change-dossier-success");

				// Add DossierLog for cancelDossier
				/*
				 * int actor = 0;
				 * 
				 * if (accountBean.isEmployee()) { actor = 2; } else if
				 * (accountBean.isBusiness() || accountBean.isCitizen()) { actor
				 * = 1; }
				 * 
				 * ActorBean actorBean = new ActorBean(actor,
				 * serviceContext.getUserId());
				 * 
				 * long fileGroupId = 0;
				 * 
				 * DossierLogLocalServiceUtil.addDossierLog(
				 * serviceContext.getUserId(), serviceContext.getScopeGroupId(),
				 * serviceContext.getCompanyId(), dossierId, fileGroupId,
				 * dossier.getDossierStatus(),
				 * PortletConstants.DOSSIER_ACTION_CHANGE_DOSSER_REQUEST,
				 * PortletConstants.DOSSIER_ACTION_CHANGE_DOSSER_REQUEST, new
				 * Date(), 0, 0, actorBean.getActor(), actorBean.getActorId(),
				 * actorBean.getActorName(), ProcessOrderPortlet.class.getName()
				 * + ".changeDossier()");
				 */MessageBusUtil.sendMessage(
						"opencps/frontoffice/out/destination", message);

			} else {
				SessionErrors.add(actionRequest,
						"user-not-have-permission-cancel-dossier");
			}
		} catch (Exception e) {
			_log.error(e);
			SessionErrors.add(actionRequest,
					"user-not-have-permission-cancel-dossier");
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void cloneDossierFile(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		Dossier dossier = null;
		DossierFile dossierFile = null;
		DossierPart dossierPart = null;

		boolean updated = false;

		long cloneDossierFileId = ParamUtil.getLong(actionRequest,
				"cloneDossierFileId");

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);

		long dossierPartId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);

		long groupDossierPartId = ParamUtil.getLong(actionRequest,
				"groupDossierPartId");

		long fileGroupId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.FILE_GROUP_ID);

		String groupName = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.GROUP_NAME);

		String redirectURL = ParamUtil.getString(actionRequest, "redirectURL");

		try {
			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);

			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);

			dossier = DossierLocalServiceUtil.getDossier(dossierId);

			AccountBean accountBean = AccountUtil.getAccountBean(
					dossier.getUserId(), serviceContext.getScopeGroupId(),
					serviceContext);

			validateCloneDossierFile(dossierId, dossierPartId,
					cloneDossierFileId, accountBean);

			dossierFile = DossierFileLocalServiceUtil
					.getDossierFile(cloneDossierFileId);

			long fileEntryId = dossierFile.getFileEntryId();

			FileEntry fileEntry = DLAppServiceUtil.getFileEntry(fileEntryId);

			dossierPart = DossierPartLocalServiceUtil
					.getDossierPart(dossierPartId);

			DossierFileLocalServiceUtil.addDossierFile(
					serviceContext.getUserId(), dossierId, dossierPartId,
					dossierPart.getTemplateFileNo(), groupName, fileGroupId,
					groupDossierPartId, accountBean.getOwnerUserId(),
					accountBean.getOwnerOrganizationId(),
					dossierFile.getDisplayName(), StringPool.BLANK,
					dossierFile != null ? dossierFile.getFileEntryId() : 0,
					PortletConstants.DOSSIER_FILE_MARK_UNKNOW,
					dossierFile.getDossierFileType(),
					dossierFile.getDossierFileNo(),
					dossierFile.getDossierFileDate(),
					dossierFile.getOriginal(),
					PortletConstants.DOSSIER_FILE_SYNC_STATUS_NOSYNC,
					dossier.getFolderId(), fileEntry.getTitle()
							+ StringPool.PERIOD + fileEntry.getExtension(),
					fileEntry.getMimeType(), fileEntry.getTitle(),
					StringPool.BLANK, StringPool.BLANK,
					fileEntry.getContentStream(), fileEntry.getSize(),
					dossierFile.getSignCheck(), dossierFile.getSignInfo(),
					serviceContext);

			ProcessStep processStep = BackendUtils
					.getProcessStepByDossierId(dossierId);

			int actor = 0;

			if (accountBean.isEmployee()) {
				actor = 2;
			} else if (accountBean.isBusiness() || accountBean.isCitizen()) {
				actor = 1;
			}

			ActorBean actorBean = new ActorBean(actor,
					serviceContext.getUserId());

			DossierFileLogLocalServiceUtil.addFileLog(
					serviceContext.getUserId(), actorBean.getActorName(),
					dossierId, fileGroupId, processStep.getProcessStepId(),
					false, dossierFile.getDisplayName(), 1, StringPool.BLANK,
					PortletConstants.DOSSIER_FILE_DUPLICATE,
					dossierFile != null ? dossierFile.getFileEntryId() : 0,
					WebKeys.ACTOR_CITIZEN);

			/*
			 * DossierLogLocalServiceUtil.addDossierLog(
			 * serviceContext.getUserId(), serviceContext.getScopeGroupId(),
			 * serviceContext.getCompanyId(), dossierId, fileGroupId,
			 * dossier.getDossierStatus(),
			 * PortletConstants.DOSSIER_ACTION_CLONE_ATTACHMENT_FILE,
			 * PortletConstants.DOSSIER_ACTION_CLONE_ATTACHMENT_FILE +
			 * StringPool.SPACE + StringPool.COLON + StringPool.SPACE +
			 * dossierFile.getDisplayName(), new Date(), 0, 0,
			 * actorBean.getActor(), actorBean.getActorId(),
			 * actorBean.getActorName(), ProcessOrderPortlet.class.getName() +
			 * ".cloneDossierFile()");
			 */

			updated = true;

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);
		} catch (Exception e) {
			updated = false;
			if (e instanceof DuplicateFileException) {
				SessionErrors.add(actionRequest, DuplicateFileException.class);
			} else if (e instanceof NoSuchDossierException) {
				SessionErrors.add(actionRequest, NoSuchDossierException.class);
			} else if (e instanceof NoSuchDossierPartException) {
				SessionErrors.add(actionRequest,
						NoSuchDossierPartException.class);
			} else if (e instanceof NoSuchAccountException) {
				SessionErrors.add(actionRequest, NoSuchAccountException.class);
			} else if (e instanceof NoSuchAccountTypeException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountTypeException.class);
			} else if (e instanceof NoSuchAccountFolderException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountFolderException.class);
			} else if (e instanceof NoSuchAccountOwnUserIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnUserIdException.class);
			} else if (e instanceof NoSuchAccountOwnOrgIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnOrgIdException.class);
			} else if (e instanceof PermissionDossierException) {
				SessionErrors.add(actionRequest,
						PermissionDossierException.class);
			} else if (e instanceof NoSuchFileEntryException) {
				SessionErrors
						.add(actionRequest, NoSuchFileEntryException.class);
			} else {
				SessionErrors.add(actionRequest, "upload-error");

			}
			_log.error(e);

		} finally {
			if (updated) {
				if (Validator.isNotNull(redirectURL)) {
					actionResponse.sendRedirect(redirectURL);
				}
			} else {
				actionResponse.setRenderParameter("redirectURL", redirectURL);
				actionResponse.setRenderParameter("content", "upload-file");
				actionResponse.setRenderParameter("tab1", "select-file");
				actionResponse
						.setRenderParameter("jspPage",
								"/html/portlets/dossiermgt/frontoffice/modal_dialog.jsp");

			}
		}

	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void createReport(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);

		String sourceFileName = StringPool.BLANK;

		InputStream inputStream = null;

		File file = null;

		JSONObject responseJSON = JSONFactoryUtil.createJSONObject();

		String fileExportDir = StringPool.BLANK;

		String fileExtension = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.FILE_EXTENSION,
				StringUtil.lowerCase(JRReportUtil.DocType.PDF.getValue()));

		try {
			validateCreateDynamicForm(dossierFileId, accountBean);

			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);
			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);

			// Get dossier file
			DossierFile dossierFile = DossierFileLocalServiceUtil
					.getDossierFile(dossierFileId);

			// Get dossier part
			DossierPart dossierPart = DossierPartLocalServiceUtil
					.getDossierPart(dossierFile.getDossierPartId());

			Dossier dossier = DossierLocalServiceUtil.getDossier(dossierFile
					.getDossierId());

			String formData = dossierFile.getFormData();
			String jrxmlTemplate = dossierPart.getFormReport();

			// Validate json string

			JSONFactoryUtil.createJSONObject(formData);

			String outputDestination = PortletPropsValues.OPENCPS_FILE_SYSTEM_TEMP_DIR;
			String fileName = System.currentTimeMillis() + StringPool.DASH
					+ dossierFileId + StringPool.DASH
					+ dossierPart.getDossierpartId() + fileExtension;

			// fileExportDir = exportToPDFFile(jrxmlTemplate, formData, null,
			// outputDestination, fileName);

			fileExportDir = exportReportFile(jrxmlTemplate, formData, null,
					outputDestination, fileName, DocType.getEnum(fileExtension));

			if (Validator.isNotNull(fileExportDir)) {

				file = new File(fileExportDir);
				inputStream = new FileInputStream(file);
				if (inputStream != null) {
					sourceFileName = fileExportDir.substring(
							fileExportDir.lastIndexOf(StringPool.SLASH) + 1,
							fileExportDir.length());
					String mimeType = MimeTypesUtil.getContentType(file);

					int actor = 0;

					if (accountBean.isEmployee()) {
						actor = 2;
					} else if (accountBean.isBusiness()
							|| accountBean.isCitizen()) {
						actor = 1;
					}

					long fileGroupId = 0;

					ActorBean actorBean = new ActorBean(actor,
							serviceContext.getUserId());

					// Add new version
					if (dossierFile.getFileEntryId() > 0) {
						DossierFile dossierFileAdd = DossierFileLocalServiceUtil
								.addDossierFile(dossierFile.getDossierFileId(),
										dossier.getFolderId(), sourceFileName,
										mimeType, dossierFile.getDisplayName(),
										StringPool.BLANK, StringPool.BLANK,
										inputStream, file.length(),
										serviceContext);

						// Add Log exportFile
						ProcessStep processStep = BackendUtils
								.getProcessStepByDossierId(dossier
										.getDossierId());

						DossierFileLogLocalServiceUtil.addFileLog(
								serviceContext.getUserId(),
								actorBean.getActorName(),
								dossier.getDossierId(),
								fileGroupId,
								processStep.getProcessStepId(),
								false,
								dossierFileAdd.getDisplayName(),
								1,
								StringPool.BLANK,
								PortletConstants.DOSSIER_FILE_ADD,
								dossierFileAdd != null ? dossierFileAdd
										.getFileEntryId() : 0,
								WebKeys.ACTOR_CITIZEN);

						/*
						 * DossierLogLocalServiceUtil.addDossierLog(
						 * serviceContext.getUserId(),
						 * serviceContext.getScopeGroupId(),
						 * serviceContext.getCompanyId(),
						 * dossierFile.getDossierId(), fileGroupId,
						 * dossier.getDossierStatus(),
						 * PortletConstants.DOSSIER_ACTION_EXPORT_FILE,
						 * PortletConstants.DOSSIER_ACTION_EXPORT_FILE +
						 * StringPool.SPACE + StringPool.COLON +
						 * StringPool.SPACE + dossierFile.getDisplayName(), new
						 * Date(), 0, 0, actorBean.getActor(),
						 * actorBean.getActorId(), actorBean.getActorName(),
						 * ProcessOrderPortlet.class.getName() +
						 * ".exportFile()");
						 */

					} else {
						// Update version 1
						DossierFile dossierFileAdd = DossierFileLocalServiceUtil
								.updateDossierFile(dossierFileId,
										dossier.getFolderId(), sourceFileName,
										mimeType, dossierFile.getDisplayName(),
										StringPool.BLANK, StringPool.BLANK,
										inputStream, file.length(),
										serviceContext);

						ProcessStep processStep = BackendUtils
								.getProcessStepByDossierId(dossier
										.getDossierId());

						DossierFileLogLocalServiceUtil.addFileLog(
								serviceContext.getUserId(),
								actorBean.getActorName(),
								dossier.getDossierId(),
								fileGroupId,
								processStep.getProcessStepId(),
								false,
								dossierFileAdd.getDisplayName(),
								1,
								StringPool.BLANK,
								PortletConstants.DOSSIER_FILE_UPDATE,
								dossierFileAdd != null ? dossierFileAdd
										.getFileEntryId() : 0,
								WebKeys.ACTOR_CITIZEN);

						// Update Log UpdateVersion File
						/*
						 * 
						 * Locale locale = new Locale("vi", "VN");
						 * 
						 * String dossierAcctionUpdateVersionFile = LanguageUtil
						 * .get(locale,
						 * PortletConstants.DOSSIER_ACTION_UPDATE_VERSION_FILE);
						 * 
						 * DossierLogLocalServiceUtil .addDossierLog(
						 * serviceContext.getUserId(),
						 * serviceContext.getScopeGroupId(),
						 * serviceContext.getCompanyId(),
						 * dossierFile.getDossierId(), fileGroupId,
						 * dossier.getDossierStatus(),
						 * PortletConstants.DOSSIER_ACTION_UPDATE_VERSION_FILE,
						 * dossierAcctionUpdateVersionFile + StringPool.SPACE +
						 * StringPool.COLON + StringPool.SPACE +
						 * dossierFile.getDisplayName(), new Date(), 0, 0,
						 * actorBean.getActor(), actorBean.getActorId(),
						 * actorBean.getActorName(),
						 * ProcessOrderPortlet.class.getName() +
						 * ".updateVersionFile()");
						 */

					}
				}

				SessionMessages.add(actionRequest,
						MessageKeys.DEFAULT_SUCCESS_KEY);

			}

		} catch (Exception e) {
			if (e instanceof NoSuchDossierFileException) {
				SessionErrors.add(actionRequest,
						NoSuchDossierFileException.class);
			} else if (e instanceof NoSuchAccountException) {
				SessionErrors.add(actionRequest, NoSuchAccountException.class);
			} else if (e instanceof NoSuchAccountTypeException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountTypeException.class);
			} else if (e instanceof NoSuchAccountFolderException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountFolderException.class);
			} else if (e instanceof NoSuchAccountOwnUserIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnUserIdException.class);
			} else if (e instanceof NoSuchAccountOwnOrgIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnOrgIdException.class);
			} else if (e instanceof PermissionDossierException) {
				SessionErrors.add(actionRequest,
						PermissionDossierException.class);
			} else if (e instanceof DuplicateFileException) {
				SessionErrors.add(actionRequest, DuplicateFileException.class);
			} else {
				SessionErrors.add(actionRequest, PortalException.class);
			}

			_log.error(e);
		} finally {
			responseJSON.put("fileExportDir", fileExportDir);
			PortletUtil.writeJSON(actionRequest, actionResponse, responseJSON);
			if (inputStream != null) {
				inputStream.close();
			}

			if (file.exists()) {
				file.delete();
			}
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void deleteAttachmentFile(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);
		DossierFile dossierFile = null;

		JSONObject jsonObject = null;

		try {
			if (dossierFileId > 0) {
				jsonObject = JSONFactoryUtil.createJSONObject();
				AccountBean accountBean = AccountUtil
						.getAccountBean(actionRequest);
				ServiceContext serviceContext = ServiceContextFactory
						.getInstance(actionRequest);

				dossierFile = DossierFileLocalServiceUtil
						.getDossierFile(dossierFileId);
				Dossier dossier = DossierLocalServiceUtil
						.fetchDossier(dossierFile.getDossierId());

				long fileEntryId = dossierFile.getFileEntryId();
				DossierFileLocalServiceUtil.deleteDossierFile(dossierFileId,
						fileEntryId);
				jsonObject.put("deleted", Boolean.TRUE);

				int actor = 0;

				if (accountBean.isEmployee()) {
					actor = 2;
				} else if (accountBean.isBusiness() || accountBean.isCitizen()) {
					actor = 1;
				}

				ActorBean actorBean = new ActorBean(actor,
						serviceContext.getUserId());

				long fileGroupId = 0;

				ProcessStep processStep = BackendUtils
						.getProcessStepByDossierId(dossier.getDossierId());

				// Add DossierLog Delete File
				DossierFileLogLocalServiceUtil.addFileLog(
						serviceContext.getUserId(), actorBean.getActorName(),
						dossier.getDossierId(), fileGroupId,
						processStep.getProcessStepId(), false,
						dossierFile.getDisplayName(), 1, StringPool.BLANK,
						PortletConstants.DOSSIER_FILE_REMOVE,
						dossierFile != null ? dossierFile.getFileEntryId() : 0,
						WebKeys.ACTOR_CITIZEN);
				/*
				 * DossierLogLocalServiceUtil.addDossierLog(
				 * serviceContext.getUserId(), serviceContext.getScopeGroupId(),
				 * serviceContext.getCompanyId(), dossierFile.getDossierId(),
				 * fileGroupId, dossier.getDossierStatus(),
				 * PortletConstants.DOSSIER_ACTION_REMOVE_ATTACTMENT_FILE,
				 * PortletConstants.DOSSIER_ACTION_REMOVE_ATTACTMENT_FILE +
				 * StringPool.SPACE + StringPool.COLON + StringPool.SPACE +
				 * dossierFile.getDisplayName(), new Date(), 0, 0,
				 * actorBean.getActor(), actorBean.getActorId(),
				 * actorBean.getActorName(), ProcessOrderPortlet.class.getName()
				 * + ".delDossierFile()");
				 */

			}

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			jsonObject.put("deleted", Boolean.FALSE);
			_log.error(e);
		} finally {
			PortletUtil.writeJSON(actionRequest, actionResponse, jsonObject);
		}

	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void deleteDossier(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);

		String dossierStatus = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.DOSSIER_STATUS);

		String redirectURL = ParamUtil.getString(actionRequest, "redirectURL");

		AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

		try {
			if (dossierStatus.equals(PortletConstants.DOSSIER_STATUS_NEW)) {
				validateDeleteDossier(dossierId, accountBean);

				DossierLocalServiceUtil.deleteDossierByDossierId(dossierId);

			}

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			if (e instanceof NoSuchDossierException) {
				SessionErrors.add(actionRequest, NoSuchDossierException.class);
			}

			else if (e instanceof NoSuchAccountException) {
				SessionErrors.add(actionRequest, NoSuchAccountException.class);
			} else if (e instanceof NoSuchAccountTypeException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountTypeException.class);
			} else if (e instanceof NoSuchAccountFolderException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountFolderException.class);
			} else if (e instanceof NoSuchAccountOwnUserIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnUserIdException.class);
			} else if (e instanceof NoSuchAccountOwnOrgIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnOrgIdException.class);
			} else if (e instanceof PermissionDossierException) {
				SessionErrors.add(actionRequest,
						PermissionDossierException.class);
			} else {
				SessionErrors.add(actionRequest, PortalException.class);
			}

			_log.error(e);
		} finally {
			if (Validator.isNotNull(redirectURL)) {
				actionResponse.sendRedirect(redirectURL);
			}
		}

	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void deleteDossierFile(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);
		if (dossierFileId > 0) {
			DossierFile dossierFile = null;
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			try {
				dossierFile = DossierFileLocalServiceUtil
						.getDossierFile(dossierFileId);
				if (dossierFile != null) {
					long fileEntryId = dossierFile.getFileEntryId();
					try {
						if (fileEntryId > 0) {
							DLAppServiceUtil.deleteFileEntry(fileEntryId);
						}
					} catch (Exception e) {
						// nothing to do
					}

					Dossier dossier = DossierLocalServiceUtil
							.fetchDossier(dossierFile.getDossierId());

					DossierFileLocalServiceUtil.deleteDossierFile(dossierFile);

					AccountBean accountBean = AccountUtil
							.getAccountBean(actionRequest);
					ServiceContext serviceContext = ServiceContextFactory
							.getInstance(actionRequest);
					long fileGroupId = 0;

					int actor = 0;

					if (accountBean.isEmployee()) {
						actor = 2;
					} else if (accountBean.isBusiness()
							|| accountBean.isCitizen()) {
						actor = 1;
					}

					ActorBean actorBean = new ActorBean(actor,
							serviceContext.getUserId());

					ProcessStep processStep = BackendUtils
							.getProcessStepByDossierId(dossier.getDossierId());

					DossierFileLogLocalServiceUtil.addFileLog(serviceContext
							.getUserId(), actorBean.getActorName(), dossier
							.getDossierId(), fileGroupId, processStep
							.getProcessStepId(), false, dossierFile
							.getDisplayName(), 1, StringPool.BLANK,
							PortletConstants.DOSSIER_FILE_REMOVE,
							dossierFile != null ? dossierFile.getFileEntryId()
									: 0, WebKeys.ACTOR_CITIZEN);

					/*
					 * DossierLogLocalServiceUtil.addDossierLog(
					 * serviceContext.getUserId(),
					 * serviceContext.getScopeGroupId(),
					 * serviceContext.getCompanyId(),
					 * dossierFile.getDossierId(), fileGroupId,
					 * dossier.getDossierStatus(),
					 * PortletConstants.DOSSIER_ACTION_REMOVE_ATTACTMENT_FILE,
					 * PortletConstants.DOSSIER_ACTION_REMOVE_ATTACTMENT_FILE +
					 * StringPool.SPACE + StringPool.COLON + StringPool.SPACE +
					 * dossierFile.getDisplayName(), new Date(), 0, 0,
					 * actorBean.getActor(), actorBean.getActorId(),
					 * actorBean.getActorName(),
					 * ProcessOrderPortlet.class.getName() +
					 * ".delDossierFile()");
					 */
					jsonObject.put("deleted", Boolean.TRUE);
				}

				SessionMessages.add(actionRequest,
						MessageKeys.DEFAULT_SUCCESS_KEY);

			} catch (Exception e) {
				_log.error(e);

				jsonObject.put("deleted", Boolean.FALSE);

			}

			writeJSON(actionRequest, actionResponse, jsonObject);
		}

	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void deleteTempFile(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		long fileEntryId = ParamUtil.getLong(actionRequest, "fileEntryId");

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		try {
			DLAppServiceUtil.deleteFileEntry(fileEntryId);
			jsonObject.put("deleted", Boolean.TRUE);

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			String errorMessage = themeDisplay
					.translate("an-unexpected-error-occurred-while-deleting-the-file");

			jsonObject.put("deleted", Boolean.FALSE);
			jsonObject.put("errorMessage", errorMessage);
		}

		writeJSON(actionRequest, actionResponse, jsonObject);
	}

	/**
	 * @param groupId
	 * @param folderId
	 * @param tempFolderName
	 * @param fileName
	 * @throws Exception
	 */
	protected void deleteTempFile(long groupId, long folderId,
			String tempFolderName, String fileName) throws Exception {

		try {
			DLAppServiceUtil.deleteTempFileEntry(groupId, folderId, fileName,
					tempFolderName);

		} catch (Exception e) {
			_log.error(e);
		}
	}

	public void deleteTempFiles(ActionRequest actionRequest,
			ActionResponse actionResponse) {

		String strFileEntryIds = ParamUtil.getString(actionRequest,
				"fileEntryIds");

		if (Validator.isNotNull(strFileEntryIds)) {
			long[] fileEntryIds = StringUtil.split(strFileEntryIds, 0L);
			if (fileEntryIds != null) {
				for (int i = 0; i < fileEntryIds.length; i++) {
					try {
						DLAppServiceUtil.deleteFileEntry(fileEntryIds[i]);
					} catch (Exception e) {
						continue;
					}
				}
			}
		}

	}

	/**
	 * @param jrxmlTemplate
	 * @param formData
	 * @param map
	 * @param outputDestination
	 * @param fileName
	 * @return
	 */
	protected String exportToPDFFile(String jrxmlTemplate, String formData,
			Map<String, Object> map, String outputDestination, String fileName) {

		return JRReportUtil.createReportPDFFile(jrxmlTemplate, formData, map,
				outputDestination, fileName);
	}

	/**
	 * @param jrxmlTemplate
	 * @param formData
	 * @param map
	 * @param outputDestination
	 * @param fileName
	 * @param docType
	 * @return
	 */
	protected String exportReportFile(String jrxmlTemplate, String formData,
			Map<String, Object> map, String outputDestination, String fileName,
			DocType docType) {

		return JRReportUtil.createReportFile(jrxmlTemplate, formData, map,
				outputDestination, fileName, docType);
	}

	public void exportReport(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);

		String docType = ParamUtil.getString(actionRequest, "docType");

		_log.info("docType &&&&&&&& ^^^^^^^^^^  " + docType);

		InputStream inputStream = null;

		File file = null;

		// JSONObject responseJSON = JSONFactoryUtil.createJSONObject();

		String fileExportDir = StringPool.BLANK;

		try {
			validateCreateDynamicForm(dossierFileId, accountBean);

			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);
			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);

			// Get dossier file
			DossierFile dossierFile = DossierFileLocalServiceUtil
					.getDossierFile(dossierFileId);

			// Get dossier part
			DossierPart dossierPart = DossierPartLocalServiceUtil
					.getDossierPart(dossierFile.getDossierPartId());

			String formData = dossierFile.getFormData();
			String jrxmlTemplate = dossierPart.getFormReport();

			// Validate json string

			JSONFactoryUtil.createJSONObject(formData);

			String outputDestination = PortletPropsValues.OPENCPS_FILE_SYSTEM_TEMP_DIR;
			String fileName = System.currentTimeMillis() + StringPool.DASH
					+ dossierFileId + StringPool.DASH
					+ dossierPart.getDossierpartId() + docType;

			DocType type = DocType.getEnum(docType);

			_log.info("type ^%%%%%%%%********* ^^^^^^^^ " + type.toString());

			fileExportDir = exportReportFile(jrxmlTemplate, formData, null,
					outputDestination, fileName, type);

			if (Validator.isNotNull(fileExportDir)) {

				file = new File(fileExportDir);
				inputStream = new FileInputStream(file);
				String mimeType = MimeTypesUtil.getContentType(file);

				PortletUtil.sendFile(actionRequest, actionResponse, fileName,
						inputStream, file.length(), mimeType);

			}

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			if (e instanceof NoSuchDossierFileException) {
				SessionErrors.add(actionRequest,
						NoSuchDossierFileException.class);
			} else if (e instanceof NoSuchAccountException) {
				SessionErrors.add(actionRequest, NoSuchAccountException.class);
			} else if (e instanceof NoSuchAccountTypeException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountTypeException.class);
			} else if (e instanceof NoSuchAccountFolderException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountFolderException.class);
			} else if (e instanceof NoSuchAccountOwnUserIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnUserIdException.class);
			} else if (e instanceof NoSuchAccountOwnOrgIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnOrgIdException.class);
			} else if (e instanceof PermissionDossierException) {
				SessionErrors.add(actionRequest,
						PermissionDossierException.class);
			} else if (e instanceof DuplicateFileException) {
				SessionErrors.add(actionRequest, DuplicateFileException.class);
			} else if (e instanceof RolePermissionsException) {
				SessionErrors
						.add(actionRequest, RolePermissionsException.class);
			} else {
				SessionErrors.add(actionRequest, PortalException.class);
			}

			_log.error(e);
		} finally {
			// responseJSON.put("fileExportDir", fileExportDir);
			// PortletUtil.writeJSON(actionRequest, actionResponse,
			// responseJSON);

			if (inputStream != null) {
				inputStream.close();
			}

			if (file.exists()) {
				file.delete();
			}
		}
	}

	/**
	 * @param object
	 * @param dictCollection
	 * @return
	 */
	protected String getSelectedItems(Object object,
			DictCollection dictCollection) {

		String selectedItems = StringPool.BLANK;
		String cityCode = StringPool.BLANK;
		String districtCode = StringPool.BLANK;
		String wardCode = StringPool.BLANK;
		if (object instanceof Citizen) {
			Citizen citizen = (Citizen) object;
			cityCode = citizen.getCityCode();
			districtCode = citizen.getDistrictCode();
			wardCode = citizen.getWardCode();
		} else if (object instanceof Business) {
			Business business = (Business) object;
			cityCode = business.getCityCode();
			districtCode = business.getDistrictCode();
			wardCode = business.getWardCode();
		} else if (object instanceof Dossier) {
			Dossier dossier = (Dossier) object;
			cityCode = dossier.getCityCode();
			districtCode = dossier.getDistrictCode();
			wardCode = dossier.getWardCode();
		}

		try {
			DictItem city = DictItemLocalServiceUtil
					.getDictItemInuseByItemCode(
							dictCollection.getDictCollectionId(), cityCode);
			DictItem district = DictItemLocalServiceUtil
					.getDictItemInuseByItemCode(
							dictCollection.getDictCollectionId(), districtCode);
			DictItem ward = DictItemLocalServiceUtil
					.getDictItemInuseByItemCode(
							dictCollection.getDictCollectionId(), wardCode);

			String[] dictItemIds = new String[3];

			dictItemIds[0] = city != null ? String.valueOf(city.getItemCode())
					: StringPool.BLANK;

			dictItemIds[1] = district != null ? String.valueOf(district
					.getItemCode()) : StringPool.BLANK;

			dictItemIds[2] = ward != null ? String.valueOf(ward.getItemCode())
					: StringPool.BLANK;

			// dictItemIds[0] = city != null ? String
			// .valueOf(city.getDictItemId()) : StringPool.BLANK;

			// dictItemIds[1] = district != null ? String.valueOf(district
			// .getDictItemId()) : StringPool.BLANK;

			// dictItemIds[2] = ward != null ? String
			// .valueOf(ward.getDictItemId()) : StringPool.BLANK;

			selectedItems = StringUtil.merge(dictItemIds);
		} catch (Exception e) {
			// Nothing todo
		}

		return selectedItems;
	}

	/**
	 * @param path
	 * @param renderRequest
	 * @param renderResponse
	 * @throws IOException
	 * @throws PortletException
	 */
	@Override
	protected void include(String path, RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		if (!_hasPermission) {
			path = "/html/portlets/dossiermgt/frontoffice/warning.jsp";
		}

		super.include(path, renderRequest, renderResponse);
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 */
	public void previewAttachmentFile(ActionRequest actionRequest,
			ActionResponse actionResponse) {

		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);
		String url = DLFileEntryUtil.getDossierFileAttachmentURL(dossierFileId,
				themeDisplay);
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		// url = "http://docs.google.com/gview?url=" + url + "&embedded=true";
		jsonObject.put("url", url);
		try {
			PortletUtil.writeJSON(actionRequest, actionResponse, jsonObject);
		} catch (IOException e) {
			_log.error(e);
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void previewDynamicForm(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);

		String redirectURL = ParamUtil.getString(actionRequest, "redirectURL");

		HttpServletResponse response = PortalUtil
				.getHttpServletResponse(actionResponse);
		response.setContentType("text/html");

		PrintWriter writer = null;

		try {
			writer = response.getWriter();

			// Get dossier file
			DossierFile dossierFile = DossierFileLocalServiceUtil
					.getDossierFile(dossierFileId);

			// Get dossier part
			DossierPart dossierPart = DossierPartLocalServiceUtil
					.getDossierPart(dossierFile.getDossierPartId());

			String formData = dossierFile.getFormData();
			String jrxmlTemplate = dossierPart.getFormReport();

			// Validate json string

			JSONFactoryUtil.createJSONObject(formData);

			JRReportUtil.renderReportHTMLStream(response, writer,
					jrxmlTemplate, formData, null);

		} catch (Exception e) {
			_log.error(e);
		} finally {
			if (Validator.isNotNull(redirectURL)) {
				response.sendRedirect(redirectURL);
			}

		}

	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void removeAttachmentFile(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);

		JSONObject jsonObject = null;

		try {

			DossierFile dossierFile = DossierFileLocalServiceUtil
					.getDossierFile(dossierFileId);

			Dossier dossier = DossierLocalServiceUtil.fetchDossier(dossierFile
					.getDossierId());

			DossierPart dossierPart = DossierPartLocalServiceUtil
					.getDossierPart(dossierFile.getDossierPartId());
			jsonObject = JSONFactoryUtil.createJSONObject();
			if (dossierFileId > 0
					&& dossierPart.getPartType() != PortletConstants.DOSSIER_PART_TYPE_OTHER) {

				if (dossierFile.getSyncStatus() != PortletConstants.DOSSIER_FILE_SYNC_STATUS_SYNCSUCCESS) {
					DossierFileLocalServiceUtil.deleteDossierFile(
							dossierFileId, dossierFile.getFileEntryId());
				} else {
					DossierFileLocalServiceUtil
							.removeDossierFile(dossierFileId);
				}

			} else {

				DossierFileLocalServiceUtil.deleteDossierFile(dossierFileId,
						dossierFile.getFileEntryId());
			}

			// Add dossierLog for removeDossierFile
			AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);

			int actor = 0;

			if (accountBean.isEmployee()) {
				actor = 2;
			} else if (accountBean.isBusiness() || accountBean.isCitizen()) {
				actor = 1;
			}

			ActorBean actorBean = new ActorBean(actor,
					serviceContext.getUserId());

			long fileGroupId = 0;

			// Add DossierLog Delete File

			ProcessStep processStep = BackendUtils
					.getProcessStepByDossierId(dossier.getDossierId());

			DossierFileLogLocalServiceUtil.addFileLog(serviceContext
					.getUserId(), actorBean.getActorName(), dossier
					.getDossierId(), fileGroupId, processStep
					.getProcessStepId(), false, dossierFile.getDisplayName(),
					1, StringPool.BLANK, PortletConstants.DOSSIER_FILE_REMOVE,
					dossierFile != null ? dossierFile.getFileEntryId() : 0,
					WebKeys.ACTOR_CITIZEN);

			/*
			 * DossierLogLocalServiceUtil.addDossierLog(
			 * serviceContext.getUserId(), serviceContext.getScopeGroupId(),
			 * serviceContext.getCompanyId(), dossierFile.getDossierId(),
			 * fileGroupId, dossier.getDossierStatus(),
			 * PortletConstants.DOSSIER_ACTION_REMOVE_ATTACTMENT_FILE,
			 * PortletConstants.DOSSIER_ACTION_REMOVE_ATTACTMENT_FILE +
			 * StringPool.SPACE + StringPool.COLON + StringPool.SPACE +
			 * dossierFile.getDisplayName(), new Date(), 0, 0,
			 * actorBean.getActor(), actorBean.getActorId(),
			 * actorBean.getActorName(), ProcessOrderPortlet.class.getName() +
			 * ".delDossierFile()");
			 */
			jsonObject.put("deleted", Boolean.TRUE);

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			jsonObject.put("deleted", Boolean.FALSE);
			_log.error(e);
		} finally {
			PortletUtil.writeJSON(actionRequest, actionResponse, jsonObject);
		}

	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void removeIndividualGroup(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		long fileGroupId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.FILE_GROUP_ID);

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);

		long dossierPartId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);

		JSONObject jsonObject = null;
		try {
			if (fileGroupId > 0) {
				jsonObject = JSONFactoryUtil.createJSONObject();
				FileGroupLocalServiceUtil.deleteFileGroup(dossierId,
						dossierPartId, fileGroupId);

				jsonObject.put("deleted", Boolean.TRUE);
			}

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			jsonObject.put("deleted", Boolean.FALSE);
			_log.error(e);
		} finally {
			PortletUtil.writeJSON(actionRequest, actionResponse, jsonObject);
		}
	}

	/**
	 * @param renderRequest
	 * @param renderResponse
	 * @throws PortletException
	 * @throws IOException
	 */
	@Override
	public void render(RenderRequest renderRequest,
			RenderResponse renderResponse) throws PortletException, IOException {

		// Reset check permission flag
		setHasPermission(true);

		HttpServletRequest request = PortalUtil
				.getHttpServletRequest(renderRequest);

		HttpSession session = PortalSessionContext.get(request
				.getRequestedSessionId());

		// HttpSession session = request.getSession();

		validatePermission(renderRequest, renderResponse);

		AccountBean accountBean = AccountUtil.getAccountBean(renderRequest);

		if (_hasPermission) {

			ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest
					.getAttribute(WebKeys.THEME_DISPLAY);

			Dossier dossier = (Dossier) renderRequest
					.getAttribute(WebKeys.DOSSIER_ENTRY);

			DictCollection dictCollection = null;

			long dossierId = ParamUtil.getLong(renderRequest,
					DossierDisplayTerms.DOSSIER_ID);

			long dossierFileId = ParamUtil.getLong(renderRequest,
					DossierFileDisplayTerms.DOSSIER_FILE_ID);

			long dossierPartId = ParamUtil.getLong(renderRequest,
					DossierFileDisplayTerms.DOSSIER_PART_ID);

			long serviceConfigId = ParamUtil.getLong(renderRequest,
					DossierDisplayTerms.SERVICE_CONFIG_ID);

			String accountType = GetterUtil.getString(session
					.getAttribute(WebKeys.ACCOUNT_TYPE));

			String selectedItems = StringPool.BLANK;

			try {

				if (dossierId > 0) {
					dossier = DossierLocalServiceUtil.getDossier(dossierId);
				}

				dictCollection = DictCollectionLocalServiceUtil
						.getDictCollection(
								themeDisplay.getScopeGroupId(),
								PortletPropsValues.DATAMGT_MASTERDATA_ADMINISTRATIVE_REGION);

				if (dossier != null) {
					renderRequest.setAttribute(WebKeys.DOSSIER_ENTRY, dossier);
					serviceConfigId = dossier.getServiceConfigId();
					selectedItems = getSelectedItems(dossier, dictCollection);

				} else {

					if (accountType
							.equals(PortletPropsValues.USERMGT_USERGROUP_NAME_CITIZEN)) {

						// Citizen citizen =
						// (Citizen)
						// session.getAttribute(WebKeys.CITIZEN_ENTRY);

						Citizen citizen = (Citizen) accountBean
								.getAccountInstance();
						selectedItems = getSelectedItems(citizen,
								dictCollection);

					} else if (accountType
							.equals(PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS)) {
						// Business business =
						// (Business)
						// session.getAttribute(WebKeys.BUSINESS_ENTRY);

						Business business = (Business) accountBean
								.getAccountInstance();
						selectedItems = getSelectedItems(business,
								dictCollection);

					}
				}
				renderRequest.setAttribute(WebKeys.DICT_ITEM_SELECTED,
						selectedItems);

				if (serviceConfigId > 0) {
					ServiceConfig serviceConfig = ServiceConfigLocalServiceUtil
							.getServiceConfig(serviceConfigId);

					renderRequest.setAttribute(WebKeys.SERVICE_CONFIG_ENTRY,
							serviceConfig);

					if (serviceConfig != null
							&& serviceConfig.getServiceInfoId() > 0) {
						ServiceInfo serviceInfo = ServiceInfoLocalServiceUtil
								.getServiceInfo(serviceConfig
										.getServiceInfoId());

						renderRequest.setAttribute(WebKeys.SERVICE_INFO_ENTRY,
								serviceInfo);

						DossierTemplate dossierTemplate = DossierTemplateLocalServiceUtil
								.getDossierTemplate(serviceConfig
										.getDossierTemplateId());
						renderRequest
								.setAttribute(WebKeys.DOSSIER_TEMPLATE_ENTRY,
										dossierTemplate);
					}
				}

				if (dossierFileId > 0) {
					DossierFile dossierFile = DossierFileLocalServiceUtil
							.getDossierFile(dossierFileId);
					renderRequest.setAttribute(WebKeys.DOSSIER_FILE_ENTRY,
							dossierFile);
				}

				if (dossierPartId > 0) {
					DossierPart dossierPart = DossierPartLocalServiceUtil
							.getDossierPart(dossierPartId);
					renderRequest.setAttribute(WebKeys.DOSSIER_PART_ENTRY,
							dossierPart);
				}
			} catch (Exception e) {
				_log.error(e);
			}
		}

		super.render(renderRequest, renderResponse);

	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void updateDossier(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		HttpServletRequest request = PortalUtil
				.getHttpServletRequest(actionRequest);

		HttpSession session = request.getSession();

		String accountType = GetterUtil.getString(session
				.getAttribute(WebKeys.ACCOUNT_TYPE));

		Dossier dossier = null;

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);
		long dossierTemplateId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_TEMPLATE_ID);
		long serviceInfoId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.SERVICE_INFO_ID);
		// long cityId = ParamUtil.getLong(actionRequest,
		// DossierDisplayTerms.CITY_ID);
		// long districtId = ParamUtil.getLong(actionRequest,
		// DossierDisplayTerms.DISTRICT_ID);
		// long wardId = ParamUtil.getLong(actionRequest,
		// DossierDisplayTerms.WARD_ID);

		String cityCode = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.CITY_CODE);
		String districtCode = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.DISTRICT_CODE);
		String wardCode = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.WARD_CODE);

		long serviceConfigId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.SERVICE_CONFIG_ID);

		long govAgencyOrganizationId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.GOVAGENCY_ORGANIZATION_ID);

		long ownerOrganizationId = GetterUtil.getLong(session
				.getAttribute(WebKeys.ACCOUNT_OWNERORGANIZATIONID));

		int serviceMode = ParamUtil.getInteger(actionRequest,
				DossierDisplayTerms.SERVICE_MODE);
		String serviceDomainIndex = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.SERVICE_DOMAIN_INDEX);

		String govAgencyCode = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.GOVAGENCY_CODE);
		String govAgencyName = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.GOVAGENCY_NAME);

		String serviceAdministrationIndex = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.SERVICE_ADMINISTRATION_INDEX);
		String templateFileNo = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.TEMPLATE_FILE_NO);
		String subjectName = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.SUBJECT_NAME);
		String subjectId = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.SUBJECT_ID);
		String address = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.ADDRESS);
		String contactName = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.CONTACT_NAME);
		String contactTelNo = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.CONTACT_TEL_NO);
		String contactEmail = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.CONTACT_EMAIL);
		String note = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.NOTE);

		String backURL = ParamUtil.getString(actionRequest, "backURL");

		String redirectURL = ParamUtil.getString(actionRequest, "redirectURL");

		String redirectPaymentURL = ParamUtil.getString(request,
				DossierDisplayTerms.REDIRECT_PAYMENT_URL);

		boolean isEditDossier = ParamUtil.getBoolean(request, "isEditDossier");

		boolean update = false;

		try {
			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);

			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);

			if (dossierId > 0) {
				dossier = DossierLocalServiceUtil.getDossier(dossierId);
			}

			String dossierDestinationFolder = StringPool.BLANK;

			SplitDate splitDate = PortletUtil.splitDate(new Date());

			dossierDestinationFolder = PortletUtil.getDossierDestinationFolder(
					serviceContext.getScopeGroupId(), splitDate.getYear(),
					splitDate.getMonth(), splitDate.getDayOfMoth());

			if (dossier != null) {
				dossierDestinationFolder += StringPool.SLASH + dossier.getOid();
			}

			validateDossier(cityCode, districtCode, wardCode, accountType,
					dossierDestinationFolder, subjectName, subjectId, address,
					contactName, contactTelNo, contactEmail);

			String cityName = StringPool.BLANK;
			String districtName = StringPool.BLANK;
			String wardName = StringPool.BLANK;

			DictItem city = DictItemLocalServiceUtil
					.getDictItemByCode(cityCode);
			DictItem district = DictItemLocalServiceUtil
					.getDictItemByCode(districtCode);
			DictItem ward = DictItemLocalServiceUtil
					.getDictItemByCode(wardCode);

			if (city != null) {
				cityCode = city.getItemCode();
				cityName = city.getItemName(themeDisplay.getLocale());
			}

			if (district != null) {
				districtCode = district.getItemCode();
				districtName = district.getItemName(themeDisplay.getLocale());
			}

			if (ward != null) {
				wardCode = ward.getItemCode();
				wardName = ward.getItemName(themeDisplay.getLocale());
			}

			DLFolder dossierFolder = DLFolderUtil.getTargetFolder(
					serviceContext.getUserId(),
					serviceContext.getScopeGroupId(),
					serviceContext.getScopeGroupId(), false, 0,
					dossierDestinationFolder, StringPool.BLANK, false,
					serviceContext);

			ActorBean actor = new ActorBean(1, serviceContext.getUserId());

			if (dossierId == 0) {
				dossier = DossierLocalServiceUtil.addDossier(
						serviceContext.getUserId(), ownerOrganizationId,
						dossierTemplateId, templateFileNo, serviceConfigId,
						serviceInfoId, serviceDomainIndex,
						govAgencyOrganizationId, govAgencyCode, govAgencyName,
						serviceMode, serviceAdministrationIndex, cityCode,
						cityName, districtCode, districtName, wardName,
						wardCode, subjectName, subjectId, address, contactName,
						contactTelNo, contactEmail, note,
						PortletConstants.DOSSIER_SOURCE_DIRECT,
						PortletConstants.DOSSIER_STATUS_NEW,
						dossierFolder.getFolderId(), redirectPaymentURL,
						serviceContext);

				// Add DossierLog (for Add dossier)

				DossierLogLocalServiceUtil.addDossierLog(
						serviceContext.getUserId(),
						serviceContext.getScopeGroupId(),
						serviceContext.getCompanyId(), dossier.getDossierId(),
						0, PortletConstants.DOSSIER_STATUS_NEW,
						"create-dossier",
						"create-dossier", new Date(), 0, 0,
						actor.getActor(), actor.getActorId(),
						actor.getActorName(),
						DossierMgtFrontOfficePortlet.class.getName()
								+ ".updateDossier()", 0, 0, false);

			} else {
				dossier = DossierLocalServiceUtil.updateDossier(dossierId,
						serviceContext.getUserId(), ownerOrganizationId,
						dossierTemplateId, templateFileNo, serviceConfigId,
						serviceInfoId, serviceDomainIndex,
						govAgencyOrganizationId, govAgencyCode, govAgencyName,
						serviceMode, serviceAdministrationIndex, cityCode,
						cityName, districtCode, districtName, wardName,
						wardCode, subjectName, subjectId, address, contactName,
						contactTelNo, contactEmail, note,

						dossierFolder.getFolderId(), serviceContext);

				// Add DossierLog (for Update dossier)

				DossierLogLocalServiceUtil.addDossierLog(
						serviceContext.getUserId(),
						serviceContext.getScopeGroupId(),
						serviceContext.getCompanyId(), dossierId, 0,
						PortletConstants.DOSSIER_STATUS_UPDATE,
						"update-dossier",
						"update-dossier", new Date(), 0,
						0, actor.getActor(), actor.getActorId(),
						actor.getActorName(),
						DossierMgtFrontOfficePortlet.class.getName()
								+ ".updateDossier()", 0, 0, false);

			}

			SessionMessages.add(actionRequest,
					MessageKeys.DOSSIER_UPDATE_SUCCESS);

			update = true;
		} catch (Exception e) {
			update = false;
			if (e instanceof EmptyDossierCityCodeException
					|| e instanceof EmptyDossierDistrictCodeException
					|| e instanceof EmptyDossierWardCodeException
					|| e instanceof InvalidDossierObjectException
					|| e instanceof CreateDossierFolderException
					|| e instanceof EmptyDossierSubjectNameException
					|| e instanceof OutOfLengthDossierSubjectNameException
					|| e instanceof EmptyDossierSubjectIdException
					|| e instanceof OutOfLengthDossierSubjectIdException
					|| e instanceof EmptyDossierAddressException
					|| e instanceof OutOfLengthDossierContactEmailException
					|| e instanceof OutOfLengthDossierContactNameException
					|| e instanceof OutOfLengthDossierContactTelNoException
					|| e instanceof EmptyDossierContactNameException
					|| e instanceof OutOfLengthDossierAddressException
					|| e instanceof EmptyDossierFileException
					|| e instanceof DuplicateFolderNameException) {

				SessionErrors.add(actionRequest, e.getClass());
			} else {
				SessionErrors.add(actionRequest,
						MessageKeys.DOSSIER_SYSTEM_EXCEPTION_OCCURRED);
			}

			_log.error(e);

		} finally {
			if (update) {
				if (Validator.isNotNull(redirectURL)) {

					actionResponse.sendRedirect(redirectURL + "&_"
							+ WebKeys.DOSSIER_MGT_PORTLET + "_dossierId="
							+ dossier.getDossierId());

				}
			} else {

				actionResponse.setRenderParameter("backURL", backURL);

				actionResponse.setRenderParameter(
						DossierDisplayTerms.SERVICE_CONFIG_ID,
						String.valueOf(serviceConfigId));
				actionResponse.setRenderParameter(
						DossierDisplayTerms.DOSSIER_ID, String
								.valueOf(dossier != null ? dossier
										.getDossierId() : 0));

				actionResponse.setRenderParameter("isEditDossier",
						String.valueOf(isEditDossier));

				actionResponse
						.setRenderParameter("mvcPath",
								"/html/portlets/dossiermgt/frontoffice/edit_dossier.jsp");
			}

		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 */
	@Deprecated
	public void updateDossierFile(ActionRequest actionRequest,
			ActionResponse actionResponse) {

		UploadPortletRequest uploadPortletRequest = PortalUtil
				.getUploadPortletRequest(actionRequest);

		long dossierFileId = ParamUtil.getLong(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);

		long dossierPartId = ParamUtil.getLong(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);

		int index = ParamUtil.getInteger(uploadPortletRequest,
				DossierFileDisplayTerms.INDEX);

		String groupName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.GROUP_NAME);

		String templateFileNo = ParamUtil.getString(uploadPortletRequest,
				DossierDisplayTerms.TEMPLATE_FILE_NO);

		String fileName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.FILE_NAME);

		String redirectURL = ParamUtil.getString(uploadPortletRequest,
				"redirectURL");

		String displayName = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DISPLAY_NAME);

		String dossierFileNo = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_NO);

		String dossierFileDate = ParamUtil.getString(uploadPortletRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_DATE);

		String sourceFileName = uploadPortletRequest
				.getFileName(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

		sourceFileName = sourceFileName.concat(
				PortletConstants.TEMP_RANDOM_SUFFIX).concat(
				StringUtil.randomString());

		String accountType = ParamUtil.getString(uploadPortletRequest,
				WebKeys.ACCOUNT_TYPE);

		InputStream inputStream = null;

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		try {
			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(uploadPortletRequest);
			DossierFile dossierFile = DossierFileLocalServiceUtil
					.getDossierFile(dossierFileId);

			long storeFolderId = 0;

			if (dossierFile != null) {
				long fileEntryId = dossierFile.getFileEntryId();
				if (fileEntryId > 0) {
					FileEntry fileEntry = DLAppServiceUtil
							.getFileEntry(fileEntryId);
					storeFolderId = fileEntry.getFolderId();
				} else {
					long dossierId = dossierFile.getDossierId();
					Dossier dossier = DossierLocalServiceUtil
							.getDossier(dossierId);

					int dossierNo = dossier.getCounter();

					String destination = StringPool.BLANK;
					if (accountType
							.equals(PortletPropsValues.USERMGT_USERGROUP_NAME_CITIZEN)) {
						destination = PortletUtil
								.getCitizenDossierDestinationFolder(
										dossier.getGroupId(),
										themeDisplay.getUserId())
								+ StringPool.SLASH + String.valueOf(dossierNo);

					} else if (accountType
							.equals(PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS)) {

						destination = PortletUtil
								.getBusinessDossierDestinationFolder(
										dossier.getGroupId(),
										dossier.getOwnerOrganizationId())
								+ StringPool.SLASH + String.valueOf(dossierNo);

					}

					DLFolder storeFolder = DLFolderUtil.getTargetFolder(
							themeDisplay.getUserId(),
							themeDisplay.getScopeGroupId(),
							themeDisplay.getScopeGroupId(), false, 0,
							destination, StringPool.BLANK, false,
							serviceContext);

					storeFolderId = storeFolder.getFolderId();
				}
			}

			inputStream = uploadPortletRequest
					.getFileAsStream(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

			long size = uploadPortletRequest
					.getSize(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

			String contentType = uploadPortletRequest
					.getContentType(DossierFileDisplayTerms.DOSSIER_FILE_UPLOAD);

			String mimeType = Validator.isNotNull(contentType) ? MimeTypesUtil
					.getContentType(contentType) : StringPool.BLANK;

			FileEntry fileEntry = DLAppServiceUtil.addFileEntry(
					serviceContext.getScopeGroupId(), storeFolderId,
					sourceFileName, mimeType, displayName, StringPool.BLANK,
					StringPool.BLANK, inputStream, size, serviceContext);

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_FILE_NO,
					dossierFileNo);

			jsonObject.put(DossierFileDisplayTerms.DISPLAY_NAME, displayName);

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_FILE_DATE,
					dossierFileDate);

			jsonObject.put(DossierFileDisplayTerms.FILE_TITLE,
					fileEntry.getTitle());

			jsonObject.put(DossierFileDisplayTerms.MIME_TYPE,
					fileEntry.getMimeType());

			jsonObject.put(DossierFileDisplayTerms.FILE_NAME, fileName);

			jsonObject.put(DossierFileDisplayTerms.FILE_ENTRY_ID,
					fileEntry.getFileEntryId());
			jsonObject.put(DossierFileDisplayTerms.FOLDE_ID,
					fileEntry.getFolderId());

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_PART_ID,
					dossierPartId);

			jsonObject.put(DossierFileDisplayTerms.INDEX, index);

			jsonObject.put(DossierFileDisplayTerms.GROUP_NAME, groupName);

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_FILE_ORIGINAL,
					PortletConstants.DOSSIER_FILE_ORIGINAL);

			jsonObject.put(DossierFileDisplayTerms.DOSSIER_FILE_TYPE,
					PortletConstants.DOSSIER_FILE_TYPE_INPUT);

			jsonObject
					.put(DossierDisplayTerms.TEMPLATE_FILE_NO, templateFileNo);

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			_log.error(e);
			SessionErrors.add(actionRequest, "upload-error");
		} finally {
			StreamUtil.cleanUp(inputStream);
			HttpServletRequest request = PortalUtil
					.getHttpServletRequest(actionRequest);
			request.setAttribute(WebKeys.RESPONSE_UPLOAD_TEMP_DOSSIER_FILE,
					jsonObject);

			if (Validator.isNotNull(redirectURL)) {
				actionResponse
						.setRenderParameter("jspPage",
								"/html/portlets/dossiermgt/frontoffice/upload_dossier_file.jsp");
			}
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 * @throws SystemException
	 * @throws PortalException
	 */

	public void updateDossierStatus(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException, PortalException,
			SystemException {

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);

		String note = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.NOTE);

		long fileGroupId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_DATE);

		String dossierStatus = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.DOSSIER_STATUS);

		String redirectURL = ParamUtil.getString(actionRequest, "redirectURL");

		String backURL = ParamUtil.getString(actionRequest, "backURL");

		// String holdPosition = ParamUtil.getString(actionRequest, "hold");

		Dossier dossier = DossierLocalServiceUtil.getDossier(dossierId);

		boolean isUpdateStatusSuccessFlag = false;

		try {
			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);

			UserActionMsg actionMsg = new UserActionMsg();

			Message message = new Message();

			boolean isSend = true;

			switch (dossierStatus) {
			case PortletConstants.DOSSIER_STATUS_WAITING:

				actionMsg.setAction(WebKeys.ACTION_RESUBMIT_VALUE);

				actionMsg.setDossierId(dossierId);

				actionMsg.setFileGroupId(fileGroupId);

				actionMsg.setLocale(serviceContext.getLocale());

				actionMsg.setUserId(serviceContext.getUserId());

				actionMsg.setGroupId(serviceContext.getScopeGroupId());

				actionMsg.setCompanyId(dossier.getCompanyId());

				actionMsg.setGovAgencyCode(dossier.getGovAgencyCode());

				actionMsg.setDossierOId(dossier.getOid());

				actionMsg.setDossierStatus(dossierStatus);

				isSend = false;

				message.put("msgToEngine", actionMsg);

				break;

			case PortletConstants.DOSSIER_STATUS_NEW:

				validateSubmitDossier(dossierId);

				actionMsg.setAction(WebKeys.ACTION_SUBMIT_VALUE);

				actionMsg.setDossierId(dossierId);

				actionMsg.setFileGroupId(fileGroupId);

				actionMsg.setLocale(serviceContext.getLocale());

				actionMsg.setUserId(serviceContext.getUserId());

				actionMsg.setGroupId(serviceContext.getScopeGroupId());

				actionMsg.setGovAgencyCode(dossier.getGovAgencyCode());

				actionMsg.setCompanyId(dossier.getCompanyId());

				actionMsg.setDossierStatus(dossierStatus);

				message.put("msgToEngine", actionMsg);

				break;

			default:
				break;
			}

			ActorBean actor = new ActorBean(1, serviceContext.getUserId());

			String msgInfo = StringPool.BLANK;

			if (Validator.isNotNull(dossier.getNote())) {
				msgInfo = dossier.getNote();
			}

			else {
				msgInfo = isSend ? LanguageUtil.get(serviceContext.getLocale(),
						"send-dossier") : LanguageUtil.get(
						serviceContext.getLocale(), "resend-dossier");
			}

			actionMsg.setActionNote(msgInfo);

			DossierLocalServiceUtil.updateDossierStatus(dossierId, fileGroupId,
					PortletConstants.DOSSIER_STATUS_SYSTEM,
					WebKeys.DOSSIER_ACTOR_CITIZEN, actor.getActorId(), actor
							.getActorName(), msgInfo, PortletUtil
							.getActionInfo(
									PortletConstants.DOSSIER_STATUS_SYSTEM,
									actionRequest.getLocale()),
					StringPool.BLANK,
					PortletConstants.DOSSIER_FILE_SYNC_STATUS_REQUIREDSYNC,
					PortletConstants.DOSSIER_LOG_NORMAL);

			MessageBusUtil.sendMessage("opencps/frontoffice/out/destination",
					message);

			// Add DossierLog

			/*
			 * DossierLogLocalServiceUtil.addDossierLog(
			 * serviceContext.getUserId(), serviceContext .getScopeGroupId(),
			 * serviceContext.getCompanyId(), dossierId, fileGroupId, isSend ?
			 * PortletConstants.DOSSIER_STATUS_NEW :
			 * PortletConstants.DOSSIER_STATUS_WAITING, isSend ?
			 * PortletConstants.DOSSIER_ACTION_SEND :
			 * PortletConstants.DOSSIER_ACTION_RESEND, msgInfo, new Date(), 0,
			 * 2, actor.getActor(), actor.getActorId(), actor.getActorName(),
			 * DossierMgtFrontOfficePortlet.class.getName() +
			 * ".updateDossierStatus()", 0, 0, true);
			 */

			ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
					.getAttribute(WebKeys.THEME_DISPLAY);

			// add default message success
			SessionMessages.add(actionRequest,
					MessageKeys.DEFAULT_SUCCESS_KEY_X, LanguageUtil.format(
							themeDisplay.getLocale(),
							MessageKeys.DEFAULT_SUCCESS_KEY_X,
							String.valueOf(dossier.getDossierId())));
			// lat co
			isUpdateStatusSuccessFlag = true;
		} catch (Exception e) {

			if (e instanceof NoSuchDossierException
					|| e instanceof NoSuchDossierTemplateException
					|| e instanceof RequiredDossierPartException) {

				SessionErrors.add(actionRequest, e.getClass());

			} else {
				SessionErrors.add(actionRequest,
						MessageKeys.DOSSIER_SYSTEM_EXCEPTION_OCCURRED);
			}

			_log.error(e);
		} finally {
			if (isUpdateStatusSuccessFlag) {
				
				if (Validator.isNotNull(backURL)) {
					actionResponse.sendRedirect(backURL);
					
				} else if (Validator.isNotNull(redirectURL)) {
					
					actionResponse.sendRedirect(redirectURL);
				}

			} else {

				if (Validator.isNotNull(backURL)) {
					actionResponse.sendRedirect(backURL);
				}
			}
		}
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void updateDynamicFormData(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

		DossierFile dossierFile = null;

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);
		long dossierPartId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);
		long dossierFileId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_FILE_ID);
		long fileGroupId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.FILE_GROUP_ID);
		long groupDossierPartId = ParamUtil.getLong(actionRequest,
				"groupDossierPartId");
		String redirectURL = ParamUtil.getString(actionRequest, "redirectURL");

		long fileEntryId = 0;

		// Default value
		int dossierFileMark = PortletConstants.DOSSIER_FILE_MARK_UNKNOW;
		int dossierFileType = PortletConstants.DOSSIER_FILE_TYPE_INPUT;
		int syncStatus = PortletConstants.DOSSIER_FILE_SYNC_STATUS_NOSYNC;
		int original = PortletConstants.DOSSIER_FILE_ORIGINAL;

		String formData = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.FORM_DATA);

		// Default value
		String dossierFileNo = StringPool.BLANK;
		String templateFileNo = StringPool.BLANK;
		String displayName = StringPool.BLANK;
		String groupName = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.GROUP_NAME);

		String regexStr = StringPool.BLANK;
		Date dossierFileDate = null;

		try {
			validateDynamicFormData(dossierId, dossierPartId, accountBean);

			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);

			DossierPart dossierPart = DossierPartLocalServiceUtil
					.getDossierPart(dossierPartId);

			if (Validator.isNotNull(dossierPart.getTemplateFileNo())) {
				templateFileNo = dossierPart.getTemplateFileNo();
			}

			if (Validator.isNotNull(dossierPart.getPartName())) {
				displayName = dossierPart.getPartName();
			}

			if (dossierFileId > 0) {
				DossierFile dossierFileDel = DossierFileLocalServiceUtil
						.getDossierFile(dossierFileId);
				if (Validator.isNotNull(dossierFileDel)
						&& dossierFileDel.getSyncStatus() == PortletConstants.DOSSIER_FILE_SYNC_STATUS_NOSYNC) {
					DossierFileLocalServiceUtil.deleteDossierFile(
							dossierFileId, dossierFileDel.getFileEntryId());
					regexStr = "_13_WAR_opencpsportlet_dossierFileId="
							+ dossierFileId;
				}
			}

			// #/issues/1112 create new dossierFile any case
			// if (dossierFileId == 0) {
			dossierFile = DossierFileLocalServiceUtil.addDossierFile(
					serviceContext.getUserId(), dossierId, dossierPartId,
					templateFileNo, groupName, fileGroupId, groupDossierPartId,
					accountBean.getOwnerUserId(),
					accountBean.getOwnerOrganizationId(), displayName,
					formData, fileEntryId, dossierFileMark, dossierFileType,
					dossierFileNo, dossierFileDate, original, syncStatus,
					serviceContext);

			if (Validator.isNotNull(dossierFile)) {
				JSONObject sampleDataJson = JSONFactoryUtil
						.createJSONObject(dossierPart.getSampleData());

				JSONObject formDataJson = JSONFactoryUtil
						.createJSONObject(dossierFile.getFormData());

				String dossierFileNoKey = sampleDataJson
						.getString(PortletConstants.DOSSIER_FILE_NO_KEY);
				String dossierFileDateKey = sampleDataJson
						.getString(PortletConstants.DOSSIER_FILE_NO_DATE);

				dossierFile.setDossierFileNo(formDataJson
						.getString(dossierFileNoKey));
				dossierFile.setDossierFileDate(DateTimeUtil
						.convertStringToDate(formDataJson
								.getString(dossierFileDateKey)));

				DossierFileLocalServiceUtil.updateDossierFile(dossierFile);
			}

			SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

		} catch (Exception e) {
			if (e instanceof NoSuchDossierException) {
				SessionErrors.add(actionRequest, NoSuchDossierException.class);
			} else if (e instanceof NoSuchDossierPartException) {
				SessionErrors.add(actionRequest,
						NoSuchDossierPartException.class);
			} else if (e instanceof NoSuchAccountException) {
				SessionErrors.add(actionRequest, NoSuchAccountException.class);
			} else if (e instanceof NoSuchAccountTypeException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountTypeException.class);
			} else if (e instanceof NoSuchAccountFolderException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountFolderException.class);
			} else if (e instanceof NoSuchAccountOwnUserIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnUserIdException.class);
			} else if (e instanceof NoSuchAccountOwnOrgIdException) {
				SessionErrors.add(actionRequest,
						NoSuchAccountOwnOrgIdException.class);
			} else if (e instanceof PermissionDossierException) {
				SessionErrors.add(actionRequest,
						PermissionDossierException.class);
			} else {
				SessionErrors.add(actionRequest, PortalException.class);
			}

			_log.error(e);
		} finally {

			if (Validator.isNotNull(dossierFile)
					&& Validator.isNotNull(redirectURL)
					&& Validator.isNotNull(regexStr)) {

				String newRegexStr = "_13_WAR_opencpsportlet_dossierFileId="
						+ dossierFile.getDossierFileId();

				redirectURL = redirectURL.replaceAll(regexStr, newRegexStr);

				actionResponse.sendRedirect(redirectURL);
			} else {
				actionResponse.setRenderParameter("primaryKey", String
						.valueOf(dossierFile != null ? dossierFile
								.getDossierFileId() : 0));
				actionResponse.setRenderParameter("content",
						"declaration-online");
				actionResponse
						.setRenderParameter("jspPage",
								"/html/portlets/dossiermgt/frontoffice/modal_dialog.jsp");
			}
		}
	}

	protected String getDataStringFromAlpaca(JSONObject dataJson, String key) {
		String data = StringPool.BLANK;
		data = dataJson.getString(key);
		return data;
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void updateTempDynamicFormData(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		long dossierPartId = ParamUtil.getLong(actionRequest,
				DossierFileDisplayTerms.DOSSIER_PART_ID);
		int index = ParamUtil.getInteger(actionRequest,
				DossierFileDisplayTerms.INDEX);
		String formData = ParamUtil.getString(actionRequest,
				DossierFileDisplayTerms.FORM_DATA);

		HttpServletRequest request = PortalUtil
				.getHttpServletRequest(actionRequest);

		request.setAttribute(WebKeys.FORM_DATA + String.valueOf(dossierPartId)
				+ StringPool.DASH + String.valueOf(index), formData);

		HttpSession session = request.getSession();
		session.setAttribute(WebKeys.FORM_DATA + String.valueOf(dossierPartId)
				+ StringPool.DASH + String.valueOf(index), formData);

		actionResponse.setRenderParameter("mvcPath",
				"/html/portlets/dossiermgt/frontoffice/dynamic_form.jsp");

		SessionMessages.add(actionRequest, MessageKeys.DEFAULT_SUCCESS_KEY);

	}

	/**
	 * @param dossierId
	 * @param partName
	 * @throws NoSuchDossierException
	 * @throws EmptyFileGroupException
	 * @throws DuplicateFileGroupException
	 */
	private void valiadateFileGroup(long dossierId, String partName)
			throws NoSuchDossierException, EmptyFileGroupException,
			DuplicateFileGroupException {

		if (dossierId <= 0) {
			throw new NoSuchDossierException();
		} else if (Validator.isNull(partName.trim())) {
			throw new EmptyFileGroupException();
		}

		int count = 0;

		try {
			count = FileGroupLocalServiceUtil.countByD_DN(dossierId,
					partName.trim());
		} catch (Exception e) {
		}

		if (count > 0) {
			throw new DuplicateFileGroupException();
		}
	}

	/**
	 * @param accountBean
	 * @throws NoSuchAccountTypeException
	 * @throws NoSuchAccountException
	 * @throws NoSuchAccountFolderException
	 * @throws NoSuchAccountOwnUserIdException
	 * @throws NoSuchAccountOwnOrgIdException
	 */
	private void validateAccount(AccountBean accountBean)
			throws NoSuchAccountTypeException, NoSuchAccountException,
			NoSuchAccountFolderException, NoSuchAccountOwnUserIdException,
			NoSuchAccountOwnOrgIdException {

		if (accountBean == null) {
			throw new NoSuchAccountException();
		} else if (Validator.isNull(accountBean.getAccountType())) {
			throw new NoSuchAccountTypeException();
		}

		else if (accountBean.isCitizen() && accountBean.getOwnerUserId() == 0) {
			throw new NoSuchAccountOwnUserIdException();
		}

		else if (accountBean.isBusiness()
				&& accountBean.getOwnerOrganizationId() == 0) {
			throw new NoSuchAccountOwnOrgIdException();
		}
	}

	/**
	 * @param dossierId
	 * @param dossierPartId
	 * @param dossierFileId
	 * @param displayName
	 * @param size
	 * @param sourceFileName
	 * @param inputStream
	 * @param accountBean
	 * @throws NoSuchDossierException
	 * @throws NoSuchDossierPartException
	 * @throws NoSuchAccountException
	 * @throws NoSuchAccountTypeException
	 * @throws NoSuchAccountFolderException
	 * @throws NoSuchAccountOwnUserIdException
	 * @throws NoSuchAccountOwnOrgIdException
	 * @throws PermissionDossierException
	 * @throws FileSizeException
	 */
	private void validateAddAttachDossierFile(long dossierId,
			long dossierPartId, long dossierFileId, String displayName,
			long size, String sourceFileName, InputStream inputStream,
			AccountBean accountBean, String fileTypes, float maxUploadFileSize,
			String maxUploadFileSizeUnit, float maxTotalUploadFileSize,
			String maxTotalUploadFileSizeUnit) throws NoSuchDossierException,
			NoSuchDossierPartException, NoSuchAccountException,
			NoSuchAccountTypeException, NoSuchAccountFolderException,
			NoSuchAccountOwnUserIdException, NoSuchAccountOwnOrgIdException,
			PermissionDossierException, FileSizeException,
			FileExtensionException {

		validateAccount(accountBean);

		if (dossierId <= 0) {
			throw new NoSuchDossierException();
		}

		if (dossierPartId < 0) {
			throw new NoSuchDossierPartException();
		}

		Dossier dossier = null;

		try {
			dossier = DossierLocalServiceUtil.getDossier(dossierId);
		} catch (Exception e) {
			throw new NoSuchDossierPartException();
		}

		if (accountBean.isBusiness()) {
			if (dossier.getOwnerOrganizationId() != accountBean
					.getOwnerOrganizationId()) {
				throw new PermissionDossierException();
			}

		} else if (accountBean.isCitizen()) {
			if (dossier.getUserId() != accountBean.getOwnerUserId()) {
				throw new PermissionDossierException();
			}

		}

		try {
			DossierPartLocalServiceUtil.getDossierPart(dossierPartId);
		} catch (Exception e) {
			throw new NoSuchDossierPartException();
		}

		String[] fileTypeArr = fileTypes.split("\\W+");

		if (fileTypeArr.length > 0) {

			boolean fileTypeIsAgreed = false;

			for (String fileType : fileTypeArr) {
				if (sourceFileName.toLowerCase().endsWith(fileType.toLowerCase())) {
					fileTypeIsAgreed = true;
				}
			}

			if (!fileTypeIsAgreed) {
				throw new FileExtensionException();
			}
		}

		FileSizeUnit uploadFileSizeUnit = FileSizeUnit
				.getEnum(maxUploadFileSizeUnit);

		float maxUploadFileSizeInByte = PortletUtil.convertSizeUnitToByte(
				maxUploadFileSize, uploadFileSizeUnit);

		FileSizeUnit totalUploadFileSizeUnit = FileSizeUnit
				.getEnum(maxUploadFileSizeUnit);
		float maxTotalUploadFileSizeInByte = PortletUtil.convertSizeUnitToByte(
				maxTotalUploadFileSize, totalUploadFileSizeUnit);

		if (size == 0) {
			throw new FileSizeException();
		} else if (size > maxUploadFileSizeInByte
				&& maxUploadFileSizeInByte > 0) {
			throw new FileSizeException();
		}

		List<DossierFile> dossierFileList = new ArrayList<DossierFile>();
		if (dossierId > 0) {
			try {
				dossierFileList = DossierFileLocalServiceUtil
						.getDossierFileByDossierId(dossierId);
			} catch (Exception e) {
			}
		}

		float totalUploadFileSizeInByte = 0;

		if (!dossierFileList.isEmpty()) {
			for (DossierFile tempDossierFile : dossierFileList) {
				if (tempDossierFile.getRemoved() == 0) {
					long fileEntryId = tempDossierFile.getFileEntryId();

					DLFileEntry fileEntry = null;
					try {
						fileEntry = DLFileEntryLocalServiceUtil
								.getDLFileEntry(fileEntryId);
					} catch (Exception e) {
					}

					if (Validator.isNotNull(fileEntry)) {
						totalUploadFileSizeInByte += fileEntry.getSize();
					}
				}
			}
		}

		totalUploadFileSizeInByte += size;

		if (totalUploadFileSizeInByte > maxTotalUploadFileSizeInByte
				&& maxTotalUploadFileSizeInByte > 0) {
			throw new FileSizeException();
		}
	}

	/**
	 * @param dossierId
	 * @param dossierPartId
	 * @param dossierFileId
	 * @param accountBean
	 * @throws NoSuchDossierException
	 * @throws NoSuchDossierPartException
	 * @throws NoSuchAccountException
	 * @throws NoSuchAccountTypeException
	 * @throws NoSuchAccountFolderException
	 * @throws NoSuchAccountOwnUserIdException
	 * @throws NoSuchAccountOwnOrgIdException
	 * @throws PermissionDossierException
	 * @throws FileSizeException
	 * @throws NoSuchDossierFileException
	 * @throws NoSuchFileEntryException
	 */
	private void validateCloneDossierFile(long dossierId, long dossierPartId,
			long dossierFileId, AccountBean accountBean)
			throws NoSuchDossierException, NoSuchDossierPartException,
			NoSuchAccountException, NoSuchAccountTypeException,
			NoSuchAccountFolderException, NoSuchAccountOwnUserIdException,
			NoSuchAccountOwnOrgIdException, PermissionDossierException,
			NoSuchDossierFileException, NoSuchFileEntryException {

		validateAccount(accountBean);

		if (dossierFileId <= 0) {
			throw new NoSuchDossierFileException();
		}

		DossierFile dossierFile = null;

		try {
			dossierFile = DossierFileLocalServiceUtil
					.getDossierFile(dossierFileId);
		} catch (Exception e) {

		}

		if (dossierFile == null) {
			throw new NoSuchDossierFileException();
		}

		if (dossierFile.getFileEntryId() <= 0) {
			throw new NoSuchFileEntryException();
		}

		if (dossierId <= 0) {
			throw new NoSuchDossierException();
		}

		if (dossierPartId < 0) {
			throw new NoSuchDossierPartException();
		}

		Dossier dossier = null;

		try {
			dossier = DossierLocalServiceUtil.getDossier(dossierId);
		} catch (Exception e) {
			throw new NoSuchDossierPartException();
		}

		if (accountBean.isBusiness()) {
			if (dossier.getOwnerOrganizationId() != accountBean
					.getOwnerOrganizationId()) {
				throw new PermissionDossierException();
			}

		} else if (accountBean.isCitizen()) {
			if (dossier.getUserId() != accountBean.getOwnerUserId()) {
				throw new PermissionDossierException();
			}

		}

		try {
			DossierPartLocalServiceUtil.getDossierPart(dossierPartId);
		} catch (Exception e) {
			throw new NoSuchDossierPartException();
		}
	}

	/**
	 * @param dossierFileId
	 * @param accountBean
	 * @throws NoSuchAccountTypeException
	 * @throws NoSuchAccountException
	 * @throws NoSuchAccountFolderException
	 * @throws NoSuchAccountOwnUserIdException
	 * @throws NoSuchAccountOwnOrgIdException
	 * @throws NoSuchDossierFileException
	 */
	private void validateCreateDynamicForm(long dossierFileId,
			AccountBean accountBean) throws NoSuchAccountTypeException,
			NoSuchAccountException, NoSuchAccountFolderException,
			NoSuchAccountOwnUserIdException, NoSuchAccountOwnOrgIdException,
			NoSuchDossierFileException {

		validateAccount(accountBean);

		if (dossierFileId < 0) {
			throw new NoSuchDossierFileException();
		}

		DossierFile dossierFile = null;

		try {
			dossierFile = DossierFileLocalServiceUtil
					.getDossierFile(dossierFileId);
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (dossierFile == null) {
			throw new NoSuchDossierFileException();
		}
	}

	/**
	 * @param dossierId
	 * @param accountBean
	 * @throws NoSuchAccountTypeException
	 * @throws NoSuchAccountException
	 * @throws NoSuchAccountFolderException
	 * @throws NoSuchAccountOwnUserIdException
	 * @throws NoSuchAccountOwnOrgIdException
	 * @throws NoSuchDossierException
	 */
	private void validateDeleteDossier(long dossierId, AccountBean accountBean)
			throws NoSuchAccountTypeException, NoSuchAccountException,
			NoSuchAccountFolderException, NoSuchAccountOwnUserIdException,
			NoSuchAccountOwnOrgIdException, NoSuchDossierException {

		validateAccount(accountBean);

		if (dossierId <= 0) {
			throw new NoSuchDossierException();
		}

		if (dossierId > 0) {
			Dossier dossier = null;

			try {
				dossier = DossierLocalServiceUtil.getDossier(dossierId);
			} catch (Exception e) {
			}

			if (dossier == null) {
				throw new NoSuchDossierException();
			}
		}
	}

	/**
	 * @param cityId
	 * @param districtId
	 * @param wardId
	 * @param accountType
	 * @param dossierDestinationFolder
	 * @param subjectName
	 * @param subjectId
	 * @param address
	 * @param contactName
	 * @param contactTelNo
	 * @param contactEmail
	 * @throws EmptyDossierCityCodeException
	 * @throws EmptyDossierDistrictCodeException
	 * @throws EmptyDossierWardCodeException
	 * @throws InvalidDossierObjectException
	 * @throws CreateDossierFolderException
	 * @throws EmptyDossierSubjectNameException
	 * @throws OutOfLengthDossierSubjectNameException
	 * @throws EmptyDossierSubjectIdException
	 * @throws OutOfLengthDossierSubjectIdException
	 * @throws EmptyDossierAddressException
	 * @throws OutOfLengthDossierContactEmailException
	 * @throws OutOfLengthDossierContactNameException
	 * @throws OutOfLengthDossierContactTelNoException
	 * @throws EmptyDossierContactNameException
	 * @throws OutOfLengthDossierAddressException
	 * @throws InvalidDossierObjectException
	 * @throws EmptyDossierFileException
	 */
	@Deprecated
	private void validateDossier(long cityId, long districtId, long wardId,
			String accountType, String dossierDestinationFolder,
			String subjectName, String subjectId, String address,
			String contactName, String contactTelNo, String contactEmail)
			throws EmptyDossierCityCodeException,
			EmptyDossierDistrictCodeException, EmptyDossierWardCodeException,
			InvalidDossierObjectException, CreateDossierFolderException,
			EmptyDossierSubjectNameException,
			OutOfLengthDossierSubjectNameException,
			EmptyDossierSubjectIdException,
			OutOfLengthDossierSubjectIdException, EmptyDossierAddressException,
			OutOfLengthDossierContactEmailException,
			OutOfLengthDossierContactNameException,
			OutOfLengthDossierContactTelNoException,
			EmptyDossierContactNameException,
			OutOfLengthDossierAddressException, InvalidDossierObjectException,
			EmptyDossierFileException {

		if (cityId <= 0) {
			throw new EmptyDossierCityCodeException();
		}

		if (districtId <= 0) {
			throw new EmptyDossierDistrictCodeException();
		}

		if (wardId <= 0) {
			throw new EmptyDossierWardCodeException();
		}

		if (Validator.isNull(accountType)) {
			throw new InvalidDossierObjectException();
		}

		if (Validator.isNull(dossierDestinationFolder)) {
			throw new CreateDossierFolderException();
		}

		if (Validator.isNull(subjectName)) {
			throw new EmptyDossierSubjectNameException();
		}

		if (subjectName.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_SUBJECT_NAME_LENGTH) {
			throw new OutOfLengthDossierSubjectNameException();
		}

		if (subjectId.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_SUBJECT_ID_LENGTH) {
			throw new OutOfLengthDossierSubjectIdException();
		}

		if (Validator.isNull(address)) {
			throw new EmptyDossierAddressException();
		}

		if (address.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_ADDRESS_LENGTH) {
			throw new OutOfLengthDossierAddressException();
		}

		if (Validator.isNull(contactName)) {
			throw new EmptyDossierContactNameException();
		}

		if (contactName.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_CONTACT_NAME_LENGTH) {
			throw new OutOfLengthDossierContactNameException();
		}

		if (contactTelNo.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_CONTACT_TEL_NO_LENGTH) {
			throw new OutOfLengthDossierContactTelNoException();
		}

		if (contactEmail.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_CONTACT_EMAIL_LENGTH) {
			throw new OutOfLengthDossierContactEmailException();
		}
	}

	/**
	 * @param cityCode
	 * @param districtCode
	 * @param wardCode
	 * @param accountType
	 * @param dossierDestinationFolder
	 * @param subjectName
	 * @param subjectId
	 * @param address
	 * @param contactName
	 * @param contactTelNo
	 * @param contactEmail
	 * @throws InvalidDossierObjectException
	 * @throws SystemException
	 * @throws PortalException
	 */
	private void validateDossier(String cityCode, String districtCode,
			String wardCode, String accountType,
			String dossierDestinationFolder, String subjectName,
			String subjectId, String address, String contactName,
			String contactTelNo, String contactEmail) throws PortalException,
			SystemException {

		if (Validator.isNull(cityCode)) {
			throw new EmptyDossierCityCodeException();
		}

		if (Validator.isNull(districtCode)) {
			throw new EmptyDossierDistrictCodeException();
		}

		if (Validator.isNull(wardCode)) {
			throw new EmptyDossierWardCodeException();
		}
		// TODO Validate reference of:
		// city_
		// |_district_
		// |_ward

		if (Validator.isNull(accountType)) {
			throw new InvalidDossierObjectException();
		}

		if (Validator.isNull(dossierDestinationFolder)) {
			throw new CreateDossierFolderException();
		}

		if (Validator.isNull(subjectName)) {
			throw new EmptyDossierSubjectNameException();
		}

		if (subjectName.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_SUBJECT_NAME_LENGTH) {
			throw new OutOfLengthDossierSubjectNameException();
		}

		if (subjectId.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_SUBJECT_ID_LENGTH) {
			throw new OutOfLengthDossierSubjectIdException();
		}

		if (Validator.isNull(address)) {
			throw new EmptyDossierAddressException();
		}

		if (address.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_ADDRESS_LENGTH) {
			throw new OutOfLengthDossierAddressException();
		}

		if (Validator.isNull(contactName)) {
			throw new EmptyDossierContactNameException();
		}

		if (contactName.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_CONTACT_NAME_LENGTH) {
			throw new OutOfLengthDossierContactNameException();
		}

		if (contactTelNo.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_CONTACT_TEL_NO_LENGTH) {
			throw new OutOfLengthDossierContactTelNoException();
		}

		if (contactEmail.trim().length() > PortletPropsValues.DOSSIERMGT_DOSSIER_CONTACT_EMAIL_LENGTH) {
			throw new OutOfLengthDossierContactEmailException();
		}
	}

	/**
	 * @param dossierId
	 * @param dossierPartId
	 * @param accountBean
	 * @throws NoSuchAccountTypeException
	 * @throws NoSuchAccountException
	 * @throws NoSuchAccountFolderException
	 * @throws NoSuchAccountOwnUserIdException
	 * @throws NoSuchAccountOwnOrgIdException
	 * @throws NoSuchDossierException
	 * @throws NoSuchDossierPartException
	 * @throws PermissionDossierException
	 */
	private void validateDynamicFormData(long dossierId, long dossierPartId,
			AccountBean accountBean) throws NoSuchAccountTypeException,
			NoSuchAccountException, NoSuchAccountFolderException,
			NoSuchAccountOwnUserIdException, NoSuchAccountOwnOrgIdException,
			NoSuchDossierException, NoSuchDossierPartException,
			PermissionDossierException {

		validateAccount(accountBean);
		if (dossierId <= 0) {
			throw new NoSuchDossierException();
		}

		if (dossierPartId < 0) {
			throw new NoSuchDossierPartException();
		}

		Dossier dossier = null;

		try {
			dossier = DossierLocalServiceUtil.getDossier(dossierId);
		} catch (Exception e) {
			throw new NoSuchDossierPartException();
		}

		if (accountBean.isBusiness()) {
			if (dossier.getOwnerOrganizationId() != accountBean
					.getOwnerOrganizationId()) {
				throw new PermissionDossierException();
			}

		} else if (accountBean.isCitizen()) {
			if (dossier.getUserId() != accountBean.getOwnerUserId()) {
				throw new PermissionDossierException();
			}

		}

		try {
			DossierPartLocalServiceUtil.getDossierPart(dossierPartId);
		} catch (Exception e) {
			throw new NoSuchDossierPartException();
		}
	}

	/**
	 * @param renderRequest
	 * @param renderResponse
	 */
	private void validatePermission(RenderRequest renderRequest,
			RenderResponse renderResponse) {

		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		AccountBean accountBean = AccountUtil.getAccountBean(renderRequest);

		long ownerId = 0;

		if (accountBean == null
				|| (!accountBean.isBusiness() && !accountBean.isCitizen())) {
			setHasPermission(false);
			return;
		} else {
			ownerId = accountBean.isBusiness() ? accountBean
					.getOwnerOrganizationId() : accountBean.getOwnerUserId();

			long dossierId = ParamUtil.getLong(renderRequest,
					DossierDisplayTerms.DOSSIER_ID);

			long dossierFileId = ParamUtil.getLong(renderRequest,
					DossierFileDisplayTerms.DOSSIER_FILE_ID);

			try {
				if (dossierId > 0) {
					Dossier dossier = DossierLocalServiceUtil
							.getDossier(dossierId);
					if (dossier.getUserId() != themeDisplay.getUserId()) {
						setHasPermission(false);
						return;
					}
				}

				if (dossierFileId > 0) {
					DossierFile dossierFile = DossierFileLocalServiceUtil
							.getDossierFile(dossierFileId);

					if (dossierFile.getOwnerUserId() > 0
							&& dossierFile.getOwnerUserId() != ownerId) {
						setHasPermission(false);
						return;
					}

					if (dossierFile.getOwnerOrganizationId() > 0
							&& dossierFile.getOwnerOrganizationId() != ownerId) {
						setHasPermission(false);
						return;
					}

				}
			} catch (Exception e) {
				_log.info("Resource does not exist width " + "dossierId="
						+ dossierId + " dossierFileId=" + dossierFileId
						+ " ownerId=" + ownerId + " account="
						+ accountBean.getAccountType());
				_hasPermission = false;
			}
		}
	}

	/**
	 * @param dossierId
	 * @throws NoSuchDossierException
	 * @throws NoSuchDossierTemplateException
	 * @throws RequiredDossierPartException
	 */
	private void validateSubmitDossier(long dossierId)
			throws NoSuchDossierException, NoSuchDossierTemplateException,
			RequiredDossierPartException {

		if (dossierId <= 0) {
			throw new NoSuchDossierException();
		}

		Dossier dossier = null;

		try {
			dossier = DossierLocalServiceUtil.getDossier(dossierId);
		} catch (Exception e) {
		}

		if (dossier == null) {
			throw new NoSuchDossierException();
		}

		DossierTemplate dossierTemplate = null;

		try {
			dossierTemplate = DossierTemplateLocalServiceUtil
					.getDossierTemplate(dossier.getDossierTemplateId());
		} catch (Exception e) {
		}

		if (dossierTemplate == null) {
			throw new NoSuchDossierTemplateException();
		}

		List<DossierPart> dossierPartsLevel1 = new ArrayList<DossierPart>();

		try {
			dossierPartsLevel1 = DossierPartLocalServiceUtil
					.getDossierPartsByT_P(
							dossierTemplate.getDossierTemplateId(), 0);
		} catch (Exception e) {

		}

		List<Long> requiredDossierPartIds = new ArrayList<Long>();

		if (dossierPartsLevel1 != null) {
			for (DossierPart dossierPartLevel1 : dossierPartsLevel1) {
				List<DossierPart> dossierParts = DossierMgtUtil
						.getTreeDossierPart(dossierPartLevel1
								.getDossierpartId());

				for (DossierPart dossierPart : dossierParts) {
					if (dossierPart.getPartType() != PortletConstants.DOSSIER_PART_TYPE_RESULT
							&& dossierPart.getPartType() != PortletConstants.DOSSIER_PART_TYPE_MULTIPLE_RESULT
							&& dossierPart.getPartType() != PortletConstants.DOSSIER_PART_TYPE_OPTION
							&& dossierPart.getRequired()) {
						DossierFile dossierFile = null;
						try {
							dossierFile = DossierFileLocalServiceUtil
									.getDossierFileInUse(dossierId,
											dossierPart.getDossierpartId());
						} catch (Exception e) {
							// TODO: handle exception
						}

						requiredDossierPartIds = PortletUtil
								.getDossierPartRequired(requiredDossierPartIds,
										dossierPartLevel1, dossierPart,
										dossierFile);

					}

				}
			}
		}

		if (requiredDossierPartIds != null && !requiredDossierPartIds.isEmpty()) {
			throw new RequiredDossierPartException();
		}
	}

	public void menuCounterAction(ActionRequest actionRequest,
			ActionResponse actionResponse) throws PortalException,
			SystemException, IOException {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		long groupId = themeDisplay.getScopeGroupId();

		// now read your parameters, e.g. like this:
		// long someParameter = ParamUtil.getLong(request, "someParameter");

		// String keywords = ParamUtil.getString(actionRequest, "keywords");

		long serviceDomainId = ParamUtil.getLong(actionRequest,
				"serviceDomainId");

		DictItem domainItem = null;

		if (serviceDomainId > 0) {
			domainItem = DictItemLocalServiceUtil
					.fetchDictItem(serviceDomainId);
		}

		long counterVal = 0;
		JSONObject obj = null;
		for (DictItem item : PortletUtil.getDossierStatus(groupId)) {
			obj = JSONFactoryUtil.createJSONObject();

			counterVal = DossierLocalServiceUtil.countDossierByUser(groupId,
					themeDisplay.getUserId(), StringPool.BLANK, Validator
							.isNotNull(domainItem) ? domainItem.getTreeIndex()
							: StringPool.BLANK, item.getItemCode());

			obj.put("code", item.getItemCode());
			obj.put("counter", String.valueOf(counterVal));
			jsonArray.put(obj);
		}

		jsonObject.put("badge", jsonArray);
		PortletUtil.writeJSON(actionRequest, actionResponse, jsonObject);
	}

	public void keywordsAutoComplete(ActionRequest actionRequest,
			ActionResponse actionResponse) throws PortalException,
			SystemException, IOException {

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		String keywords = ParamUtil.getString(actionRequest, "keywords");

		long administrationId = ParamUtil.getLong(actionRequest,
				"administrationId");

		List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();

		DictItem domainItem = null;

		String administrationIndex = StringPool.BLANK;

		if (administrationId > 0) {

			domainItem = DictItemLocalServiceUtil.getDictItem(administrationId);

			administrationIndex = domainItem.getTreeIndex();

		}

		serviceInfos = ServiceInfoLocalServiceUtil
				.getServiceInFosByG_DI_Status(themeDisplay.getScopeGroupId(),
						StringPool.BLANK, administrationIndex, 1, keywords,
						QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		for (ServiceInfo serviceInfo : serviceInfos) {

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

			jsonObject.put("serviceinfoId",
					String.valueOf(serviceInfo.getServiceinfoId()));

			jsonObject.put("serviceName", serviceInfo.getServiceName());

			jsonArray.put(jsonObject);
		}

		PortletUtil.writeJSON(actionRequest, actionResponse, jsonArray);
	}

	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 */
	public void quickUpdateDossier(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {

		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest
				.getAttribute(WebKeys.THEME_DISPLAY);

		AccountBean accountBean = AccountUtil.getAccountBean(actionRequest);

		HttpServletRequest request = PortalUtil
				.getHttpServletRequest(actionRequest);

		HttpSession session = request.getSession();

		String accountType = GetterUtil.getString(session
				.getAttribute(WebKeys.ACCOUNT_TYPE));

		Dossier dossier = null;

		long dossierId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_ID);
		long dossierTemplateId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.DOSSIER_TEMPLATE_ID);
		long serviceInfoId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.SERVICE_INFO_ID);
		// long cityId = ParamUtil.getLong(actionRequest,
		// DossierDisplayTerms.CITY_ID);
		// long districtId = ParamUtil.getLong(actionRequest,
		// DossierDisplayTerms.DISTRICT_ID);
		// long wardId = ParamUtil.getLong(actionRequest,
		// DossierDisplayTerms.WARD_ID);

		String cityCode = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.CITY_CODE);
		String districtCode = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.DISTRICT_CODE);
		String wardCode = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.WARD_CODE);
		long serviceConfigId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.SERVICE_CONFIG_ID);

		long govAgencyOrganizationId = ParamUtil.getLong(actionRequest,
				DossierDisplayTerms.GOVAGENCY_ORGANIZATION_ID);

		long ownerOrganizationId = GetterUtil.getLong(session
				.getAttribute(WebKeys.ACCOUNT_OWNERORGANIZATIONID));

		int serviceMode = ParamUtil.getInteger(actionRequest,
				DossierDisplayTerms.SERVICE_MODE);
		String serviceDomainIndex = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.SERVICE_DOMAIN_INDEX);

		String govAgencyCode = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.GOVAGENCY_CODE);
		String govAgencyName = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.GOVAGENCY_NAME);

		String serviceAdministrationIndex = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.SERVICE_ADMINISTRATION_INDEX);
		String templateFileNo = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.TEMPLATE_FILE_NO);
		String subjectName = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.SUBJECT_NAME);
		String subjectId = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.SUBJECT_ID);
		String address = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.ADDRESS);
		String contactName = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.CONTACT_NAME);
		String contactTelNo = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.CONTACT_TEL_NO);
		String contactEmail = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.CONTACT_EMAIL);
		String note = ParamUtil.getString(actionRequest,
				DossierDisplayTerms.NOTE);

		String backURL = ParamUtil.getString(actionRequest, "backURL");

		String redirectURL = ParamUtil.getString(actionRequest, "redirectURL");

		String redirectPaymentURL = ParamUtil.getString(request,
				DossierDisplayTerms.REDIRECT_PAYMENT_URL);

		boolean isEditDossier = ParamUtil.getBoolean(request, "isEditDossier");

		boolean update = false;

		try {
			ServiceContext serviceContext = ServiceContextFactory
					.getInstance(actionRequest);

			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);

			if (dossierId > 0) {
				dossier = DossierLocalServiceUtil.getDossier(dossierId);
			}

			String dossierDestinationFolder = StringPool.BLANK;

			SplitDate splitDate = PortletUtil.splitDate(new Date());

			dossierDestinationFolder = PortletUtil.getDossierDestinationFolder(
					serviceContext.getScopeGroupId(), splitDate.getYear(),
					splitDate.getMonth(), splitDate.getDayOfMoth());

			if (dossier != null) {
				dossierDestinationFolder += StringPool.SLASH + dossier.getOid();
			}

			String cityName = StringPool.BLANK;
			String districtName = StringPool.BLANK;
			String wardName = StringPool.BLANK;

			DictItem city = null;

			DictItem district = null;

			DictItem ward = null;

			if (accountBean.isCitizen()) {
				Citizen citizen = (Citizen) accountBean.getAccountInstance();
				city = DictItemLocalServiceUtil.getDictItemByCode(citizen
						.getCityCode());
				district = DictItemLocalServiceUtil.getDictItemByCode(citizen
						.getDistrictCode());
				ward = DictItemLocalServiceUtil.getDictItemByCode(citizen
						.getWardCode());
			}

			if (accountBean.isBusiness()) {
				Business business = (Business) accountBean.getAccountInstance();
				city = DictItemLocalServiceUtil.getDictItemByCode(business
						.getCityCode());
				district = DictItemLocalServiceUtil.getDictItemByCode(business
						.getDistrictCode());
				ward = DictItemLocalServiceUtil.getDictItemByCode(business
						.getWardCode());
			}

			if (city != null) {
				cityCode = city.getItemCode();
				cityName = city.getItemName(themeDisplay.getLocale());

			}

			if (district != null) {
				districtCode = district.getItemCode();
				districtName = district.getItemName(themeDisplay.getLocale());

			}

			if (ward != null) {
				wardCode = ward.getItemCode();
				wardName = ward.getItemName(themeDisplay.getLocale());

			}

			validateDossier(cityCode, districtCode, wardCode, accountType,
					dossierDestinationFolder, subjectName, subjectId, address,
					contactName, contactTelNo, contactEmail);

			DLFolder dossierFolder = DLFolderUtil.getTargetFolder(
					serviceContext.getUserId(),
					serviceContext.getScopeGroupId(),
					serviceContext.getScopeGroupId(), false, 0,
					dossierDestinationFolder, StringPool.BLANK, false,
					serviceContext);

			if (dossierId == 0) {
				dossier = DossierLocalServiceUtil.addDossier(
						serviceContext.getUserId(), ownerOrganizationId,
						dossierTemplateId, templateFileNo, serviceConfigId,
						serviceInfoId, serviceDomainIndex,
						govAgencyOrganizationId, govAgencyCode, govAgencyName,
						serviceMode, serviceAdministrationIndex, cityCode,
						cityName, districtCode, districtName, wardName,
						wardCode, subjectName, subjectId, address, contactName,
						contactTelNo, contactEmail, note,
						PortletConstants.DOSSIER_SOURCE_DIRECT,
						PortletConstants.DOSSIER_STATUS_NEW,
						dossierFolder.getFolderId(), redirectPaymentURL,
						serviceContext);

			} else {
				dossier = DossierLocalServiceUtil.updateDossier(dossierId,
						serviceContext.getUserId(), ownerOrganizationId,
						dossierTemplateId, templateFileNo, serviceConfigId,
						serviceInfoId, serviceDomainIndex,
						govAgencyOrganizationId, govAgencyCode, govAgencyName,
						serviceMode, serviceAdministrationIndex, cityCode,
						cityName, districtCode, districtName, wardName,
						wardCode, subjectName, subjectId, address, contactName,
						contactTelNo, contactEmail, note,

						dossierFolder.getFolderId(), serviceContext);
			}

			// Add DossierLog (for Update dossier)

			ActorBean actor = new ActorBean(1, serviceContext.getUserId());

			DossierLogLocalServiceUtil.addDossierLog(
					serviceContext.getUserId(),
					serviceContext.getScopeGroupId(),
					serviceContext.getCompanyId(), dossier.getDossierId(), 0,
					PortletConstants.DOSSIER_STATUS_NEW,
					PortletConstants.DOSSIER_STATUS_NEW,
					PortletConstants.DOSSIER_STATUS_NEW, new Date(), 0, 0,
					actor.getActor(), actor.getActorId(), actor.getActorName(),
					DossierMgtFrontOfficePortlet.class.getName()
							+ ".updateDossier()", 0, 0, false);

			SessionMessages.add(actionRequest,
					MessageKeys.DOSSIER_UPDATE_SUCCESS);

			update = true;
		} catch (Exception e) {
			update = false;
			if (e instanceof EmptyDossierCityCodeException
					|| e instanceof EmptyDossierDistrictCodeException
					|| e instanceof EmptyDossierWardCodeException
					|| e instanceof InvalidDossierObjectException
					|| e instanceof CreateDossierFolderException
					|| e instanceof EmptyDossierSubjectNameException
					|| e instanceof OutOfLengthDossierSubjectNameException
					|| e instanceof EmptyDossierSubjectIdException
					|| e instanceof OutOfLengthDossierSubjectIdException
					|| e instanceof EmptyDossierAddressException
					|| e instanceof OutOfLengthDossierContactEmailException
					|| e instanceof OutOfLengthDossierContactNameException
					|| e instanceof OutOfLengthDossierContactTelNoException
					|| e instanceof EmptyDossierContactNameException
					|| e instanceof OutOfLengthDossierAddressException
					|| e instanceof EmptyDossierFileException
					|| e instanceof DuplicateFolderNameException) {

				SessionErrors.add(actionRequest, e.getClass());
			} else {
				SessionErrors.add(actionRequest,
						MessageKeys.DOSSIER_SYSTEM_EXCEPTION_OCCURRED);
			}

			_log.error(e);

		} finally {
			if (update) {
				if (Validator.isNotNull(redirectURL)) {

					actionResponse.sendRedirect(redirectURL + "&_"
							+ WebKeys.DOSSIER_MGT_PORTLET + "_dossierId="
							+ dossier.getDossierId());

				}
			} else {

				actionResponse.setRenderParameter("backURL", backURL);

				actionResponse.setRenderParameter(
						DossierDisplayTerms.SERVICE_CONFIG_ID,
						String.valueOf(serviceConfigId));
				actionResponse.setRenderParameter(
						DossierDisplayTerms.DOSSIER_ID, String
								.valueOf(dossier != null ? dossier
										.getDossierId() : 0));

				actionResponse.setRenderParameter("isEditDossier",
						String.valueOf(isEditDossier));

				actionResponse
						.setRenderParameter("mvcPath",
								"/html/portlets/dossiermgt/frontoffice/edit_dossier.jsp");
			}

		}
	}

	public void updateDossierSuggestion(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {
		long dossierSuggestionId = ParamUtil.getLong(actionRequest,
				"dossierSuggestionId");
		long dossierId = ParamUtil.getLong(actionRequest, "dossierId");

		ServiceContext serviceContext;

		boolean clone = false;

		try {
			serviceContext = ServiceContextFactory.getInstance(actionRequest);

			if (dossierSuggestionId > 0 && dossierId > 0) {
				Dossier dossierSuggestion = DossierLocalServiceUtil
						.getDossier(dossierSuggestionId);

				Dossier dossier = DossierLocalServiceUtil.getDossier(dossierId);

				List<DossierPart> dossierPartsSuggestion = DossierPartLocalServiceUtil
						.getDossierParts(dossierSuggestion
								.getDossierTemplateId());

				List<DossierPart> dossierParts = DossierPartLocalServiceUtil
						.getDossierParts(dossier.getDossierTemplateId());

				for (DossierPart dossierPartSuggestion : dossierPartsSuggestion) {

					if (dossierPartSuggestion.getPartType() == PortletConstants.DOSSIER_PART_TYPE_MULTIPLE_RESULT
							&& dossierPartSuggestion.getPartType() == PortletConstants.DOSSIER_PART_TYPE_RESULT) {
						continue;
					}

					for (DossierPart dossierPart : dossierParts) {

						if (dossierPartSuggestion.getPartType() == dossierPart
								.getPartType()
								&& dossierPartSuggestion.getPartNo()
										.equalsIgnoreCase(
												dossierPart.getPartNo())
								&& dossierPart.getPartType() != PortletConstants.DOSSIER_PART_TYPE_MULTIPLE_RESULT
								&& dossierPart.getPartType() != PortletConstants.DOSSIER_PART_TYPE_RESULT) {

							try {
								DossierFile dossierFileSuggestion = DossierFileLocalServiceUtil
										.getDossierFileInUse(
												dossierSuggestionId,
												dossierPartSuggestion
														.getDossierpartId());

								DLFileEntry fileEntry = DLFileEntryLocalServiceUtil
										.getDLFileEntry(dossierFileSuggestion
												.getFileEntryId());

								if (Validator.isNotNull(fileEntry
										.getContentStream())) {
									DossierFileLocalServiceUtil
											.addDossierFile(
													dossierFileSuggestion
															.getUserId(),
													dossierId,
													dossierPart
															.getDossierpartId(),
													dossierFileSuggestion
															.getTemplateFileNo(),
													StringPool.BLANK,
													0,// dossierFile.getGroupFileId()
													0,
													dossierFileSuggestion
															.getOwnerUserId(),
													dossierFileSuggestion
															.getOwnerOrganizationId(),
													dossierFileSuggestion
															.getDisplayName(),
													dossierFileSuggestion
															.getFormData(),
													dossierFileSuggestion != null ? dossierFileSuggestion
															.getFileEntryId()
															: 0,
													dossierFileSuggestion
															.getDossierFileMark(),
													dossierFileSuggestion
															.getDossierFileType(),
													dossierFileSuggestion
															.getDossierFileNo(),
													dossierFileSuggestion
															.getDossierFileDate(),
													dossierFileSuggestion
															.getOriginal(),
													PortletConstants.DOSSIER_FILE_SYNC_STATUS_NOSYNC,
													dossier.getFolderId(),
													fileEntry.getTitle(),
													fileEntry.getMimeType(),
													fileEntry.getTitle(),
													fileEntry.getDescription(),
													StringPool.BLANK,
													fileEntry
															.getContentStream(),
													fileEntry.getSize(),
													dossierFileSuggestion
															.getSignCheck(),
													dossierFileSuggestion
															.getSignInfo(),
													serviceContext);
								}

							} catch (Exception e) {
								_log.info(e.getMessage());

								continue;
							}
						}
					}
				}
			}
			clone = true;
			SessionMessages.add(actionRequest, MessageKeys.SUGGEST_SUCESS);

		} catch (Exception e) {
			clone = false;
			_log.error(e);
		} finally {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
			if (clone) {
				jsonObject.put("msg", "success");

			} else {
				jsonObject.put("msg", "error");
			}
			PortletUtil.writeJSON(actionRequest, actionResponse, jsonObject);
		}
	}

	public void deleteDossierSuggesstion(ActionRequest actionRequest,
			ActionResponse actionResponse) throws IOException {
		long dossierId = ParamUtil.getLong(actionRequest, "dossierId");
		String currentURL = ParamUtil.getString(actionRequest, "currentURL");
		try {
			if (dossierId > 0) {
				DossierLocalServiceUtil
						.deleteAllDossierFilesByDossierId(dossierId);
			}
		} catch (Exception e) {
			_log.error(e);
		} finally {
			actionResponse.sendRedirect(currentURL);
		}
	}

	private boolean _hasPermission = true;

	public boolean hasPermission() {

		return _hasPermission;
	}

	public void setHasPermission(boolean hasPermission) {

		this._hasPermission = hasPermission;
	}

	private Log _log = LogFactoryUtil.getLog(DossierMgtFrontOfficePortlet.class
			.getName());
	
	
}
