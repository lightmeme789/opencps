<%
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme"%>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui"%>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util"%>

<portlet:defineObjects />

<!-- begin content-top -->
<div class="content-top">
    <div class="content-topinner">
        <div class="col-xs-12 col-sm-6 col-lg-3 content-common span3" style="margin-left: 0px; width: 25%;">
            <div class="register-account">
                <a href="#" title="<liferay-ui:message key="dvc-register-account"/>" class="step-common">
                    <div class="step-one color-common">
                        <span><liferay-ui:message key="dvc-step-one"/></span>
                    </div>
                    <div class="body-step btn-common">
                        <i class="dvcicon-openaccount"></i><br>
                        <span class="sp-step"><liferay-ui:message key="dvc-register-account"/></span>
                    </div>
                    <div class="number-step dvc-step1">
                        <span><img alt=""></span>
                    </div>
                </a>
            </div>
        </div>
        <div class="col-xs-12 col-sm-6 col-lg-3 content-common span3" style="margin-left: 0px; width: 25%;">
            <div class="register-account">
                <a href="#" title="<liferay-ui:message key="dvc-submit-online"/>" class="step-common">
                    <div class="step-one color-common">
                        <span><liferay-ui:message key="dvc-step-two"/></span>
                    </div>
                    <div class="body-step btn-common">
                        <i class="dvcicon-log"></i><br>
                        <span class="sp-step"><liferay-ui:message key="dvc-submit-online"/></span>
                    </div>
                    <div class="number-step dvc-step2">
                        <span><img alt=""></span>
                    </div>
                </a>
            </div>
        </div>
        <div class="col-xs-12 col-sm-6 col-lg-3 content-common span3" style="margin-left: 0px; width: 25%;">
            <div class="register-account">
                <a href="#" title="<liferay-ui:message key="dvc-uw-process-dossier"/>" class="step-common">
                    <div class="step-one color-common">
                        <span><liferay-ui:message key="dvc-step-three"/></span>
                    </div>
                    <div class="body-step btn-common">
                        <i class="dvcicon-accept"></i><br>
                        <span class="sp-step"><liferay-ui:message key="dvc-uw-process-dossier"/></span>
                    </div>
                    <div class="number-step dvc-step3">
                        <span><img alt=""></span>
                    </div>
                </a>
            </div>
        </div>
        <div class="col-xs-12 col-sm-6 col-lg-3 content-common span3" style="margin-left: 0px; width: 25%;">
            <div class="register-account">
                <a href="#" title="<liferay-ui:message key="dvc-return-dossier"/>" class="step-common">
                    <div class="step-one color-common">
                        <span><liferay-ui:message key="dvc-step-four"/></span>
                    </div>
                    <div class="body-step btn-common">
                        <i class="dvcicon-results"></i><br>
                        <span class="sp-step"><liferay-ui:message key="dvc-return-dossier"/></span>
                    </div>
                    <div class="number-step dvc-step4">
                        <span><img alt=""></span>
                    </div>
                </a>
            </div>
        </div>
    </div>
</div>     
<div class="clearfix"></div>
<!-- begin content-chart -->
<div class="content-chart">
    <div class="content-chart-inner">
        <div class="col-xs-12 col-sm-6 col-lg-6 chart-left span6" style="    padding-right: 15px; width: 50%;">
            <div class="chart-inner">
                <div class="title-chart-inner">
                    <span><liferay-ui:message key="dvc-chart-title"/></span>
                </div>
                <div class="body-chart-inner">
                    <div class="col-xs-12 col-sm-7 col-lg-7 span7 wrap-lietke-bieudo">
                        <div class="lietke-bieudo">
                            <div class="dunghan lietke-bieudo-cm"><span><i class="dvcicon-dunghan"></i><liferay-ui:message key="dvc-giai-quyet-dung-han"/></span>
                            </div>
                            <div class="trehan lietke-bieudo-cm"><span><i class="dvcicon-trehan"></i><liferay-ui:message key="dvc-giai-quyet-tre-han"/></span>
                            </div>
                            <div class="lietke-bieudo-cm"><span><i class="dvcicon-dang-xuly"></i><liferay-ui:message key="dvc-dang-xu-ly"/></span>
                            </div>
                        </div>
                        <div class="tong-xuly">
                            <div class="tongso" style="line-height: 30px;"><span><liferay-ui:message key="dvc-tong-so"/></span></div>
                            <div class="hoso-daxuly" style="line-height: 30px;"><span><liferay-ui:message key="dvc-ho-so-da-xu-ly"/></span></div>
                            <div class="color-red"><span>2145</span></div>
                        </div>
                    </div>

                    <div class="col-xs-12 col-sm-5 col-lg-5 span5">
                        <span><img src="/opencps-ux-theme/images/custom/bieudo.png" width="100%" alt=""></span>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xs-12 col-sm-6 col-lg-6 span5 chart-right" style="/* padding-right: 30px; */ width: 44%;">
            <div class="chart-inner">
                <div class="col-xs-12 col-sm-12 col-lg-12 chart-right-common">
                    <div class="chart-right-inner">
                        <div class="title-chart-inner">
                            <span><liferay-ui:message key="dvc-huongdanhotro"/></span>
                        </div>
                        <div class="body-chart-inner">
                            <div class="huongdan">
                                <a href="#" title="hướng dẫn đăng kí" class="color-one"><liferay-ui:message key="dvc-huongdandangky"/></a>
                                <a href="#" title="hướng dẫn đăng kí" class="color-two"><liferay-ui:message key="dvc-huongdandangnhap"/></a>
                            </div>
                            <div class="huongdan">
                                <a href="#" title="Hướng dẫn nộp hồ sơ" class="color-two"><liferay-ui:message key="dvc-huongdannophs"/></a>
                                <a href="#" title="Hướng dẫn thanh toán" class="color-two"><liferay-ui:message key="dvc-huongdanthanhtoan"/></a>
                            </div>
                            <div class="huongdan">
                                <a href="#" title="<liferay-ui:message key="dvc-huongdantracuu"/>" class="color-two"><liferay-ui:message key="dvc-huongdantracuu"/></a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xs-12 col-sm-6 col-lg-6 span5 chart-right" style="/* padding-right: 30px; */ width: 44%;">
            <div class="chart-inner" style="padding: 0px 20px 5px;">
                <div class="col-xs-12 col-sm-12 col-lg-12 span12 chart-right-common">
                    <div class="chart-right-inner">
                        <div class="title-chart-inner">
                            <span><liferay-ui:message key="dvc-khaosatykien"/></span>
                        </div>
                        <div class="body-chart-inner">
                            <div class="chatluong-xuly">
                                <span><i><liferay-ui:message key="dvc-chatluongdichvu"/></i></span>
                            </div>
                            <div class="col-sm-6 col-lg-6 span6 wrap-body-chart-inner">
                                <form>
                                  <div class=" col-sm-6 col-lg-6 span6 xuly-dichvu"><input type="checkbox" name="tot" value="Tot"> <liferay-ui:message key="dvc-tot"/></div>
                                  <div class="col-sm-6 col-lg-6 span6 xuly-dichvu"><input type="checkbox" name="kha" value="Kha"> <liferay-ui:message key="dvc-kha"/></div>
                                  <div class="col-sm-6 col-lg-6 span6 xuly-dichvu"><input type="checkbox" name="tot" value="TB"><liferay-ui:message key="dvc-tb"/></div>
                                  <div class="col-sm-6 col-lg-6 span6 xuly-dichvu"><input type="checkbox" name="kha" value="KEM"> <liferay-ui:message key="dvc-kem"/></div>
                                </form>
                            </div>
                            <div class="col-sm-6 col-lg-6 span5">
                                <div class="send-result">
                                    <a href="#" title="Gửi ý kiến" class="color-two"><liferay-ui:message key="dvc-gui-y-kien"/></a>
                                    <a href="" title="Xem kết quả" class="color-one"><liferay-ui:message key="dvc-xem-ket-qua"/></a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<!-- end content-chart -->          
<div class="clearfix"></div>
<!-- begin content-table -->
<%-- <div class="content-table col-xs-12 col-sm-12 col-lg-12" style="padding-right: 127px;">
    <div class="content-table-inner">
        <div class="title-chart-inner">
            <span><liferay-ui:message key="dvc-table-title"/></span>
        </div>
        <div class="body-content-table">
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th> <liferay-ui:message key="dvc-tendonvi"/></th>
                        <th> <liferay-ui:message key="dvc-hstiepnhan"/></th>
                        <th> <liferay-ui:message key="dvc-danggiaiquyet"/></th>
                        <th> <liferay-ui:message key="dvc-dagiaiquyet"/></th>
                        <th> <liferay-ui:message key="dvc-hstuchoi"/></th>
                        <th> <liferay-ui:message key="dvc-coketqua"/></th>
                        <th><liferay-ui:message key="dvc-tilegiaiquyet"/></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <th scope="row"><liferay-ui:message key="dvc-sohuutritue"/></th>
                        <td>110</td>
                        <td>50</td>
                        <td>60</td>
                        <td>0</td>
                        <td>110</td>
                        <td>97%</td>
                    </tr>
                    <tr>
                        <th scope="row">  <liferay-ui:message key="dvc-vatlieuno"/></th>
                        <td>110</td>
                        <td>50</td>
                        <td>60</td>
                        <td>0</td>
                        <td>110</td>
                        <td>97%</td>
                    </tr>
                    <tr>
                        <th scope="row">  <liferay-ui:message key="dvc-sohuutritue"/></th>
                        <td>110</td>
                        <td>50</td>
                        <td>60</td>
                        <td>0</td>
                        <td>110</td>
                        <td>97%</td>
                    </tr>
                    <tr>
                        <th scope="row">  <liferay-ui:message key="dvc-sohuutritue"/></th>
                        <td>110</td>
                        <td>50</td>
                        <td>60</td>
                        <td>0</td>
                        <td>110</td>
                        <td>97%</td>
                    </tr>
                    <tr>
                        <th scope="row">  <liferay-ui:message key="dvc-sohuutritue"/></th>
                        <td>110</td>
                        <td>50</td>
                        <td>60</td>
                        <td>0</td>
                        <td>110</td>
                        <td>97%</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div> --%>
<!-- end content-table -->