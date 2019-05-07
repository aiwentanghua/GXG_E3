package wwinfo;
import java.io.*;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

public final class GlobalFun {
    public static boolean isWindows = false;
    private static Logger loggers = Logger.getLogger(GlobalFun.class);
    private static String logFile = null;
    private static String SYS_ERROR_FILENAME = "output.log";

    static{
        String strOS = System.getProperty("os.name");
        if (strOS.indexOf("Windows")>=0){
            isWindows = true;
        }else
            isWindows = false;
    }

    //获取系统WEB-INF真实目录，后面带/
    public static String getWebInfPath(){
        String classFilePath = GlobalFun.class.getResource("GlobalFun.class").toString();
        System.out.println(classFilePath);
        if (classFilePath.startsWith("file:/"))

            classFilePath = classFilePath.substring(6);
        System.out.println(classFilePath);
        try{
            classFilePath = URLDecoder.decode(classFilePath,"utf-8");
            System.out.println(classFilePath);
        }catch(Exception e){
            e.printStackTrace();
        }
        //System.out.println(classFilePath);
        int pos = classFilePath.indexOf("/WEB-INF/");
        String strPath = classFilePath.substring(0,pos+9);
        return "/"+strPath;
    }

    //调试、信息、错误输出
    public static void errorOut(String logFile){
        loggers.error(logFile);
        return;
    }
    public static void warningOut(String strPrint){
        loggers.warn(strPrint);
        return;
    }
    public static void debugOut(String strPrint){
        loggers.info(strPrint);
        return;
    }
    public static String getCrashMessage(Exception ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return writer.toString();
    }
    //写入系统错误文件
    public static synchronized void writeSysError(String strErrorInfo){
        if (logFile == null){
            logFile = getWebInfPath();
            logFile += SYS_ERROR_FILENAME;
        }
        try{
            FileWriter fw = new FileWriter(logFile,true);
            fw.write(strErrorInfo);
            fw.write("\r\n");
            fw.flush();
            fw.close();
        }catch(Exception e){
            GlobalFun.errorOut("operate " + logFile + " file failed");
        }
    }

    //在字符串数据组中查找字符串
    public static int MatchStringArray(String[] strList,String strMatch,boolean bNoCase){
        int iRet = -1;
        if ((null == strMatch) || (strMatch.length()==0))
            return -1;
        if (null == strList || strList.length < 1)
            return -1;
        for(int i=0;i<strList.length;i++){
            if (bNoCase){
                if (strMatch.compareToIgnoreCase(strList[i])==0){
                    iRet = i;
                    break;
                }
            }else{
                if (strMatch.compareTo(strList[i])==0){
                    iRet = i;
                    break;
                }
            }
        }
        return iRet;
    }

    //String 2 Long
    public static long Str2Long(String str){
        long iRet = 0;
        try{
            iRet = Long.valueOf(str);
        }catch(Exception e){
            GlobalFun.warningOut(e.getMessage());
            iRet =0;
        }
        return iRet;
    }

    //String 2 int
    public static int Str2Int(String str){
        int iRet = 0;
        try{
            iRet = Integer.valueOf(str);
        }catch(Exception e){
            GlobalFun.warningOut(e.getMessage());
            iRet =0;
        }
        return iRet;
    }

    //Strign 2 Float
    public static float Str2Float(String str){
        float fRet = 0;
        try{
            fRet = Float.valueOf(str);
        }catch(Exception e){
            GlobalFun.warningOut(e.getMessage());
            fRet =0;
        }
        return fRet;
    }

    //String数组转换为int数组
    public static int[] StrList2IntList(String[] strList){
        if (null == strList)
            return null;
        if (strList.length < 1)
            return null;
        int[] intList = new int[strList.length];
        for(int i=0;i<strList.length;i++){
            intList[i]= Str2Int(strList[i]);
        }
        return intList;
    }

    //验证IP地址
    public static boolean CheckIPAddress(String strIP){
        if (null == strIP)
            return false;
        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        if (!strIP.matches(regex)) {
            return false;
        }
        return true;
    }

    //转换整型IP掩码为String
    public static String TrasMaskInt2String(int iMask){
        if (iMask < 1 || iMask>32)
            return "";
        StringBuffer result=new StringBuffer();
        int iSub[]=new int[4];
        int iTemp = iMask;
        for(int i=0;i<4;i++){
            if (iTemp >= 8){
                iSub[i]=8;
                iTemp -= 8;
            }else{
                iSub[i]=iTemp;
                iTemp = 0;
            }
        }
        for(int i=0;i<4;i++){
            if (i>0)
                result.append('.');
            int iField = (int)Math.pow(2,iSub[i]) - 1;
            result.append(iField);
        }
        return result.toString();
    }

    //把从1970/1/1零点开始的秒数转为String
    public static String Long2TimeStr(long lTime,String strFormat){
        Date time = new Date();
        time.setTime(lTime*1000);
        SimpleDateFormat df;
        if (null != strFormat && strFormat.length()>0)
            df = new SimpleDateFormat(strFormat);
        else
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(time);
    }

    //把时间字符串转换为从1970/1/1的秒数
    public static long TimeStr2Long(String strTime,String strFormat){
        char c = (char)160;
        String strNewTime = strTime.replace(c, ' ');
        SimpleDateFormat df = new SimpleDateFormat(strFormat);
        Date date = null;
        try{
            date = df.parse(strNewTime);
            return date.getTime()/1000;
        } catch (ParseException e) {
            GlobalFun.debugOut(e.getMessage());
            return 0;
        }
    }

    //获取访问者的IP地址
    public static String getRequestIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown"))
            ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown"))
            ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown"))
            ip = request.getRemoteAddr();
        return ip;
    }

    //转换String为HTML，转换特殊字符
    public static String TransStr2Html(String strSource){
        StringBuffer strRet;
        char[] cCharList={' ','&','<','>','"','\''};
        String[] strTransList={"&nbsp;","&amp;","&lt;","&gt;","&quot;","&apos;"};
        int iCharNum = cCharList.length;
        int iStrNum = strTransList.length;
        if (iCharNum != iStrNum){
            errorOut("char num not match string num in TransStr2Html");
            if (iCharNum > iStrNum)
                iCharNum = iStrNum;
        }
        int iSourceLen = strSource.length();
        strRet = new StringBuffer(iSourceLen*2);
        for(int i=0;i<iSourceLen;i++){
            char c = strSource.charAt(i);
            boolean bMatch = false;
            for(int j=0;j<iCharNum;j++){
                if (c == cCharList[j]){
                    strRet.append(strTransList[j]);
                    bMatch = true;
                    break;
                }
            }
            if (!bMatch)
                strRet.append(c);
        }
        return strRet.toString();
    }

    //获取UTF8字符串的字符长度
    public static int GetStringLength(String strInput){
        int iRet=0;
        try{
            iRet = strInput.getBytes("utf8").length;
        }catch (Exception e) {
            GlobalFun.errorOut(e.getMessage());
        }
        return iRet;
    }

    //密码加密
    public static String getSecrit(String orgString){
        return secritMD5(orgString,null);
    }

    //MD5加密
    public static String secritMD5(String orgString,String codename){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (null == codename || codename.isEmpty())
                md.update(orgString.getBytes("UTF-8"));
            else
                md.update(orgString.getBytes(codename));
            byte b[] = md.digest();

            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String doPost(String url, HashMap<String, String> params,String charCode){
        String postData = "";
        for (Entry<String, String> entry : params.entrySet()) {
            if (!postData.isEmpty())
                postData += "&";
            postData += entry.getKey()+"="+entry.getValue();
        }
        return doPostExec(url,postData,charCode,null,null);
    }
    public static String doPost(String url, String body,String charCode){
        return doPostExec(url,body,charCode,null,null);
    }
    public static String doPostExec(String url, String body,String charCode,
                                    String bodyType,HashMap<String,String> header){
        String response = null;
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod(url);
        if (null == charCode || charCode.isEmpty())
            charCode = "utf-8";
        postMethod.getParams().setParameter(
                HttpMethodParams.HTTP_CONTENT_CHARSET, charCode);
        postMethod.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset="+charCode);
        //postMethod.addRequestHeader("Content-Type", "application/json");
        if (null != header){
            for (Entry<String, String> entry : header.entrySet()) {
                postMethod.addRequestHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            GlobalFun.debugOut("post url:"+url);
            GlobalFun.debugOut("body:"+body);

            if (null == bodyType)
                bodyType = "text/plain";	//"text/json"、"text/xml"、"text/html"...
            StringRequestEntity ent = new StringRequestEntity(body,bodyType,charCode);
            postMethod.setRequestEntity(ent);
            client.executeMethod(postMethod);
            int code = postMethod.getStatusCode();
            if (code == HttpStatus.SC_OK) {
                response = postMethod.getResponseBodyAsString();
                //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                //System.out.println(df.format(new Date())+2222);
                GlobalFun.debugOut("return:"+response);
            }else if (code == 301 || code == 302){
                GlobalFun.debugOut("http return code:"+Integer.toString(code));
                //Header[] headers = postMethod.getResponseHeaders();
                Header hd = postMethod.getResponseHeader("Location");
                if (null != hd){
                    String newUrl = hd.getValue();
                    return doPostExec(newUrl,body,charCode,bodyType,header);
                }
            }else{
                GlobalFun.debugOut("http return code:"+Integer.toString(code));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            postMethod.releaseConnection();
        }
        return response;
    }

    public static boolean download(String url, String fileName){
        try {
            HttpClient client = new HttpClient();
            GetMethod httpget = new GetMethod(url);
            client.executeMethod(httpget);
            int code = httpget.getStatusCode();
            if (code == HttpStatus.SC_OK) {
                InputStream is = httpget.getResponseBodyAsStream();
                File file = new File(fileName);
                FileOutputStream fileout = new FileOutputStream(file);

                byte[] buffer=new byte[2048];
                int ch = 0;
                while ((ch = is.read(buffer)) != -1) {
                    fileout.write(buffer,0,ch);
                }
                is.close();
                fileout.flush();
                fileout.close();
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
