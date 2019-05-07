package wwinfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class test{

    public static void main(String[] args) {

       /* JSONObject jsonObject=JSONObject.fromObject("{\"data\":[]}");
        JSONArray items=jsonObject.getJSONArray("data");
        if (null != items && items.size()>0){
            System.out.println(111);
        }else {

            System.out.println(2222);
        }*/


        String key = "D1Mtest";
        String data="{\"is_valid\":\"Y\"}";
        String data1= "{\n" +
                "    \"vouchers_no\":\"w00001\",\n" +
                "    \"identify_code\":\"w00001\",\n" +
                "    \"vou_type\":\"VOU4\",\n" +
                "    \"vou_dis\":0.4,\n" +
                "    \"amt_discount\":100,\n" +
                "    \"amt_acount\":100,\n" +
                "    \"start_date\":\"20190111\",\n" +
                "    \"valid_date\":\"20190111\",\n" +
                "    \"is_valid\":\"Y\",\n" +
                "    \"is_verifyed\":\"N\",\n" +
                "    \"vip_id\":\"1472858\",\n" +
                "    \"vouchers_template_no\":\"y007\"\n" +
                "}\n";
        SimpleDateFormat dfTemp = new SimpleDateFormat("yyyyMMddHHmmss");
        String curTime = dfTemp.format(new Date());
        String requestTime = curTime;
        String signtest="key="+key+"&requestTime="+requestTime+
                "&secret=12345678test&version=1.0&func=couponemplate.select&data="+data;


        String checkSign = GlobalFun.secritMD5(signtest,"UTF-8");


        HashMap<String,String> hmParams=new HashMap<String,String>();

        String url="http://pos.gxg1978.com:8769/restapi";
        String url1="http://127.0.0.1:8080/restapi";
        String ur="http://60.12.223.144:8321/restapi";
        hmParams.put("key", key);
        hmParams.put("version","1.0");


        hmParams.put("requestTime",curTime);
        hmParams.put("func","couponemplate.select");
        hmParams.put("format","json");
        hmParams.put("sign",checkSign);
        hmParams.put("data",String.valueOf(data));

        System.out.println(hmParams.toString());
        String resp = GlobalFun.doPost(url,hmParams,null);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date())+2222);
        System.out.println(resp);

    }







    /*
*//*
    *//*
    public static void main(String[] args) {




*//*
        String key = "qimen";

        SimpleDateFormat dfTemp = new SimpleDateFormat("yyyyMMddHHmmss");
        String curTime = dfTemp.format(new Date());
        String requestTime = curTime;
        String filename="/Users/tang/Desktop/CKYCD2018102501271日志.txt";
        String data=readToString(filename);
        String signtest="key="+key+"&requestTime="+requestTime+
                "&secret=s83jfsuh3dfik3hj&version=1.0&func=e3trans.add&data="+data;


        String checkSign = GlobalFun.secritMD5(signtest,"UTF-8");


        HashMap<String,String> hmParams=new HashMap<String,String>();

        String url="http://127.0.0.1:8080/GXG/restapi";
            hmParams.put("key", key);
            hmParams.put("version","1.0");


        hmParams.put("requestTime",curTime);
        hmParams.put("func","e3trans.add");
        hmParams.put("format","json");
        hmParams.put("sign",checkSign);
        hmParams.put("data",String.valueOf(data));

        JSONObject json = null;
        boolean bSuccess = false;
        boolean bNeedResend = true;
        //if (tbname.compareTo("BOS_E3_STORAGE")==0){
        //	GlobalFun.debugOut("start post,time:"+(new Date()).toString());
        //}
        String resp = GlobalFun.doPost(url,hmParams,null);
        System.out.println(resp);*//*
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<request><deliveryOrder><deliveryOrderCode>18112952447178</deliveryOrderCode><orderType>JYCK</orderType><warehouseCode>G000096</warehouseCode><orderFlag></orderFlag><sourcePlatformCode>TB</sourcePlatformCode><sourcePlatformName>淘宝</sourcePlatformName><createTime>2018-11-29 17:44:05</createTime><placeOrderTime>2018-11-29 17:40:10</placeOrderTime><payTime>2018-11-29 17:40:29</payTime><payNo>253114799186053013</payNo><operateTime>2018-11-29 17:44:03</operateTime><shopNick>gxg官方旗舰店</shopNick><buyerNick>~MCUvWJcNeDGZlp9h82IZ2Q==~Qt1h31cRAncZYvyh3yWzWgFa+ZIt~1~~</buyerNick><totalAmount>398.00</totalAmount><itemAmount>398.00</itemAmount><discountAmount>0.00</discountAmount><freight>0.00</freight><arAmount>0</arAmount><gotAmount>398.00</gotAmount><logisticsCode>SF</logisticsCode><logisticsName>顺丰速运</logisticsName><expressCode>889567274809</expressCode><buyerMessage></buyerMessage><sellerMessage></sellerMessage><remark></remark><senderInfo><name>GXG</name><zipCode></zipCode><tel>0574-00000000</tel><mobile>0574-00000000</mobile><email></email><province>浙江省</province><city>宁波市</city><area>鄞州区</area><detailAddress>衫衫路111号</detailAddress></senderInfo><receiverInfo><name>~9rEcP0QEYsTefImxN1ngBw==~9w70~1~~</name><zipCode>723000</zipCode><tel></tel><mobile>$182$GCPn5+tXNCT1kPcfr8skgg==$1$</mobile><email></email><province>陕西省</province><city>汉中市</city><area>汉台区</area><detailAddress>北关街道椰岛广场盛世家园一号楼502室</detailAddress></receiverInfo><invoiceFlag>N</invoiceFlag><invoices><invoice><type></type><header></header><amount>398</amount><content></content></invoice></invoices></deliveryOrder><orderLines><orderLine><orderLineNo>32697704</orderLineNo><sourceOrderCode>253114799186053013</sourceOrderCode><subSourceOrderCode>253114799186053013</subSourceOrderCode><ownerCode>c1470754368990</ownerCode><itemCode>17410202700030</itemCode><itemName>长裤</itemName><planQty>2</planQty><actualPrice>199.00</actualPrice></orderLine></orderLines></request>";    try{
            Document docXML = DocumentHelper.parseText(data); // 将字符串转为XML
            Element rootElt = docXML.getRootElement(); // 获取根节点
            Element entryOrder = rootElt.element("deliveryOrder");

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
            try{
                totalAmount = entryOrder.element("totalAmount").getText();
            }catch(Exception e){
            }
            String freight = "";
            try{
                freight = entryOrder.element("freight").getText();
            }catch(Exception e){
            }

            //新增字段 2017-06-08
            String shopNick="";
            try{
                shopNick = entryOrder.element("shopNick").getText();
            }catch(Exception e){
            }

            Element orderLines = rootElt.element("orderLines");
            List<Element> listOrder = orderLines.elements("orderLine");
            if (listOrder.size()>0){
                List<HashMap> hmList = new ArrayList<HashMap>();
                for(int i=0;i<listOrder.size();i++){
                    Element order = listOrder.get(i);
                    String itemCode = order.element("itemCode").getText();
                    String planQty = order.element("planQty").getText();
                    String actualPrice = order.element("actualPrice").getText();
                    HashMap hm = new HashMap();
                    hm.put("deliveryOrderCode",deliveryOrderCode);
                    hm.put("warehouseCode",warehouseCode);
                    hm.put("RETAILBILLTYPE","正常零售");
                    hm.put("placeOrderTime",createTime);
                    hm.put("payTime",placeOrderTime);
                    hm.put("itemCode",itemCode);
                    hm.put("planQty",planQty);
                    hm.put("actualPrice",actualPrice);
                    hm.put("remark",remark);

                    hm.put("expressCode",expressCode);
                    hm.put("logisticsCode",logisticsCode);
                    hm.put("logisticsName",logisticsName);

                    hm.put("totalAmount",totalAmount);
                    hm.put("freight",freight);
                    hm.put("shopNick", shopNick);
                    hmList.add(hm);
	            	*//*
	            	if (!DBOperate.addData("E3_BOS_RETAIL", hm)){
	        			retFlag="failure";
	        			retCode="-1";
	            		retMsg="insert database failed";
	            		break;
	            	}
	            	*//*
                }

            }

    *//*public static void main(String[] args) {


       String filename="/Users/tang/Desktop/CKYCD2018102501271日志.txt";

        JSONArray jsonArray=JSONArray.fromObject(readToString(filename));
        System.out.println(jsonArray.get(0));
        String data=readToString(filename);
        String checkSign = GlobalFun.secritMD5(data,"UTF-8");
        System.out.println(checkSign);
        *//**//*for (int i = 0; i < jsonArray.size(); i++) {

            System.out.println(jsonArray.get(i));
        }*//**//*

    }*//*
   *//* public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }*//*
        } catch (DocumentException e) {
            e.printStackTrace();
            System.out.println(2222222);
        }
    }*/}
