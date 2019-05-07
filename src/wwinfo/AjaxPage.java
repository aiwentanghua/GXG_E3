package wwinfo;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;
import java.io.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.text.SimpleDateFormat;
import net.sf.json.*;

public class AjaxPage extends HttpServlet {
	private static final long serialVersionUID = 4L;
	
	protected void doRequest(HttpServletRequest request,HttpServletResponse response)
			throws ServletException,IOException{
		String strAction = request.getParameter("action");
		HashMap hm = new HashMap();
		if (strAction==null || strAction.isEmpty()){
			GlobalFun.errorOut("strAction is empty in ajaxpage");
			hm.put("status", 400);
			hm.put("msg", "parameter is wrong!");
			String json = JSONObject.fromObject(hm).toString();
			response.getOutputStream().write(json.getBytes("UTF-8"));			
			return;
		}
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		try{
			Method method = this.getClass().getMethod(strAction, 
					new Class[]{HttpServletRequest.class,HttpServletResponse.class});
			if (null == method){
				hm.put("status", 400);
				hm.put("msg", "no action method!");				
			}else{
				method.invoke(this,
						new Object[]{request,response});
				return;
			}
		}catch(Exception e){
			e.printStackTrace();
			hm.put("status", 400);
			hm.put("msg", "系统未知错误！");
		}
		String json = JSONObject.fromObject(hm).toString();
		String callback = request.getParameter("callback");
		if (null != callback && !callback.isEmpty()){
			response.setContentType("text/javascript");
			json = callback + "(" + json + ")";
		}
		response.getOutputStream().write(json.getBytes("UTF-8"));			
		return;
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response)
			throws ServletException,IOException{
		doRequest(request,response);
	}
	public void doPost(HttpServletRequest request,HttpServletResponse response)
			throws ServletException,IOException{
		doRequest(request,response);
	}

	//单据取消接口
	public void ordercancel(HttpServletRequest request,HttpServletResponse response)
			throws ServletException,IOException{
		HashMap hmRet = new HashMap();
		String orderCode = request.getParameter("ordercode");
		String orderType = request.getParameter("ordertype");
		String callback = request.getParameter("callback");
		if (null == orderCode || orderCode.isEmpty()
			|| null == orderType || orderType.isEmpty()){
			hmRet.put("status", 400);
			hmRet.put("msg", "参数有误！");
		}else{
			try{
				StringBuilder jsonBuilder = new StringBuilder();
				jsonBuilder.append("<warehouseCode>OTHER</warehouseCode>");
				jsonBuilder.append("<orderCode>");
				jsonBuilder.append(orderCode);
				jsonBuilder.append("</orderCode>");
				jsonBuilder.append("<orderType>");
				jsonBuilder.append(orderType);
				jsonBuilder.append("</orderType>");
				String retMsg = CallQMAPI("taobao.qimen.order.cancel",jsonBuilder.toString());
				if (null == retMsg)
					hmRet.put("status", 200);
				else{
					hmRet.put("status", 400);
					hmRet.put("msg", retMsg);
				}
			}catch(Exception e){
				e.printStackTrace();
				hmRet.put("status", 400);
				hmRet.put("msg", "提交时发生错误！");
			}
		}
		String json = JSONObject.fromObject(hmRet).toString();
		if (null != callback && !callback.isEmpty()){
			response.setContentType("text/javascript");
			json = callback + "(" + json + ")";
		}
		response.getOutputStream().write(json.getBytes("UTF-8"));			
	}
	
	private String CallQMAPI(String apiName,String data){
		String xml="<?xml version=\"1.0\" encoding=\"utf-8\"?><request>";
		xml += data;
		xml += "</request>";
		
		//md5计算
		final String app_key="1012585822";
		final String secret="sandbox71082618023b4db126f76426e";
		final String customerId = "c1470792410866";
		final String ver = "2.0";
		final String url="http://qimenapi.tbsandbox.com/router/qimen/service?";
		
		SimpleDateFormat dfTemp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curTime = dfTemp.format(new Date());
		String signString = secret + "app_key"+app_key+"customerId"+customerId+
			"formatxmlmethod"+apiName+"sign_methodmd5timestamp"+curTime+
			"v"+ver+xml+secret;
		String sign = GlobalFun.secritMD5(signString,"UTF-8").toUpperCase();
		String sendUrl = "method="+apiName+"&timestamp="+curTime + 
			"&format=xml&app_key="+app_key + "&v="+ver+
			"&sign="+sign+"&sign_method=md5&customerId="+customerId;
		try{
			sendUrl=url + URLEncoder.encode(sendUrl, "UTF-8");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		boolean bSuccess = false;
		String msg=null;
		String resp = GlobalFun.doPost(sendUrl,xml,null);
		if (null != resp){
			//分析xml
			try{
				Document docXML = DocumentHelper.parseText(resp); // 将字符串转为XML
	            Element rootElt = docXML.getRootElement(); // 获取根节点
	            Element flag = rootElt.element("flag");
	            if (flag.getText().compareToIgnoreCase("success")==0){
	            	bSuccess = true;
	            }else{
	            	msg = rootElt.element("message").getText();
	            }
			}catch(Exception e){
				GlobalFun.debugOut("qimen return:"+resp);
				e.printStackTrace();
				msg="parse return xml failed";
			}
        }
		if (bSuccess)
			return null;
		else
			return msg;
	}
	
}
