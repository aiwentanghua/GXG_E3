package wwinfo;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;
import java.io.*;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.text.SimpleDateFormat;

import net.sf.json.*;

//REST 调用接口提供类

public class RestQimen extends HttpServlet {
    private static final long serialVersionUID = 3L;
    private static final String app_key[] = {"1021752740", "1023261361", "21752740", "23261361"};
    private static final String secrit[] = {"sandboxece4460ca5d565958dccf966c",
            "sandbox71082618023b4db126f76426e",
            "c301d8eece4460ca5d565958dccf966c",
            "1fc88e271082618023b4db126f76426e"};
    private static boolean configReaded = false;
    private static String appKeyConfig[] = {"", ""};
    private static String secritConfig[] = {"", ""};
    private static String e3url = "";
    private static DBOperate dbop = new DBOperate();
    //private static final String customerId = "c1470754368990";
    //private static final String ver = "2.0";

    protected void doRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!configReaded) {
            readConfig();
            configReaded = true;
        }
		/*
		Date dt = new Date();
		if (dt.getMonth()>5){
			return;
		}
		*/

        String func = request.getParameter("method");
        if (null != func && !func.isEmpty()) {
            func = func.replace(".", "_");
            try {
                Class[] paraClass = new Class[]{HttpServletRequest.class, HttpServletResponse.class};
                Method method = this.getClass().getMethod(func, paraClass);
                if (method != null) {
                    method.invoke(this, new Object[]{request, response});
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>failure</flag><code>-99</code><message>未知功能</message></response>";
        response.getOutputStream().write(retXML.getBytes("UTF-8"));
        return;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response);
    }

    /*
     * 读取配置信息，文件：classes\config.properties
     */
    private boolean readConfig() {
        String webInfoPath = GlobalFun.getWebInfPath();
        String file = webInfoPath + "classes/config.properties";

        boolean ret = true;
        try {
            InputStream is = new FileInputStream(file);
            Properties p = new Properties();
            p.load(is);

            //奇门参数
            appKeyConfig[0] = p.getProperty("qimen_appkey");
            secritConfig[0] = p.getProperty("qimen_secret");

            appKeyConfig[1] = p.getProperty("qimen_appkey2");
            secritConfig[1] = p.getProperty("qimen_secret2");

        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    //验证参数并返回数据体
    String CheckAndGetData(String func, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException {
        String contentStr = "";
        try {
            InputStream is = null;
            is = request.getInputStream();
            contentStr = IOUtils.toString(is, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String sign = request.getParameter("sign");
        String appKey = request.getParameter("app_key");
        String getCustomerId = request.getParameter("customerId");
        String format = request.getParameter("format");
        String method = request.getParameter("method");
        String sign_method = request.getParameter("sign_method");
        String timestamp = request.getParameter("timestamp");
        String v = request.getParameter("v");
        if (null == timestamp || timestamp.isEmpty()
                || null == sign || sign.isEmpty()) {
            GlobalFun.errorOut("qimen callback parameters is wrong");
            String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>failure</flag><code>-1</code><message>参数有误</message></response>";
            response.getOutputStream().write(retXML.getBytes("UTF-8"));
            return null;
        }
        String curSecret = null;
        for (int i = 0; i < app_key.length; i++) {
            if (app_key[i].compareTo(appKey) == 0) {
                curSecret = secrit[i];
                break;
            }
        }
        //比对从配置文件中读取的
        if (null == curSecret) {
            for (int i = 0; i < 2; i++) {
                if (null != appKeyConfig[i]) {
                    if (appKeyConfig[i].compareTo(appKey) == 0) {
                        curSecret = secritConfig[i];
                    }
                }
            }
        }

        if (null == curSecret) {
            GlobalFun.debugOut("unknown key:" + appKey);
            String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>failure</flag><code>-3</code><message>无效appkey:" + appKey + "</message></response>";
            response.getOutputStream().write(retXML.getBytes("UTF-8"));
            return null;
        }
        //post 数据
        String xml = contentStr;
        String signString = curSecret + "app_key" + appKey + "customerId" + getCustomerId +
                "format" + format + "method" + method + "sign_method" + sign_method + "timestamp" + timestamp +
                "v" + v + xml + curSecret;
        String checkSign = GlobalFun.secritMD5(signString, "UTF-8").toUpperCase();
        if (checkSign.compareToIgnoreCase(sign) != 0) {
            GlobalFun.errorOut("qimen recall sign is uncorrect");
            GlobalFun.debugOut("signString:" + signString);
            GlobalFun.debugOut("get sign:" + sign);
            GlobalFun.debugOut("check sign:" + checkSign);

            String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>failure</flag><code>-2</code><message>sign签名失败</message></response>";
            response.getOutputStream().write(retXML.getBytes("UTF-8"));
            return null;
        }
        //是否超过5分钟

        GlobalFun.debugOut("get data for " + func + ":");
        GlobalFun.debugOut(xml);
        return xml;
    }

    //3.3 入库单确认接口
    //6.4	BOS创建调拨单状态回传
    //8.4   批发退货通知单入库确认
    public void entryorder_confirm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("entryorder.confirm", request, response);
        if (null == data || data.isEmpty())
            return;

        String retFlag = "success";
        String retCode = "0";
        String retMsg = "";
        //分析xml
        try {
            Document docXML = DocumentHelper.parseText(data); // 将字符串转为XML
            Element rootElt = docXML.getRootElement(); // 获取根节点
            Element entryOrder = rootElt.element("entryOrder");

            String entryOrderCode = entryOrder.element("entryOrderCode").getText();
            String entryOrderId = null;
            try {
                entryOrderId = entryOrder.element("entryOrderId").getText();
            } catch (Exception e) {

            }
            String operateTime = entryOrder.element("operateTime").getText();
            String entryOrderType = entryOrder.element("entryOrderType").getText();
            String tableName = "";
            if (entryOrderType.compareTo("CGRK") == 0)
                tableName = "E3_BOS_PURIN";
            else if (entryOrderType.compareTo("DBRK") == 0)
                tableName = "E3_BOS_TRANQTY2";
            else if (entryOrderType.compareTo("B2BRK") == 0 || entryOrderType.compareTo("PTRK") == 0)
                tableName = "E3_BOS_RETSALEIN";
            else {
                retFlag = "failure";
                retCode = "-2";
                retMsg = "orderType is unknown";
            }
            if (!tableName.isEmpty()) {
                Element orderLines = rootElt.element("orderLines");
                List<Element> listOrder = orderLines.elements("orderLine");
                if (listOrder.size() > 0) {
                    List<HashMap> hmList = new ArrayList<HashMap>();
                    for (int i = 0; i < listOrder.size(); i++) {
                        Element order = listOrder.get(i);
                        String itemCode = order.element("itemCode").getText();
                        String actualQty = order.element("actualQty").getText();
                        HashMap hm = new HashMap();
                        hm.put("orderType", entryOrderType);
                        hm.put("Docno", entryOrderCode);
                        hm.put("Docno_E3", entryOrderId);
                        hm.put("dateIn", operateTime);
                        hm.put("ProductAlias", itemCode);
                        hm.put("qtyIn", actualQty);
                        hmList.add(hm);
		            	/*
		            	if (!DBOperate.addData(tableName, hm)){
		        			retFlag="failure";
		        			retCode="-1";
		            		retMsg="insert database failed";
		            		break;
		            	}
		            	*/
                    }
                    if (!DBOperate.addDataArray(tableName, hmList)) {
                        retFlag = "failure";
                        retCode = "-1";
                        retMsg = "insert database failed";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            retFlag = "failure";
            retCode = "-2";
            retMsg = "parse xml failed";
        }

        String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
        response.getOutputStream().write(retXML.getBytes());
        return;
    }

    //3.4	采购退货单出库确认
    //8.2      批发通知单出库确认
    public void stockout_confirm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("stockout.confirm", request, response);
        if (null == data || data.isEmpty())
            return;

        String retFlag = "success";
        String retCode = "0";
        String retMsg = "";
        //分析xml
        try {
            Document docXML = DocumentHelper.parseText(data); // 将字符串转为XML
            Element rootElt = docXML.getRootElement(); // 获取根节点
            Element entryOrder = rootElt.element("deliveryOrder");

            String orderType = entryOrder.element("orderType").getText();
            String tableName = null;
            if (orderType.compareTo("CGTH") == 0)
                tableName = "E3_BOS_RETPUROUT";
            else if (orderType.compareTo("B2BCK") == 0 || orderType.compareTo("PTCK") == 0)
                tableName = "E3_BOS_SALEOUT";
            else {
                retFlag = "failure";
                retCode = "-2";
                retMsg = "orderType is unknown";
                String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
                response.getOutputStream().write(retXML.getBytes());
                return;
            }

            String entryOrderCode = entryOrder.element("deliveryOrderCode").getText();
            String confirmTime = entryOrder.element("orderConfirmTime").getText();
            Element orderLines = rootElt.element("orderLines");
            List<Element> listOrder = orderLines.elements("orderLine");
            if (listOrder.size() > 0) {
                List<HashMap> hmList = new ArrayList<HashMap>();
                for (int i = 0; i < listOrder.size(); i++) {
                    Element order = listOrder.get(i);
                    String itemCode = order.element("itemCode").getText();
                    String planQty = order.element("actualQty").getText();
                    HashMap hm = new HashMap();
                    hm.put("orderType", orderType);
                    hm.put("Docno", entryOrderCode);
                    hm.put("dateOut", confirmTime);
                    hm.put("ProductAlias", itemCode);
                    hm.put("qtyOut", planQty);
                    hmList.add(hm);
		        	/*
		        	if (!DBOperate.addData(tableName, hm)){
		    			retFlag="failure";
		    			retCode="-1";
		        		retMsg="insert database failed";
		        		break;
		        	}
		        	*/
                }
                if (!DBOperate.addDataArray(tableName, hmList)) {
                    retFlag = "failure";
                    retCode = "-1";
                    retMsg = "insert database failed";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            retFlag = "failure";
            retCode = "-2";
            retMsg = "parse return xml failed";
        }

        String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
        response.getOutputStream().write(retXML.getBytes());
        return;
    }

    //4.1	库存盘点单（完工单创建）接口
    public void inventory_report(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("inventory.report", request, response);
        if (null == data || data.isEmpty())
            return;

        String retFlag = "success";
        String retCode = "0";
        String retMsg = "";
        //分析xml
        try {
            Document docXML = DocumentHelper.parseText(data); // 将字符串转为XML
            Element rootElt = docXML.getRootElement(); // 获取根节点

            String checkOrderCode = rootElt.element("checkOrderCode").getText();
            String warehouseCode = rootElt.element("warehouseCode").getText();
            String checkTime = rootElt.element("checkTime").getText();
            String remark = rootElt.element("remark").getText();
            String checkOrderId = rootElt.element("checkOrderId").getText();
            if (null == remark)
                remark = "";
            Element items = rootElt.element("items");
            List<Element> listItem = items.elements("item");
            if (listItem.size() > 0) {
                List<HashMap> hmList = new ArrayList<HashMap>();
                for (int i = 0; i < listItem.size(); i++) {
                    Element item = listItem.get(i);
                    String itemCode = item.element("itemCode").getText();
                    String quantity = item.element("quantity").getText();
                    ;
	            	/*
	                Element extendProps = item.element("extendProps");
	                if (null != extendProps){
	                	Element elQuantity = extendProps.element("spQuantity");
	                	if (null != elQuantity)
	                		quantity = elQuantity.getText();
	                }
	                */
                    if (null == quantity || quantity.isEmpty()) {
                        retFlag = "failure";
                        retCode = "-1";
                        retMsg = "no spQuantity field";
                        break;
                    }
                    HashMap hm = new HashMap();
                    hm.put("DocnoE3", checkOrderCode);
                    hm.put("store", warehouseCode);
                    hm.put("billdate", checkTime);
                    hm.put("ProductAlias", itemCode);
                    hm.put("qtyOut", quantity);
                    hm.put("remark", remark);
                    hm.put("checkOrderId", checkOrderId);
                    hmList.add(hm);
	            	/*
	            	if (!DBOperate.addData("E3_BOS_INVENTORY", hm)){
	        			retFlag="failure";
	        			retCode="-1";
	            		retMsg="insert database failed";
	            		break;
	            	}
	            	*/
                }
                if (!DBOperate.addDataArray("E3_BOS_INVENTORY", hmList)) {
                    retFlag = "failure";
                    retCode = "-1";
                    retMsg = "insert database failed";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            retFlag = "failure";
            retCode = "-2";
            retMsg = "parse return xml failed";
        }

        String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
        response.getOutputStream().write(retXML.getBytes());
        return;
    }

    //9.1	批发通知单创建
    public void stockout_create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("stockout.create", request, response);
        if (null == data || data.isEmpty())
            return;

        String retFlag = "success";
        String retCode = "0";
        String retMsg = "";
        //分析xml
        try {
            Document docXML = DocumentHelper.parseText(data); // 将字符串转为XML
            Element rootElt = docXML.getRootElement(); // 获取根节点
            Element entryOrder = rootElt.element("deliveryOrder");

            String deliveryOrderCode = entryOrder.element("deliveryOrderCode").getText();
            String warehouseCode = entryOrder.element("warehouseCode").getText();
            String orderCreateTime = entryOrder.element("createTime").getText();
            String remark = entryOrder.element("remark").getText();

            //新增字段
            String receiverName = "";
            try {
                receiverName = entryOrder.element("name").getText();
            } catch (Exception e) {
            }
            //String soReference4 = entryOrder.element("buyerNick").getText();
            String province = "";
            try {
                province = entryOrder.element("province").getText();
            } catch (Exception e) {

            }
            String city = "";
            try {
                city = entryOrder.element("city").getText();
            } catch (Exception e) {

            }
            String area = "";
            try {
                area = entryOrder.element("district").getText();
            } catch (Exception e) {

            }
            String zip = "";
            try {
                zip = entryOrder.element("zipCode").getText();
            } catch (Exception e) {

            }
            String address = "";
            try {
                address = entryOrder.element("detailAddress").getText();
            } catch (Exception e) {

            }
            //新增字段2
            String totalAmount = "";
            try {
                totalAmount = entryOrder.element("totalAmount").getText();
            } catch (Exception e) {
            }
            String freight = "";
            try {
                freight = entryOrder.element("freight").getText();
            } catch (Exception e) {
            }

            //新增字段 2017-06-08
            String shopNick = "";
            try {
                shopNick = entryOrder.element("shopNick").getText();
            } catch (Exception e) {
            }

            //新增字段2018-04-09
            String shop_nick = "";
            try {
                shop_nick = entryOrder.element("shop_nick").getText();
            } catch (Exception e) {
            }
            String buyer_nick = "";
            try {
                buyer_nick = entryOrder.element("buyer_nick").getText();
            } catch (Exception e) {
            }

            if (null == remark)
                remark = "";
            Element orderLines = rootElt.element("orderLines");
            List<Element> listOrder = orderLines.elements("orderLine");
            if (listOrder.size() > 0) {
                List<HashMap> hmList = new ArrayList<HashMap>();
                for (int i = 0; i < listOrder.size(); i++) {
                    Element order = listOrder.get(i);
                    String itemCode = order.element("itemCode").getText();
                    String planQty = order.element("planQty").getText();
                    String actualPrice = null;
                    Element extendProps = order.element("extendProps");
                    if (null != extendProps) {
                        Element elActualPrice = extendProps.element("actualPrice");
                        if (null != elActualPrice)
                            actualPrice = elActualPrice.getText();
                    }
                    HashMap hm = new HashMap();
                    hm.put("deliveryOrderCode", deliveryOrderCode);
                    hm.put("warehouseCode", warehouseCode);
                    hm.put("RETAILBILLTYPE", "正常零售");
                    hm.put("placeOrderTime", orderCreateTime);
                    hm.put("payTime", orderCreateTime);
                    hm.put("itemCode", itemCode);
                    hm.put("planQty", planQty);
                    hm.put("actualPrice", actualPrice);
                    hm.put("remark", remark);

                    hm.put("receiverName", receiverName);
                    //hm.put("soReference4",soReference4);
                    hm.put("province", province);
                    hm.put("city", city);
                    hm.put("area", area);
                    hm.put("zip", zip);
                    hm.put("address", address);
                    hm.put("totalAmount", totalAmount);
                    hm.put("freight", freight);
                    hm.put("shopNick", shopNick);
                    hm.put("shop_nick", shop_nick);
                    hm.put("buyer_nick", buyer_nick);
                    hmList.add(hm);
	            	/*
	            	if (!DBOperate.addData("E3_BOS_RETAIL", hm)){
	        			retFlag="failure";
	        			retCode="-1";
	            		retMsg="insert database failed";
	            		break;
	            	}
	            	*/
                }
                GlobalFun.debugOut("insert array data to E3_BOS_RETAIL:");
                GlobalFun.debugOut(hmList.toString());
                if (!DBOperate.addDataArray("E3_BOS_RETAIL", hmList)) {
                    retFlag = "failure";
                    retCode = "-1";
                    retMsg = "insert database failed";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            GlobalFun.debugOut(data);
            retFlag = "failure";
            retCode = "-2";
            retMsg = "parse return xml failed";
        }

        String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
        response.getOutputStream().write(retXML.getBytes());
        return;
    }

    //9.2   批发退货通知单创建
    public void entryorder_create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("entryorder.create", request, response);
        if (null == data || data.isEmpty())
            return;

        String retFlag = "success";
        String retCode = "0";
        String retMsg = "";
        //分析xml
        try {
            Document docXML = DocumentHelper.parseText(data); // 将字符串转为XML
            Element rootElt = docXML.getRootElement(); // 获取根节点
            Element entryOrder = rootElt.element("entryOrder");

            String entryOrderCode = entryOrder.element("entryOrderCode").getText();
            String warehouseCode = entryOrder.element("warehouseCode").getText();
            String orderCreateTime = entryOrder.element("orderCreateTime").getText();
            String remark = entryOrder.element("remark").getText();
            if (null == remark)
                remark = "";

            //新增字段2
            String totalAmount = "";
            try {
                totalAmount = entryOrder.element("totalAmount").getText();
            } catch (Exception e) {
            }
            String freight = "";
            try {
                freight = entryOrder.element("freight").getText();
            } catch (Exception e) {
            }

            //新增字段 2017-06-08
            String shopNick = "";
            try {
                shopNick = entryOrder.element("shopNick").getText();
            } catch (Exception e) {
            }

            Element orderLines = rootElt.element("orderLines");
            List<Element> listOrder = orderLines.elements("orderLine");
            if (listOrder.size() > 0) {
                List<HashMap> hmList = new ArrayList<HashMap>();
                for (int i = 0; i < listOrder.size(); i++) {
                    Element order = listOrder.get(i);
                    String itemCode = order.element("itemCode").getText();
                    String planQty = order.element("planQty").getText();
                    String actualPrice = null;
                    Element extendProps = order.element("extendProps");
                    if (null != extendProps) {
                        Element elActualPrice = extendProps.element("actualPrice");
                        if (null != elActualPrice)
                            actualPrice = elActualPrice.getText();
                    }
                    HashMap hm = new HashMap();
                    hm.put("deliveryOrderCode", entryOrderCode);
                    hm.put("warehouseCode", warehouseCode);
                    hm.put("RETAILBILLTYPE", "退货零售");
                    hm.put("placeOrderTime", orderCreateTime);
                    hm.put("payTime", orderCreateTime);
                    hm.put("itemCode", itemCode);
                    hm.put("planQty", planQty);
                    hm.put("actualPrice", actualPrice);
                    hm.put("remark", remark);
                    hm.put("totalAmount", totalAmount);
                    hm.put("freight", freight);
                    hm.put("shopNick", shopNick);
                    hmList.add(hm);
	            	/*
	            	if (!DBOperate.addData("E3_BOS_RETAIL", hm)){
	        			retFlag="failure";
	        			retCode="-1";
	            		retMsg="insert database failed";
	            		break;
	            	}
	            	*/
                }
                GlobalFun.debugOut("insert array data to E3_BOS_RETAIL:");
                GlobalFun.debugOut(hmList.toString());
                if (!DBOperate.addDataArray("E3_BOS_RETAIL", hmList)) {
                    retFlag = "failure";
                    retCode = "-1";
                    retMsg = "insert database failed";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            retFlag = "failure";
            retCode = "-2";
            retMsg = "parse return xml failed";
        }

        String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
        response.getOutputStream().write(retXML.getBytes());
        return;
    }

    //10.3  销售单出库回传
    public void deliveryorder_create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("deliveryorder.create", request, response);
        if (null == data || data.isEmpty())
            return;

        String retFlag = "success";
        String retCode = "0";
        String retMsg = "";
        //分析xml
        try {
            Document docXML = DocumentHelper.parseText(data); // 将字符串转为XML
            Element rootElt = docXML.getRootElement(); // 获取根节点
            Element entryOrder = rootElt.element("deliveryOrder");

            //获取O2O订单类型
            String sourcePlatformCode = entryOrder.element("sourcePlatformCode").getText();


            //如果为null直接返回报错
            if (sourcePlatformCode == null || sourcePlatformCode.equals("")) {
                retFlag = "failure";
                retCode = "-2";
                retMsg = "parse return xml sourcePlatformCode null";
                String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
                response.getOutputStream().write(retXML.getBytes());
                return;
            }

            if (sourcePlatformCode.equals("TB")) {//仓库发货

                String deliveryOrderCode = entryOrder.element("deliveryOrderCode").getText();
                String warehouseCode = entryOrder.element("warehouseCode").getText();
                String createTime = entryOrder.element("createTime").getText();
                String placeOrderTime = entryOrder.element("placeOrderTime").getText();
                String remark = entryOrder.element("remark").getText();
                String expressCode = entryOrder.element("expressCode").getText();
                String logisticsCode = entryOrder.element("logisticsCode").getText();
                String logisticsName = entryOrder.element("logisticsName").getText();
                if (null == remark)
                    remark = "";

                //新增字段2
                String totalAmount = "";
                try {
                    totalAmount = entryOrder.element("totalAmount").getText();
                } catch (Exception e) {
                }
                String freight = "";
                try {
                    freight = entryOrder.element("freight").getText();
                } catch (Exception e) {
                }

                //新增字段 2017-06-08
                String shopNick = "";
                try {
                    shopNick = entryOrder.element("shopNick").getText();
                } catch (Exception e) {
                }

                Element orderLines = rootElt.element("orderLines");
                List<Element> listOrder = orderLines.elements("orderLine");
                if (listOrder.size() > 0) {
                    List<HashMap> hmList = new ArrayList<HashMap>();
                    for (int i = 0; i < listOrder.size(); i++) {
                        Element order = listOrder.get(i);
                        String itemCode = order.element("itemCode").getText();
                        String planQty = order.element("planQty").getText();
                        String actualPrice = order.element("actualPrice").getText();
                        HashMap hm = new HashMap();
                        hm.put("deliveryOrderCode", deliveryOrderCode);
                        hm.put("warehouseCode", warehouseCode);
                        hm.put("RETAILBILLTYPE", "正常零售");
                        hm.put("placeOrderTime", createTime);
                        hm.put("payTime", placeOrderTime);
                        hm.put("itemCode", itemCode);
                        hm.put("planQty", planQty);
                        hm.put("actualPrice", actualPrice);
                        hm.put("remark", remark);

                        hm.put("expressCode", expressCode);
                        hm.put("logisticsCode", logisticsCode);
                        hm.put("logisticsName", logisticsName);

                        hm.put("totalAmount", totalAmount);
                        hm.put("freight", freight);
                        hm.put("shopNick", shopNick);
                        hmList.add(hm);
	            	/*
	            	if (!DBOperate.addData("E3_BOS_RETAIL", hm)){
	        			retFlag="failure";
	        			retCode="-1";
	            		retMsg="insert database failed";
	            		break;
	            	}
	            	*/
                    }
                    GlobalFun.debugOut("insert array data to E3_BOS_RETAIL:");
                    //GlobalFun.debugOut(hmList.toString());
                    if (!DBOperate.addDataArray("E3_BOS_RETAIL", hmList)) {
                        retFlag = "failure";
                        retCode = "-1";
                        retMsg = "insert database failed";
                    }
                }
                String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
                response.getOutputStream().write(retXML.getBytes());
                return;
            } else if (sourcePlatformCode.equals("POS")) {

                //门店发货
                //出库单号
                final String deliveryOrderCode = entryOrder.element("deliveryOrderCode").getText();
                //仓库编码(统仓统配等无需ERP指定仓储编码的情况填OTHER)
                String warehouseCode = entryOrder.element("warehouseCode").getText();
                //发货单创建时间(YYYY-MM-DD HH:MM:SS)
                String createTime = entryOrder.element("createTime").getText();
                //前台订单/店铺订单的创建时间/下单时间
                String placeOrderTime = entryOrder.element("placeOrderTime").getText();
                //备注
                String remark = entryOrder.element("remark").getText();

                //收货人姓名
                String name = entryOrder.element("name").getText();
                //电话
                String mobile = entryOrder.element("mobile").getText();

                //省
                String province = entryOrder.element("province").getText();
                //市
                String city = entryOrder.element("city").getText();
                //地区
                String area = entryOrder.element("area").getText();
                //详细地址
                String detailAddress = entryOrder.element("detailAddress").getText();
                if (null == remark)
                    remark = "";

                //新增字段2
                String totalAmount = "";
                try {
                    //订单总金额(订单总金额=应收金额+已收金额=商品总金额-订单折扣金额+快递费用 ;单位 元)
                    totalAmount = entryOrder.element("totalAmount").getText();
                } catch (Exception e) {
                }
                String freight = "";
                try {
                    //快递费用(元)
                    freight = entryOrder.element("freight").getText();
                } catch (Exception e) {
                }

                //新增字段 2017-06-08
                String shopNick = "";
                try {
                    //店铺名称
                    shopNick = entryOrder.element("shopNick").getText();
                } catch (Exception e) {
                }

                Element orderLines = rootElt.element("orderLines");
                List<Element> listOrder = orderLines.elements("orderLine");

                if (listOrder.size() > 0) {
                    List<HashMap> mapList = new ArrayList<HashMap>();
                    boolean state = true;
                    JSONObject json = new JSONObject();
                    for (int i = 0; i < listOrder.size(); i++) {
                        Element order = listOrder.get(i);
                        //商品编码
                        String itemCode = order.element("itemCode").getText();
                        //应发商品数量
                        String planQty = order.element("planQty").getText();
                        //实际成交价
                        String actualPrice = order.element("actualPrice").getText();

                        HashMap<String, String> map = new HashMap<>();
                        map.put("order_sn", warehouseCode);
                        map.put("outer_code", itemCode);
                        List<HashMap> list = dbop.getD1MSendListMember("SKUstock", map);


                        if (Integer.valueOf(list.get(0).toString()) < Integer.valueOf(planQty)) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject reject = new JSONObject();
                                        reject.put("orderCode", deliveryOrderCode);//传出库单号
                                        reject.put("Note", "o2o订单(门店无法满足发货条件)");
                                        reject.put("orderStatus", "CANCELED");//目前没有确定

                                        SimpleDateFormat dfTemp = new SimpleDateFormat("yyyyMMddHHmmss");
                                        String curTime = dfTemp.format(new Date());
                                        reject.put("operateorTime", curTime);

                                        HashMap<String, String> hmreject = E3requestUtil.getE3dataRequest("UpdateSalesOrder", reject.toString());
                                        String e3reject = HttpRequest.doPost(e3url, hmreject, null);

                                        if (e3reject != null && !e3reject.equals("")) {
                                            GlobalFun.debugOut("UpdateSalesOrder info result:" + e3reject);
                                            JSONObject e3jsonreject = JSONObject.fromObject(e3reject);
                                            if (e3jsonreject.get("message").equals("success")) {

                                                //重新给对比库存的sku赋值
                                                //e3data = JSONArray.fromObject(e3jsonreject.get("data").toString());
                                                GlobalFun.errorOut("O2O订单自动取消 成功！");


                                            } else {
                                                GlobalFun.errorOut("O2O订单自动取消 接口数据处理失败：" + e3jsonreject.get("message").toString());
                                            }

                                        } else {
                                            //接口返回null
                                            GlobalFun.errorOut("O2O订单自动取消 调用E3接口UpdateSalesOrder失败返回null");
                                        }

                                    } catch (Exception e2) {
                                        //调用E3接口UpdateSalesOrder出现异常
                                        GlobalFun.errorOut("O2O订单自动取消 调用E3接口UpdateSalesOrder失败返回null error :" + GlobalFun.getCrashMessage(e2));
                                        //return ResultGenerator.genFailResult("调用UpdateSalesOrder自动重新派单接口出现异常！");
                                    }

                                }
                            }).start();
                            state = false;
                            break;
                        } else {
                            json.put("itemcode", itemCode);
                            json.put("planqty", planQty);
                            json.put("actualprice", actualPrice);

                        }

                    }

                    if (state) {
                        //o2o保存
                        HashMap<String, Object> map = new HashMap<>();
                        json.accumulate("deliveryordercode", deliveryOrderCode);
                        json.accumulate("placeordertime", placeOrderTime);
                        json.accumulate("warehousecode", warehouseCode);
                        json.accumulate("c_v_employee_id", "");
                        json.accumulate("receiver_name", name);
                        json.accumulate("c_province_id", province);
                        json.accumulate("c_city_id", city);
                        json.accumulate("receiver_district", area);
                        json.accumulate("c_range_id", "");
                        json.accumulate("receiver_address", detailAddress);
                        json.accumulate("receiver_zip", "");//收货人邮编
                        json.accumulate("soreference4", "");//平台会员昵称
                        json.accumulate("totalamount", totalAmount);
                        json.accumulate("freight", freight);
                        json.accumulate("shopnick", shopNick);
                        json.accumulate("expresscode", "");
                        json.accumulate("logisticscode", "");
                        json.accumulate("logisticsname", "");

                        map.put("p_param", json.toString());
                        map.put("p_code", 0);
                        map.put("p_message", "");

                        HashMap<String, String> reqo2o = dbop.addD1MData("o2oorderDg", map);

                        if(null==reqo2o){
                            //调用保存o2o订单程序出现异常
                            try {
                                JSONObject reject = new JSONObject();
                                reject.put("orderCode", deliveryOrderCode);//传出库单号
                                reject.put("Note", "o2o订单(门店无法满足发货条件)");
                                reject.put("orderStatus", "CANCELED");//目前没有确定

                                SimpleDateFormat dfTemp = new SimpleDateFormat("yyyyMMddHHmmss");
                                String curTime = dfTemp.format(new Date());
                                reject.put("operateorTime", curTime);

                                HashMap<String, String> hmreject = E3requestUtil.getE3dataRequest("UpdateSalesOrder", reject.toString());
                                String e3reject = HttpRequest.doPost(e3url, hmreject, null);

                                if (e3reject != null && !e3reject.equals("")) {
                                    GlobalFun.debugOut("UpdateSalesOrder info result:" + e3reject);
                                    JSONObject e3jsonreject = JSONObject.fromObject(e3reject);
                                    if (e3jsonreject.get("message").equals("success")) {

                                        //重新给对比库存的sku赋值
                                        //e3data = JSONArray.fromObject(e3jsonreject.get("data").toString());
                                        GlobalFun.errorOut("oracle O2O订单自动取消 成功！");


                                    } else {
                                        GlobalFun.errorOut("oracle O2O订单自动取消 接口数据处理失败：" + e3jsonreject.get("message").toString());
                                    }

                                } else {
                                    //接口返回null
                                    GlobalFun.errorOut("oracle O2O订单自动取消 调用E3接口UpdateSalesOrder失败返回null");
                                }

                            } catch (Exception e2) {
                                //调用E3接口UpdateSalesOrder出现异常
                                GlobalFun.errorOut("oracle O2O订单自动取消 调用E3接口UpdateSalesOrder失败返回null error :" + GlobalFun.getCrashMessage(e2));
                                //return ResultGenerator.genFailResult("调用UpdateSalesOrder自动重新派单接口出现异常！");
                            }




                        }else {
                            //调用存储过程成功-获取处理结果
                            int p_code = Integer.valueOf(reqo2o.get("p_code"));
                            String p_message = String.valueOf(reqo2o.get("p_message"));
                            if (p_code==1){
                                String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
                                response.getOutputStream().write(retXML.getBytes());
                                return;
                            }else {

                                retFlag = "failure";
                                retCode = "-2";
                                retMsg = "parse return db Exception :"+p_message;
                                String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
                                response.getOutputStream().write(retXML.getBytes());
                                return;


                            }
                        }
                    } else {

                        //bos门店无法满足订单发货返回奇门调用接口成功，异步调用该订单取消接口

                        retFlag = "success";
                        retCode = "0";
                        retMsg = "bos门店无法满足订单发货，异步调用该订单取消接口";

                        String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
                        response.getOutputStream().write(retXML.getBytes());
                        return;

                    }

                } else {

                    retFlag = "failure";
                    retCode = "-2";
                    retMsg = "parse return xml not orderLines";
                    String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
                    response.getOutputStream().write(retXML.getBytes());
                    return;
                }


            } else {//未知类型

                retFlag = "failure";
                retCode = "-2";
                retMsg = "parse return xml not sourcePlatformCode";
                String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
                response.getOutputStream().write(retXML.getBytes());
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            retFlag = "failure";
            retCode = "-2";
            retMsg = "parse return xml failed";
            String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
            response.getOutputStream().write(retXML.getBytes());
            return;
        }

        /*String retXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><response><flag>" + retFlag + "</flag><code>" + retCode + "</code><message>" + retMsg + "</message></response>";
        response.getOutputStream().write(retXML.getBytes());
        return;*/
    }


}

