package wwinfo;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class D1MController {

    private static DBOperate dbop = new DBOperate();


    //优惠卷模版查询接口处理
    public static void D1Mgetcouponemplate(String data, HttpServletResponse response) throws IOException {

        //HashMap<String,Object> hmRet=new HashMap<>();
        JSONObject res = new JSONObject();

        try {


            HashMap<String, String> map = new HashMap<>();
            JSONObject couponjson = JSONObject.fromObject(data);

            map.put("is_valid", couponjson.optString("is_valid"));
            List<HashMap> list = dbop.getD1MSendListMember("C_VOUCHERSemplate", map);
            if (null == list) {
                res.accumulate("status", "failure");
                res.accumulate("message", "No data query!");
                res.accumulate("data", "[]");
                GlobalFun.debugOut("从表C_VOUCHERS读取data数据失败" + ";time:" + (new Date()).toString());
                String json = res.toString();
                response.getOutputStream().write(json.getBytes("UTF-8"));
                return;
            } else {
                GlobalFun.debugOut("从表C_VOUCHERS读取到" + Integer.toString(list.size()) + "条数据需要data;time:" + (new Date()).toString());
                JSONArray jsonArray = new JSONArray();

                for (int j = 0; j < list.size(); j++) {
                    JSONObject jsonObject = new JSONObject();
                    HashMap<String, String> hmItem = list.get(j);

                    //GlobalFun.errorOut("map="+hmItem.toString());
                    //GlobalFun.errorOut("amt_noles="+hmItem.get("amt_noles"));

                    jsonObject.accumulate("vou_type", hmItem.get("VOU_TYPE"));
                    jsonObject.accumulate("vouchers_id", hmItem.get("ID"));
                    jsonObject.accumulate("vouchers_no", hmItem.get("VOUCHERS_NO"));

                    jsonObject.accumulate("amt_noles", hmItem.get("AMT_NOLES"));
                    jsonObject.accumulate("is_all_store", hmItem.get("IS_ALLSTORE"));

                    jsonArray.add(jsonObject);
                }
                res.accumulate("status", "success");
                res.accumulate("message", "成功");
                res.accumulate("data", jsonArray);
                String json = res.toString();
                response.getOutputStream().write(json.getBytes("UTF-8"));
                return;
            }


        } catch (Exception e) {
            e.printStackTrace();
            GlobalFun.errorOut("D1Mgetcouponemplate BUG :" + GlobalFun.getCrashMessage(e));
            res.accumulate("status", "failure");
            res.accumulate("message", "system exception");
            res.accumulate("data", "[]");
            String json = res.toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return;
        }


    }

    //优惠卷新增或者修改接口
    public static void D1Maddcoupon(String data, HttpServletResponse response) throws IOException {

        JSONObject resp = new JSONObject();
        Integer rp_code = 0;
        String rp_message = "";
        try {
            JSONObject req = JSONObject.fromObject(data);
            HashMap<String, Object> map = new HashMap<>();
            HashMap<String, Object> mapvip = new HashMap<>();
            //HashMap<String,Object> mapid=new HashMap<>();

            String p_vouchers_no = req.optString("vouchers_no");
            String p_identify_code = req.optString("identify_code");
            String p_vou_type = req.optString("vou_type");
            String p_vou_dis = req.optString("vou_dis");
            String p_amt_discount = req.optString("amt_discount");
            String p_amt_acount = req.optString("amt_acount");
            String p_start_date = req.optString("start_date");
            String p_valid_date = req.optString("valid_date");
            String p_is_valid = req.optString("is_valid");
            String p_is_verifyed = req.optString("is_verifyed");
            String p_vip_id = req.optString("vip_id");
            String p_vouchers_template_no = req.optString("vouchers_template_no");
            String vouchers_id = req.optString("vouchers_id");
            if (vouchers_id != null && !vouchers_id.equals("")) {

                //修改优惠卷
                map.put("vouchers_no", p_vouchers_no);
                map.put("identify_code", p_identify_code);
                map.put("vou_type", p_vou_type);
                map.put("vou_dis", p_vou_dis);
                map.put("amt_discount", p_amt_discount);
                map.put("amt_acount", p_amt_acount);
                map.put("start_date", p_start_date);
                map.put("valid_date", p_valid_date);
                map.put("is_valid", p_is_valid);
                map.put("is_verifyed", p_is_verifyed);
                map.put("vouchers_template_no", p_vouchers_template_no);
                //map.put("vouchers_id",vouchers_id);


                boolean st = dbop.updateD1Mcoupon(map, "D1Mc_vouchers");
                if (st) {
                    //修改成功
                    //mapid.put("vouchers_no", p_vouchers_no);
                    List<HashMap> list = dbop.getD1MSendListMember("C_VOUCHERSID", map);

                    String id = String.valueOf(list.get(0).get("ID"));
                    if (id != null && !id.equals("")) {
                        //获取优惠卷id成功修改vip
                        if (p_vip_id != null && !p_vip_id.equals("")) {
                            mapvip.put("vip_id", p_vip_id);
                            mapvip.put("id", id);
                            boolean stvip = dbop.updateD1Mcoupon(mapvip, "D1Mc_vouchersvip");
                            if (stvip) {
                                resp.accumulate("status", "success");
                                resp.accumulate("message", "成功!");
                                resp.accumulate("data", "[]");
                                String json = resp.toString();
                                response.getOutputStream().write(json.getBytes("UTF-8"));
                                return;
                            } else {
                                resp.accumulate("status", "failure");
                                resp.accumulate("message", "system exception-修改vipid失败");
                                resp.accumulate("data", "[]");
                                String json = resp.toString();
                                response.getOutputStream().write(json.getBytes("UTF-8"));
                                return;
                            }
                        }
                        //修改成功
                        resp.accumulate("status", "success");
                        resp.accumulate("message", "成功");
                        resp.accumulate("data", "[]");
                        String json = resp.toString();
                        response.getOutputStream().write(json.getBytes("UTF-8"));
                        return;
                    } else {
                        //获取优惠卷id失败
                        resp.accumulate("status", "failure");
                        resp.accumulate("message", "system exception-获取优惠卷ID失败");
                        resp.accumulate("data", "[]");
                        String json = resp.toString();
                        response.getOutputStream().write(json.getBytes("UTF-8"));
                        return;
                    }

                } else {
                    //修改失败
                    resp.accumulate("status", "failure");
                    resp.accumulate("message", "system exception-修改优惠卷主表信息失败");
                    resp.accumulate("data", "[]");
                    String json = resp.toString();
                    response.getOutputStream().write(json.getBytes("UTF-8"));
                    return;
                }


            } else {//新增优惠卷

                map.put("p_vouchers_no", p_vouchers_no);
                map.put("p_identify_code", p_identify_code);
                map.put("p_vou_type", p_vou_type);
                map.put("p_vou_dis", p_vou_dis);
                map.put("p_amt_discount", p_amt_discount);
                map.put("p_amt_acount", p_amt_acount);
                map.put("p_start_date", p_start_date);
                map.put("p_valid_date", p_valid_date);
                map.put("p_is_valid", p_is_valid);
                map.put("p_vip_id", p_vip_id);
                map.put("p_is_verifyed", p_is_verifyed);
                map.put("p_vouchers_template_no", p_vouchers_template_no);
                map.put("p_code", rp_code);
                map.put("p_message", rp_message);

                HashMap<String, String> list = dbop.addD1MData("d1m_to_bos_vouchers", map);
                GlobalFun.errorOut("存储过程返回LIST：" + list);
                if (null == list) {
                    resp.accumulate("status", "failure");
                    resp.accumulate("message", "No data query-procedure");
                    resp.accumulate("data", JSONArray.fromObject(""));
                    GlobalFun.debugOut("D1M 新增优惠卷存储过程返回null data ：" + map);
                    String json = resp.toString();
                    response.getOutputStream().write(json.getBytes("UTF-8"));
                    return;
                } else {

                    /*for (int j = 0; j < list.size(); j++) {

                        HashMap<String, String> hmItem = list.get(j);*/
                    int p_code = Integer.valueOf(list.get("p_code"));
                    String p_message = String.valueOf(list.get("p_message"));

                    if (p_code == 1) {
                        resp.accumulate("status", "success");
                        resp.accumulate("message", "成功");
                        JSONObject object = new JSONObject();
                        JSONArray array = new JSONArray();
                        object.accumulate("vouchers_id", String.valueOf(list.get("p_vouchersid")));
                        array.add(object);
                        resp.accumulate("data", array);
                        String json = resp.toString();
                        response.getOutputStream().write(json.getBytes("UTF-8"));
                        return;
                    } else if (p_code == 2) {
                        resp.accumulate("status", "failure");
                        resp.accumulate("message", p_message);
                        resp.accumulate("data", "[]");
                        String json = resp.toString();
                        response.getOutputStream().write(json.getBytes("UTF-8"));
                        return;

                    }
                }

            }
            /*}*/
        } catch (Exception e) {
            e.printStackTrace();
            GlobalFun.errorOut("D1Maddcoupon BUG :" + GlobalFun.getCrashMessage(e));
            resp.accumulate("status", "failure");
            resp.accumulate("message", "system exception");
            resp.accumulate("data", "[]");
            String json = resp.toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return;

        }


    }

    //优惠卷查询
    public static void D1Mgetcoupon(String data, HttpServletResponse response) throws IOException {

        //HashMap<String,Object> hmRet=new HashMap<>();
        JSONObject res = new JSONObject();

        try {


            HashMap<String, String> map = new HashMap<>();
            JSONObject couponjson = JSONObject.fromObject(data);

            map.put("vouchers_no", couponjson.optString("vouchers_no"));
            map.put("is_valid", couponjson.optString("is_valid"));
            List<HashMap> list = dbop.getD1MSendListMember("C_VOUCHERS", map);
            if (null == list || list.isEmpty()) {
                res.accumulate("status", "failure");
                res.accumulate("message", "No data query");
                res.accumulate("data", "[]");
                GlobalFun.debugOut("从表C_VOUCHERS读取data数据失败" + ";time:" + (new Date()).toString());
                String json = res.toString();
                response.getOutputStream().write(json.getBytes("UTF-8"));
                return;
            } else {
                GlobalFun.debugOut("从表C_VOUCHERS读取到" + Integer.toString(list.size()) + "条数据需要data;time:" + (new Date()).toString());
                //JSONArray jsonArray=new JSONArray();
                JSONObject jsonObject = new JSONObject();
                for (int j = 0; j < list.size(); j++) {

                    HashMap<String, String> hmItem = list.get(j);
                    jsonObject.accumulate("vouchers_no", hmItem.get("VOUCHERS_NO"));
                    jsonObject.accumulate("vouchers_id", hmItem.get("ID"));
                    jsonObject.accumulate("vou_type", hmItem.get("VOU_TYPE"));
                    jsonObject.accumulate("vou_dis", hmItem.get("VOU_DIS"));
                    jsonObject.accumulate("amt_discount", hmItem.get("AMT_DISCOUNT"));
                    jsonObject.accumulate("amt_acount", hmItem.get("AMT_ACOUNT"));
                    jsonObject.accumulate("start_date", hmItem.get("START_DATE"));
                    jsonObject.accumulate("valid_date", hmItem.get("VALID_DATE"));
                    jsonObject.accumulate("is_valid", hmItem.get("IS_VALID"));
                    jsonObject.accumulate("is_verifyed", hmItem.get("IS_VERIFYED"));
                    jsonObject.accumulate("vip_id", hmItem.get("C_VIPID"));
                    jsonObject.accumulate("vouchers_template_no", hmItem.get("VOUCHERS_NO"));
                }
                res.accumulate("status", "success");
                res.accumulate("message", "成功");
                res.accumulate("data", jsonObject);
                String json = res.toString();
                response.getOutputStream().write(json.getBytes("UTF-8"));
                return;
            }


        } catch (Exception e) {
            e.printStackTrace();
            GlobalFun.errorOut("D1Mgetcoupon BUG :" + GlobalFun.getCrashMessage(e));
            res.accumulate("status", "failure");
            res.accumulate("message", "system exception");
            res.accumulate("data", "[]");
            String json = res.toString();
            response.getOutputStream().write(json.getBytes("UTF-8"));
            return;
        }


    }

    public static void ordercancel(String data, HttpServletResponse response) {

        JSONObject resp = new JSONObject();
        JSONObject jsonObject = JSONObject.fromObject(data);
        HashMap<String, Object> mapid = new HashMap<>();

        mapid.put("docno", jsonObject.getString("ordercode"));

        List<HashMap> list = dbop.getD1MSendListMember("eb_orderso", mapid);
        String id = String.valueOf(list.get(0).get("ID"));
        if (id != null && !id.equals("")) {

            HashMap<String, Object> map = new HashMap<>();
            map.put("p_submittedsheetid", id);
            map.put("p_code", "");
            map.put("p_message", "");
            HashMap<String, String> mapccgc = dbop.addD1MData("EB_SO_UNSUBMIT", map);
            GlobalFun.errorOut("存储过程返回：" + mapccgc);

            if (null == mapccgc) {
                resp.accumulate("status", "failure");
                resp.accumulate("message", "No data query-procedure");
                resp.accumulate("data", "[]");
                GlobalFun.debugOut("O2O 订单取消失败存储过程返回null data ：" + map);
                String json = resp.toString();
                try {
                    response.getOutputStream().write(json.getBytes("UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            } else {

                int p_code = Integer.valueOf(mapccgc.get("p_code"));
                String p_message = String.valueOf(mapccgc.get("p_message"));

                if (p_code == 1) {
                    resp.accumulate("status", "success");
                    resp.accumulate("message", "成功");
                    resp.accumulate("data","[]");
                    String json = resp.toString();
                    try {
                        response.getOutputStream().write(json.getBytes("UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                } else if (p_code == 2) {
                    resp.accumulate("status", "failure");
                    resp.accumulate("message", p_message);
                    resp.accumulate("data", "[]");
                    String json = resp.toString();
                    try {
                        response.getOutputStream().write(json.getBytes("UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;

                }

            }
        } else {
            //
            resp.accumulate("status", "failure");
            resp.accumulate("message", "system exception-获取订单ID失败");
            resp.accumulate("data", "[]");
            String json = resp.toString();
            try {
                response.getOutputStream().write(json.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }



        //p_submittedsheetid
        //p_code
        //p_message

    }
}
