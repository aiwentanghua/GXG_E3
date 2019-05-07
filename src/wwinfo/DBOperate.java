package wwinfo;

import java.io.Reader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class DBOperate {
	private static SqlMapClient sqlMapClient = null;	//静态连接
	private SqlMapClient sqlMapClientMember = null;

	// 读取配置文件
	static{
		try {
			Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");

			sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//清除缓存
	public static void flushData(){
		sqlMapClient.flushDataCache();
	}

	//获取要同步的表数据
	public static List<HashMap> getSendList(String tbname){
		List<HashMap> hmList = null;
		try {
			hmList = (List<HashMap>)sqlMapClient.queryForList("get"+tbname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hmList;
	}
	//获取要同步的子表数据
	public static List<HashMap> getSendSubList(String tbname,Long mid){
		List<HashMap> hmList = null;
		try {
			hmList = (List<HashMap>)sqlMapClient.queryForList("get"+tbname,mid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hmList;
	}
	//获取要同步的子表数据
	public static List<HashMap> getSendSubList(String tbname,String code){
		List<HashMap> hmList = null;
		try {
			hmList = (List<HashMap>)sqlMapClient.queryForList("get"+tbname+"_detail",code);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hmList;
	}

	//更新已同步的表状态
	public static boolean updateStatus(String table,String id,int status,String readtime,String errorinfo,String descript){
		boolean bRet = true;
		try{
			HashMap hm = new HashMap();
			hm.put("table",table);
			hm.put("id",id);
			hm.put("status",status);
			if (null != readtime && !readtime.isEmpty())
				hm.put("readtime",readtime);
			if (null != errorinfo)
				hm.put("errorinfo",errorinfo);
			if (null != descript)
				hm.put("descript",descript);
			sqlMapClient.update("upstatus",hm);
			bRet = true;
		} catch (SQLException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}
	//更新已同步的表状态
	public static boolean updateStatus2(String table,String keyName,String id,int status,String readtime,String errorinfo,String descript){
		boolean bRet = true;
		try{
			HashMap hm = new HashMap();
			hm.put("table",table);
			hm.put("keyname",keyName);
			hm.put("id",id);
			hm.put("status",status);
			if (null != readtime && !readtime.isEmpty())
				hm.put("readtime",readtime);
			if (null != errorinfo)
				hm.put("errorinfo",errorinfo);
			if (null != descript && !descript.isEmpty())
				hm.put("descript",descript);
			sqlMapClient.update("upstatus2",hm);
			bRet = true;
		} catch (SQLException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}
	/*
	//开始一个事务
	public static boolean startTransaction(){
		boolean ret = false;
		try{
			sqlMapClient.startTransaction();
			sqlMapClient.startBatch();
			ret = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return ret;
	}
	//提交一个事务
	public static boolean commitTransaction(){
		boolean ret = false;
		try{
			sqlMapClient.executeBatch();
			sqlMapClient.commitTransaction();
			ret = true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				sqlMapClient.endTransaction();
			}catch(Exception e2){
				e2.printStackTrace();
			}
		}
		return ret;
	}
	//取消事务
	public static void endTransaction(){
		try{
			sqlMapClient.endTransaction();
		}catch(Exception e2){
			e2.printStackTrace();
		}
	}
	*/
	//插入收到的数据
	public static boolean addData(String tbname,HashMap hm){
		boolean bRet = true;
		try{
			GlobalFun.debugOut("insert table "+tbname+" data:"+hm.toString());
			sqlMapClient.insert("insert"+tbname,hm);
		} catch (SQLException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}
	//插入收到的数据，并返回ID
	public static long addData2(String tbname,HashMap hm){
		long ret = -1;
		try{
			sqlMapClient.insert("insert"+tbname,hm);
			ret = (Long)sqlMapClient.queryForObject("selectMaxID",tbname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	//批量插入收到的数据
	public static boolean addDataArray(String tbname,List<HashMap> hmList){
		boolean bRet = true;
		try{
			//GlobalFun.debugOut("insert table "+tbname+" data:"+hm.toString());

			sqlMapClient.startTransaction();
			if (null != hmList){
				if (tbname.equals("E3_BOS_STOREPDT")){
					for (int i = 0; i < hmList.size(); i++) {

						HashMap<String,String> map=hmList.get(i);
						String status= map.get("STATUS");

						if(status.equals("0")){
							//新增
							sqlMapClient.insert("insert" + tbname, map);
						}else if (status.equals("2")){
							//删除
							sqlMapClient.delete("updateo2ostorepro",map);
						}else {
							//未知
							bRet=false;
						}
					}
				}else {


					for (int i = 0; i < hmList.size(); i++) {
						sqlMapClient.insert("insert" + tbname, hmList.get(i));
					}
				}
			}
			sqlMapClient.commitTransaction();
		} catch (SQLException e) {

			e.printStackTrace();
			bRet = false;

		}finally{
			try{
				sqlMapClient.endTransaction();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return bRet;
	}

	//修改商品档案
	private boolean updateo2ostorepro(HashMap map){


		return false;
	}
	//同时插入主表数据和子表数据
	public static boolean addDataWithSub(String tbname,HashMap hm,String subTbname,List<HashMap> subList){
		boolean bRet = true;
		try{
			//GlobalFun.debugOut("insert table "+tbname+" data:"+hm.toString());
			sqlMapClient.startTransaction();
			sqlMapClient.insert("insert"+tbname,hm);
			if (null != subList){
				for(int i=0;i<subList.size();i++){
					sqlMapClient.insert("insert"+subTbname,subList.get(i));
				}
			}
			sqlMapClient.commitTransaction();
		} catch (SQLException e) {
			e.printStackTrace();
			bRet = false;
		}finally{
			try{
				sqlMapClient.endTransaction();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return bRet;
	}



	//实例处理
	//================================================================
	DBOperate(){
		try {
			Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
			sqlMapClientMember = SqlMapClientBuilder.buildSqlMapClient(reader);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//清除缓存
	public void flushDataMember(){
		sqlMapClientMember.flushDataCache();
	}

	//获取要同步的表数据
	public List<HashMap> getSendListMember(String tbname){
		if (null == sqlMapClientMember)
			return null;

		List<HashMap> hmList = null;
		try {
			hmList = (List<HashMap>)sqlMapClientMember.queryForList("get"+tbname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hmList;
	}
	//D1M获取优惠卷模版数据
	public List<HashMap> getD1MSendListMember(String tbname,HashMap map){
		if (null == sqlMapClientMember)
			return null;

		List<HashMap> hmList = null;
		try {
			hmList = (List<HashMap>)sqlMapClientMember.queryForList("get"+tbname,map);
		} catch (SQLException e) {
			GlobalFun.errorOut("DBOperate BUG :"+GlobalFun.getCrashMessage(e));
			e.printStackTrace();
			return hmList;
		}
		return hmList;
	}

	//调用存储过程
	public HashMap<String,String> addD1MData(String tbname,HashMap hm){
		//HashMap<String,Object> map=null;
		try{
			GlobalFun.debugOut("procedure name "+tbname+" data:"+hm.toString());

			//hashMapList= (List<HashMap>) sqlMapClient.insert("procedure"+tbname,hm);
			sqlMapClientMember.queryForObject("procedure"+tbname,hm);
		} catch (SQLException e) {
			e.printStackTrace();
			GlobalFun.errorOut("addD1MData BUG :"+GlobalFun.getCrashMessage(e));
			return hm;
		}
		return hm;
	}
	public boolean updateD1Mcoupon(HashMap map,String name){
		if (null == sqlMapClientMember)
			return false;
		boolean bret=false;
		try {
			sqlMapClientMember.update("update"+name,map);
			bret=true;
		}catch (SQLException e){
			e.printStackTrace();
			GlobalFun.errorOut("updateD1Mcoupon BUG :"+GlobalFun.getCrashMessage(e));
			return bret;
		}
		return bret;
	}
	//获取要同步的子表数据
	public List<HashMap> getSendSubListMember(String tbname,Long mid){
		if (null == sqlMapClientMember)
			return null;

		List<HashMap> hmList = null;
		try {
			hmList = (List<HashMap>)sqlMapClientMember.queryForList("get"+tbname,mid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hmList;
	}
	//获取要同步的子表数据
	public List<HashMap> getSendSubListMember(String tbname,String code){
		if (null == sqlMapClientMember)
			return null;

		List<HashMap> hmList = null;
		try {
			hmList = (List<HashMap>)sqlMapClientMember.queryForList("get"+tbname+"_detail",code);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hmList;
	}

	//更新已同步的表状态
	public boolean updateStatusMember(String table,String id,int status,String readtime,String errorinfo,String descript){
		if (null == sqlMapClientMember)
			return false;

		boolean bRet = true;
		try{
			HashMap hm = new HashMap();
			hm.put("table",table);
			hm.put("id",id);
			hm.put("status",status);
			if (null != readtime && !readtime.isEmpty())
				hm.put("readtime",readtime);
			if (null != errorinfo)
				hm.put("errorinfo",errorinfo);
			if (null != descript)
				hm.put("descript",descript);
			sqlMapClientMember.update("upstatus",hm);
			bRet = true;
		} catch (SQLException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}
	//更新已同步的表状态
	public boolean updateStatus2Member(String table,String keyName,String id,int status,String readtime,String errorinfo,String descript){
		if (null == sqlMapClientMember)
			return false;

		boolean bRet = true;
		try{
			HashMap hm = new HashMap();
			hm.put("table",table);
			hm.put("keyname",keyName);
			hm.put("id",id);
			hm.put("status",status);
			if (null != readtime && !readtime.isEmpty())
				hm.put("readtime",readtime);
			if (null != errorinfo)
				hm.put("errorinfo",errorinfo);
			if (null != descript && !descript.isEmpty())
				hm.put("descript",descript);
			sqlMapClientMember.update("upstatus2",hm);
			bRet = true;
		} catch (SQLException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}
	public boolean updateStatusYun(String table,String id,int status,String readtime,String errorinfo){
		if (null == sqlMapClientMember)
			return false;

		boolean bRet = true;
		try{
			HashMap hm = new HashMap();
			hm.put("table",table);
			hm.put("id",id);
			hm.put("status",status);
			if (null != readtime && !readtime.isEmpty())
				hm.put("readtime",readtime);
			if (null != errorinfo)
				hm.put("errorinfo",errorinfo);
			sqlMapClientMember.update("upstatusYun",hm);
			bRet = true;
		} catch (SQLException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}

	//插入收到的数据
	public boolean addDataMember(String tbname,HashMap hm){
		if (null == sqlMapClientMember)
			return false;

		boolean bRet = true;
		try{
			sqlMapClientMember.insert("insert"+tbname,hm);
		} catch (SQLException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}
	//插入收到的数据，并返回ID
	public long addData2Member(String tbname,HashMap hm){
		if (null == sqlMapClientMember)
			return -1;

		long ret = -1;
		try{
			sqlMapClientMember.insert("insert"+tbname,hm);
			ret = (Long)sqlMapClientMember.queryForObject("selectMaxID",tbname);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	//================================================================
}
