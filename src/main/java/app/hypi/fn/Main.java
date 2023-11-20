package app.hypi.fn;

import io.hypi.arc.base.JSON;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);
  OkHttpClient client = new OkHttpClient.Builder()
      .readTimeout(1, TimeUnit.SECONDS)
      .writeTimeout(1, TimeUnit.SECONDS)
      .callTimeout(1, TimeUnit.SECONDS)
      .build();

  public Object invoke(Map<String, Object> input) throws Exception {
    String apiKey = ofNullable(input.get("env")).filter(v -> v instanceof Map).map(v -> ((Map) v).get("GA4_SECRET")).map(Object::toString).orElseThrow(() -> new IllegalArgumentException("Missing environment variable GA4_SECRET"));
    String measurementId = ofNullable(input.get("args")).map(v -> ((Map) v).get("measurement_id"))
        .or(() -> ofNullable(input.get("env")).filter(v -> v instanceof Map).map(v -> ((Map) v).get("GA4_MEASUREMENT_ID")))
        .map(Object::toString).orElseThrow(() -> new IllegalArgumentException("Missing environment variable GA4_MEASUREMENT_ID and measurement_id not provided in args"));
    Map<String, Object> args = ofNullable(input.get("args")).filter(v -> v instanceof Map).map(v -> (Map) v).orElse(emptyMap());
    boolean isDebug = ofNullable(input.get("args")).filter(v -> v instanceof Map).map(v -> ((Map) v).get("is_debug")).map(Object::toString).map(Boolean::parseBoolean).orElse(false);
    var req = new Request.Builder();
    if ("send-event".equalsIgnoreCase(String.valueOf(args.get("action")))) {
      var url = format(
          "https://www.google-analytics.com%s/mp/collect?api_secret=%s&measurement_id=%s",
          isDebug ? "/debug" : "",
          apiKey,
          measurementId
      );
      req.url(url);
      String accountId = (String) ((Map) input.get("hypi")).get("account_id");
      Map<String, Object> body = new LinkedHashMap<>();
      args.forEach((k, v) -> {
        if ("measurement_id".equalsIgnoreCase(k)) return;
        if ("is_debug".equalsIgnoreCase(k)) return;
        if ("action".equalsIgnoreCase(k)) return;
        body.put(k, v);
      });
      //https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference?client_type=gtag#payload_post_body
      if (ofNullable(body.get("client_id")).filter(v -> !v.toString().isBlank()).isEmpty()) {
        body.put("client_id", "hypi_fn_api_" + accountId);
      }
      body.put("user_id", accountId);
//      if (ofNullable(body.get("timestamp_micros")).filter(v -> !v.toString().isBlank()).isEmpty()) {
//        body.put("timestamp_micros", new LongNode(DateTime.now().getMillis() * 1000));
//      }
      //see
      //https://developers.google.com/analytics/devguides/collection/protocol/ga4/user-properties?client_type=gtag
      //the server code example on that page is a good way to go
      //todo body.put("user_properties", JSON.objNode(Map.of("user_id", reqCtx.getAuth().getAccountId())));
      //log.info("Sending GA event with {}\n{}", url, body);
      return buildResponse(
          client.newCall(req
          .header("Content-Type", "text/plain;charset=UTF-8")
          .post(RequestBody.create(JSON.bytes(body)))
          .build())
      );
    }
    log.info("No valid action provided {}", args);
    throw new UnsupportedOperationException("Missing 'action' parameter, currently supported actions are [send-event]");
  }

  private Object buildResponse(Call call) throws IOException {
    try (var res = call.execute()) {
      var hdrs = new LinkedHashMap<>();
      for (var header : res.headers()) {
        hdrs.put(header.getFirst(), header.getSecond());
      }
      var entity = res.body();
      Map<String, Object> body = null;
      if (entity != null) {
        body = JSON.parse(entity.string());
      }
      Map<String, Object> finalBody = body;
      return new LinkedHashMap<>() {
        {
          put("status", res.code());
          put("headers", hdrs);
          put("body", finalBody);
        }
      };
    }
  }
}
