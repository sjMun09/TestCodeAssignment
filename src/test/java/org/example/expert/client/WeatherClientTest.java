package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Method;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class WeatherClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(restTemplateBuilder.build()).thenReturn(restTemplate); // RestTemplateBuilder 모킹
        weatherClient = new WeatherClient(restTemplateBuilder);
    }

    @Test
    void 날씨_데이터를_정상적으로_가져온다() {
        // given
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
        WeatherDto[] mockResponse = {new WeatherDto(today, "맑음")};

        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // when
        String weather = weatherClient.getTodayWeather();

        // then
        assertThat(weather).isEqualTo("맑음");
    }

    @Test
    void API_응답_상태_코드가_정상적이지_않으면_예외가_발생한다() {
        // given
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessageContaining("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: 400 BAD_REQUEST");
    }

    @Test
    void API_응답_본문이_null이면_예외가_발생한다() {
        // given
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터가 없습니다.");
    }

    @Test
    void API_응답_배열이_비어있으면_예외가_발생한다() {
        // given
        WeatherDto[] emptyResponse = {};
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(emptyResponse, HttpStatus.OK));

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터가 없습니다.");
    }

    @Test
    void 오늘_날짜의_날씨_데이터가_없으면_예외가_발생한다() {
        // given
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("MM-dd"));
        WeatherDto[] mockResponse = {new WeatherDto(yesterday, "흐림")};

        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.");
    }

    @Test
    void API_요청_URL이_정상적으로_생성된다() throws Exception {
        // given
        Method method = WeatherClient.class.getDeclaredMethod("buildWeatherApiUri");
        method.setAccessible(true);

        // when
        URI uri = (URI) method.invoke(weatherClient);

        // then
        URI expectedUri = UriComponentsBuilder
                .fromUriString("https://f-api.github.io")
                .path("/f-api/weather.json")
                .encode()
                .build()
                .toUri();
        assertThat(uri).isEqualTo(expectedUri);
    }

    @Test
    void 현재_날짜를_올바르게_반환한다() throws Exception {
        // given
        Method method = WeatherClient.class.getDeclaredMethod("getCurrentDate");
        method.setAccessible(true);

        // when
        String currentDate = (String) method.invoke(weatherClient);

        // then
        String expectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
        assertThat(currentDate).isEqualTo(expectedDate);
    }
}
