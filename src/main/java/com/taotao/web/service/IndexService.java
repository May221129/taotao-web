package com.taotao.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.taotao.common.service.ApiService;

@Service
public class IndexService {

	@Autowired
	private ApiService apiService;
	
	@Value("${TAOTAO_MANAGE_URL}")
	private String TAOTAO_MANAGE_URL;
	
	@Value("${INDEX_AD1_URL}")
	private String INDEX_AD1_URL;
	
	@Value("${INDEX_AD2_URL}")
	private String INDEX_AD2_URL;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * 首页最大广告位的内容查询
	 */
	public String queryIndexAD1() {
		try {
			/*
			 * 1.url怎么来的：
			 * http://manage.taotao.com/rest/content==>通过B端的url来查询到内容.
			 * categoryId=40==>当前的queryIndexAD1()是查询首页最大广告位，40就是该广告位的id.
			 * page=1==>查询结果是按照时间顺序倒序排列的，所以只需要查第一页就能拿到想要的数据。
			 * rows=6==>C端的页面中已经写好了在首页最大广告位只展示6个广告，所以只需要拿到6条记录即可。
			 */
			String url = TAOTAO_MANAGE_URL + INDEX_AD1_URL;
			
			//2.ApiService的doGet()已经封装好了通过HttpClient来实现通过程序来访问别的服务端来请求数据，而非jsp中直接通过url来请求数据。
			String jsonData = this.apiService.doGet(url);
			if (StringUtils.isEmpty(jsonData)) {
				return null;
			}
			
			//3.解析json数据，封装成前端所需要的结构：(反序列化)
			JsonNode jsonNode = MAPPER.readTree(jsonData);
			//3.1 因为rows所对应的是一个数组，在json里数组是ArrayNode，所以就用ArrayNode来接收。
			//我们怎么知道是要获得rows呢？因为通过在浏览器上请求上面的url地址，会返回数据，数据是json格式的，有两个成员：total和rows，而total拿来没用。
			ArrayNode rows = (ArrayNode) jsonNode.get("rows");
			//3.2 现在要将得到的json数据，转为前端所需要的结构（前端需要的结构见：index.jsp中的第52行被注释掉的var data = [……]）
			List<Map<String, Object>> result = new ArrayList<>();
			for(JsonNode row : rows){
				//添加到element中的键值对必须是有序的，所以用LinkedHashMap:
				Map<String, Object> element = new LinkedHashMap<>();
				//将3.1查到的数据，按照3.2要求的格式，放到element中：
				//row.get("pic")拿到的是一个pic对象，而我们要的是pic的值。asText()：作为文本获取它的值。
				element.put("srcB", row.get("pic").asText());
				element.put("height", 240);
				element.put("alt", row.get("title").asText());
				element.put("width", 670);
				element.put("src", row.get("pic2").asText());
				element.put("widthB",550);
				element.put("href", row.get("url").asText());
				element.put("heightB", 240);
				
				result.add(element);
			}
			return MAPPER.writeValueAsString(result);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 首页右上角广告位的内容查询
	 */
	public String queryIndexAD2() {
		try {
			// 1.url:
			String url = TAOTAO_MANAGE_URL + INDEX_AD2_URL;
			
			// 2.通过HTTPClient发请求到B端，获得数据：
			String jsonData = this.apiService.doGet(url);
			if(StringUtils.isEmpty(jsonData)){
				return null;
			}
			
			//3.解析json数据，封装成前端所需要的结构：(反序列化)
			JsonNode jsonNode = MAPPER.readTree(jsonData);
			ArrayNode rows = (ArrayNode) jsonNode.get("rows");
			List<Map<String, Object>> result = new ArrayList<>();
			for(JsonNode row : rows){
				Map<String, Object> element = new LinkedHashMap<>();
				//前端要求的格式：见index.jsp的第223行的var data = [……]
				//B端传来的数据格式通过浏览器发url获得数据后将数据进行json格式化即可知。
				element.put("width", 310);
				element.put("height", 70);
				element.put("src", row.get("pic").asText());
				element.put("href", row.get("url").asText());
				element.put("alt", row.get("title").asText());
				element.put("widthB", 210);
				element.put("heightB", 70);
				element.put("srcB", row.get("pic2").asText());
				
				result.add(element);
			}
			return MAPPER.writeValueAsString(result);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
