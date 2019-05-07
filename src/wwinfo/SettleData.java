package wwinfo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

//处理数据库内需要发送的数据类

public class SettleData {
	//private final static boolean isTest = false;
	//E3参数
	private String keyE3="E3_BOS";
	private String secretE3="1a2b3c4d5e6f7g8h9i10j11k12b";
	private String e3ver = "2.0";
	private String urlE3="http://114.55.59.123/e3test/webopm/web/?app_act=api/service&app_mode=func";
	private String yunSecretKey="8ad47644ca51d953f6e3bd6174114af5";
	private String urlE3yun="http://gxgyun.gxglife.com/GxgYun/";
	//奇门参数
	private String qimenCustomerId = "c1470792410866";
	private String qimenVer = "2.0";
	private String qimenAppKey="12585822";
	private String qimenSecret="1fc88e271082618023b4db126f76426e";
	private String qimenUrl="http://qimen.api.taobao.com/router/qimen/service?";
	//E3参数2
	private String keyE3_2="";
	private String secretE3_2="";
	private String e3ver_2 = "2.0";
	private String urlE3_2="";
	private String yunSecretKey_2="";
	private String urlE3yun_2="";
	//奇门参数2
	private String qimenCustomerId_2 = "";
	private String qimenVer_2 = "2.0";
	private String qimenAppKey_2="";
	private String qimenSecret_2="";
	private String qimenUrl_2="";
	
	private final static int tryTimes = 15;			//发送重试次数
	private final static long spliteTime = 5000;			//与Timer.java中一样
	private final static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private DBOperate dbop = null;
	
	SettleData(){
		readConfig();
		dbop = new DBOperate();
	}
	
	/*
	 * 读取配置信息，文件：classes\config.properties
	 */
	private boolean readConfig(){
		String webInfoPath = GlobalFun.getWebInfPath();
		String file = webInfoPath + "classes/config.properties";

		boolean ret = true;
		try{
			InputStream is = new FileInputStream(file);
			Properties p = new Properties();
			p.load(is);
			//E3参数
			String url = p.getProperty("e3_service");
			String appkey = p.getProperty("e3_key");
			String appsecrit = p.getProperty("e3_secrit");
			if(null == url || url.isEmpty()
				|| null == appkey || appkey.isEmpty()
				|| null == appsecrit || appsecrit.isEmpty()){
				ret = false;
			}else{
				urlE3 = url.trim();
				keyE3 = appkey.trim();
				secretE3 = appsecrit.trim();
			}
			//第二个接口
			String url2 = p.getProperty("e3_service2");
			String appkey2 = p.getProperty("e3_key2");
			String appsecrit2 = p.getProperty("e3_secrit2");
			if(null != url2 && !url2.isEmpty()
				&& null != appkey2 && !appkey2.isEmpty()
				&& null != appsecrit2 && !appsecrit2.isEmpty()){
				urlE3_2 = url2.trim();
				keyE3_2 = appkey2.trim();
				secretE3_2 = appsecrit2.trim();
			}
			
			//GXG云仓参数
			url = p.getProperty("gxgyun_service");
			appsecrit = p.getProperty("gxgyun_secret");
			if(null == url || url.isEmpty()
				|| null == appsecrit || appsecrit.isEmpty()){
				ret = false;
			}else{
				urlE3yun = url.trim();
				yunSecretKey = appsecrit.trim();
			}
			//第二个接口
			url2 = p.getProperty("gxgyun_service2");
			appsecrit2 = p.getProperty("gxgyun_secret2");
			if(null != url2 && !url2.isEmpty()
				&& null != appsecrit2 && !appsecrit2.isEmpty()){
				urlE3yun_2 = url2.trim();
				yunSecretKey_2 = appsecrit2.trim();
			}
			
			//奇门参数
			url = p.getProperty("qimen_service");
			appkey = p.getProperty("qimen_appkey");
			appsecrit = p.getProperty("qimen_secret");
			String customerID = p.getProperty("qimen_customerid");
			if(null == url || url.isEmpty()
				|| null == appkey || appkey.isEmpty()
				|| null == appsecrit || appsecrit.isEmpty()
				|| null == customerID || customerID.isEmpty()){
				ret = false;
			}else{
				qimenUrl = url.trim();
				qimenAppKey = appkey.trim();
				qimenSecret = appsecrit.trim();
				qimenCustomerId = customerID.trim();
			}
			//第二个接口
			url2 = p.getProperty("qimen_service2");
			appkey2 = p.getProperty("qimen_appkey2");
			appsecrit2 = p.getProperty("qimen_secret2");
			String customerID2 = p.getProperty("qimen_customerid2");
			if(null != url2 && !url2.isEmpty()
				&& null != appkey2 && !appkey2.isEmpty()
				&& null != appsecrit2 && !appsecrit2.isEmpty()
				&& null != customerID2 && !customerID2.isEmpty()){
				qimenUrl_2 = url2.trim();
				qimenAppKey_2 = appkey2.trim();
				qimenSecret_2 = appsecrit2.trim();
				qimenCustomerId_2 = customerID2.trim();
			}
		}catch(Exception e){
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}
	
	public void SendData(String index){
		if (index.compareTo("2")==0){
			GlobalFun.writeSysError("门店库存同步开始");
			dbop.flushDataMember();
			//BOS2E3_SyncStore();		//2.1	门店库存同步
			GlobalFun.writeSysError("门店库存同步结束");
		}else{
			GlobalFun.debugOut("其它同步发送开始");
			dbop.flushDataMember();
			//BOS2E3();
			BOS2E3_2();
			
			//BOS2QIMEN();		//单表读取并发送给奇门
			//BOS2QIMEN_PURCHASE();
			//BOS2QIMEN_STOCKOUT();
			//BOS2QIMEN_TRANSFER();
			//BOS2QIMEN_RETSALE();
			GlobalFun.debugOut("其它同步发送结束");

			GlobalFun.debugOut("云仓同步发送开始");
			//BOS2E3yun();
			GlobalFun.debugOut("云仓同步发送结束");
		}
	}
	
	//获取发送服务对象的标记
	public int getApiFlag(HashMap hm){
		Object apiFlag = hm.get("API_FLAG");
		int apiType = 1;
		if (null != apiFlag){
			try{
				apiType = Integer.parseInt(apiFlag.toString());
				if (apiType < 1 || apiType > 3)
					apiType = 1;
			}catch(Exception e){
				e.printStackTrace();
			}
		}	
		return apiType;
	}
	
	//单表读取并发送给E3
	public void BOS2E3(){
		final String[] tablesBOS_E3={
			"BOS_E3_COLOR",
			"BOS_E3_SIZE",
			"BOS_E3_SIZEGROUP",
			"BOS_E3_DIM",
			"BOS_E3_PRODUCT",			//5
			"BOS_E3_STORE",
			"BOS_E3_SUPPLIER"
		};
		final String[][] fieldsBOS_E3={
			{"COLOR_CODE","COLOR_NAME","COLOR_NOTE"},
			{"SIZE_CODE","SIZE_NAME","SIZE_NOTE"},
			{"ID","NO","NAME","VALUE","NOTE","MATRIX_COL","STATUS"},
			{"CODE","NAME","P_CODE","P_NAME","NOTE"},
			{"GOODS_SN","GOODS_NAME","GOODS_SNAME","CAT_CODE","TOP_CAT_NAME","BRAND_CODE","YEAR_CODE","SEASON_CODE","SERIES_CODE","GHSDM","MARKET_PRICE","CBJ","BZSJ","IS_ON_SALE","ADD_TIME","CREATOR","MODIFIER","LAST_UPDATE"},
			//5
			{"STORECODE","STORENAME","BRAND_CODE","DISTRIBUTOR","REGION","ORGCODE","BUSINESSTYPE","CUSTOMERTYPE","RETAILTYPE","ISVALID","CREATETIME","LATESTUPDATETIME","NAME","ZIPCODE","TEL","MOBILE","FAX","PROVINCE","CITY","AREA","TOWN","DETAILADDRESS"},
			{"GHSDM","GHSMC","QDCODE","LBCODE","QYCODE","ISVALID","CREATETIME","LATESTUPDATETIME","NAME","ZIPCODE","TEL","MOBILE","FAX","PROVINCE","CITY","AREA","TOWN","DETAILADDRESS"}
		};
		final String[] nameE3API={
			"colors.add",
			"sizes.add",
			"sizegroups.add",
			"cats.add",
			"products.add",				//5
			"stores.add",
			"suppliers.add"
		};
		final String[][] fieldsE3API={
			{"color_code","color_name","color_note"},
			{"size_code","size_name","size_note"},
			{"id","no","name","value","note","matrix_col","status"},
			{"code","name","p_code","p_name","note"},
			{"goods_sn","goods_name","goods_sname","cat_code","top_cat_name","brand_code","year_code","season_code","series_code","ghsdm","market_price","cbj","bzsj","is_on_sale","add_time","creator","modifier","last_update"},
			//5
			{"storeCode","storeName","brand_code","distributor","region","orgCode","businessType","customerType","retailType","isValid","createTime","latestUpdateTime","detail\":{\"name","zipCode","tel","mobile","fax","province","city","area","town","detailAddress"},
			{"ghsdm","ghsmc","qdCode","lbCode","qyCode","isValid","createTime","latestUpdateTime","detail\":{\"name","zipCode","tel","mobile","fax","province","city","area","town","detailAddress"}
		};
		final String[] appendFieldsE3API={
			"",
			"",
			"",
			"",
			",\"disable\":\"0\"",		//5
			"}",
			"}"
		};

		int num = tablesBOS_E3.length;
		for(int i=0;i<num;i++){
			while(true){
				List<HashMap> list = dbop.getSendListMember(tablesBOS_E3[i]);
				if (null == list){
					GlobalFun.debugOut("从表"+tablesBOS_E3[i]+"读取发送数据失败"+";time:"+(new Date()).toString());
				}else{

					GlobalFun.debugOut("从表"+tablesBOS_E3[i]+"读取到"+Integer.toString(list.size())+"条数据需要发送;time:"+(new Date()).toString());
				}
				if (null != list && list.size()>0){
					for(int j =0;j<list.size();j++){
						HashMap hmItem = list.get(j);
						String id = null;
						try{
							id = hmItem.get("ID").toString();
						}catch(Exception e){
							e.printStackTrace();
						}
						if (null == id){
							GlobalFun.debugOut("表"+tablesBOS_E3[i]+"中有ID为NULL");
							continue;
						}
						
						Date lastReadTime = null;
						StringBuilder jsonBuilder = new StringBuilder();
						jsonBuilder.append("[{");
						for(int k=0;k<fieldsE3API[i].length;k++){
							if (null ==lastReadTime && null != hmItem.get("READTIME"))
								lastReadTime = (Date)hmItem.get("READTIME");
							if (k> 0)
								jsonBuilder.append(",");
							jsonBuilder.append("\"");
							jsonBuilder.append(fieldsE3API[i][k]);
							jsonBuilder.append("\":\"");
							String value="";
							Object fieldValue = hmItem.get(fieldsBOS_E3[i][k]);
							if (null != fieldValue){
								if (fieldValue instanceof Date){
									//value = Long.toString(((Date)fieldValue).getTime()/1000);
									value = df.format((Date)fieldValue);
								}else{
									try{
										value = fieldValue.toString();
									}catch(Exception e){
										e.printStackTrace();
									}
								}
							}
							jsonBuilder.append(value);
							jsonBuilder.append("\"");
						}
						jsonBuilder.append(appendFieldsE3API[i]);
						jsonBuilder.append("}]");

						int apiFlag = getApiFlag(hmItem);
						CallE3API(nameE3API[i],jsonBuilder.toString(),tablesBOS_E3[i],id,lastReadTime,apiFlag);//
					}
					GlobalFun.debugOut("从表"+tablesBOS_E3[i]+"读取的数据发送结束;time:"+(new Date()).toString());
				}else{
					break;
				}
			}
		}
	}

	//2.1	门店库存同步，单独处理
	public void BOS2E3_SyncStore(){
		while(true){
			GlobalFun.writeSysError("开始从表BOS_E3_STORAGE中读取要发送数据");
			List<HashMap> list = dbop.getSendListMember("BOS_E3_STORAGE");
			if (null != list && list.size()>0){
				int iCurNum = 0;
				String preCKDM="";
				int preApiFlag = 1;
				StringBuilder jsonBuilder = new StringBuilder();
				Date lastReadTime = null;
				List<HashMap> listID=new ArrayList<HashMap>();
				for(int j =0;j<list.size();j++){
					HashMap hmItem = list.get(j);
					String id = null;
					try{
						id = hmItem.get("ID").toString();
					}catch(Exception e){
						e.printStackTrace();
					}
					if (null == id){
						GlobalFun.writeSysError("表BOS_E3_STORAGE中有ID为NULL");
						continue;
					}
					
					String curCKDM=(String)hmItem.get("CKDM");
					int curApiFlag = getApiFlag(hmItem);
					lastReadTime = (Date)hmItem.get("READTIME");
					if ((iCurNum > 30) || (iCurNum > 0 && 
							(curCKDM.compareTo(preCKDM)!=0 || curApiFlag != preApiFlag)
							)){
						//发送之前的门店库存
						jsonBuilder.append("]}");
						GlobalFun.writeSysError("start send storage,num:"+Integer.toString(iCurNum)+";time:"+(new Date()).toString());
						CallE3API("sync_store_inventory",jsonBuilder.toString(),"BOS_E3_STORAGE",listID,lastReadTime,preApiFlag);
						GlobalFun.writeSysError("send storage end"+";time:"+(new Date()).toString());
						jsonBuilder.setLength(0);
						iCurNum = 0;
						preApiFlag = curApiFlag;
						listID.clear();
					}
					if (0 == iCurNum){
						jsonBuilder.append("{");
						jsonBuilder.append("\"ckdm\":\"");
						jsonBuilder.append(curCKDM);
						jsonBuilder.append("\",\"kc_data\":[");
					}else{
						jsonBuilder.append(",");
					}
					jsonBuilder.append("{\"spdm\":\"");
					jsonBuilder.append(null == hmItem.get("SPDM")?"":hmItem.get("SPDM"));
					jsonBuilder.append("\",\"gg1dm\":\"");
					jsonBuilder.append(null == hmItem.get("GG1DM")?"":hmItem.get("GG1DM"));
					jsonBuilder.append("\",\"gg2dm\":\"");
					jsonBuilder.append(null == hmItem.get("GG2DM")?"":hmItem.get("GG2DM"));
					jsonBuilder.append("\",\"sl\":\"");
					jsonBuilder.append(hmItem.get("SL"));
					jsonBuilder.append("\",\"skuCode\":\"");
					String skuCode = (null == hmItem.get("SKUCODE")?"":(String)hmItem.get("SKUCODE"));
					jsonBuilder.append(skuCode);
					jsonBuilder.append("\"}");
					
					HashMap hmId = new HashMap();
					hmId.put("id", id);
					hmId.put("sku",skuCode);
					listID.add(hmId);
					
					if (0 == iCurNum)
						preCKDM = curCKDM;
					iCurNum++;
				}
				if (iCurNum > 0){
					jsonBuilder.append("]}");
					GlobalFun.writeSysError("往E3发送库存同步");
					CallE3API("sync_store_inventory",jsonBuilder.toString(),"BOS_E3_STORAGE",listID,lastReadTime,preApiFlag);
					jsonBuilder.setLength(0);
					iCurNum = 0;
				}else{
					GlobalFun.writeSysError("当前要发送数据条数为0");
				}
			}else{
				GlobalFun.writeSysError("从表BOS_E3_STORAGE中读取的数据条数为0");
				break;
			}
		}
	}
	
	//主从表读取并发送给E3
	public void BOS2E3_2(){
		final String tableMain[]={
			"BOS_E3_TRANSFER",
			"BOS_E3_LOCK"
		};
		final String[] apiName={
			"ckycd.add",
			"kcsdd.create.ckycd"
		};
		final String[][] mainTableField={
			{"WMSBILLCODE","OUTWAREHOUSECODE","INWAREHOUSECODE","BILLSTATUS","BILLDATE","CREATEEMP","CREATEDATE","CHECKEMP","CHECKDATE"},
			{"BILLCODE","WAREHOUSECODE","OPTYPE","BILLDATE","NOTE"}
		};
		final String[][] mainJSONField={
			{"wmsBillCode","outWarehouseCode","inWarehouseCode","billStatus","billDate","createEmp","createDate","checkEmp","checkDate"},
			{"billCode","warehouseCode","opType","billDate","note"}
		};
		final String[] appendFields={
			",\"chargeEmp\":\"\",\"chargeDate\":\"\",\"note\":\"\"",
			""
		};
		
		final String tableSub[]={
			"BOS_E3_TRANSFERITEM",
			"BOS_E3_LOCKITEM"
		};
		final String[] subJSONFieldName={
			"products",
			"products"
		};
		final String[] subJSONName={
			"product",
			"product"
		};
		final String[][] subTableField={
			{"SKUCODE","NORMALQUANTITY","DEFECTIVEQUANTITY"},
			{"SKUCODE","QUANTITY"}
		};
		final String[][] subJSONField={
			{"skuCode","normalQuantity","defectiveQuantity"},
			{"skuCode","quantity"}
		};
		
		int num = tableMain.length;
		for(int i=0;i<num;i++){
			while(true){
				List<HashMap> list = dbop.getSendListMember(tableMain[i]);
				if (null == list){
					GlobalFun.debugOut("从表"+tableMain[i]+"读取发送数据失败"+";time:"+(new Date()).toString());					
				}else{
					GlobalFun.debugOut("从表"+tableMain[i]+"读取到"+Integer.toString(list.size())+"条数据需要发送;time:"+(new Date()).toString());
				}
				if (null != list && list.size()>0){
					for(int j =0;j<list.size();j++){
						HashMap hmItem = list.get(j);
						String id = null;
						try{
							id = hmItem.get("ID").toString();
						}catch(Exception e){
							e.printStackTrace();
						}
						if (null == id){
							GlobalFun.debugOut("表"+tableMain[i]+"中有ID为NULL");
							continue;
						}
						int apiFlag = getApiFlag(hmItem);
						
						StringBuilder jsonBuilder = new StringBuilder();
						jsonBuilder.append("{");
						for(int k=0;k<mainTableField[i].length;k++){
							if (k> 0)
								jsonBuilder.append(",");
							jsonBuilder.append("\"");
							jsonBuilder.append(mainJSONField[i][k]);
							jsonBuilder.append("\":\"");
							String value="";
							Object fieldValue = hmItem.get(mainTableField[i][k]);
							if (null != fieldValue){
								if (fieldValue instanceof Date){
									//value = Long.toString(((Date)fieldValue).getTime()/1000);
									value = df.format((Date)fieldValue);
								}else{
									try{
										value = fieldValue.toString();
									}catch(Exception e){
										e.printStackTrace();
									}
								}
							}
							jsonBuilder.append(value);
							jsonBuilder.append("\"");
						}
						jsonBuilder.append(appendFields[i]);
						
						//读取子表数据
						jsonBuilder.append(",\"");
						jsonBuilder.append(subJSONFieldName[i]);
						jsonBuilder.append("\":{\"");
						jsonBuilder.append(subJSONName[i]);
						jsonBuilder.append("\":[");
						List<HashMap> subList = dbop.getSendSubListMember(tableSub[i],Long.valueOf(id));
						if (null != subList && subList.size()>0){
							for(int ii=0;ii<subList.size();ii++){
								HashMap hmSubItem = subList.get(ii);
								if (ii> 0)
									jsonBuilder.append(",");
								jsonBuilder.append("{");
								for(int k=0;k<subJSONField[i].length;k++){
									if (k>0)
										jsonBuilder.append(",");
									jsonBuilder.append("\"");
									jsonBuilder.append(subJSONField[i][k]);
									jsonBuilder.append("\":\"");
									String value="";
									Object fieldValue = hmSubItem.get(subTableField[i][k]);
									if (null != fieldValue){
										if (fieldValue instanceof Date){
											value = df.format((Date)fieldValue);
										}else{
											try{
												value = fieldValue.toString();
											}catch(Exception e){
												e.printStackTrace();
											}
										}
									}
									jsonBuilder.append(value);
									jsonBuilder.append("\"");
								}
								jsonBuilder.append("}");
							}
						}
						jsonBuilder.append("]}");
						
						jsonBuilder.append("}");
	
						Date lastReadTime = (Date)hmItem.get("READTIME");
						if (tableMain[i].compareTo("BOS_E3_LOCK")==0){
							_CallE3API(apiName[i],jsonBuilder.toString(),tableMain[i],id,lastReadTime,hmItem,apiFlag);
						}else
							CallE3API(apiName[i],jsonBuilder.toString(),tableMain[i],id,lastReadTime,apiFlag);
					}
					GlobalFun.debugOut("从表"+tableMain[i]+"读取的数据发送结束;time:"+(new Date()).toString());
				}else{
					break;
				}
			}
		}
	}

	public void CallE3API(String apiName,String data,String tbname,Object id,Date lastReadTime,int apiFlag){
		_CallE3API(apiName,data,tbname,id,lastReadTime,null,apiFlag);
	}
	
	public void _CallE3API(String apiName,String data,String tbname,Object id,Date lastReadTime,HashMap orgInfo,int apiFlag){
		/*
		final String keyE3="E3_BOS";
		final String secretE3="1a2b3c4d5e6f7g8h9i10j11k12b";
		final String ver = "2.0";
		//正式地址
		String urlE3="http://115.238.188.14:9001/e3/webopm/web/?app_act=api/service&app_mode=func";
		if (isTest){
			//测试地址
			urlE3="http://115.238.188.14:9001/e3test/webopm/web/?app_act=api/service&app_mode=func";
		}
		*/
		SimpleDateFormat dfTemp = new SimpleDateFormat("yyyyMMddHHmmss");
		String curTime = dfTemp.format(new Date());
		String signString = "";
		if (2 == apiFlag || 3== apiFlag){
			signString ="key="+keyE3_2+"&requestTime="+curTime+
					"&secret="+secretE3_2+"&version="+e3ver_2+
					"&serviceType="+apiName+"&data="+data;
		}else{
			signString ="key="+keyE3+"&requestTime="+curTime+
					"&secret="+secretE3+"&version="+e3ver+
					"&serviceType="+apiName+"&data="+data;
		}
		String sign = GlobalFun.secritMD5(signString,"UTF-8");
		HashMap<String,String> hmParams=new HashMap<String,String>();
		String url=urlE3;
		if (2 == apiFlag || 3== apiFlag){
			hmParams.put("key", keyE3_2);
			hmParams.put("version",e3ver_2);
			url =urlE3_2;
		}else{
			hmParams.put("key", keyE3);
			hmParams.put("version",e3ver);
			url =urlE3;
		}
		hmParams.put("requestTime",curTime);
		hmParams.put("serviceType",apiName);
		hmParams.put("format","json");
		hmParams.put("sign",sign);
		hmParams.put("data",data);
		
		JSONObject json = null;
		boolean bSuccess = false;
		boolean bNeedResend = true;
		//if (tbname.compareTo("BOS_E3_STORAGE")==0){
		//	GlobalFun.debugOut("start post,time:"+(new Date()).toString());
		//}
		String resp = GlobalFun.doPost(url,hmParams,null);
		if (null != resp){
			//if (tbname.compareTo("BOS_E3_STORAGE")==0){
			//	GlobalFun.debugOut("end post,time:"+(new Date()).toString());
			//}
			json = JSONObject.fromObject(resp);
			if (null != json && 0 == "success".compareTo(json.getString("status"))){
				bSuccess = true;
			}
			bNeedResend = false;
		}
		if (bSuccess && 3 == apiFlag){
			GlobalFun.debugOut("success！send to second server.");
			signString ="key="+keyE3+"&requestTime="+curTime+
					"&secret="+secretE3+"&version="+e3ver+
					"&serviceType="+apiName+"&data="+data;
			sign = GlobalFun.secritMD5(signString,"UTF-8");
			hmParams.put("key", keyE3);
			hmParams.put("version",e3ver);
			url=urlE3;
			hmParams.put("requestTime",curTime);
			hmParams.put("serviceType",apiName);
			hmParams.put("format","json");
			hmParams.put("sign",sign);
			hmParams.put("data",data);
			
			json = null;
			bSuccess = false;
			resp = GlobalFun.doPost(url,hmParams,null);
			if (null != resp){
				json = JSONObject.fromObject(resp);
				if (null != json && 0 == "success".compareTo(json.getString("status"))){
					bSuccess = true;
				}
				bNeedResend = false;
			}
		}
		
		if (bSuccess){
			GlobalFun.debugOut("success！");
			if (tbname.compareTo("BOS_E3_STORAGE")==0){
				List<HashMap> listID = (List<HashMap>)id;
				//GlobalFun.debugOut("start update status,time:"+(new Date()).toString());
				for(int i=0;i<listID.size();i++){
					dbop.updateStatusMember(tbname, (String)listID.get(i).get("id"), 2,"sysdate", "", null);
				}
				//GlobalFun.debugOut("end update status,time:"+(new Date()).toString());
			}else if (null != orgInfo && tbname.compareTo("BOS_E3_LOCK")==0){
				orgInfo.put("IS_ALLREAD", "Y");
				dbop.addDataMember("E3_BOS_LOCKCFM", orgInfo);
				dbop.updateStatusMember(tbname, (String)id, 2,"sysdate", "", null);
			}else{
				dbop.updateStatusMember(tbname, (String)id, 2,"sysdate", "", null);
			}
		}else{
			GlobalFun.debugOut("failed:"+resp);
			//GlobalFun.debugOut("Send data: "+data);
			String msg=null;
			List<HashMap> listSku = new ArrayList<HashMap>();
			try{
				if (null != json){
					msg = json.getString("message");
            		JSONArray items = json.getJSONArray("data");
            		if (null != items && items.size()>0){
            			for(int i=0;i<items.size();i++){
	            			JSONObject jsonItem = items.getJSONObject(i);
	            			if (null == msg){
	            				try{
	            					msg = jsonItem.getString("message");
	            				}catch(Exception e){
	            				}
	            			}
	            			String skuCode = jsonItem.getString("skuCode");
	            			if (null != skuCode && !skuCode.isEmpty()){
		            			HashMap newSku = new HashMap();
		            			newSku.put("skuCode",skuCode);
		            			try{
		            				newSku.put("msg",jsonItem.getString("message"));
		            			}catch(Exception e){
		            			}
		            			try{
			            			newSku.put("quantity",jsonItem.getString("quantity"));
		            			}catch(Exception e){
		            			}
		            			try{
			            			newSku.put("lockQuantity",jsonItem.getString("lockQuantity"));
		            			}catch(Exception e){
		            			}
		            			listSku.add(newSku);
	            			}
            			}
            		}
					//GlobalFun.debugOut("E3 return: "+resp);
				}
			}catch(Exception e){
				e.printStackTrace();
				//GlobalFun.debugOut("E3 return: "+resp);
			}
			if (null!=msg && msg.length()>499){
				msg = msg.substring(0,498);
			}
			
			String needWriteTime="sysdate";
			if (bNeedResend && null != lastReadTime){
				//通过计算上次发送的时间与当前时间的间隔获取发送次数
				Date cur = new Date();
				long splite = cur.getTime() - lastReadTime.getTime();
				if (splite/spliteTime >= tryTimes)
					bNeedResend = false;
				else
					needWriteTime = null;
			}
			if (tbname.compareTo("BOS_E3_STORAGE")==0){
				//根据SKU判断哪个ID失败
				List<HashMap> listID = (List<HashMap>)id;
				for(int i=0;i<listID.size();i++){
					int iStatus = 2;
					HashMap hmID = listID.get(i);
					msg="";
					String sku = (String)hmID.get("sku");
					if (null != sku && !sku.isEmpty()){
						for(int j=0;j<listSku.size();j++){
							if (0 == sku.compareTo((String)listSku.get(j).get("skuCode"))){
								//if (bNeedResend)
								//	iStatus = 1;
								//else
									iStatus = 3;
								msg = (String)listSku.get(j).get("msg");
								break;
							}
						}
					}
					if (null!=msg && msg.length()>499){
						msg = msg.substring(0,498);
					}
					dbop.updateStatusMember(tbname, (String)hmID.get("id"), iStatus,needWriteTime, msg, null);
				}
			}else if (null != orgInfo && tbname.compareTo("BOS_E3_LOCK")==0){
				for(int j=0;j<listSku.size();j++){
					HashMap hmTemp= new HashMap(orgInfo);
					hmTemp.put("IS_ALLREAD", "N");
					hmTemp.put("SKUCODE",listSku.get(j).get("skuCode"));
					hmTemp.put("QTY",listSku.get(j).get("lockQuantity"));
					dbop.addDataMember("E3_BOS_LOCKCFM", hmTemp);
				}
				//if (bNeedResend)
				//	dbop.updateStatusMember(tbname, (String)id, 1,needWriteTime, msg, null);
				//else
					dbop.updateStatusMember(tbname, (String)id, 3,needWriteTime, msg, null);
			}else{
				//if (bNeedResend)
				//	dbop.updateStatusMember(tbname, (String)id, 1,needWriteTime, msg, null);
				//else
					dbop.updateStatusMember(tbname, (String)id, 3,needWriteTime, msg, null);
			}
		}
	}
	
	/*
	 * 奇门接口
	 */
	
	//单表读取并发送给奇门
	public void BOS2QIMEN(){
		final String[] tablesBOS_QM={
			"BOS_E3_SKU"
		};
		final String[][] fieldsBOS_QM={
			{"ITEMCODE","ITEMCODE","GOODSCODE","ITEMNAME","BARCODE","COLOR","SIZE_CODE","ITEMTYPE","TAGPRICE","RETAILPRICE","COSTPRICE","PURCHASEPRICE","SEASONCODE","SEASONNAME","BRANDCODE","BRANDNAME"}
		};
		final String[][] fieldsBOS_QM_ext={
			{"SIZEGROUPID","SIZEGRPUPNAME"}
		};
		final String[] nameQMAPI={
			"taobao.qimen.items.synchronize"
		};
		final String[][] fieldsQMAPI={
			{"itemCode","itemId","goodsCode","itemName","barCode","color","size","itemType","tagPrice","retailPrice","costPrice","purchasePrice","seasonCode","seasonName","brandCode","brandName"}
		};
		final String[][] fieldsQMAPI_ext={
			{"sizeGroupId","sizeGroupName"}
		};
		final String[] beforeFieldsQMAPI={
			"<actionType>add</actionType><warehouseCode>OTHER</warehouseCode><ownerCode>OTHER</ownerCode><items><item><supplierCode></supplierCode><supplierName></supplierName>"
		};
		final String[] appendFieldsQMAPI={
			"</item></items>"
		};
		int num = tablesBOS_QM.length;
		for(int i=0;i<num;i++){
			while(true){
				List<HashMap> list = dbop.getSendListMember(tablesBOS_QM[i]);
				if (null == list){
					GlobalFun.debugOut("从表"+tablesBOS_QM[i]+"读取发送数据失败"+";time:"+(new Date()).toString());					
				}else{
					GlobalFun.debugOut("从表"+tablesBOS_QM[i]+"读取到"+Integer.toString(list.size())+"条数据需要发送;time:"+(new Date()).toString());
				}
				if (null != list && list.size()>0){
					for(int j =0;j<list.size();j++){
						HashMap hmItem = list.get(j);
						String id = null;
						try{
							id = hmItem.get("ID").toString();
						}catch(Exception e){
							e.printStackTrace();
						}
						if (null == id){
							GlobalFun.debugOut("表"+tablesBOS_QM[i]+"中有ID为NULL");
							continue;
						}
						int apiFlag = getApiFlag(hmItem);
						
						StringBuilder jsonBuilder = new StringBuilder();
						jsonBuilder.append(beforeFieldsQMAPI[i]);
						for(int k=0;k<fieldsQMAPI[i].length;k++){
							jsonBuilder.append("<");
							jsonBuilder.append(fieldsQMAPI[i][k]);
							jsonBuilder.append(">");
							String value="";
							Object fieldValue = hmItem.get(fieldsBOS_QM[i][k]);
							if (null != fieldValue){
								if (fieldValue instanceof Date){
									value = df.format((Date)fieldValue);
								}else{
									try{
										value = fieldValue.toString();
									}catch(Exception e){
										e.printStackTrace();
									}
								}
							}
							jsonBuilder.append(value);
							jsonBuilder.append("</");
							jsonBuilder.append(fieldsQMAPI[i][k]);
							jsonBuilder.append(">");
						}
						if (fieldsQMAPI_ext[i].length > 0){
							jsonBuilder.append("<extendProps>");
							for(int k=0;k<fieldsQMAPI_ext[i].length;k++){
								jsonBuilder.append("<");
								jsonBuilder.append(fieldsQMAPI_ext[i][k]);
								jsonBuilder.append(">");
								String value="";
								Object fieldValue = hmItem.get(fieldsBOS_QM_ext[i][k]);
								if (null != fieldValue){
									if (fieldValue instanceof Date){
										value = df.format((Date)fieldValue);
									}else{
										try{
											value = fieldValue.toString();
										}catch(Exception e){
											e.printStackTrace();
										}
									}
								}
								jsonBuilder.append(value);
								jsonBuilder.append("</");
								jsonBuilder.append(fieldsQMAPI_ext[i][k]);
								jsonBuilder.append(">");
							}
							jsonBuilder.append("</extendProps>");
						}
						jsonBuilder.append(appendFieldsQMAPI[i]);
						
						CallQMAPI(nameQMAPI[i],jsonBuilder.toString(),tablesBOS_QM[i],id,null,apiFlag);
					}
					GlobalFun.debugOut("表"+tablesBOS_QM[i]+"数据发送结束;time:"+(new Date()).toString());
				}else{
					break;
				}
			}
		}
	}
	
	//3.1	采购单创建接口
	public void BOS2QIMEN_PURCHASE(){
		while(true){
			List<HashMap> list = dbop.getSendListMember("BOS_E3_PURCHASE");
			if (null == list){
				GlobalFun.debugOut("从表BOS_E3_PURCHASE读取发送数据失败"+";time:"+(new Date()).toString());					
			}else{
				GlobalFun.debugOut("从表BOS_E3_PURCHASE读取到"+Integer.toString(list.size())+"条数据需要发送;time:"+(new Date()).toString());
			}
			if (null != list && list.size()>0){
				for(int j =0;j<list.size();j++){
					HashMap hmItem = list.get(j);
					String orderCode = null;
					try{
						orderCode = (String)hmItem.get("ENTRYORDERCODE");
					}catch(Exception e){
						e.printStackTrace();
					}
					if (null == orderCode){
						GlobalFun.debugOut("表BOS_E3_PURCHASE中有ENTRYORDERCODE值为NULL");
						continue;
					}
					
					List<HashMap> listDetail = dbop.getSendSubListMember("BOS_E3_PURCHASE", orderCode);
					if (null == listDetail || listDetail.size()< 1){
						GlobalFun.debugOut("表BOS_E3_PURCHASE数据中ENTRYORDERCODE为"+orderCode+"没有明细数据");
						dbop.updateStatus2Member("BOS_E3_PURCHASE", "ENTRYORDERCODE",orderCode, 3,"sysdate", "没有明细数据", null);
						continue;
					}
					int apiFlag = getApiFlag(hmItem);
					
					StringBuilder jsonBuilder = new StringBuilder();
					jsonBuilder.append("<entryOrder><totalOrderLines>");
					jsonBuilder.append(listDetail.size());
					jsonBuilder.append("</totalOrderLines>");
					jsonBuilder.append("<entryOrderCode>");
					jsonBuilder.append(hmItem.get("ENTRYORDERCODE"));
					jsonBuilder.append("</entryOrderCode>");
					jsonBuilder.append("<ownerCode>OTHER</ownerCode>");
					jsonBuilder.append("<warehouseCode>");
					jsonBuilder.append(hmItem.get("WAREHOUSECODE"));
					jsonBuilder.append("</warehouseCode>");
					jsonBuilder.append("<orderType>");
					//jsonBuilder.append("CGRK");
					jsonBuilder.append(hmItem.get("ORDERTYPE"));
					jsonBuilder.append("</orderType>");
					jsonBuilder.append("<expectStartTime>");
					jsonBuilder.append(df.format((Date)hmItem.get("EXPECTSTARTTIME")));
					jsonBuilder.append("</expectStartTime>");
					jsonBuilder.append("<supplierCode>");
					jsonBuilder.append(hmItem.get("SUPPLIERCODE"));
					jsonBuilder.append("</supplierCode>");
					jsonBuilder.append("<operatorName>");
					jsonBuilder.append(null==hmItem.get("OPERATORNAME")?"":hmItem.get("OPERATORNAME"));
					jsonBuilder.append("</operatorName>");
					jsonBuilder.append("<remark>");
					jsonBuilder.append(null==hmItem.get("DESCRIPT")?"":hmItem.get("DESCRIPT"));
					jsonBuilder.append("</remark>");
					jsonBuilder.append("<senderInfo>");
					jsonBuilder.append("<name>");
					jsonBuilder.append(hmItem.get("NAME"));
					jsonBuilder.append("</name>");
					jsonBuilder.append("<mobile>");
					jsonBuilder.append(hmItem.get("MOBILE"));
					jsonBuilder.append("</mobile>");
					jsonBuilder.append("<province>");
					jsonBuilder.append(hmItem.get("PROVINCE"));
					jsonBuilder.append("</province>");
					jsonBuilder.append("<city>");
					jsonBuilder.append(hmItem.get("CITY"));
					jsonBuilder.append("</city>");
					jsonBuilder.append("<detailAddress>");
					jsonBuilder.append(hmItem.get("DETAILADDRESS"));
					jsonBuilder.append("</detailAddress>");
					jsonBuilder.append("</senderInfo>");
					jsonBuilder.append("</entryOrder>");
					jsonBuilder.append("<orderLines>");
					for(int k =0;k<listDetail.size();k++){
						HashMap hmItemDetail = listDetail.get(k);
						jsonBuilder.append("<orderLine>");
						jsonBuilder.append("<orderLineNo>");
						jsonBuilder.append(k+1);
						jsonBuilder.append("</orderLineNo>");
						jsonBuilder.append("<ownerCode>OTHER</ownerCode>");
						jsonBuilder.append("<itemCode>");
						jsonBuilder.append(hmItemDetail.get("ITEMCODE"));
						jsonBuilder.append("</itemCode>");
						jsonBuilder.append("<planQty>");
						jsonBuilder.append(hmItemDetail.get("PLANQTY"));
						jsonBuilder.append("</planQty>");
						jsonBuilder.append("<purchasePrice>");
						jsonBuilder.append(hmItemDetail.get("PRICE"));
						jsonBuilder.append("</purchasePrice>");
						jsonBuilder.append("</orderLine>");
					}				
					jsonBuilder.append("</orderLines>");
					
					CallQMAPI("taobao.qimen.entryorder.create",jsonBuilder.toString(),"BOS_E3_PURCHASE",orderCode,"ENTRYORDERCODE",apiFlag);
				}
				GlobalFun.debugOut("表BOS_E3_PURCHASE数据发送结束;time:"+(new Date()).toString());
			}else{
				break;
			}
		}
	}

	//3.2	采购退货单创建接口
	//8.1	批发通知单创建
	public void BOS2QIMEN_STOCKOUT(){
		final String[] tableNames={
				"BOS_E3_RETPUR",
				"BOS_E3_SALE"
		};
		final String[] keyNames={
			"DELIVERYORDERCODE",
			"DELIVERYORDERCODE"
		};
		final String[] orderTypes={
			"CGTH",
			"B2BCK"
		};
		for(int i=0;i<tableNames.length;i++){
			while(true){
				List<HashMap> list = dbop.getSendListMember(tableNames[i]);
				if (null == list){
					GlobalFun.debugOut("从表"+tableNames[i]+"读取发送数据失败"+";time:"+(new Date()).toString());					
				}else{
					GlobalFun.debugOut("从表"+tableNames[i]+"读取到"+Integer.toString(list.size())+"条数据需要发送;time:"+(new Date()).toString());
				}
				if (null != list && list.size()>0){
					for(int j =0;j<list.size();j++){
						HashMap hmItem = list.get(j);
						String orderCode = null;
						try{
							orderCode = (String)hmItem.get(keyNames[i]);
						}catch(Exception e){
							e.printStackTrace();
						}
						if (null == orderCode){
							GlobalFun.debugOut("表"+tableNames[i]+"中有"+keyNames[i]+"值为NULL");
							continue;
						}
						
						List<HashMap> listDetail = dbop.getSendSubListMember(tableNames[i], orderCode);
						if (null == listDetail || listDetail.size()< 1){
							GlobalFun.debugOut("表"+tableNames[i]+"数据中"+keyNames[i]+"为"+orderCode+"没有明细数据");
							dbop.updateStatus2Member(tableNames[i], keyNames[i],orderCode, 3,"sysdate", "没有明细数据", null);
							continue;
						}
						int apiFlag = getApiFlag(hmItem);
						
						StringBuilder jsonBuilder = new StringBuilder();
						jsonBuilder.append("<deliveryOrder><totalOrderLines>");
						jsonBuilder.append(listDetail.size());
						jsonBuilder.append("</totalOrderLines>");
						jsonBuilder.append("<deliveryOrderCode>");
						jsonBuilder.append(hmItem.get("DELIVERYORDERCODE"));
						jsonBuilder.append("</deliveryOrderCode>");
						jsonBuilder.append("<orderType>");
						//jsonBuilder.append(orderTypes[i]);
						jsonBuilder.append(hmItem.get("ORDERTYPE"));
						jsonBuilder.append("</orderType>");
						jsonBuilder.append("<warehouseCode>");
						jsonBuilder.append(hmItem.get("WAREHOUSECODE"));
						jsonBuilder.append("</warehouseCode>");
						jsonBuilder.append("<createTime>");
						jsonBuilder.append(df.format((Date)hmItem.get("CREATETIME")));
						jsonBuilder.append("</createTime>");
						jsonBuilder.append("<supplierCode>");
						jsonBuilder.append(null==hmItem.get("SUPPLIERCODE")?"":hmItem.get("SUPPLIERCODE"));
						jsonBuilder.append("</supplierCode>");
						jsonBuilder.append("<remark>");
						jsonBuilder.append(null==hmItem.get("DESCRIPT")?"":hmItem.get("DESCRIPT"));
						jsonBuilder.append("</remark>");
						jsonBuilder.append("<extendProps><operatorName>");
						jsonBuilder.append(null==hmItem.get("OPERATORNAME")?"":hmItem.get("OPERATORNAME"));
						jsonBuilder.append("</operatorName>");
						jsonBuilder.append("<customerCode>");
						jsonBuilder.append(null==hmItem.get("CUSTOMERCODE")?"":hmItem.get("CUSTOMERCODE"));
						jsonBuilder.append("</customerCode>");
						jsonBuilder.append("</extendProps>");
						jsonBuilder.append("<receiverInfo>");
						jsonBuilder.append("<name>");
						jsonBuilder.append(hmItem.get("NAME"));
						jsonBuilder.append("</name>");
						jsonBuilder.append("<mobile>");
						jsonBuilder.append(hmItem.get("MOBILE"));
						jsonBuilder.append("</mobile>");
						jsonBuilder.append("<province>");
						jsonBuilder.append(hmItem.get("PROVINCE"));
						jsonBuilder.append("</province>");
						jsonBuilder.append("<city>");
						jsonBuilder.append(hmItem.get("CITY"));
						jsonBuilder.append("</city>");
						jsonBuilder.append("<detailAddress>");
						jsonBuilder.append(hmItem.get("DETAILADDRESS"));
						jsonBuilder.append("</detailAddress>");
						jsonBuilder.append("</receiverInfo>");
						jsonBuilder.append("</deliveryOrder>");
						jsonBuilder.append("<orderLines>");
						for(int k =0;k<listDetail.size();k++){
							HashMap hmItemDetail = listDetail.get(k);
							jsonBuilder.append("<orderLine>");
							jsonBuilder.append("<orderLineNo>");
							jsonBuilder.append(k+1);
							jsonBuilder.append("</orderLineNo>");
							jsonBuilder.append("<ownerCode>");
							jsonBuilder.append(hmItem.get("OWNERCODE"));
							jsonBuilder.append("</ownerCode>");
							jsonBuilder.append("<itemCode>");
							jsonBuilder.append(hmItemDetail.get("ITEMCODE"));
							jsonBuilder.append("</itemCode>");
							jsonBuilder.append("<itemId>");
							jsonBuilder.append(hmItemDetail.get("ITEMCODE"));
							jsonBuilder.append("</itemId>");
							jsonBuilder.append("<planQty>");
							jsonBuilder.append(hmItemDetail.get("PLANQTY"));
							jsonBuilder.append("</planQty>");
							jsonBuilder.append("<extendProps><price>");
							jsonBuilder.append(null==hmItemDetail.get("PRICE")?"":hmItemDetail.get("PRICE"));
							jsonBuilder.append("</price></extendProps>");
							jsonBuilder.append("</orderLine>");
						}				
						jsonBuilder.append("</orderLines>");
						
						CallQMAPI("taobao.qimen.stockout.create",jsonBuilder.toString(),tableNames[i],orderCode,keyNames[i],apiFlag);
					}
					GlobalFun.debugOut("表"+tableNames[i]+"数据发送结束;time:"+(new Date()).toString());
				}else{
					break;
				}
			}
		}
	}

	//6.3	BOS创建调拨单（已出未入）
	public void BOS2QIMEN_TRANSFER(){
		while(true){
			List<HashMap> list = dbop.getSendListMember("BOS_E3_TRANSFER2");
			if (null == list){
				GlobalFun.debugOut("从表BOS_E3_TRANSFER(TYPE=N)读取发送数据失败"+";time:"+(new Date()).toString());					
			}else{
				GlobalFun.debugOut("从表BOS_E3_TRANSFER(TYPE=N)读取到"+Integer.toString(list.size())+"条数据需要发送;time:"+(new Date()).toString());
			}
			if (null != list && list.size()>0){
				for(int j =0;j<list.size();j++){
					HashMap hmItem = list.get(j);
					Long id = null;
					try{
						id = Long.valueOf(hmItem.get("ID").toString());
					}catch(Exception e){
						e.printStackTrace();
					}
					if (null == id){
						GlobalFun.debugOut("表BOS_E3_TRANSFER(TYPE=N)数据中有ID为NULL");
						continue;
					}
					int apiFlag = getApiFlag(hmItem);
					
					List<HashMap> listDetail = dbop.getSendSubListMember("BOS_E3_TRANSFERITEM", id);
					if (null == listDetail || listDetail.size()< 1){
						GlobalFun.debugOut("表BOS_E3_TRANSFER(TYPE=N)数据中ID为"+Long.toString(id)+"没有明细数据");
						dbop.updateStatusMember("BOS_E3_TRANSFER", Long.toString(id), 3,"sysdate", "没有明细数据", null);
						continue;
					}
					
					StringBuilder jsonBuilder = new StringBuilder();
					jsonBuilder.append("<entryOrder><totalOrderLines>");
					jsonBuilder.append(listDetail.size());
					jsonBuilder.append("</totalOrderLines>");
					jsonBuilder.append("<entryOrderCode>");
					jsonBuilder.append(hmItem.get("WMSBILLCODE"));
					jsonBuilder.append("</entryOrderCode>");
					jsonBuilder.append("<ownerCode>OTHER</ownerCode>");
					jsonBuilder.append("<warehouseCode>");
					jsonBuilder.append(hmItem.get("INWAREHOUSECODE"));
					jsonBuilder.append("</warehouseCode>");
					jsonBuilder.append("<orderType>DBRK</orderType>");
					jsonBuilder.append("<expectStartTime>");
					jsonBuilder.append(null==hmItem.get("BILLDATE")?"":df.format((Date)hmItem.get("BILLDATE")));
					jsonBuilder.append("</expectStartTime>");
					jsonBuilder.append("<supplierCode>FLUX1</supplierCode>");
					jsonBuilder.append("<operatorName>");
					jsonBuilder.append(hmItem.get("CREATEEMP"));
					jsonBuilder.append("</operatorName>");
					jsonBuilder.append("<remark>");
					jsonBuilder.append(null==hmItem.get("DESCRIPT")?"":hmItem.get("DESCRIPT"));
					jsonBuilder.append("</remark>");
					jsonBuilder.append("</entryOrder>");
					jsonBuilder.append("<orderLines>");
					for(int k =0;k<listDetail.size();k++){
						HashMap hmItemDetail = listDetail.get(k);
						jsonBuilder.append("<orderLine>");
						jsonBuilder.append("<orderLineNo>");
						jsonBuilder.append(k+1);
						jsonBuilder.append("</orderLineNo>");
						jsonBuilder.append("<ownerCode>OTHER</ownerCode>");
						jsonBuilder.append("<itemCode>");
						jsonBuilder.append(hmItemDetail.get("SKUCODE"));
						jsonBuilder.append("</itemCode>");
						jsonBuilder.append("<planQty>");
						jsonBuilder.append(hmItemDetail.get("NORMALQUANTITY"));
						jsonBuilder.append("</planQty>");
						jsonBuilder.append("</orderLine>");
					}				
					jsonBuilder.append("</orderLines>");
					
					CallQMAPI("taobao.qimen.entryorder.create",jsonBuilder.toString(),"BOS_E3_TRANSFER",id.toString(),"ID",apiFlag);
				}
				GlobalFun.debugOut("表BOS_E3_TRANSFER(TYPE=N)数据发送结束;time:"+(new Date()).toString());
			}else{
				break;
			}
		}
	}

	//8.3	 批发退货通知单创建
	public void BOS2QIMEN_RETSALE(){
		while(true){
			List<HashMap> list = dbop.getSendListMember("BOS_E3_RETSALE");
			if (null == list){
				GlobalFun.debugOut("从表BOS_E3_RETSALE读取发送数据失败"+";time:"+(new Date()).toString());					
			}else{
				GlobalFun.debugOut("从表BOS_E3_RETSALE读取到"+Integer.toString(list.size())+"条数据需要发送;time:"+(new Date()).toString());
			}
			if (null != list && list.size()>0){
				for(int j =0;j<list.size();j++){
					HashMap hmItem = list.get(j);
					String orderCode = null;
					try{
						orderCode = (String)hmItem.get("ENTRYORDERCODE");
					}catch(Exception e){
						e.printStackTrace();
					}
					if (null == orderCode){
						GlobalFun.debugOut("表BOS_E3_RETSALE数据中有ENTRYORDERCODE为NULL");
						continue;
					}
					
					List<HashMap> listDetail = dbop.getSendSubListMember("BOS_E3_RETSALE", orderCode);
					if (null == listDetail || listDetail.size()< 1){
						GlobalFun.debugOut("表BOS_E3_RETSALE数据中ENTRYORDERCODE为"+orderCode+"没有明细数据");
						dbop.updateStatus2Member("BOS_E3_RETSALE", "ENTRYORDERCODE",orderCode, 3,"sysdate", "没有明细数据", null);
						continue;
					}
					int apiFlag = getApiFlag(hmItem);
					
					StringBuilder jsonBuilder = new StringBuilder();
					jsonBuilder.append("<entryOrder><totalOrderLines>");
					jsonBuilder.append(listDetail.size());
					jsonBuilder.append("</totalOrderLines>");
					jsonBuilder.append("<entryOrderCode>");
					jsonBuilder.append(hmItem.get("ENTRYORDERCODE"));
					jsonBuilder.append("</entryOrderCode>");
					jsonBuilder.append("<ownerCode>OTHER</ownerCode>");
					jsonBuilder.append("<warehouseCode>");
					jsonBuilder.append(hmItem.get("WAREHOUSECODE"));
					jsonBuilder.append("</warehouseCode>");
					jsonBuilder.append("<orderType>");
					//jsonBuilder.append("B2BRK");
					jsonBuilder.append(hmItem.get("ORDERTYPE"));
					jsonBuilder.append("</orderType>");
					jsonBuilder.append("<expectStartTime>");
					jsonBuilder.append(df.format((Date)hmItem.get("EXPECTSTARTTIME")));
					jsonBuilder.append("</expectStartTime>");
					jsonBuilder.append("<operatorName>");
					jsonBuilder.append(null==hmItem.get("OPERATORNAME")?"":hmItem.get("OPERATORNAME"));
					jsonBuilder.append("</operatorName>");
					jsonBuilder.append("<remark>");
					jsonBuilder.append(null==hmItem.get("DESCRIPT")?"":hmItem.get("DESCRIPT"));
					jsonBuilder.append("</remark>");
					jsonBuilder.append("<senderInfo>");
					jsonBuilder.append("<name>");
					jsonBuilder.append(hmItem.get("NAME"));
					jsonBuilder.append("</name>");
					jsonBuilder.append("<mobile>");
					jsonBuilder.append(hmItem.get("MOBILE"));
					jsonBuilder.append("</mobile>");
					jsonBuilder.append("<province>");
					jsonBuilder.append(hmItem.get("PROVINCE"));
					jsonBuilder.append("</province>");
					jsonBuilder.append("<city>");
					jsonBuilder.append(hmItem.get("CITY"));
					jsonBuilder.append("</city>");
					jsonBuilder.append("<detailAddress>");
					jsonBuilder.append(hmItem.get("DETAILADDRESS"));
					jsonBuilder.append("</detailAddress>");
					jsonBuilder.append("</senderInfo>");
					jsonBuilder.append("<extendProps><customerCode>");
					jsonBuilder.append(hmItem.get("CUSTOMERCODE"));
					jsonBuilder.append("</customerCode></extendProps></entryOrder>");
					jsonBuilder.append("<orderLines>");
					for(int k =0;k<listDetail.size();k++){
						HashMap hmItemDetail = listDetail.get(k);
						jsonBuilder.append("<orderLine>");
						jsonBuilder.append("<orderLineNo>");
						jsonBuilder.append(k+1);
						jsonBuilder.append("</orderLineNo>");
						jsonBuilder.append("<ownerCode>OTHER</ownerCode>");
						jsonBuilder.append("<itemCode>");
						jsonBuilder.append(hmItemDetail.get("ITEMCODE"));
						jsonBuilder.append("</itemCode>");
						jsonBuilder.append("<planQty>");
						jsonBuilder.append(hmItemDetail.get("PLANQTY"));
						jsonBuilder.append("</planQty>");
						jsonBuilder.append("<purchasePrice>");
						jsonBuilder.append(hmItemDetail.get("PRICE"));
						jsonBuilder.append("</purchasePrice>");
						jsonBuilder.append("</orderLine>");
					}				
					jsonBuilder.append("</orderLines>");
					
					CallQMAPI("taobao.qimen.entryorder.create",jsonBuilder.toString(),"BOS_E3_RETSALE",orderCode,"ENTRYORDERCODE",apiFlag);
				}
				list.clear();
				GlobalFun.debugOut("表BOS_E3_RETSALE数据发送结束;time:"+(new Date()).toString());
			}else{
				break;
			}
		}
	}
	
	public void CallQMAPI(String apiName,String data,String tbname,String id,String keyName,int apiFlag){
		try{
			data = new String(data.getBytes("UTF-8"),"UTF-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		String xml="<?xml version=\"1.0\" encoding=\"utf-8\"?><request>";
		xml += data;
		xml += "</request>";
/*	
		//md5计算
		final String customerId = "c1470792410866";
		final String ver = "2.0";
		
		//正式环境
		String app_key="12585822";
		String secret="1fc88e271082618023b4db126f76426e";
		String url="http://qimen.api.taobao.com/router/qimen/service?";

		if (isTest){
			//测试环境
			app_key="1012585822";
			secret="sandbox71082618023b4db126f76426e";
			url="http://qimenapi.tbsandbox.com/router/qimen/service?";			
		}
*/		
		SimpleDateFormat dfTemp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String curTime = dfTemp.format(new Date());
		String signString = "";
		if (2 == apiFlag || 3 == apiFlag){
			signString = qimenSecret_2 + "app_key"+qimenAppKey_2+"customerId"+qimenCustomerId_2+
				"formatxmlmethod"+apiName+"sign_methodmd5timestamp"+curTime+
				"v"+qimenVer+xml+qimenSecret;
		}else{
			signString = qimenSecret + "app_key"+qimenAppKey+"customerId"+qimenCustomerId+
					"formatxmlmethod"+apiName+"sign_methodmd5timestamp"+curTime+
					"v"+qimenVer+xml+qimenSecret;
		}
		//GlobalFun.debugOut("奇门签名内容:"+signString);
		String sign = GlobalFun.secritMD5(signString,"UTF-8").toUpperCase();
		//GlobalFun.debugOut("奇门签名结果:"+sign);
		String sendUrl = "";
		if (2 == apiFlag || 3 == apiFlag){
			sendUrl = "method="+apiName+"&timestamp="+curTime + 
				"&format=xml&app_key="+qimenAppKey_2 + "&v="+qimenVer_2+
				"&sign="+sign+"&sign_method=md5&customerId="+qimenCustomerId_2;
		}else{
			sendUrl = "method="+apiName+"&timestamp="+curTime + 
					"&format=xml&app_key="+qimenAppKey + "&v="+qimenVer+
					"&sign="+sign+"&sign_method=md5&customerId="+qimenCustomerId;
		}
		try{
			if (2 == apiFlag || 3 == apiFlag){
				sendUrl=qimenUrl_2 + URLEncoder.encode(sendUrl, "UTF-8");
			}else{
				sendUrl=qimenUrl + URLEncoder.encode(sendUrl, "UTF-8");
			}
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		
		boolean bSuccess = false;
		String msg=null;
		String resp = GlobalFun.doPost(sendUrl,xml,null);
		//GlobalFun.debugOut("奇门URL:"+sendUrl);
		//GlobalFun.debugOut("POST内容:"+xml);
		//GlobalFun.debugOut("奇门返回:"+resp);
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
	            	if (null == msg || msg.isEmpty()){
	            		Element items = rootElt.element("items");
	            		if (null != items){
	                        List<Element> itemList = items.elements("item");
	                        if (null != itemList && itemList.size()>0)
	                        	msg = itemList.get(0).getText();
	            		}
	            	}
	            }
			}catch(Exception e){
				e.printStackTrace();
				msg="qimen return:"+resp;
			}
        }
		if (bSuccess && 3 == apiFlag){
			signString = qimenSecret + "app_key"+qimenAppKey+"customerId"+qimenCustomerId+
					"formatxmlmethod"+apiName+"sign_methodmd5timestamp"+curTime+
					"v"+qimenVer+xml+qimenSecret;
			sign = GlobalFun.secritMD5(signString,"UTF-8").toUpperCase();
			sendUrl = "method="+apiName+"&timestamp="+curTime + 
					"&format=xml&app_key="+qimenAppKey + "&v="+qimenVer+
					"&sign="+sign+"&sign_method=md5&customerId="+qimenCustomerId;
			try{
				sendUrl=qimenUrl + URLEncoder.encode(sendUrl, "UTF-8");
			}catch(Exception e){
				e.printStackTrace();
				return;
			}
			
			bSuccess = false;
			msg=null;
			resp = GlobalFun.doPost(sendUrl,xml,null);
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
		            	if (null == msg || msg.isEmpty()){
		            		Element items = rootElt.element("items");
		            		if (null != items){
		                        List<Element> itemList = items.elements("item");
		                        if (null != itemList && itemList.size()>0)
		                        	msg = itemList.get(0).getText();
		            		}
		            	}
		            }
				}catch(Exception e){
					e.printStackTrace();
					msg="qimen return:"+resp;
				}
	        }
		}
				
		if (bSuccess){
			if (null == keyName || keyName.isEmpty())
				dbop.updateStatusMember(tbname, id, 2,"sysdate", "", null);
			else
				dbop.updateStatus2Member(tbname, keyName,id, 2,"sysdate", "", null);
		}else{
			GlobalFun.debugOut("qimen return:"+resp);
			
			if (null!=msg && msg.length()>499){
				msg = msg.substring(0,498);
			}
			if (null == keyName || keyName.isEmpty())
				dbop.updateStatusMember(tbname, id, 3,"sysdate", msg, null);
			else
				dbop.updateStatus2Member(tbname, keyName,id, 3,"sysdate", msg, null);
		}
	}
	
	/*
	 *云仓接口 
	 */
	
	//云仓订单
	public void BOS2E3yun()
	{
		final String[] fieldsBOS_E3_SO={
			"DOCNO","BILLDATE","C_ORIG_CODE","C_ORIG_NAME","SOURCE",
			"RECEIVER_NAME","PROVINCE","CITY","RECEIVER_DISTRICT","RECEIVER_ADDRESS",
			"RECEIVER_ZIP","RECEIVER_MOBILE","RECEIVER_PHONE"
		};
		final String[] fieldsE3API={
			"djbh","add_time","fhdcbh","fhdcmc","from",
			"consignee","province","city","district","address",
			"zipcode","mobile","tel"
		};
		
		final String[] fieldsBOS_E3_SO_detail={
			"PRODUCTALIAS","PRODUCT","VALUE","QTY","PRICE"
		};
		final String[] fieldsE3API_detail={
			"sku","goods_sn","name","number","price"
		};
		
		while(true){
			List<HashMap> list = dbop.getSendListMember("BOS_E3_SO");
			if (null == list){
				GlobalFun.debugOut("从表BOS_E3_SO读取发送数据失败"+";time:"+(new Date()).toString());					
			}else{
				GlobalFun.debugOut("从表BOS_E3_SO读取到"+Integer.toString(list.size())+"条数据需要发送;time:"+(new Date()).toString());
			}
			if (null != list && list.size()>0){
				for(int j =0;j<list.size();j++){
					HashMap hmItem = list.get(j);
					String id = null;
					try{
						id = hmItem.get("ID").toString();
					}catch(Exception e){
						e.printStackTrace();
					}
					if (null == id){
						GlobalFun.debugOut("表BOS_E3_SO中有ID为NULL");
						continue;
					}
					int apiFlag = getApiFlag(hmItem);
					
					HashMap<String,String> hmParams=new HashMap<String,String>();
					
					for(int k=0;k<fieldsBOS_E3_SO.length;k++){
						String value="";
						Object fieldValue = hmItem.get(fieldsBOS_E3_SO[k]);
						if (null != fieldValue){
							if (fieldValue instanceof Date){
								value = df.format((Date)fieldValue);
							}else{
								try{
									value = fieldValue.toString();
								}catch(Exception e){
									e.printStackTrace();
								}
							}
						}
						hmParams.put(fieldsE3API[k], value);
					}
					
					//明细
					StringBuilder goodsList = new StringBuilder();
					List<HashMap> listDetail = dbop.getSendSubListMember("BOS_E3_SO",id);
					if (null == listDetail || listDetail.size()< 1){
						GlobalFun.debugOut("表BOS_E3_SO数据中ID为"+id+"没有明细数据");
					}else{
						for(int ii =0;ii<listDetail.size();ii++){
							HashMap hmItemDetail = listDetail.get(ii);
							if (goodsList.length()>0){
								goodsList.append(',');
							}
							for(int kk=0;kk < fieldsBOS_E3_SO_detail.length;kk++){
								String value="";
								Object fieldValue = hmItemDetail.get(fieldsBOS_E3_SO_detail[kk]);
								if (null != fieldValue){
									try{
										value = fieldValue.toString();
									}catch(Exception e){
										e.printStackTrace();
									}
								}
								if (kk > 0)
									goodsList.append('|');
								goodsList.append(fieldsE3API_detail[kk]);
								goodsList.append(':');
								goodsList.append(value);
							}
						}
					}
					hmParams.put("goods_list", goodsList.toString());
				
					CallE3yunAPI("Order.UploadOrder",hmParams,"BOS_E3_SO",id,apiFlag);
				}
				GlobalFun.debugOut("从表BOS_E3_SO读取的数据发送结束;time:"+(new Date()).toString());
			}else{
				break;
			}
		}
	}
	
	public void CallE3yunAPI(String serviceName,HashMap<String,String> data,String tbname,String id,int apiFlag){
		HashMap<String,String> params = new HashMap<String,String>(data);
		params.put("service", serviceName);
		
		StringBuilder signString = new StringBuilder();
		if (2 == apiFlag || 3 == apiFlag)
			signString.append(yunSecretKey_2);
		else
			signString.append(yunSecretKey);
		Object[] arrKey = params.keySet().toArray();
		Arrays.sort(arrKey);
		for(Object key : arrKey){
			String value = params.get(key);
			signString.append(key);
			signString.append(value);
		}
		String url = urlE3yun;
		if (2 == apiFlag || 3 == apiFlag){
			signString.append(yunSecretKey_2);
			url = urlE3yun_2;
		}else{
			signString.append(yunSecretKey);
			url = urlE3yun;
		}
			
		String sign = GlobalFun.secritMD5(signString.toString(),"UTF-8").toUpperCase();
		params.put("sign",sign);
		
		JSONObject json = null;
		boolean bSuccess = false;
		String msg=null;
		String resp = GlobalFun.doPost(url,params,null);
		if (null != resp){
			try{
				json = JSONObject.fromObject(resp);
				if (null != json){
					if (json.getInt("ret")==200){
						JSONObject jsonData = json.getJSONObject("data");
						if (null != jsonData){
							if (0 == "0".compareTo(jsonData.getString("code")))
								bSuccess = true;
							msg = jsonData.getString("msg");
						}
					}else{
						msg = json.getString("msg");
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			GlobalFun.debugOut("e3yun url: "+url);
			GlobalFun.debugOut("params: "+params.toString());
		}
		
		if (3 == apiFlag){
			signString = new StringBuilder();
			signString.append(yunSecretKey);
			for(Object key : arrKey){
				String value = params.get(key);
				signString.append(key);
				signString.append(value);
			}
			signString.append(yunSecretKey);
			url = urlE3yun;
			sign = GlobalFun.secritMD5(signString.toString(),"UTF-8").toUpperCase();
			params.put("sign",sign);
			
			json = null;
			bSuccess = false;
			msg=null;
			resp = GlobalFun.doPost(url,params,null);
			if (null != resp){
				try{
					json = JSONObject.fromObject(resp);
					if (null != json){
						if (json.getInt("ret")==200){
							JSONObject jsonData = json.getJSONObject("data");
							if (null != jsonData){
								if (0 == "0".compareTo(jsonData.getString("code")))
									bSuccess = true;
								msg = jsonData.getString("msg");
							}
						}else{
							msg = json.getString("msg");
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				GlobalFun.debugOut("e3yun url: "+url);
				GlobalFun.debugOut("params: "+params.toString());
			}
		}
		
		if (bSuccess){
			dbop.updateStatusYun(tbname, id, 1,"sysdate", "");
		}else{
			GlobalFun.debugOut("gxgyun return: "+resp);
			if (null!=msg && msg.length()>499){
				msg = msg.substring(0,498);
			}
			dbop.updateStatusYun(tbname, id, 2,"sysdate", msg);
		}
	}	
}
