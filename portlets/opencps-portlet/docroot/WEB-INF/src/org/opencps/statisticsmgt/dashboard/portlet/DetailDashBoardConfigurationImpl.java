
package org.opencps.statisticsmgt.dashboard.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;

import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;

/**
 * @author trungnt
 */
public class DetailDashBoardConfigurationImpl
	extends DefaultConfigurationAction {

	@Override
	public void processAction(
		PortletConfig portletConfig, ActionRequest actionRequest,
		ActionResponse actionResponse)
		throws Exception {

		// TODO Auto-generated method stub
		String portletResource =
			ParamUtil.getString(actionRequest, "portletResource");

		PortletPreferences preferences =
			PortletPreferencesFactoryUtil.getPortletSetup(
				actionRequest, portletResource);
		String displayStyle =
			ParamUtil.getString(actionRequest, "displayStyle");

		String chartTitle = ParamUtil.getString(actionRequest, "chartTitle");

		String govCodes = ParamUtil.getString(actionRequest, "govCodes");

		String domainCodes = ParamUtil.getString(actionRequest, "domainCodes");

		String filterKey = ParamUtil.getString(actionRequest, "filterKey");

		String chartType = ParamUtil.getString(actionRequest, "chartType");

		String xaxisUnit = ParamUtil.getString(actionRequest, "xaxisUnit");

		String yaxisUnit = ParamUtil.getString(actionRequest, "yaxisUnit");

		/*
		 * String fieldTotalFormula = ParamUtil.getString(actionRequest,
		 * "fieldTotalFormula"); String fieldTotalLabel =
		 * ParamUtil.getString(actionRequest, "fieldTotalLabel"); String
		 * fieldTotalKey = ParamUtil.getString(actionRequest, "fieldTotalKey");
		 */

		int startMonth = ParamUtil.getInteger(actionRequest, "startMonth");

		int startYear = ParamUtil.getInteger(actionRequest, "startYear");

		int period = ParamUtil.getInteger(actionRequest, "period");

		int level = ParamUtil.getInteger(actionRequest, "level");

		int domainDeepLevel =
			ParamUtil.getInteger(actionRequest, "domainDeepLevel");

		// boolean notNullGov = ParamUtil.getBoolean(actionRequest,
		// "notNullGov");

		// boolean notNullDomain =
		// ParamUtil.getBoolean(actionRequest, "notNullDomain");

		//String[] fields =
		//	ParamUtil.getParameterValues(actionRequest, "fields", new String[] {
		//		"received-number"
		//	});

		int[] fieldsIndexes =
			StringUtil.split(
				ParamUtil.getString(actionRequest, "fieldsIndexes"), 0);

		if (fieldsIndexes != null && fieldsIndexes.length > 0) {

			String[] fieldLabels = new String[fieldsIndexes.length];
			String[] fieldKeys = new String[fieldsIndexes.length];
			String[] fieldFormulas = new String[fieldsIndexes.length];
			for (int f = 0; f < fieldsIndexes.length; f++) {

				String fieldLabel =
					ParamUtil.getString(actionRequest, "fieldLabel" +
						fieldsIndexes[f]);
				String fieldKey =
					ParamUtil.getString(actionRequest, "fieldKey" +
						fieldsIndexes[f]);
				String fieldFormula =
					ParamUtil.getString(actionRequest, "fieldFormula" +
						fieldsIndexes[f]);

				fieldLabels[f] = fieldLabel;
				fieldKeys[f] = fieldKey;
				fieldFormulas[f] = fieldFormula;

				System.out.println(fieldLabel + "-----" + fieldKey + "-----" +
					fieldFormula);
			}

			preferences.setValues("fieldLabels", fieldLabels);
			preferences.setValues("fieldKeys", fieldKeys);
			preferences.setValues("fieldFormulas", fieldFormulas);
		}

		// preferences.setValue("fieldTotalFormula", fieldTotalFormula);
		// preferences.setValue("fieldTotalLabel", fieldTotalLabel);
		// preferences.setValue("fieldTotalKey", fieldTotalKey);

		preferences.setValue("chartTitle", chartTitle);
		preferences.setValue("xaxisUnit", xaxisUnit);
		preferences.setValue("yaxisUnit", yaxisUnit);
		// preferences.setValue("notNullGov", String.valueOf(notNullGov));
		// preferences.setValue("notNullDomain", String.valueOf(notNullDomain));
		preferences.setValue("govCodes", govCodes);
		preferences.setValue("domainCodes", domainCodes);
		preferences.setValue("startMonth", String.valueOf(startMonth));
		preferences.setValue("startYear", String.valueOf(startYear));
		preferences.setValue("period", String.valueOf(period));
		preferences.setValue("displayStyle", displayStyle);
		preferences.setValue("filterKey", filterKey);
		preferences.setValue("level", String.valueOf(level));
		// preferences.setValue("fields", StringUtil.merge(fields));
		preferences.setValue("chartType", String.valueOf(chartType));
		preferences.setValue("domainDeepLevel", String.valueOf(domainDeepLevel));
		preferences.store();

		super.processAction(portletConfig, actionRequest, actionResponse);
	}
}
