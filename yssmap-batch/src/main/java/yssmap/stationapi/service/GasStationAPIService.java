package yssmap.stationapi.service;

import static java.lang.Thread.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import yssmap.main.dto.GasStationDto;
import yssmap.stationapi.domain.ResponseFieldName;
import yssmap.stationapi.exception.ApiResponseException;

@Service
@Transactional(readOnly = true)
public class GasStationAPIService {

	private static final Logger logger = LoggerFactory.getLogger("file");
	public static final int INIT_PAGE = 1;
	public static final int INIT_PER_PAGE = 200;

	private final RestTemplate restTemplate;

	@Value("${dataportal.url}")
	private String baseRequestUrl;

	@Value("${dataportal.servicekey}")
	private String serviceKey;

	@Autowired
	public GasStationAPIService(	RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = initRestTemplate(restTemplateBuilder);
	}

	private RestTemplate initRestTemplate(RestTemplateBuilder restTemplateBuilder) {
		HttpComponentsClientHttpRequestFactory factory = makeRequestFactory(makeHttpClient());
		return restTemplateBuilder.requestFactory(() -> factory).build();
	}

	public List<GasStationDto> fetchStations(int page) {
		return fetchStations(page, INIT_PER_PAGE);
	}

	public List<GasStationDto> fetchStations(int page, int pageSize) {
		Map result = requestAPICall(page, pageSize);
		return convertDataToGasStationEntity(result);
	}

	public int getTotalPage() {
		return getTotalPage(INIT_PER_PAGE);
	}

	public int getTotalPage(int pageSize) {
		return (int)Math.ceil((double)getTotalCount(pageSize) / pageSize);
	}

	protected int getTotalCount(int pageSize) {
		Map result = requestAPICall(INIT_PAGE, pageSize);
		return (Integer)result.get("totalCount");
	}

	protected Map requestAPICall(int page, int pageSize) {
		String requestUrl = getApiPath(page, pageSize);
		System.out.println(requestUrl);
		return callRestApi(requestUrl);
	}

	protected Map callRestApi(String requestUrl) {
		try {
			return restTemplate.exchange(requestUrl, HttpMethod.GET, getHeaders(), Map.class).getBody();
		} catch (HttpClientErrorException ex) {
			/* RestTemplate??? DefaultResponseErrorHandler??? ???????????? ????????? ?????????.
				DefaultResponseErrorHandler??? 4xx, 5xx ????????? ?????? HttpClientErrorException ????????? ????????????
				??? ????????? ????????? ??? ??? ????????? ?????? ??? ?????? ????????? ?????????.
			 */
			logger.error("????????? ??????????????? ???????????? ???????????????.\n???????????? : {} , ??????????????? : {}", ex.getStatusText(), ex.getMessage());
			throw new ApiResponseException(ex);
		} catch (ResourceAccessException ex) {
			// ???????????? ????????? ?????? 5??? ??? ???????????????.
			try {
				sleep(5000); // 5??? ??? ?????????
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// ????????? ???????????? ?????? ?????? ??????
			return restTemplate.exchange(requestUrl, HttpMethod.GET, getHeaders(), Map.class).getBody();
		}
	}

	private HttpComponentsClientHttpRequestFactory makeRequestFactory(HttpClient httpClient) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		factory.setConnectTimeout(10 * 1000);
		factory.setReadTimeout(10 * 1000);
		return factory;
	}

	private HttpClient makeHttpClient() {
		return HttpClientBuilder.create()
			.setMaxConnTotal(100)
			.setMaxConnPerRoute(1) //?????? ???????????? ????????? ????????? ???????????? ???????????? ??? ??? ??????.
			.build();
	}

	private HttpEntity<?> getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		httpHeaders.set(HttpHeaders.AUTHORIZATION, serviceKey);
		return new HttpEntity<>(httpHeaders);
	}

	private String getApiPath(int page, int pageSize) {
		StringBuilder sb = new StringBuilder();
		sb.append(baseRequestUrl)
			.append("?page=").append(page)
			.append("&perPage=").append(pageSize)
			.append("&serviceKey=").append(serviceKey);
		return sb.toString();
	}

	private List<GasStationDto> convertDataToGasStationEntity(Map responseBody) {
		List<Map> extractedData = (ArrayList)responseBody.get("data");

		return extractedData.stream().map(data ->
			new GasStationDto(
				(String)data.get(ResponseFieldName.STATION_CODE.getName()),
				(String)data.get(ResponseFieldName.NAME.getName()),
				(String)data.get(ResponseFieldName.ADDRESS.getName()),
				(String)data.get(ResponseFieldName.TEL_NO.getName()),
				(String)data.get(ResponseFieldName.OPENNING_HOURS.getName()),
				String.valueOf(data.get(ResponseFieldName.STOCSK.getName())),
				(String)data.get(ResponseFieldName.PRICE.getName()),
				Double.parseDouble((String)data.get(ResponseFieldName.LATITUDE.getName())),
				Double.parseDouble((String)data.get(ResponseFieldName.LONGITUDE.getName())),
				convertToLocalDateTime((String)data.get(ResponseFieldName.STD_DT.getName()))
			)).collect(Collectors.toList());
	}

	private static LocalDateTime convertToLocalDateTime(String date) {
		StringBuilder sb = new StringBuilder(date.substring(0, 10)).append("T").append(date.substring(11));
		return LocalDateTime.parse(sb.toString());
	}
}
