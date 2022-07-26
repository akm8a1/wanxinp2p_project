package com.liu.wanxinp2p.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

/**
 * HttpUtil工具类 HttpClient连接通过单例模式实现
 */
class Obj {
    private Integer code;
    private String msg;
    private Map<String,String> result;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, String> getResult() {
        return result;
    }

    public void setResult(Map<String, String> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Obj{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", result=" + result +
                '}';
    }
}
public class HttpClientUitl {
//    public static void main(String[] args){
//        Map<String,String> map = new HashMap<>();
//        map.put("mobile","15524182058");
//        String response = doPostByJson("http://localhost:56085/sailing/generate?effectiveTime=300&name=sms",map);
//        System.out.println(response);
//        JSONObject jsonObject = JSONObject.parseObject(response);
//        System.out.println(jsonObject.get("result"));
//        Obj obj = jsonObject.toJavaObject(Obj.class);
//        System.out.println(obj);
//        StringBuilder sb = new StringBuilder("http://localhost:56085/sailing/verify?name=sms");
//        Map<String,String> map = new HashMap<>();
//        map.put("verificationKey","sms:0b5bb31d78384f56aee4558a754f3bf8");
//        map.put("verificationCode","153242");
//        String s = HttpClientUitl.doPost(sb.toString(),map);
//        System.out.println(s);
//    }
    //连接池
    private static final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
    //HttpClient
    private static CloseableHttpClient client = null;
    //请求参数配置
    private static RequestConfig config = null;;
    static {
        //最多同时连接20个请求
        manager.setMaxTotal(20);
        //每个路由最大连接数(ip+port)
        manager.setDefaultMaxPerRoute(50);
        //设置参数
        config = RequestConfig.custom()
                .setConnectTimeout(5000) //客户端和服务器建立连接的timeout
                .setConnectionRequestTimeout(3000) //从连接池获取连接的timeout
                .setSocketTimeout(10*1000) //读取服务器返回数据的timeout
                .build();
        client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setConnectionManager(manager)
                .disableAutomaticRetries()
                .build();
    }

    /**
     *
     * @param url 请求地址
     * @param params get请求的参数
     * @param encode 请求的编码方式
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String doGet(String url, Map<String,String> params, String encode)  {
        String content = null;
        try {
            URIBuilder builder = new URIBuilder(url);
            if (null != params) {
                Set<String> keys = params.keySet();
                for (String key : keys) {
                    builder.addParameter(key,params.get(key));
                }
            }
            URI uri = builder.build();
            HttpGet get = new HttpGet(uri);
            get.setConfig(config);
            //响应
            HttpResponse response = client.execute(get);
            content = EntityUtils.toString(response.getEntity(),encode);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 默认编码为UTF-8的GET请求处理
     * @param url
     * @param params
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String doGet(String url, Map<String,String> params) {
        return doGet(url,params,"utf-8");
    }

    /**
     *
     * @param url 请求地址
     * @param params get请求的参数
     * @param encode 请求的编码方式
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String doPost(String url, Map<String,String> params, String encode){
        String content = null;
        try {
            URIBuilder builder = new URIBuilder(url);
            if (null != params) {
                Set<String> keys = params.keySet();
                for (String key : keys) {
                    builder.addParameter(key,params.get(key));
                }
            }
            URI uri = builder.build();
            HttpPost post = new HttpPost(uri);
            post.setConfig(config);
            //响应
            HttpResponse response = client.execute(post);
            content = EntityUtils.toString(response.getEntity(),encode);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 默认编码为UTF-8的POST请求处理
     * @param url
     * @param params
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String doPost(String url, Map<String,String> params) {
        return doPost(url,params,"utf-8");
    }

    /**
     * 处理json数据的POST请求
     * @param url
     * @param params
     * @param encode
     * @return
     * @throws IOException
     */
    public static String doPostByJson(String url,Map<String,String> params,String encode) {
        String content = null;
        String jsonString = JSONObject.toJSONString(params);
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type","application/json;charset=" + encode);
        try {
            post.setEntity(new StringEntity(jsonString));
            HttpResponse response = client.execute(post);
            content = EntityUtils.toString(response.getEntity(),encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 默认为utf-8的处理json数据的POST请求
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String doPostByJson(String url,Map<String,String> params) {
        return doPostByJson(url,params,"utf-8");
    }

}
